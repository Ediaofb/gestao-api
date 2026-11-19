package br.com.lydero.gestao_api.admin.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Uma linha da matriz de checkboxes: o modulo (pelo codigo, ex.: "ESTOQUE")
 * e as quatro acoes liberadas ou nao.
 */
public record PermissaoDto(
        @NotBlank(message = "Informe o codigo do modulo") String modulo,
        boolean visualizar,
        boolean criar,
        boolean editar,
        boolean excluir) {
}
