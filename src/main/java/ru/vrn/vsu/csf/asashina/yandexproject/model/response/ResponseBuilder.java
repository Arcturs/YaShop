package ru.vrn.vsu.csf.asashina.yandexproject.model.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseBuilder {
    public static ResponseEntity<?> build(HttpStatus httpStatus, Object data) {
        return ResponseEntity.status(httpStatus).body(data);
    }
}
