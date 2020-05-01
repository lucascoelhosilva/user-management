package com.coelho.user_management.middleware;

import com.coelho.user_management.constants.Constants;
import com.coelho.user_management.exceptions.UnauthorizedException;
import com.coelho.user_management.security.HydraService;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import sh.ory.hydra.model.OAuth2TokenIntrospection;

import static java.util.Objects.nonNull;

public class RequestHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestHelper.class);
  private final HydraService hydraService;

  public RequestHelper(HydraService hydraService) {
    this.hydraService = hydraService;
  }

  public void validateAccessToKen(RoutingContext rc) {

    User user;
    if (rc.session() == null) {
      user = rc.user();
    } else {
      user = rc.session().get("user");
    }

    if(nonNull(user) || rc.request().headers().get("Authorization") != null) {
      String token = user !=null ? user.principal().getString("access_token") : rc.request().headers().get("Authorization");
      OAuth2TokenIntrospection introspection = hydraService.instrospect(token, null);
      if (introspection.getActive().equals(false)) {
        LOGGER.error("token active ==== {0}", introspection.getActive());
        throw new UnauthorizedException("Unauthorized");
      }
    } else {
      LOGGER.error("Header 'authorization' is required");
      LOGGER.error("REDIRECT TO AUTHORIZE");

      rc.response()
              .putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON_UTF8)
              .setStatusCode(HttpResponseStatus.FOUND.code())
              .putHeader(HttpHeaders.LOCATION, "http://localhost:9090/api-users/authorize").end();
    }
    rc.next();
  }
}