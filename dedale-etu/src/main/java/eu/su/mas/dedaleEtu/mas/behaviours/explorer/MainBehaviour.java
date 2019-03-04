package eu.su.mas.dedaleEtu.mas.behaviours.explorer;

import java.util.HashMap;
import java.util.HashSet;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.FSMBehaviour;




public class MainBehaviour extends FSMBehaviour {
	private MapRepresentation myMap;
	private HashMap<String,HashSet<String>> myGraph;

	
		// TODO Auto-generated constructor stub
		
		
		private static final long serialVersionUID = 1L;

		public MainBehaviour(final AbstractDedaleAgent myagent) {
			super(myagent);
			
			//STATES
			this.registerFirstState(new ExploreBehaviour(myagent,myMap,myGraph) ,"EXPLORE");
			//registerState(new ExchangeMapBehaviour(myagent), "EXCHANGEMAP");
			registerState(new InterblocageBehaviour(myagent), "INTERBLOCAGE");
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
