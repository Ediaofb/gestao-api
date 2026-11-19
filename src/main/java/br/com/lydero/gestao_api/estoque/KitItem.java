package br.com.lydero.gestao_api.estoque;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Um item (produto ou servico) dentro de um kit, com a quantidade usada em cada kit. */
@Entity
@Table(name = "produto_kit_item")
@Getter
@Setter
@NoArgsConstructor
public class KitItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kit_id", nullable = false)
    private Long kitId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal quantidade;
}
