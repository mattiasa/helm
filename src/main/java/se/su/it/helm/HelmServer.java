package se.su.it.helm;

import java.io.*;
import java.net.*;
import java.util.*;

public class HelmServer implements Runnable {
	private ServerSocket serverSocket;
	private Thread serverThread;
	private boolean isRunning;
	private String version;
	
	public HelmServer(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		isRunning = true;
		serverThread = new Thread(this);
		serverThread.start();
		version = "helm-0.0.1";
	}
	public String getVersion() {
		return version;
	}
	public void run() {
		while(isRunning) {
			try {
				Socket socket = serverSocket.accept();
				ClientHandler cl = new ClientHandler(this, socket);
				cl.start();
				
				Thread.sleep(100);
			} catch(InterruptedException ie){
				
			} catch(IOException ioe) {
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
}