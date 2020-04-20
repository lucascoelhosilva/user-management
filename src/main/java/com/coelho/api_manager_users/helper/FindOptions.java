package com.coelho.api_manager_users.helper;

import io.vertx.core.json.JsonArray;

public class FindOptions {

  private String orderBy;
  private JsonArray fields;
  private int limit;
  private int skip;


  public String getOrderBy() {
    return orderBy;
  }

  public void setOrderBy(String orderBy) {
    this.orderBy = orderBy;
  }

  public JsonArray getFields() {
    return fields;
  }

  public void setFields(JsonArray fields) {
    this.fields = fields;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public int getSkip() {
    return skip;
  }

  public void setSkip(int skip) {
    this.skip = skip;
  }

}
