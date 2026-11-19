package br.com.lydero.gestao_api.auditoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registro de auditoria: quem fez o que, quando e em qual registro.
 * As colunas JSON (dados_antes e dados_depois) ainda nao sao usadas.
 */
@Entity
@Table(name = "log_auditoria")
@Getter
@Setter
@NoArgsConstructor
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false, length = 30)
    private String acao;

    @Column(length = 40)
    private String modulo;

    @Column(length = 60)
    private String entidade;

    @Column(name = "entidade_id", length = 40)
    private String entidadeId;

    @Column(length = 255)
    private String descricao;

    @Column(length = 45)
    private String ip;

    /** Preenchido pelo proprio banco (DEFAULT CURRENT_TIMESTAMP). */
    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;
}
