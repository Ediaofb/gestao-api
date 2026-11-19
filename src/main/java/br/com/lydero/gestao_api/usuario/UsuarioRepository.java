package br.com.lydero.gestao_api.usuario;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<Usuario> findByEmpresaIdOrderByNomeAsc(Long empresaId);

    /** Busca ja filtrando pela empresa: usuario de outra empresa nunca e encontrado. */
    Optional<Usuario> findByIdAndEmpresaId(Long id, Long empresaId);

    long countByEmpresaIdAndTipoAndAtivoTrue(Long empresaId, String tipo);
}
