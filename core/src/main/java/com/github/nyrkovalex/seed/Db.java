package com.github.nyrkovalex.seed;

import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

public class Db {

    public static ConnectionBuilder connectTo(String connectionString) {
        return new ConnectionBuilder(connectionString);
    }

    public static interface Runner {
        void run(String sql) throws Db.Err;

        void query(String sql, Consumer<ResultSet> callback) throws Db.Err;
    }

    public static interface Connection extends Runner, AutoCloseable {
        void transaction(Function<Runner, Boolean> callback) throws Db.Err;
    }

    public static class ConnectionBuilder {
        private final String connectionString;

        public ConnectionBuilder(String connectionString) {
            this.connectionString = connectionString;
        }

        public Connection with(Driver driver) throws Db.Err {
            try {
                return new DbConnection(driver.connect(connectionString, null));
            } catch (SQLException e) {
                throw new Db.Err(e);
            }
        }
    }
    
    public static class Err extends Exception {
        private static final long serialVersionUID = 1L;

        private Err(Throwable cause) {
            super(cause);
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
            void call() throws SQLException;
        }
    }
}
