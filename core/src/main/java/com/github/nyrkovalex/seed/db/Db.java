package com.github.nyrkovalex.seed.db;

import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;

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
