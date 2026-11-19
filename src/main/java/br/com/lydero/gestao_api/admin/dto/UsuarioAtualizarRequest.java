package br.com.lydero.gestao_api.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioAtualizarRequest(
        @NotBlank(message = "Informe o nome") @Size(max = 120) String nome,
        @NotBlank(message = "Informe o e-mail") @Email(message = "E-mail invalido") @Size(max = 150) String email,
        @NotBlank(message = "Informe o tipo")
        @Pattern(regexp = "ADMIN|COMUM", message = "Tipo deve ser ADMIN ou COMUM") String tipo,
        @NotNull(message = "Informe se o usuario esta ativo") Boolean ativo) {
}
