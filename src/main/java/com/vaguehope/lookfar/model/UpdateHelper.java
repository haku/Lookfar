package com.vaguehope.lookfar.model;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.vaguehope.lookfar.util.AsciiTable;
import com.vaguehope.lookfar.util.DateFormatFactory;

public final class UpdateHelper {

	private static final String NULL = "-";

	private UpdateHelper () {
		throw new AssertionError();
	}

	public static void printUpdates (final Collection<Update> updates, final HttpServletResponse resp) throws IOException {
		Table<Integer, String, String> table = TreeBasedTable.create();
		int i = 0;
		for (Update u : updates) {
			Integer row = Integer.valueOf(i++);
			table.put(row, "node", u.getNode());
			table.put(row, "updated", u.getUpdated() == null ? NULL : DateFormatFactory.format(u.getUpdated()));
			table.put(row, "key", String.valueOf(u.getKey()));
			table.put(row, "value", String.valueOf(u.getValue()));
			table.put(row, "threshold", u.getThreshold() == null ? NULL : u.getThreshold());
			table.put(row, "expire", u.getExpire() == null ? NULL : u.getExpire());
			table.put(row, "flag", u.calculateFlag().toString());
		}
		AsciiTable.printTable(table, new String[] { "node", "updated", "key", "value", "threshold", "expire", "flag" }, resp);
	}

}
