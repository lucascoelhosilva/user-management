package com.coelho.user_management.exceptions;

public abstract class Exception extends RuntimeException {

    public Exception(String message) {
        super(message);
    }

    public abstract int statusCode();
}