package jms.touser;

import javax.ejb.Local;

import model.ACLMessage;

@Local
public interface MessageToConsumer {
	
	public void sendMessageToAgent(String username, String password, String adresa);
    public void sendACLM(ACLMessage e);
    public void logoutMessage(String username);
    public void getRegisteredUsers();
    public void getActiveUsers();
}
