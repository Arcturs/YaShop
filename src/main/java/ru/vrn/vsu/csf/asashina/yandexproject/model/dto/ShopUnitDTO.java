package ru.vrn.vsu.csf.asashina.yandexproject.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.vrn.vsu.csf.asashina.yandexproject.model.enumeration.ShopUnitType;

import java.time.Instant;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShopUnitDTO {
    private UUID id;
    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant date;

    private UUID parentId;
    private ShopUnitType type;
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

        ShopUnitDTO dto = (ShopUnitDTO) obj;
        return Objects.equals(id, dto.getId())
                && Objects.equals(name, dto.getName())
                && Objects.equals(date, dto.getDate())
                && Objects.equals(type, dto.getType())
                && Objects.equals(price, dto.getPrice());
    }
}
