package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.HashMap;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * This example behaviour try to send a hello message (every 3s maximum) to agents Collect2 Collect1
 * @author hc
 *
 */
public class CommunicationBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = -2058134622078521998L;
	
	private boolean finished=false;
	private HashMap<String,String[]> graph;
	private String targetAgentName;

	/**
	 * An agent tries to contact its friend and to give him its current position
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	public CommunicationBehaviour (final Agent myagent, HashMap<String,String[]> graph, String targetAgentName) {
		super(myagent);
		this.graph = graph;
		this.targetAgentName = targetAgentName;
	}

	@Override
	public void action() {
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		//A message is defined by : a performative, a sender, a set of receivers, (a protocol),(a content (and/or contentOBject))
		ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.myAgent.getAID());
		msg.setProtocol("MapSending");
		
		if (myPosition!=""){
			//System.out.println("Agent "+this.myAgent.getLocalName()+ " is trying to reach its friends");
			try {
				//TODO serialize Map
				msg.setContentObject(graph);
				
				msg.addReceiver(new AID(this.targetAgentName,AID.ISLOCALNAME));
				
				//Mandatory to use this method (it takes into account the environment to decide if someone is reachable or not)
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				this.finished = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean done() {
		return finished;
	}
	
}