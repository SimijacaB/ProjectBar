package com.app.projectbar.application.exception.table;

public class TableNotFoundException extends RuntimeException {
    public TableNotFoundException(String message) {
        super(message);
    }
}
