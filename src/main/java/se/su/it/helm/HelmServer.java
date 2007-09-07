package se.su.it.helm;

import java.io.*;
import java.net.*;
import java.util.*;

public class HelmServer implements Runnable {
	private ServerSocket serverSocket;
	private Thread serverThread;
	private boolean isRunning;
	private String version;
	private Greylist greylist;
	private Logger log;
	private boolean stats=false;
	private boolean stop=false;
	
	protected Long requests = new Long(0);
	
	public HelmServer(int port) throws TerminatingHelmException {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			throw new TerminatingHelmException("Couldn't create server socket on port " + port, e);
		}
		isRunning = true;
		log = new Logger();
		greylist = new Greylist(log);
		serverThread = new Thread(this);
		serverThread.start();
		version = "helm-0.0.1";
		
	}
	public String getVersion() {
		return version;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public void stop() {
		isRunning = true;
	}

	public void run() {
		
		if(stats) {
			StatHandler h = new StatHandler(this, log);
			h.start();
		}
		
		log.log("Started main server acceptor");
		while(isRunning()) {
			
			log.debug("Waiting for connection");
			try {
				Socket socket = serverSocket.accept();
				try {
					ClientHandler cl = new ClientHandler(this, socket, log, greylist);
					log.debug("Got connection, spawning child");
					cl.start();
				} catch (FatalHelmException e) {
					log.error("Got fatal error when spawning child");
				}
			} catch (IOException e) {
				log.error("Failed to call serverSocket.accept()" + e);
			}
				try {
				Thread.sleep(100);
			} catch(InterruptedException ie){
			}
		}
	}
	public static void main(String[] args) {
		try {
			switch(args.length) {
				case 1:
					int port = Integer.parseInt(args[0]);
					if(port < 1 || port > 65536)
						throw new IllegalArgumentException();
					new HelmServer(port);
					break;
				default:
					System.out.println("usage: java -cp helm-<ver>.jar se.su.it.helm.HelmServer <port>");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public long getRequests() {
		long ret;
		synchronized (requests) {
			ret = requests.longValue();
		}
		return ret;
	}
	public void addRequest() {
		synchronized (requests) {
			requests++;
		}
	}
}