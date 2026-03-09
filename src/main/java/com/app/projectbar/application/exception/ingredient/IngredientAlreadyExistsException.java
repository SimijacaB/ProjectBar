package com.app.projectbar.application.exception.ingredient;

public class IngredientAlreadyExistsException extends RuntimeException {
    public IngredientAlreadyExistsException(String message) {
        super(message);
    }
}
