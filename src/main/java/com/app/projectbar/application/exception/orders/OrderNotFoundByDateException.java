package com.app.projectbar.application.exception.orders;

public class OrderNotFoundByDateException extends RuntimeException {
  public OrderNotFoundByDateException(String message) {
    super(message);
  }
}
