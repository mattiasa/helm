package se.su.it.helm;

import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HelmServer {

	
	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext(
		        new String[] {"helm.xml"});

		// an ApplicationContext is also a BeanFactory (via inheritance)
		BeanFactory factory = context;
		
		try {
			if (args.length != 2) {
				System.out.println("usage: java -cp helm-<ver>.jar se.su.it.helm.HelmServer <configfile> [start|stop|statistics|create-database|reset-database|gc]");
				Runtime.getRuntime().exit(1);
			}

			Configuration cnf = new PropertiesConfiguration(args[0]);
					
			if (args[1].equals("start")) {
				HelmMaster s = new HelmMaster(cnf);
				s.startService();
			} else if (args[1].equals("create-database")) {
				HelmMaster s = new HelmMaster(cnf);
				s.createDatabase();
			} else if (args[1].equals("reset-database")) {
				HelmMaster s = new HelmMaster(cnf);
				s.resetDatabase();
			} else if (args[1].equals("gc")) {
				ControllerClient client = new ControllerClient(cnf, HelmMaster.getControllerPort(cnf));
				String r = client.runGarbageCollector();
				System.out.println("gc: " + r);
			} else if (args[1].equals("stop")) {
				ControllerClient client = new ControllerClient(cnf, HelmMaster.getControllerPort(cnf));
				String r = client.stopServer();
				System.out.println("stop: " + r);
			} else if (args[1].equals("statistics")) {
				ControllerClient client = new ControllerClient(cnf, HelmMaster.getControllerPort(cnf));
				
				List<ControllerStatistic> stats= client.getStatistics();
				
				for (ControllerStatistic o : stats) {
					System.out.println(o.getType() + "/" + o.getName() + ": " + o.getValue());
				}
			} else {
				System.err.println("unknown command: " + args[1]);
				Runtime.getRuntime().exit(1);
			}
		} catch (HelmException e) {
			System.out.println(e.getString());
		} catch(Exception e) {
			System.err.println("main exception: " + e);
			e.printStackTrace();
		}
	}
}
