package model;

import java.util.ArrayList;
import java.util.HashMap;

public class ACLMessage {
	
	private Enum performative;
	private String sender;
	private ArrayList<AID> receivers;
	private AID replyTo;
	private String content;
	private Object contentObj;
	private HashMap<String, Object> userArgs;
	private String Language;
	private String Encoding;
	private String Ontology;
	private String Protocol;
	private String ConversationId;
	private String replyWith;
	private String inReplyTo;
	private Long replyBy;
	
	public ACLMessage() {
		super();
	}

	public ACLMessage(String sender, String content) {
		this.content = content;
		this.sender = sender;
	}

	public Enum getPerformative() {
		return performative;
	}

	public void setPerformative(Enum performative) {
		this.performative = performative;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public ArrayList<AID> getReceivers() {
		return receivers;
	}

	public void setReceivers(ArrayList<AID> receivers) {
		this.receivers = receivers;
	}

	public AID getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(AID replyTo) {
		this.replyTo = replyTo;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Object getContentObj() {
		return contentObj;
	}

	public void setContentObj(Object contentObj) {
		this.contentObj = contentObj;
	}

	public HashMap<String, Object> getUserArgs() {
		return userArgs;
	}

	public void setUserArgs(HashMap<String, Object> userArgs) {
		this.userArgs = userArgs;
	}

	public String getLanguage() {
		return Language;
	}

	public void setLanguage(String language) {
		Language = language;
	}

	public String getEncoding() {
		return Encoding;
	}

	public void setEncoding(String encoding) {
		Encoding = encoding;
	}

	public String getOntology() {
		return Ontology;
	}

	public void setOntology(String ontology) {
		Ontology = ontology;
	}

	public String getProtocol() {
		return Protocol;
	}

	public void setProtocol(String protocol) {
		Protocol = protocol;
	}

	public String getConversationId() {
		return ConversationId;
	}

	public void setConversationId(String conversationId) {
		ConversationId = conversationId;
	}

	public String getReplyWith() {
		return replyWith;
	}

	public void setReplyWith(String replyWith) {
		this.replyWith = replyWith;
	}

	public String getInReplyTo() {
		return inReplyTo;
	}

	public void setInReplyTo(String inReplyTo) {
		this.inReplyTo = inReplyTo;
	}

	public Long getReplyBy() {
		return replyBy;
	}

	public void setReplyBy(Long replyBy) {
		this.replyBy = replyBy;
	}
	
}
