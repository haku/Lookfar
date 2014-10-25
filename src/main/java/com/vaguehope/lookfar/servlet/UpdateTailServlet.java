package com.vaguehope.lookfar.servlet;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

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

public class UpdateTailServlet extends WebSocketServlet {

	public static final String CONTEXT = "/tailupdates";

	private static final Logger LOG = LoggerFactory.getLogger(UpdateTailServlet.class);
	private static final long serialVersionUID = -2680201340985993654L;

	private final SocketMgr socketMgr = new SocketMgr();

	public UpdateTailServlet (final DataStore dataStore) {
		dataStore.addListener(this.socketMgr);
	}

	@Override
	public void configure (final WebSocketServletFactory factory) {
		factory.setCreator(this.socketMgr);
	}

	private static class SocketMgr implements WebSocketCreator, DataUpdateListener {

		private final Collection<TailSocket> sockets = new CopyOnWriteArraySet<>();

		public SocketMgr () {}

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
		public void onUpdate (final String node, final Map<String, String> data) {
			try {
				getRemote().sendString(String.format("%s: %s", node, data));
			}
			catch (final IOException e) {
				LOG.warn("Session can not continue: ", e.toString());
				getSession().close(StatusCode.SERVER_ERROR, e.getMessage());
			}
		}

	}

}
