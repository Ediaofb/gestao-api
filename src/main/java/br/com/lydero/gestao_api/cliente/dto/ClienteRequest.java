package br.com.lydero.gestao_api.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Cadastro/edicao de cliente. Todos os campos, exceto o nome, sao opcionais. */
public record ClienteRequest(
        String tipoPessoa,
        @NotBlank(message = "Informe o nome") @Size(max = 150, message = "Nome com no maximo 150 caracteres") String nome,
        @Size(max = 18, message = "Documento muito longo") String documento,
        LocalDate dataNascimento,
        @Size(max = 30, message = "Telefone com no maximo 30 caracteres") String telefone,
        @Size(max = 30, message = "Telefone com no maximo 30 caracteres") String telefone2,
        @Email(message = "E-mail invalido") @Size(max = 150, message = "E-mail com no maximo 150 caracteres") String email,
        @Size(max = 9, message = "CEP invalido") String cep,
        @Size(max = 150) String logradouro,
        @Size(max = 20) String numero,
        @Size(max = 80) String complemento,
        @Size(max = 80) String bairro,
        @Size(max = 80) String cidade,
        @Size(max = 2, message = "UF com 2 letras") String uf,
        @Size(max = 500, message = "Link muito longo") String localizacaoUrl,
        @Size(max = 100) String instagram,
        @Size(max = 150) String facebook,
        @Size(max = 255) String outrasRedes,
        @Size(max = 1000, message = "Observacoes com no maximo 1000 caracteres") String observacoes) {
}
