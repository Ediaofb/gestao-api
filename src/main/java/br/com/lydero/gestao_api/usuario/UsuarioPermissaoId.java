package br.com.lydero.gestao_api.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Chave composta (usuario + modulo) da tabela usuario_permissao. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UsuarioPermissaoId implements Serializable {

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "modulo_id")
    private Long moduloId;
}
