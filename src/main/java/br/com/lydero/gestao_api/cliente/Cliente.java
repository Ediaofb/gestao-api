package br.com.lydero.gestao_api.cliente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Cliente de uma empresa (pessoa fisica ou juridica). */
@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    public static final String FISICA = "FISICA";
    public static final String JURIDICA = "JURIDICA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "tipo_pessoa", nullable = false, length = 10)
    private String tipoPessoa = FISICA;

    @Column(nullable = false, length = 150)
    private String nome;

    /** So digitos. */
    @Column(length = 18)
    private String documento;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(length = 30)
    private String telefone;

    @Column(length = 30)
    private String telefone2;

    @Column(length = 150)
    private String email;

    @Column(length = 9)
    private String cep;

    @Column(length = 150)
    private String logradouro;

    @Column(length = 20)
    private String numero;

    @Column(length = 80)
    private String complemento;

    @Column(length = 80)
    private String bairro;

    @Column(length = 80)
    private String cidade;

    @Column(length = 2, columnDefinition = "CHAR(2)")
    private String uf;

    @Column(name = "localizacao_url", length = 500)
    private String localizacaoUrl;

    @Column(length = 100)
    private String instagram;

    @Column(length = 150)
    private String facebook;

    @Column(name = "outras_redes", length = 255)
    private String outrasRedes;

    @Column(length = 1000)
    private String observacoes;

    @Column(nullable = false)
    private boolean ativo = true;
}
