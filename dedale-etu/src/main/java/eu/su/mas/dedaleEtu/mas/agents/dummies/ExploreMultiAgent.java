package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveMessageBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SayHello;
import eu.su.mas.dedaleEtu.mas.behaviours.treatMessageBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.explorer.ExploMultiBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.explorer.FsmBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.explorer.MainBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
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
	
	
	//private Graph graph=  new SingleGraph("");
	private List<String> chemin = new ArrayList<String>();
	private ArrayList<AID> agentsNearby = new ArrayList<AID>();
	private ArrayList<String> opened = new ArrayList<String>();
	
	private ArrayList<String> treasures = new ArrayList<String>();
	private ArrayList<String> diamonds = new ArrayList<String>();
	
	// {aid : (position_initiale, capacite_courante, type) }
	private HashMap<AID, ArrayList<String>> agentList = new HashMap<AID, ArrayList<String>>();
	private String firstPosition;
	private String type ="";
	private String treasureToFind="";
	private AID agentToReach;
	
	private int comingbackState = 0;
	private int pickingState = 0;
	private int communicationState = 0;
	private boolean interblocage = false;	
	private int interblocageState = 0;
	private boolean moved = true;
	private String lastPosition = "";
	
	//derniers agents avec qui ont a communiqué
	private ArrayList<AID> lastCom= new ArrayList<AID>();
	
	
	public Graph getGraph() {
		return (Graph) myMap;
	}

	public void setGraph(Graph graph) {
		this.myMap = (MapRepresentation) graph;
	}

	public List<String> getChemin() {
		return chemin;
	}

	public void setChemin(List<String> chemin) {
		this.chemin = chemin;
	}
	
	public HashMap<AID, ArrayList<String>> getAgentList() {
		return agentList;
	}

	public void setAgentList(HashMap<AID, ArrayList<String>> agentList) {
		this.agentList = agentList;
	}
	
	public String getFirstPosition() {
		return firstPosition;
	}

	public void setFirstPosition(String firstPosition) {
		this.firstPosition = firstPosition;
	}

	public int getComingbackState() {
		return comingbackState;
	}

	public void setComingbackState(int comingbackState) {
		this.comingbackState = comingbackState;
	}

	public ArrayList<AID> getAgentsNearby() {
		return agentsNearby;
	}

	public void setAgentsNearby(ArrayList<AID> agentsNearby) {
		this.agentsNearby = agentsNearby;
	}

	public ArrayList<String> getOpened() {
		return opened;
	}

	public void setOpened(ArrayList<String> opened) {
		this.opened = opened;
	}

	
	public ArrayList<String> getTreasures() {
		return treasures;
	}

	public void setTreasures(ArrayList<String> treasures) {
		this.treasures = treasures;
	}

	public ArrayList<String> getDiamonds() {
		return diamonds;
	}

	public void setDiamonds(ArrayList<String> diamonds) {
		this.diamonds = diamonds;
	}

	public int getCommunicationState() {
		return communicationState;
	}

	public void setCommunicationState(int communicationState) {
		this.communicationState = communicationState;
	}
	
	public boolean isInterblocage() {
		return interblocage;
	}

	public void setInterblocage(boolean interblocage) {
		this.interblocage = interblocage;
	}
	
	public ArrayList<AID> getLastCom() {
		return lastCom;
	}

	public void setLastCom(ArrayList<AID> lastCom) {
		this.lastCom = lastCom;
	}
	

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	
	public int getInterblocageState() {
		return interblocageState;
	}

	public void setInterblocageState(int interblocageState) {
		this.interblocageState = interblocageState;
	}

	public String getLastPosition() {
		return lastPosition;
	}

	public void setLastPosition(String lastPosition) {
		this.lastPosition = lastPosition;
	}

	public boolean getMoved() {
		return moved;
	}

	public void setMoved(boolean moved) {
		this.moved = moved;
	}

	public int getPickingState() {
		return pickingState;
	}

	public void setPickingState(int pickingState) {
		this.pickingState = pickingState;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTreasureToFind() {
		return treasureToFind;
	}

	public void setTreasureToFind(String treasureToFind) {
		this.treasureToFind = treasureToFind;
	}
	
	public AID getAgentToReach() {
		return agentToReach;
	}

	public void setAgentToReach(AID agentToReach) {
		this.agentToReach = agentToReach;
	}

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
		//ParallelBehaviour comportementparallele = new ParallelBehaviour();
		MainBehaviour fsm = new MainBehaviour(this) ;
		//scomportementparallele.addSubBehaviour(fsm);
		//ReceiveMessageBehaviour rmb=new ReceiveMessageBehaviour(this);
		//comportementparallele.addSubBehaviour(rmb);
		
			

		
				
		lb.add(fsm);
		//lb.add(comportementparallele);
	
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		
		
		addBehaviour(new startMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	
	
}
