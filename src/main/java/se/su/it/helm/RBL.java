package se.su.it.helm;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

public class RBL {

	Logger log;
	Configuration config;

	List<String> rbls;
	
	public RBL(Configuration config, Logger log) {
		this.config = config;
		this.log = log;
		
		
		init();
		
	}
	@SuppressWarnings("unchecked")
	private void init() {
		rbls = config.getList("rbls"); 
	}

	private boolean isInRBL(String rbl, String address, String invAaddress) {
		try {
			InetAddress ret = InetAddress.getByName(invAaddress + "." + rbl);
			if(ret != null) {
				log.debug("Found address " + address + " in rbl " + rbl);
				return true;
			} else {
				return false;
			}
		} catch (UnknownHostException uhe) {
			return false;
		} catch (Exception e) {
			log.info("Caught unexpected exception: " + e.getMessage());
			
			return false;
		}
		
	}
	
	
	private String invert(String address) {
		StringTokenizer t = new StringTokenizer(address, ".");
		
		/* First token is not to be followed by . */
		String inverted = t.nextToken();
		
		while(t.hasMoreTokens()) {
			inverted = t.nextToken() + "." + inverted;  
		}
		
		return inverted;
	}
	
	public boolean isInRBLS(String address) {
		String invAddress = invert(address);
		
		
		for (String rbl:rbls) {
			if(isInRBL(rbl, address, invAddress)) {
				return true;
			}
		}
		
		return false;
	}
	
}

