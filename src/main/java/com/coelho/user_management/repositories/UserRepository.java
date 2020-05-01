package com.coelho.user_management.repositories;

import com.coelho.user_management.helper.FindOptions;
import com.coelho.user_management.models.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserRepository.class);

  private final JDBCClient jdbcClient;

  public UserRepository(JDBCClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public void count(Handler<AsyncResult<JsonObject>> handler) {
    String sql = "SELECT COUNT(*) FROM T_USER";

    jdbcClient.query(sql, jdbcClientHandler -> {
      if (jdbcClientHandler.succeeded()) {
        LOGGER.debug("Success: {0}", jdbcClientHandler.result());
        handler.handle(
          jdbcClientHandler.map(rs -> {
            List<JsonObject> rows = rs.getRows();
            return rows.stream().findFirst().get();
          })
        );
      } else {
        LOGGER.error("Failed execute query: {0}", jdbcClientHandler.cause().getMessage());
        handler.handle(Future.failedFuture(jdbcClientHandler.cause()));
      }
    });
  }

  public void queryList(FindOptions options, Handler<AsyncResult<List<User>>> handler) {
    LOGGER.info("[queryList] options => " + options);

    String sql = "SELECT * FROM T_USER ORDER BY name LIMIT ? OFFSET ?";
    JsonArray obj = new JsonArray()
      .add(options.getLimit())
      .add(options.getSkip());

    jdbcClient.queryWithParams(sql, obj, jdbcClientHandler -> {
      if (jdbcClientHandler.succeeded()) {
        LOGGER.debug("Success: {0}", jdbcClientHandler.result());
        handler.handle(jdbcClientHandler.map(rs -> rs.getRows().stream().map(User::new).collect(Collectors.toList())));
      } else {
        LOGGER.error("Failed execute query: {0}", jdbcClientHandler.cause().getMessage());
        handler.handle(Future.failedFuture(jdbcClientHandler.cause()));
      }
    });
  }

  public void querySingle(String uuid, Handler<AsyncResult<User>> handler) {
    JsonArray options = new JsonArray().add(uuid);
    LOGGER.info("[querySingle] options => " + options);

    String sql = "SELECT * FROM T_USER WHERE uuid = ?";

    jdbcClient.queryWithParams(sql, options, jdbcClientHandler -> {
      if (jdbcClientHandler.succeeded()) {
        LOGGER.debug("Success: {0}", jdbcClientHandler.result());
        handler.handle(
          jdbcClientHandler.map(rs -> {
//            if (rs.getRows().isEmpty()) {
              List<JsonObject> rows = rs.getRows();
              JsonObject row = rows.get(0);
              return new User(row);
//            } else {
//              return new User();
//            }
          })
        );
      } else {
        LOGGER.error("Failed execute query: {0}", jdbcClientHandler.cause().getMessage());
        handler.handle(Future.failedFuture(jdbcClientHandler.cause()));
      }
    });
  }

  public void create(User user, Handler<AsyncResult<Void>> handler) {
    String sql = "INSERT INTO T_USER (uuid, name, email, password) VALUES (?, ?, ?, ?)";
    JsonArray obj = new JsonArray().add(UUID.randomUUID().toString()).add(user.getName()).add(user.getEmail()).add(user.getPassword());

    executeQuery(sql, obj, handler);
  }

  public void updateOne(String uuid, User user, Handler<AsyncResult<Void>> handler) {
    StringBuilder sql = new StringBuilder().append("UPDATE T_USER SET ");
    JsonArray obj = new JsonArray();

    if (user.getName() != null && !user.getName().isBlank()) {
      sql.append(" name = ? ");
      obj.add(user.getName());
    }

    if (user.getEmail() != null && !user.getEmail().isBlank()) {
      sql.append(" email = ?");
      obj.add(user.getEmail());
    }

    if(user.getPassword() != null && !user.getPassword().isBlank()) {
      sql.append(" password = ?");
      obj.add(user.getPassword());
    }

    sql.append(" WHERE uuid = ?");
    obj.add(uuid);

    executeQuery(sql.toString(), obj, handler);
  }

  public void deleteOne(String uuid, Handler<AsyncResult<Void>> handler) {
    LOGGER.info("[delete] => " + uuid);

    String sql = "DELETE FROM T_USER WHERE uuid = ?";

    JsonArray obj = new JsonArray()
      .add(uuid);

    executeQuery(sql, obj, handler);
  }

  private void executeQuery(String sql, JsonArray obj, Handler<AsyncResult<Void>> handler) {
    jdbcClient.updateWithParams(sql, obj, jdbcClientHandler -> {
      if (jdbcClientHandler.succeeded()) {
        LOGGER.debug("Success: {0}", jdbcClientHandler.result());
        handler.handle(Future.succeededFuture());
      } else {
        LOGGER.error("Failed execute query: {0}", jdbcClientHandler.cause().getMessage());
        handler.handle(Future.failedFuture(jdbcClientHandler.cause()));
      }
    });
  }

  public void findUserByEmailAndPassword(String email, String password, Handler<AsyncResult<User>> handler) {
    LOGGER.info("[querySingle] options => email: {0} password: {0}", email, password);
    JsonArray options = new JsonArray().add(email).add(password);
    String sql = "SELECT * FROM T_USER WHERE email = ? AND password = ?";

    jdbcClient.queryWithParams(sql, options, jdbcClientHandler -> {
      if (jdbcClientHandler.succeeded()) {
        LOGGER.debug("Success: {0}", jdbcClientHandler.result());
        handler.handle(
          jdbcClientHandler.map(rs -> {
            List<JsonObject> rows = rs.getRows();
            JsonObject row = rows.get(0);
            return new User(row);
          })
        );
      } else {
        LOGGER.error("Failed execute query: {0}", jdbcClientHandler.cause().getMessage());
        handler.handle(Future.failedFuture(jdbcClientHandler.cause()));
      }
    });
  }
}