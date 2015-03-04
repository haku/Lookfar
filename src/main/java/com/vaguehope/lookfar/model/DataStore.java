package com.vaguehope.lookfar.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.vaguehope.lookfar.config.Modes;

/**
 * http://jdbc.postgresql.org/documentation/91/index.html
 * http://www.postgresql.org/docs/9.1/static/sql-createtable.html
 * http://www.postgresql.org/docs/9.1/static/datatype.html
 */
public class DataStore {

	private static final Logger LOG = LoggerFactory.getLogger(DataStore.class);

	private final UpdateFactory updateFactory;
	private final Connection conn;

	public DataStore (final UpdateFactory updateFactory) throws URISyntaxException, ClassNotFoundException, SQLException {
		this.updateFactory = updateFactory;
		this.conn = getConnection();
	}

	private static Connection getConnection () throws URISyntaxException, ClassNotFoundException, SQLException {
		final String dbEnv = System.getenv("DATABASE_URL");
		if (dbEnv == null) throw new IllegalStateException("Env var DATABASE_URL not set.");
		final URI dbUri = new URI(dbEnv);
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbUri.getPort() + dbUri.getPath();
		if (Modes.isPostgresSsl()) {
			dbUrl = dbUrl + "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";
		}
		final String username = dbUri.getUserInfo().split(":")[0];
		final String password = dbUri.getUserInfo().split(":")[1];
		Class.forName("org.postgresql.Driver");
		final Connection conn = DriverManager.getConnection(dbUrl, username, password);
		LOG.info("Postgres DB connect: {}", dbUrl);
		return conn;
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private final Collection<DataUpdateListener> dataUpdateListeners = new CopyOnWriteArraySet<>();
	private final DataUpdateListener dataUpdateNotifier = new DataUpdateListenerNotifier(this.dataUpdateListeners);

	public void addListener(final DataUpdateListener listener) {
		this.dataUpdateListeners.add(listener);
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	public List<Node> getAllNodes () throws SQLException {
		final List<Node> ret = Lists.newArrayList();
		final PreparedStatement st = this.conn.prepareStatement("SELECT node,updated FROM nodes ORDER BY node");
		try {
			final ResultSet rs = st.executeQuery();
			try {
				while (rs.next()) {
					final String node = rs.getString(1);
					final Date updated = timestampToDate(rs.getTimestamp(2));
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

	public String getNodeHashpw (final String nodeName) throws SQLException {
		final PreparedStatement st = this.conn.prepareStatement("SELECT pass FROM nodes WHERE node=?");
		try {
			st.setString(1, nodeName);
			final ResultSet rs = st.executeQuery();
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

	public void upsertNode (final String nodeName, final String hashpw) throws SQLException {
		final PreparedStatement st = this.conn.prepareStatement("UPDATE nodes SET pass=?, updated=now() WHERE node=?");
		try {
			st.setString(1, hashpw);
			st.setString(2, nodeName);
			final int rowsUpdates = st.executeUpdate();
			if (rowsUpdates < 1) { // FIXME race condition.
				insertNode(nodeName, hashpw);
			}
		}
		finally {
			st.close();
		}
	}

	private void insertNode (final String nodeName, final String hashpw) throws SQLException {
		final PreparedStatement st = this.conn.prepareStatement("INSERT INTO nodes (node, updated, pass) VALUES (?, now(), ?)");
		try {
			st.setString(1, nodeName);
			st.setString(2, hashpw);
			final int rowInserted = st.executeUpdate();
			if (rowInserted < 1) {
				throw new SQLException("Failed to insert into nodes table.  " + rowInserted + " rows updated.");
			}
		}
		finally {
			st.close();
		}
	}

	public int deleteNode (final String nodeName) throws SQLException {
		final PreparedStatement st = this.conn.prepareStatement("DELETE FROM nodes WHERE node=?");
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
		final List<Update> ret = Lists.newArrayList();
		final PreparedStatement st = this.conn.prepareStatement("SELECT node,updated,key,value,threshold,expire FROM updates ORDER BY node, key");
		try {
			final ResultSet rs = st.executeQuery();
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

	public List<Update> getUpdates (final String nodeName) throws SQLException {
		final List<Update> ret = Lists.newArrayList();
		final PreparedStatement st = this.conn.prepareStatement("SELECT node,updated,key,value,threshold,expire FROM updates WHERE node=? ORDER BY node, key");
		try {
			st.setString(1, nodeName);
			final ResultSet rs = st.executeQuery();
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

	public Update getUpdate (final String nodeName, final String keyName) throws SQLException {
		final PreparedStatement st = this.conn.prepareStatement("SELECT node,updated,key,value,threshold,expire FROM updates WHERE node=? AND key=?");
		try {
			st.setString(1, nodeName);
			st.setString(2, keyName);
			final ResultSet rs = st.executeQuery();
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

	private Update readUpdate (final ResultSet rs) throws SQLException {
		final String node = rs.getString(1);
		final Date updated = timestampToDate(rs.getTimestamp(2));
		final String key = rs.getString(3);
		final String value = rs.getString(4);
		final String threshold = rs.getString(5);
		final String expire = rs.getString(6);
		return this.updateFactory.makeUpdate(node, updated, key, value, threshold, expire);
	}

	public void update (final String node, final Map<String, String> data) throws SQLException {
		final PreparedStatement stUpdate = this.conn.prepareStatement("UPDATE updates SET value=?, updated=now() WHERE node=? AND key=?");
		try {
			for (final Entry<String, String> datum : data.entrySet()) {
				stUpdate.setString(1, datum.getValue());
				stUpdate.setString(2, node);
				stUpdate.setString(3, datum.getKey());
				final int rowsUpdates = stUpdate.executeUpdate();
				if (rowsUpdates < 1) { // FIXME race condition.
					insertUpdate(node, datum);
				}
			}
		}
		finally {
			stUpdate.close();
		}
		this.dataUpdateNotifier.onUpdate(node, data);
	}

	private void insertUpdate (final String node, final Entry<String, String> datum) throws SQLException {
		final PreparedStatement stInsert = this.conn.prepareStatement("INSERT INTO updates (node, updated, key, value) VALUES (?, now(), ?, ?)");
		try {
			stInsert.setString(1, node);
			stInsert.setString(2, datum.getKey());
			stInsert.setString(3, datum.getValue());
			final int rowInserted = stInsert.executeUpdate();
			if (rowInserted < 1) {
				throw new SQLException("Failed to insert into updates table.  " + rowInserted + " rows updated.");
			}
		}
		finally {
			stInsert.close();
		}
	}

	public int clearUpdateUpdated (final String nodeName, final String keyName) throws SQLException {
		final PreparedStatement st = this.conn.prepareStatement("UPDATE updates SET updated=null WHERE node=? AND key=?");
		try {
			st.setString(1, nodeName);
			st.setString(2, keyName);
			return st.executeUpdate();
		}
		finally {
			st.close();
		}
	}

	public int deleteUpdate (final String nodeName, final String keyName) throws SQLException {
		final PreparedStatement st = this.conn.prepareStatement("DELETE FROM updates WHERE node=? AND key=?");
		try {
			st.setString(1, nodeName);
			st.setString(2, keyName);
			return st.executeUpdate();
		}
		finally {
			st.close();
		}
	}

	public int setThreshold (final String nodeName, final String keyName, final String threshold) throws SQLException {
		final PreparedStatement st = this.conn.prepareStatement("UPDATE updates SET threshold=? WHERE node=? AND key=?");
		try {
			st.setString(1, threshold);
			st.setString(2, nodeName);
			st.setString(3, keyName);
			return st.executeUpdate();
		}
		finally {
			st.close();
		}
	}

	public int setExpire (final String nodeName, final String keyName, final String expire) throws SQLException {
		final PreparedStatement st = this.conn.prepareStatement("UPDATE updates SET expire=? WHERE node=? AND key=?");
		try {
			st.setString(1, expire);
			st.setString(2, nodeName);
			st.setString(3, keyName);
			return st.executeUpdate();
		}
		finally {
			st.close();
		}
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private static Date timestampToDate (final Timestamp t) {
		return t == null ? null : new Date(t.getTime());
	}

}
