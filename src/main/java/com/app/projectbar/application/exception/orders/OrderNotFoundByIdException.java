package com.app.projectbar.application.exception.orders;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class OrderNotFoundByIdException extends RuntimeException {
    public OrderNotFoundByIdException(String message) {
        super(message);
    }
}
