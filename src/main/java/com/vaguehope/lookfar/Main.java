package com.vaguehope.lookfar;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaguehope.lookfar.reporter.JvmReporter;
import com.vaguehope.lookfar.reporter.Reporter;

public class Main {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private final Server server;

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	public Main () throws Exception { // NOSONAR Exception is throw by Server.start().
		// Reporting.
		Reporter reporter = new Reporter(new JvmReporter());
		reporter.start();

		// Servlet container.
		ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletHandler.setContextPath("/");

		// Static files on classpath.
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(false);
		resourceHandler.setWelcomeFiles(new String[] { "index.html" });
		resourceHandler.setResourceBase(
				Boolean.parseBoolean(System.getenv("DEBUG")) ?
						"./src/main/resources/webroot" :
						Main.class.getResource("/webroot").toExternalForm()
				);

		// Prepare final handler.
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resourceHandler, servletHandler });

		// Listening connector.
		String portString = System.getenv("PORT"); // Heroko pattern.
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setMaxIdleTime(Config.SERVER_MAX_IDLE_TIME_MS);
		connector.setAcceptors(Config.SERVER_ACCEPTORS);
		connector.setStatsOn(false);
		connector.setLowResourcesConnections(Config.SERVER_LOW_RESOURCES_CONNECTIONS);
		connector.setLowResourcesMaxIdleTime(Config.SERVER_LOW_RESOURCES_MAX_IDLE_TIME_MS);
		connector.setPort(Integer.parseInt(portString));

		// Start server.
		this.server = new Server();
		this.server.setHandler(handlers);
		this.server.addConnector(connector);
		this.server.start();
		LOG.info("Server ready on port " + portString + ".");
	}

	public void join () throws InterruptedException {
		this.server.join();
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	public static void main (String[] args) throws Exception { // NOSONAR throw by Server.start().
		new Main().join();
	}

}
