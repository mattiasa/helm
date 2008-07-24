package se.su.it.helm;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.List;

import org.apache.commons.configuration.Configuration;

public class ControllerClient {
	private Controller controller;
	private String controllerAddr;
	private int port;	
	
	public ControllerClient(HelmConfiguration config)
		throws RemoteException, NotBoundException
	{
		controllerAddr = config.getControllerAddress();
		port = config.getControllerPort();

	    Registry registry = ControllerRegistry.getRegistry(controllerAddr, port);
	    controller = (Controller) registry.lookup("HelmController");
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
	public List<ControllerStatistic> getStatistics() throws RemoteException {
		return controller.getStatistics();
	}
}