package br.com.lydero.gestao_api.modulo;

/**
 * Acoes que podem ser liberadas ou bloqueadas em cada modulo.
 * Na autorizacao, cada permissao vira o texto "MODULO:ACAO" (ex.: "ESTOQUE:CRIAR").
 */
public enum Acao {
    VISUALIZAR,
    CRIAR,
    EDITAR,
    EXCLUIR
}
