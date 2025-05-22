package io.github.irfnhanif.rifasims.exception;

import io.github.irfnhanif.rifasims.dto.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

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
}
