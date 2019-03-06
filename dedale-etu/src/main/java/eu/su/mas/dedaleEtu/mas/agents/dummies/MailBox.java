package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.LinkedList;
import java.util.Queue;

import jade.lang.acl.ACLMessage;

public class MailBox {
	
	
	private Queue<ACLMessage> mails;
	private Queue<ACLMessage> mailsInterblocage;

	
	
	public MailBox(Queue<ACLMessage> mails) {
		this.mails=mails;
	}
	
	public MailBox() {
		this(new LinkedList<ACLMessage>());
	}
	
	public boolean hasMessage() {
		return hasMessage(false);
	}
	
	public boolean hasMessage(boolean isInterblocage) {
		
		if(isInterblocage) {
			return !mailsInterblocage.isEmpty();
		}
		return !mails.isEmpty();
	}
	public ACLMessage getFirstMessage() {
		return getFirstMessage(false);
	}
	
	public ACLMessage getFirstMessage(boolean isInterblocage) {
		if(isInterblocage) {
			return mailsInterblocage.poll();
		}
		return mails.poll();
	}
	
	public int nbWaiting() {
		
		return nbWaiting(false);
	}
	
	
	public int nbWaiting(boolean isInterblocage) {
		
		if(isInterblocage) {
			return mailsInterblocage.size();
		}
		return mails.size();
	}
	
	public void addMsg(ACLMessage msg) {
		addMsg(msg,false);
	}
	
	public void addMsg(ACLMessage msg,boolean isInterblocage) {
		
		if(isInterblocage) {
			 this.mailsInterblocage.add(msg);
		}
		this.mails.add(msg);
	}
	
	
	



}
