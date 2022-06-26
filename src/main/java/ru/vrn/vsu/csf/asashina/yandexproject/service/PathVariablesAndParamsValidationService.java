package ru.vrn.vsu.csf.asashina.yandexproject.service;

import org.springframework.stereotype.Service;
import ru.vrn.vsu.csf.asashina.yandexproject.exception.validation.DateFormatException;

import java.time.Instant;
import java.util.UUID;

@Service
public class PathVariablesAndParamsValidationService {

    public UUID validateId(String variable) {
        try {
            return UUID.fromString(variable);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Validation Failed");
        }
    }

    public Instant validateDate(String date) {
        if (date.matches("((\\d{4})\\-(\\d{2})\\-(\\d{2}))T((\\d{2})\\:(\\d{2})\\:(\\d{2}).(\\d{3}))Z")) {
            try {
                return Instant.parse(date);
            } catch (Exception e) {
                throw new DateFormatException("Validation Failed");
            }
        }
        throw new DateFormatException("Validation Failed");
    }
}
