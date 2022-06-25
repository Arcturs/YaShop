package ru.vrn.vsu.csf.asashina.yandexproject.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.vrn.vsu.csf.asashina.yandexproject.exception.validation.*;
import ru.vrn.vsu.csf.asashina.yandexproject.exception.ObjectNotFoundException;
import ru.vrn.vsu.csf.asashina.yandexproject.model.dto.Error;
import ru.vrn.vsu.csf.asashina.yandexproject.model.response.ResponseBuilder;

@ControllerAdvice
@Slf4j
public class ErrorHandlerController {

    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    private static final String BAD_REQUEST_ERROR = "Validation Failed";

    private static final Integer INTERNAL_SERVER_ERROR_STATUS_CODE = 500;
    private static final Integer BAD_REQUEST_STATUS_CODE = 400;
    private static final Integer NOT_FOUND_STATUS_CODE = 404;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> externalServerError(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR,
                new Error(INTERNAL_SERVER_ERROR_STATUS_CODE, INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler({IllegalArgumentException.class, DateFormatException.class, WrongParentTypeException.class,
            PriceException.class, IdException.class, UpdatingTypeException.class})
    public ResponseEntity<?> badRequest(Exception e) {
        return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                new Error(BAD_REQUEST_STATUS_CODE, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> badRequestForPreValidation(MethodArgumentNotValidException e) {
        return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                new Error(BAD_REQUEST_STATUS_CODE, BAD_REQUEST_ERROR));
    }

    @ExceptionHandler({ObjectNotFoundException.class})
    public ResponseEntity<?> notFount(Exception e) {
        return ResponseBuilder.build(HttpStatus.NOT_FOUND,
                new Error(NOT_FOUND_STATUS_CODE, e.getMessage()));
    }
}
