package com.vaguehope.lookfar.model;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.vaguehope.lookfar.util.AsciiTable;
import com.vaguehope.lookfar.util.DateFormatFactory;

public final class UpdateHelper {

	private UpdateHelper () {
		throw new AssertionError();
	}

	public static void printUpdates (Collection<Update> updates, HttpServletResponse resp) throws IOException {
		Table<Integer, String, String> table = TreeBasedTable.create();
		int i = 0;
		for (Update u : updates) {
			Integer row = Integer.valueOf(i++);
			table.put(row, "node", u.getNode());
			table.put(row, "updated", DateFormatFactory.format(u.getUpdated()));
			table.put(row, "key", u.getKey());
			table.put(row, "value", u.getValue());
			table.put(row, "flag", u.calculateFlag().toString());
		}
		AsciiTable.printTable(table, new String[] { "node", "updated", "key", "value", "flag" }, resp);
	}

	public static void printUpdate (Update update, HttpServletResponse resp) throws IOException {
		Table<Integer, String, String> table = TreeBasedTable.create();
		Integer row = Integer.valueOf(0);
		table.put(row, "node", update.getNode());
		table.put(row, "updated", DateFormatFactory.format(update.getUpdated()));
		table.put(row, "key", update.getKey());
		table.put(row, "value", update.getValue());
		table.put(row, "flag", update.calculateFlag().toString());
		AsciiTable.printTable(table, new String[] { "node", "pw" }, resp);
	}

}
