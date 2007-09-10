package se.su.it.helm;

import java.io.*;
import java.net.*;

import org.apache.commons.configuration.BaseConfiguration;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
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
		config.setProperty("delay", "5");
		config.setProperty("serverport", "4712");
		
		server = new HelmServer(config);
		server.createDatabase();
		// setup server
	}
	
	@AfterClass
	public void tearDown() {
		System.out.println("stop helm");
		server.stop();
	}
	
	@Test (groups = {"connnection"})
	public void connectionTest() throws Exception {
		System.out.println("connection test");
		testMessage("sender=bar@su.se\n" + 
					"recipient=foo@su.se\n" +	
					"client_address=130.237.162.110",	
					"action=defer_if_permit");
	}
	
	@Test (groups = {"connnection"})
	public void connectionBlockTest() throws Exception {
		System.out.println("connection test");
		testMessage("sender=block-s@su.se\n" + 
					"recipient=block-r@su.se\n" +	
					"client_address=130.237.162.2",	
					"action=defer_if_permit");
	}

	@Test (groups = {"connnection"})
	public void connectionPassTest() throws Exception {
		System.out.println("connection test");
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

	
	
	
}
