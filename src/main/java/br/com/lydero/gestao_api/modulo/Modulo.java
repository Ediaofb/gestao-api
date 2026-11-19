package br.com.lydero.gestao_api.modulo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Catalogo global de modulos do sistema (igual para todas as empresas). */
@Entity
@Table(name = "modulo")
@Getter
@Setter
@NoArgsConstructor
public class Modulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(nullable = false)
    private Integer ordem;

    @Column(nullable = false)
    private boolean ativo;
}
