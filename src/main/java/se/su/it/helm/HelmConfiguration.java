package se.su.it.helm;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

public class HelmConfiguration {

	
	private Configuration configuration;
	
	

	
	public HelmConfiguration() throws ConfigurationException {

		this(new PropertiesConfiguration(System.getProperty("helmConfiguration")));
	}
	
	public HelmConfiguration(Configuration config) {
		this.configuration = config;
		

		String log4jConfig = configuration.getString("log4jConfig");
		if (log4jConfig != null) {
			PropertyConfigurator.configure(log4jConfig);
		} else {
			BasicConfigurator.configure();
		}
		
	}
	

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	

	public int getControllerPort()
	{
		return configuration.getInt("controllerPort", 4713);
	}
	
	public String getControllerAddress() {
		return configuration.getString("controllerAddress", "localhost");

	} 
	
	public String getBindAddress() { 
		return configuration.getString("bindAddress", "localhost");
	}

	public int getServerPort() throws TerminatingHelmException { 

		int serverPort = configuration.getInt("serverPort");
		if(serverPort < 1 || serverPort > 65535)
			throw new TerminatingHelmException("Incorrect port number specified: " + serverPort, null);
		return serverPort;
	}	

	public long getGcInterval() {
	
		long gcInterval = configuration.getInt("gcInterval", 60);
		gcInterval *= 1000;
	
		return gcInterval;
	}
	
	
}
