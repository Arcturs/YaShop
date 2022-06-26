package ru.vrn.vsu.csf.asashina.yandexproject.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopUnitOfferDTO extends ShopUnitDTO {
    private ShopUnitDTO children = null;
}
