package ru.vrn.vsu.csf.asashina.yandexproject.validators;

import ru.vrn.vsu.csf.asashina.yandexproject.annotation.ShopUnitTypeEnum;
import ru.vrn.vsu.csf.asashina.yandexproject.model.enumeration.ShopUnitType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class ShopUnitTypeEnumValidator implements ConstraintValidator<ShopUnitTypeEnum, ShopUnitType> {

    private ShopUnitType[] subset;

    @Override
    public boolean isValid(ShopUnitType shopUnitType, ConstraintValidatorContext constraintValidatorContext) {
        return shopUnitType != null && Arrays.asList(subset).contains(shopUnitType);
    }

    @Override
    public void initialize(ShopUnitTypeEnum constraintAnnotation) {
        this.subset = constraintAnnotation.anyOf();
    }
}
