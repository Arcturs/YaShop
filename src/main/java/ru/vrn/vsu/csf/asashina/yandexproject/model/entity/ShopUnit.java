package ru.vrn.vsu.csf.asashina.yandexproject.model.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import ru.vrn.vsu.csf.asashina.yandexproject.model.enumeration.ShopUnitType;

import javax.persistence.*;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "shop_unit")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ShopUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_unit_id")
    private Long shopUnitId;

    @GenericGenerator(name = "IdGenerator", strategy = "ru.vrn.vsu.csf.asashina.yandexproject.model.IdGenerator")
    @GeneratedValue(generator = "IdGenerator")
    @Column(name = "id", updatable = false, unique = true, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "date", nullable = false)
    private Instant date;

    @Column(name = "parent_id")
    private UUID parentId;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "shop_unit_parent_id")
    private ShopUnit shopUnitParent;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false, columnDefinition = "shop_unit_type")
    private ShopUnitType unitType;

    @Column(name = "price")
    private Long price;

    @OneToMany(mappedBy = "shopUnitParent", cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private Set<ShopUnit> children;

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }

        ShopUnit entity = (ShopUnit) obj;
        return Objects.equals(shopUnitId, entity.getShopUnitId())
                && Objects.equals(id, entity.getId())
                && Objects.equals(name, entity.getName())
                && Objects.equals(date, entity.getDate())
                && Objects.equals(unitType, entity.getUnitType())
                && Objects.equals(price, entity.getPrice());
    }
}
