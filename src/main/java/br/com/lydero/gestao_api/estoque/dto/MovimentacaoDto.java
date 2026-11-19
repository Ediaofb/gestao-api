package br.com.lydero.gestao_api.estoque.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimentacaoDto(
        Long id,
        String tipo,
        BigDecimal quantidade,
        BigDecimal quantidadeAnterior,
        BigDecimal quantidadePosterior,
        String motivo,
        String observacao,
        String usuarioNome,
        /** UTC. */
        LocalDateTime criadoEm) {
}
