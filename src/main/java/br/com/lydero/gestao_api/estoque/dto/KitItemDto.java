package br.com.lydero.gestao_api.estoque.dto;

import java.math.BigDecimal;

public record KitItemDto(
        Long itemId,
        String nome,
        String codigo,
        String tipo,
        String unidade,
        BigDecimal quantidade,
        BigDecimal valorCusto,
        BigDecimal valorVenda,
        /** Estoque atual do item (null para servico). */
        BigDecimal estoqueItem,
        boolean ativo) {
}
