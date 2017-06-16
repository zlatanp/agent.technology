package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Singleton;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/websocket")
@Singleton
public class WSEndpoint {

	private static final Logger LOG = Logger.getLogger(WSEndpoint.class.getName());
	private static Set<Session> peers = Collections.synchronizedSet(new HashSet<Session>());
	private boolean delete = true;
	private int i = 2;
	private HttpURLConnection conn;
	
	public WSEndpoint() {
	}

	@OnMessage
	public String onMessage(Session session, String message) {
		System.out.println(message);

		if (message.equals("c00d3")){
			for (Session session2 : peers) {
				// if(!session2.getId().equals(session.getId())){
				RemoteEndpoint.Basic other = session2.getBasicRemote();
				try {
					other.sendText(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
				// }
			}
		}
		
		if (message.equals("c00d4")){
			for (Session s : peers) {
				// if(!session2.getId().equals(session.getId())){
				RemoteEndpoint.Basic other = s.getBasicRemote();
				try {
					other.sendText(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
				// }
			}
		}
		
		if (message.equals("c00d5")){
			for (Session s : peers) {
				// if(!session2.getId().equals(session.getId())){
				RemoteEndpoint.Basic other = s.getBasicRemote();
				try {
					other.sendText(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
				// }
			}
		}

		return "";
	}

	@OnOpen
	public void onOpen(Session peer) {
		System.out.println("OPEN");
		LOG.info("Connection opened ...");
		peers.add(peer);
	}

	@OnClose
	public void onClose(Session peer) {
		System.out.println("CLOSE");
		LOG.info("Connection closed ...");

//		if (delete && i>0) {
//			try {
//
//				URL url1 = new URL("http://localhost:8080/ChatApp/rest/agents/deleteMe/8100");
//				URL url2 = new URL("http://localhost:8080/ChatApp/rest/agents/deleteMe/8090");
//				
//				if(i==2){
//					conn = (HttpURLConnection) url1.openConnection();
//					i--;
//				}else{
//					conn = (HttpURLConnection) url2.openConnection();
//					i--;
//				}
//				
//				conn.setRequestMethod("GET");
//				conn.setRequestProperty("Accept", "application/json");
//
//				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
//
//				String output;
//				System.out.println("Output from Server .... \n");
//				while ((output = br.readLine()) != null) {
//					System.out.println(output);
//
//				}
//
//				conn.disconnect();
//
//			} catch (MalformedURLException e) {
//
//				e.printStackTrace();
//
//			} catch (IOException e) {
//
//				e.printStackTrace();
//
//			}
//
//			peers.remove(peer);
//
//			for (Session session2 : peers) {
//				// if(!session2.getId().equals(session.getId())){
//				RemoteEndpoint.Basic other = session2.getBasicRemote();
//				try {
//					other.sendText("c00d3");
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//
//			}
//
//		}
	}

}