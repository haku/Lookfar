package com.vaguehope.lookfar.util;

import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

public class AsciiTableTest {

	@Test
	public void itPrintsSinglecolumn () throws Exception {
		Table<Integer, String, String> table = TreeBasedTable.create();
		table.put(Integer.valueOf(0), "col0", "val0");
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		AsciiTable.printTable(table, new String[] { "col0" }, pw);
		assertEquals("  | col0\n0 | val0\n", sw.toString());
	}

	@Test
	public void itDoesNotFailWithNoData () throws Exception {
		Table<Integer, String, String> table = TreeBasedTable.create();
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		AsciiTable.printTable(table, new String[] { "col0" }, pw);
		assertEquals("  | col0\n", sw.toString());
	}

}
