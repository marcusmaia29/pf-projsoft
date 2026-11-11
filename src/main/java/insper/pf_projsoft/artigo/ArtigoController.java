package insper.pf_projsoft.artigo;

import java.util.List;

import insper.pf_projsoft.security.WebSecurityConfig;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/artigos")
public class ArtigoController {
    private final ArtigoService service;

    public ArtigoController(ArtigoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArtigoResponse criar(@Valid @RequestBody ArtigoRequest request,
                                @AuthenticationPrincipal Jwt jwt) {
        return service.criar(request, jwt.getClaimAsString(WebSecurityConfig.EMAIL_CLAIM));
    }

    @GetMapping
    public List<ArtigoResponse> listar() {
        return service.listar();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}
