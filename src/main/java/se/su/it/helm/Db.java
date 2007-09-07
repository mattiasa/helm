package se.su.it.helm;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.testng.annotations.Test;

public class Db {
	Logger log;
	
	public Db(String connectURI, Logger log) throws TerminatingHelmException {
		this.log = log;
		/*
		try {
		*/
		setupDriver(connectURI);
	}
	
	public Connection getConnection() throws SQLException {
		Connection conn;
		log.debug("Creating connection.");
        conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:helm");
        return conn;
	}
	
	public void returnConnection(Connection conn) throws NonFatalHelmException {
		
		try { 
			conn.close(); 
		
		} catch(SQLException e) { 
			throw new NonFatalHelmException("Caught exception when terminating db connection", e);
		}
		
	}
	
    public void setupDriver(String connectURI) throws TerminatingHelmException {
    	String driver = "com.mysql.jdbc.Driver";
    	/*
    	 * Begin by loading the mysql driver
    	 */
    	
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new TerminatingHelmException("Couldn't find jdbc driver" + driver, e);
        }
    	
    	//
        // First, we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        ObjectPool connectionPool = new GenericObjectPool(null);

        //
        // Next, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        //
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI,null);

        //
        // Now we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);

        //
        // Finally, we create the PoolingDriver itself...
        //
        String poolingDriver = "org.apache.commons.dbcp.PoolingDriver";
        try {
        	
        	Class.forName(poolingDriver);
        } catch (ClassNotFoundException e) {
            throw new TerminatingHelmException("Couldn't find pooling driver " + poolingDriver, e);
        }
        
        try {
        	PoolingDriver poolDriver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        	poolDriver.registerPool("helm",connectionPool);
        } catch (SQLException e) {
        	throw new TerminatingHelmException("Could not initiate dbcp", e); 
        }
    }
	
	/*
	
	@Test (groups = {"test1"})
	public void testGetGreylistData() {
		ConnectionData data = new ConnectionData();
		data.setRecipientAddress("ture@teknolog.se");
		data.setSenderAddress("nisse@kaka.se");
		data.setSenderIp("17.47.11.13");
		
		GreylistData gd = getGreylistData(data);
		
		assert(gd != null);
		assert(gd.getIp().equals(data.getSenderIp()));
		assert(gd.getSender()).equals(data.getSenderAddress());
		assert(gd.getRecipient()).equals(data.getRecipientAddress());
	}
	*/
}
