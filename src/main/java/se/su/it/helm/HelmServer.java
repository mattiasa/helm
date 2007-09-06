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
	
	protected Long requests = new Long(0);
	
	public HelmServer(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		isRunning = true;
		log = new Logger();
		serverThread = new Thread(this);
		serverThread.start();
		version = "helm-0.0.1";
		greylist = new Greylist(log);
	}
	public String getVersion() {
		return version;
	}
	
	
	public boolean isRunning() {
		return isRunning;
	}
	
	
	public void run() {
		
		StatHandler h = new StatHandler(this, log);
		h.start();
		
		
	
	  log.log("Started main server acceptor");
		while(isRunning()) {
			try {
			  System.out.println("Waiting for connection");
				Socket socket = serverSocket.accept();
				ClientHandler cl = new ClientHandler(this, socket, log, greylist);
				System.out.println("Got connection, spawning child");
				cl.start();
				
				Thread.sleep(100);
			} catch(InterruptedException ie){

			  ie.printStackTrace();
			} catch(IOException ioe) {
			  ioe.printStackTrace();
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