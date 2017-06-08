package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.servlet.ServletConfig;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import model.Agent;
import model.AgentCentre;
import model.AgentType;
import model.Host;

@Startup
@Lock(LockType.READ)
@Singleton
@Path("/agents")
public class AcAcControllerImpl implements AcAcController {

	private ArrayList<AgentCentre> allCentres = new ArrayList<AgentCentre>();
	private ArrayList<AgentType> types = new ArrayList<AgentType>();
	private ArrayList<AgentType> allTypes = new ArrayList<AgentType>();
	private ArrayList<Agent> runningAgents = new ArrayList<Agent>();

	@Context
	ServletConfig config;

	@Context
	private UriInfo uriInfo;

	private AgentCentre masterCenter;
	private String myAdress;
	private String alias = randomIdentifier();

	@Override
	@POST
	@Path("/node")
	public String registerNewOnMaster() {


		masterCenter = new AgentCentre();
		masterCenter.setAdress("8080");
		masterCenter.setAlias("MasterCenter");
		
		AgentType ping = new AgentType("Ping", "01");
		AgentType pong = new AgentType("Pong", "02");
		AgentType mapReduce = new AgentType("MapReduce", "03");
		AgentType contractNet = new AgentType("ContractNet", "04");
		
		allTypes.add(ping);
		allTypes.add(pong);

		boolean firstMaster = true;

		myAdress = uriInfo.getBaseUri().toString();
		String[] pom = myAdress.split(":");
		String[] pom2 = pom[2].split("/");
		String adress = pom2[0];

		System.out.println(myAdress);

		if (!(myAdress.equals("http://localhost:8080/ChatApp/rest/"))) {
			System.out.println("Dodajem nemastere");
			String url = "http://localhost:8080/ChatApp/rest/agents/node/" + adress + "/" + alias;

			try {

				URL url2 = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept", "application/json");

				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

				String output;
				System.out.println("Output from Server .... \n");
				while ((output = br.readLine()) != null) {
					System.out.println(output);

				}

				conn.disconnect();

			} catch (MalformedURLException e) {

				e.printStackTrace();

			} catch (IOException e) {

				e.printStackTrace();

			}
			
		} else {
			System.out.println("Dodajem Mastera");
			for (AgentCentre c : allCentres) {
				if (c.getAlias().equals("MasterCenter"))
					firstMaster = false;
			}
			if (firstMaster){
				allCentres.add(masterCenter);
				types.add(ping);
				types.add(pong);
			}
		}

		return null;
	}

	@Override
	@GET
	@Path("/node/{adress}/{aliasPassed}")
	public void addToMaster(@PathParam("adress") String address, @PathParam("aliasPassed") String aliasPassed) {
		AgentCentre c = new AgentCentre(aliasPassed, address);
		allCentres.add(c);
		System.out.println("Dodajem neMastera");
		// Dodaj listu svima drugima
		for (int i = 0; i < allCentres.size(); i++) {
			System.err.println(allCentres.size());
			if(!allCentres.get(i).getAdress().equals("8080"))
				updateAllNodes(allCentres.get(i).getAdress());
				updateType(allCentres.get(i).getAdress());
		}

	}

	@Override
	@POST
	@Path("/updateCenters")
	@Consumes(MediaType.APPLICATION_JSON)
	synchronized public void updateCenters(ArrayList<AgentCentre> s) {
		System.out.println("USPIO:" + s);
		this.allCentres = s;
	}
	
	@Override
	@POST
	@Path("/updateType")
	@Consumes(MediaType.APPLICATION_JSON)
	synchronized public void updateTypes(ArrayList<AgentType> s) {
		System.out.println("TYPES:" + s);
		boolean isnew = true;
		int i;
		for (AgentType t : s) {
			for(i=0;i<types.size();i++){
				if(t.getName().equals(types.get(i).getName()))
					isnew = false;
			}
			if(i==types.size()-1 && isnew)
				types.add(t);
			isnew = true;
		}
	}

	//Test liste
	@Override
	@GET
	@Path("/all")
	public void bla() {
		System.out.println("Svi centri: " + allCentres.size());
		for (AgentType tip : allTypes) {
			System.out.println(tip.getName());
		}
	}

	//MASTER UPDATE TIPOVA NOVOG
	@Override
	@GET
	@Path("/agents/classes")
	public String registerNewTypeOnMaster() {
		return null;
	}

	//MASTER UPDATE POKRENUTIH NOVOGA
	@Override
	@POST
	@Path("/agents/running")
	public String getRunningAgents() {
		return null;
	}

	// nakon dolaska novog svi pozivaju

	public void updateAllNodes(String adress) {
		System.out.println("udjo" + adress);
		
		try {
			URL url = new URL("http://localhost:"+ adress +"/ChatApp/rest/agents/updateCenters");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");

			String input = new Gson().toJson(allCentres);

			System.out.println("INPUT JE: " + input);

			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
	}
	
	public void updateType(String adress){
		try {

			URL url = new URL("http://localhost:"+ adress +"/ChatApp/rest/agents/updateType");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");

			String input = new Gson().toJson(types);

			//System.out.println("INPUT JE: " + input);
			
			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			conn.disconnect();

		  } catch (MalformedURLException e) {

			e.printStackTrace();

		  } catch (IOException e) {

			e.printStackTrace();

		 }
		
	}

	private ArrayList<AgentCentre> fromJson(String output, Type type) {
		return new Gson().fromJson(output, type);
	}

	// Generate random Alias for non-master chat app

	final static String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345674890";

	final static java.util.Random rand = new java.util.Random();

	// consider using a Map<String,Boolean> to say whether the identifier is
	// being used or not
	final static Set<String> identifiers = new HashSet<String>();

	public static String randomIdentifier() {
		StringBuilder builder = new StringBuilder();
		while (builder.toString().length() == 0) {
			int length = rand.nextInt(5) + 5;
			for (int i = 0; i < length; i++) {
				builder.append(lexicon.charAt(rand.nextInt(lexicon.length())));
			}
			if (identifiers.contains(builder.toString())) {
				builder = new StringBuilder();
			}
		}
		return builder.toString();
	}

}
