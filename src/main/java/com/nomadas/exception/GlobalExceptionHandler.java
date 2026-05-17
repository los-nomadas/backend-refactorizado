package com.nomadas.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflict(
            ConflictException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, resolveBadRequestMessage(request.getRequestURI()), request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, resolveBadRequestMessage(request.getRequestURI()), request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Resource not found", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request.getRequestURI());
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ErrorResponseDTO> handleInternalServer(
            InternalServerErrorException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
    }

    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(HttpStatus status, String message, String path) {
        ErrorResponseDTO response = new ErrorResponseDTO(
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                LocalDateTime.now().withNano(0)
        );
        return ResponseEntity.status(status).body(response);
    }

    private String resolveBadRequestMessage(String path) {
        if (path != null && path.startsWith("/api/users")) {
            return "Invalid user data";
        }
        if (path != null && path.startsWith("/api/hotels")) {
            return "Invalid hotel data";
        }
        if (path != null && path.startsWith("/api/drivers")) {
            return "Invalid driver data";
        }
        if (path != null && path.startsWith("/api/buses")) {
            return "Invalid bus data";
        }
        if (path != null && path.startsWith("/api/bookings")) {
            return "Invalid booking data";
        }
        if (path != null && path.startsWith("/api/auth")) {
            return "Invalid login data";
        }
        return "Invalid request data";
    }
}

