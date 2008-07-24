package se.su.it.helm;

import org.apache.log4j.Logger;

public class StatHandler extends Thread {

	private HelmMaster server;
	private Logger log;
	
	public StatHandler(HelmMaster server, Logger log) {
		
		this.server = server;
		this.log = log;
		
		Thread t = new Thread(this);
		t.start();
	}
	
	public void run(){
		
		log.info("Started stats thread.");
		
		long lasttime=System.currentTimeMillis();
		long curtime;
		long curreqs;
		long lastreqs = server.getRequests();
		
		while(server.isRunning()) {
			
			curtime = System.currentTimeMillis();
			curreqs = server.getRequests();
			
			long deltatime = curtime-lasttime;
			long deltareqs = curreqs-lastreqs;
			
			if (deltatime == 0)
				deltatime = 1;

			long freq = deltareqs/deltatime;

			log.debug("Processed " + freq + " requests/second.");

			server.setFreq(freq);
			
			lasttime = curtime;
			lastreqs = curreqs;

			synchronized(server) {
				try {
					server.wait(1000);
				} catch (InterruptedException e) {
					continue;
				}
			}
		}
	}
}
