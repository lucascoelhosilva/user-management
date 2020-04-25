package com.coelho.api_manager_users.middleware;

import com.coelho.api_manager_users.constants.Constants;
import com.coelho.api_manager_users.security.hydra.HydraService;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import sh.ory.hydra.model.OAuth2TokenIntrospection;

public class RequestHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestHelper.class);
  private HydraService hydraService;

  public RequestHelper() {
    this.hydraService = new HydraService();
  }

  public void validateAccessToKen(RoutingContext rc) {

    if (StringUtils.isBlank(rc.request().getHeader("auth")) ) {
      LOGGER.error("Header 'auth' is required");
      LOGGER.error("REDIRECT TO AUTHORIZE");
      rc.response()
              .putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON_UTF8)
              .setStatusCode(302)
              .putHeader("location", "http://localhost:9090/api-users/authorize").end();
      return;
    } else {
      OAuth2TokenIntrospection introspection = hydraService.instrospect(rc.request().getHeader("auth"), "offline");
      if (introspection.getActive().equals(false)) {
        LOGGER.error("UNAUTHORIZED");
        rc.response()
                .setStatusCode(403)
                .end("UNAUTHORIZED");
      }
    }

    rc.next();
  }
}