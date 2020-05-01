package com.coelho.user_management.security;

import com.coelho.user_management.exceptions.CustomException;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import sh.ory.hydra.ApiClient;
import sh.ory.hydra.ApiException;
import sh.ory.hydra.Configuration;
import sh.ory.hydra.api.AdminApi;
import sh.ory.hydra.model.AcceptConsentRequest;
import sh.ory.hydra.model.AcceptLoginRequest;
import sh.ory.hydra.model.CompletedRequest;
import sh.ory.hydra.model.ConsentRequest;
import sh.ory.hydra.model.LoginRequest;
import sh.ory.hydra.model.OAuth2TokenIntrospection;
import sh.ory.hydra.model.RejectRequest;

public class HydraService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HydraService.class);

  private final String hydraAdminUrl;

  public HydraService(String hydraAdminUrl) {
    this.hydraAdminUrl = hydraAdminUrl;
  }

  public LoginRequest getLoginRequest(String challenge) {
    LOGGER.debug("getLoginRequest challenge ==== {0}", challenge);

    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath(hydraAdminUrl);

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      return apiInstance.getLoginRequest(challenge);
    } catch (ApiException e) {
      LOGGER.error("Error getLoginRequest ==== {0}", e);
      throw new CustomException(e.getMessage(), e.getCode());
    }
  }

  public CompletedRequest rejectLoginRequest(String challenge, RejectRequest body) {
    LOGGER.debug("rejectLoginRequest challenge ==== {0} body === {0}", challenge, body);

    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath(hydraAdminUrl);

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      return apiInstance.rejectLoginRequest(challenge, body);
    } catch (ApiException e) {
      LOGGER.error("Error rejectLoginRequest ==== {0}", e);
      throw new CustomException(e.getMessage(), e.getCode());
    }
  }

  public CompletedRequest acceptLoginRequest(String challenge, AcceptLoginRequest body) {
    LOGGER.debug("acceptLoginRequest challenge ==== {0} body === {0}", challenge, body);

    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath(hydraAdminUrl);

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      return apiInstance.acceptLoginRequest(challenge, body);
    } catch (ApiException e) {
      LOGGER.error("Error acceptLoginRequest ==== {0}", e);
      throw new CustomException(e.getMessage(), e.getCode());
    }
  }

  public ConsentRequest getConsentRequest(String challenge){
    LOGGER.debug("getConsentRequest challenge ==== {0}", challenge);

    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath(hydraAdminUrl);

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      return apiInstance.getConsentRequest(challenge);
    } catch (ApiException e) {
      LOGGER.error("Error getConsentRequest ==== {0}", e);
      throw new CustomException(e.getMessage(), e.getCode());
    }
  }

  public CompletedRequest acceptConsentRequest(String challenge, AcceptConsentRequest body){
    LOGGER.debug("acceptConsentRequest challenge ==== {0}  body ===== {0}", challenge, body);

    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath(hydraAdminUrl);

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      return apiInstance.acceptConsentRequest(challenge, body);
    } catch (ApiException e) {
      LOGGER.error("Error acceptConsentRequest ==== {0}", e);
      throw new CustomException(e.getMessage(), e.getCode());
    }
  }

  public OAuth2TokenIntrospection instrospect(String token, String scope) {
    LOGGER.debug("instrospect token ==== {0}  scope ===== {0}", token, scope);

    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath(hydraAdminUrl);

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      return apiInstance.introspectOAuth2Token(token, scope);
    } catch (ApiException e) {
      LOGGER.error("Error instrospect ==== {0}", e);
      throw new CustomException(e.getMessage(), e.getCode());
    }
  }

}