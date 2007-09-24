package se.su.it.helm;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

class ssf implements RMIServerSocketFactory {
	ServerSocket s;
	HelmServer server;

	ssf(HelmServer server) {
		this.server = server;
	}
	public ServerSocket createServerSocket(int port) throws IOException
	{
		s = new ServerSocket(port, 4, InetAddress.getByName(server.getBindAddress()));
		return s;
	}
	public int hashCode() {
		return s.hashCode();
	}
	public boolean equals(Object obj) {
			return (getClass() == obj.getClass()) && s.equals(((ssf)obj).s);
	}
}

class csf implements RMIClientSocketFactory {
	Socket s;
	public Socket createSocket(String host, int port) throws IOException {
		s = new Socket(host, port);
		return s;
	}
	public int hashCode() {
		return s.hashCode();
	}
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (s == null) { return false; }
		return (getClass() == obj.getClass()) && s.equals(((csf)obj).s);
	}
}

public class HelmControllerRegistry {
	static Registry createRegistry(HelmServer server, int port) throws RemoteException
	{
		return LocateRegistry.createRegistry(port,
				(RMIClientSocketFactory)new csf(),
				(RMIServerSocketFactory)new ssf(server));
	}
	
	static Registry getRegistry(String controllerAddr, int port) throws RemoteException
	{
		return LocateRegistry.getRegistry(controllerAddr, port,
				(RMIClientSocketFactory)new csf());
	}
}
