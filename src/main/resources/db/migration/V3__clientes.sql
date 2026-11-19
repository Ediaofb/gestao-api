-- =====================================================================
-- V3 - Clientes (cadastro)
-- O perfil financeiro, o comportamento e a "saude" do cliente (modulo 5.1 a 5.3)
-- serao calculados a partir das vendas (PDV) e do financeiro, entao nao ha tabela para eles.
-- =====================================================================

CREATE TABLE cliente (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    empresa_id       BIGINT        NOT NULL,
    tipo_pessoa      VARCHAR(10)   NOT NULL DEFAULT 'FISICA' COMMENT 'FISICA ou JURIDICA',
    nome             VARCHAR(150)  NOT NULL,
    documento        VARCHAR(18)   NULL     COMMENT 'CPF ou CNPJ, so digitos (opcional, unico por empresa)',
    data_nascimento  DATE          NULL     COMMENT 'Nascimento (pessoa fisica) ou fundacao (juridica)',
    telefone         VARCHAR(30)   NULL,
    telefone2        VARCHAR(30)   NULL,
    email            VARCHAR(150)  NULL,
    cep              VARCHAR(9)    NULL,
    logradouro       VARCHAR(150)  NULL,
    numero           VARCHAR(20)   NULL,
    complemento      VARCHAR(80)   NULL,
    bairro           VARCHAR(80)   NULL,
    cidade           VARCHAR(80)   NULL,
    uf               CHAR(2)       NULL,
    localizacao_url  VARCHAR(500)  NULL     COMMENT 'Link do Google Maps',
    instagram        VARCHAR(100)  NULL,
    facebook         VARCHAR(150)  NULL,
    outras_redes     VARCHAR(255)  NULL,
    observacoes      VARCHAR(1000) NULL,
    ativo            TINYINT(1)    NOT NULL DEFAULT 1,
    criado_em        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                     ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cliente_empresa_documento (empresa_id, documento),
    KEY idx_cliente_empresa_nome (empresa_id, nome),
    CONSTRAINT fk_cliente_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
