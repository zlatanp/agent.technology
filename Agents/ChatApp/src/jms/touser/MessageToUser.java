package jms.touser;

import javax.ejb.Local;

import model.User;

@Local
public interface MessageToUser {
	
	public void registerMessage(String username, String password);
    public void loginMessage(String username, String password);
    public void logoutMessage(String username);
    public void getRegisteredUsers();
    public void getActiveUsers();
}
