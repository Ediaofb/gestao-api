package br.com.lydero.gestao_api.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Informe o e-mail") @Size(max = 150) String email,
        @NotBlank(message = "Informe a senha") @Size(max = 100) String senha) {
}
