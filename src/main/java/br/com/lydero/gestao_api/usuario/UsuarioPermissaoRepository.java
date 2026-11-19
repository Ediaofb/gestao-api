package br.com.lydero.gestao_api.usuario;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioPermissaoRepository extends JpaRepository<UsuarioPermissao, UsuarioPermissaoId> {

    List<UsuarioPermissao> findByIdUsuarioId(Long usuarioId);
}
