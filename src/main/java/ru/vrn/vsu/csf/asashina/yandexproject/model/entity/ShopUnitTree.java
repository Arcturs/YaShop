package ru.vrn.vsu.csf.asashina.yandexproject.model.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "shop_unit_tree")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ShopUnitTree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "node_id")
    private Long nodeId;

    @Column(name = "path", columnDefinition = "ltree")
    private String path;

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

        ShopUnitTree entity = (ShopUnitTree) obj;
        return Objects.equals(id, entity.getId())
                && Objects.equals(nodeId, entity.getNodeId())
                && Objects.equals(path, entity.getPath());
    }
}
