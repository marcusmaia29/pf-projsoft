package insper.pf_projsoft;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BaseIntegrationTests {
    private static final String ISSUER = "https://issuer.simulado.test/";
    private static final String AUDIENCE = "https://simulado-arqobj-api";
    private static final String EMAIL_CLAIM = "https://social-insper.com/email";
    private static final String ROLES_CLAIM = "https://social-insper.com/roles";
    private static final KeyPair KEYS;
    private static final HttpServer JWKS;
    private final HttpClient client = HttpClient.newHttpClient();

    static {
        try {
            var generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KEYS = generator.generateKeyPair();
            String publicKey = new RSAKey.Builder((RSAPublicKey) KEYS.getPublic())
                    .keyID("integration-test").build().toJSONString();
            byte[] body = ("{\"keys\":[" + publicKey + "]}").getBytes(StandardCharsets.UTF_8);
            JWKS = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            JWKS.createContext("/jwks", exchange -> {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                try (var output = exchange.getResponseBody()) {
                    output.write(body);
                }
            });
            JWKS.start();
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    @DynamicPropertySource
    static void authenticationProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> ISSUER);
        registry.add("spring.security.oauth2.resourceserver.jwt.audiences", () -> AUDIENCE);
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> "http://127.0.0.1:" + JWKS.getAddress().getPort() + "/jwks");
    }

    @AfterAll
    static void stopJwks() {
        JWKS.stop(0);
    }

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate jdbc;

    private String token(String email, List<String> roles) throws Exception {
        return token(email, roles, ISSUER, AUDIENCE, Instant.now().plusSeconds(300), KEYS);
    }

    private String token(String email, List<String> roles, String issuer, String audience,
                         Instant expiresAt, KeyPair keys) throws Exception {
        var claims = new JWTClaimsSet.Builder()
                .issuer(issuer).audience(audience).subject("auth0|integration-test")
                .issueTime(Date.from(Instant.now().minusSeconds(600)))
                .expirationTime(Date.from(expiresAt))
                .claim(EMAIL_CLAIM, email).claim(ROLES_CLAIM, roles).build();
        var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID("integration-test").build(), claims);
        jwt.sign(new RSASSASigner((RSAPrivateKey) keys.getPrivate()));
        return jwt.serialize();
    }

    private HttpResponse<String> request(String method, String path, String token, String body)
            throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path));
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            request.header("Content-Type", "application/json");
        }
        request.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body));
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void healthAndDatabaseAreAvailable() throws Exception {
        var response = request("GET", "/actuator/health", null, null);
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"status\":\"UP\""));
    }

    @Test
    void anonymousCannotAccessProtectedRoutes() throws Exception {
        assertEquals(401, request("GET", "/artigos", null, null).statusCode());
        assertEquals(401, request("DELETE", "/artigos/0", null, null).statusCode());
    }

    @Test
    void userCanListButCannotAccessAdminRoute() throws Exception {
        String jwt = token("user@example.com", List.of());
        assertEquals(200, request("GET", "/artigos", jwt, null).statusCode());
        assertEquals(403, request("DELETE", "/artigos/0", jwt, null).statusCode());
    }

    @Test
    void adminCanAccessAdminRoute() throws Exception {
        String jwt = token("admin@example.com", List.of("ADMIN"));
        assertEquals(200, request("GET", "/artigos", jwt, null).statusCode());
        assertEquals(404, request("DELETE", "/artigos/0", jwt, null).statusCode());
    }

    @Test
    void rejectsWrongIssuer() throws Exception {
        String jwt = token("admin@example.com", List.of("ADMIN"), "https://other-issuer.test/",
                AUDIENCE, Instant.now().plusSeconds(300), KEYS);
        assertEquals(401, request("GET", "/artigos", jwt, null).statusCode());
    }

    @Test
    void rejectsWrongAudience() throws Exception {
        String jwt = token("admin@example.com", List.of("ADMIN"), ISSUER,
                "https://other-api.test", Instant.now().plusSeconds(300), KEYS);
        assertEquals(401, request("GET", "/artigos", jwt, null).statusCode());
    }

    @Test
    void rejectsExpiredToken() throws Exception {
        String jwt = token("admin@example.com", List.of("ADMIN"), ISSUER,
                AUDIENCE, Instant.now().minusSeconds(300), KEYS);
        assertEquals(401, request("GET", "/artigos", jwt, null).statusCode());
    }

    @Test
    void rejectsInvalidSignatureAndMalformedToken() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        String jwt = token("admin@example.com", List.of("ADMIN"), ISSUER,
                AUDIENCE, Instant.now().plusSeconds(300), generator.generateKeyPair());
        assertEquals(401, request("GET", "/artigos", jwt, null).statusCode());
        assertEquals(401, request("GET", "/artigos", "invalid-token", null).statusCode());
    }

}
