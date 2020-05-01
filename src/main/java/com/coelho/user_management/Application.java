package com.coelho.user_management;

import com.coelho.user_management.verticles.UserEndpointVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class Application {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx(new VertxOptions());

    ConfigRetriever retriever = ConfigRetriever.create(vertx);
    retriever.getConfig(json -> {
      DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(json.result());
      vertx.deployVerticle(UserEndpointVerticle.class, deploymentOptions);
    });
  }
}
