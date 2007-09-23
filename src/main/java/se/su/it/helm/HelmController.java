package se.su.it.helm;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HelmController extends Remote {
	String stopServer() throws RemoteException;
	String checkServer() throws RemoteException;
	String runGarbageCollector() throws RemoteException;
}
