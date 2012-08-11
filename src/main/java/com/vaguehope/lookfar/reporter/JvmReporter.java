package com.vaguehope.lookfar.reporter;

public class JvmReporter implements ReportProvider {

	private static final long BYTES_IN_MB = 1024L * 1024L;

	private final ThreadGroup rootThreadGroup;

	public JvmReporter () {
		ThreadGroup tg = Thread.currentThread().getThreadGroup();
		while (tg.getParent() != null) {
			tg = tg.getParent();
		}
		this.rootThreadGroup = tg;
	}

	@Override
	public void appendReport (StringBuilder r) {
		long heapFreeSize = Runtime.getRuntime().freeMemory() / BYTES_IN_MB;
		long heapSize = Runtime.getRuntime().totalMemory() / BYTES_IN_MB;
		r.append(heapSize - heapFreeSize).append(" mb of ").append(heapSize).append(" mb heap used.");
		r.append(" ").append(this.rootThreadGroup.activeCount()).append(" active threads.");
	}

}
