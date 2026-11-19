package br.com.lydero.gestao_api.estoque.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * Cadastro/edicao de produto, servico ou kit.
 * Preco de venda: informe valorVenda OU margem (percentual sobre o custo).
 * "quantidade" so vale na criacao (estoque inicial); depois o estoque muda por movimentacao.
 */
public record ProdutoRequest(
        @NotBlank(message = "Informe o tipo (PRODUTO, SERVICO ou KIT)") String tipo,
        @Size(max = 50, message = "Codigo com no maximo 50 caracteres") String codigo,
        @NotBlank(message = "Informe o nome") @Size(max = 150, message = "Nome com no maximo 150 caracteres") String nome,
        @Size(max = 80, message = "Categoria com no maximo 80 caracteres") String categoria,
        @Size(max = 10) String unidade,
        @DecimalMin(value = "0", message = "O valor de custo nao pode ser negativo") BigDecimal valorCusto,
        @DecimalMin(value = "0", message = "O valor de venda nao pode ser negativo") BigDecimal valorVenda,
        @DecimalMin(value = "0", message = "A margem nao pode ser negativa") BigDecimal margem,
        @DecimalMin(value = "0", message = "A quantidade nao pode ser negativa") BigDecimal quantidade,
        @DecimalMin(value = "0", message = "A quantidade minima nao pode ser negativa") BigDecimal quantidadeMinima,
        @Size(max = 30, message = "Tamanho com no maximo 30 caracteres") String tamanho,
        @Size(max = 30, message = "Cor com no maximo 30 caracteres") String cor,
        @Size(max = 1000, message = "Observacoes com no maximo 1000 caracteres") String observacoes,
        @Valid List<KitItemRequest> itens) {
}
