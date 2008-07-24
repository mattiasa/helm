package se.su.it.helm;

public class GarbageCollector extends Thread {

	private HelmMaster server;
	long sleeptime;

	public GarbageCollector(HelmMaster server, long sleeptime) {

		this.server = server;
		this.sleeptime = sleeptime;
		
		Thread t = new Thread(this);
		t.start();
	}

	public void run() {

		server.getLogger().info("Started garbage collector thread.");

		long t = 600 * 1000; /* 10 min */

		synchronized(server) {
			try {
				server.wait(t);
			} catch (InterruptedException e1) {
			}
		}
		
		while (server.isRunning()) {

			server.getLogger().info("Running garbage collection");
			try {
				server.garbageCollectDatabase();
			} catch (HelmException e) {
				server.getLogger().info("Got exception with running gc: " + e);
			}
			server.getLogger().info("Done running garbage collection");


			synchronized(server) {
				try {
					server.wait(sleeptime);
				} catch (InterruptedException e) {
				}
			}
		}
	}

}
