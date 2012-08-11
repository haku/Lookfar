package com.vaguehope.lookfar;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http://jdbc.postgresql.org/documentation/91/index.html
 */
public class DataStore {

	private static final Logger LOG = LoggerFactory.getLogger(DataStore.class);

	private final Connection conn;

	public DataStore () throws URISyntaxException, ClassNotFoundException, SQLException {
		this.conn = getConnection();
	}

	private static Connection getConnection () throws URISyntaxException, ClassNotFoundException, SQLException {
		String dbEnv = System.getenv("DATABASE_URL");
		if (dbEnv == null) throw new IllegalStateException("Env var DATABASE_URL not set.");
		URI dbUri = new URI(dbEnv);
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbUri.getPort() + dbUri.getPath();
		if (Modes.isPostgresSsl()) {
			dbUrl = dbUrl + "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";
		}
		String username = dbUri.getUserInfo().split(":")[0];
		String password = dbUri.getUserInfo().split(":")[1];
		Class.forName("org.postgresql.Driver");
		Connection conn = DriverManager.getConnection(dbUrl, username, password);
		LOG.info("Postgres DB connect: {}", dbUrl);
		return conn;
	}

	public void update (String node, HashMap<String, String> data) throws SQLException {
		PreparedStatement stUpdate = this.conn.prepareStatement("UPDATE updates SET value=?, updated=now() WHERE node=? AND key=?");
		try {
			for (Entry<String, String> datum : data.entrySet()) {
				stUpdate.setString(1, datum.getValue());
				stUpdate.setString(2, node);
				stUpdate.setString(3, datum.getKey());
				int rowsUpdates = stUpdate.executeUpdate();
				if (rowsUpdates < 1) { // FIXME race condition.
					insertUpdate(node, datum);
				}
			}
		}
		finally {
			stUpdate.close();
		}
	}

	private void insertUpdate (String node, Entry<String, String> datum) throws SQLException {
		PreparedStatement stInsert = this.conn.prepareStatement("INSERT INTO updates (node, updated, key, value) VALUES (?, now(), ?, ?)");
		try {
			stInsert.setString(1, node);
			stInsert.setString(2, datum.getKey());
			stInsert.setString(3, datum.getValue());
			int rowInserted = stInsert.executeUpdate();
			if (rowInserted < 1) {
				throw new SQLException("Failed to insert into updates table.  " + rowInserted + " rows updated.");
			}
		}
		finally {
			stInsert.close();
		}
	}

}
