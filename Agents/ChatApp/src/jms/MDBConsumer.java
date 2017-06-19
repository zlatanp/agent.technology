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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import model.Agent;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/jms/queue/chatQueue") })
public class MDBConsumer implements MessageListener {

	private ArrayList<Agent> runningAgents = new ArrayList<Agent>();

	@Override
	public void onMessage(Message msg) {
		// TODO Auto-generated method stub
		System.out.println("Chat primljeno");

		TextMessage tmsg = (TextMessage) msg;
		String text;

		try {
			text = tmsg.getText();
			
			if(text.startsWith("%")){
				System.out.println(text);
				String pom[] = text.split("%");
				
				sendAnswer(pom[1], pom[2]);
			}else{

			Agent A = null;

			String name;
			String message;
			String adresa;
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

			for (Agent agent : runningAgents) {
				if (agent.getId().getName().equals(name))
					A = agent;
			}

			A.receiveMessage(name, message, adresa, runningAgents);

			// Treba da nadjem agenta sa tim imenom i da mu prosledim tekst a
			// onda
			// on nesto dalje da radi
			}
		} catch (

		JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ArrayList<Agent> fromJson(String output, Type type) {
		return new Gson().fromJson(output, type);
	}

	public void sendAnswer(String res, String adresa) {
		try {

			URL url2 = new URL("http://localhost:" + adresa + "/ChatApp/rest/agents/receive/");
			HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");

			String input = new Gson().toJson(res);

			// System.out.println("INPUT JE: " + input);

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
