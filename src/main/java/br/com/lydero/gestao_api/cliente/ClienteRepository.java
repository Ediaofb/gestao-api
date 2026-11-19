package br.com.lydero.gestao_api.cliente;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByEmpresaIdOrderByNomeAsc(Long empresaId);

    Optional<Cliente> findByIdAndEmpresaId(Long id, Long empresaId);

    boolean existsByEmpresaIdAndDocumento(Long empresaId, String documento);

    boolean existsByEmpresaIdAndDocumentoAndIdNot(Long empresaId, String documento, Long id);
}
