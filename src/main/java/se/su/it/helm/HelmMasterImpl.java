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


public class HelmMasterImpl implements Runnable, HelmMaster {
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
	
	private HelmConfiguration helmConfiguration;
	
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
 
	

	public void init() throws TerminatingHelmException {
		
		config = helmConfiguration.getConfiguration();
		
		
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
		log = Logger.getLogger(HelmMasterImpl.class.getName());
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
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#getVersion()
	 */
	public String getVersion() {
		return version;
	}
	
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#isRunning()
	 */
	public boolean isRunning() {
		return isRunning;
	}
	
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#stop()
	 */
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
	
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#startService()
	 */
	public void startService() {
		isRunning = true;
		serverThread.start();
	}

	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#run()
	 */
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
	
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#getRequests()
	 */
	public Long getRequests() {
		synchronized (requests) {
			return requests;
		}
	}
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#addRequest()
	 */
	public void addRequest() {
		synchronized (requests) {
			requests++;
		}
	}
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#getClients()
	 */
	public Long getClients() {
		synchronized (clients) {
			return clients;
		}
	}
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#addClient()
	 */
	public void addClient() {
		synchronized (clients) {
			clients++;
		}
	}
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#delClient()
	 */
	public void delClient() {
		synchronized (clients) {
			clients--;
		}
	}

	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#getFirstInsert()
	 */
	public Long getFirstInsert() {
		synchronized (firstInsert) {
			return firstInsert;
		}
	}
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#addFirstInsert()
	 */
	public synchronized void addFirstInsert() {
		synchronized (firstInsert) {
			firstInsert++;
		}
	}
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#getadmittedMatch()
	 */
	public Long getadmittedMatch() {
		synchronized (admittedMatch) {
			return admittedMatch;
		}
	}
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#addadmittedMatch()
	 */
	public synchronized void addadmittedMatch() {
		synchronized (admittedMatch) {
			admittedMatch++;
		}
	}
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#getadmittedAWL()
	 */
	public Long getadmittedAWL() {
		synchronized (admittedAWL) {
			return admittedAWL;
		}
	}
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#addadmittedAWL()
	 */
	public synchronized void addadmittedAWL() {
		synchronized (admittedAWL) {
			admittedAWL++;
		}
	}
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#getfirstReject()
	 */
	public Long getfirstReject() {
		synchronized (firstReject) {
			return firstReject;
		}
	}
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#addfirstReject()
	 */
	public synchronized void addfirstReject() {
		synchronized (firstReject) {
			firstReject++;
		}
	}
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#getUpdate()
	 */
	public Long getUpdate() {
		synchronized (update) {
			return update;
		}
	}
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#addUpdate()
	 */
	public synchronized void addUpdate() {
		synchronized (update) {
			update++;
		}
	}
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#getFreq()
	 */
	public Long getFreq() {
		synchronized(freq) {
			return freq;
		}
	}
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#setFreq(java.lang.Long)
	 */
	public void setFreq(Long freq) {
		synchronized(freq) {
			this.freq = freq;
		}
	}

	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#getConfig()
	 */
	public Configuration getConfig() 
	{
		return config;
	}
	
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#getLogger()
	 */
	public Logger getLogger()
	{
		return log;
	}
	
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#getBindAddress()
	 */
	public String getBindAddress()
	{
		return bindAddr;
	}
	
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#createDatabase()
	 */
	public void createDatabase() throws FatalHelmException, NonFatalHelmException {
		greylist.createDatabase();
	}
	
	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#garbageCollectDatabase()
	 */
	public void garbageCollectDatabase() throws FatalHelmException, NonFatalHelmException {
		greylist.garbageCollectDatabase();
	}

	/* (non-Javadoc)
	 * @see se.su.it.helm.HelmMaster#resetDatabase()
	 */
	public void resetDatabase() throws FatalHelmException, NonFatalHelmException {
		greylist.resetDatabase();
	}
	
	public void setHelmConfiguration(HelmConfiguration helmConfiguration) {
		this.helmConfiguration = helmConfiguration;
	}

	
}