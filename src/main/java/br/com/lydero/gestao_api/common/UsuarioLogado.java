package br.com.lydero.gestao_api.common;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Quem esta fazendo a requisicao, lido do token JWT. A empresa SEMPRE vem daqui,
 * nunca de um parametro enviado pelo cliente (isso garante o isolamento entre empresas).
 */
public record UsuarioLogado(Long id, Long empresaId, boolean admin, Set<String> permissoes, String ip) {

    public static UsuarioLogado de(Jwt jwt, HttpServletRequest request) {
        Number empresa = jwt.getClaim("empresaId");
        List<String> authorities = jwt.getClaimAsStringList("authorities");
        return new UsuarioLogado(
                Long.valueOf(jwt.getSubject()),
                empresa.longValue(),
                "ADMIN".equals(jwt.getClaimAsString("tipo")),
                authorities == null ? Set.of() : new HashSet<>(authorities),
                request.getRemoteAddr());
    }

    public boolean possui(String permissao) {
        return permissoes.contains(permissao);
    }
}
