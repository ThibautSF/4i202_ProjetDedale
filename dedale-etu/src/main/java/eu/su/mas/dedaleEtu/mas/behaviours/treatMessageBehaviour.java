package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Queue;

import eu.su.mas.dedaleEtu.mas.agents.dummies.MailBox;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class treatMessageBehaviour extends SimpleBehaviour{
	
	public MailBox mailBox;
	public boolean finished=false;
	
	public treatMessageBehaviour(final Agent myAgent,MailBox mailBox) {
		super(myAgent);
		this.mailBox=mailBox;
		
	}

	@Override
	public void action() {
		// TODO Stub de la méthode généré automatiquement
		//2.5) getMessage → answer messages in mailbox
		System.out.println(this.myAgent.getLocalName()+" : I have "+this.mailBox.nbWaiting()+" messages in mailbox");
		while(this.mailBox.hasMessage()) {
			ACLMessage message = this.mailBox.getFirstMessage();
			if(message!=null) {
				switch (message.getProtocol()) {
				case "PositionSending":
					/*
					 * TODO
					 * 1) get message sender
					 * 2) check if robot in team and robot type
					 * 3) send message to sender with map
					 */
					message.getContent();
					message.getSender();
					
					System.out.println(this.myAgent.getLocalName()+" : The message ("+message.getPostTimeStamp()+") from "+message.getSender()+" is \""+message.getContent()+"\"");
					
					break;
				
				case "MapSending":
					/*
					 * TODO
					 * 1) get HashMap object
					 * For all node in the map
					 *   2) update this.myGraph
					 *   3) update this.myMap
					 *   4) remove from openNodes and add to closedNodes
					 */
					try {
						message.getContentObject();
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
					
					
					break;

				default:
					break;
				}
				
				//System.out.println(this.myAgent.getLocalName()+" -- pos received: "+msg);
				
				//this.closedNodes.add(msg);
				//this.openNodes.remove(msg);
				
				//this.messageReceiver = new ReceiveMessageBehaviour(this.myAgent);
				//this.myAgent.addBehaviour(this.messageReceiver);
			}
			if(!this.mailBox.hasMessage()) {
				this.finished=true;
			}
			
		}

		
	}

	@Override
	public boolean done() {
		// TODO Stub de la méthode généré automatiquement
		return this.finished;
	}

}
