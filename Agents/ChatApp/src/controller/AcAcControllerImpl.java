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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.servlet.ServletConfig;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import model.AID;
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
	private HashMap<String, String> heartbeat = new HashMap<String, String>();

	@Context
	ServletConfig config;

	@Context
	private UriInfo uriInfo;

	private AgentCentre masterCenter;
	private String myAdress;
	private String alias = randomIdentifier();
	private boolean ismaster = false;

	@Override
	@GET
	@Path("/nodee")
	synchronized public boolean active() {
		return true;
	}

	@Override
	@POST
	@Path("/node")
	public String registerNewOnMaster() {

		masterCenter = new AgentCentre();
		masterCenter.setAdress("8080");
		masterCenter.setAlias("MasterCenter");

		AgentType ping = new AgentType("Ping", "8080");
		AgentType pong = new AgentType("Pong", "8080");
		AgentType mapReduce = new AgentType("MapReduce", "8090");
		AgentType contractNet = new AgentType("ContractNet", "8100");

		boolean firstMaster = true;
		boolean added90 = false;
		boolean added100 = false;

		myAdress = uriInfo.getBaseUri().toString();
		String[] pom = myAdress.split(":");
		String[] pom2 = pom[2].split("/");
		String adress = pom2[0];

		System.out.println(myAdress);

		if (!(myAdress.equals("http://localhost:8080/ChatApp/rest/"))) {
			System.out.println("Dodajem nemastere");

			if (myAdress.equals("http://localhost:8090/ChatApp/rest/") && !added90) {
				ping.setModule("8090");
				types.add(ping);
				types.add(mapReduce);
				added90 = true;
			} else if (myAdress.equals("http://localhost:8100/ChatApp/rest/") && !added100) {
				pong.setModule("8100");
				types.add(pong);
				types.add(contractNet);
				added100 = true;
			}

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

			// Salji tip svoj na 8080
			try {
				URL url2 = new URL("http://localhost:8080/ChatApp/rest/agents/updateType");
				HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");

				String input = new Gson().toJson(types);

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

		} else { // U SLUCAJU DA SI MASTER
			ismaster = true;
			System.out.println("Dodajem Mastera");
			for (AgentCentre c : allCentres) {
				if (c.getAlias().equals("MasterCenter"))
					firstMaster = false;
			}
			if (firstMaster) {
				allCentres.add(masterCenter);
				types.add(ping);
				types.add(pong);
				allTypes.add(ping);
				allTypes.add(pong);
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
			if (!allCentres.get(i).getAdress().equals("8080"))
				updateAllNodes(allCentres.get(i).getAdress());
			refreshRunningAgentsOnAllCentres("8080");
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
		for (AgentType e : s) {
			allTypes.add(e);
		}
		for (int i = 0; i < allCentres.size(); i++) {
			System.err.println(allCentres.size());
			if (!allCentres.get(i).getAdress().equals("8080"))
				updateAllTypes(allCentres.get(i).getAdress());
		}
	}

	@Override
	@POST
	@Path("/updateRunning")
	@Consumes(MediaType.APPLICATION_JSON)
	synchronized public void updateRunning(ArrayList<Agent> s) {
		System.out.println("USPIO:" + s);
		this.runningAgents = s;
	}

	@Override
	@POST
	@Path("/updateTypesAll")
	@Consumes(MediaType.APPLICATION_JSON)
	synchronized public void udpdateTypesAll(ArrayList<AgentType> t) {
		System.out.println("USPIO:" + t);
		this.allTypes = t;
	}

	// Test liste
	@Override
	@GET
	@Path("/all")
	public void bla() {
		System.out.println("Svi centri: " + allCentres.size());
		System.out.println("Svi tipovi: " + allTypes.size());
		for (AgentType tip : allTypes) {
			System.out.println(tip.getName());
		}
		System.out.println("Moji tipovi: " + types.size());
		for (AgentType tip : types) {
			System.out.println(tip.getName());
		}
		System.out.println("Moji trceci: " + runningAgents.size());
		for (Agent agent : runningAgents) {
			System.out.println(agent.getId().getName());
		}

	}

	@Override
	@GET
	@Path("/deleteMe/{adress}")
	public void deleteCent(@PathParam("adress") String adresa) {

		System.out.println("za obrisati" + adresa);

		System.err.println(allCentres.size());

		// Brisanje cvora

		for (int i = 0; i < allCentres.size(); i++) {
			if (allCentres.get(i).getAdress().equals(adresa))
				allCentres.remove(i);
		}

		// Brisanje tipova

		for (int j = 0; j < allTypes.size(); j++) {
			if (allTypes.get(j).getModule().equals(adresa))
				allTypes.remove(j);

		}
		for (int j = 0; j < allTypes.size(); j++) {
			if (allTypes.get(j).getModule().equals(adresa))
				allTypes.remove(j);

		}
		for (int j = 0; j < allTypes.size(); j++) {
			if (allTypes.get(j).getModule().equals(adresa))
				allTypes.remove(j);

		}
		for (int j = 0; j < allTypes.size(); j++) {
			if (allTypes.get(j).getModule().equals(adresa))
				allTypes.remove(j);

		}

		// Brisanje agenata

		for (int j = 0; j < runningAgents.size(); j++) {
			if (runningAgents.get(j).getId().getHost().getAdress().equals(adresa))
				runningAgents.remove(j);

		}
		for (int j = 0; j < runningAgents.size(); j++) {
			if (runningAgents.get(j).getId().getHost().getAdress().equals(adresa))
				runningAgents.remove(j);

		}

		for (int j = 0; j < runningAgents.size(); j++) {
			if (runningAgents.get(j).getId().getHost().getAdress().equals(adresa))
				runningAgents.remove(j);

		}
		for (int j = 0; j < runningAgents.size(); j++) {
			if (runningAgents.get(j).getId().getHost().getAdress().equals(adresa))
				runningAgents.remove(j);

		}

		// Sad update svih

		for (int i = 0; i < allCentres.size(); i++) {
			System.out.println("ulazim u: " + allCentres.get(i).getAdress());
			if (!allCentres.get(i).getAdress().equals(adresa)) {
				try {
					URL url = new URL(
							"http://localhost:" + allCentres.get(i).getAdress() + "/ChatApp/rest/agents/updateCenters");
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

				try {
					URL url = new URL("http://localhost:" + allCentres.get(i).getAdress()
							+ "/ChatApp/rest/agents/updateTypesAll");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setDoOutput(true);
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json");

					String input = new Gson().toJson(allTypes);

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

				try {
					URL url = new URL(
							"http://localhost:" + allCentres.get(i).getAdress() + "/ChatApp/rest/agents/updateRunning");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setDoOutput(true);
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json");

					String input = new Gson().toJson(runningAgents);

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
		}
	}

	// MASTER UPDATE TIPOVA NOVOG
	@Override
	@GET
	@Path("/agents/classes")
	public String registerNewTypeOnMaster() {
		return null;
	}

	// MASTER UPDATE POKRENUTIH NOVOGA
	@Override
	@POST
	@Path("/agents/running")
	public String getRunningAgents() {
		return null;
	}

	// POKRETANJE NOVOG AGENTA

	@Override
	@POST
	@Path("/running/{type}/{name}/{adress}")
	public void newRunningAgent(@PathParam("type") String type, @PathParam("name") String name,
			@PathParam("adress") String adress) {
		System.out.println("Novi trci:" + type + " " + name + " " + adress);
		Agent newAgent = new Agent();
		AgentCentre newAC = null;
		AgentType newType = null;

		for (AgentType t : types) {
			if (t.getName().equals(type) && t.getModule().equals(adress)) {
				newType = t;
			}
		}

		for (AgentCentre ac : allCentres) {
			if (ac.getAdress().equals(adress))
				newAC = ac;
		}

		AID newAgentAid = new AID(name, newAC, newType);
		newAgent.setId(newAgentAid);

		runningAgents.add(newAgent);
		refreshRunningAgentsOnAllCentres(adress); // Salje svima na mrezi dalje
													// aktivne agente
	}

	public void refreshRunningAgentsOnAllCentres(String adress) { // prolazi
																	// kroz sve
																	// centre i
																	// update
																	// listu
																	// aktivnih
																	// agenata
		for (int i = 0; i < allCentres.size(); i++) {
			if (!allCentres.get(i).getAdress().equals(adress)) {
				try {
					URL url = new URL(
							"http://localhost:" + allCentres.get(i).getAdress() + "/ChatApp/rest/agents/updateRunning");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setDoOutput(true);
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json");

					String input = new Gson().toJson(runningAgents);

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
		}
	}

	// nakon dolaska novog svi pozivaju

	public void updateAllNodes(String adress) {
		System.out.println("udjo" + adress);

		try {
			URL url = new URL("http://localhost:" + adress + "/ChatApp/rest/agents/updateCenters");
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

	public void updateAllTypes(String adress) {
		System.out.println("udjo" + adress);

		try {
			URL url = new URL("http://localhost:" + adress + "/ChatApp/rest/agents/updateTypesAll");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");

			String input = new Gson().toJson(allTypes);

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

	// BRISANJE CVORa

	@Override
	@DELETE
	@Path("/node/{alias}")
	synchronized public void deleteCenter(@PathParam("alias") String alias) {
		// brisi i centar, i tipove i trcece agente
		String adress = null;
		for (int i = 0; i < allCentres.size(); i++) {
			if (allCentres.get(i).getAlias().equals(alias)) {
				adress = allCentres.get(i).getAdress();
				allCentres.remove(i);
				break;
			}
		}

		ArrayList<AgentType> pomTypes = new ArrayList<AgentType>();

		for (AgentType agentType : allTypes) {
			pomTypes.add(agentType);
		}

		System.out.println(pomTypes.size());
		System.out.println(allTypes.size());

		if (adress != null) {
			for (int j = 0; j < allTypes.size(); j++) {
				if (allTypes.get(j).getModule().equals(adress))
					allTypes.remove(j);

			}
			for (int j = 0; j < allTypes.size(); j++) {
				if (allTypes.get(j).getModule().equals(adress))
					allTypes.remove(j);

			}
		}

	}

	public void deleteOneCenter(String adress, String alias) {
		try {

			URL url = new URL("http://localhost:" + adress + "/ChatApp/rest/agents/node/" + alias);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("DELETE");
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
	}

	// HeartBeat

//	@Schedule(second = "*", minute = "*/1", hour = "*", persistent = false)
//	public void runTask1() {
//		if (ismaster && allCentres.size() > 1) {
//			for (int i = 1; i < allCentres.size(); i++) {
//				String url = "http://localhost:" + allCentres.get(i).getAdress() + "/ChatApp/rest/agents/nodee";
//				try {
//
//					URL url2 = new URL(url);
//					HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
//					conn.setRequestMethod("GET");
//					conn.setRequestProperty("Accept", "application/json");
//
//					System.out.println("neki shit" + conn.getResponseCode());
//
//					if (conn.getResponseCode() == 200) {
//						BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
//
//						String output;
//						System.out.println("Output from Server .... \n");
//						while ((output = br.readLine()) != null) {
//
//							System.out.println(output);
//							if (output.equals("true")) {
//								System.out.println("if");
//								heartbeat.put(allCentres.get(i).getAdress(), output);
//							} else {
//								System.out.println("els");
//								String before = heartbeat.get(allCentres.get(i).getAdress());
//								if (!before.equals("true"))
//									System.out.println("mrtav");
//							}
//						}
//
//					} else {
//
//						System.out.println("mrtav");
//
//					}
//					conn.disconnect();
//
//				} catch (MalformedURLException e) {
//
//					e.printStackTrace();
//
//				} catch (IOException e) {
//
//					e.printStackTrace();
//
//				}
//
//			}
//		}
//	}

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
