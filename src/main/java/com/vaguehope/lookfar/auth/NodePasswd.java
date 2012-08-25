package com.vaguehope.lookfar.auth;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.mindrot.jbcrypt.BCrypt;

import com.vaguehope.lookfar.model.DataStore;
import com.vaguehope.lookfar.servlet.ServletHelper;

public class NodePasswd implements PasswdChecker {

	private final DataStore dataStore;

	public NodePasswd (DataStore dataStore) {
		this.dataStore = dataStore;
	}

	@Override
	public boolean verifyPasswd (HttpServletRequest req, String user, String pass) throws IOException {
		try {
			String nodeName = ServletHelper.extractPathElement(req);
			if (nodeName == null || !nodeName.equals(user)) return false;
			String hashpw = this.dataStore.getNodeHashpw(nodeName);
			if (hashpw == null) return false;
			return BCrypt.checkpw(pass, hashpw);
		}
		catch (SQLException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

}
