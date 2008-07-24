package se.su.it.helm;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

public interface HelmMaster {

	public abstract String getVersion();

	public abstract boolean isRunning();

	public abstract void stop();

	public abstract void startService();

	public abstract void run();

	public abstract Long getRequests();

	public abstract void addRequest();

	public abstract Long getClients();

	public abstract void addClient();

	public abstract void delClient();

	public abstract Long getFirstInsert();

	public abstract void addFirstInsert();

	public abstract Long getadmittedMatch();

	public abstract void addadmittedMatch();

	public abstract Long getadmittedAWL();

	public abstract void addadmittedAWL();

	public abstract Long getfirstReject();

	public abstract void addfirstReject();

	public abstract Long getUpdate();

	public abstract void addUpdate();

	public abstract Long getFreq();

	public abstract void setFreq(Long freq);

	public abstract Configuration getConfig();

	public abstract Logger getLogger();

	public abstract String getBindAddress();

	public abstract void createDatabase() throws FatalHelmException,
			NonFatalHelmException;

	public abstract void garbageCollectDatabase() throws FatalHelmException,
			NonFatalHelmException;

	public abstract void resetDatabase() throws FatalHelmException,
			NonFatalHelmException;
	public void setHelmConfiguration(HelmConfiguration helmConfiguration);
	
	public void init() throws TerminatingHelmException;


}