package com.vaguehope.lookfar.cli;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.vaguehope.lookfar.util.AsciiTable;
import com.vaguehope.lookfar.util.StringHelper;

public class CmdInvoker<T> {

	private static class Arg0AndCount {
		private final String arg0;
		private final int count;

		public Arg0AndCount (final String arg0, final int count) {
			this.arg0 = arg0;
			this.count = count;
		}

		@Override
		public int hashCode () {
			return Objects.hash(this.arg0, this.count);
		}

		@Override
		public boolean equals (final Object obj) {
			if (obj == null) return false;
			if (obj == this) return true;
			if (!(obj instanceof CmdInvoker.Arg0AndCount)) return false;
			final CmdInvoker.Arg0AndCount that = (CmdInvoker.Arg0AndCount) obj;
			return Objects.equals(this.arg0, that.arg0)
					&& Objects.equals(this.count, that.count);
		}
	}

	private final Map<CmdInvoker.Arg0AndCount, Cmd<T>> nameToCmd;
	private final Cmd<T> handleUnknown;
	private final String helpText;

	public CmdInvoker (final Cmd<T>[] cmds, final Cmd<T> handleUnknown) {
		this.nameToCmd = mapCmds(cmds);
		this.helpText = helpText(cmds);
		this.handleUnknown = handleUnknown;
	}

	public String helpText () {
		return this.helpText;
	}

	public void invoke (final T context, final String rawArg) throws Exception {
		final Queue<String> args = StringHelper.splitTerms(rawArg, 10);
		if (args.size() < 1) return;
		final String arg0 = args.poll();
		final Cmd<T> cmd = this.nameToCmd.get(new Arg0AndCount(arg0, args.size()));
		if (cmd != null) {
			cmd.invoke(context, arg0, args);
		}
		else {
			this.handleUnknown.invoke(context, arg0, args);
		}
	}

	private ImmutableMap<Arg0AndCount, Cmd<T>> mapCmds (final Cmd<T>[] cmds) {
		final ImmutableMap.Builder<CmdInvoker.Arg0AndCount, Cmd<T>> cmdsBuilder = new ImmutableMap.Builder<>();
		for (final Cmd<T> cmd : cmds) {
			if (cmd.arg0() != null) cmdsBuilder.put(new Arg0AndCount(cmd.arg0(), cmd.argCount()), cmd);
		}
		ImmutableMap<Arg0AndCount, Cmd<T>> build = cmdsBuilder.build();
		return build;
	}

	private String helpText (final Cmd<T>[] cmds) {
		final Table<Integer, String, String> table = TreeBasedTable.create();
		int i = 0;
		for (Cmd<T> cmd : cmds) {
			if (cmd.arg0() == null || cmd.isAliasOf() != null) continue;
			final Integer row = Integer.valueOf(i++);
			table.put(row, "cmd", cmd.arg0());
			table.put(row, "args", String.valueOf(cmd.description().subList(1, cmd.description().size())));
			table.put(row, "description", cmd.description().get(0));
		}
		final StringWriter sw = new StringWriter();
		AsciiTable.printTable(table, new String[] { "cmd", "args", "description" }, new PrintWriter(sw));
		return sw.toString();
	}

}
