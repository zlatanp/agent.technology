package controller;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
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
	private static ArrayList<Session> peers = new ArrayList<Session>();
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

		for (int i = 0; i < peers.size(); i++) {
			if(peers.get(i).getId().equals(peer.getId())){
				peers.remove(i);
				break;
			}
		}
		
	}

}