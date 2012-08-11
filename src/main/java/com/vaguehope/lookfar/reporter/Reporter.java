package com.vaguehope.lookfar.reporter;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reporter extends TimerTask {

	private static final long DELAY = 10L * 1000L; // 10 seconds.
	private static final long INTERVAL = 5L * 60L * 1000L; // 5 minutes.

	private static final Logger LOG = LoggerFactory.getLogger(Reporter.class);

	private final Timer timer = new Timer("reporter", true);
	private final ReportProvider[] providers;

	public Reporter (ReportProvider... providers) {
		this.providers = providers;
	}

	public void start () {
		this.timer.scheduleAtFixedRate(this, DELAY, INTERVAL);
	}

	public void dispose () {
		this.timer.cancel();
	}

	@Override
	public void run () {
		StringBuilder r = new StringBuilder();
		for (ReportProvider provider : this.providers) {
			if (r.length() > 0) r.append(" ");
			provider.appendReport(r);
		}
		LOG.info(r.toString());
	}

}
