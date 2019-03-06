package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.LinkedList;
import java.util.Queue;

import eu.su.mas.dedaleEtu.mas.agents.dummies.MailBox;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This behaviour is a one Shot.
 * It receives a message tagged with an inform performative, print the content in the console and destroy itlself
 * 
 * @author Cédric Herpson
 *
 */
public class ReceiveMessageBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = 9088209402507795289L;

	private MailBox mailBox;
	//private boolean finished = false;

	/**
	 * 
	 * This behaviour is a one Shot.
	 * It receives a message tagged with an inform performative, print the content in the console and destroy itlself
	 * @param myagent
	 */
	public ReceiveMessageBehaviour(final Agent myagent,MailBox mailBox) {
		super(myagent);
		System.out.println("jy suis");
		this.mailBox =mailBox;
	}


	public void action() {
		//1) receive the message
		
		final MessageTemplate msgIn= MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		final MessageTemplate msgPr = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
		final MessageTemplate msgT= MessageTemplate.and(msgIn,msgPr);
		


		final ACLMessage msg = this.myAgent.receive(msgT);

		if (msg != null) {
			System.out.println("jy suis");
			this.mailBox.addMsg(msg);
			
			//System.out.println(this.myAgent.getLocalName()+"<----Result received from "+msg.getSender().getLocalName()+" ,content= "+msg.getContent());
			/* 
			 * TODO Se fait dans le comportement général
			 * 
			switch (msg.getProtocol()) {
			case "PositionSending":
				this.position = msg.getContent();
				break;
			
			case "MapSending":
				try {
					msg.getContentObject();
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
				break;

			default:
				break;
			}
			*/
			
			//this.finished=true;
		}
		
		block(); // the behaviour goes to sleep until the arrival of a new message in the agent's Inbox.
	}
	

	/*
	public boolean done() {
		return finished;
	}
	*/
	
}