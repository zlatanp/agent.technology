package controller;

import java.io.IOException;
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

	public WSEndpoint() {
	}

	@OnMessage
	public String onMessage(Session session, String message) {
		
		publish(message);
		
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
		peers.remove(peer);
	}
	
	public void publish(String message){
		if (message != null) {

			//Slanje poruka svima osim sebi
			for (Session s : peers) {
				RemoteEndpoint.Basic other = s.getBasicRemote();
				try {
					other.sendText(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		
		}
	}
}