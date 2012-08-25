package com.vaguehope.lookfar.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.vaguehope.lookfar.model.DataStore;
import com.vaguehope.lookfar.model.Update;
import com.vaguehope.lookfar.util.AsciiTable;
import com.vaguehope.lookfar.util.DateFormatFactory;

public class UpdateGetServlet extends HttpServlet {

	public static final String CONTEXT = "/update/*";

	private static final long serialVersionUID = -1999920374623838318L;
	private static final Logger LOG = LoggerFactory.getLogger(UpdateGetServlet.class);

	private final DataStore dataStore;

	public UpdateGetServlet (DataStore dataStore) {
		this.dataStore = dataStore;
	}

	@Override
	protected void doGet (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Table<Integer, String, String> table = TreeBasedTable.create();
		try {
			int i = 0;
			for (Update u : this.dataStore.getAllUpdates()) {
				Integer row = Integer.valueOf(i++);
				table.put(row, "node", u.getNode());
				table.put(row, "updated", DateFormatFactory.format(u.getUpdated()));
				table.put(row, "key", u.getKey());
				table.put(row, "value", u.getValue());
				table.put(row, "flag", u.calculateFlag().toString());
			}
			AsciiTable.printTable(table, new String[] { "node", "updated", "key", "value", "flag" }, resp);
		}
		catch (SQLException e) {
			LOG.warn("Failed to read data from store.", e);
			throw new ServletException(e.getMessage());
		}
	}

}
