package com.coelho.api_manager_users.handlers;

import com.coelho.api_manager_users.repositories.UserRepository;
import com.coelho.api_manager_users.security.hydra.HydraService;
import io.vertx.core.http.Cookie;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import sh.ory.hydra.model.AcceptConsentRequest;
import sh.ory.hydra.model.AcceptLoginRequest;
import sh.ory.hydra.model.CompletedRequest;
import sh.ory.hydra.model.ConsentRequest;
import sh.ory.hydra.model.LoginRequest;
import sh.ory.hydra.model.OAuth2TokenIntrospection;

import java.util.UUID;

public class AuthenticationHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationHandler.class);

  private final UserRepository userRepository;
  private OAuth2Auth oauth2;
  private HydraService hydraService;
  private WebClient client;

  public AuthenticationHandler(JDBCClient jdbcClient, OAuth2Auth oauth2, WebClient client) {
    this.userRepository = new UserRepository(jdbcClient);
    this.oauth2 = oauth2;
    this.hydraService = new HydraService();
    this.client = client;
  }

  public void authorize(RoutingContext rc) {
    String state = UUID.randomUUID().toString();

    String authorization_uri = oauth2.authorizeURL(new JsonObject()
            .put("redirect_uri", "http://localhost:9090/api-users/auth-callback")
            .put("scope", "offline")
            .put("state", state));

    LOGGER.info("AUTHORIZE REDIRECT == {0}", authorization_uri);

    rc.response()
            .addCookie(Cookie.cookie("state", state))
            .putHeader("location", authorization_uri)
            .setStatusCode(302).end();
    rc.next();
  }


  public void login(RoutingContext rc) {
    String challenge = rc.request().getParam("login_challenge");

    LoginRequest loginResquest = hydraService.getLoginRequest(challenge);
      AcceptLoginRequest body = new AcceptLoginRequest();
      body.subject("test");
      CompletedRequest completed = hydraService.acceptLoginRequest(challenge, body);

      String redirect = completed.getRedirectTo();
      LOGGER.info("LOGIN REDIRECT == {0}", completed.getRedirectTo());
      rc.response().putHeader("location", redirect).setStatusCode(302).end();
      rc.next();
  }

  public void consent(RoutingContext rc) {
    LOGGER.info("CONSENT ============");

    ConsentRequest consentRequest = hydraService.getConsentRequest(rc.request().getParam("consent_challenge"));
    AcceptConsentRequest body = new AcceptConsentRequest();
    body.grantScope(consentRequest.getRequestedScope());
    body.grantAccessTokenAudience(consentRequest.getRequestedAccessTokenAudience());
    body.remember(false);
    body.rememberFor(3600L);

    CompletedRequest acceptConsent = hydraService.acceptConsentRequest(consentRequest.getChallenge(), body);

    String redirect = acceptConsent.getRedirectTo();
    LOGGER.info("ACCEPT REDIRECT ============ {0}", redirect);
    rc.response().putHeader("location", redirect).setStatusCode(302).end();
    rc.next();
  }

  public void callback(RoutingContext rc) {
    LOGGER.info("CALLBACK == {0}", rc.request().params().toString());

    JsonObject tokenConfig = new JsonObject();
    tokenConfig.put("access_token", rc.request().params().get("code"));
    tokenConfig.put("scope", rc.request().params().get("scope"));
    tokenConfig.put("redirect_uri", "http://localhost:4444/oauth2/auth");

    JsonObject authenticate = new JsonObject()
            .put("code", rc.request().params().get("code"))
            .put("grant_type", "client_credentials")
            .put("redirect_uri", "http://localhost:9090/api-users/auth-callback");

    oauth2.authenticate(authenticate, res -> {
      if (res.failed()) {
        LOGGER.info("OAUTH AUTHENTICATED FAILED {0}", res.cause().getMessage());
        rc.response().setStatusCode(500).end("FAILED TO AUTHENTICATED");
      } else {
        // save the token and continue...
        LOGGER.info("OAUTH AUTHENTICATED RESULT {0}", res.result());

        OAuth2TokenIntrospection test = hydraService.instrospect(res.result().principal().getString("access_token"), "offline");
        LOGGER.info("HYDRA INTROSPECT ===== {0}", test.toString());

        rc.response().setStatusCode(200).end(res.result().principal().getString("access_token"));
      }
    });
  }
}