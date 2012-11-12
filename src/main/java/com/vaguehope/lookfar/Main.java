package com.vaguehope.lookfar;

import javax.servlet.Filter;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaguehope.lookfar.auth.BasicAuthFilter;
import com.vaguehope.lookfar.auth.HerokoHttpsFilter;
import com.vaguehope.lookfar.auth.NodePasswd;
import com.vaguehope.lookfar.auth.PasswdGen;
import com.vaguehope.lookfar.auth.SharedPasswd;
import com.vaguehope.lookfar.config.Config;
import com.vaguehope.lookfar.config.Modes;
import com.vaguehope.lookfar.expire.ExpireParser;
import com.vaguehope.lookfar.model.DataStore;
import com.vaguehope.lookfar.model.UpdateFactory;
import com.vaguehope.lookfar.reporter.JvmReporter;
import com.vaguehope.lookfar.reporter.Reporter;
import com.vaguehope.lookfar.servlet.EchoServlet;
import com.vaguehope.lookfar.servlet.NodeServlet;
import com.vaguehope.lookfar.servlet.TextServlet;
import com.vaguehope.lookfar.servlet.UpdateGetServlet;
import com.vaguehope.lookfar.servlet.UpdatePostServlet;
import com.vaguehope.lookfar.splunk.Splunk;
import com.vaguehope.lookfar.threshold.ThresholdParser;

public final class Main {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private final Server server;

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private Main () throws Exception { // NOSONAR Exception is throw by Server.start().
		// Reporting.
		Reporter reporter = new Reporter(new JvmReporter());
		reporter.start();

		// Model.
		ThresholdParser thresholdParser = new ThresholdParser();
		ExpireParser expireParser = new ExpireParser();
		UpdateFactory updateFactory = new UpdateFactory(thresholdParser, expireParser);
		DataStore dataStore = new DataStore(updateFactory);
		Splunk splunk = new Splunk();

		// Dependencies.
		PasswdGen passwdGen = new PasswdGen();

		// Servlets.
		ServletContextHandler generalServlets = createGeneralServlets(dataStore, passwdGen);
		ServletContextHandler nodeServlets = createNodeServlets(dataStore, splunk);

		// Static files on classpath.
		ResourceHandler resourceHandler = createStaticFilesHandler();

		// Top level handler.
		HandlerList handler = new HandlerList();
		handler.setHandlers(new Handler[] { resourceHandler, generalServlets, nodeServlets });

		// Listening connector.
		int port = Integer.parseInt(System.getenv("PORT")); // Heroko pattern.
		SelectChannelConnector connector = createHttpConnector(port);

		// Start server.
		this.server = new Server();
		this.server.setHandler(handler);
		this.server.addConnector(connector);
		this.server.start();
		LOG.info("Server ready on port " + port + ".");
	}

	private static ServletContextHandler createGeneralServlets (DataStore dataStore, PasswdGen passwdGen) {
		ServletContextHandler generalServlets = new ServletContextHandler();
		generalServlets.setContextPath("/");
		if (Modes.isSecure()) addFilter(generalServlets, new HerokoHttpsFilter());
		addFilter(generalServlets, new BasicAuthFilter(new SharedPasswd()));
		generalServlets.addServlet(new ServletHolder(new EchoServlet()), EchoServlet.CONTEXT);
		generalServlets.addServlet(new ServletHolder(new TextServlet(dataStore)), TextServlet.CONTEXT);
		generalServlets.addServlet(new ServletHolder(new UpdateGetServlet(dataStore)), UpdateGetServlet.CONTEXT);
		generalServlets.addServlet(new ServletHolder(new NodeServlet(dataStore, passwdGen)), NodeServlet.CONTEXT);
		return generalServlets;
	}

	private static ServletContextHandler createNodeServlets (DataStore dataStore, Splunk splunk) {
		ServletContextHandler nodeServlets = new ServletContextHandler();
		nodeServlets.setContextPath("/");
		if (Modes.isSecure()) addFilter(nodeServlets, new HerokoHttpsFilter());
		addFilter(nodeServlets, new BasicAuthFilter(new NodePasswd(dataStore)));
		nodeServlets.addServlet(new ServletHolder(new UpdatePostServlet(dataStore, splunk)), UpdatePostServlet.CONTEXT);
		return nodeServlets;
	}

	private static ResourceHandler createStaticFilesHandler () {
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(false);
		resourceHandler.setWelcomeFiles(new String[] { "index.html" });
		resourceHandler.setResourceBase(
				Modes.isDebug() ?
						"./src/main/resources/webroot" :
						Main.class.getResource("/webroot").toExternalForm()
				);
		return resourceHandler;
	}

	private static SelectChannelConnector createHttpConnector (int port) {
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setMaxIdleTime(Config.SERVER_MAX_IDLE_TIME_MS);
		connector.setAcceptors(Config.SERVER_ACCEPTORS);
		connector.setStatsOn(false);
		connector.setLowResourcesConnections(Config.SERVER_LOW_RESOURCES_CONNECTIONS);
		connector.setLowResourcesMaxIdleTime(Config.SERVER_LOW_RESOURCES_MAX_IDLE_TIME_MS);
		connector.setPort(port);
		return connector;
	}

	private static void addFilter (ServletContextHandler handler, Filter httpsFilter) {
		FilterHolder holder = new FilterHolder(httpsFilter);
		handler.addFilter(holder, "/*", null);
	}

	private void join () throws InterruptedException {
		this.server.join();
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	public static void main (String[] args) throws Exception { // NOSONAR throw by Server.start().
		new Main().join();
	}

}
