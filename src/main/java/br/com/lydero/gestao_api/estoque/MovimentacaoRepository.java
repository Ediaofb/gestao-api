package br.com.lydero.gestao_api.estoque;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    /** As 100 movimentacoes mais recentes do produto. */
    List<MovimentacaoEstoque> findTop100ByProdutoIdAndEmpresaIdOrderByIdDesc(Long produtoId, Long empresaId);
}
