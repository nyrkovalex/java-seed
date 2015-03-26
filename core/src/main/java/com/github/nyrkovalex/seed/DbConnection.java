package com.github.nyrkovalex.seed;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;
import java.util.function.Function;

class DbConnection implements Db.Connection {

    private final Connection connection;

    DbConnection(java.sql.Connection connection) {
        this.connection = connection;
    }

    @Override
    public void run(String sql) throws Db.Err {
        Db.Err.rethrow(() -> {
            try (Statement statement = connection.createStatement()) {
                statement.executeQuery(sql);
            }
        });
    }

    @Override
    public void query(String sql, Consumer<ResultSet> callback) throws Db.Err {
        Db.Err.rethrow(() -> {
            try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
                callback.accept(resultSet);
            }
        });
    }

    @Override
    public void transaction(Function<Db.Runner, Boolean> callback) throws Db.Err {
        Db.Err.rethrow(() -> {
            try {
                connection.setAutoCommit(false);
                if (callback.apply(this))
                    connection.commit();
                else
                    connection.rollback();
            } finally {
                connection.setAutoCommit(true);
            }
        });
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
