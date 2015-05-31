package com.github.nyrkovalex.seed;

import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class Db {

  public static ConnectionBuilder connectTo(String connectionString) {
    return new ConnectionBuilder(connectionString);
  }

  public interface Runner {

    void run(String sql) throws Db.Err;

    void one(String string, VoidCallback callback) throws Db.Err;

    void query(String sql, VoidCallback callback) throws Db.Err;
  }

  public interface Connection extends Runner, AutoCloseable {

    void transaction(TransactionCallback callback) throws Db.Err;
  }

  public static class ConnectionBuilder {

    private final String connectionString;

    public ConnectionBuilder(String connectionString) {
      this.connectionString = connectionString;
    }

    public Connection with(Driver driver) throws Db.Err {
      try {
        java.sql.Connection connection = driver.connect(connectionString, null);
        return new DbConnection(connection);
      } catch (SQLException e) {
        throw new Db.Err(e);
      }
    }
  }

  public static final class Err extends Exception {

    private static final long serialVersionUID = 1L;

    Err(Throwable cause) {
      super(cause);
    }

    Err(String message) {
      super(message);
    }
  }

  @FunctionalInterface
  public interface VoidCallback {

    void call(ResultSet rs) throws SQLException;
  }

  @FunctionalInterface
  public interface TransactionCallback {

    boolean call(Runner runner) throws Db.Err;
  }

  private static class DbConnection implements Db.Connection {

    private final java.sql.Connection connection;

    DbConnection(java.sql.Connection connection) {
      Objects.requireNonNull(connection, "Connection must not be null, check your connection string");
      this.connection = connection;
    }

    @Override
    public void run(String sql) throws Db.Err {
      Errors.rethrow(() -> {
        try (final Statement statement = connection.createStatement()) {
          statement.execute(sql);
        }
      }, Err::new);
    }

    @Override
    public void query(String sql, Db.VoidCallback callback) throws Db.Err {
      Errors.rethrow(() -> {
        try (final Statement statement = connection.createStatement(); final ResultSet resultSet = statement.executeQuery(sql)) {
          callback.call(resultSet);
        }
      }, Err::new);
    }

    @Override
    public void transaction(Db.TransactionCallback callback) throws Db.Err {
      Errors.rethrow(() -> {
        try {
          connection.setAutoCommit(false);
          if (callback.call(this)) {
            connection.commit();
          } else {
            connection.rollback();
          }
        } catch (SQLException | Db.Err | RuntimeException ex) {
          connection.rollback();
          throw new Db.Err(ex);
        } finally {
          connection.setAutoCommit(true);
        }
      }, Err::new);
    }

    @Override
    public void one(String query, Db.VoidCallback callback) throws Db.Err {
      Errors.rethrow(() -> {
        try (final Statement statement = connection.createStatement()) {
          ResultSet rs = statement.executeQuery(query);
          if (rs.next()) {
            callback.call(rs);
            return;
          }
          throw new Db.Err("Query returned no results");
        }
      }, Err::new);
    }

    @Override
    public void close() throws Exception {
      connection.close();
    }
  }

}
