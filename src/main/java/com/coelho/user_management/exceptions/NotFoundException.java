package com.coelho.user_management.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;

public class NotFoundException extends Exception {

    public NotFoundException(String message) {
        super(message);
    }

    @Override
    public int statusCode() {
        return HttpResponseStatus.NOT_FOUND.code();
    }
}
