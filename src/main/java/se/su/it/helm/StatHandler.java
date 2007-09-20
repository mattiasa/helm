package se.su.it.helm;

import org.apache.log4j.Logger;

public class StatHandler extends Thread {

	private HelmServer server;
	private Logger log;
	
	public StatHandler(HelmServer server, Logger log) {
		
		this.server = server;
		this.log = log;
		
	}
	
	
	public void run(){
		
		log.info("Started stats thread.");
		
		long lasttime=System.currentTimeMillis();
		long curtime;
		long curreqs;
		long lastreqs = server.getRequests();
		
		while(server.isRunning()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				continue;
			}
			
			curtime = System.currentTimeMillis();
			curreqs = server.getRequests();
			
			long deltatime = curtime-lasttime;
			long deltareqs = curreqs-lastreqs;

			long freq = deltareqs/deltatime;

			log.debug("Processed " + freq + " requests/second.");
			
			lasttime = curtime;
			lastreqs = curreqs;
		}
	}
}
