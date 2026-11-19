package br.com.lydero.gestao_api.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UsuarioCriarRequest(
        @NotBlank(message = "Informe o nome") @Size(max = 120) String nome,
        @NotBlank(message = "Informe o e-mail") @Email(message = "E-mail invalido") @Size(max = 150) String email,
        @NotBlank(message = "Informe a senha")
        @Size(min = 8, max = 72, message = "A senha deve ter de 8 a 72 caracteres") String senha,
        @Pattern(regexp = "ADMIN|COMUM", message = "Tipo deve ser ADMIN ou COMUM") String tipo,
        @Valid List<PermissaoDto> permissoes) {
}
