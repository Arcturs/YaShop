package ru.vrn.vsu.csf.asashina.yandexproject.model.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.vrn.vsu.csf.asashina.yandexproject.annotation.ShopUnitTypeEnum;
import ru.vrn.vsu.csf.asashina.yandexproject.deserializer.ParentIdDeserializer;
import ru.vrn.vsu.csf.asashina.yandexproject.model.enumeration.ShopUnitType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShopUnitImport {

    @NotBlank
    private String id;

    @NotBlank
    @Size(max = 1024)
    private String name;

    @JsonDeserialize(using = ParentIdDeserializer.class)
    private String parentId;

    @ShopUnitTypeEnum(anyOf = {ShopUnitType.OFFER, ShopUnitType.CATEGORY})
    private ShopUnitType type;

    private Long price;
}
