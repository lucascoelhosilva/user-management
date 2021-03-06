package com.coelho.user_management.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;

public class UnauthorizedException extends Exception {

  public UnauthorizedException(String message) {
    super(message);
  }

  @Override
  public int statusCode() {
    return HttpResponseStatus.UNAUTHORIZED.code();
  }

}