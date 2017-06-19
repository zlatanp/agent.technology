package controller;

import java.util.ArrayList;

import model.Agent;
import model.AgentCentre;
import model.AgentType;

public interface AcAcController {

	boolean active();
	String registerNewOnMaster();
	void addToMaster(String address, String aliasPassed);
	void updateCenters(ArrayList<AgentCentre> s);
	void updateTypes(ArrayList<AgentType> s);
	void udpdateTypesAll(ArrayList<AgentType> t);
	void bla();
	void deleteCent(String adress);
	void deleteCenter(String alias);
	String newRunningAgent(String alias, String name, String adress);
	void updateRunning(ArrayList<Agent> s);
	ArrayList<AgentType> getAllTypes();
	ArrayList<Agent> getAllRunning();
	String registerNewTypeOnMaster();
	String getRunningAgents(String name, String adress);
	ArrayList<AgentCentre> getAllCentres();
	void runTask1();
	void SendMessage(String poruka, String name, String adresa);
	void receiveMessage(String poruka);
	String getOutput();
}
