package com.coelho.api_manager_users.exceptions;

public abstract class Exception extends RuntimeException {

    public Exception(String message) {
        super(message);
    }

    public abstract int statusCode();
}