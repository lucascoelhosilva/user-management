package com.coelho.api_manager_users.security.hydra;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

public class HydraService {

  private WebClient client;


  public void acceptLoginRequest() {
    JsonObject challenge = new JsonObject();
    challenge.put("subject", "");
    challenge.put("remember", true);
    challenge.put("remember_for", HydraConstants.consentTimeInSeconds);

  }

  private void makeRequest(String flow, JsonObject challenge) {
    String url = HydraConstants.hydraUrl + flow;
  }
}
