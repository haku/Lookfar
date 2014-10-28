package com.vaguehope.lookfar.cli;

import java.util.Map;
import java.util.Objects;
import java.util.Queue;

import com.google.common.collect.ImmutableMap;
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

	public CmdInvoker (final Cmd<T>[] cmds, final Cmd<T> handleUnknown) {
		this.handleUnknown = handleUnknown;
		final ImmutableMap.Builder<CmdInvoker.Arg0AndCount, Cmd<T>> cmdsBuilder = new ImmutableMap.Builder<>();
		for (final Cmd<T> cmd : cmds) {
			if (cmd.arg0() != null) cmdsBuilder.put(new Arg0AndCount(cmd.arg0(), cmd.argCount()), cmd);
		}
		this.nameToCmd = cmdsBuilder.build();
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

}