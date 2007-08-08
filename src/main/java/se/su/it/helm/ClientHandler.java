package se.su.it.helm;

import java.io.*;
import java.net.*;

class ClientHandler extends Thread {
	private BufferedReader in;
	private PrintWriter out;
	private HelmServer server;
	private Socket socket;
	
	public ClientHandler(HelmServer server, Socket socket) throws IOException {
		this.server = server;
		this.socket = socket;
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    out = new PrintWriter(socket.getOutputStream(), true);
	}
	public void run(){
		try {
		 	String message;
		 	while ((message = in.readLine()) != null) {
		 	}

		 	out.close();
		 	in.close();
		 	socket.close();
	     } catch(IOException e) {
	     }
	}
}