package jms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import controller.UserChatControllerImpl;
import model.User;

//@MessageDriven(activationConfig = {
//		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
//		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/jms/queue/chatQueue") })
public class ChatJMS implements MessageListener {

	@Override
	public void onMessage(Message arg0) {
		// TODO Auto-generated method stub
		
	}
//
//	
//	@Override
//	public void onMessage(Message msg) {
//		// TODO Auto-generated method stub
//		System.out.println("Chat primljeno");
//		// treba da novog ulogovanog korisnika prosirim dalje
//
//		
//		TextMessage tmsg = (TextMessage) msg;
//		String text;
//		try {
//			text = tmsg.getText();
//			System.out.println(text);
//			String[] data = text.split("=");
//			String method = data[0];
//			String username = data[1];
//			String password = data[2];
//
//			if (method.equals("add")) {
//				try {
//					System.out.println(username + " " + password);
//					URL url = new URL(
//							"http://localhost:8080/ChatApp/rest/userChatController/addUser/" + username + "/" + password);
//
//					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//					conn.setRequestMethod("GET");
//					conn.setRequestProperty("Accept", "application/json");
//
//					BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
//
//					String output;
//					System.out.println("Output from Server .... \n");
//					while ((output = br.readLine()) != null) {
//						System.out.println(output);
//					}
//					conn.disconnect();
//				} catch (MalformedURLException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			
//			}
//
//			if (method.equals("delete")) {
//				try {
//					System.out.println(username + " " + password);
//					URL url = new URL(
//							"http://localhost:8080/ChatApp/rest/userChatController/removeUser/" + username);
//
//					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//					conn.setRequestMethod("GET");
//					conn.setRequestProperty("Accept", "application/json");
//
//					BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
//
//					String output;
//					System.out.println("Output from Server .... \n");
//					while ((output = br.readLine()) != null) {
//						System.out.println(output);
//					}
//					conn.disconnect();
//				} catch (MalformedURLException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		} catch (JMSException e) {
//			e.printStackTrace();
//		}
//	}

}
