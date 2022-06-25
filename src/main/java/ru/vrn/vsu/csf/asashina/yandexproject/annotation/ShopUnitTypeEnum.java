package ru.vrn.vsu.csf.asashina.yandexproject.annotation;

import ru.vrn.vsu.csf.asashina.yandexproject.model.enumeration.ShopUnitType;
import ru.vrn.vsu.csf.asashina.yandexproject.validators.ShopUnitTypeEnumValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = ShopUnitTypeEnumValidator.class)
public @interface ShopUnitTypeEnum {

    ShopUnitType[] anyOf();

    String message() default "must be any of {anyOf}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
