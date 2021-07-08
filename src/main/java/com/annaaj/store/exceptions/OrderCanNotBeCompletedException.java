package com.annaaj.store.exceptions;

public class OrderCanNotBeCompletedException extends IllegalArgumentException {
    public OrderCanNotBeCompletedException(String msg) {
        super(msg);
    }
}
