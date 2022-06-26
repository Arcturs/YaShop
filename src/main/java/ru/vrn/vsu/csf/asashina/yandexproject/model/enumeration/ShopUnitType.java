package ru.vrn.vsu.csf.asashina.yandexproject.model.enumeration;

public enum ShopUnitType {
    OFFER("OFFER"),
    CATEGORY("CATEGORY");

    private final String code;

    ShopUnitType(String code) {
        this.code = code;
    }

    public static ShopUnitType fromStringToTag(String s) {
        if (s.equals("OFFER")) {
            return OFFER;
        }
        return CATEGORY;
    }

    public String getCode() {
        return code;
    }
}
