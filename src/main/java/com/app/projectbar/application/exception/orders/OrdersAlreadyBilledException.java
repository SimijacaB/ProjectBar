package com.app.projectbar.application.exception.orders;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class OrdersAlreadyBilledException extends RuntimeException {
    public OrdersAlreadyBilledException(String message) {
        super(message);
    }
}
