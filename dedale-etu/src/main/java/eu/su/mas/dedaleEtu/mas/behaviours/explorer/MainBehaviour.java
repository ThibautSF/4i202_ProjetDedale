package eu.su.mas.dedaleEtu.mas.behaviours.explorer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.MailBox;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.FSMBehaviour;
import jade.lang.acl.ACLMessage;




public class MainBehaviour extends FSMBehaviour {


	
		// TODO Auto-generated constructor stub
		
		
		private static final long serialVersionUID = 1L;

		public MainBehaviour(final AbstractDedaleAgent myagent,MapRepresentation myMap,HashMap<String,HashSet<String>> myGraph, MailBox mailBox) {
			super(myagent);
			
			//STATES
			this.registerFirstState(new ExploreBehaviour(myagent,myMap,myGraph) ,"EXPLORE");
			//registerState(new ExchangeMapBehaviour(myagent), "EXCHANGEMAP");
			registerState(new InterblocageBehaviour(myagent,mailBox), "INTERBLOCAGE");
			//registerLastState(new EndBehaviour(), "END");
			
			//TRANSITIONS
			registerDefaultTransition("EXPLORE", "EXPLORE", new String[]{"EXPLORE"});
			
			//registerTransition("EXPLORE", "EXCHANGEMAP", 2, new String[]{"EXCHANGEMAP"});
			registerTransition("EXPLORE", "INTERBLOCAGE", 2, new String[]{"INTERBLOCAGE"});
			registerTransition("EXPLORE", "END", 4);
			
			//registerDefaultTransition("EXCHANGEMAP", "EXPLORE", new String[]{"EXPLORE"});
			//registerTransition("EXCHANGEMAP", "INTERBLOCAGE", 1, new String[]{"INTERBLOCAGE"});
			
			registerDefaultTransition("INTERBLOCAGE", "EXPLORE", new String[]{"EXPLORE"});
			//registerTransition("INTERBLOCAGE", "EXCHANGEMAP", 1, new String[]{"EXCHANGEMAP"});


	}

}
