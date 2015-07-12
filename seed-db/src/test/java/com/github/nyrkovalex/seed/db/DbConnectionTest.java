package com.github.nyrkovalex.seed.db;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DbConnectionTest {

	Db.Connection conn;

	@Before
	public void createDb() throws Exception {
		conn = Db.connectTo("jdbc:h2:mem:seed").with(new org.h2.Driver());
		conn.run("create table users (id int primary key, name varchar)");
		conn.run("insert into users (id, name) values (1, 'Dude')");
		conn.run("insert into users (id, name) values (2, 'Walter')");
		conn.run("insert into users (id, name) values (3, 'Donny')");
	}

	@After
	public void destroyDb() throws Exception {
		conn.close();
	}

	private int userCount() throws SQLException {
		int[] wrap = { 0 };
		conn.one("select count(*) from users", rs -> {
			wrap[0] = rs.getInt(1);
		});
		return wrap[0];
	}

	@Test
	public void testShouldCountUsers() throws Exception {
		assertThat(userCount(), is(3));
	}

	@Test
	public void testShouldRemoveOneUser() throws Exception {
		conn.run("delete from users where id = 3");
		assertThat(userCount(), is(2));
	}

	@Test
	public void testShouldUpdateOneUser() throws Exception {
		conn.run("update users set name = 'Lebowski' where id = 1");
		conn.one("select name from users where id = 1", rs -> {
			assertThat(rs.getString(1), is("Lebowski"));
		});
	}

	@Test
	public void testShouldInsertOneUser() throws Exception {
		conn.run("insert into users(id, name) values (4, 'Jesus')");
		assertThat(userCount(), is(4));
	}

	@Test
	public void testShouldLoopThroughUsers() throws Exception {
		Map<Integer, String> names = new HashMap<>(3);
		names.put(0, "Dude");
		names.put(1, "Walter");
		names.put(2, "Donny");
		conn.query("select * from users order by id", rs -> {
			for (int i = 0; rs.next(); i++) {
				assertThat(rs.getString("name"), is(names.get(i)));
			}
		});
	}

	@Test
	public void testShouldCommitTransactionbyFlag() throws Exception {
		conn.transaction(t -> {
			t.run("insert into users(id, name) values (4, 'Jesus')");
			return true;
		});
		assertThat(userCount(), is(4));
	}

	@Test
	public void testShouldRollbackTransactionByFlag() throws Exception {
		conn.transaction(t -> {
			t.run("insert into users(id, name) values (4, 'Jesus')");
			return false;
		});
		assertThat(userCount(), is(3));
	}

	@Test
	public void testShouldRollbackTransactionByErr() throws Exception {
		try {
			conn.transaction(t -> {
				t.run("insert into users(id, name) values (4, 'Jesus')");
				throw new RuntimeException("Something bad happened");
			});
		} catch (SQLException err) {
			// That's OK
		}
		assertThat(userCount(), is(3));
	}
}
