package com.toedter.spring.hateoas.jsonapi.example.exception;

import com.toedter.spring.hateoas.jsonapi.JsonApiErrors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

// tag::exception-controller-advice[]
@ControllerAdvice(annotations = RestController.class)
@Slf4j
public class ExceptionControllerAdvice {
    /**
     * Handles all manually thrown exceptions of type JsonApiErrorsException.
     */
    @ExceptionHandler
    public ResponseEntity<JsonApiErrors> handle(JsonApiErrorsException ex) {
        log.error("JSON:API error: Http status:{}, message:{}", ex.getStatus(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(ex.getErrors());
    }

    /**
     * Handles all exceptions which are not of type JsonAPIErrorsException and treats
     * them as internal errors or maps specific exceptions to specific HTTP status codes.
     */
    @ExceptionHandler
    public ResponseEntity<JsonApiErrors> handle(Exception ex) {
        log.error("Internal error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                body(JsonApiErrors.create().withError(
                        CommonErrors.newInternalServerError().withDetail(ex.getMessage())));
    }
}
// end::exception-controller-advice[]
