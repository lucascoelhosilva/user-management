package com.coelho.user_management.handlers;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.jdbc.JDBCClient;

public class HealthHandler {

  private final Vertx vertx;
  private final JDBCClient jdbcClient;
  private final JsonObject pingPostgresDB;

  public HealthHandler(Vertx vertx, JDBCClient jdbcClient) {
    this.vertx = vertx;
    this.jdbcClient = jdbcClient;
    this.pingPostgresDB = new JsonObject().put("ping", 1);
  }

  public HealthCheckHandler health() {
    return HealthCheckHandler.create(vertx)
      .register("api-users", ar -> ar.complete(Status.OK()))
      .register("postgresDB", ar -> jdbcClient.getConnection(connection ->
      {
        if (connection.failed()) {
          ar.fail(connection.cause());
        } else {
          ar.complete(Status.OK());
        }
      }));
  }
}
