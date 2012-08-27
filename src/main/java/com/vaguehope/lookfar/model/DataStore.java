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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.vaguehope.lookfar.Modes;

/**
 * http://jdbc.postgresql.org/documentation/91/index.html
 * http://www.postgresql.org/docs/9.1/static/sql-createtable.html
 * http://www.postgresql.org/docs/9.1/static/datatype.html
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

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	public List<Node> getAllNodes () throws SQLException {
		List<Node> ret = Lists.newArrayList();
		PreparedStatement st = this.conn.prepareStatement("SELECT node,updated FROM nodes ORDER BY node");
		try {
			ResultSet rs = st.executeQuery();
			try {
				while (rs.next()) {
					String node = rs.getString(1);
					Date updated = timestampToDate(rs.getTimestamp(2));
					ret.add(new Node(node, updated));
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

	public String getNodeHashpw (String nodeName) throws SQLException {
		PreparedStatement st = this.conn.prepareStatement("SELECT pass FROM nodes WHERE node=?");
		try {
			st.setString(1, nodeName);
			ResultSet rs = st.executeQuery();
			try {
				while (rs.next()) {
					return rs.getString(1);
				}
				return null;
			}
			finally {
				rs.close();
			}
		}
		finally {
			st.close();
		}
	}

	public void upsertNode (String nodeName, String hashpw) throws SQLException {
		PreparedStatement st = this.conn.prepareStatement("UPDATE nodes SET pass=?, updated=now() WHERE node=?");
		try {
			st.setString(1, hashpw);
			st.setString(2, nodeName);
			int rowsUpdates = st.executeUpdate();
			if (rowsUpdates < 1) { // FIXME race condition.
				insertNode(nodeName, hashpw);
			}
		}
		finally {
			st.close();
		}
	}

	private void insertNode (String nodeName, String hashpw) throws SQLException {
		PreparedStatement st = this.conn.prepareStatement("INSERT INTO nodes (node, updated, pass) VALUES (?, now(), ?)");
		try {
			st.setString(1, nodeName);
			st.setString(2, hashpw);
			int rowInserted = st.executeUpdate();
			if (rowInserted < 1) {
				throw new SQLException("Failed to insert into nodes table.  " + rowInserted + " rows updated.");
			}
		}
		finally {
			st.close();
		}
	}

	public int deleteNode (String nodeName) throws SQLException {
		PreparedStatement st = this.conn.prepareStatement("DELETE FROM nodes WHERE node=?");
		try {
			st.setString(1, nodeName);
			return st.executeUpdate();
		}
		finally {
			st.close();
		}
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	public List<Update> getAllUpdates () throws SQLException {
		List<Update> ret = Lists.newArrayList();
		PreparedStatement st = this.conn.prepareStatement("SELECT node,updated,key,value FROM updates ORDER BY node, key");
		try {
			ResultSet rs = st.executeQuery();
			try {
				while (rs.next()) {
					ret.add(readUpdate(rs));
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

	public List<Update> getUpdates (String nodeName) throws SQLException {
		List<Update> ret = Lists.newArrayList();
		PreparedStatement st = this.conn.prepareStatement("SELECT node,updated,key,value FROM updates WHERE node=? ORDER BY node, key");
		try {
			st.setString(1, nodeName);
			ResultSet rs = st.executeQuery();
			try {
				while (rs.next()) {
					ret.add(readUpdate(rs));
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

	public Update getUpdate (String nodeName, String keyName) throws SQLException {
		PreparedStatement st = this.conn.prepareStatement("SELECT node,updated,key,value FROM updates WHERE node=? AND key=?");
		try {
			st.setString(1, nodeName);
			st.setString(2, keyName);
			ResultSet rs = st.executeQuery();
			try {
				while (rs.next()) {
					return readUpdate(rs);
				}
				return null;
			}
			finally {
				rs.close();
			}
		}
		finally {
			st.close();
		}
	}

	private static Update readUpdate (ResultSet rs) throws SQLException {
		String node = rs.getString(1);
		Date updated = timestampToDate(rs.getTimestamp(2));
		String key = rs.getString(3);
		String value = rs.getString(4);
		return new Update(node, updated, key, value);
	}

	public void update (String node, Map<String, String> data) throws SQLException {
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

	public int deleteUpdate (String nodeName, String keyName) throws SQLException {
		PreparedStatement st = this.conn.prepareStatement("DELETE FROM updates WHERE node=? AND key=?");
		try {
			st.setString(1, nodeName);
			st.setString(2, keyName);
			return st.executeUpdate();
		}
		finally {
			st.close();
		}
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private static Date timestampToDate (Timestamp t) {
		return t == null ? null : new Date(t.getTime());
	}

}
