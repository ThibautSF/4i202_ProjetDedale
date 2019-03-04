package eu.su.mas.dedaleEtu.mas.behaviours.explorer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.SayHello;
import eu.su.mas.dedaleEtu.mas.behaviours.treatMessageBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.FSMBehaviour;
import jade.lang.acl.ACLMessage;

public class FsmBehaviour extends FSMBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MapRepresentation myMap;
	private HashMap<String,HashSet<String>> myGraph;
	private Queue<ACLMessage> mailBox;


	public FsmBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
		// TODO Auto-generated constructor stub
		this.onInit();
		
	}
	
	
	public void onInit() {
		//definiton des etats
		this.registerFirstState	(new ExploMultiBehaviour(this.myAgent,this.myMap,this.myGraph),"A");

		//fsm.registerState(new ReceiveMessageBehaviour(this),"B");
		this.registerState(new treatMessageBehaviour(this.myAgent,mailBox),"C");
		this.registerLastState(new SayHello(this.myAgent),"D");

		//definition des transaction
		this.registerDefaultTransition("A","C");
		this.registerDefaultTransition("A","D"); //si boite est vide 
		
		//this.registerTransition("A","C",1); // si boite n'est pas vide
		//this.registerTransition("A","D",2); //si boite est vide 
		this.registerTransition("D","A",1); //si y'a personne à ma portée
		this.registerTransition("D","C",1); //si y'a quelq'un à ma portée

		//this.registerTransition("D","C",2); //si y'a quelq'un à ma portée

	}



}
