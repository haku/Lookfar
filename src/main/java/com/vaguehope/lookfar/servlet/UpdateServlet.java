package com.vaguehope.lookfar.servlet;

import static com.vaguehope.lookfar.servlet.ServletHelper.validateStringParam;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.vaguehope.lookfar.DataStore;

public class UpdateServlet extends HttpServlet {

	public static final String CONTEXT = "/update";

	private static final long serialVersionUID = 1157053289236694746L;
	private static final Logger LOG = LoggerFactory.getLogger(UpdateServlet.class);
	private static final String PARAM_NODE = "node";

	private final DataStore dataStore;

	public UpdateServlet (DataStore dataStore) {
		this.dataStore = dataStore;
	}

	@Override
	protected void doPost (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String node = validateStringParam(req, resp, PARAM_NODE);
		if (node == null) return;

		HashMap<String, String> data = Maps.newHashMap();
		for (Entry<String, String[]> datum : ((Map<String, String[]>) req.getParameterMap()).entrySet()) {
			if (PARAM_NODE.equals(datum.getKey())) continue;
			data.put(datum.getKey(), Arrays.toString(datum.getValue()));
		}
		try {
			this.dataStore.update(node, data);
		}
		catch (SQLException e) {
			LOG.warn("Failed to store data.", e);
			ServletHelper.error(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to store data: " + e.getMessage());
		}
	}
}
