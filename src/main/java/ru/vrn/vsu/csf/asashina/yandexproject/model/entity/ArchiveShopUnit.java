package ru.vrn.vsu.csf.asashina.yandexproject.model.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import ru.vrn.vsu.csf.asashina.yandexproject.model.enumeration.ShopUnitType;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "archive_shop_unit")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ArchiveShopUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "archive_shop_unit_id")
    private Long archiveShopUnitId;

    @Column(name = "shop_unit_id", nullable = false, updatable = false)
    private Long shopUnitId;

    @GenericGenerator(name = "IdGenerator", strategy = "ru.vrn.vsu.csf.asashina.yandexproject.model.IdGenerator")
    @GeneratedValue(generator = "IdGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "date", nullable = false)
    private Instant date;

    @Column(name = "parent_id")
    private UUID parentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false, columnDefinition = "shop_unit_type")
    private ShopUnitType unitType;

    @Column(name = "price")
    private Long price;

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

        ArchiveShopUnit entity = (ArchiveShopUnit) obj;
        return Objects.equals(archiveShopUnitId, entity.getArchiveShopUnitId())
                && Objects.equals(shopUnitId, entity.getShopUnitId())
                && Objects.equals(id, entity.getId())
                && Objects.equals(name, entity.getName())
                && Objects.equals(date, entity.getDate())
                && Objects.equals(unitType, entity.getUnitType())
                && Objects.equals(price, entity.getPrice());
    }
}
