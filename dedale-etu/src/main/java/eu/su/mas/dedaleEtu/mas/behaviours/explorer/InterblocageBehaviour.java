package eu.su.mas.dedaleEtu.mas.behaviours.explorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.MailBox;
import eu.su.mas.dedaleEtu.mas.agents.dummies.Data;

public class InterblocageBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 1L;
	private final int waitingTime = 7;
	private int cptWait = 0;
	private AID agent ; 
	private List<String> otherAgentPath ;
	private ArrayList<String> cheminCarrefour ;
	private MailBox mailBox;
	/**
	 * exit_value : 0 -> Explore
	 * 				1 -> ExchangeMap
	 */
	private int exit_value = 0;

	
	public InterblocageBehaviour(final AbstractDedaleAgent myagent, MailBox mailBox) {
		super(myagent);
		this.mailBox=mailBox;
	}
	
	public void setAgent(AID agent) {
		this.agent = agent;
	}
	
	@Override
	public void action() {
		
		exit_value = 0 ;
		int state = ((ExploreMultiAgent)super.myAgent).getInterblocageState();
		
		switch(state) {
		
			case 0 : 
				getInterblocageMessage(state);
			
				
				break ;
				
			case 1:
				
				/* TODO
				 * 1-choisir une priorité(aléatoire) ok 
				 * 2-agent maitre envoie son chemin à esclave ok 
				 * 3-esclave recule suivant le chemin de maitre
				 * 4-si esclave rencontre une alternative il prend l'alternative .
				 * 5- il laisse passer maitre("je suis passé")
				 * 6- esclave reprend son chemin
				 * 
				 */
				
				
				
			case 2 : //VOIR SI L'INTERBLOCAGE EST REGLE
				//regarder les maps voir si le noeud destination n'est plus interessant (SANS TRESOR)
				List<String> chemin = ((ExploreMultiAgent)this.myAgent).getChemin();
				String dest =chemin.get(chemin.size()-1);
				ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
				msg.setSender(this.myAgent.getAID()); msg.addReceiver(agent);
				
				//System.out.println(((CleverAgent)this.myAgent).getGraph().getNode(dest.getId()).getAttribute("state"));
				if (((ExploreMultiAgent)this.myAgent).getGraph().getNode(dest).getAttribute("state").equals("closed")){
					//a ce stade, un des deux agents peut etre débloqué
					// si un des deux est débloqué a va peut etre débloquer l'autre apres le deplacement du premier 
					// donc des que un est débloqué -> débloquer l'autre
					// if je suis débloqué : 
					// 		- envoie un message "good"					
					msg.setContent("good"); 	
				}
					// else je ne suis pas débloqué :
					// 		- envoie un message "bad"
				else {
					msg.setContent("bad"); 
				}
				
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				System.out.println(super.myAgent.getLocalName()+" envoie un message "+msg.getContent()+" a "+agent.getLocalName());
				// attendre r�ception message
				ACLMessage response = this.myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));
				
				//TODO: rajouter timeout  
				// timeout pose pb si deux agents arrivent dans ce case, un envoie bad, attends puis quitte
				// l'autre envoie bad, a re�u bad et part tout seul dans les next case
				while( (response == null || !(response.getSender().equals(agent)))&& cptWait<=1.5*waitingTime){
					response = this.myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));
					cptWait++;
					block(1000);
					if(response!=null)
						System.out.println("case 2 interblocage: "+super.myAgent.getLocalName()+" a recu "+response.getSender().getLocalName());
				}

				//if bad et moi aussi : state 3  else : finish
				if( response != null && response.getContent().equals("bad") && msg.getContent().equals("bad")){
					cptWait=0;
					((ExploreMultiAgent)this.myAgent).setInterblocageState(state+1);
					System.err.println("passage au case 3: ECHANGE DE DISTANCES AU CARREFOUR");
				}
					
				else{ 
					System.out.println("FIN de l'INTERBLOCAGE");
					((ExploreMultiAgent)this.myAgent).setInterblocage(false);
					((ExploreMultiAgent)this.myAgent).setInterblocageState(0);
					((ExploreMultiAgent)this.myAgent).setChemin(new ArrayList<String>());
				}
				
				break ;
				
			case 3 : // ECHANGE DES DISTANCES AU CARREFOUR LE PLUS PROCHE (2 messages)
				//les deux agents sont présents et n'ont pas réglé leur interblocage
				// calcul de la distance au carrefour le plus proche ( carrefour[0] = distance ; carrefour[1] : le chemin )
				List<String> carrefour = calculDistanceCarrefour() ;
				// dans l'ordre alphabétique le premier agent est celui qui devra envoyer le message avec sa distance ...
				if(super.myAgent.getLocalName().compareTo(agent.getLocalName())<0){
					ACLMessage message = new ACLMessage(ACLMessage.INFORM_REF);
					message.setSender(super.myAgent.getAID()); message.addReceiver(agent);
					try {
						List<String> idChemin = (((ExploreMultiAgent)super.myAgent).getChemin());
						Data<Integer, List<String>, Integer, Integer> toSend = new Data<Integer, List<String>, Integer, Integer>(carrefour.size(), idChemin, null, null);
						message.setContentObject(toSend);
					} catch (IOException e) {
						e.printStackTrace();
					}
					((AbstractDedaleAgent)this.myAgent).sendMessage(message);
					try {
						System.out.println(super.myAgent.getLocalName()+" envoie sa distance carrefour "+message.getContentObject().toString()+" a "+agent.getLocalName());
					} catch (UnreadableException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					//...puis attendre la réponse de l'agent2
					message = null ;
					do {
						message = super.myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM_REF));
						cptWait++;
						block(1000);
					} while(message ==null&& cptWait<=5*waitingTime);
					
					cptWait=0;
					if(message != null && message.getContent().equals("not you")){
						((ExploreMultiAgent)super.myAgent).setInterblocageState(5);
					}else{
						if(message==null){
							System.out.println(myAgent.getLocalName().toString()+" ne sait pas qui doit bouger !");
							((ExploreMultiAgent)super.myAgent).setInterblocageState(6);
							((ExploreMultiAgent)super.myAgent).setInterblocage(false);
						} else {
							
							try {
								otherAgentPath =( (Data<String,List<String>,Integer,Integer>) message.getContentObject()).getSecond() ;
							} catch (UnreadableException e) {
								e.printStackTrace();
							}
							
							cheminCarrefour = (ArrayList<String>) carrefour;
							((ExploreMultiAgent)super.myAgent).setInterblocageState(4);
						}
					}
						
				} 
				
				else {
					// agent2 attend la distance de l'autre ...
					ACLMessage distanceMsg ;
					do{
						distanceMsg = super.myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM_REF));
						cptWait++;
						block(1000);
					} while(distanceMsg ==null && cptWait<=5*waitingTime);
					cptWait=0;
					if(distanceMsg==null){
						System.out.println(myAgent.getLocalName().toString()+" n'a pas re�u la distance!");
						((ExploreMultiAgent)super.myAgent).setInterblocageState(6);
						((ExploreMultiAgent)super.myAgent).setInterblocage(false);
					} else {
						//...puis envoie qui doit bouger
						int otherDistance=0;
						
						try {
							otherDistance = ((Data<Integer, List<String>,Integer, Integer>)distanceMsg.getContentObject()).getFirst();
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
						
						ACLMessage decideWhoMoves = new ACLMessage(ACLMessage.INFORM_REF);
						decideWhoMoves.setSender(super.myAgent.getAID()); decideWhoMoves.addReceiver(agent);
						
						if(otherDistance < (int)carrefour.size()){
							List<String> idChemin = ((ExploreMultiAgent)super.myAgent).getChemin();
							Data<String, List<String>,Integer,Integer> toSend = new Data<String,List<String>,Integer,Integer>("you",idChemin,null,null);
							
							try {
								decideWhoMoves.setContentObject(toSend);
							} catch (IOException e) {
								e.printStackTrace();
							}
							((AbstractDedaleAgent)this.myAgent).sendMessage(decideWhoMoves);
							System.out.println(super.myAgent.getLocalName()+" envoie 'c est a toi de bouger' a "+agent.getLocalName());
							
							((ExploreMultiAgent)super.myAgent).setInterblocageState(5);
						} else {
							decideWhoMoves.setContent("not you");
							((AbstractDedaleAgent)this.myAgent).sendMessage(decideWhoMoves);
							System.out.println(super.myAgent.getLocalName()+" envoie 'c est a moi d bouger' a "+agent.getLocalName());
							
							
							try {
								otherAgentPath =( (Data<Integer,List<String>,Integer,Integer>) distanceMsg.getContentObject()).getSecond() ;
							} catch (UnreadableException e) {
								e.printStackTrace();
							}
							
							cheminCarrefour = (ArrayList<String>) carrefour;
							((ExploreMultiAgent)super.myAgent).setInterblocageState(4);
						}
					}
				}
				
				break ;
				
			case 4 : //L'AGENT DOIT BOUGER
				// il faut ajouter un noeud voisin au noeud carrefour pour le déplacement
				// le noeud ne doit pas etre sur le chemin de l'autre agent
				System.out.println(myAgent.getLocalName().toString()+" va jusqu'au carrefour");
				String idCarrefour = (cheminCarrefour.isEmpty())? ((AbstractDedaleAgent)super.myAgent).getCurrentPosition() : cheminCarrefour.get(cheminCarrefour.size()-1) ; 
				
				Iterator<Node> carrefourIterator = ((ExploreMultiAgent)super.myAgent).getGraph().getNode(idCarrefour).getNeighborNodeIterator();
				ArrayList<Node> neighbours = new ArrayList<Node>();
				Node neighbour;
				
				Random r= new Random();
				int index;
				
				while (carrefourIterator.hasNext()){
					neighbours.add(carrefourIterator.next());
				}
				
				do{
					index = r.nextInt(neighbours.size());
					neighbour = neighbours.get(index);
				}while(otherAgentPath.contains(neighbour.getId()));
				
				cheminCarrefour.add(neighbour.getId());
				
				
				((ExploreMultiAgent)super.myAgent).setChemin(cheminCarrefour);
				//signaler � l'autre qu'il peut bouger ?
				ACLMessage youCanMoveMsg = new ACLMessage(ACLMessage.CONFIRM);
				youCanMoveMsg.setSender(super.myAgent.getAID()); youCanMoveMsg.addReceiver(agent);
				youCanMoveMsg.setContent("move");
				((AbstractDedaleAgent)this.myAgent).sendMessage(youCanMoveMsg);
				System.out.println(myAgent.getLocalName().toString()+" dit que "+agent.getLocalName().toString()+" peut bouger");
				((ExploreMultiAgent)super.myAgent).setInterblocageState(6);
				((ExploreMultiAgent)super.myAgent).setInterblocage(false);
				break ;
				
			case 5 : //L'AGENT ATTEND QUE L'AUTRE BOUGE
				ACLMessage moveAnswer = super.myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));
				if(moveAnswer!=null){
					//attend un peu avant de bouger
					block(1000);
					System.out.println(myAgent.getLocalName().toString()+" peut bouger !");
					((ExploreMultiAgent)super.myAgent).setInterblocageState(0);
					((ExploreMultiAgent)super.myAgent).setInterblocage(false);
				}
				break;
				
			default :
				break;
		}
	}
	
	
	
	
	/**
	 * Calcule la distance entre l'agent et le carrefour (noeud � au moins 3 branches) le plus proche.
	 * @return un tableau contenant la distance du plus court chemin � un carrefour et ce chemin
	 */
	public List<String> calculDistanceCarrefour() {
		List<String> shortestPath=new ArrayList<String>();
		
		String pos = ((AbstractDedaleAgent)super.myAgent).getCurrentPosition();
		Graph graph = ((ExploreMultiAgent)super.myAgent).getGraph() ;
		int min = Integer.MAX_VALUE;
		Node closest = null;
		
		//Dijkstra
		Dijkstra dijk = new Dijkstra(Dijkstra.Element.NODE, null, null);
		dijk.init(graph);
		dijk.setSource(graph.getNode(pos));
		dijk.compute();
		
		for (Node n : graph){
			if(n.getDegree() >= 3){
				double length = dijk.getPathLength(n);
				if(length < min){
					min = (int) length ;
					closest = n ;
				}
			}
		}
		

		List<Node> shortPath = dijk.getPath(closest).getNodePath();
		Iterator<Node> iter=shortPath.iterator();
		while (iter.hasNext()){
			shortestPath.add(iter.next().getId());
		}
		shortestPath.remove(0);
		
		return shortestPath ;

	}

	
	
	/**
	 * Convertit une liste de noeuds en liste d'identifiant de ces noeuds
	 * @param path : la liste de noeuds � convertir
	 * @return la liste des identifiants
	 */
	public List<String> convertNodeToId(List<Node> path){
		List<String> idChemin = new ArrayList<String>() ;
		for (Node n : path){
			idChemin.add(n.getId());
		}
		
		return idChemin;
	}
	
	public void getInterblocageMessage(int state) {
		
		

		// ATTENTE DU MESSAGE DE L'AGENT EN INTERBLOCAGE AVEC NOUS
		System.out.println(super.myAgent.getLocalName()+": Je suis en interblocage");
		
		//attendre le message de l'agent avec qui on est en interblocage
		
		if(this.mailBox.hasMessage(true)) {
		
		
		ACLMessage answer = this.mailBox.getFirstMessage(true);
		
		
		// le message recu contiendra : notre position_la position de l'autre
		if(answer != null && answer.getContent().equals(((AbstractDedaleAgent)this.myAgent).getCurrentPosition()+"_"+((ExploreMultiAgent)this.myAgent).getChemin().get(0))){
			
			agent = answer.getSender(); 
			cptWait=0;
			((ExploreMultiAgent)this.myAgent).setInterblocageState(state+1);
		} else {
			answer = null;
			block(1500);
			cptWait++;
		}
		//si temps d'attente trop long on gére gollem
		if(cptWait==waitingTime && answer==null){
			System.out.println("Temps d'attente trop long pour "+this.myAgent.getLocalName()+" qui quitte l'interblocage");
			((ExploreMultiAgent)this.myAgent).setInterblocage(false);
		}
		}
		
		
		
	}
	
	public void setPriority() {
		
		Random r1 = new Random();
        int n = r1.nextInt(1000000);
        
        //envoi msg priorité
        ACLMessage msg= new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol("Priorite");
        msg.setContent(r1.toString());
        msg.addReceiver(agent);
        msg.setSender(myAgent.getAID());
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
      
        
	}
	
	public void getMessagePriority() {
		while(this.mailBox.hasMessage()) {
			ACLMessage message = this.mailBox.getFirstMessage();
			if(message!=null) {
				switch (message.getProtocol()) {
				case "Priorite":
				
					ExploreMultiAgent moi= ((ExploreMultiAgent)super.myAgent);
					int p=Integer.parseInt(message.getContent());
					if(p<(moi.getPriority())){
						
						
						
						ACLMessage msg= new ACLMessage(ACLMessage.INFORM);
				        msg.setProtocol("Maitre");
				        
				        try {
				        msg.setContentObject(moi.getChemin());
				        }
				        catch(IOException e) {
				        	e.printStackTrace();
				        }
				        msg.addReceiver(agent);
				        msg.setSender(myAgent.getAID());
				        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				      
						
						
					}
					
					System.out.println(this.myAgent.getLocalName()+" : The message ("+message.getPostTimeStamp()+") from "+message.getSender()+" is \""+message.getContent()+"\"");
					
					break;
				}
			}
		}
	}
	
	
	
	
	
	@Override
	public int onEnd() {
		cptWait = 0;
		ACLMessage mapMessage;
		do{
			mapMessage = this.myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM_REF));
		}while(mapMessage!=null);
		return exit_value;
	}
	
	@Override
	public boolean done() {
		return (!((ExploreMultiAgent)super.myAgent).isInterblocage()) || exit_value==1;
	}

}

