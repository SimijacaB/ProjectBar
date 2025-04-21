package com.app.projectbar.application.exception;

public enum ErrorMessagesService {

    // ORDERS
    ORDER_ALREADY_BILLED_EXCEPTION("Orders with the following IDs have already been billed ");

    private final String message;

    ErrorMessagesService(String message){
        this.message = message;
    }

}
