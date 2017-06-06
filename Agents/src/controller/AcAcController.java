package controller;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

public interface AcAcController {

	 
	 String registerNewOnMaster();
	 //Registruj novog CENTRA na master, update listu svuda
	 //init ga pokreci
	 
	 void bla();
	 
	 String registerNewTypeOnMaster();
	 //Pitaj novog za tip koji podrzava, dodaj tip, updateuj listu tipova
	 
	 
	 String getRunningAgents();
	 //Dostavi listu svih koji trce
	 
}
