package br.com.lydero.gestao_api.empresa;

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
 * Empresa (cliente do sistema). Por enquanto mapeia so os campos essenciais;
 * endereco, logo e cores entram quando criarmos a tela de dados da empresa.
 */
@Entity
@Table(name = "empresa")
@Getter
@Setter
@NoArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razao_social", nullable = false, length = 150)
    private String razaoSocial;

    @Column(name = "nome_fantasia", length = 150)
    private String nomeFantasia;

    @Column(length = 14)
    private String documento;

    @Column(nullable = false, length = 30)
    private String segmento = "GERAL";

    @Column(nullable = false)
    private boolean ativo = true;

    /** Preenchido pelo proprio banco (DEFAULT CURRENT_TIMESTAMP). */
    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;
}
