package io.github.irfnhanif.rifasims.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import io.github.irfnhanif.rifasims.dto.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<Void>> handleGenericException(Exception e) {
        APIResponse<Void> response = new APIResponse<>(false, "Internal server error", null,
                Collections.singletonList(e.getMessage()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException e) {
        APIResponse<Void> response = new APIResponse<>(false, e.getMessage(), null, Collections.singletonList(e.getMessage()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<APIResponse<Void>> handleBadRequestException(BadRequestException e) {
        APIResponse<Void> response = new APIResponse<>(false, e.getMessage(), null, Collections.singletonList(e.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<APIResponse<Void>> handleInvalidCredentialsException(InvalidCredentialsException e) {
        APIResponse<Void> response = new APIResponse<>(false, e.getMessage(), null, Collections.singletonList(e.getMessage()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(io.github.irfnhanif.rifasims.exception.AccessDeniedException.class)
    public ResponseEntity<APIResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        APIResponse<Void> response = new APIResponse<>(false, e.getMessage(), null, Collections.singletonList(e.getMessage()));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<APIResponse<Void>> handleOrganicAccessDeniedException(AccessDeniedException e) {
        APIResponse<Void> response = new APIResponse<>(false, e.getMessage(), null, Collections.singletonList(e.getMessage()));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<APIResponse<Void>> handleInternalServerException(InternalServerException e) {
        APIResponse<Void> response = new APIResponse<>(false, e.getMessage(), null, Collections.singletonList(e.getMessage()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        APIResponse<Void> response = new APIResponse<>(false, "Validation failed", null, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<APIResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String message = "Format data tidak valid";
        if (e.getCause() instanceof JsonMappingException) {
            message = "Format data tidak valid: pastikan tipe data sesuai";
        }

        APIResponse<Void> response = new APIResponse<>(false, message, null,
                Collections.singletonList(message));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
