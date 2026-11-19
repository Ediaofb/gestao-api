package br.com.lydero.gestao_api.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SenhaRequest(
        @NotBlank(message = "Informe a nova senha")
        @Size(min = 8, max = 72, message = "A senha deve ter de 8 a 72 caracteres") String senha) {
}
