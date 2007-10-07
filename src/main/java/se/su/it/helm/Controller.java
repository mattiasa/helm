package se.su.it.helm;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Controller extends Remote {
	String stopServer() throws RemoteException;
	String checkServer() throws RemoteException;
	List<ControllerStatistic> getStatistics() throws RemoteException;
	String runGarbageCollector() throws RemoteException;
}
