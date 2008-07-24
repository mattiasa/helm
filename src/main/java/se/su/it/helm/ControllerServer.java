package se.su.it.helm;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;

public class ControllerServer extends Thread {

	private HelmMaster server;
	private int controllerPort;
	private ControllerServerImpl impl;

	public ControllerServer(HelmMaster server, int controllerPort) throws RemoteException {
		this.server = server;
		this.controllerPort = controllerPort;
		this.impl = new ControllerServerImpl(server);
		
		Thread t = new Thread(this);
		t.start();
	}
	
	public void run(){
		try {
			Registry registry;
			
			registry = ControllerRegistry.createRegistry(server, controllerPort);
			registry.bind("HelmController", impl);

			server.getLogger().info("HelmControllerServer ready");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
			return;
		}
		while(server.isRunning()) {
			synchronized(server) {
				try {
					server.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

}
