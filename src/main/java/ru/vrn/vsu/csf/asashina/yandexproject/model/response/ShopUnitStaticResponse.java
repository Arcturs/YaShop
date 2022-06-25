package ru.vrn.vsu.csf.asashina.yandexproject.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShopUnitStaticResponse {

    private List<ShopUnitStaticUnit> items;

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

        ShopUnitStaticResponse dto = (ShopUnitStaticResponse) obj;
        return Objects.equals(items, dto.getItems());
    }
}
