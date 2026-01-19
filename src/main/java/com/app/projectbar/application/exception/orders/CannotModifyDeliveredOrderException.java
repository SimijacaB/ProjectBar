package com.app.projectbar.application.exception.orders;

public class CannotModifyDeliveredOrderException extends RuntimeException {
    public CannotModifyDeliveredOrderException(String message) {
        super(message);
    }
}
