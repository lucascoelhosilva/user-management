package com.coelho.api_manager_users.exceptions;

import com.coelho.api_manager_users.constants.Constants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class UnauthorizedException extends RuntimeException {

  public UnauthorizedException(RoutingContext rc) {
    rc.response()
      .putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON_UTF8)
      .setStatusCode(HttpResponseStatus.UNAUTHORIZED.code())
      .end(Json.encodePrettily(new JsonObject().put("message", "UNAUTHORIZED")));
  }

}
