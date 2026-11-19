package br.com.lydero.gestao_api.estoque.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Um item do kit: qual produto/servico e quantas unidades dele entram em cada kit. */
public record KitItemRequest(
        @NotNull(message = "Informe o item do kit") Long itemId,
        @NotNull(message = "Informe a quantidade do item")
        @DecimalMin(value = "0.001", message = "A quantidade do item deve ser maior que zero")
        BigDecimal quantidade) {
}
