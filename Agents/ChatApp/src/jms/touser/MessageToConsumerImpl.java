package jms.touser;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import jms.UserMessage;
import model.User;

@Stateless
@Local(MessageToConsumer.class)
public class MessageToConsumerImpl implements MessageToConsumer {



	@Resource(mappedName = "java:/ConnectionFactory")
	private ConnectionFactory factory;

	@Resource(mappedName = "java:/jms/queue/chatQueue")
	private Queue userQueue;

	private Connection connection;
	private QueueSender sender;
	private QueueSession session;

	@Override
	public void sendMessageToAgent(String username, String password, String adresa) {
		try {
			initialise();
		} catch (NamingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {

			TextMessage msg = session.createTextMessage(username + "=" + password + "=" + adresa);
			sender.send(msg);

			destroy();
		} catch (JMSException e) {
		}

	}

	@Override
	public void loginMessage(String username, String password) {
		try {
			initialise();
		} catch (NamingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {

			TextMessage msg = session.createTextMessage("login=" + username + "=" + password);
			sender.send(msg);

			destroy();
		} catch (JMSException e) {
		}

	}

	@Override
	public void logoutMessage(String username) {
		try {
			initialise();
		} catch (NamingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {

			TextMessage msg = session.createTextMessage("logout=" + username + "=null");
			sender.send(msg);

			destroy();
		} catch (JMSException e) {
		}
	}

	@Override
	public void getRegisteredUsers() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getActiveUsers() {
		// TODO Auto-generated method stub

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
