package com.app.projectbar.application.exception.bill;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class BillNotFoundByNumberException extends RuntimeException {
    public BillNotFoundByNumberException(String message) {
        super(message);
    }
}
