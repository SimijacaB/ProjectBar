package com.app.projectbar.infra.errorHandler;

import com.app.projectbar.application.exception.bill.BillNotFoundByIdException;
import com.app.projectbar.application.exception.bill.BillNotFoundByNumberException;
import com.app.projectbar.application.exception.orders.OrderNotFoundByIdException;
import com.app.projectbar.application.exception.orders.OrdersAlreadyBilledException;
import com.app.projectbar.application.exception.orders.OrdersNotFoundByStatusException;
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

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Application Layer
    // Orders
    @ExceptionHandler(OrderNotFoundByIdException.class)
    public ResponseEntity<Map<String, String>> handlerOrderNotFoundException(OrderNotFoundByIdException ex){
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OrdersAlreadyBilledException.class)
    public ResponseEntity<Map<String, String>> handlerOrdersAlreadyBilledException(OrdersAlreadyBilledException ex){
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(OrdersNotFoundByStatusException.class)
    public ResponseEntity<Map<String, String>> handlerOrdersNotFoundByStatusException(OrdersNotFoundByStatusException ex){
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Bills
    @ExceptionHandler(BillNotFoundByIdException.class)
    public ResponseEntity<Map<String, String>> handlerBillNotFoundByIdException(BillNotFoundByIdException ex){
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BillNotFoundByNumberException.class)
    public ResponseEntity<Map<String, String>> handlerBillNotFoundByNumberException(BillNotFoundByNumberException ex){
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
