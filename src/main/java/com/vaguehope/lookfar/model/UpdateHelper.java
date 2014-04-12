package com.vaguehope.lookfar.model;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Objects;
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
		final Table<Integer, String, String> table = TreeBasedTable.create();
		int i = 0;
		String lastNode = null;
		boolean firstOfNode = false;
		for (final Update u : updates) {
			Integer row = Integer.valueOf(i++);

			if (!Objects.equal(u.getNode(), lastNode)) {
				table.put(row, "node", AsciiTable.ROW_DIVIDER);
				row = Integer.valueOf(i++);
				lastNode = u.getNode();
				firstOfNode = true;
			}

			if (firstOfNode) table.put(row, "node", u.getNode());
			table.put(row, "updated", u.getUpdated() == null ? NULL : DateFormatFactory.format(u.getUpdated()));
			table.put(row, "key", String.valueOf(u.getKey()));
			table.put(row, "value", String.valueOf(u.getValue()));
			table.put(row, "threshold", u.getThreshold() == null ? NULL : u.getThreshold());
			table.put(row, "expire", u.getExpire() == null ? NULL : u.getExpire());
			table.put(row, "flag", u.calculateFlag().toString());

			firstOfNode = false;
		}
		AsciiTable.printTable(table, new String[] { "node", "updated", "key", "value", "threshold", "expire", "flag" }, resp);
	}

}
