package br.com.lydero.gestao_api.cliente.dto;

import java.time.LocalDate;

public record ClienteDto(
        Long id,
        String tipoPessoa,
        String nome,
        String documento,
        LocalDate dataNascimento,
        String telefone,
        String telefone2,
        String email,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String localizacaoUrl,
        String instagram,
        String facebook,
        String outrasRedes,
        String observacoes,
        boolean ativo) {
}
