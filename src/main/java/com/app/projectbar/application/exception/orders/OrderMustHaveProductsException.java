package com.app.projectbar.application.exception.orders;

public class OrderMustHaveProductsException extends RuntimeException {
    public OrderMustHaveProductsException(String message) {
        super(message);
    }
}
