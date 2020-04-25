package com.coelho.api_manager_users.security.hydra;

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

public class HydraService {

  public LoginRequest getLoginRequest(String challenge) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://hydra:4445");

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      return apiInstance.getLoginRequest(challenge);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#getLoginRequest");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
      return null;
    }
  }

  public CompletedRequest acceptLoginRequest(String challenge, AcceptLoginRequest body) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://hydra:4445");

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      return apiInstance.acceptLoginRequest(challenge, body);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#getLoginRequest");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
      return null;
    }
  }


  public ConsentRequest getConsentRequest(String consentChallenge){
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://hydra:4445");

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      return apiInstance.getConsentRequest(consentChallenge);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#getConsentRequest");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
      return null;
    }
  }

  public CompletedRequest acceptConsentRequest(String consentChallenge, AcceptConsentRequest body){
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://hydra:4445");

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      return apiInstance.acceptConsentRequest(consentChallenge, body);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#acceptConsentRequest");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
      return null;
    }
  }

  public OAuth2TokenIntrospection instrospect(String token, String scope) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://hydra:4445");

    AdminApi apiInstance = new AdminApi(defaultClient);
    try {
      return apiInstance.introspectOAuth2Token(token, scope);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdminApi#introspectOAuth2Token");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      return null;
    }

  }
}
