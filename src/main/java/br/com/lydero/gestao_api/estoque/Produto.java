package br.com.lydero.gestao_api.estoque;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Produto, servico ou kit/combo de uma empresa. */
@Entity
@Table(name = "produto")
@Getter
@Setter
@NoArgsConstructor
public class Produto {

    public static final String TIPO_PRODUTO = "PRODUTO";
    public static final String TIPO_SERVICO = "SERVICO";
    public static final String TIPO_KIT = "KIT";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(nullable = false, length = 10)
    private String tipo;

    @Column(length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 80)
    private String categoria;

    @Column(nullable = false, length = 10)
    private String unidade = "UN";

    @Column(name = "valor_custo", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorCusto = BigDecimal.ZERO;

    @Column(name = "valor_venda", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorVenda = BigDecimal.ZERO;

    /** Estoque atual (so faz sentido para PRODUTO). Muda apenas por movimentacao. */
    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal quantidade = BigDecimal.ZERO;

    @Column(name = "quantidade_minima", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantidadeMinima = BigDecimal.ZERO;

    @Column(length = 30)
    private String tamanho;

    @Column(length = 30)
    private String cor;

    @Column(length = 1000)
    private String observacoes;

    @Column(nullable = false)
    private boolean ativo = true;

    public boolean isProduto() {
        return TIPO_PRODUTO.equals(tipo);
    }

    public boolean isServico() {
        return TIPO_SERVICO.equals(tipo);
    }

    public boolean isKit() {
        return TIPO_KIT.equals(tipo);
    }
}
