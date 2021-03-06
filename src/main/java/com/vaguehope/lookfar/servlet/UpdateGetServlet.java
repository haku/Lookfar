package com.vaguehope.lookfar.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaguehope.lookfar.model.DataStore;
import com.vaguehope.lookfar.model.Update;

public class UpdateGetServlet extends HttpServlet {

	public static final String CONTEXT = "/updates";

	private static final long serialVersionUID = -3725822523022950831L;
	private static final Logger LOG = LoggerFactory.getLogger(UpdateGetServlet.class);

	private final ObjectMapper mapper = new ObjectMapper();
	private final DataStore dataStore;

	public UpdateGetServlet (final DataStore dataStore) {
		this.dataStore = dataStore;
	}

	@Override
	protected void doGet (final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5000");
		resp.setHeader("Access-Control-Allow-Headers", "*");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Methods", "GET");

		try {
			List<Update> updates = this.dataStore.getAllUpdates();
			this.mapper.writeValue(resp.getWriter(), updates);
		}
		catch (SQLException e) {
			LOG.warn("Failed to read data from store.", e);
			throw new ServletException(e.getMessage());
		}
	}

}
