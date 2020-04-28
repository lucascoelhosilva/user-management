package com.coelho.api_manager_users.middleware;

import com.coelho.api_manager_users.constants.Constants;
import com.coelho.api_manager_users.exceptions.UnauthorizedException;
import com.coelho.api_manager_users.hydra.HydraService;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import sh.ory.hydra.model.OAuth2TokenIntrospection;

public class RequestHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestHelper.class);
  private final HydraService hydraService;

  public RequestHelper(HydraService hydraService) {
    this.hydraService = hydraService;
  }

  public void validateAccessToKen(RoutingContext rc) {
    if (StringUtils.isBlank(rc.request().getHeader(Constants.HEADER_AUTHORIZATION))) {
      LOGGER.error("Header 'authorization' is required");
      LOGGER.error("REDIRECT TO AUTHORIZE");

      rc.response()
              .putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON_UTF8)
              .setStatusCode(HttpResponseStatus.FOUND.code())
              .putHeader(HttpHeaders.LOCATION, "http://localhost:9090/api-users/authorize").end();
      rc.next();
    } else {
      OAuth2TokenIntrospection introspection = hydraService.instrospect(rc.request().getHeader(Constants.HEADER_AUTHORIZATION), null);
      if (introspection.getActive().equals(false)) {
        LOGGER.error("token active ==== {0}", introspection.getActive());
        throw new UnauthorizedException("Unauthorized");
      }
    }
  }

}