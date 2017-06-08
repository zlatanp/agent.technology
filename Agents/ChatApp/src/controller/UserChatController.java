package controller;

import java.util.ArrayList;


import exceptions.AliasExistsException;
import model.Host;
import model.Message;
import model.User;

public interface UserChatController {

	public String initialise();
	String register(String address, String alias) throws AliasExistsException;
	String unregister();
	String registerUser(String username, String password, String passwordRepeat);
	String login(String username, String password);
	String logout(String username);
	void addUser(String username, String password);
	void removeUser(String username);
	void publish(Message message);
	public ArrayList<User> getAllUsers();
	void updateCvorove(ArrayList<Host> u);
	void addUserOnAnother(String username, String password);
	void removeOnAnother(String username);
	ArrayList<User> getOnlineUsers();
	
}
