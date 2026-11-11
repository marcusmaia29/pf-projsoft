package insper.pf_projsoft.base;

import java.util.Map;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import insper.pf_projsoft.security.WebSecurityConfig;

/** Rotas neutras para verificar a base antes de implementar o enunciado. */
@RestController
public class BaseController {
    @GetMapping("/base/status")
    public Map<String, Object> status(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString(WebSecurityConfig.EMAIL_CLAIM);
        List<String> roles = jwt.getClaimAsStringList(WebSecurityConfig.ROLES_CLAIM);
        return Map.of("status", "ok", "projeto", "pf-projsoft",
                "usuario", email == null ? jwt.getSubject() : email,
                "roles", roles == null ? List.of() : roles);
    }

    @GetMapping("/base/admin")
    public Map<String, String> admin() {
        return Map.of("status", "ok", "acesso", "ADMIN");
    }
}
