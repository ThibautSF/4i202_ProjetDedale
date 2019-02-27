package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.SayHello;
import eu.su.mas.dedaleEtu.mas.behaviours.treatMessageBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.explorer.ExploMultiBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * ExploreSolo agent. 
 * It explore the map using a DFS algorithm.
 * It stops when all nodes have been visited
 *  
 *  
 * @author hc
 *
 */

public class ExploreMultiAgent extends AbstractDedaleAgent {
	
	private static final long serialVersionUID = -4650860467651727307L;
	private MapRepresentation myMap;
	private HashMap<String,HashSet<String>> myGraph;
	private Queue<ACLMessage> mailBox;
	

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();
		

		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		/************************************************
		 * 
		 * ADD the behaviours of the Dummy Moving Agent
		 * 
		 ************************************************/
		
		//lb.add(new ExploMultiBehaviour(this,this.myMap,this.myGraph));
		//lb.add(new SayHello(this));
		//lb.add(new ReceiveMessageBehaviour(this));
		
		FSMBehaviour fsm = new FSMBehaviour(this) {
			public int onEnd() {
				System.out.println("FSM behaviour terminé");
				myAgent.doDelete();
				return super.onEnd();
			}

		};
		//definiton des etats
		fsm.registerFirstState	(new ExploMultiBehaviour(this,this.myMap,this.myGraph),"A");
		//fsm.registerState(new ReceiveMessageBehaviour(this),"B");
		fsm.registerState(new treatMessageBehaviour(this,mailBox),"C");
		fsm.registerLastState(new SayHello(this),"D");
		
		//definition des transaction
		//fsm.registerDefaultTransition("A","B");
		fsm.registerTransition("A","C",1); // si boite n'est pas vide
		fsm.registerTransition("A","D",2); //si boite est vide 
		fsm.registerTransition("D","A",1); //si y'a personne à ma portée
		fsm.registerTransition("D","C",2); //si y'a quelq'un à ma portée
				
		lb.add(fsm);
	
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		
		
		addBehaviour(new startMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	
	
}
