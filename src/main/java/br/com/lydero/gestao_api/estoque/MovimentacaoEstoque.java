package br.com.lydero.gestao_api.estoque;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Registro de cada entrada ou baixa no estoque: o que mudou, quando, por quem e por que. */
@Entity
@Table(name = "movimentacao_estoque")
@Getter
@Setter
@NoArgsConstructor
public class MovimentacaoEstoque {

    public static final String ENTRADA = "ENTRADA";
    public static final String BAIXA_INTERNA = "BAIXA_INTERNA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "quantidade_anterior", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantidadeAnterior;

    @Column(name = "quantidade_posterior", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantidadePosterior;

    @Column(nullable = false, length = 40)
    private String motivo;

    @Column(length = 255)
    private String observacao;

    @Column(name = "usuario_id")
    private Long usuarioId;

    /** Gravado em UTC pela aplicacao (o frontend converte para o horario local). */
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;
}
