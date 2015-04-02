package com.github.nyrkovalex.seed;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class Db {

    public static ConnectionBuilder connectTo(String connectionString) {
        return new ConnectionBuilder(connectionString);
    }

    public static interface Runner {
        void run(String sql) throws Db.Err;
        void one(String string, VoidCallback callback) throws Db.Err;
        void query(String sql, VoidCallback callback) throws Db.Err;
    }

    public static interface Connection extends Runner, AutoCloseable {
        void transaction(TransctionCallback callback) throws Db.Err;
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

        static void rethrow(SqlCall call) throws Db.Err {
            try {
                call.call();
            } catch (SQLException ex) {
                throw new Db.Err(ex);
            }
        }

        @FunctionalInterface
        static interface SqlCall {
            void call() throws Db.Err, SQLException;
        }
    }

	@FunctionalInterface
	public static interface VoidCallback {
		void call(ResultSet rs) throws SQLException;
	}

	@FunctionalInterface
	public static interface TransctionCallback {
		boolean call(Runner runner) throws Db.Err;
	}
}


class DbConnection implements Db.Connection {

	private final Connection connection;

	DbConnection(java.sql.Connection connection) {
		Objects.requireNonNull(connection,
				"Connection must not be null, check your connection string");

		this.connection = connection;
	}

	@Override
	public void run(String sql) throws Db.Err {
		Db.Err.rethrow(() -> {
			try (Statement statement = connection.createStatement()) {
				statement.execute(sql);
			}
		});
	}

	@Override
	public void query(String sql, Db.VoidCallback callback) throws Db.Err {
		Db.Err.rethrow(() -> {
			try (
					Statement statement = connection.createStatement();
					ResultSet resultSet = statement.executeQuery(sql)) {
				callback.call(resultSet);
			}
		});
	}

	@Override
	public void transaction(Db.TransctionCallback callback) throws Db.Err {
		Db.Err.rethrow(() -> {
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
		});
	}

	@Override
	public void one(String query, Db.VoidCallback callback) throws Db.Err {
		Db.Err.rethrow(() -> {
			try (Statement statement = connection.createStatement()) {
				ResultSet rs = statement.executeQuery(query);
				if (rs.next()) {
					callback.call(rs);
					return;
				}
				throw new Db.Err("Query returned no results");
			}
		});
	}

	@Override
	public void close() throws Exception {
		connection.close();
	}
}

