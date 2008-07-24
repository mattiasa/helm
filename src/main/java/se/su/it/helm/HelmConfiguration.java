package se.su.it.helm;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class HelmConfiguration {

	
	private Configuration configuration;
	
	
	public HelmConfiguration() throws ConfigurationException {

		setConfiguration(new PropertiesConfiguration(System.getProperty("helmConfiguration")));
	}
	
	public HelmConfiguration(Configuration config) {
		this.configuration = config;
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
	
	public String getControllerAddres() {
		return configuration.getString("controllerAddress", "localhost");

	}

	
}
