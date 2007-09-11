package se.su.it.helm;

import java.sql.Timestamp;

import org.testng.annotations.Test;

public class GreylistData {

	public GreylistData(int id, String sender, String recipient, String ip, Timestamp first_seen, Timestamp last_seen, int count) { 
		this.id = id;
		this.sender = sender;
		this.recipient = recipient;
		this.ip = ip;
		this.first_seen = first_seen;
		this.last_seen = last_seen;
		this.count = count;
	}
	
	protected int id;
	protected String sender;
	protected String recipient;
	protected String ip;
	protected Timestamp last_seen;
	protected Timestamp first_seen;
	protected int count;
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Timestamp getLast_seen() {
		return last_seen;
	}
	public void setLast_seen(Timestamp last_seen) {
		this.last_seen = last_seen;
	}
	public Timestamp getFirst_seen() {
		return first_seen;
	}
	public void setFirst_seen(Timestamp first_seen) {
		this.first_seen = first_seen;
	}
	public String getRecipient() {
		return recipient;
	}
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	
	@Test (groups = {"test1"})
	public void testCount() {
		int i = 17;
		setCount(i);
		
		int j = getCount();
		assert i == j;
	}
	
	
}
