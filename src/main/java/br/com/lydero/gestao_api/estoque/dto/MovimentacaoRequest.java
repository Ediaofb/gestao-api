package br.com.lydero.gestao_api.estoque.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** tipo: ENTRADA ou BAIXA_INTERNA. quantidade e sempre positiva; o tipo diz se soma ou subtrai. */
public record MovimentacaoRequest(
        @NotBlank(message = "Informe o tipo da movimentacao") String tipo,
        @NotNull(message = "Informe a quantidade")
        @DecimalMin(value = "0.001", message = "A quantidade deve ser maior que zero")
        BigDecimal quantidade,
        @NotBlank(message = "Informe o motivo") String motivo,
        @Size(max = 255, message = "Observacao com no maximo 255 caracteres") String observacao) {
}
