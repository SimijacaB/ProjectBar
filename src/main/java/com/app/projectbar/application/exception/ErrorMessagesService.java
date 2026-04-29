package com.app.projectbar.application.exception;

public enum ErrorMessagesService {

    // ORDERS
    ORDER_ALREADY_BILLED_EXCEPTION("Orders with the following IDs have already been billed "),
    ORDER_NOT_FOUND_BY_ID_EXCEPTION("We haven't found an order with this id."),
    ORDERS_NOT_FOUND_BY_STATUS_EXCEPTION("We haven't found orders with the indicated status."),
    ORDER_MUST_HAVE_PRODUCTS("Order must have at least one product."),
    ORDER_ITEM_NOT_FOUND("Order item not found with id: %s"),
    CANNOT_ADD_ITEMS_TO_DELIVERED_ORDER("Cannot add items to a delivered order. Please create a new order."),
    CANNOT_BILL_NOT_DELIVERED_ORDERS("Cannot bill orders that are not DELIVERED. Order IDs: %s"),
    ORDER_IDS_NOT_EXIST("The following Order IDs do not exist: %s"),
    INVALID_STATUS_TRANSITION("Invalid status transition from %s to %s"),
    WAITER_REQUIRED_FOR_IN_PROGRESS("Cannot change to IN_PROGRESS without an assigned waiter."),
    CREATED_ORDER_CAN_ONLY_BE_ASSIGNED_OR_CANCELLED("Order in CREATED status can only be ASSIGNED or CANCELLED."),
    ASSIGNED_ORDER_CAN_ONLY_MOVE_TO_IN_PROGRESS_OR_CANCELLED("Order in ASSIGNED status can only move to IN_PROGRESS or CANCELLED."),
    ONLY_CREATED_ORDERS_CAN_BE_ASSIGNED("Only orders in CREATED status can be assigned. Current status: %s"),

    // PRODUCTS
    PRODUCT_NOT_FOUND_BY_ID("Product not found with id: %s"),
    PRODUCT_NOT_FOUND_BY_CODE("Product not found with code: %s"),
    PRODUCT_NOT_FOUND_BY_NAME("Product not found with name: %s"),
    PRODUCT_ID_OR_NAME_REQUIRED("Product ID or name is required."),
    PRODUCT_NAME_ALREADY_EXISTS("We already have a product with that name."),
    PRODUCT_CODE_ALREADY_EXISTS("We already have a product with that code."),
    PRODUCT_ALREADY_EXISTS_BY_CODE("Another product with code '%s' already exists"),
    PRODUCT_ALREADY_EXISTS_BY_NAME("Another product with name '%s' already exists"),

    // INGREDIENTS
    INGREDIENT_NOT_FOUND_EXCEPTION("Ingredient not found."),

    // INVENTORY
    INSUFFICIENT_INGREDIENT_INVENTORY("Insufficient inventory of ingredient '%s' for product '%s'. %s"),
    INSUFFICIENT_PRODUCT_INVENTORY("Insufficient inventory of product '%s'. %s"),
    INVENTORY_NOT_FOUND_BY_CODE("Inventory not found with code: %s"),
    INVENTORY_ALREADY_EXISTS("Inventory already exists with code: %s"),
    PRODUCT_OR_INGREDIENT_NOT_FOUND_BY_CODE("Product or Ingredient not found with code: %s"),
    PREPARED_PRODUCT_CANNOT_HAVE_INVENTORY("Cannot create inventory for prepared products. Their availability depends on ingredient stock."),
    INSUFFICIENT_INVENTORY_TO_DEDUCT("There is not enough inventory to deduct for code: %s"),

    // INGREDIENTS
    INGREDIENT_NOT_FOUND_BY_ID("Ingredient not found with id: %s"),
    INGREDIENT_NOT_FOUND_BY_CODE("Ingredient not found with code: %s"),
    INGREDIENT_ID_REQUIRED("Ingredient id is required to update"),
    INGREDIENT_ALREADY_EXISTS_BY_CODE("Another ingredient with code '%s' already exists"),

    // TABLE
    TABLE_NOT_FOUND_BY_NUMBER("Table not found with number: %s. Please create the table first."),

    // AUTH
    USER_NOT_AUTHENTICATED("User must be authenticated to perform this action."),
    INVALID_CREDENTIALS("Invalid email or password"),

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
