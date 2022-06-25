package ru.vrn.vsu.csf.asashina.yandexproject.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShopUnitImportRequest {

    @Valid
    private ShopUnitImport[] items;

    @NotBlank
    private String updateDate;
}
