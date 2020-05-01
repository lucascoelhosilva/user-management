package com.coelho.user_management.handlers;

import com.coelho.user_management.constants.Constants;
import com.coelho.user_management.exceptions.UnauthorizedException;
import com.coelho.user_management.security.HydraService;
import com.coelho.user_management.repositories.UserRepository;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.common.template.TemplateEngine;
import sh.ory.hydra.model.AcceptConsentRequest;
import sh.ory.hydra.model.AcceptLoginRequest;
import sh.ory.hydra.model.CompletedRequest;
import sh.ory.hydra.model.ConsentRequest;
import sh.ory.hydra.model.LoginRequest;
import sh.ory.hydra.model.OAuth2TokenIntrospection;
import sh.ory.hydra.model.RejectRequest;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AuthenticationHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationHandler.class);

  private static final String LOGIN_CHALLENGE = "login_challenge";
  private static final String CONSENT_CHALLENGE = "consent_challenge";

  private final UserRepository userRepository;
  private final OAuth2Auth oauth2;
  private final HydraService hydraService;
  private final TemplateEngine engine;

  public AuthenticationHandler(JDBCClient jdbcClient, OAuth2Auth oauth2, HydraService hydraService, TemplateEngine engine) {
    this.userRepository = new UserRepository(jdbcClient);
    this.oauth2 = oauth2;
    this.hydraService = hydraService;
    this.engine = engine;
  }

  public void authorize(RoutingContext rc) {
    String state = UUID.randomUUID().toString();

    String authorizationUri = oauth2.authorizeURL(new JsonObject()
            .put("redirect_uri", "http://localhost:9090/api-user-management/auth-callback")
            .put("scope", "offline openid")
            .put("state", state));

    LOGGER.debug("AUTHORIZE REDIRECT == {0}", authorizationUri);

    rc.response()
            .addCookie(Cookie.cookie("state", state))
            .putHeader(HttpHeaders.LOCATION, authorizationUri)
            .setStatusCode(HttpResponseStatus.FOUND.code()).end();
    rc.next();
  }

  public void getLogin(RoutingContext rc) {
    String challenge = rc.request().getParam(LOGIN_CHALLENGE);

    LoginRequest result = hydraService.getLoginRequest(challenge);
    LOGGER.debug("GET LOGIN == {0}", result);

    if (Objects.equals(result.getSkip(), true)) {
      AcceptLoginRequest acceptLoginRequest = new AcceptLoginRequest();
      acceptLoginRequest.setSubject(result.getSubject());
      CompletedRequest completed = hydraService.acceptLoginRequest(challenge, acceptLoginRequest);
      String redirect = completed.getRedirectTo();

      LOGGER.debug("LOGIN REDIRECT == {0}", completed.getRedirectTo());

      rc.response().putHeader(HttpHeaders.LOCATION, redirect).setStatusCode(HttpResponseStatus.FOUND.code()).end();
      rc.next();
    } else {
      engine.render(new JsonObject().put(LOGIN_CHALLENGE, challenge), "assets/login.hbs", res -> {
        if (res.succeeded()) {
          rc.response().end(res.result());
        } else {
          rc.response().end(res.cause().getMessage());
        }
      });
    }
  }

  public void login(RoutingContext rc) {
    String challenge = rc.request().getParam(LOGIN_CHALLENGE);
    String email = rc.request().getParam("email");
    String password = rc.request().getParam("password");

    userRepository.findUserByEmailAndPassword(email, password, res -> {
      if (res.succeeded() && Objects.nonNull(res.result())) {
        AcceptLoginRequest body = new AcceptLoginRequest();
        body.subject(res.result().getEmail());
        body.setRememberFor(Constants.EXPIRES_TOKEN);
        body.setRemember(rc.request().getParam("remember") != null);

        CompletedRequest completed = hydraService.acceptLoginRequest(challenge, body);

        LOGGER.info("LOGIN REDIRECT == {0}", completed.getRedirectTo());
        rc.response().putHeader(HttpHeaders.LOCATION, completed.getRedirectTo()).setStatusCode(HttpResponseStatus.FOUND.code()).end();
        rc.next();
      } else {
        hydraService.rejectLoginRequest(challenge, new RejectRequest().error("Invalid credentials"));
        throw new UnauthorizedException("Invalid Credentials");
      }
    });
  }

  public void getConsent(RoutingContext rc) {
    String challenge = rc.request().getParam(CONSENT_CHALLENGE);
    ConsentRequest consentRequest = hydraService.getConsentRequest(challenge);
    consentRequest.getRequestedScope();

    JsonObject data = new JsonObject();
    data.put(CONSENT_CHALLENGE, challenge);
    data.put("user", consentRequest.getSubject());
    data.put("client_name", Objects.requireNonNull(consentRequest.getClient()).getClientName());
    data.put("client_id", consentRequest.getClient().getClientId());
    data.put("scopes", consentRequest.getRequestedScope());

    engine.render(data, "assets/consent.hbs", res -> {
      if (res.succeeded()) {
        rc.response().end(res.result());
      } else {
        rc.response().end(res.cause().getMessage());
      }
    });
  }

  public void consent(RoutingContext rc) {
    LOGGER.info("CONSENT ============");
    String challenge = rc.request().getParam(CONSENT_CHALLENGE);
    String remember = rc.request().getParam("remember");
    List<String> grantScope = rc.request().params().getAll("grant_scope");

    AcceptConsentRequest body = new AcceptConsentRequest();
    body.grantScope(grantScope);
    body.remember(remember != null);
    body.rememberFor(Constants.EXPIRES_TOKEN);

    CompletedRequest acceptConsent = hydraService.acceptConsentRequest(challenge, body);

    LOGGER.info("ACCEPT REDIRECT ======= {0}", acceptConsent.getRedirectTo());
    rc.response().putHeader(HttpHeaders.LOCATION, acceptConsent.getRedirectTo()).setStatusCode(HttpResponseStatus.FOUND.code()).end();
    rc.next();
  }

  public void callback(RoutingContext rc) {
    LOGGER.info("CALLBACK == {0}", rc.request().params().toString());

    JsonObject authenticate = new JsonObject()
            .put("code", rc.request().params().get("code"))
            .put("redirect_uri", "http://localhost:9090/api-user-management/auth-callback");

    oauth2.authenticate(authenticate, res -> {
      if (res.failed()) {
        LOGGER.info("OAUTH AUTHENTICATED FAILED {0}", res.cause().getMessage());
        rc.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end("FAILED TO AUTHENTICATED");
      } else {
        // save the token and continue...
        LOGGER.info("OAUTH AUTHENTICATED RESULT {0}", res.result().principal().toString());
//        rc.setUser(res.result());
//        Session session = rc.session();
//        User user = res.result();
//        session.put("user", user);

        OAuth2TokenIntrospection test = hydraService.instrospect(res.result().principal().getString("access_token"), null);
        LOGGER.info("HYDRA INTROSPECT ===== {0}", test.toString());

        rc.response().setStatusCode(HttpResponseStatus.OK.code()).end(res.result().principal().encodePrettily());
      }
    });
  }
}