package model;

public class AID {

	private String name;
	private AgentCentre Host;
	private AgentType type;
	
	public AID() {
		super();
	}

	public AID(String name, AgentCentre host, AgentType type) {
		super();
		this.name = name;
		Host = host;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AgentCentre getHost() {
		return Host;
	}

	public void setHost(AgentCentre host) {
		Host = host;
	}

	public AgentType getType() {
		return type;
	}

	public void setType(AgentType type) {
		this.type = type;
	}
	
	
	
}
