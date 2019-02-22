package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;


/**
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.</br>
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs.</br> 
 * This (non optimal) behaviour is done until all nodes are explored. </br> 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.</br> 
 * Warning, this behaviour is a solo exploration and does not take into account the presence of other agents (or well) and indefinitely tries to reach its target node
 * @author hc
 *
 */
public class ExploMultiBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 1297041785790783610L;

	private boolean finished = false;
	
	private ReceiveMessageBehaviour messageReceiver;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;
	private HashMap<String,String[]> myGraph;

	/**
	 * Nodes known but not yet visited
	 */
	private List<String> openNodes;
	/**
	 * Visited nodes
	 */
	private Set<String> closedNodes;


	public ExploMultiBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, HashMap<String,String[]> myGraph) {
		super(myagent);
		this.myMap=myMap;
		this.myGraph=myGraph;
		this.openNodes=new ArrayList<String>();
		this.closedNodes=new HashSet<String>();
	}

	@Override
	public void action() {
		
		if(this.messageReceiver==null) {
			this.messageReceiver = new ReceiveMessageBehaviour(this.myAgent);
			this.myAgent.addBehaviour(this.messageReceiver);
		}
		
		if(this.myMap==null)
			this.myMap= new MapRepresentation();
		
		if(this.myGraph==null)
			this.myGraph = new HashMap<>();
		
		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
	
		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			

			//1) remove the current node from openlist and add it to closedNodes.
			this.closedNodes.add(myPosition);
			this.openNodes.remove(myPosition);

			this.myMap.addNode(myPosition);
			this.myGraph.put(myPosition, new String[1]);
			
			

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNode=null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			List<String> childs = new ArrayList<String>();
			while(iter.hasNext()){
				String nodeId=iter.next().getLeft();
				
				if (!this.closedNodes.contains(nodeId)){
					if (!this.openNodes.contains(nodeId)){
						this.openNodes.add(nodeId);
						this.myMap.addNode(nodeId, MapAttribute.open);
						this.myMap.addEdge(myPosition, nodeId);
					}else{
						//the node exist, but not necessarily the edge
						this.myMap.addEdge(myPosition, nodeId);
					}
					
					childs.add(nodeId);
					
					if (nextNode==null) nextNode=nodeId;
				}
			}
			
			this.myGraph.put(myPosition, childs.toArray(new String[0]));
			
			//2.5) getMessage → answer messages in mailbox
			System.out.println(this.myAgent.getLocalName()+" : I have "+messageReceiver.nbWaiting()+" messages in mailbox");
			while(messageReceiver.hasMessage()) {
				ACLMessage message = messageReceiver.getFirstMessage();
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
				
			}

			//3) while openNodes is not empty, continues.
			if (this.openNodes.isEmpty()){
				//Explo finished
				finished=true;
				System.out.println(this.myAgent.getLocalName()+" : Exploration successufully done, behaviour removed.");
			}else{
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNode==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					nextNode=this.myMap.getShortestPath(myPosition, this.openNodes.get(0)).get(0);
				}
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}

		}
	}

	@Override
	public boolean done() {
		return finished;
	}

}
