package br.com.lydero.gestao_api.estoque.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * quantidade: PRODUTO = estoque; KIT = quantos kits da para montar; SERVICO = null.
 * situacao: SEM_CONTROLE, ZERADO, BAIXO ou OK.
 */
public record ProdutoDto(
        Long id,
        String tipo,
        String codigo,
        String nome,
        String categoria,
        String unidade,
        BigDecimal valorCusto,
        BigDecimal valorVenda,
        BigDecimal margem,
        BigDecimal quantidade,
        BigDecimal quantidadeMinima,
        String tamanho,
        String cor,
        String observacoes,
        boolean ativo,
        String situacao,
        List<KitItemDto> itens) {
}
