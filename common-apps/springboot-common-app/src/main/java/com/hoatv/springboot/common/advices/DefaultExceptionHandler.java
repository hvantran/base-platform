package com.hoatv.springboot.common.advices;

import com.hoatv.fwk.common.exceptions.AppException;
import com.hoatv.fwk.common.exceptions.DuplicateResourceException;
import com.hoatv.fwk.common.exceptions.EntityNotFoundException;
import com.hoatv.fwk.common.exceptions.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
public class DefaultExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    @ResponseBody
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        LOGGER.error("An MethodArgumentNotValidException occurred while processing", ex);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(errors);
    }

    /**
     * Handle validation errors from @Min, @Max, @Size annotations on method parameters
     * Introduced in Spring Boot 3.x
     */
    @ResponseBody
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        LOGGER.error("A HandlerMethodValidationException occurred while processing", ex);
        
        // Extract validation error messages
        String errors = ex.getAllValidationResults().stream()
            .flatMap(result -> result.getResolvableErrors().stream())
            .map(error -> error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", errors.isEmpty() ? "Validation failure" : errors);
        
        return ResponseEntity.badRequest()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(errorResponse);
    }

    @ResponseBody
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<Object> handleInvalidArgumentException(InvalidArgumentException ex) {
        LOGGER.error("An InvalidArgumentException occurred while processing", ex);
        return ResponseEntity.badRequest()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("message", ex.getMessage()));
    }

    /**
     * Handle IllegalArgumentException from PageRequest and other validation failures
     */
    @ResponseBody
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        LOGGER.error("An IllegalArgumentException occurred while processing", ex);
        return ResponseEntity.badRequest()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(value= {AppException.class})
    protected ResponseEntity<Object> handleAppException(RuntimeException ex, WebRequest request) {
        LOGGER.error("An AppException occurred while processing", ex);
        String responseMessage = String.format("{\"message\": \"%s\"}", ex.getMessage());
        request.setAttribute("jakarta.servlet.error.exception", ex, 0);
        return ResponseEntity.internalServerError()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(responseMessage);
    }

    @ExceptionHandler(value = {EntityNotFoundException.class})
    protected ResponseEntity<Object> handleEntityNotFound(RuntimeException ex, WebRequest request) {
        LOGGER.error("An EntityNotFoundException occurred while processing", ex);
        String responseMessage = String.format("{\"message\": \"%s\"}", ex.getMessage());
        request.setAttribute("jakarta.servlet.error.exception", ex, 0);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(responseMessage);
    }

    @ExceptionHandler(value = {DuplicateResourceException.class})
    protected ResponseEntity<Object> handleDuplicateResource(RuntimeException ex, WebRequest request) {
        LOGGER.error("An DuplicateResourceException occurred while processing", ex);
        String responseMessage = String.format("{\"message\": \"%s\"}", ex.getMessage());
        request.setAttribute("jakarta.servlet.error.exception", ex, 0);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(responseMessage);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal error: " + ex.getMessage());
    }
}
