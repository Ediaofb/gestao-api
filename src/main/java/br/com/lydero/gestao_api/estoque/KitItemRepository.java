package br.com.lydero.gestao_api.estoque;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KitItemRepository extends JpaRepository<KitItem, Long> {

    List<KitItem> findByKitId(Long kitId);

    List<KitItem> findByKitIdIn(Collection<Long> kitIds);

    List<KitItem> findByItemId(Long itemId);
}
