package se.su.it.helm;

import java.io.*;
import java.net.*;

import org.apache.commons.configuration.BaseConfiguration;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterClass;

public class TestServer {
	
	int testServerPort = 4712;
	HelmServer server;
	
	private void
	testMessage(String message, String expects)
		throws Exception
	{
		Socket socket;
		
		socket = new Socket("localhost", testServerPort);
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

	    out.println(message);
	    out.println("");
	    String msg = in.readLine();
	    
 	    if (msg == null) {
 	    	throw new Exception("server disconnected on us");
 	    }
	    
	    if (!msg.equals(expects)) {
	    	throw new Exception("expected data not returned: " + msg);
	    }
	    out.close();
	    in.close();
	    socket.close();
	}

	@BeforeClass
	public void setUp() throws Exception {
		System.out.println("setup db");
		System.out.println("start helm");

		BaseConfiguration config = new BaseConfiguration();
		
		config.setProperty("jdbcUrl", "jdbc:hsqldb:mem:aname");
		config.setProperty("jdbcDriver", "org.hsqldb.jdbcDriver");
		config.setProperty("serverport", "4712");
		config.setProperty("controllerPort", "4713");
		config.setProperty("delay", "5");
		
		server = new HelmServer(config);
		server.createDatabase();
		server.startService();
		// setup server
	}
	
	@AfterClass
	public void tearDown() {
		System.out.println("stop helm");
		server.stop();
	}
	
	@BeforeMethod
	public void clearDataBase() throws Exception {
		server.resetDatabase();
	}
	
	@Test (groups = {"connection"})
	public void connectionTest() throws Exception {
		System.out.println("connection test");
		testMessage("sender=bar@su.se\n" + 
					"recipient=foo@su.se\n" +	
					"client_address=130.237.162.1",	
					"action=defer_if_permit");
	}
	
	@Test (groups = {"connection"})
	public void connectionBlockTest() throws Exception {
		System.out.println("connection block test");
		testMessage("sender=block-s@su.se\n" + 
					"recipient=block-r@su.se\n" +	
					"client_address=130.237.162.2",	
					"action=defer_if_permit");
	}

	@Test (groups = {"connection"})
	public void connectionPassTest() throws Exception {
		System.out.println("connection pass test");
		testMessage("sender=pass-s@su.se\n" + 
					"recipient=pass-r@su.se\n" +	
					"client_address=130.237.162.3",	
					"action=defer_if_permit");
	
		Thread.sleep(6000);
		
		testMessage("sender=pass-s@su.se\n" + 
					"recipient=pass-r@su.se\n" +	
					"client_address=130.237.162.3",	
					"action=dunno");

		
	}

	@Test (groups = {"connection"})
	public void connectionMultiConnTest() throws Exception {
		System.out.println("connection multi test");
		testMessage("sender=pass2-s@su.se\n" + 
					"recipient=pass2-r@su.se\n" +	
					"client_address=130.237.162.4",	
					"action=defer_if_permit");
	
		Thread.sleep(2000);

		testMessage("sender=pass2-s@su.se\n" + 
					"recipient=pass2-r@su.se\n" +	
					"client_address=130.237.162.4",	
					"action=defer_if_permit");

		Thread.sleep(6000);
		
		testMessage("sender=pass2-s@su.se\n" + 
					"recipient=pass2-r@su.se\n" +	
					"client_address=130.237.162.4",	
					"action=dunno");
		
	}

	@Test (groups = {"connection"})
	public void connectionMultiSendTest() throws Exception {
		System.out.println("connection multi send test");
		testMessage("sender=pass3-s@su.se\n" + 
					"recipient=pass3-r@su.se\n" +	
					"client_address=130.237.162.5",	
					"action=defer_if_permit");
	
		Thread.sleep(6000);
		
		testMessage("sender=pass3-s@su.se\n" + 
					"recipient=pass3-r@su.se\n" +	
					"client_address=130.237.162.5",	
					"action=dunno");

		Thread.sleep(1000);
	
		testMessage("sender=pass3-s@su.se\n" + 
					"recipient=pass3-r@su.se\n" +	
					"client_address=130.237.162.5",	
					"action=dunno");	
		
	}
	
	@Test (groups = {"connection"})
	public void connectionMultiSendDiffrentTest() throws Exception {
		System.out.println("connection send diffrent test");
		testMessage("sender=pass4-s@su.se\n" + 
					"recipient=pass4-r@su.se\n" +	
					"client_address=130.237.162.6",	
					"action=defer_if_permit");

		Thread.sleep(6000);
		
		testMessage("sender=pass4-s@su.se\n" + 
					"recipient=pass4-r@su.se\n" +	
					"client_address=130.237.162.7",	
					"action=defer_if_permit");
	
		testMessage("sender=pass4-s@su.se\n" + 
					"recipient=pass4-r@su.se\n" +	
					"client_address=130.237.162.6",	
					"action=dunno");

		Thread.sleep(1000);
	
		testMessage("sender=pass3-s@su.se\n" + 
					"recipient=pass3-r@su.se\n" +	
					"client_address=130.237.162.6",	
					"action=dunno");	
		
	}
	
	@Test (groups = {"connection"})
	public void connectionAWL() throws Exception {
		System.out.println("connection awl test");
		testMessage("sender=pass5-s@su.se\n" + 
					"recipient=pass5-r@su.se\n" +	
					"client_address=130.237.162.8",	
					"action=defer_if_permit");

		Thread.sleep(6000);
		
		testMessage("sender=pass5-s@su.se\n" + 
					"recipient=pass5-r@su.se\n" +	
					"client_address=130.237.162.8",	
					"action=dunno");
	
		testMessage("sender=pass5-s2@su.se\n" + 
					"recipient=pass5-r2@su.se\n" +	
					"client_address=130.237.162.8",	
					"action=dunno");
	}
	
	@Test (groups = {"controller"})
	public void controllerCheck() throws Exception {
		HelmControllerClient client = new HelmControllerClient(4713);
		
		String s = client.checkServer();
		if (!s.equals("Server running")) {
		    throw new Exception("server not running ?");
		}
	}
	@Test (groups = {"controller"})
	public void controllerGC() throws Exception {
		HelmControllerClient client = new HelmControllerClient(4713);
		
		String s = client.runGarbageCollector();
		if (!s.equals("ok")) {
		    throw new Exception("error while running gc?: " + s);
		} 
	}

}
