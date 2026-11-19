package br.com.lydero.gestao_api.estoque;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByEmpresaIdOrderByNomeAsc(Long empresaId);

    boolean existsByEmpresaIdAndCodigoIgnoreCase(Long empresaId, String codigo);

    boolean existsByEmpresaIdAndCodigoIgnoreCaseAndIdNot(Long empresaId, String codigo, Long id);

    /**
     * Busca o produto travando a linha no banco ate o fim da transacao. Evita que duas baixas
     * feitas ao mesmo tempo (dois usuarios, dois caixas) se atropelem e "percam" uma delas.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p where p.id = :id and p.empresaId = :empresaId")
    Optional<Produto> buscarParaAtualizar(@Param("id") Long id, @Param("empresaId") Long empresaId);

    @Query("select distinct p.categoria from Produto p "
            + "where p.empresaId = :empresaId and p.categoria is not null and p.categoria <> '' "
            + "order by p.categoria")
    List<String> categorias(@Param("empresaId") Long empresaId);
}
