package br.com.lydero.gestao_api.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Usuario com a matriz completa: uma linha para CADA modulo ativo (mesmo os sem
 * nenhuma permissao), para a tela montar a grade de checkboxes direto desta resposta.
 */
public record UsuarioDetalheDto(
        Long id,
        String nome,
        String email,
        String tipo,
        boolean ativo,
        LocalDateTime ultimoAcesso,
        List<PermissaoDto> permissoes) {
}
