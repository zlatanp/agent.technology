package jms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import controller.UserChatControllerImpl;
import model.Agent;
import model.User;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/jms/queue/chatQueue") })
public class MDBConsumer implements MessageListener {

	private ArrayList<Agent> runningAgents = new ArrayList<Agent>();

	@Override
	public void onMessage(Message msg) {
		// TODO Auto-generated method stub
		System.out.println("Chat primljeno");
		String name;
		String message;
		String adresa;
		String type = null;
		String res = "";
		TextMessage tmsg = (TextMessage) msg;
		try {
			String text = tmsg.getText();
			System.out.println(text);
			String pom[] = text.split("=");
			name = pom[1];
			message = pom[0];
			adresa = pom[2];

			// Daj mi sve pokrenute
			try {

				URL url2 = new URL("http://localhost:8080/ChatApp/rest/agents/dajSveRunning");
				HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept", "application/json");

				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

				String output;
				System.out.println("Output from Server .... \n");
				while ((output = br.readLine()) != null) {
					System.out.println(output);
					runningAgents = (ArrayList<Agent>) fromJson(output, new TypeToken<ArrayList<Agent>>() {
					}.getType());

				}

				conn.disconnect();

			} catch (MalformedURLException e) {

				e.printStackTrace();

			} catch (IOException e) {

				e.printStackTrace();

			}

			System.out.println("velicina svih runningovanih: " + runningAgents.size());

			for (Agent agent : runningAgents) {
				if (agent.getId().getName().equals(name))
					type = agent.getId().getType().getName();
			}

			if (type.equals("Pong"))
				res = name + ": Pong\n";

			if (type.equals("Ping")) {
				boolean pong = false;
				String pongname = "";
				for (Agent agent : runningAgents) {
					if (agent.getId().getType().getName().equals("Pong")) {
						pong = true;
						pongname = agent.getId().getName();
					}
				}

				if (pong) {
					res = pongname + ": " + message + "\n";
				} else {
					res = name + ": Ping\n";
				}
			}
			
			if(type.equals("MapReduce")){
				Map<Character, Integer> freqList = new LinkedHashMap<Character, Integer>();
				char[] charArray = message.toCharArray();
				for(char key : charArray) {
		            if(freqList.containsKey(key)) {
		               freqList.put(key, freqList.get(key) + 1);
		            } else
		                freqList.put(key, 1);
		        }
				res = name + ":\n";
				for (Map.Entry<Character, Integer> entry : freqList.entrySet())
				{
				    System.out.println(entry.getKey() + ":" + entry.getValue());
				    res += entry.getKey() + ":" + entry.getValue() + "\n";
				}
			}
			
			try {

				URL url2 = new URL("http://localhost:" + adresa + "/ChatApp/rest/agents/receive/");
				HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");

				String input = new Gson().toJson(res);

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
			
			

		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Treba da nadjem agenta sa tim imenom i da mu prosledim tekst a onda
		// on nesto dalje da radi

	}

	private ArrayList<Agent> fromJson(String output, Type type) {
		return new Gson().fromJson(output, type);
	}

}
