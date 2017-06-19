package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import javax.ejb.Singleton;
import javax.ejb.Startup;
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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import exceptions.AliasExistsException;
import jms.touser.MessageToConsumerImpl;
import model.Host;
import model.Message;
import model.User;

@Startup
@Singleton
@Path("/userChatController")
public class UserChatControllerImpl implements UserChatController {

	@Context
	private UriInfo uriInfo;
	@Context
	ServletConfig config;
	private String MyAdress;
	private Host masterHost;
	private ArrayList<Host> MasterCvorovi = new ArrayList<Host>();
	//private ArrayList<Host> mojiCvorovi = new ArrayList<Host>();
	private ArrayList<User> allUsers = new ArrayList<User>();
	private boolean masterAdded = false;
	private boolean slaveAdded = false;
	private String alias = randomIdentifier();
	
	
	public ArrayList<User> onlineUsers =  new ArrayList<User>();

	public UserChatControllerImpl() {
	}

	@Override
	@GET
	@Path("/init")
	public String initialise() {
		masterHost = new Host();
		masterHost.setAdress("http://localhost:8080/ChatApp/rest/");
		masterHost.setAlias("MasterChat");

		MyAdress = uriInfo.getBaseUri().toString();
		String[] pom = MyAdress.split(":");
		String[] pom2 = pom[2].split("/");
		String myHost = pom2[0];
		
		for(int j=0;j<MasterCvorovi.size();j++){
			if(MasterCvorovi.get(j).getAlias().equals(alias))
				slaveAdded = true;
		}

		if (!(MyAdress.equals("http://localhost:8080/ChatApp/rest/")) && !slaveAdded) {
			//System.out.println("Call Master for registration");
			String url = "http://localhost:8080/ChatApp/rest/userChatController/registerNewChat/" + myHost + "/" + alias;

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
					MasterCvorovi= (ArrayList<Host>) fromJson(output,new TypeToken<ArrayList<Host>>() {}.getType());
					
				}

				conn.disconnect();

			} catch (MalformedURLException e) {

				e.printStackTrace();

			} catch (IOException e) {

				e.printStackTrace();

			}
			//2. ako sam slave gadjam 8080/chatapp koji gadja userapp i dobijam sve
			
			try {

				URL url2 = new URL("http://localhost:8080/ChatApp/rest/userChatController/getAllUsers");
				HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept", "application/json");

				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

				String output;
				System.out.println("Output from Server .... \n");
				while ((output = br.readLine()) != null) {
					System.out.println(output);
					allUsers= (ArrayList<User>) fromJsonUsers(output,new TypeToken<ArrayList<User>>() {}.getType());
					
				}

				conn.disconnect();

			} catch (MalformedURLException e) {

				e.printStackTrace();

			} catch (IOException e) {

				e.printStackTrace();

			}
			
			//System.out.println(MasterCvorovi.size());
		} else {
			if(!masterAdded && !slaveAdded){
				System.out.println("I am master");
				MasterCvorovi.add(masterHost);
				masterAdded = true;
				
				//1. ako sam master gadjam 8080/userapp/getall i stavljam u listu
				allUsers = getAllUsers();
				System.out.println(allUsers.size());
			}
		}
		return MyAdress;
	}

	//Zatim šalje dodatni zahtev getAllUsers koji obrađuje User app master čvora i beleži listu korisnika kod sebe.
	@Override
	@GET
	@Path("/getAllUsers")
	public ArrayList<User> getAllUsers(){
		ArrayList<User> allUsers = new ArrayList<User>();
		
		try {

			URL url = new URL("http://localhost:8080/UserApp/rest/userController/allUsers");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
				allUsers= (ArrayList<User>) fromJsonUsers(output,new TypeToken<ArrayList<User>>() {}.getType());
				
			}

			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
		
		return allUsers;
	}
	
	@Override
	@GET
	@Path("/registerNewChat/{adress}/{alias}")
	public String register(@PathParam("adress") String address, @PathParam("alias") String alias)
			throws AliasExistsException {
		Host newOne = new Host();
		String realAdress = "http://localhost:" + address + "/ChatApp/rest/";
		newOne.setAdress(realAdress);
		newOne.setAlias(alias);
		MasterCvorovi.add(newOne);
		//System.err.println(MyAdress + "dodao sam u listu hostova, a velicina je:  " + MasterCvorovi.size());
		
		//Treba da prodje kroz masterCvorove i da svima do poslednjeg posalje novu update-ovanu listu masterCvorova
		for(int i=1;i<MasterCvorovi.size()-1;i++) //Nulti je master cvor, a poslednjem listu vraca return, znaci svima izmedju treba update liste
			updateAllNodes(MasterCvorovi.get(i).getAdress());
		
		return  new Gson().toJson(MasterCvorovi);
	}
	
	
	private void updateAllNodes(String adress){
		try {

			URL url = new URL(adress + "userChatController/updateCvorove");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");

			String input = new Gson().toJson(MasterCvorovi);

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
	
	@Override
	@POST
	@Path("/updateCvorove")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateCvorove(ArrayList<Host> u){
		MasterCvorovi = u;
	}

	@Override
	@GET
	@Path("/poz")
	public String unregister() {
		System.out.println("SVI MOJI CVOROVI: ");
		for (int i = 0; i < MasterCvorovi.size(); i++)
			System.out.println(MasterCvorovi.get(i).getAlias());
		System.out.println(" ");
//		System.out.println("SVI MOJI USERI: ");
//		for (int i = 0; i < allUsers.size(); i++)
//			System.out.println(allUsers.get(i).getUsername());
		
		System.out.println("ULOGOVANI USERI NA MOJ KANAL CHAT:");
		System.err.println(onlineUsers.size());
		for(int i=0; i<onlineUsers.size(); i++){
			System.out.println(onlineUsers.get(i).getUsername());
			System.out.println(onlineUsers.get(i).getPassword());
		}
		
		return "cao";

	}
	
	//2. tacka
	@Override
	@POST
	@Path("/registerUser/{username}/{password}/{passwordRepeat}")
	public String registerUser(@PathParam("username") String username, @PathParam("password") String password, @PathParam("passwordRepeat") String passwordRepeat){
		//System.err.println(username + " " + password);
		if(username.isEmpty() || password.isEmpty() || passwordRepeat.isEmpty() || username.equals(null) || password.equals(null) || password.equals("null") || username.equals("null")){
			return "null";
		}else if(!password.equals(passwordRepeat)){
			return "null";
		}else{
			if(!MyAdress.contains("8080")){ //ako nisu na istom cvoru
			try {
				//System.out.println(username + " " + password);
				URL url = new URL("http://localhost:8080/UserApp/rest/userController/register/" + username + "/" + password);
				
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
			return "REST REGISTER Done";
			}else{
				//jms queue jer su na istom portu...
				
				
				MessageToConsumerImpl m = new MessageToConsumerImpl();
				//System.out.println("saljem");
				//m.registerMessage(username, password);
				
				return "JMS REGISTER Done";
			}
		}
	}
	
	@POST
	@Path("/loginUser/{username}/{password}")
	@Override
	public String login(@PathParam("username") String username, @PathParam("password") String password){
		boolean ok = false;
		if(username.isEmpty() || password.isEmpty() || username.equals(null) || password.equals(null)){
			return "null";
		}
		
		if(!MyAdress.contains("8080")){ //ako nisu na istom cvoru
			try {
				//System.out.println(username + " " + password);
				URL url = new URL("http://localhost:8080/UserApp/rest/userController/login/" + username + "/" + password);
				
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setRequestProperty("Accept", "application/json");

					BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

					String output;
					System.out.println("Output from Server .... \n");
					while ((output = br.readLine()) != null) {
						//System.err.println("sad ce outpuuuuuuuuut");
						System.out.println(output);
						//System.err.println("sad ce outpuuuuuuuuut");
						ok= (boolean) fromlogin(output,new TypeToken<Boolean>() {}.getType());
						
					}

					conn.disconnect();

			  } catch (MalformedURLException e) {

				e.printStackTrace();

			  } catch (IOException e) {

				e.printStackTrace();

			 }
			if(ok){
			return "REST LOGIN Done";
			}else{
				return "no";
			}
			}else{
				//jms queue jer su na istom portu...
				String s = config.getServletContext().getRealPath("");
				String databasePath = s.substring(0, 42);
				ArrayList<User> allUsers = getAllUsersFromFile(databasePath);
				for (int i = 0; i < allUsers.size(); i++) {
					if ((allUsers.get(i).getUsername()).equals(username) && (allUsers.get(i).getPassword()).equals(password)) {
						ok = true;
						MessageToConsumerImpl m = new MessageToConsumerImpl();
						//System.out.println("saljem");
						m.loginMessage(username, password);
					}
				
				}
				
				if(ok){
				return "JMS LOGIN Done";
				}else{
					return "no";
				}
				}
		}
	
	@GET
	@Path("/logoutUser/{username}")
	@Override
	public String logout(@PathParam("username") String username){
		//System.err.println(username);
		if(username.isEmpty() || username.equals(null)){
			return "null";
		}
		
		if(!MyAdress.contains("8080")){ //ako nisu na istom cvoru
			try {
				//System.out.println(username);
				URL url = new URL("http://localhost:8080/UserApp/rest/userController/logout/" + username);
				
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
			return "REST LOGOUT Done";
			}else{
				//jms queue jer su na istom portu...
				
				
				MessageToConsumerImpl m = new MessageToConsumerImpl();
				//System.out.println("saljem");
				m.logoutMessage(username);
				
				return "JMS LOGOUT Done";
			}
		}

	@Override
	@GET
	@Path("/addUser/{username}/{password}")
	public void addUser(@PathParam("username") String username, @PathParam("password") String password) {
		User u = new User();
		u.setUsername(username);
		u.setPassword(password);
		
		onlineUsers.add(u);
		//System.out.println(MasterCvorovi.size());
		if(MasterCvorovi.size()>1){
			for(int i=1;i<MasterCvorovi.size(); i++){
				//System.out.println(MasterCvorovi.get(i).getAdress() );
				try {
					//System.out.println(username + " " + password);
					URL url = new URL(
							MasterCvorovi.get(i).getAdress() + "userChatController/addUserOnAnother/" + username + "/" + password);

					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
			}
			
		}
		
		
	}
	
	@Override
	@GET
	@Path("/addUserOnAnother/{username}/{password}")
	public void addUserOnAnother(@PathParam("username") String username, @PathParam("password") String password) {
		//System.out.println("ANODEEEER AADDDD");
		User u = new User();
		u.setUsername(username);
		u.setPassword(password);
		
		onlineUsers.add(u);
		
		
	}

	@Override
	@GET
	@Path("/removeUser/{username}")
	public void removeUser(@PathParam("username") String username) {
		for(int i=0;i<onlineUsers.size();i++){
			if(onlineUsers.get(i).getUsername().equals(username)){
				onlineUsers.remove(i);
				break;
			}
		}
		//System.out.println(MasterCvorovi.size());
		if(MasterCvorovi.size()>1){
			for(int i=1;i<MasterCvorovi.size(); i++){
				//System.out.println(MasterCvorovi.get(i).getAdress() );
				try {
					//System.out.println(username);
					URL url = new URL(
							MasterCvorovi.get(i).getAdress() + "userChatController/removeOnAnother/" + username);

					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
			}
			
		}
		

	}
	
	@Override
	@GET
	@Path("/removeOnAnother/{username}")
	public void removeOnAnother(@PathParam("username") String username) {
		//System.out.println("ANODEEEER DELETEEE");
		for(int i=0;i<onlineUsers.size();i++){
			if(onlineUsers.get(i).getUsername().equals(username)){
				onlineUsers.remove(i);
				break;
			}
		}
		
		
	}


	@Override
	public void publish(Message message) {
		// TODO Auto-generated method stub

	}
	
	//Generate random Alias for non-master chat app
	
	final static String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345674890";

	final static java.util.Random rand = new java.util.Random();

	// consider using a Map<String,Boolean> to say whether the identifier is being used or not 
	final static Set<String> identifiers = new HashSet<String>();

	public static String randomIdentifier() {
	    StringBuilder builder = new StringBuilder();
	    while(builder.toString().length() == 0) {
	        int length = rand.nextInt(5)+5;
	        for(int i = 0; i < length; i++) {
	            builder.append(lexicon.charAt(rand.nextInt(lexicon.length())));
	        }
	        if(identifiers.contains(builder.toString())) {
	            builder = new StringBuilder();
	        }
	    }
	    return builder.toString();
	}
	
	private ArrayList<Host> fromJson(String output, Type type) {
		return new Gson().fromJson(output, type);
	}
	
	private ArrayList<User> fromJsonUsers(String output, Type type) {
		return new Gson().fromJson(output, type);
	}
	
	private boolean fromlogin(String output, Type type){
		return new Gson().fromJson(output, type);
	}
	
	@Override
	@GET
	@Path("/on")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<User> getOnlineUsers() {
		return onlineUsers;
	}

	public void setOnlineUsers(ArrayList<User> onlineUsers) {
		this.onlineUsers = onlineUsers;
	}

	// JSON read/write
		public static synchronized ArrayList<User> getAllUsersFromFile(String path) {
			ArrayList<User> allUsers = new ArrayList<User>();
			String generatedPath = FilePaths.getPath(path).getPath();
			String FileName = generatedPath + "//RegisteredUsers.txt";

			ArrayList<JSONObject> jsons = new ArrayList<JSONObject>();
			try {
				jsons = ReadJSON(new File(FileName), "UTF-8");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}

			for (int i = 0; i < jsons.size(); i++) {
				String iusername = (String) jsons.get(i).get("username");
				String ipassword = (String) jsons.get(i).get("password");

				User u = new User();
				u.setUsername(iusername);
				u.setPassword(ipassword);
				allUsers.add(u);
			}
			return allUsers;
		}

		@SuppressWarnings("resource")
		public static synchronized ArrayList<JSONObject> ReadJSON(File MyFile, String Encoding)
				throws FileNotFoundException, ParseException {
			Scanner scn = new Scanner(MyFile, Encoding);
			ArrayList<JSONObject> json = new ArrayList<JSONObject>();
			// Reading and Parsing Strings to Json
			while (scn.hasNext()) {
				JSONObject obj = (JSONObject) new JSONParser().parse(scn.nextLine());
				json.add(obj);
			}

			return json;
		}

	
}
