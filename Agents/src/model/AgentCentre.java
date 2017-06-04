package model;

public class AgentCentre {

	private String alias;
	private String adress;
	
	public AgentCentre() {
		super();
	}

	public AgentCentre(String alias, String adress) {
		super();
		this.alias = alias;
		this.adress = adress;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getAdress() {
		return adress;
	}

	public void setAdress(String adress) {
		this.adress = adress;
	}
	
}
