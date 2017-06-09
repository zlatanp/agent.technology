package controller;

import java.util.ArrayList;


import model.AgentCentre;
import model.AgentType;

public interface AcAcController {

	String registerNewOnMaster();
	void addToMaster(String address, String aliasPassed);
	void updateCenters(ArrayList<AgentCentre> s);
	void updateTypes(ArrayList<AgentType> s);
	void udpdateTypesAll(ArrayList<AgentType> t);
	void bla();
	void deleteCent();
	void deleteCenter(String alias);
	String registerNewTypeOnMaster();
	String getRunningAgents();
}