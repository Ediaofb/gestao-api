package br.com.lydero.gestao_api.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Uma linha da matriz de permissoes: o que o usuario pode fazer em um modulo. */
@Entity
@Table(name = "usuario_permissao")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioPermissao {

    @EmbeddedId
    private UsuarioPermissaoId id;

    @Column(name = "pode_visualizar", nullable = false)
    private boolean podeVisualizar;

    @Column(name = "pode_criar", nullable = false)
    private boolean podeCriar;

    @Column(name = "pode_editar", nullable = false)
    private boolean podeEditar;

    @Column(name = "pode_excluir", nullable = false)
    private boolean podeExcluir;
}
