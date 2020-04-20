package com.coelho.api_manager_users.handlers;

import com.coelho.api_manager_users.repositories.UserRepository;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.oauth2.AccessToken;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.RoutingContext;

public class AuthenticationHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationHandler.class);

  private final UserRepository userRepository;
  private OAuth2Auth oauth2;

  public AuthenticationHandler(JDBCClient jdbcClient, OAuth2Auth oauth2) {
    this.userRepository = new UserRepository(jdbcClient);
    this.oauth2 = oauth2;
  }

  public void auth(RoutingContext rc) {
    String authorization_uri = oauth2.authorizeURL(new JsonObject()
      .put("scope", "offline")
      .put("state", true));

    oauth2.authenticate(new JsonObject()
      .put("redirect_uri", "http://localhost:9090/api-users/auth-callback")
      .put("scope", "offline")
      .put("state", true), handler -> {

      if(handler.succeeded()) {
        handler.result().principal().getString("access-token");
        LOGGER.info("ALLALALALALLALALALA ======= {0}", handler.result());
      }
    });

    LOGGER.info("authorization_uri ======= ", authorization_uri);


    // Redirect example using Vert.x
    rc.response()
      .putHeader("Location", authorization_uri)
      .setStatusCode(302)
      .end();

    JsonObject tokenConfig = new JsonObject()
      .put("code", "<code>")
      .put("redirect_uri", "http://localhost:9090/api-users/auth-callback");

    // Callbacks
    // Save the access token
    oauth2.getToken(tokenConfig, res -> {
      if (res.failed()) {
        System.err.println("Access Token Error: " + res.cause().getMessage());
      } else {
        // Get the access token object (the authorization code is given from the previous step).
        AccessToken token = res.result();
      }
    });
  }
}
