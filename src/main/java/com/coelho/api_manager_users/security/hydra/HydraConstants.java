package com.coelho.api_manager_users.security.hydra;

public class HydraConstants {

  private HydraConstants() {}

  public static final String hydraUrl = "http://hydra/oauth2/auth/requests/";

  public static final String consentPath = "/oauth2/auth/requests/consent";
  public static final String consentAcceptancePath = consentPath + "/accept";
  public static final String consentRejectionPath  = consentPath + "/reject";
  public static final int consentTimeInSeconds  = 3600;

  public static final String loginPath           = "/oauth2/auth/requests/login";
  public static final String loginAcceptancePath = loginPath + "/accept";
  public static final String loginRejectionPath  = loginPath + "/reject";

  public static final String introspectPath = "/oauth2/introspect";
}
