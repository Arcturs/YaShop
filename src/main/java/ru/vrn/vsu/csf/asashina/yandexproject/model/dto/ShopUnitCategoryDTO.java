package ru.vrn.vsu.csf.asashina.yandexproject.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class ShopUnitCategoryDTO extends ShopUnitDTO {
    private Set<ShopUnitDTO> children = new HashSet<>();
}
