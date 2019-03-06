package eu.su.mas.dedaleEtu.mas.behaviours.explorer;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import dataStructures.tuple.Couple;
import java.util.Set;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import eu.su.mas.dedale.env.ElementType;
import eu.su.mas.dedale.env.Observation;

import jade.core.AID;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
//import mas.agents.CleverAgent;

public class ExploreBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 9088209402507795289L;
	private boolean finished = false;
	private Graph graph ;
	private List<String> chemin;
	private ArrayList<String> opened ;
	private int step = 0;
	private int immo = 0;
	private final int MAX_STEP = 10;
	private MapRepresentation myMap;
	private HashMap<String,HashSet<String>> myGraph;
	
	/**
	 * Nodes known but not yet visited
	 */
	private List<String> openNodes;
	/**
	 * Visited nodes
	 */
	private Set<String> closedNodes;
	/**
	 * exit value : 0 -> explore
	 * 				2 -> communication Time 
	 * 				3 -> interblocage sur un chemin
	 * 				4 -> fin de la phase d'exploration
	 */
	private int exitValue = 0;
	

	
	public ExploreBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, HashMap<String,HashSet<String>> myGraph){
		super(myagent);
		this.myMap=myMap;
		this.myGraph=myGraph;
		
		graph = ((ExploreMultiAgent) myagent).getGraph();
		//graph= new SingleGraph("My world vision");
		//graph.setAttribute("ui.stylesheet",nodeStyle);
		System.out.println(graph);
		openNodes = ((ExploreMultiAgent) myagent).getOpened();
		chemin = ((ExploreMultiAgent) myagent).getChemin();
		
		this.closedNodes=new HashSet<String>();
		//attention: cache l'exception IdAlreadyInUse
		//this.graph.setStrict(false);
	}
	

	/**
	 * fonction de recherche du plus court chemin vers le noeud ouvert le plus proche
	 * @param root : noeud racine
	 * @return le plus court chemin sans la racine vers le noeud ouvert le plus proche
	 */
	public List<Node> search(String root){

		Dijkstra dijk = new Dijkstra(Dijkstra.Element.NODE, null, null);
		dijk.init(graph);
		dijk.setSource(graph.getNode(root));
		dijk.compute();

		int min = Integer.MAX_VALUE;
		String shortest = null;
		
		for(String id : openNodes){
			double l = dijk.getPathLength(graph.getNode(id));
			if(l < min){
				min = (int) l;
				shortest = id ;
			}
		}	
		List<Node> shortPath = dijk.getPath(graph.getNode(shortest)).getNodePath();
		return shortPath ;
	}
	
	/**
	 * Deplacement de l'agent qui suit un chemin : traite les interblocages
	 */
	public void followPath(){
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if(myPosition.equals(this.openNodes.get(0)))
			this.openNodes.remove(0);

		String next = this.openNodes.get(0);
		/*
		 * si on a pas pu se déplacer il y a un agent qui nous bloque
		 * rentrer en communication avec lui
		 * dans interblocage state : 0 -> attente d'un message d'interblocage aussi
		 */
		
		//TODO version td
		/*
		 * 1- agent voit qu'il est bloqué
		 * 2-il envoie un message avec sa position et la position cible
		 * 3- si il recoie une reponse -> interblocage(a géré)
		 * 4-sinon si timeout alors gollem 
		 */
		
		if(!((ExploreMultiAgent) this.myAgent).getMoved()){
			//chemin.add(0,next); //pour conserver le chemin en entier, le noeud bloqué est donc le premier du chemin et destination le dernier
			
			((ExploreMultiAgent)this.myAgent).setInterblocage(true);	
			((ExploreMultiAgent)this.myAgent).setInterblocageState(0);
			refreshAgent();
			System.out.println("INTERBLOCAGE pour agent "+myAgent.getName()+" qui veut aller en "+next);
			final ACLMessage mess = new ACLMessage(ACLMessage.PROPOSE);
			mess.setSender(this.myAgent.getAID()); mess.setContent(next+"_"+myPosition); //le noeud qui nous bloque
			
			if(this.myAgent.getLocalName().equals("Explo1")) {
				mess.addReceiver(new AID("Explo2",AID.ISLOCALNAME));
			} else {
				mess.addReceiver(new AID("Explo1",AID.ISLOCALNAME));
			}
			/**
			Set<AID> cles = ((ExploreMultiAgent)this.myAgent).getAgentList().keySet();		
			for (AID aid : cles){
				mess.addReceiver(aid);
			}
			**/
			((AbstractDedaleAgent)this.myAgent).sendMessage(mess);
			System.out.println("j'ai envoyé le message");
			((ExploreMultiAgent)this.myAgent).setAgentsNearby(new ArrayList<AID>());
			exitValue = 2;
			step = 0;
			immo = 0;
			finished = true ;
		}
		else{
			((AbstractDedaleAgent)this.myAgent).moveTo(next);
		}
			
	}

	
	
	@Override
	public void action() {
		
		ArrayList<String> neighbors = new ArrayList<String>();
	
		exitValue= 0 ;
		
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		//graph = ((ExploreMultiAgent) myAgent).getGraph();
		opened = ((ExploreMultiAgent) myAgent).getOpened();
		chemin = ((ExploreMultiAgent) myAgent).getChemin();
		//attention: cache l'exception IdAlreadyInUse
		//graph.setStrict(false);
		
		//si on a pas changé de position et on était déjà en exloration -> on a pas pu se deplacer
		if(((ExploreMultiAgent)super.myAgent).getLastPosition().equals(myPosition) && step >  0)
			((ExploreMultiAgent)super.myAgent).setMoved(false);
		//mise a jour de la position
		else{
			((ExploreMultiAgent)super.myAgent).setLastPosition(myPosition);
			((ExploreMultiAgent)super.myAgent).setMoved(true);
			immo = 0;
		}
		
	
		
		if(this.myGraph==null)
			this.myGraph = new HashMap<>();
		
		
		if (myPosition!=""){
			
			
			
			
			

			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
			
			this.closedNodes.add(myPosition);
			this.openNodes.remove(myPosition);

			this.myMap.addNode(myPosition);
			this.myGraph.put(myPosition, new HashSet<String>());
			
			

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNode=null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			
			
			
			
			
		
		
			
		
			
			
			//List<String> childs = new HashSet<String>();
			while(iter.hasNext()){
				String nodeId=iter.next().getLeft();
				
				if (!this.closedNodes.contains(nodeId)){
					neighbors.add(nodeId);
					if (!this.openNodes.contains(nodeId)){
						
						this.openNodes.add(nodeId);
						this.myMap.addNode(nodeId, MapAttribute.open);
						this.myMap.addEdge(myPosition, nodeId);
					}else{
						//the node exist, but not necessarily the edge
						this.myMap.addEdge(myPosition, nodeId);
					}
					
					//childs.add(nodeId);
					this.myGraph.get(myPosition).add(nodeId);
					
					if (nextNode==null) nextNode=nodeId;
				}
			}
			
			//this.myGraph.put(myPosition, childs);
			
			
			}

//			
//			}

			block(1000);
			
//			
			//If there is a message in the inbox of someone trying to exchange maps
			//save the sender and finish this behaviour
			final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);			
			final ACLMessage msg = this.myAgent.receive(msgTemplate);
			
			//If someone is blocked by this agent
//			final MessageTemplate msgTemp = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);			
//			final ACLMessage blockMsg = this.myAgent.receive(msgTemp);
			
			// TODO: lastCom
			//ArrayList<AID> lastCom = ((CleverAgent)super.myAgent).getLastCom();
			
			//si l'expéditeur est qq'un avec qui on a communiqué récemment, ignorer
			//if(msg != null && !msg.getContent().equals("ok") && !lastCom.subList(0, lastCom.size()/4).contains(msg.getSender())){
			
			if(msg != null && !msg.getContent().equals("ok")){
				ArrayList<AID> sender = new ArrayList<AID>();
				sender.add((AID) msg.getSender());
				((ExploreMultiAgent) super.myAgent).setAgentsNearby(sender);
				((ExploreMultiAgent) super.myAgent).setCommunicationState(2);
				refreshAgent();
				
				String content = msg.getContent();
				HashMap<AID, ArrayList<String>> agentList = ((ExploreMultiAgent)this.myAgent).getAgentList();
				//si je n'ai pas sa position initiale (et sa capacite)
				if( agentList.get(sender.get(0)).get(0) == ""){
					String[] tokens = content.split("[:]");
					ArrayList<String> infos = new ArrayList<String>(Arrays.asList(tokens[0],tokens[1],""));
					agentList.replace(sender.get(0), infos);
					((ExploreMultiAgent)this.myAgent).setAgentList(agentList);
					System.err.println(this.myAgent.getLocalName()+" a initialise pos: "+tokens[0]+" et cap: "+tokens[1]+" de "+sender.get(0).getLocalName());
				}
								
				step = 0;
				finished = true;
				exitValue = 2;
				System.out.println(this.myAgent.getLocalName()+" is in Explore and has a new message in the mailbox "+msg.getContent());
			}
			
					
			//tous les MAX_STEP temps, on echange la map a ceux proches de nous			
			else if(step>=MAX_STEP){
				((ExploreMultiAgent) super.myAgent).setCommunicationState(0);
				refreshAgent();
				step = 0;
				immo = 0;
				finished = true ;
				exitValue = 2;
				System.out.println("COMMUNICATION TIME for "+myAgent.getName());
				
			} else {
				//si on n'a plus de noeuds ouverts, l'exploration est finie
				if(openNodes.isEmpty()){
				System.err.println("Fin de l'exploration pour "+myAgent.getLocalName().toString());
					exitValue = 4;
					refreshAgent();
					finished = true;
//					Random r= new Random();
//					int moveId=r.nextInt(lobs.size());
//					((mas.abstractAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
				}
				else{
					step++;
					//si on a un chemin a suivre
					//et que ce chemin ne reduit pas a ma position
					if(chemin.size() > 1 || (!chemin.isEmpty() && !chemin.get(0).equals(myPosition))){
						followPath();
					}
					else{
						
						chemin = new ArrayList<String>();
						//si on a un voisin ouvert 
						if(this.openNodes.size()!= 0){
							Random r= new Random();
							 int i = r.nextInt(openNodes.size());
							 System.out.println("la liste des open est "+this.openNodes.get(0)+","+myPosition);
							 //neighbors.get(i)
						
							
							 String next = this.myMap.getShortestPath(myPosition,this.openNodes.get(0)).get(0);
							//String next=this.myMap.getNode(this.openNodes.get(0));
							
							System.out.println(myAgent.getLocalName()+" va en "+ next+"il reste "+this.openNodes+"il est "+myPosition);
							immo++;

							//si on a essaye trop de fois de bouger vers un voisin ouvert -> chercher un autre chemin	
							if(!((ExploreMultiAgent)super.myAgent).getMoved() && immo > 4){							
								chemin =this.myMap.getShortestPath(myPosition, this.openNodes.get(0));
								System.out.println("mon chemin est "+ chemin);
								followPath();			
							}
							else{
							    //next=this.myMap.getNode(this.openNodes.get(i));
								((AbstractDedaleAgent)this.myAgent).moveTo(next);
								refreshAgent();
							}
							
						}
						else{
							// si pas de voisins
							//on cherche le noeud le plus proche						
							//chemin = search(this.openNodes.get(0));
							chemin =this.myMap.getShortestPath(myPosition, this.openNodes.get(0));
							System.out.println(this.myAgent.getLocalName()+" : Je cherche un nouveau chemin");
							followPath();
							
						}	
						
						
					}
	
				}
			}
		}

		
	
	  
	public void refreshAgent(){
		((ExploreMultiAgent) super.myAgent).setGraph(graph);
		((ExploreMultiAgent) super.myAgent).setChemin(chemin);
		((ExploreMultiAgent) super.myAgent).setOpened(opened);
	}
	
	public int onEnd(){
		refreshAgent();
		return exitValue;
	}
	
	@Override
	public boolean done() {
		return finished;
	}

}
