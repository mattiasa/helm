package se.su.it.helm;

import org.apache.log4j.Logger;

public class GarbageCollector extends Thread {

	private HelmServer server;
	long sleeptime;

	public GarbageCollector(HelmServer server, long sleeptime) {

		this.server = server;
		this.sleeptime = sleeptime;
	}

	public void run() {

		server.getLogger().info("Started garbage collector thread.");

		long t = 600; /* 10 min */

		while (server.isRunning()) {
			try {
				Thread.sleep(t);
			} catch (InterruptedException e) {
				continue;
			}

			server.getLogger().info("Running garbage collection");
			try {
				server.garbageCollectDatabase();
			} catch (HelmException e) {
				server.getLogger().info("Got exception with running gc: " + e);
			}
			server.getLogger().info("Done running garbage collection");

			t = sleeptime;
		}
	}

}
