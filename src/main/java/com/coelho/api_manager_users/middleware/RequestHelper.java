package com.coelho.api_manager_users.middleware;

import com.coelho.api_manager_users.constants.Constants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;

public class RequestHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestHelper.class);

  public static void validateAccessToKen(RoutingContext rc) {

    if (StringUtils.isBlank(rc.request().getHeader("auth")) ) {
      LOGGER.error("Header 'auth' is required");
      rc.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON_UTF8)
        .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
        .end(Json.encodePrettily(new JsonObject().put("message", "Header 'auth' is required")));
      return;
    }

    rc.next();
  }
}
