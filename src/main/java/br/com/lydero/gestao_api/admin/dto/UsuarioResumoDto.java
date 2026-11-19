package br.com.lydero.gestao_api.admin.dto;

import java.time.LocalDateTime;

/** Linha da listagem de usuarios (sem a matriz de permissoes). */
public record UsuarioResumoDto(
        Long id,
        String nome,
        String email,
        String tipo,
        boolean ativo,
        LocalDateTime ultimoAcesso) {
}
