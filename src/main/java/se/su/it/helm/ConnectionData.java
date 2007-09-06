package se.su.it.helm;

import java.util.Date;

import org.testng.annotations.Test;




public class ConnectionData {
	private String senderIp;
	private String senderAddress;
	private String recipientAddress;
	private Date creationTime = new Date();
	
	public String getRecipientAddress() {
		return recipientAddress;
	}
	public void setRecipientAddress(String recipientAddress) {
		this.recipientAddress = recipientAddress;
	}
	public String getSenderAddress() {
		return senderAddress;
	}
	public void setSenderAddress(String senderAddress) {
		this.senderAddress = senderAddress;
	}
	public String getSenderIp() {
		return senderIp;
	}
	public void setSenderIp(String senderIp) {
		this.senderIp = senderIp;
	}

	@Test (groups = {"test1"})
	public void testSenderIp() {
		String ip = "10.47.11.17";
		setSenderIp(ip);
		
		String newIp = getSenderIp();
		assert ip.equals(newIp);
	}
	
	@Test (groups = {"test1"})
	public void testSenderAddress() {
		String data = "ture@teknolog.se";
		setSenderAddress(data);
		
		String newData = getSenderAddress();
		assert data.equals(newData);
	}
	
	@Test (groups = {"test1"})
	public void testRecipientAddress() {
		String data = "arne@anka.se";
		setRecipientAddress(data);
		
		String newData = getRecipientAddress();
		assert data.equals(newData);
	}
	
	
	/*
	@Parameters({ "first-name" })
	@Test
	public void testSingleString(String firstName) { 
	  System.out.println("Invoked testString " + firstName);
	  assert "Cedric".equals(firstName);
	}
	
	*/
	
	public String toString() {
		String ret = 
			"Helm ConnectionData:" + "\n" +
			"  Time: " + creationTime + "\n" +
			"  IP: " + getSenderIp() + "\n" +
			"  Sender Address: " + getSenderAddress() + "\n" +
			"  Recipient Address: " + getRecipientAddress() + "\n";
		return ret;
		
	}

	

public static void main() {
	return;
}
}