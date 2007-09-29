package se.su.it.helm;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class HelmControllerServer extends Thread implements HelmController {

	HelmServer server;
	int controllerPort;

	public HelmControllerServer(HelmServer server, int controllerPort) {
		this.server = server;
		this.controllerPort = controllerPort;
		
		Thread t = new Thread(this);
		t.start();
	}

	public String stopServer() throws RemoteException {
		String s = "ok";
		server.stop();
		return s;
	}

	public String checkServer() throws RemoteException {
		return "Server running";
	}
	public String runGarbageCollector() throws RemoteException {
		String s = "ok";
		try {
			server.garbageCollectDatabase();
		} catch (FatalHelmException e) {
			s = "hard failure while running gc";
			e.printStackTrace();
		} catch (NonFatalHelmException e) {
			s = "soft failure while running gc";
			e.printStackTrace();
		}
		return s;
	}

	public void run(){
		try {
			HelmController stub = 
				(HelmController) UnicastRemoteObject.exportObject(this, 0);

			Registry registry;
			
			registry = HelmControllerRegistry.createRegistry(server, controllerPort);
			registry.bind("HelmController", stub);

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
