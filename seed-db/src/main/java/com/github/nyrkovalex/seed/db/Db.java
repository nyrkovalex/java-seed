package com.github.nyrkovalex.seed.db;

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
		void run(String sql) throws SQLException;
		void one(String sql, VoidCallback callback) throws SQLException;
		void query(String sql, VoidCallback callback) throws SQLException;
	}

	public interface Connection extends Runner, AutoCloseable {
		void transaction(TransactionCallback callback) throws SQLException;
	}

	public static class ConnectionBuilder {

		private final String connectionString;

		public ConnectionBuilder(String connectionString) {
			this.connectionString = connectionString;
		}

		public Connection with(Driver driver) throws SQLException {
			java.sql.Connection connection = driver.connect(connectionString, null);
			return new DbConnection(connection);
		}
	}

	@FunctionalInterface
	public interface VoidCallback {
		void call(ResultSet rs) throws SQLException;
	}

	@FunctionalInterface
	public interface TransactionCallback {
		boolean call(Runner runner) throws SQLException;
	}

	private static class DbConnection implements Db.Connection {

		private final java.sql.Connection connection;

		DbConnection(java.sql.Connection connection) {
			Objects.requireNonNull(connection,
					"Connection must not be null, check your connection string");
			this.connection = connection;
		}

		@Override
		public void run(String sql) throws SQLException {
			try (final Statement statement = connection.createStatement()) {
				statement.execute(sql);
			}
		}

		@Override
		public void query(String sql, Db.VoidCallback callback) throws SQLException {
			try (final Statement statement = connection.createStatement();
			     final ResultSet resultSet = statement.executeQuery(sql)) {
				callback.call(resultSet);
			}
		}

		@Override
		public void transaction(Db.TransactionCallback callback) throws SQLException {
			try {
				connection.setAutoCommit(false);
				if (callback.call(this)) {
					connection.commit();
				} else {
					connection.rollback();
				}
			} catch (SQLException | RuntimeException ex) {
				connection.rollback();
				throw new SQLException(ex);
			} finally {
				connection.setAutoCommit(true);
			}
		}

		@Override
		public void one(String sql, Db.VoidCallback callback) throws SQLException {
			try (final Statement statement = connection.createStatement()) {
				ResultSet rs = statement.executeQuery(sql);
				if (rs.next()) {
					callback.call(rs);
					return;
				}
				throw new SQLException("Query returned no results");
			}
		}

		@Override
		public void close() throws Exception {
			connection.close();
		}
	}

}
