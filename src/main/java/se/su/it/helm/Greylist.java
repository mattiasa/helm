package se.su.it.helm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

public class Greylist {

	private Db db;
	private Logger log;
	private Configuration config;
	private int delay;
	
	public Greylist(Configuration config, Logger log) throws TerminatingHelmException {
		this.config = config;
		this.log = log;
		
		db = new Db(config.getString("jdbcDriver", "com.mysql.jdbc.Driver"), 
					config.getString("jdbcUrl"),
					log);
		delay = config.getInt("delay", 20);
		delay *= 1000;		
	}

	void shutdown() throws TerminatingHelmException {
		db.shutdownDriver();
	}
	
	/**
	 * Add an new previous unregistered SMTP connection to the graylist database.
	 * 
	 * @param data - Data about the SMTP connection
	 * @return
	 * @throws NonFatalHelmException
	 */

	public void addGreylistData(ConnectionData data) throws NonFatalHelmException {
		
		PreparedStatement statement;

		Connection conn = null;
		    
		try {
	        	
			log.debug("Getting db connection for add");
			conn = db.getConnection();
	        	
			statement = conn.prepareStatement("INSERT INTO greylist (ip, sender, recipient, first_seen, last_seen, connection_count) " +
					"values (?,?,?,?,?,?)");
			
			statement.setString(1, data.getSenderIp());
			statement.setString(2, data.getSenderAddress());
			statement.setString(3, data.getRecipientAddress());
			statement.setTimestamp(4,new Timestamp(System.currentTimeMillis()));
			statement.setTimestamp(5,new Timestamp(System.currentTimeMillis()));
			statement.setInt(6, 0);
			
			
			log.debug("Executing statement: " + statement);
			statement.execute();
	            
		} catch(SQLException e) {
			e.printStackTrace();
			throw new NonFatalHelmException("Caught exception when inserting data into database.", e);
		} 
		finally { 
			if(conn != null)
				db.returnConnection(conn);
		}

		return;
		
	}

	
	
	/**
	 * Fetch the corresponding graylist entry for the ConnectionData from the
	 * database if it exists
	 * 
	 * @param data Data about the SMTP connection
	 * @return a GreylistData object or null if the entry was not in the
	 *         database
	 * @throws NonFatalHelmException
	 */

	public GreylistData getGreylistData(ConnectionData data) throws NonFatalHelmException {
		
		PreparedStatement statement;
		GreylistData ret = null;
		
		Connection conn = null;
		ResultSet rset = null;
	        
		try {
	        	
			log.debug("Getting db connection for get");
			conn = db.getConnection();
	        	
			statement = conn.prepareStatement("SELECT id,ip,sender,recipient,first_seen,last_seen,connection_count FROM greylist where ip = ? and sender = ? and recipient = ?");
			
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
				Timestamp first_seen = rset.getTimestamp("first_seen");
				Timestamp last_seen = rset.getTimestamp("last_seen");
				int count = rset.getInt("connection_count");
			
				ret = new GreylistData(id , sender, recipient, ip, first_seen, last_seen, count);
			}
			
			if(rset.next()) { 
				log.warn("Got multiple entries for query " + data);
			}
	            
		} catch(SQLException e) {
			e.printStackTrace();
			throw new NonFatalHelmException("Got SQLException when looking up in database", e);
		} 
		finally { 
			if(conn != null)
				db.returnConnection(conn);
		}

		return ret;
		
	}

	
	/**
	 * Update the entry in the graylist database.
	 * 
	 * @param data - Database entry representing about the SMTP connection
	 * @throws NonFatalHelmException
	 */

	public void updateGreylistData(GreylistData data) throws NonFatalHelmException {
		
		PreparedStatement statement;
		
		Connection conn = null;

		try {
	        	
			log.debug("Getting db connection for update");
			conn = db.getConnection();
	        	
			statement = conn.prepareStatement("UPDATE greylist set connection_count=?, last_seen=? where id = ?");
			
			statement.setInt(1,data.getCount());
			statement.setTimestamp(2,data.getLast_seen());
			statement.setInt(3, data.getId());
			
			// log.debug("Executing statement: " + statement);
			statement.execute();

	            
		} catch(SQLException e) {
			e.printStackTrace();
			throw new NonFatalHelmException("Got SQLException when updating database", e);
			
		} 
		finally { 
			if(conn != null)
				db.returnConnection(conn);
		}

		
	}
	
	/**
	 * Check if there is an AWL (auto white list entry) for this SMTP connection data
	 * in the graylist database.
	 *  
	 * @param data
	 * @return true if there is an AWL entry
	 * @throws NonFatalHelmException
	 */

	private boolean
	checkAWL(ConnectionData data, long currentTime) throws NonFatalHelmException
	{
		
		PreparedStatement statement;
		boolean ret = false;
		
		Connection conn = null;
		ResultSet rset = null;
	        
		try {
	        	
			log.debug("Getting db connection for check AWL");
			conn = db.getConnection();
	        	
			statement = conn.prepareStatement("SELECT id FROM greylist WHERE ip = ? and first_seen < ? and connection_count >= 1");
			
			statement.setString(1, data.getSenderIp());
			statement.setTimestamp(2, new Timestamp(currentTime - delay));
			
			log.debug("Executing statement: " + statement);
			rset = statement.executeQuery();
			
			/* found at least one entry */
			if(rset.next()) {
				ret = true;
			}
	            
		} catch(SQLException e) {
			e.printStackTrace();
			throw new NonFatalHelmException("Got SQLException when looking up in database", e);
		} 
		finally { 
			if(conn != null)
				db.returnConnection(conn);
		}

		return ret;
	}
	
	/**
	 * Check if data is allowed to pass or not by looking it up in the
	 * graylist database.
	 * 
	 * @param data the current connection
	 * @return true if connection is allowed to pass, false if needs to be defered.
	 * @throws NonFatalHelmException
	 */
	
	
	public boolean check(ConnectionData data) throws NonFatalHelmException {
				
		GreylistData gl;
		long currentTime = System.currentTimeMillis();

		gl = getGreylistData(data);
		if(gl == null) {
			addGreylistData(data);
		} else if(gl.getLast_seen().before(new Timestamp(currentTime - delay))) {
			gl.setCount(gl.getCount()+1);
			gl.setLast_seen(new Timestamp(currentTime));
			updateGreylistData(gl);
		}
		
		/* does this entry pass by itself */
		if (gl != null && gl.getCount() >= 1) {
			log.debug("Found entry");
			return true;
		}
		
		/* ok, not passing by itself, lets try AWL */
		if (checkAWL(data, currentTime)) {
			log.debug("found AWL entry");
			return true;
		}
		
		log.debug("Not old enough entry found");

		return false;
	}
	
	/**
	 * Create a Graylist database
	 * *
	 * @throws FatalHelmException
	 * @throws NonFatalHelmException
	 */
	
	public void createDatabase() throws FatalHelmException, NonFatalHelmException
	{
		Statement statement;
		Connection conn = null;


		try {
			log.debug("Getting db connection for create");
			conn = db.getConnection();

			String driverName = conn.getMetaData().getDriverName();
			log.error("driver name: " + driverName);
			
			String idIdentity;
			String cached;
			
			if (driverName.equals("HSQL Database Engine Driver")) {
				idIdentity = "id INTEGER IDENTITY";
				cached = "CACHED ";
			} else if (driverName.equals("MySQL-AB JDBC Driver")) {
				idIdentity = "id INTEGER AUTO_INCREMENT";
				cached = "";
			} else {
				throw new FatalHelmException("driver " + driverName + " is unsupported", null);
			}
			
			statement = conn.createStatement();
			statement.executeUpdate(
					"CREATE " + cached + "TABLE greylist (" +
					idIdentity + "," +
					"		sender VARCHAR(255), " +
					"		recipient VARCHAR(255)," +
					"		ip VARCHAR(48),	" +
					"		last_seen DATETIME," +
					"		first_seen DATETIME," +
					"       connection_count INTEGER," +
					"       PRIMARY KEY(id));");
			//statement.executeQuery();

		} catch(SQLException e) {
			e.printStackTrace();
			throw new FatalHelmException("Got SQLException when createing database", e);
		} 
		finally { 
			if(conn != null)
				db.returnConnection(conn);
		}
	}
	
	/**
	 * GC the graylisting database, uses the "gcday" option in the configuration file.
	 * @throws FatalHelmException
	 * @throws NonFatalHelmException
	 */
	
	public void garbageCollectDatabase() throws FatalHelmException, NonFatalHelmException
	{
		PreparedStatement statement;
		Connection conn = null;
		
		long gcdays = config.getInt("gcdays", 5);

		long lastseen = System.currentTimeMillis() - 1000 * 3600 * 24 * gcdays;
		
		try {
			log.debug("Getting db connection for gc");

			conn = db.getConnection();
			statement = conn.prepareStatement("DELETE FROM greylist WHERE last_seen <= ?");
			statement.setTimestamp(1,new Timestamp(lastseen));
			statement.executeUpdate();

		} catch(SQLException e) {
			e.printStackTrace();
			throw new FatalHelmException("Got SQLException when gc-ing database", e);
		} 
		finally { 
			if(conn != null)
				db.returnConnection(conn);
		}
	}
	
	public void resetDatabase() throws FatalHelmException, NonFatalHelmException
	{
		Statement statement;
		Connection conn = null;

		try {
			log.debug("Getting db connection for reset db");

			conn = db.getConnection();

			statement = conn.createStatement();
			// hsqldb doesn't support TRUNCATE TABLE, so use DELETE FROM
			statement.executeUpdate("DELETE FROM greylist");
		} catch(SQLException e) {
			e.printStackTrace();
			throw new FatalHelmException("Got SQLException when resetting database", e);
		} 
		finally { 
			if(conn != null)
				db.returnConnection(conn);
		}

	}

}
