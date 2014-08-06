package com.vaguehope.lookfar;

import javax.servlet.Filter;

import org.apache.camel.CamelContext;
import org.apache.camel.component.rabbitmq.RabbitMQComponent;
import org.apache.camel.impl.DefaultCamelContext;
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
import com.vaguehope.lookfar.splunk.SplunkProducer;
import com.vaguehope.lookfar.threshold.ThresholdParser;
import com.vaguehope.lookfar.twitter.TwitterPoster;
import com.vaguehope.lookfar.twitter.TwitterProducer;
import com.vaguehope.lookfar.twitter.TwitterRoutes;
import com.vaguehope.lookfar.twitter.TwitterTimeoutProducer;

public final class Main {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private final Server server;

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private Main () throws Exception { // NOSONAR Exception is throw by Server.start().
		// Model.
		ThresholdParser thresholdParser = new ThresholdParser();
		ExpireParser expireParser = new ExpireParser();
		UpdateFactory updateFactory = new UpdateFactory(thresholdParser, expireParser);
		DataStore dataStore = new DataStore(updateFactory);

		// Twitter.
		final TwitterPoster twitterPoster = new TwitterPoster();
		final TwitterRoutes twitterRoutes = new TwitterRoutes(twitterPoster);
		final TwitterProducer twitterProducer = new TwitterProducer(dataStore, updateFactory, twitterRoutes);
		new TwitterTimeoutProducer(dataStore, twitterProducer).spawn();

		// Camel.
		final CamelContext camelCtx = new DefaultCamelContext();
		camelCtx.addComponent("rabbitmq", new RabbitMQComponent());
		camelCtx.addRoutes(twitterRoutes);
		camelCtx.start();

		// Splunk.
		Splunk splunk = new Splunk();
		SplunkProducer splunkProducer = new SplunkProducer(splunk);

		// Reporting.
		Reporter reporter = new Reporter(new JvmReporter(), twitterProducer.getReporter(), twitterPoster.getReporter(), splunk.getSplunkRepoter());
		reporter.start();

		// Dependencies.
		PasswdGen passwdGen = new PasswdGen();

		// Servlets.
		ServletContextHandler generalServlets = createGeneralServlets(dataStore, passwdGen);
		ServletContextHandler nodeServlets = createNodeServlets(dataStore, twitterProducer, splunkProducer);

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

	private static ServletContextHandler createGeneralServlets (final DataStore dataStore, final PasswdGen passwdGen) {
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

	private static ServletContextHandler createNodeServlets (final DataStore dataStore, final TwitterProducer twitterProducer, final SplunkProducer splunkProducer) {
		ServletContextHandler nodeServlets = new ServletContextHandler();
		nodeServlets.setContextPath("/");
		if (Modes.isSecure()) addFilter(nodeServlets, new HerokoHttpsFilter());
		addFilter(nodeServlets, new BasicAuthFilter(new NodePasswd(dataStore)));
		nodeServlets.addServlet(new ServletHolder(new UpdatePostServlet(dataStore, twitterProducer, splunkProducer)), UpdatePostServlet.CONTEXT);
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

	private static SelectChannelConnector createHttpConnector (final int port) {
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setMaxIdleTime(Config.SERVER_MAX_IDLE_TIME_MS);
		connector.setAcceptors(Config.SERVER_ACCEPTORS);
		connector.setStatsOn(false);
		connector.setLowResourcesConnections(Config.SERVER_LOW_RESOURCES_CONNECTIONS);
		connector.setLowResourcesMaxIdleTime(Config.SERVER_LOW_RESOURCES_MAX_IDLE_TIME_MS);
		connector.setPort(port);
		return connector;
	}

	private static void addFilter (final ServletContextHandler handler, final Filter httpsFilter) {
		FilterHolder holder = new FilterHolder(httpsFilter);
		handler.addFilter(holder, "/*", null);
	}

	private void join () throws InterruptedException {
		this.server.join();
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	public static void main (final String[] args) throws Exception { // NOSONAR throw by Server.start().
		new Main().join();
	}

}
