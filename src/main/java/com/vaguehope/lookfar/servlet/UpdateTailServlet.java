package com.vaguehope.lookfar.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.vaguehope.lookfar.cli.Cmd;
import com.vaguehope.lookfar.cli.CmdInvoker;
import com.vaguehope.lookfar.model.DataStore;
import com.vaguehope.lookfar.model.DataUpdateListener;
import com.vaguehope.lookfar.model.UpdateHelper;
import com.vaguehope.lookfar.util.AsciiTable;

public class UpdateTailServlet extends WebSocketServlet {

	public static final String CONTEXT = "/tailupdates";

	private static final Logger LOG = LoggerFactory.getLogger(UpdateTailServlet.class);
	private static final long serialVersionUID = -2680201340985993654L;

	private final ScheduledExecutorService schEx;
	private final SocketMgr socketMgr;

	public UpdateTailServlet (final DataStore dataStore) {
		this.schEx = Executors.newScheduledThreadPool(1);
		this.socketMgr = new SocketMgr(dataStore, new CmdInvoker<TailSocket>(Cmds.values(), Cmds.UNKNOWN));
		this.schEx.scheduleWithFixedDelay(new EmitPing(this.socketMgr), 10, 10, TimeUnit.SECONDS);
		dataStore.addListener(this.socketMgr);
	}

	@Override
	public void configure (final WebSocketServletFactory factory) {
		factory.setCreator(this.socketMgr);
	}

	private enum Cmds implements Cmd<TailSocket> {
		HELP("help", "This help text.") {
			@Override
			public void invoke (final TailSocket context, final String cmd, final Queue<String> args) throws Exception {
				context.sendString(context.getMgr().getCmdInvoker().helpText());
			}
		},
		LS("ls", "List nodes.") {
			@Override
			public void invoke (final TailSocket context, final String cmd, final Queue<String> args) throws SQLException {
				final StringWriter sw = new StringWriter();
				AsciiTable.printTable(UpdateHelper.allNodesAsTable(context.getMgr().getDataStore()), new String[] { "node", "updated" }, new PrintWriter(sw));
				context.sendString(sw.toString());
			}
		},
		L("l", LS),
		LS_NODE("ls", 1, "List node's updates.", "<node>") {
			@Override
			public void invoke (final TailSocket context, final String cmd, final Queue<String> args) throws Exception {
				final StringWriter sw = new StringWriter();
				AsciiTable.printTable(UpdateHelper.updatesAsTable(context.getMgr().getDataStore().getUpdates(args.poll())),
						new String[] { "node", "updated", "key", "value", "threshold", "expire", "flag" }, new PrintWriter(sw));
				context.sendString(sw.toString());
			}
		},
		L_NODE("l", LS_NODE),
		UNKNOWN {
			@Override
			public void invoke (final TailSocket context, final String cmd, final Queue<String> args) throws Exception {
				context.sendString(String.format("unknown: %s (%s args)", cmd, args.size()));
			}
		};

		private final String arg0;
		private final int argCount;
		private final Cmd<TailSocket> isAliasOf;
		private final List<String> description;

		private Cmds () {
			this(null, (String) null);
		}

		private Cmds (final String arg0, final String description) {
			this(arg0, 0, description);
		}

		private Cmds (final String arg0, final int argCount, final String... description) {
			this.description = description != null && description.length > 0 && description[0] != null
					? ImmutableList.copyOf(description)
					: Collections.<String> emptyList();
			this.arg0 = canonicalArg0(arg0);
			this.argCount = argCount;
			this.isAliasOf = null;
		}

		private Cmds (final String arg0, final Cmd<TailSocket> isAliasOf) {
			this.arg0 = canonicalArg0(arg0);
			this.argCount = isAliasOf.argCount();
			this.isAliasOf = isAliasOf;
			this.description = isAliasOf.description();
		}

		private static String canonicalArg0 (final String arg0) {
			return arg0 != null ? arg0.toLowerCase(Locale.ENGLISH) : null;
		}

		@Override
		public String arg0 () {
			return this.arg0;
		}

		@Override
		public int argCount () {
			return this.argCount;
		}

		@Override
		public List<String> description () {
			return this.description;
		}

		@Override
		public Cmd<TailSocket> isAliasOf () {
			return this.isAliasOf;
		}

		@Override
		public void invoke (final TailSocket context, final String cmd, final Queue<String> args) throws Exception {
			if (this.isAliasOf != null) {
				this.isAliasOf.invoke(context, cmd, args);
			}
			else {
				throw new UnsupportedOperationException("Not Implemented.");
			}
		}
	}

	private static class SocketMgr implements WebSocketCreator, DataUpdateListener {

		private final DataStore dataStore;
		private final CmdInvoker<TailSocket> cmdInvoker;
		private final Collection<TailSocket> sockets = new CopyOnWriteArraySet<>();

		public SocketMgr (final DataStore dataStore, final CmdInvoker<TailSocket> cmdInvoker) {
			this.dataStore = dataStore;
			this.cmdInvoker = cmdInvoker;
		}

		public DataStore getDataStore () {
			return this.dataStore;
		}

		public CmdInvoker<TailSocket> getCmdInvoker () {
			return this.cmdInvoker;
		}

		public Collection<TailSocket> getSockets () {
			return this.sockets;
		}

		@Override
		public Object createWebSocket (final ServletUpgradeRequest req, final ServletUpgradeResponse resp) {
			return new TailSocket(this);
		}

		@Override
		public void onUpdate (final String node, final Map<String, String> data) {
			for (final TailSocket s : this.sockets) {
				s.onUpdate(node, data);
			}
		}

		public void emitPing () {
			for (final TailSocket s : this.sockets) {
				s.emitPing();
			}
		}

	}

	public static class TailSocket extends WebSocketAdapter implements DataUpdateListener {

		private final SocketMgr mgr;

		public TailSocket (final SocketMgr mgr) {
			this.mgr = mgr;
		}

		public SocketMgr getMgr () {
			return this.mgr;
		}

		@Override
		public void onWebSocketConnect (final Session sess) {
			super.onWebSocketConnect(sess);
			this.mgr.getSockets().add(this);
		}

		@Override
		public void onWebSocketClose (final int statusCode, final String reason) {
			this.mgr.getSockets().remove(this);
			super.onWebSocketClose(statusCode, reason);
		}

		@Override
		public void onWebSocketText (final String in) {
			try {
				this.mgr.getCmdInvoker().invoke(this, in);
			}
			catch (final Exception e) {
				onException(e);
			}
		}

		@Override
		public void onUpdate (final String node, final Map<String, String> data) {
			sendString(String.format("%s: %s", node, data));
		}

		public void emitPing () {
			try {
				getRemote().sendPing(null);
			}
			catch (final IOException e) {
				onException(e);
			}
		}

		protected void sendString (final String s) {
			try {
				getRemote().sendString(s);
				getRemote().flush();
			}
			catch (final IOException e) {
				onException(e);
			}
		}

		private void onException (final Exception e) {
			LOG.warn("Session can not continue: ", e.toString());
			getSession().close(StatusCode.SERVER_ERROR, e.getMessage());
		}

	}

	private static class EmitPing implements Runnable {

		private final SocketMgr socketMgr;

		public EmitPing (final SocketMgr socketMgr) {
			this.socketMgr = socketMgr;
		}

		@Override
		public void run () {
			this.socketMgr.emitPing();
		}

	}

}
