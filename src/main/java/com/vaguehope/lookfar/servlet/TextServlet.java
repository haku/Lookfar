package com.vaguehope.lookfar.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaguehope.lookfar.model.DataStore;
import com.vaguehope.lookfar.model.UpdateHelper;

public class TextServlet extends HttpServlet {

	public static final String CONTEXT = "/text";

	private static final long serialVersionUID = -1999920374623838318L;
	private static final Logger LOG = LoggerFactory.getLogger(TextServlet.class);

	private final DataStore dataStore;

	public TextServlet (DataStore dataStore) {
		this.dataStore = dataStore;
	}

	@Override
	protected void doGet (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			UpdateHelper.printUpdates(this.dataStore.getAllUpdates(), resp);
		}
		catch (SQLException e) {
			LOG.warn("Failed to read data from store.", e);
			throw new ServletException(e.getMessage());
		}
	}

}
