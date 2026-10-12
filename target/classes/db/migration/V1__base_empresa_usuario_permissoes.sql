-- =====================================================================
-- V1 - Base do sistema
--   empresa (multiempresa), modulo, perfil, usuario, permissoes
--   e log de auditoria.
--
-- Banco: MySQL 8
-- Convencao multiempresa: toda tabela de negocio tem empresa_id.
-- Tabelas filhas (ex.: usuario_permissao) herdam a empresa pelo pai.
-- =====================================================================

-- ---------------------------------------------------------------------
-- EMPRESA (cada cliente do sistema; inclui dados e aparencia)
-- ---------------------------------------------------------------------
CREATE TABLE empresa (
                         id                  BIGINT       NOT NULL AUTO_INCREMENT,
                         razao_social        VARCHAR(150) NOT NULL,
                         nome_fantasia       VARCHAR(150) NULL,
                         documento           VARCHAR(14)  NULL COMMENT 'CNPJ ou CPF, somente numeros',
                         inscricao_estadual  VARCHAR(30)  NULL,
                         segmento            VARCHAR(30)  NOT NULL DEFAULT 'GERAL'
                             COMMENT 'GERAL, VAREJO, OFICINA, HORTIFRUTI, AGRO',
                         email               VARCHAR(150) NULL,
                         telefone            VARCHAR(20)  NULL,
                         cep                 VARCHAR(8)   NULL,
                         logradouro          VARCHAR(150) NULL,
                         numero              VARCHAR(15)  NULL,
                         complemento         VARCHAR(80)  NULL,
                         bairro              VARCHAR(80)  NULL,
                         cidade              VARCHAR(80)  NULL,
                         uf                  CHAR(2)      NULL,
                         logo_caminho        VARCHAR(255) NULL,
                         cor_primaria        CHAR(7)      NULL COMMENT 'Hexadecimal, ex.: #1F6FEB',
                         cor_secundaria      CHAR(7)      NULL,
                         ativo               TINYINT(1)   NOT NULL DEFAULT 1,
                         criado_em           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         atualizado_em       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                             ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (id),
                         UNIQUE KEY uk_empresa_documento (documento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- MODULO (catalogo global dos modulos do sistema, igual para todos)
-- ---------------------------------------------------------------------
CREATE TABLE modulo (
                        id      BIGINT       NOT NULL AUTO_INCREMENT,
                        codigo  VARCHAR(40)  NOT NULL,
                        nome    VARCHAR(80)  NOT NULL,
                        ordem   INT          NOT NULL DEFAULT 0,
                        ativo   TINYINT(1)   NOT NULL DEFAULT 1,
                        PRIMARY KEY (id),
                        UNIQUE KEY uk_modulo_codigo (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO modulo (codigo, nome, ordem, ativo) VALUES
                                                    ('ADMINISTRACAO', 'Administração', 1, 1),
                                                    ('ESTOQUE',       'Estoque',       2, 1),
                                                    ('PDV',           'PDV (Painel de Venda)', 3, 1),
                                                    ('CLIENTES',      'Clientes',      4, 1),
                                                    ('FINANCEIRO',    'Financeiro',    5, 1),
                                                    ('COMPRAS',       'Compras',       6, 1),
                                                    ('COMERCIAL',     'Comercial',     7, 1),
                                                    ('DOCUMENTOS',    'Documentos',    8, 0),  -- futuro
                                                    ('DASHBOARD',     'Dashboard',     9, 1);

-- ---------------------------------------------------------------------
-- PERFIL (modelo de permissoes por empresa, ex.: "Vendedor")
-- ---------------------------------------------------------------------
CREATE TABLE perfil (
                        id          BIGINT       NOT NULL AUTO_INCREMENT,
                        empresa_id  BIGINT       NOT NULL,
                        nome        VARCHAR(60)  NOT NULL,
                        descricao   VARCHAR(200) NULL,
                        criado_em   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (id),
                        UNIQUE KEY uk_perfil_empresa_nome (empresa_id, nome),
                        CONSTRAINT fk_perfil_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Permissoes do perfil (matriz: modulo x acao)
CREATE TABLE perfil_permissao (
                                  perfil_id         BIGINT     NOT NULL,
                                  modulo_id         BIGINT     NOT NULL,
                                  pode_visualizar   TINYINT(1) NOT NULL DEFAULT 0,
                                  pode_criar        TINYINT(1) NOT NULL DEFAULT 0,
                                  pode_editar       TINYINT(1) NOT NULL DEFAULT 0,
                                  pode_excluir      TINYINT(1) NOT NULL DEFAULT 0,
                                  PRIMARY KEY (perfil_id, modulo_id),
                                  CONSTRAINT fk_perfperm_perfil FOREIGN KEY (perfil_id) REFERENCES perfil (id) ON DELETE CASCADE,
                                  CONSTRAINT fk_perfperm_modulo FOREIGN KEY (modulo_id) REFERENCES modulo (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- USUARIO
--   email e unico em todo o sistema e funciona como login.
--   tipo: ADMIN (acesso total na empresa) ou COMUM (usa as permissoes).
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
                         id             BIGINT       NOT NULL AUTO_INCREMENT,
                         empresa_id     BIGINT       NOT NULL,
                         perfil_id      BIGINT       NULL,
                         nome           VARCHAR(120) NOT NULL,
                         email          VARCHAR(150) NOT NULL,
                         senha_hash     VARCHAR(100) NOT NULL COMMENT 'Hash BCrypt, nunca a senha pura',
                         tipo           VARCHAR(20)  NOT NULL DEFAULT 'COMUM' COMMENT 'ADMIN ou COMUM',
                         ativo          TINYINT(1)   NOT NULL DEFAULT 1,
                         ultimo_acesso  DATETIME     NULL,
                         criado_em      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         atualizado_em  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                             ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (id),
                         UNIQUE KEY uk_usuario_email (email),
                         KEY idx_usuario_empresa (empresa_id),
                         CONSTRAINT fk_usuario_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
                         CONSTRAINT fk_usuario_perfil  FOREIGN KEY (perfil_id)  REFERENCES perfil (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Permissoes efetivas do usuario (podem ser ajustadas alem do perfil)
CREATE TABLE usuario_permissao (
                                   usuario_id        BIGINT     NOT NULL,
                                   modulo_id         BIGINT     NOT NULL,
                                   pode_visualizar   TINYINT(1) NOT NULL DEFAULT 0,
                                   pode_criar        TINYINT(1) NOT NULL DEFAULT 0,
                                   pode_editar       TINYINT(1) NOT NULL DEFAULT 0,
                                   pode_excluir      TINYINT(1) NOT NULL DEFAULT 0,
                                   PRIMARY KEY (usuario_id, modulo_id),
                                   CONSTRAINT fk_usrperm_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
                                   CONSTRAINT fk_usrperm_modulo  FOREIGN KEY (modulo_id)  REFERENCES modulo (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- LOG DE AUDITORIA (quem fez o que, quando e em qual registro)
-- ---------------------------------------------------------------------
CREATE TABLE log_auditoria (
                               id            BIGINT       NOT NULL AUTO_INCREMENT,
                               empresa_id    BIGINT       NOT NULL,
                               usuario_id    BIGINT       NULL,
                               acao          VARCHAR(30)  NOT NULL COMMENT 'LOGIN, LOGOUT, CRIAR, EDITAR, EXCLUIR, ESTORNO...',
                               modulo        VARCHAR(40)  NULL,
                               entidade      VARCHAR(60)  NULL,
                               entidade_id   VARCHAR(40)  NULL,
                               descricao     VARCHAR(255) NULL,
                               dados_antes   JSON         NULL,
                               dados_depois  JSON         NULL,
                               ip            VARCHAR(45)  NULL,
                               criado_em     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (id),
                               KEY idx_log_empresa_data (empresa_id, criado_em),
                               KEY idx_log_empresa_usuario (empresa_id, usuario_id),
                               KEY idx_log_entidade (empresa_id, entidade, entidade_id),
                               CONSTRAINT fk_log_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
                               CONSTRAINT fk_log_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;