package com.vaguehope.lookfar.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
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
		this.socketMgr = new SocketMgr(dataStore);
		this.schEx.scheduleWithFixedDelay(new EmitPing(this.socketMgr), 10, 10, TimeUnit.SECONDS);
		dataStore.addListener(this.socketMgr);
	}

	@Override
	public void configure (final WebSocketServletFactory factory) {
		factory.setCreator(this.socketMgr);
	}

	private static class SocketMgr implements WebSocketCreator, DataUpdateListener {

		private final DataStore dataStore;
		private final Collection<TailSocket> sockets = new CopyOnWriteArraySet<>();

		public SocketMgr (final DataStore dataStore) {
			this.dataStore = dataStore;
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

		@Override
		public void onWebSocketConnect (final Session sess) {
			super.onWebSocketConnect(sess);
			this.mgr.sockets.add(this);
		}

		@Override
		public void onWebSocketClose (final int statusCode, final String reason) {
			this.mgr.sockets.remove(this);
			super.onWebSocketClose(statusCode, reason);
		}

		@Override
		public void onWebSocketText (final String in) {
			try {
				if ("ls".equals(in) || "l".equals(in)) {
					final StringWriter sw = new StringWriter();
					AsciiTable.printTable(UpdateHelper.allNodesAsTable(this.mgr.dataStore), new String[] { "node", "updated" }, new PrintWriter(sw));
					sendString(sw.toString());
				}
				else {
					sendString(String.format("unknown: %s", in));
				}
			}
			catch (final SQLException e) {
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

		private void sendString (final String s) {
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
