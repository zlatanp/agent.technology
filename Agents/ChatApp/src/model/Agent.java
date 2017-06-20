package model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.JMSSessionMode;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


import org.jboss.resteasy.annotations.providers.jaxb.IgnoreMediaTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
@Stateless
@Local(Agent.class)
public class Agent {
	
	@Resource(mappedName = "java:/ConnectionFactory")
	private ConnectionFactory factory;

	@Resource(mappedName = "java:/jms/queue/chatQueue")
	private Queue userQueue;

	private Connection connection;
	private QueueSender sender;
	private QueueSession session;

	private AID id;
	
	private String res = "";

	public Agent() {
		super();
	}
	
	public Agent(AID id) {
		super();
		this.id = id;
	}

	public AID getId() {
		return id;
	}

	public void setId(AID id) {
		this.id = id;
	}
	
	public void receiveMessage(String name, String message, String adress, ArrayList<Agent> runningAgents){
		System.out.println(name + message + adress);
		System.out.println(id.getType().getName());
		
		if (id.getType().getName().equals("Pong"))
			res = name + ": Pong,";

		if (id.getType().getName().equals("Ping")) {
			boolean pong = false;
			
			Agent a = null;
			
			for (Agent agent : runningAgents) {
				if (agent.getId().getType().getName().equals("Pong")) {
					pong = true;
					a = agent;
				}
			}

			if (pong) {
				a.sendPongMessage(message, adress);
			} else {
				res = name + ": Ping,";
			}
		}
		
		if(id.getType().getName().equals("MapReduce")){
			Map<Character, Integer> freqList = new LinkedHashMap<Character, Integer>();
			char[] charArray = message.toCharArray();
			for(char key : charArray) {
	            if(freqList.containsKey(key)) {
	               freqList.put(key, freqList.get(key) + 1);
	            } else
	                freqList.put(key, 1);
	        }
			res = name + ":";
			for (Map.Entry<Character, Integer> entry : freqList.entrySet())
			{
			    System.out.println(entry.getKey() + ":" + entry.getValue());
			    res += entry.getKey() + ":" + entry.getValue() + "; ";
			}
			res += ",";
		}
		
		sendMessage(res, adress);
	}
	
	public void sendPongMessage(String message, String adress){
		res = getId().getName() + ": " + message + ",";
		sendMessage(res, adress);
	}
	
	public void sendMessage(String res, String adress){
		try {
			initialise();
		} catch (NamingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			System.out.println("na agentu izlaz je:" + res);
			TextMessage msg = session.createTextMessage("%" + res + "%"+ adress);
			sender.send(msg);

			destroy();
		} catch (JMSException e) {
		}
	}
	
	public void initialise() throws NamingException {
		try {
			Context context = new InitialContext();
			this.factory = (ConnectionFactory) context.lookup("java:/ConnectionFactory");
			this.connection = factory.createConnection();
			connection.start();
			this.session = (QueueSession) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			userQueue = (Queue) context.lookup("java:/jms/queue/chatQueue");

			this.sender = session.createSender(userQueue);
		} catch (JMSException e) {
			return;
		}

	}

	public void destroy() {
		try {
			connection.stop();
			sender.close();
		} catch (JMSException e) {
		}
	}
	
}
