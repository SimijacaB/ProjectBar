package com.app.projectbar.application.exception.bill;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class BillNotFoundByIdException extends RuntimeException {
    public BillNotFoundByIdException(String message) {
        super(message);
    }
}
