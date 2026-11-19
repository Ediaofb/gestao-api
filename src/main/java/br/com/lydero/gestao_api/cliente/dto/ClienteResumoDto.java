package br.com.lydero.gestao_api.cliente.dto;

/** Paineis do topo da tela de clientes (so clientes ativos). */
public record ClienteResumoDto(long totalAtivos, long totalInativos, long aniversariantesMes, long aniversariantesHoje) {
}
