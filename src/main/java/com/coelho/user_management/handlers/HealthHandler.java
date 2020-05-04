package com.coelho.user_management.handlers;

import io.vertx.core.Vertx;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.jdbc.JDBCClient;

public class HealthHandler {

  private final Vertx vertx;
  private final JDBCClient jdbcClient;

  public HealthHandler(Vertx vertx, JDBCClient jdbcClient) {
    this.vertx = vertx;
    this.jdbcClient = jdbcClient;
  }

  public HealthCheckHandler health() {
    return HealthCheckHandler.create(vertx)
            .register("api-token-manager", ar -> ar.complete(Status.OK()))
            .register("postgresdb", ar -> jdbcClient.getConnection(connection -> {
              if (connection.failed()) {
                ar.fail(connection.cause());
              } else {
                connection.result().close();
                ar.complete(Status.OK());
              }
            }));
  }
}