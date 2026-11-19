package br.com.lydero.gestao_api.auth;

import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** Devolve quem esta logado, lendo os dados do proprio token. */
    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("id", jwt.getSubject());
        dados.put("empresaId", jwt.getClaim("empresaId"));
        dados.put("nome", jwt.getClaimAsString("nome"));
        dados.put("email", jwt.getClaimAsString("email"));
        dados.put("tipo", jwt.getClaimAsString("tipo"));
        dados.put("permissoes", jwt.getClaimAsStringList("authorities"));
        return dados;
    }
}
