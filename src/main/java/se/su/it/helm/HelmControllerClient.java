package se.su.it.helm;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

public class HelmControllerClient {
	HelmController controller;
		
	public HelmControllerClient(int port)
		throws RemoteException, NotBoundException
	{
	    Registry registry = HelmControllerRegistry.getRegistry(port);
	    controller = (HelmController) registry.lookup("HelmController");
	}
	
	public String checkServer() throws RemoteException {
		return controller.checkServer();
	}
	public String runGarbageCollector() throws RemoteException {
		return controller.runGarbageCollector();
	}
}