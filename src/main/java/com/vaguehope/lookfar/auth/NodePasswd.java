package com.vaguehope.lookfar.auth;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.mindrot.jbcrypt.BCrypt;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.vaguehope.lookfar.model.DataStore;
import com.vaguehope.lookfar.util.ServletHelper;

public class NodePasswd implements PasswdChecker {

	private final LoadingCache<String, String> paswdCache;
	private final String name;

	public NodePasswd (final DataStore dataStore) {
		this.name = String.format("nodePasswd{%s}", dataStore);
		// FIXME Move cache to DataStore so entries expire when modified.
		this.paswdCache = CacheBuilder.newBuilder()
				.expireAfterWrite(1, TimeUnit.MINUTES)
				.maximumSize(500)
				.build(new PasswdLoader(dataStore));
	}

	@Override
	public String toString () {
		return this.name;
	}

	@Override
	public boolean verifyPasswd (final HttpServletRequest req, final String user, final String pass) throws IOException {
		try {
			final String nodeName = ServletHelper.extractPathElement(req, 1);
			if (nodeName == null || !nodeName.equals(user)) return false;
			final String hashpw = this.paswdCache.get(nodeName);
			if (hashpw == null) return false;
			return BCrypt.checkpw(pass, hashpw);
		}
		catch (final ExecutionException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	private static class PasswdLoader extends CacheLoader<String, String> {

		private final DataStore dataStore;

		public PasswdLoader (final DataStore dataStore) {
			this.dataStore = dataStore;
		}

		@Override
		public String load (final String nodeName) throws SQLException {
			return this.dataStore.getNodeHashpw(nodeName);
		}

	}

}
