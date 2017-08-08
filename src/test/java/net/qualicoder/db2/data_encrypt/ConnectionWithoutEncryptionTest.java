package net.qualicoder.db2.data_encrypt;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConnectionWithoutEncryptionTest {

	private Connection connection;

	@Before
	public void setUp() throws Exception {
		try {

			// Create an instance of the DB2Driver
			Class.forName("com.ibm.db2.jcc.DB2Driver");

			// Connect to the database
			// Note: I am using the port 50002 in this example! 
			final Properties properties = new Properties();
			properties.put("user", "db2inst1");
			properties.put("password", "db2inst1-pwd");
			properties.put("securityMechanism", "13");
			connection = DriverManager.getConnection("jdbc:db2://localhost:50002/mydb", properties);
			
			// This works as well !!
			//	connection = DriverManager.getConnection("jdbc:db2://localhost:50002/mydb:securityMechanism=13;", "db2inst1", "db2inst1-pwd");
			connection.setAutoCommit(false);

			createOrClearTestSchema();

			connection.commit();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e);
			throw e;
		}
	}

	private void createOrClearTestSchema() throws SQLException {
		// Drop if exists
		try {
			connection.prepareStatement("drop table TEST_SCHEMA.TEST_1").executeUpdate();
		} catch (final SQLException e) {
			// Not interesting
		}

		// Create if not exists
		try {
			connection.prepareStatement("create schema TEST_SCHEMA").executeUpdate();
		} catch (final SQLException e) {
			// Not interesting
		}

		// Create the test table;
		connection.prepareStatement("create table TEST_SCHEMA.TEST_1 (SC varchar(10))").executeUpdate();

	}

	@After
	public void tearDown() throws Exception {
		connection.commit();
		connection.close();
	}

	@Test
	public void test() throws SQLException {
		final PreparedStatement update = connection.prepareStatement("insert into TEST_SCHEMA.TEST_1 (SC) values (?)");

		update.setString(1, "blahblah42");
		update.executeUpdate();

		connection.commit();

		final ResultSet resultSet = connection.prepareStatement("select * from TEST_SCHEMA.TEST_1").executeQuery();

		final List<String> results = new ArrayList<>();
		while (resultSet.next()) {
			results.add(resultSet.getString(1));
		}

		assertEquals(1, results.size());
		assertEquals("blahblah42", results.get(0));
	}
}
