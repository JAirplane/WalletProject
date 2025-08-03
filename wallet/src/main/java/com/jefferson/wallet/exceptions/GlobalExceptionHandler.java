package com.jefferson.wallet.exceptions;

import com.jefferson.wallet.enums.OperationType;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.sqm.ParsingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //Controller validation exceptions handling
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>>
        handleValidationArgumentException(MethodArgumentNotValidException exception) {
            Map<String, String> errors = new HashMap<>();
            exception.getBindingResult().getFieldErrors()
                    .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

            log.error("Validation errors found in Controller: {}", errors.size());
            errors.forEach((field, msg) -> log.error("Field: '{}': {}", field, msg));

            return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleParseErrors(HttpMessageNotReadableException exception) {
        Throwable rootCause = exception.getRootCause() != null ? exception.getRootCause() : exception;
        ErrorResponse response = null;
        if(rootCause instanceof IllegalArgumentException &&
                rootCause.getMessage() != null &&
                rootCause.getMessage().contains("OperationType")) {
            response = createErrorResponse("INVALID_ENUM_VALUE",
                "Invalid OperationType. Allowed values: " + Arrays.toString(OperationType.values()));
        }
        else if(rootCause instanceof IllegalArgumentException &&
                rootCause.getMessage() != null &&
                rootCause.getMessage().contains("Invalid UUID")) {
           response = createErrorResponse("INVALID_UUID_FORMAT", "Invalid UUID format");
        }
        else if (rootCause.getMessage() != null && rootCause.getMessage().contains("JSON parse error")) {
            response = createErrorResponse("MALFORMED_JSON", "Invalid JSON format");
        }
        else {
            response = createErrorResponse("INVALID_REQUEST_BODY", "Request body is invalid");
        }

        log.error("HttpMessageNotReadableException occurred: {}.", response.errorTitle());
        return ResponseEntity.badRequest().body(response);
    }

    //Service validation exception handling
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>>
        handleConstraintViolationException(ConstraintViolationException exception) {
            Map<String, String> errors = new HashMap<>();
            exception.getConstraintViolations()
                    .forEach(constraintViolation -> {
                        String path = constraintViolation.getPropertyPath().toString();
                        String[] paths = path.split("\\.");
                        errors.put(paths[paths.length - 1], constraintViolation.getMessage());
                    });

            log.error("Validation errors found in Service: {}.", errors.size());

            errors.forEach((field, msg) -> log.error("Field: '{}': {}", field, msg));

            return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatchException(MethodArgumentTypeMismatchException exception) {
            log.error("MethodArgumentTypeMismatchException occurred. Message: {}", exception.getMessage());

            return ResponseEntity.badRequest()
                    .body(Map.of("Error", "Invalid format: " + exception.getValue()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        log.error("DataIntegrityViolationException occurred. Message: {}", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("Error", "Unique index or primary key violation."));
    }

    @ExceptionHandler(ParsingException.class)
    public ResponseEntity<Map<String, String>> handleQueryParsingException(ParsingException exception) {
        log.error("ParsingException occurred. Message: {}", exception.getMessage());

        return ResponseEntity
                .internalServerError()
                .body(Map.of("Error", "Bad query parsing."));
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleWalletNotFoundException(WalletNotFoundException exception) {
        log.warn(exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("Error", exception.getMessage()));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientException(InsufficientFundsException exception) {
        log.warn(exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("Error", exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleCommonException(Exception exception) {
        log.error("Common exception occurred. Message: {}", exception.getMessage());
        return ResponseEntity.internalServerError()
                .body(Map.of("Error", exception.getMessage()));
    }

    private ErrorResponse createErrorResponse(String code, String message) {
        return new ErrorResponse(code, message, Instant.now());
    }
}
