package com.app.projectbar.application.exception;

public enum ErrorMessagesService {

    // ORDERS
    ORDER_ALREADY_BILLED_EXCEPTION("Orders with the following IDs have already been billed "),
    ORDER_NOT_FOUND_BY_ID_EXCEPTION("We haven't found an order with this id."),
    ORDERS_NOT_FOUND_BY_STATUS_EXCEPTION("We haven't found orders with the indicated status."),

    // BILLS
    BILL_NOT_FOUND_BY_ID_EXCEPTION("We haven't found a bill with this id."),
    BILL_NOT_FOUND_BY_NUMBER_EXCEPTION("We haven't found a bill with this number.");

    private final String message;

    ErrorMessagesService(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
