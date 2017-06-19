package jms.touser;

import javax.ejb.Local;

@Local
public interface MessageToConsumer {
	
	public void sendMessageToAgent(String username, String password, String adresa);
    public void loginMessage(String username, String password);
    public void logoutMessage(String username);
    public void getRegisteredUsers();
    public void getActiveUsers();
}
