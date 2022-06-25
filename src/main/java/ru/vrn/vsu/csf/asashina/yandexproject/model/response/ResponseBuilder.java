package ru.vrn.vsu.csf.asashina.yandexproject.model.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class ResponseBuilder {
    public static ResponseEntity<?> build(HttpStatus httpStatus, Map<String, Object> data) {
        return ResponseEntity.status(httpStatus).body(data);
    }

    public static ResponseEntity<?> build(HttpStatus httpStatus, Object data) {
        return ResponseEntity.status(httpStatus).body(data);
    }
}
