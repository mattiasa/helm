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
import org.apache.log4j.Logger;

public class Db {
	Logger log = Logger.getLogger(this.getClass());
	

	HelmConfiguration config;
	
	public void init() throws TerminatingHelmException {
		setupDriver(config.getJdbcDriver(),
				config.getJdbcUrl());
	}
	
	public Connection getConnection() throws SQLException {
		Connection conn;
		log.debug("Creating connection.");
		long starttime = System.currentTimeMillis();
        conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:helm");
        log.debug("TIMER: getConnection: " + (System.currentTimeMillis() - starttime));
        return conn;
	}
	
	public void returnConnection(Connection conn) throws NonFatalHelmException {
		
		try { 
			conn.close(); 
		
		} catch(SQLException e) { 
			throw new NonFatalHelmException("Caught exception when terminating db connection", e);
		}
		
	}
	
    public void setupDriver(String driver, String connectURI) throws TerminatingHelmException {
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
        
        // Just to remove eclipse warning 
        poolableConnectionFactory.toString();
        
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
    
    void shutdownDriver() throws TerminatingHelmException {
        try {
        	PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        	driver.closePool("example");
        } catch (SQLException e) {
        	throw new TerminatingHelmException("Could not close pool", e);
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
    

	public void setConfig(HelmConfiguration config) {
		this.config = config;
	}
    
}
