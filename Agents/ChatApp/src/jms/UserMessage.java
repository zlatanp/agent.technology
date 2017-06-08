package jms;

import java.io.Serializable;

import model.User;

public class UserMessage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4085178134013924849L;
	
	public static enum types {
		REGISTER, LOGIN, LOGOUT
	}
	
	private String username;
	private String password;
	private types type;
	private String address;
    private String alias;
    private User u;
    private String sessionId;
    
	
	public UserMessage(){}

	public UserMessage(String username, String password, String address, String alias, types type) {
        this.username    = username;
        this.password    = password;
        this.address     = address;
        this.alias       = alias;
        this.type = type;
    }
	
	public UserMessage(String username) {
        this.username    = username;
    }
	

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public types getType() {
		return type;
	}

	public void setType(types type) {
		this.type = type;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public User getU() {
		return u;
	}

	public void setU(User u) {
		this.u = u;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	

}
