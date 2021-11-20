package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.FSMExploAgent;
import jade.core.behaviours.OneShotBehaviour;


/**
 * <pre>
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs. 
 * This (non optimal) behaviour is done until all nodes are explored. 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.
 * Warning, the sub-behaviour ShareMap periodically share the whole map
 * </pre>
 * @author dl
 *
 */
public class ObservationBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	/**
	 * @param myAgent
	 */
	public ObservationBehaviour(final AbstractDedaleAgent myAgent) {
		super(myAgent);
	}

	@Override
	public void action() {

		String myPosition;
		String lastPosition;
		String nextNode = null;

		switch (((FSMExploAgent) this.myAgent).getMode()) {

		case 0 : // EXPLORATION MODE

			//System.out.println("Hello I'm " + this.myAgent.getLocalName() + " and I'm exploring");

			/*****************************************************
			 * Wait to see what happen on the graph
			 *****************************************************/
			try {
				this.myAgent.doWait(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			/************************************************
			 * 
			 * Map's Initialisation 
			 * 
			 ************************************************/

			if (((FSMExploAgent) this.myAgent).getMyMap() == null) {
				((FSMExploAgent) this.myAgent).initiateMyMap(); // Initiate the map of the agent
				((FSMExploAgent) this.myAgent).setWait(1);
				((FSMExploAgent) this.myAgent).setGetoutCnt(0);
				System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : Initialisation");
			}



			myPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
			lastPosition = ((FSMExploAgent) this.myAgent).getLastPosition();

			//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : My position - " + myPosition);
			//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : My last position - " + lastPosition);
			//System.out.println(this.myAgent.getLocalName() + " <--- I see some agents there - " + ((FSMExploAgent) this.myAgent).getPosition() );

			/************************************************
			 * 
			 * Check if someone is blocking the path
			 * 
			 ************************************************/
			String myDestination = ((FSMExploAgent) this.myAgent).getDestination();
			
			if (((FSMExploAgent) this.myAgent).getGetoutCnt()>=10) {
				//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : Exploration done (not finished)");
				((FSMExploAgent) this.myAgent).setMode(1);
				if (((FSMExploAgent) this.myAgent).getMyMap().checkTypeGraph()<0.05) {
					((FSMExploAgent) this.myAgent).setStyle(0);
					//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : Tree Hunt Started");
				}
				else {
					((FSMExploAgent) this.myAgent).setStyle(1);
					//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : Graph Hunt Started");
				}
			}
			
			if (myPosition.equals(myDestination)) {
				((FSMExploAgent) this.myAgent).setDest_wumpusfound(false);
			}

			if (lastPosition.equals(myPosition) && ((FSMExploAgent) this.myAgent).getPosition().contains(((FSMExploAgent) this.myAgent).getNextDest())) {
				//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : I'm blocked by an agent");
				String far = null;
				while (((far == null) || (far.equals(myPosition))) || ((((FSMExploAgent) this.myAgent).getMyMap().getShortestPath(myPosition, far) == null) || ((FSMExploAgent) this.myAgent).getMyMap().getShortestPath(myPosition, far).contains(((FSMExploAgent) this.myAgent).getNextDest()))){
					far = ((FSMExploAgent) this.myAgent).getMyMap().getRandomNode();
				}
				//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : I choosed to go on the node - " + far);
				nextNode = ((FSMExploAgent) this.myAgent).getMyMap().getShortestPath(myPosition, far).get(0);
				Random rando = new Random();
				if(rando.nextDouble() >= 0.5) {
					nextNode = ((FSMExploAgent) this.myAgent).getNextDest();
				}
			}
			else if (((FSMExploAgent) this.myAgent).isDest_wumpusfound() && (myDestination != null)) {
				nextNode = ((FSMExploAgent) this.myAgent).getMyMap().getShortestPath(myPosition, myDestination).get(0);
			}
			else if (lastPosition.equals(myPosition) && !((FSMExploAgent) this.myAgent).getPosition().contains(((FSMExploAgent) this.myAgent).getNextDest()) && !(myPosition.equals(((FSMExploAgent) this.myAgent).getNextDest()))) {
				//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : Wumpus found");
				((FSMExploAgent) this.myAgent).increaseWumpusCnt();
				nextNode = ((FSMExploAgent) this.myAgent).getNextDest();
			}


			/************************************************
			 * 
			 * Explore the Next Node
			 * 
			 ************************************************/
			else if (myPosition != null) {
				// Observe around the current position
				List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();

				/************************************************
				 * 
				 * Update informations about nodes
				 * 
				 ************************************************/
				((FSMExploAgent) this.myAgent).setWumpusCnt(0);
				((FSMExploAgent) this.myAgent).getMyMap().addNode(myPosition, MapAttribute.closed);
				Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();

				while(iter.hasNext()){
					String nodeId=iter.next().getLeft();
					boolean isNewNode=((FSMExploAgent) this.myAgent).getMyMap().addNewNode(nodeId);
					if (!(myPosition.equals(nodeId))){
						((FSMExploAgent) this.myAgent).getMyMap().addEdge(myPosition, nodeId);
						if ( (nextNode==null && isNewNode) && (!((FSMExploAgent) this.myAgent).getPosition().contains(nodeId)) ) {
							nextNode=nodeId;
						}
					}
				}

				/************************************************
				 * 
				 * Update informations about stenchs
				 * 
				 ************************************************/

				Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter2=lobs.iterator();
				ArrayList<String> NodeStench= new ArrayList<String>();

				while(iter2.hasNext()){
					Couple<String, List<Couple<Observation, Integer>>> i = iter2.next();
					String nodeId=i.getLeft();
					((FSMExploAgent) this.myAgent).addOpenNextNodes(nodeId);
					if(!(i.getRight().isEmpty())) {
						if(!(i.getRight().get(0).getLeft()==null)) {
							String a = i.getRight().get(0).getLeft().toString();
							if (a.equals("Stench")){
								NodeStench.add(nodeId);
							}
						}
					}
				}

				//System.out.println(this.myAgent.getLocalName() + " <-- Observation exploreur : "+ lobs);
				//System.out.println(this.myAgent.getLocalName() + " <-- Odeur exploreur : "+ NodeStench);


				if (NodeStench.size() == 1) {
					((FSMExploAgent) this.myAgent).setOwnStenchDirection(NodeStench.get(0));
				}
				else if(NodeStench.size() > 1) {
					((FSMExploAgent) this.myAgent).setOwnInsideStench(myPosition);
				}
				else {
					((FSMExploAgent) this.myAgent).setOwnStenchDirection(null);
					((FSMExploAgent) this.myAgent).setOwnInsideStench(null);
				}



				/************************************************
				 * 
				 * Priority to nodes with stench
				 * 
				 ************************************************/

				//if(NodeStench.size()>0 || !(((FSMExploAgent) this.myAgent).getStenchDirection().isEmpty()) || !(((FSMExploAgent) this.myAgent).getInsideStench().isEmpty())) {
				//	System.err.println(this.agent.getName()+" - Stench : "+this.agent.getStench());
				//	nearest = this.myMap.getNearestTargetPathAvailable(myPosition, this.agent.getStench(), agentsAround);
				//	if(nearest!=null && nearest.size()>0) {
				//		nextNode= nearest.get(0);
				//		System.err.println(this.agent.getName()+" - Priority to stench node : "+nextNode);
				//	}else {
				//		System.err.println(this.agent.getName()+" - no path to stench node found ");
				//	}
				//	}


				/************************************************
				 * 
				 * Test if exploration's done or continue
				 * 
				 ************************************************/

				if (!((FSMExploAgent) this.myAgent).getMyMap().hasOpenNode()){
					//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : Exploration successfully done");
					((FSMExploAgent) this.myAgent).setMode(1);
					if (((FSMExploAgent) this.myAgent).getMyMap().checkTypeGraph()<0.05) {
						((FSMExploAgent) this.myAgent).setStyle(0);
						//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : Tree Hunt Started");
					}
					else {
						((FSMExploAgent) this.myAgent).setStyle(1);
						//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : Graph Hunt Started");
					}
					break;
				}

				if ((nextNode==null) && ((FSMExploAgent) this.myAgent).getMyMap().hasOpenNode()){ 
					nextNode = ((FSMExploAgent) this.myAgent).getMyMap().getShortestPathToClosestOpenNode(myPosition,((FSMExploAgent) this.myAgent).getPosition()).get(0);
				}

			}
			if (((FSMExploAgent) this.myAgent).getWait()>0) {
				((FSMExploAgent) this.myAgent).decreaseWait();
			}

			//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : I'm going on the node - "+nextNode);
			((FSMExploAgent) this.myAgent).setLastPosition(myPosition);
			((FSMExploAgent) this.myAgent).setNextDest(nextNode);
			((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);

			((FSMExploAgent) this.myAgent).cleanPosition();
			((FSMExploAgent) this.myAgent).cleanStenchDirection();
			((FSMExploAgent) this.myAgent).cleanInsideStench();
			((FSMExploAgent) this.myAgent).cleanNextNodes();

			break;

		case 1 : // HUNT MODE

			//System.out.println("Hello I'm " + this.myAgent.getLocalName() + " and I'm hunting");

			myPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
			lastPosition = ((FSMExploAgent) this.myAgent).getLastPosition();

			/*****************************************************
			 * Wait to see what happen on the graph
			 *****************************************************/
			try {
				this.myAgent.doWait(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : My position - " + myPosition);
			//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : My last position - " + lastPosition);
			//System.out.println(this.myAgent.getLocalName() + " <--- I see some agents there - " + ((FSMExploAgent) this.myAgent).getPosition() );


			/** On regarde sur quelle type de carte on est**/

			/************************************************
			 * 
			 * Check if destination has been reached
			 * 
			 ************************************************/

			if (myPosition.equals(((FSMExploAgent) this.myAgent).getDestination())) {
				((FSMExploAgent) this.myAgent).setDestination(null);
				((FSMExploAgent) this.myAgent).setDestinationAlea(false);
				((FSMExploAgent) this.myAgent).setDestinationStench(false);
				((FSMExploAgent) this.myAgent).setDestinationInsideStench(false);
				((FSMExploAgent) this.myAgent).setDestinationInterblocage(false);

			}

			// Observe around the current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs2=((AbstractDedaleAgent)this.myAgent).observe();

			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter3=lobs2.iterator();

			/************************************************
			 * 
			 * Observe the stench
			 * 
			 ************************************************/

			ArrayList<String> NodeStench= new ArrayList<String>();
			while(iter3.hasNext()){
				Couple<String, List<Couple<Observation, Integer>>> i = iter3.next();
				//System.out.println(i);
				String nodeId=i.getLeft();
				((FSMExploAgent) this.myAgent).addNextNodes(nodeId);
				if(!(i.getRight().isEmpty())) {
					if(!(i.getRight().get(0).getLeft()==null)) {
						String a = i.getRight().get(0).getLeft().toString();
						if (a.equals("Stench")){
							NodeStench.add(nodeId);
						}
					}
				}
			}



			/************************************************
			 * 
			 * We check if stench direction or inside stench
			 * 
			 ************************************************/
			//System.out.println(NodeStench);

			if (NodeStench.size() == 1) {
				((FSMExploAgent) this.myAgent).setOwnStenchDirection(NodeStench.get(0));
			}
			else if(NodeStench.size() > 1) {
				((FSMExploAgent) this.myAgent).setOwnInsideStench(myPosition);
			}
			else {
				((FSMExploAgent) this.myAgent).setOwnStenchDirection(null);
				((FSMExploAgent) this.myAgent).setOwnInsideStench(null);
			}


			/************************************************
			 * 
			 * Check if someone is blocking the path
			 * 
			 ************************************************/
			
			String myDestination2 = ((FSMExploAgent) this.myAgent).getDestination();
					
			if (myPosition.equals(myDestination2)) {
				((FSMExploAgent) this.myAgent).setDest_wumpusfound(false);
			}

			if (lastPosition.equals(myPosition) && ((FSMExploAgent) this.myAgent).getPosition().contains(((FSMExploAgent) this.myAgent).getNextDest())) {
				//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : I'm blocked by an agent");
				if (!(NodeStench.isEmpty()) && (((FSMExploAgent) this.myAgent).getStyle()==1)) {// Si on percoit une odeur autour on y va
					for (String next : NodeStench) {
						if (!((FSMExploAgent) this.myAgent).getPosition().contains(next)  && !(next.equals(myPosition))) {
							//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : I'm following the stench node - " + next);
							nextNode = next;
							break;
						}
					}
				}
				if(nextNode==null) { // Si on ne peut pas on va vers une case adjacente aléatoire  
					String far = null;
					while (((far == null) || (far.equals(myPosition))) || (((FSMExploAgent) this.myAgent).getMyMap().getShortestPath(myPosition, far).isEmpty() || ((FSMExploAgent) this.myAgent).getMyMap().getShortestPath(myPosition, far).contains(((FSMExploAgent) this.myAgent).getNextDest()))){
						far = ((FSMExploAgent) this.myAgent).getMyMap().getRandomNode();
					}
					//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : I choosed to go on the node - " + far);
					//System.out.println(this.myAgent.getLocalName() + " <---Je me dirige vers une noeud aleatoire");
					nextNode = ((FSMExploAgent) this.myAgent).getMyMap().getShortestPath(myPosition, far).get(0);
					Random rando = new Random();
					if(rando.nextDouble() >= 0.5) {
						nextNode = ((FSMExploAgent) this.myAgent).getNextDest();
					}

				}
			}
			
			else if (((FSMExploAgent) this.myAgent).isDest_wumpusfound() && (myDestination2 != null)) {
				nextNode = ((FSMExploAgent) this.myAgent).getMyMap().getShortestPath(myPosition, myDestination2).get(0);
			}
			
			else if (lastPosition.equals(myPosition) && !((FSMExploAgent) this.myAgent).getPosition().contains(((FSMExploAgent) this.myAgent).getNextDest()) && !(myPosition.equals(((FSMExploAgent) this.myAgent).getNextDest()))) {
				//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : Wumpus found");
				((FSMExploAgent) this.myAgent).increaseWumpusCnt();
				nextNode = ((FSMExploAgent) this.myAgent).getNextDest();
			}




			/************************************************
			 * 
			 * Explore the Next Node
			 * 
			 ************************************************/

			else if (myPosition != null) {

				boolean dest_alea = ((FSMExploAgent) this.myAgent).getDestinationAlea();
				boolean dest_stench_direction = ((FSMExploAgent) this.myAgent).getDestinationStench();
				boolean dest_inside_stench = ((FSMExploAgent) this.myAgent).getDestinationInsideStench();
				boolean receive_stench_direction = !(((FSMExploAgent) this.myAgent).getStenchDirection().isEmpty());
				boolean receive_inside_stench =  !(((FSMExploAgent) this.myAgent).getInsideStench().isEmpty());

				/*****************************************************/


				/** Si on percoit des odeurs sur une case adjacente on y va en priorité **/

				/************************************************
				 * 
				 * If stench direction go there
				 * 
				 ************************************************/
				
				((FSMExploAgent) this.myAgent).setWumpusCnt(0);
				if ((NodeStench.size() == 1) && (((FSMExploAgent) this.myAgent).getStyle()==1)) {
					if (!(NodeStench.get(0).equals(myPosition))) {
						nextNode = NodeStench.get(0);
						//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : I'm following my stench direction node - " + nextNode);
					}
				}

				/************************************************
				 * 
				 * If approx wumpus go there
				 * 
				 ************************************************/

				//else if (((FSMExploAgent) this.myAgent).getStenchDirection().size()>1) {
				//	List<String> approx = ((FSMExploAgent) this.myAgent).getMyMap().getShortestPath(((FSMExploAgent) this.myAgent).getStenchDirection().get(0), ((FSMExploAgent) this.myAgent).getStenchDirection().get(1));
				//	String approxNode = approx.get(approx.size()/2);
				//	((FSMExploAgent) this.myAgent).setDestination(approxNode);
				//	nextNode = ((FSMExploAgent) this.myAgent).getMyMap().getShortestPath(myPosition, approxNode).get(0);
				//}

				/************************************************
				 * 
				 * If inside stench check around
				 * 
				 ************************************************/

				if ((NodeStench.size() > 1 && (nextNode==null)) && (((FSMExploAgent) this.myAgent).getStyle()==1))  {
					Collections.shuffle(NodeStench);
					for (String next : NodeStench) {
						if (!((FSMExploAgent) this.myAgent).getPosition().contains(next) && !(next.equals(myPosition))) {
							nextNode = next;
							//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : I'm following my inside stench node - " + nextNode);
							//((FSMExploAgent) this.myAgent).removeNextNodes(next);
							break;
						}
					}

				}


				/** Si on a pas observé d'odeurs autour de nous on regarde alors nos destinations en cours**/

				/************************************************
				 * 
				 * If destination already 
				 * 
				 ************************************************/



				/** If the destination is not random we keep going */
				if ( (nextNode==null) && ((FSMExploAgent) this.myAgent).getDestination()!=null && dest_alea == false && (dest_stench_direction==true || dest_inside_stench == true)) {
					//System.out.println(((FSMExploAgent) this.myAgent).getMyMap().getShortestPath(myPosition, ((FSMExploAgent) this.myAgent).getDestination()));
					//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : I'm following some stench node - " + ((FSMExploAgent) this.myAgent).getDestination());
					nextNode = ((FSMExploAgent) this.myAgent).getMyMap().getShortestPath(myPosition, ((FSMExploAgent) this.myAgent).getDestination()).get(0);
				}

				/** If the destination is random we only go there if we have received no other information */
				if ((nextNode==null) &&((FSMExploAgent) this.myAgent).getDestination()!=null && dest_alea == true && receive_stench_direction == false && receive_inside_stench == false) {
					//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : I don't know where to go I'm going somewhere random - " + ((FSMExploAgent) this.myAgent).getDestination());
					nextNode = ((FSMExploAgent) this.myAgent).getMyMap().getShortestPath(myPosition, ((FSMExploAgent) this.myAgent).getDestination()).get(0);
				}


				/************************************************
				 * 
				 * If we have received StenchDirection
				 * 
				 ************************************************/

				if (((nextNode==null) && !(((FSMExploAgent) this.myAgent).getStenchDirection().isEmpty())) && ((FSMExploAgent) this.myAgent).getStyle()==0) {
					for (String dest : ((FSMExploAgent) this.myAgent).getStenchDirection()){
						if (!(dest.equals(myPosition))) {
							((FSMExploAgent) this.myAgent).setDestination(dest);
							((FSMExploAgent) this.myAgent).setDestinationStench(true);
							((FSMExploAgent) this.myAgent).setDestinationInsideStench(false);
							((FSMExploAgent) this.myAgent).setDestinationAlea(false);
							((FSMExploAgent) this.myAgent).setDestinationInterblocage(false);
							//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : I'm following my stench direction node - " + dest);
							nextNode = ((FSMExploAgent) this.myAgent).getMyMap().getShortestPath(myPosition, dest).get(0);
							break;
						}
					}
				}


				/************************************************
				 * 
				 * If we have received InsideStech
				 * 
				 ************************************************/

				if ((!(((FSMExploAgent) this.myAgent).getInsideStench().isEmpty()) && (nextNode==null)) && (((FSMExploAgent) this.myAgent).getStyle()==0)){
					List<String> insideStench = ((FSMExploAgent) this.myAgent).getInsideStench();
					Collections.shuffle(insideStench);
					for (String dest : insideStench){
						if (!(dest.equals(myPosition))) {
							((FSMExploAgent) this.myAgent).setDestination(dest);
							((FSMExploAgent) this.myAgent).setDestinationInsideStench(true);
							((FSMExploAgent) this.myAgent).setDestinationStench(false);
							((FSMExploAgent) this.myAgent).setDestinationAlea(false);
							((FSMExploAgent) this.myAgent).setDestinationInterblocage(false);
							//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : I'm following my inside stench node - " + dest);
							nextNode = ((FSMExploAgent) this.myAgent).getMyMap().getShortestPath(myPosition, dest).get(0);
							break;
						}
					}
				}


				/************************************************
				 * 
				 * Else go somewhere if we have not received InsideStench or StenchDirection
				 * 
				 ************************************************/

				if((nextNode==null)) {
					String rando = null;
					while (rando == null || (rando.equals(myPosition))) {
						if (((FSMExploAgent) this.myAgent).getStyle()==0) {
							rando = ((FSMExploAgent) this.myAgent).getMyMap().getRandomOneNode();
						}
						else {
							rando = ((FSMExploAgent) this.myAgent).getMyMap().getRandomNode();
						}
					}
					((FSMExploAgent) this.myAgent).setDestination(rando);
					((FSMExploAgent) this.myAgent).setDestinationAlea(true);
					((FSMExploAgent) this.myAgent).setDestinationStench(false);
					((FSMExploAgent) this.myAgent).setDestinationInsideStench(false);
					((FSMExploAgent) this.myAgent).setDestinationInterblocage(false);
					//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : I'm following some random node - " + rando);
					nextNode = ((FSMExploAgent) this.myAgent).getMyMap().getShortestPath(myPosition, rando).get(0);

				}
			}

			/************************************************
			 * 
			 * Move to the position
			 * 
			 ************************************************/
			List<Couple<String,List<Couple<Observation,Integer>>>> l =((AbstractDedaleAgent)this.myAgent).observe();
			//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : I see around me - " + l);
			//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : I choosed to go on the node - " + nextNode);
			((FSMExploAgent) this.myAgent).setLastPosition(myPosition);
			((FSMExploAgent) this.myAgent).setNextDest(nextNode);
			((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);

			((FSMExploAgent) this.myAgent).cleanPosition();
			((FSMExploAgent) this.myAgent).cleanStenchDirection();
			((FSMExploAgent) this.myAgent).cleanInsideStench();
			((FSMExploAgent) this.myAgent).cleanNextNodes();
			break;


		}

	}
}