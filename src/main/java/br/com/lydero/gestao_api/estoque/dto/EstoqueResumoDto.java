package br.com.lydero.gestao_api.estoque.dto;

import java.math.BigDecimal;

/** Paineis do topo da tela de estoque (consideram apenas itens ativos). */
public record EstoqueResumoDto(
        long totalItens,
        long itensZerados,
        long estoqueBaixo,
        BigDecimal valorCustoEstoque,
        BigDecimal valorVendaEstoque) {
}
