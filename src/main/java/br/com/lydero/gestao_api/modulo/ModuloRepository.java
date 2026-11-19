package br.com.lydero.gestao_api.modulo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuloRepository extends JpaRepository<Modulo, Long> {

    List<Modulo> findByAtivoTrueOrderByOrdemAsc();
}
