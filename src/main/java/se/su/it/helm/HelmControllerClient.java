package se.su.it.helm;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import org.apache.commons.configuration.Configuration;

public class HelmControllerClient {
	private HelmController controller;
	private String controllerAddr;

	
	public HelmControllerClient(Configuration config, int port)
		throws RemoteException, NotBoundException
	{
		controllerAddr = config.getString("controllerAddress", "localhost");

	    Registry registry = HelmControllerRegistry.getRegistry(controllerAddr, port);
	    controller = (HelmController) registry.lookup("HelmController");
	}
	
	public String checkServer() throws RemoteException {
		return controller.checkServer();
	}
	public String runGarbageCollector() throws RemoteException {
		return controller.runGarbageCollector();
	}
	public String stopServer() throws RemoteException {
		return controller.stopServer();
	}
}