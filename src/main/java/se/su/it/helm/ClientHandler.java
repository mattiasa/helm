package se.su.it.helm;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

class ClientHandler extends Thread {
	private BufferedReader in;
	private PrintWriter out;
	private HelmServer server;
	private Logger log;
	private Socket socket;
	private Greylist greylist;
	
	
	
	public ClientHandler(HelmServer server, Socket socket, Logger log, Greylist greylist) throws IOException {
		this.server = server;
		this.socket = socket;
		this.log = log;
		this.greylist = greylist;
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    out = new PrintWriter(socket.getOutputStream(), true);
	}
	
	/**
	 * @param in - input stream
	 * @return object with data about session
	 */
	
	private ConnectionData readObject(BufferedReader in) {
		ConnectionData ret = new ConnectionData();
		
		Map<String, String> map = new HashMap<String, String>(); 
		
		while(true) {
			String msg = "";
			try {
				msg = in.readLine();
				
				/* null means end of stream */
				if(msg == null) {
					return null;
				}
			} 
			catch (IOException ioe) {
				ioe.printStackTrace();
				return null;
			}
			
			if(msg.equals("")) {
				break;
			}
			
			int index = msg.indexOf('=');
			if(index == -1) {
				log.log("String index not found in data " + msg);
				return null;
			}
			String var = msg.substring(0, index);
			String value = msg.substring(index+1);
		
			map.put(var, value);
			
			// log.debug("Variable = " + var + " value = " + value);
		}
		
		ret.setSenderAddress(map.get("sender"));
		ret.setRecipientAddress(map.get("recipient"));
		ret.setSenderIp(map.get("client_address"));
		
		if(ret.getRecipientAddress() != null &&
				ret.getSenderAddress() != null &&
				ret.getSenderIp() != null) {
			return ret;
		} else {
			
			log.debug("Returning null");
			return null;
		}
	}
	
	public void run(){
		ConnectionData data = new ConnectionData();
		log.debug("  Started child");
		try {
		
  		 	while (true) {
  		 		data = readObject(in); 
  		 		
  		 		if(data == null)
  		 			break;
  		 			
  		 		try {
  		 			if(greylist.check(data)) {
  		 				out.println("action=dunno");
  		 			} else {
  		 				out.println("action=defer_if_permit");
  		 			}
  		 		} catch (Exception e) {
  		 			/* fall through */
  		 			e.printStackTrace();
  		 			out.println("action=dunno");
  		 		}
  		 		// log.debug("Read object:\n" + data);

  		 	}
  		 	
		 	out.close();
		 	in.close();
		 	socket.close();
	     
		 	log.debug("Client terminated");
		} catch(IOException e) {
		}
	}
}