package br.com.lydero.gestao_api.auth;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UsuarioResumo usuario,
        List<String> permissoes) {

    public record UsuarioResumo(Long id, Long empresaId, String nome, String email, String tipo) {
    }
}
