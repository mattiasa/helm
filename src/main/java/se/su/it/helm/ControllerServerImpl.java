package se.su.it.helm;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ControllerServerImpl extends UnicastRemoteObject implements Controller {
	private static final long serialVersionUID = 3239921844111508863L;
	private HelmMaster server;

	public ControllerServerImpl(HelmMaster server) throws RemoteException {
		super();
		this.server = server;
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

	public List<ControllerStatistic> getStatistics() throws RemoteException {

		List<ControllerStatistic> stats = new ArrayList<ControllerStatistic>();
		
		stats.add(new ControllerStatistic("clients", server.getClients().toString(), "gauge"));
		stats.add(new ControllerStatistic("version", server.getVersion(), "string"));
		stats.add(new ControllerStatistic("requests", server.getRequests().toString(), "counter"));
		stats.add(new ControllerStatistic("firstInsert", server.getFirstInsert().toString(), "counter"));
		stats.add(new ControllerStatistic("admittedMatch", server.getadmittedMatch().toString(), "counter"));
		stats.add(new ControllerStatistic("admittedAWL", server.getadmittedAWL().toString(), "counter"));
		stats.add(new ControllerStatistic("firstReject", server.getfirstReject().toString(), "counter"));
		stats.add(new ControllerStatistic("update", server.getUpdate().toString(), "counter"));
		stats.add(new ControllerStatistic("request-per-second", server.getFreq().toString(), "gauge"));

		return stats;
	}
}
