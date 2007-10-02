package se.su.it.helm;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

class ClientHandler extends Thread {
	private BufferedReader in;
	private PrintWriter out;
	private HelmServer server;
	private Logger log;
	private Socket socket;
	private Greylist greylist;
	
	public ClientHandler(HelmServer server, Socket socket, Logger log, Greylist greylist) throws FatalHelmException {
		this.server = server;
		this.socket = socket;
		this.log = log;
		this.greylist = greylist;
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			throw new FatalHelmException("Caught IOException when creating input reader", e);
		}
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			throw new FatalHelmException("Caught IOException when creating output writer", e);
		}
	}
	
	/**
	 * @param in - input stream
	 * @return object with data about session
	 */
	
	private ConnectionData readObject(BufferedReader in) throws NonFatalHelmException, FatalHelmException {
		ConnectionData ret = new ConnectionData();
		
		Map<String, String> map = new HashMap<String, String>(); 
		
		while(true) {
			String msg = "";
			try {
				msg = in.readLine();
				
				/* null means end of stream */
				if(msg == null) {
					log.info("Client closed connection");
					return null;
				}
			} 
			catch (IOException ioe) {
				throw new FatalHelmException("Got IOException from readline", ioe);
			}
			
			/* End of this object */
			if(msg.equals("")) {
				break;
			}
			
			int index = msg.indexOf('=');
			if(index == -1) {
				throw new FatalHelmException("String index not found in data " + msg, null);
			}
			String var = msg.substring(0, index);
			String value = msg.substring(index+1);
		
			map.put(var, value);
		}
		
		ret.setSenderAddress(map.get("sender"));
		ret.setRecipientAddress(map.get("recipient"));
		ret.setSenderIp(map.get("client_address"));
		
		if(ret.getRecipientAddress() != null &&
				ret.getSenderAddress() != null &&
				ret.getSenderIp() != null) {
			return ret;
		} else {
			throw new FatalHelmException("Did not get mandatory data", null);
		}
	}
	
	public void run(){
		ConnectionData data;
		log.info("connection opened to client");
		
		try {
			while (server.isRunning()) {
				String action;
  		 		
				try {
					data = readObject(in); 
					if(data == null) {
						log.info("connection closed to client");
						break;
					}
					
					if (log.isDebugEnabled()) {
						log.debug("Read object:\n" + data);
					}
					
					GreylistResult res = greylist.check(data);
					
					if (res.passmail()) {
						log.info("Passed message " + data);
						action="dunno";
					} else {
						log.info("Blocked message " + data);
						
						action="defer_if_permit " + res.getMessage();
					}
  		 		} catch (NonFatalHelmException e) {
  		 			log.warn("Caught non-fatal exception " + e.getString());
  		 			action = "dunno";
  		 		}
  		 		out.println("action=" + action);
  		 		out.println();
			}
		} catch (FatalHelmException e) {
			log.error("Caught FatalHelmException. Terminating thread: " + e.getString());
			
			/* This is the ultimate fall-through. We tell postfix to pass the email and then close the socket */
			out.println("action=dunno");
		} finally {
			try {
				if(out != null)
					out.close();
				if(in != null)
					in.close();
				if(socket != null)
					socket.close();
			} catch(IOException e) { } 
			
		}
	}
}