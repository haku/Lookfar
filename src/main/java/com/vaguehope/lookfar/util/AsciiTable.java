package com.vaguehope.lookfar.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.vaguehope.lookfar.servlet.ServletHelper;

public final class AsciiTable {

	private static final String COLUMN_SEPARATOR = " | ";

	private AsciiTable () {
		throw new AssertionError();
	}

	public static <R, C, V> void printTable (Table<R, C, V> table, C[] columnOrder, HttpServletResponse resp) throws IOException {
		resp.setContentType(ServletHelper.CONTENT_TYPE_HTML);
		PrintWriter w = resp.getWriter();
		w.println("<!DOCTYPE html><html><body><pre>");
		printTable(table, columnOrder, w);
		w.println("</pre></body></html>");
	}

	public static <R, C, V> void printTable (Table<R, C, V> table, C[] columnOrder, PrintWriter w) {
		int rowKeyWidth = findRowKeyWidest(table);
		Map<C, Integer> colWidths = measureColumnWidths(table);
		printHeader(columnOrder, rowKeyWidth, colWidths, w);

		printRows(table, columnOrder, rowKeyWidth, colWidths, w);
	}

	private static <R, C, V> int findRowKeyWidest (Table<R, C, V> table) {
		int rowKeyWidth = 1;
		for (R rowKey : table.rowKeySet()) {
			rowKeyWidth = Math.max(rowKeyWidth, rowKey.toString().length());
		}
		return rowKeyWidth;
	}

	private static <R, C, V> Map<C, Integer> measureColumnWidths (Table<R, C, V> table) {
		final Map<C, Integer> colWidths = Maps.newHashMap();
		for (Entry<C, Map<R, V>> column : table.columnMap().entrySet()) {
			int width = column.getKey().toString().length();
			for (V cell : column.getValue().values()) {
				width = Math.max(width, cell.toString().length());
			}
			colWidths.put(column.getKey(), Integer.valueOf(width));
		}
		return colWidths;
	}

	private static <C> void printHeader (C[] columnOrder, int rowKeyWidth, Map<C, Integer> colWidths, PrintWriter w) {
		w.print(padRight("", rowKeyWidth));
		for (C col : columnOrder) {
			w.print(COLUMN_SEPARATOR);
			w.print(padRight(col, colWidths.get(col).intValue()));
		}
		w.println();
	}

	private static <R, C, V> void printRows (Table<R, C, V> table, C[] columnOrder, int rowKeyWidth, Map<C, Integer> colWidths, PrintWriter w) {
		for (Entry<R, Map<C, V>> entry : table.rowMap().entrySet()) {
			w.print(padRight(entry.getKey(), rowKeyWidth));
			for (C col : columnOrder) {
				w.print(COLUMN_SEPARATOR);
				w.print(padRight(entry.getValue().get(col), colWidths.get(col).intValue()));
			}
			w.println();
		}
	}

	private static String padRight (Object s, int n) {
		return String.format("%1$-" + n + "s", s == null ? "null" : s.toString());
	}

}
