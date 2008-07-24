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
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.Runtime;


public class HelmMaster implements Runnable {
	private ServerSocket serverSocket;
	private boolean isRunning;
	private String version = "helm-0.0.3";
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
	private Long freq = new Long(0);


	public HelmMaster(Configuration config) throws TerminatingHelmException {
		
		this.config = config;
		
		bindAddr = config.getString("bindAddress", "localhost");
		
		serverPort = config.getInt("serverPort");
		if(serverPort < 1 || serverPort > 65535)
			throw new IllegalArgumentException();
		
		try {
			serverSocket = new ServerSocket(serverPort, 10, 
					InetAddress.getByName(bindAddr));
		} catch (IOException e) {
			throw new TerminatingHelmException("Couldn't create server socket on port " + config, e);
		}
		log = Logger.getLogger(HelmMaster.class.getName());
		log.setLevel(Level.WARN);

		greylist = new Greylist(this, log);
		
		String log4jConfig = config.getString("log4jConfig");
		if (log4jConfig != null) {
			PropertyConfigurator.configure(log4jConfig);
		} else {
			BasicConfigurator.configure();
		}
		
		gcInterval = config.getInt("gcInterval", 60);
		gcInterval *= 1000;
		
		controllerPort = getControllerPort(config); 
		
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

	public static int getControllerPort(Configuration cnf)
	{
		return cnf.getInt("controllerPort", 4713);
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
	public Long getFreq() {
		synchronized(freq) {
			return freq;
		}
	}
	public void setFreq(Long freq) {
		synchronized(freq) {
			this.freq = freq;
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