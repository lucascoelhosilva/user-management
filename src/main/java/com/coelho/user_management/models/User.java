package com.coelho.user_management.models;

import io.vertx.core.json.JsonObject;

import java.io.Serializable;

public class User implements Serializable {

  private static final long serialVersionUID = 1L;

  private String uuid;
  private String name;
  private String email;
  private String password;

  public User() {}

  public User(String uuid, String name, String email, String password) {
    this.uuid = uuid;
    this.name = name;
    this.email = email;
    this.password = password;
  }

  public User(JsonObject json) {
    this(
      json.getString("uuid"),
      json.getString("name"),
      json.getString("email"),
      json.getString("password")
    );
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public String toString() {
    return "User {" +
      "uuid=" + uuid +
      ", name='" + name + '\'' +
      ", email='" + email + '\'' +
      ", password='" + password + '\'' +
      '}';
  }
}
