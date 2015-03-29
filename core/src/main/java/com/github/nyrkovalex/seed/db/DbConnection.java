package com.github.nyrkovalex.seed.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class DbConnection implements Db.Connection {

	private final Connection connection;

	DbConnection(java.sql.Connection connection) {
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
