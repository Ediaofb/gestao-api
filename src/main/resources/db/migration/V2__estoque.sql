-- =====================================================================
-- V2 - Estoque
--   produto (produtos, servicos e kits), itens dos kits e historico
--   de movimentacoes de estoque (entradas e baixas internas).
--
-- Regras de modelagem:
--  * quantidade e quantidade_minima aceitam 3 casas decimais (kg, litro, metro...).
--  * Kit NAO guarda estoque proprio: a disponibilidade e calculada a partir dos itens.
--  * Servico nao controla estoque.
--  * A quantidade do produto so muda por movimentacao (fica registrado quem, quando e por que).
-- =====================================================================

CREATE TABLE produto (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    empresa_id         BIGINT        NOT NULL,
    tipo               VARCHAR(10)   NOT NULL COMMENT 'PRODUTO, SERVICO ou KIT',
    codigo             VARCHAR(50)   NULL     COMMENT 'Codigo interno ou de barras (opcional, unico por empresa)',
    nome               VARCHAR(150)  NOT NULL,
    categoria          VARCHAR(80)   NULL,
    unidade            VARCHAR(10)   NOT NULL DEFAULT 'UN',
    valor_custo        DECIMAL(14,2) NOT NULL DEFAULT 0,
    valor_venda        DECIMAL(14,2) NOT NULL DEFAULT 0,
    quantidade         DECIMAL(14,3) NOT NULL DEFAULT 0,
    quantidade_minima  DECIMAL(14,3) NOT NULL DEFAULT 0,
    tamanho            VARCHAR(30)   NULL,
    cor                VARCHAR(30)   NULL,
    observacoes        VARCHAR(1000) NULL,
    ativo              TINYINT(1)    NOT NULL DEFAULT 1,
    criado_em          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                       ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_produto_empresa_codigo (empresa_id, codigo),
    KEY idx_produto_empresa_nome (empresa_id, nome),
    KEY idx_produto_empresa_categoria (empresa_id, categoria),
    CONSTRAINT fk_produto_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT chk_produto_quantidade CHECK (quantidade >= 0),
    CONSTRAINT chk_produto_minima CHECK (quantidade_minima >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Itens que compoem um kit/combo (o item pode ser produto ou servico)
CREATE TABLE produto_kit_item (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    kit_id      BIGINT        NOT NULL,
    item_id     BIGINT        NOT NULL,
    quantidade  DECIMAL(14,3) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_kit_item (kit_id, item_id),
    KEY idx_kit_item_item (item_id),
    CONSTRAINT fk_kititem_kit  FOREIGN KEY (kit_id)  REFERENCES produto (id) ON DELETE CASCADE,
    CONSTRAINT fk_kititem_item FOREIGN KEY (item_id) REFERENCES produto (id),
    CONSTRAINT chk_kititem_quantidade CHECK (quantidade > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Historico de movimentacoes (hoje: ENTRADA e BAIXA_INTERNA; depois: VENDA, COMPRA...)
CREATE TABLE movimentacao_estoque (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    empresa_id            BIGINT        NOT NULL,
    produto_id            BIGINT        NOT NULL,
    tipo                  VARCHAR(20)   NOT NULL,
    quantidade            DECIMAL(14,3) NOT NULL COMMENT 'Sempre positiva; o tipo diz se soma ou subtrai',
    quantidade_anterior   DECIMAL(14,3) NOT NULL,
    quantidade_posterior  DECIMAL(14,3) NOT NULL,
    motivo                VARCHAR(40)   NOT NULL,
    observacao            VARCHAR(255)  NULL,
    usuario_id            BIGINT        NULL,
    criado_em             DATETIME      NOT NULL COMMENT 'Gravado em UTC pela aplicacao',
    PRIMARY KEY (id),
    KEY idx_mov_produto_data (produto_id, criado_em),
    KEY idx_mov_empresa_data (empresa_id, criado_em),
    CONSTRAINT fk_mov_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_mov_produto FOREIGN KEY (produto_id) REFERENCES produto (id),
    CONSTRAINT fk_mov_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
