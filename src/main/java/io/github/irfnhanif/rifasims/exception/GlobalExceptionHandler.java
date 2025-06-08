package io.github.irfnhanif.rifasims.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import io.github.irfnhanif.rifasims.dto.APIResponse;
import jakarta.transaction.RollbackException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<Void>> handleGenericException(Exception e) {
        log.error("Exception caught:", e);

        String message = "Internal server error";
        String detailedMessage = e.getMessage();

        // Extract root cause for transaction exceptions
        if (e instanceof TransactionSystemException) {
            Throwable cause = e.getCause();
            if (cause instanceof RollbackException && cause.getCause() != null) {
                // This is typically where validation errors are
                Throwable rootCause = cause.getCause();
                log.error("Root cause:", rootCause);

                if (rootCause instanceof ConstraintViolationException) {
                    detailedMessage = "Constraint violation: " +
                            ((ConstraintViolationException) rootCause).getConstraintName() + " - " +
                            rootCause.getMessage();
                } else {
                    detailedMessage = rootCause.getMessage();
                }
            }
        }

        APIResponse<Void> response = new APIResponse<>(false, message, null,
                Collections.singletonList(detailedMessage));
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
        System.out.println("Exception caught in GlobalExceptionHandler: \n" + e);
        APIResponse<Void> response = new APIResponse<>(false, e.getMessage(), null, Collections.singletonList(e.getMessage()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(io.github.irfnhanif.rifasims.exception.AccessDeniedException.class)
    public ResponseEntity<APIResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        APIResponse<Void> response = new APIResponse<>(false, e.getMessage(), null, Collections.singletonList(e.getMessage()));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<APIResponse<Void>> handleSpringSecurityAccessDeniedException(AccessDeniedException e) {
        APIResponse<Void> response = new APIResponse<>(false, e.getMessage(), null, Collections.singletonList(e.getMessage()));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<APIResponse<Void>> handleSpringSecurityBadCrendentialsException(BadCredentialsException e) {
        APIResponse<Void> response = new APIResponse<>(false, "Username atau Password Salah", null, Collections.singletonList(e.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
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
