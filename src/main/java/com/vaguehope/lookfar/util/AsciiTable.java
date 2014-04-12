package com.vaguehope.lookfar.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

public final class AsciiTable {

	public static final String ROW_DIVIDER = "*--";

	private static final String COLUMN_SEPARATOR = " | ";
	private static final String COLUMN_SEPARATOR_DIV_ROW = "-|-";

	private AsciiTable () {
		throw new AssertionError();
	}

	public static <R, C, V> void printTable (final Table<R, C, V> table, final C[] columnOrder, final HttpServletResponse resp) throws IOException {
		resp.setContentType(Http.CONTENT_TYPE_HTML);
		final PrintWriter w = resp.getWriter();
		w.println("<!DOCTYPE html><html><body><pre>");
		printTable(table, columnOrder, w);
		w.println("</pre></body></html>");
	}

	public static <R, C, V> void printTable (final Table<R, C, V> table, final C[] columnOrder, final PrintWriter w) {
		final int rowKeyWidth = findRowKeyWidest(table);
		final Map<C, Integer> colWidths = measureColumnWidths(table, columnOrder);
		printHeader(columnOrder, rowKeyWidth, colWidths, w);

		printRows(table, columnOrder, rowKeyWidth, colWidths, w);
	}

	private static <R, C, V> int findRowKeyWidest (final Table<R, C, V> table) {
		int rowKeyWidth = 1;
		for (final R rowKey : table.rowKeySet()) {
			rowKeyWidth = Math.max(rowKeyWidth, rowKey.toString().length());
		}
		return rowKeyWidth;
	}

	private static <R, C, V> Map<C, Integer> measureColumnWidths (final Table<R, C, V> table, final C[] columnNames) {
		final Map<C, Integer> colWidths = Maps.newHashMap();
		if (table.size() > 0) {
			for (final Entry<C, Map<R, V>> column : table.columnMap().entrySet()) {
				int width = column.getKey().toString().length();
				for (final V cell : column.getValue().values()) {
					width = Math.max(width, cell.toString().length());
				}
				colWidths.put(column.getKey(), Integer.valueOf(width));
			}
		}
		else {
			for (final C col : columnNames) {
				colWidths.put(col, Integer.valueOf(col.toString().length()));
			}
		}
		return colWidths;
	}

	private static <C> void printHeader (final C[] columnOrder, final int rowKeyWidth, final Map<C, Integer> colWidths, final PrintWriter w) {
		w.print(padRight("", rowKeyWidth));
		for (final C col : columnOrder) {
			w.print(COLUMN_SEPARATOR);
			w.print(padRight(col, colWidths.get(col).intValue()));
		}
		w.println();
	}

	private static <R, C, V> void printRows (final Table<R, C, V> table, final C[] columnOrder, final int rowKeyWidth, final Map<C, Integer> colWidths, final PrintWriter w) {
		for (final Entry<R, Map<C, V>> entry : table.rowMap().entrySet()) {
			final boolean divRow = Objects.equal(ROW_DIVIDER, entry.getValue().get(columnOrder[0]));

			w.print(padRight(entry.getKey(), rowKeyWidth));
			for (final C col : columnOrder) {
				if (divRow) {
					w.print(COLUMN_SEPARATOR_DIV_ROW);
					printRepeatChar(w, colWidths.get(col).intValue(), '-');
				}
				else {
					w.print(COLUMN_SEPARATOR);
					w.print(padRight(entry.getValue().get(col), colWidths.get(col).intValue()));
				}
			}
			w.println();
		}
	}

	private static void printRepeatChar (final PrintWriter w, final int count, final char c) {
		for (int i = 0; i < count; i ++) {
			w.print(c);
		}
	}

	private static String padRight (final Object s, final int n) {
		return String.format("%1$-" + n + "s", StringHelper.toStringOrDefault(s, ""));
	}

}
