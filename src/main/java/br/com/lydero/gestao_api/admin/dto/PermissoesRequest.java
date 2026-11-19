package br.com.lydero.gestao_api.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PermissoesRequest(
        @NotNull(message = "Informe a lista de permissoes") @Valid List<PermissaoDto> permissoes) {
}
