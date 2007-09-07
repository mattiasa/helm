package se.su.it.helm;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;


public class HelmServer implements Runnable {
	private ServerSocket serverSocket;
	private Thread serverThread;
	private boolean isRunning;
	private String version;
	private Greylist greylist;
	private Logger log;
	private boolean stats=false;
	
	private Configuration config ;
	
	protected Long requests = new Long(0);

	public HelmServer(Configuration config) throws TerminatingHelmException {
		
		this.config = config;
		
		int serverport = config.getInt("serverport");
		if(serverport < 1 || serverport > 65536)
			throw new IllegalArgumentException();
		
		try {
			serverSocket = new ServerSocket(serverport);
		} catch (IOException e) {
			throw new TerminatingHelmException("Couldn't create server socket on port " + config, e);
		}
		isRunning = true;
		log = new Logger();
		greylist = new Greylist(config, log);
		serverThread = new Thread(this);
		serverThread.start();
		version = "helm-0.0.1";
		
	}
	public String getVersion() {
		return version;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public void stop() {
		isRunning = true;
	}

	public void run() {
		
		if(stats) {
			StatHandler h = new StatHandler(this, log);
			h.start();
		}
		
		log.log("Started main server acceptor");
		while(isRunning()) {
			
			log.debug("Waiting for connection");
			try {
				Socket socket = serverSocket.accept();
				try {
					ClientHandler cl = new ClientHandler(this, socket, log, greylist);
					log.debug("Got connection, spawning child");
					cl.start();
				} catch (FatalHelmException e) {
					log.error("Got fatal error when spawning child");
				}
			} catch (IOException e) {
				log.error("Failed to call serverSocket.accept()" + e);
			}
				try {
				Thread.sleep(100);
			} catch(InterruptedException ie){
			}
		}
	}
	public static void main(String[] args) {
		try {
			switch(args.length) {
				case 1:
					
					Configuration cnf = new PropertiesConfiguration(args[0]);
					
	
					new HelmServer(cnf);
					break;
				default:
					System.out.println("usage: java -cp helm-<ver>.jar se.su.it.helm.HelmServer <configfile>");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public long getRequests() {
		long ret;
		synchronized (requests) {
			ret = requests.longValue();
		}
		return ret;
	}
	public void addRequest() {
		synchronized (requests) {
			requests++;
		}
	}
	
	public Configuration getConfig() 
	{
		return config;
	}
	
	public void createDatabase() throws FatalHelmException, NonFatalHelmException {
		greylist.createDatabase();
	}
	
}