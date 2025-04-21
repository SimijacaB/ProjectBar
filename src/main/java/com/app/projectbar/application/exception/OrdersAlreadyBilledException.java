package com.app.projectbar.application.exception;

public class OrdersAlreadyBilledException extends RuntimeException {
    public OrdersAlreadyBilledException(String message) {
        super(message);
    }
}
