package com.coelho.api_manager_users.handlers;

import com.coelho.api_manager_users.constants.Constants;
import com.coelho.api_manager_users.helper.FindOptions;
import com.coelho.api_manager_users.models.User;
import com.coelho.api_manager_users.repositories.UserRepository;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;

public class UserHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserHandler.class);

  private final UserRepository userRepository;

  public UserHandler(JDBCClient jdbcClient) {
    this.userRepository = new UserRepository(jdbcClient);
  }

  public void count(RoutingContext rc) {
    LOGGER.info("Count ======= ");
    userRepository.count(handler -> {
      if (handler.succeeded()) {
        rc.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON_UTF8)
          .setStatusCode(HttpResponseStatus.OK.code())
          .end(Json.encodePrettily(handler.result()));
      } else {
        rc.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
      }
    });
  }

  public void findAll(RoutingContext rc) {
    LOGGER.info("FindAll ======= ");

    userRepository.queryList(options(rc), handler -> {
      if (handler.succeeded()) {
        rc.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON_UTF8)
          .setStatusCode(HttpResponseStatus.OK.code())
          .end(Json.encodePrettily(handler.result()));
      } else {
        rc.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
      }
    });
  }

  public void findById(RoutingContext rc) {
    String uuid = rc.request().getParam("UUID");
    LOGGER.info("FindById ======= " + uuid);

    userRepository.querySingle(uuid, handler -> {
      if (handler.succeeded()) {
        rc.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON_UTF8)
          .setStatusCode(HttpResponseStatus.OK.code())
          .end(Json.encodePrettily(handler.result()));
      } else {
        rc.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
      }
    });
  }

  public void create(RoutingContext rc) {
    LOGGER.info("Create ======= " + rc.getBodyAsJson());

    User user = rc.getBodyAsJson().mapTo(User.class);

    userRepository.create(user, handler -> {
      if (handler.succeeded()) {
        rc.response().setStatusCode(HttpResponseStatus.CREATED.code()).end();
      } else {
        rc.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
      }
    });
  }

  public void update(RoutingContext rc) {
    LOGGER.info("UPDATE ======= " + rc.getBodyAsJson());

    String uuid = rc.request().getParam("UUID");
    User user = rc.getBodyAsJson().mapTo(User.class);

    userRepository.updateOne(uuid, user, handler -> {
      if (handler.succeeded()) {
        rc.response().setStatusCode(HttpResponseStatus.OK.code()).end();
      } else {
        rc.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
      }
    });

  }

  public void delete(RoutingContext rc) {
    String uuid = rc.request().getParam("UUID");

    LOGGER.info("DELETE ======= " + uuid);

    userRepository.deleteOne(uuid, handler -> {
      if (handler.succeeded()) {
        rc.response().setStatusCode(HttpResponseStatus.OK.code()).end();
      } else {
        rc.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
      }
    });
  }


  private FindOptions options(RoutingContext rc) {
    FindOptions findOptions = new FindOptions();

    Optional<String> skipOpt = rc.queryParam("skip").stream().findFirst();
    findOptions.setSkip(skipOpt.map(Integer::valueOf).orElse(0));

    Optional<String> limitOpt = rc.queryParam("limit").stream().findFirst();
    findOptions.setLimit(limitOpt.map(Integer::valueOf).orElse(10));

    return findOptions;
  }
}
