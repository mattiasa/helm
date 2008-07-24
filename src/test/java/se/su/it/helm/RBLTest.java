package se.su.it.helm;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.junit.Test;

import junit.framework.TestCase;




public class RBLTest extends TestCase {

	private Logger log = Logger.getLogger(RBLTest.class.getName());
	private RBL rbl;
	
	protected void setUp() throws Exception {
		super.setUp();

		Configuration config = new BaseConfiguration();
		

		config.setProperty("delay", "5");
		config.setProperty("rbldelay", "10");
		config.setProperty("rbls", "pbl.spamhaus.org");
		
		rbl = new RBL(config, log);
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
 
	@Test
	public void test127_0_0_1() {
		assertFalse(rbl.isInRBLS("127.0.0.1"));
	}
	@Test
	public void test127_0_0_2() {
		assertTrue(rbl.isInRBLS("127.0.0.2"));
	}
	
	
}
