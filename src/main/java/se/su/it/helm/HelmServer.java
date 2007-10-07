package se.su.it.helm;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Level;
import java.lang.Runtime;


public class HelmServer implements Runnable {
	private ServerSocket serverSocket;
	private boolean isRunning;
	private String version = "helm-0.0.1";
	private Greylist greylist;
	private Logger log;
	private boolean stats=false;
	private long gcInterval;
	private Thread serverThread;
	private int controllerPort;
	private int serverPort;
	private String bindAddr;
	
	private Configuration config ;
	
	/* statistics */
	private Long requests = new Long(0);
	private Long clients = new Long(0);
	private Long firstInsert = new Long(0);
	private Long admittedMatch = new Long(0);
	private Long admittedAWL = new Long(0);
	private Long firstReject = new Long(0);
	private Long update = new Long(0);


	public HelmServer(Configuration config) throws TerminatingHelmException {
		
		this.config = config;
		
		bindAddr = config.getString("bindAddress", "localhost");
		
		serverPort = config.getInt("serverPort");
		if(serverPort < 1 || serverPort > 65536)
			throw new IllegalArgumentException();
		
		try {
			serverSocket = new ServerSocket(serverPort, 10, 
					InetAddress.getByName(bindAddr));
		} catch (IOException e) {
			throw new TerminatingHelmException("Couldn't create server socket on port " + config, e);
		}
		log = Logger.getLogger(HelmServer.class.getName());
		greylist = new Greylist(this, log);
		
		log.setLevel(Level.WARN);
		String log4jFile = config.getString("logj4file");
		if (log4jFile != null) {
			PropertyConfigurator.configure(log4jFile);
		} else {
			BasicConfigurator.configure();
		}
		
		gcInterval = config.getInt("gcInterval", 60);
		gcInterval *= 1000;
		
		controllerPort = config.getInt("controllerPort", 4713);
		
		serverThread = new Thread(this);
	}
	public String getVersion() {
		return version;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public void stop() {
		isRunning = false;
		synchronized(this) {
			this.notifyAll();
		}
		try {
			greylist.shutdown();
		} catch (TerminatingHelmException e) {
			log.error("shutdown error: " + e.getString());
		}
	}
	
	public void startService() {
		isRunning = true;
		serverThread.start();
	}

	public void run() {
		
		if(stats) {
			new StatHandler(this, log);
		}
		new GarbageCollector(this, gcInterval);
		try {
			new ControllerServer(this, controllerPort);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		
		log.warn("Started main server acceptor");
		while(isRunning()) {
			
			log.debug("Waiting for connection");
			try {
				Socket socket = serverSocket.accept();
				try {
					ClientHandler cl = new ClientHandler(this, socket, log, greylist);
					log.debug("Got connection, spawning child");
					cl.start();
				} catch (FatalHelmException e) {
					log.error("Got fatal error when spawning child" + e);
				}
			} catch (IOException e) {
				log.error("Failed to call serverSocket.accept()" + e);
			}
			synchronized(this) {
				try {
					this.wait(100);
				} catch(InterruptedException ie){
				}
			}
		}
	}
	public static void main(String[] args) {

		try {
			if (args.length != 2) {
				System.out.println("usage: java -cp helm-<ver>.jar se.su.it.helm.HelmServer <configfile> [start|create-database]");
				Runtime.getRuntime().exit(1);
			}

			Configuration cnf = new PropertiesConfiguration(args[0]);
					
			if (args[1].equals("start")) {
				HelmServer s = new HelmServer(cnf);
				s.startService();
			} else if (args[1].equals("create-database")) {
				HelmServer s = new HelmServer(cnf);
				s.createDatabase();
			} else if (args[1].equals("reset-database")) {
				HelmServer s = new HelmServer(cnf);
				s.resetDatabase();
			} else if (args[1].equals("gc")) {
				ControllerClient client = new ControllerClient(cnf, 4713);
				String r = client.runGarbageCollector();
				System.out.println("gc: " + r);
			} else if (args[1].equals("stop")) {
				ControllerClient client = new ControllerClient(cnf, 4713);
				String r = client.stopServer();
				System.out.println("stop: " + r);
			} else if (args[1].equals("statistics")) {
				ControllerClient client = new ControllerClient(cnf, 4713);
				
				List<ControllerStatistic> stats= client.getStatistics();
				
				for (ControllerStatistic o : stats) {
					System.out.println(o.getType() + "/" + o.getName() + ": " + o.getValue());
				}
			} else {
				System.err.println("unknown command: " + args[1]);
				Runtime.getRuntime().exit(1);
			}
		} catch (HelmException e) {
			System.out.println(e.getString());
		} catch(Exception e) {
			System.err.println("main exception: " + e);
			e.printStackTrace();
		}
	}
	public Long getRequests() {
		synchronized (requests) {
			return requests;
		}
	}
	public void addRequest() {
		synchronized (requests) {
			requests++;
		}
	}
	public Long getClients() {
		synchronized (clients) {
			return clients;
		}
	}
	public void addClient() {
		synchronized (clients) {
			clients++;
		}
	}
	public void delClient() {
		synchronized (clients) {
			clients--;
		}
	}

	public Long getFirstInsert() {
		synchronized (firstInsert) {
			return firstInsert;
		}
	}
	public synchronized void addFirstInsert() {
		synchronized (firstInsert) {
			firstInsert++;
		}
	}
	public Long getadmittedMatch() {
		synchronized (admittedMatch) {
			return admittedMatch;
		}
	}
	public synchronized void addadmittedMatch() {
		synchronized (admittedMatch) {
			admittedMatch++;
		}
	}
	public Long getadmittedAWL() {
		synchronized (admittedAWL) {
			return admittedAWL;
		}
	}
	public synchronized void addadmittedAWL() {
		synchronized (admittedAWL) {
			admittedAWL++;
		}
	}
	public Long getfirstReject() {
		synchronized (firstReject) {
			return firstReject;
		}
	}
	public synchronized void addfirstReject() {
		synchronized (firstReject) {
			firstReject++;
		}
	}
	public Long getUpdate() {
		synchronized (update) {
			return update;
		}
	}
	public synchronized void addUpdate() {
		synchronized (update) {
			update++;
		}
	}

	public Configuration getConfig() 
	{
		return config;
	}
	
	public Logger getLogger()
	{
		return log;
	}
	
	public String getBindAddress()
	{
		return bindAddr;
	}
	
	public void createDatabase() throws FatalHelmException, NonFatalHelmException {
		greylist.createDatabase();
	}
	
	public void garbageCollectDatabase() throws FatalHelmException, NonFatalHelmException {
		greylist.garbageCollectDatabase();
	}

	public void resetDatabase() throws FatalHelmException, NonFatalHelmException {
		greylist.resetDatabase();
	}
}