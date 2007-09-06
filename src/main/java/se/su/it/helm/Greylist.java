package se.su.it.helm;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Greylist {

	Db db;
	Logger log;
	
	public Greylist(Logger log) {
		this.log = log;
		
		db = new Db("jdbc:mysql://srv2.db.su.se/helm_devel?user=helm_devel&password=bF6f4qgEme7QkL8o", log);
	}
	
	/**
	 * 
	 * @param data - Data about the smtp connection
	 * @return
	 */

	public void putGreylistData(ConnectionData data) {
		
		PreparedStatement statement;

		Connection conn = null;
		    
		try {
	        	
			log.debug("Getting connection");
			conn = db.getConnection();
	        	
			statement = conn.prepareStatement("INSERT INTO greylist (ip, sender, recipient, first_seen, last_seen, count) " +
					"values (?,?,?,?,?,?)");
			
			statement.setString(1, data.getSenderIp());
			statement.setString(2, data.getSenderAddress());
			statement.setString(3, data.getRecipientAddress());
			statement.setTimestamp(4,new Timestamp(System.currentTimeMillis()));
			statement.setTimestamp(5,new Timestamp(System.currentTimeMillis()));
			statement.setInt(6, 0);
			
			
			log.debug("Executing statement: " + statement);
			if(!statement.execute()) {
				log.log("Failed to execute statement " + statement);
			}
	            
		} catch(SQLException e) {
			e.printStackTrace();
		} 
		finally { 
			if(conn != null)
				db.returnConnection(conn);
		}

		return;
		
	}

	
	
	/**
	 * 
	 * @param data - Data about the smtp connection
	 * @return a GreylistData object or null if the entry was not in the database
	 */

	public GreylistData getGreylistData(ConnectionData data) {
		
		PreparedStatement statement;
		GreylistData ret = null;
		
		Connection conn = null;
		ResultSet rset = null;
	        
		try {
	        	
			log.debug("Getting connection");
			conn = db.getConnection();
	        	
			statement = conn.prepareStatement("SELECT * FROM greylist where ip = ? and sender = ? and recipient = ?");
			
			statement.setString(1, data.getSenderIp());
			statement.setString(2, data.getSenderAddress());
			statement.setString(3, data.getRecipientAddress());
			
			log.debug("Executing statement: " + statement);
			rset = statement.executeQuery();
			
			
			if(rset.next()) { 
			
				int id = rset.getInt("id");
				String ip = rset.getString("ip");
				String sender = rset.getString("sender");
				String recipient = rset.getString("recipient");
				Timestamp last_seen = rset.getTimestamp("last_seen");
				int count = rset.getInt("count");
			
				ret = new GreylistData(id, sender, recipient, ip, last_seen, count);
			}
			
			if(rset.next()) { 
				log.log("Got multiple entries for query " + data);
			}
	            
		} catch(SQLException e) {
			e.printStackTrace();
		} 
		finally { 
			if(conn != null)
				db.returnConnection(conn);
		}

		return ret;
		
	}

	

	/**
	 * 
	 * @param data - Data about the smtp connection
	 */

	public void updateGreylistData(GreylistData data) {
		
		PreparedStatement statement;
		
		Connection conn = null;

		try {
	        	
			log.debug("Getting connection");
			conn = db.getConnection();
	        	
			statement = conn.prepareStatement("UPDATE greylist set count=?,last_seen=? where ip = ? and sender = ? and recipient = ?");
			
			statement.setInt(1,data.getCount());
			statement.setTimestamp(2,data.getLast_seen());
			statement.setString(3, data.getIp());
			statement.setString(4, data.getSender());
			statement.setString(5, data.getRecipient());
			
			// log.debug("Executing statement: " + statement);
			statement.execute();

	            
		} catch(SQLException e) {
			e.printStackTrace();
		} 
		finally { 
			if(conn != null)
				db.returnConnection(conn);
		}

		
	}

	public boolean checkAWL(ConnectionData data) {
		
		PreparedStatement statement;
		boolean ret = false;
		
		Connection conn = null;
		ResultSet rset = null;
	        
		try {
	        	
			log.debug("Getting connection");
			conn = db.getConnection();
	        	
			statement = conn.prepareStatement("SELECT id FROM greylist where ip = ? and first_seen < ? and count >= 1");
			
			statement.setString(1, data.getSenderIp());
			statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()- 60 * 1000));
			
			// log.debug("Executing statement: " + statement);
			rset = statement.executeQuery();
			
			
			if(rset.next()) { 
				log.debug("Found entry");
				return true;
			}
			    
		} catch(SQLException e) {
			e.printStackTrace();
		} 
		finally { 
			if(conn != null)
				db.returnConnection(conn);
		}

		return ret;
		
	}
		
		
	
	
	public boolean check(ConnectionData data) {
		
/*		Db db = new Db("jdbc:mysql://srv2.db.su.se/helm_devel?user=helm_devel&password=nv7UI0nkz");*/
		
		GreylistData gl = getGreylistData(data);
		long currentTime = System.currentTimeMillis();
		
		if(gl == null) {
			putGreylistData(data);
		} else if(gl.getLast_seen().before(new Timestamp(currentTime - 60 * 1000 ))) {
			gl.setCount(gl.getCount()+1);
			gl.setLast_seen(new Timestamp(currentTime));
			updateGreylistData(gl);
		}
		
		return checkAWL(data);
		
	}
	
}
