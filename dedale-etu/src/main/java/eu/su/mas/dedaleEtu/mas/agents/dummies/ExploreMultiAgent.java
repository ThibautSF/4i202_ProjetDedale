package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploMultiBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SayHello;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;

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
	private Map<String,String[]> myGraph;
	

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
		
		lb.add(new ExploMultiBehaviour(this,this.myMap,this.myGraph));
		lb.add(new SayHello(this));
		//lb.add(new ReceiveMessageBehaviour(this));
		
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		
		
		addBehaviour(new startMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	
	
}
