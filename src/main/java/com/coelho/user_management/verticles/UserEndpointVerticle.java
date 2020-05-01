package com.coelho.user_management.verticles;

import com.coelho.user_management.constants.Constants;
import com.coelho.user_management.exceptions.Exception;
import com.coelho.user_management.handlers.AuthenticationHandler;
import com.coelho.user_management.handlers.HealthHandler;
import com.coelho.user_management.handlers.UserHandler;
import com.coelho.user_management.security.HydraService;
import com.coelho.user_management.security.KetoService;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine;

import java.util.HashSet;
import java.util.Set;

public class UserEndpointVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserEndpointVerticle.class);

  protected HttpServer httpServer;
  protected JDBCClient jdbcClient;
  protected OAuth2Auth oauth2;
  protected HydraService hydraService;

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
      jdbcClient = JDBCClient.createShared(vertx, new JsonObject()
      .put("url", config().getString("DATABASE_URL"))
      .put("driver_class", config().getString("DATABASE_DRIVER_CLASS"))
      .put("user", config().getString("DATABASE_USER"))
      .put("password", config().getString("DATABASE_PASSWORD")), "api_manager_users");

    createTableIfNeeded();

    hydraService = new HydraService(config().getString("HYDRA_ADMIN_URL"));

    oauth2 = OAuth2Auth.create(vertx, new OAuth2ClientOptions()
            .setClientID("auth-code-client")
            .setClientSecret("secret")
            .setSite(config().getString("HYDRA_PUBLIC_URL"))
            .setAuthorizationPath("/oauth2/auth")
            .setTokenPath("http://hydra:4444/oauth2/token")
            .setIntrospectionPath("http://hydra:4445/oauth2/introspect"));
  }

  @Override
  public void start(Future<Void> future) {
    LOGGER.info("Starting User Endpoint Verticle");

    // In order to use a Thymeleaf template we first need to create an engine
    final TemplateEngine engine = HandlebarsTemplateEngine.create(vertx);

    final UserHandler userHandler = new UserHandler(jdbcClient);
    final AuthenticationHandler authHandler = new AuthenticationHandler(jdbcClient, oauth2, hydraService, engine);
    final HealthHandler healthHandler = new HealthHandler(getVertx(), jdbcClient);
    final KetoService ketoService = new KetoService(config().getString("KETO_URL"));

    Router subRouter = Router.router(getVertx());
    enableCorsSupport(subRouter);

    subRouter.route().handler(BodyHandler.create());

    CookieHandler cookieHandler = CookieHandler.create();
    subRouter.route().handler(cookieHandler);
    // Session Handler
    SessionStore store = LocalSessionStore.create(vertx);
    SessionHandler sessionHandler = SessionHandler.create(store);
    subRouter.route().handler(sessionHandler);
    subRouter.route().handler(UserSessionHandler.create(oauth2));

    // handlerError
    subRouter.route().failureHandler(ctx -> {
      Exception exception = (Exception) ctx.failure();
      final JsonObject error = new JsonObject()
              .put("timestamp", System.nanoTime())
              .put("status", exception.statusCode())
              .put("error", HttpResponseStatus.valueOf(exception.statusCode()).reasonPhrase())
              .put("path", ctx.normalisedPath());
      if(exception.getMessage() != null) {
        error.put("message", exception.getMessage());
      }
      ctx.response().setStatusCode(exception.statusCode()).end(error.encode());
    });

    subRouter.post("/keto/policies").handler(ketoService::upsertOryAccessControlPolicy);
    subRouter.get("/keto/policies").handler(ketoService::listAccessControlPolicies);
    subRouter.get("/keto/roles").handler(ketoService::listAccessControlPolicyRoles);
    subRouter.post("/keto/roles").handler(ketoService::upsertAccessControlPolicyRole);

    subRouter.post("/register").handler(userHandler::create);
    subRouter.get("/authorize").handler(authHandler::authorize);
    subRouter.get("/login").handler(authHandler::getLogin);
    subRouter.post("/login").handler(authHandler::login);
    subRouter.get("/consent").handler(authHandler::getConsent);
    subRouter.post("/consent").handler(authHandler::consent);
    subRouter.get("/auth-callback").handler(authHandler::callback);

//    subRouter.route("/users*").handler(requestHelper::validateAccessToKen);
    subRouter.get("/users/count").handler(userHandler::count);
    subRouter.get("/users").handler(userHandler::findAll);
    subRouter.get("/users/:UUID").handler(userHandler::findById);
    subRouter.post("/users").handler(userHandler::create);
    subRouter.delete("/users/:UUID").handler(userHandler::delete);
    subRouter.put("/users/:UUID").handler(userHandler::update);
    subRouter.patch("/users/:UUID").handler(userHandler::update);

    Router mainRouter = Router.router(getVertx());
    mainRouter.mountSubRouter(config().getString("BASE_PATH", Constants.BASE_PATH), subRouter);

    mainRouter.route("/health*").handler(healthHandler.health());

    httpServer = getVertx().createHttpServer()
      .requestHandler(mainRouter)
      .listen(config().getInteger("HTTP_PORT", Constants.HTTP_SERVER_PORT), ar -> {
        if (ar.succeeded()) {
          future.complete();
          LOGGER.info("HTTP Server running at port {0}", String.valueOf(ar.result().actualPort()));
        } else {
          future.fail(ar.cause().getMessage());
        }
      });
  }

  @Override
  public void stop() {
    LOGGER.info("Stoping Endpoint Verticle");
    jdbcClient.close();
    httpServer.close();
  }

  private void createTableIfNeeded() {
    vertx.fileSystem().readFile("tables.sql", ar -> {
      if (ar.failed()) {
        LOGGER.info("failed ", ar.cause().getMessage());
      } else {
        jdbcClient.query(ar.result().toString(), resultSetAsyncResult -> {
          if(resultSetAsyncResult.succeeded()){
            LOGGER.info("success: {0}", resultSetAsyncResult.result());
          } else {
            LOGGER.info("failed: {0}", resultSetAsyncResult.cause().getMessage());
          }
        });
      }
    });
  }

  private void enableCorsSupport(Router router) {
    Set<String> allowHeaders = new HashSet<>();
    allowHeaders.add("x-requested-with");
    allowHeaders.add("Access-Control-Allow-Origin");
    allowHeaders.add("origin");
    allowHeaders.add("Content-Type");
    allowHeaders.add("accept");
    // CORS support
    router.route().handler(CorsHandler.create("*")
            .allowedHeaders(allowHeaders)
            .allowedMethod(HttpMethod.GET)
            .allowedMethod(HttpMethod.POST)
            .allowedMethod(HttpMethod.DELETE)
            .allowedMethod(HttpMethod.PATCH)
            .allowedMethod(HttpMethod.PUT)
    );
  }
}