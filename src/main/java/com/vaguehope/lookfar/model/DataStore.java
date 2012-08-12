package com.vaguehope.lookfar.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.vaguehope.lookfar.Modes;

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

	public List<Update> readAllUpdates () throws SQLException {
		List<Update> ret = Lists.newArrayList();
		PreparedStatement st = this.conn.prepareStatement("SELECT node,updated,key,value FROM updates ORDER BY node, key");
		try {
			ResultSet rs = st.executeQuery();
			try {
				while (rs.next()) {
					String node = rs.getString(1);
					Date updated = timestampToDate(rs.getTimestamp(2));
					String key = rs.getString(3);
					String value = rs.getString(4);
					ret.add(new Update(node, updated, key, value));
				}
				return ret;
			}
			finally {
				rs.close();
			}
		}
		finally {
			st.close();
		}
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

	private static Date timestampToDate (Timestamp t) {
		return t == null ? null : new Date(t.getTime());
	}

}