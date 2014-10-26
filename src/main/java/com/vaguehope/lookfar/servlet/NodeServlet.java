package com.vaguehope.lookfar.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.vaguehope.lookfar.auth.PasswdGen;
import com.vaguehope.lookfar.model.DataStore;
import com.vaguehope.lookfar.model.Update;
import com.vaguehope.lookfar.model.UpdateHelper;
import com.vaguehope.lookfar.util.AsciiTable;
import com.vaguehope.lookfar.util.ServletHelper;
import com.vaguehope.lookfar.util.StringHelper;

public class NodeServlet extends HttpServlet {

	public static final String CONTEXT = "/node/*";

	private static final long serialVersionUID = -6331817712234978364L;
	private static final Logger LOG = LoggerFactory.getLogger(NodeServlet.class);

	private final DataStore dataStore;
	private final PasswdGen passwdGen;

	public NodeServlet (final DataStore dataStore, final PasswdGen passwdGen) {
		this.dataStore = dataStore;
		this.passwdGen = passwdGen;
	}

	@Override
	protected void doGet (final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		try {
			if (req.getPathInfo() != null && req.getPathInfo().length() > 1) {
				getNodeValue(req, resp);
			}
			else {
				getNodes(resp);
			}
		}
		catch (SQLException e) {
			LOG.warn("Failed to read nodes.", e);
			ServletHelper.error(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to read nodes: " + e.getMessage());
			return;
		}
	}

	private void getNodes (final HttpServletResponse resp) throws IOException, ServletException {
		try {
			AsciiTable.printTable(UpdateHelper.allNodesAsTable(this.dataStore), new String[] { "node", "updated" }, resp);
		}
		catch (SQLException e) {
			LOG.warn("Failed to read data from store.", e);
			throw new ServletException(e.getMessage());
		}
	}

	private void getNodeValue (final HttpServletRequest req, final HttpServletResponse resp) throws IOException, SQLException {
		String nodeName = ServletHelper.extractPathElement(req, 1, resp);
		if (nodeName == null) return;

		String keyName = ServletHelper.extractPathElement(req, 2);
		if (keyName == null) {
			UpdateHelper.printUpdates(this.dataStore.getUpdates(nodeName), resp);
		}
		else {
			String propName = ServletHelper.extractPathElement(req, 3);
			Update update = this.dataStore.getUpdate(nodeName, keyName);
			if (update != null) {
				if (propName == null) {
					resp.getWriter().print(update.getValue());
				}
				else if ("threshold".equals(propName)) {
					resp.getWriter().print(update.getThreshold());
				}
				else if ("expire".equals(propName)) {
					resp.getWriter().print(update.getExpire());
				}
				else {
					ServletHelper.error(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown property: " + propName + "'.");
					return;
				}
			}
			else {
				ServletHelper.error(resp, HttpServletResponse.SC_NOT_FOUND, "Failed to find key with name '" + keyName + "' for node with name '" + nodeName + "'.");
				return;
			}
		}
	}

	@Override
	protected void doPut (final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		String nodeName = ServletHelper.extractPathElement(req, 1, resp);
		if (nodeName == null) return;

		String pw = this.passwdGen.makePasswd();
		String hashpw = BCrypt.hashpw(pw, BCrypt.gensalt());

		try {
			this.dataStore.upsertNode(nodeName, hashpw);
		}
		catch (SQLException e) {
			LOG.warn("Failed to store node.", e);
			ServletHelper.error(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to store node: " + e.getMessage());
			return;
		}

		resp.setStatus(HttpServletResponse.SC_CREATED);
		Table<Integer, String, String> table = TreeBasedTable.create();
		table.put(Integer.valueOf(0), "node", nodeName);
		table.put(Integer.valueOf(0), "pw", pw);
		AsciiTable.printTable(table, new String[] { "node", "pw" }, resp);
	}

	@Override
	protected void doDelete (final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		String nodeName = ServletHelper.extractPathElement(req, 1, resp);
		if (nodeName == null) return;

		String keyName = ServletHelper.extractPathElement(req, 2);
		if (keyName == null) {
			deleteNode(resp, nodeName);
		}
		else {
			String propName = ServletHelper.extractPathElement(req, 3);
			if (propName == null) {
				deleteKey(resp, nodeName, keyName);
			}
			else if ("threshold".equals(propName)) {
				setThreshold(resp, nodeName, keyName, null);
			}
			else if ("expire".equals(propName)) {
				setExpire(resp, nodeName, keyName, null);
			}
			else {
				ServletHelper.error(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown property: " + propName + "'.");
				return;
			}
		}

	}

	private void deleteNode (final HttpServletResponse resp, final String nodeName) throws IOException {
		try {
			if (this.dataStore.deleteNode(nodeName) < 1) {
				ServletHelper.error(resp, HttpServletResponse.SC_NOT_FOUND, "Failed to delete node with name '" + nodeName + "'.");
				return;
			}
		}
		catch (SQLException e) {
			LOG.warn("Failed to delete node.", e);
			ServletHelper.error(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete node: " + e.getMessage());
			return;
		}
	}

	private void deleteKey (final HttpServletResponse resp, final String nodeName, final String keyName) throws IOException {
		try {
			if (this.dataStore.deleteUpdate(nodeName, keyName) < 1) {
				ServletHelper.error(resp, HttpServletResponse.SC_NOT_FOUND, "Failed to delete key '" + keyName + "' for node '" + nodeName + "'.");
				return;
			}
		}
		catch (SQLException e) {
			LOG.warn("Failed to delete key.", e);
			ServletHelper.error(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete key: " + e.getMessage());
			return;
		}
	}

	@Override
	protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		String nodeName = ServletHelper.extractPathElement(req, 1, resp);
		if (nodeName == null) return;

		String keyName = ServletHelper.extractPathElement(req, 2, resp);
		if (keyName == null) return;

		String propName = ServletHelper.extractPathElement(req, 3, resp);
		if (propName == null) return;

		if ("threshold".equals(propName)) {
			String threshold = StringHelper.readerFirstLine(req, 50);
			setThreshold(resp, nodeName, keyName, threshold);
		}
		else if ("expire".equals(propName)) {
			String expire = StringHelper.readerFirstLine(req, 50);
			setExpire(resp, nodeName, keyName, expire);
		}
		else {
			ServletHelper.error(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown property: " + propName + "'.");
			return;
		}
	}

	private void setThreshold (final HttpServletResponse resp, final String nodeName, final String keyName, final String threshold) throws IOException {
		try {
			if (this.dataStore.setThreshold(nodeName, keyName, threshold) < 1) {
				ServletHelper.error(resp, HttpServletResponse.SC_NOT_FOUND, "Failed to write threshold '" + threshold + "' for key '" + keyName + "' for node '" + nodeName + "'.");
				return;
			}
		}
		catch (SQLException e) {
			LOG.warn("Failed to set threshold.", e);
			ServletHelper.error(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to set threshold: " + e.getMessage());
			return;
		}
	}

	private void setExpire (final HttpServletResponse resp, final String nodeName, final String keyName, final String expire) throws IOException {
		try {
			if (this.dataStore.setExpire(nodeName, keyName, expire) < 1) {
				ServletHelper.error(resp, HttpServletResponse.SC_NOT_FOUND, "Failed to write expire '" + expire + "' for key '" + keyName + "' for node '" + nodeName + "'.");
				return;
			}
		}
		catch (SQLException e) {
			LOG.warn("Failed to set expire.", e);
			ServletHelper.error(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to set expire: " + e.getMessage());
			return;
		}
	}

}
