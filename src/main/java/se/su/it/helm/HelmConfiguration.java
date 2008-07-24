package se.su.it.helm;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

public class HelmConfiguration {

	
	private Configuration config;
	
	private static class ConfigurationFactory {
		public static Configuration getConfiguration() throws ConfigurationException {
			String configFile = System.getProperty("helmConfiguration");
			if(configFile != null) {
				return new PropertiesConfiguration(configFile);
			} else {
				return new BaseConfiguration();
			}
			
		}
	}

	
	public HelmConfiguration() throws ConfigurationException {
		this(ConfigurationFactory.getConfiguration());

			
	}
	
	public HelmConfiguration(Configuration cfg) {
		this.config = cfg;
		

		String log4jConfig = config.getString("log4jConfig");
		if (log4jConfig != null) {
			PropertyConfigurator.configure(log4jConfig);
		} else {
			BasicConfigurator.configure();
		}
		
	}
	

	public Configuration getConfiguration() {
		return config;
	}

	public void setConfiguration(Configuration configuration) {
		this.config = configuration;
	}
	

	public int getControllerPort()
	{
		return config.getInt("controllerPort", 4713);
	}
	
	public String getControllerAddress() {
		return config.getString("controllerAddress", "localhost");

	} 
	
	public String getBindAddress() { 
		return config.getString("bindAddress", "localhost");
	}

	public int getServerPort() throws TerminatingHelmException { 

		int serverPort = config.getInt("serverPort");
		if(serverPort < 1 || serverPort > 65535)
			throw new TerminatingHelmException("Incorrect port number specified: " + serverPort, null);
		return serverPort;
	}	

	public long getGcInterval() {
	
		long gcInterval = config.getInt("gcInterval", 60);
		gcInterval *= 1000;
	
		return gcInterval;
	}
	public String getJdbcDriver() {
		return config.getString("jdbcDriver", "com.mysql.jdbc.Driver");
	}
	public String getJdbcUrl() {
		return config.getString("jdbcUrl");
	}
	
	public long getDelay() { 
		long shortDelay = config.getInt("delay", 20);
		shortDelay *= 1000;
		return shortDelay;
	}
	
	public long getRblDelay() {

		long longDelay = config.getInt("rbldelay", 3600);
		longDelay *= 1000;
		return longDelay;
	}
		
	public String getGlMessage() {
		return config.getString("glmessage", "Temporarily blocked for @SECONDS@ seconds.");
	}
	
	public int getGcDays() {
		return config.getInt("gcdays", 5);

	}
	
}
