package com.app.projectbar.infra.errorHandler;

import com.app.projectbar.application.exception.auth.InvalidCredentialsException;
import com.app.projectbar.application.exception.auth.UserNotAuthenticatedException;
import com.app.projectbar.application.exception.bill.BillNotFoundByIdException;
import com.app.projectbar.application.exception.bill.BillNotFoundByNumberException;

import com.app.projectbar.application.exception.orders.OrderNotFoundByIdException;
import com.app.projectbar.application.exception.orders.OrdersAlreadyBilledException;
import com.app.projectbar.application.exception.orders.OrdersNotFoundByStatusException;
import com.app.projectbar.application.exception.product.ProductAlreadyExistsException;
import com.app.projectbar.application.exception.product.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ── Orders ──────────────────────────────────────────────────────────────

    @ExceptionHandler(OrderNotFoundByIdException.class)
    public ResponseEntity<Map<String, String>> handleOrderNotFoundException(OrderNotFoundByIdException ex) {
        return notFound(ex.getMessage());
    }

    @ExceptionHandler(OrdersAlreadyBilledException.class)
    public ResponseEntity<Map<String, String>> handleOrdersAlreadyBilledException(OrdersAlreadyBilledException ex) {
        return conflict(ex.getMessage());
    }

    @ExceptionHandler(OrdersNotFoundByStatusException.class)
    public ResponseEntity<Map<String, String>> handleOrdersNotFoundByStatusException(OrdersNotFoundByStatusException ex) {
        return notFound(ex.getMessage());
    }

    @ExceptionHandler(OrderItemNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleOrderItemNotFoundException(OrderItemNotFoundException ex) {
        return notFound(ex.getMessage());
    }

    @ExceptionHandler(OrderMustHaveProductsException.class)
    public ResponseEntity<Map<String, String>> handleOrderMustHaveProductsException(OrderMustHaveProductsException ex) {
        return badRequest(ex.getMessage());
    }

    @ExceptionHandler(InvalidOrderStatusTransitionException.class)
    public ResponseEntity<Map<String, String>> handleInvalidOrderStatusTransitionException(InvalidOrderStatusTransitionException ex) {
        return badRequest(ex.getMessage());
    }

    @ExceptionHandler(CannotModifyDeliveredOrderException.class)
    public ResponseEntity<Map<String, String>> handleCannotModifyDeliveredOrderException(CannotModifyDeliveredOrderException ex) {
        return conflict(ex.getMessage());
    }

    // ── Bills ────────────────────────────────────────────────────────────────

    @ExceptionHandler(BillNotFoundByIdException.class)
    public ResponseEntity<Map<String, String>> handleBillNotFoundByIdException(BillNotFoundByIdException ex) {
        return notFound(ex.getMessage());
    }

    @ExceptionHandler(BillNotFoundByNumberException.class)
    public ResponseEntity<Map<String, String>> handleBillNotFoundByNumberException(BillNotFoundByNumberException ex) {
        return notFound(ex.getMessage());
    }

    // ── Products ─────────────────────────────────────────────────────────────

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProductNotFoundException(ProductNotFoundException ex) {
        return notFound(ex.getMessage());
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleProductAlreadyExistsException(ProductAlreadyExistsException ex) {
        return conflict(ex.getMessage());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ResponseEntity<Map<String, String>> notFound(String message) {
        return buildResponse(message, HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<Map<String, String>> conflict(String message) {
        return buildResponse(message, HttpStatus.CONFLICT);
    }

    private ResponseEntity<Map<String, String>> badRequest(String message) {
        return buildResponse(message, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Map<String, String>> unauthorized(String message) {
        return buildResponse(message, HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity<Map<String, String>> buildResponse(String message, HttpStatus status) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", message);
        return new ResponseEntity<>(errorResponse, status);
    }
}
