package com.app.projectbar.application.exception.orders;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class OrdersNotFoundByStatusException extends RuntimeException {
    public OrdersNotFoundByStatusException(String message) {
        super(message);
    }
}
