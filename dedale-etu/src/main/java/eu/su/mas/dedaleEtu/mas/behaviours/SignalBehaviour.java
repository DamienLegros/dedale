package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.FSMExploAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * The agent periodically share its map.
 * It blindly tries to send all its graph to its friend(s)  	
 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

 * @author hc
 *
 */
public class SignalBehaviour extends OneShotBehaviour{

	/**
	 * The agent periodically share its map.
	 * It blindly tries to send all its graph to its friend(s)  	
	 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

	 * @param a the agent
	 * @param period the periodicity of the behaviour (in ms)
	 * @param mymap (the map to share)
	 * @param receivers the list of agents to send the map to
	 */
	public SignalBehaviour(Agent myAgent) {
		super(myAgent);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;

	@SuppressWarnings({ "unchecked", "unlikely-arg-type" })
	@Override
	public void action() {

		if (((FSMExploAgent) this.myAgent).getMode()==0) {
			//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : Mode - EXPLORATION");
		}
		else {
			//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : Mode - HUNT");
		}

		AID[] receivers;

		boolean inboxEmpty;

		/************************************************
		 * 
		 * Send my position
		 * 
		 ************************************************/

		final ACLMessage msgPosition = new ACLMessage(ACLMessage.INFORM);
		msgPosition.setSender(this.myAgent.getAID());
		msgPosition.setProtocol("SHARE-POSITION");
		receivers = ((FSMExploAgent) this.myAgent).getServices("Explorer");
		for(int i = 0; i < receivers.length; i++) {
			if (!this.myAgent.getAID().getName().equals(receivers[i].getName())) msgPosition.addReceiver(receivers[i]);
		}
		msgPosition.setContent(((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
		//System.out.println(this.myAgent.getLocalName() + " ---> I send my position " + ((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
		((AbstractDedaleAgent) this.myAgent).sendMessage(msgPosition);




		/************************************************
		 * 
		 * Receive positions
		 * 
		 ************************************************/

		inboxEmpty = false; 
		do {
			final MessageTemplate ansPositionTemplate = MessageTemplate.MatchProtocol("SHARE-POSITION");			
			final ACLMessage ansPosition = this.myAgent.receive(ansPositionTemplate);
			if (ansPosition != null) {
				try {
					String positionReceived = (String) ansPosition.getContent();
					//System.out.println(this.myAgent.getLocalName() + " <--- I received a position of an explorer : " + positionReceived);
					if (positionReceived != null) {
						((FSMExploAgent) this.myAgent).addPosition(positionReceived);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				inboxEmpty = true;
			}
		} while (inboxEmpty == false);

		/************************************************
		 * 
		 * Send a signal if wumpus found
		 * 
		 ************************************************/
		//System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : Counter wumpus blocked - +" + ((FSMExploAgent) this.myAgent).getWumpusCnt());
		if ((!((FSMExploAgent) this.myAgent).getPosition().isEmpty()) && (((FSMExploAgent) this.myAgent).getWumpusCnt()>100)) {
			final ACLMessage msgWumpusFound = new ACLMessage(ACLMessage.INFORM);
			msgWumpusFound.setSender(this.myAgent.getAID());
			msgWumpusFound.setProtocol("SHARE-WUMPUSFOUND");
			receivers = ((FSMExploAgent) this.myAgent).getServices("Explorer");
			for(int i = 0; i < receivers.length; i++) {
				if (!receivers[i].equals(this.myAgent.getLocalName())) msgWumpusFound.addReceiver(receivers[i]);
			}
			msgWumpusFound.setContent("WUMPUS");
			//System.out.println(this.myAgent.getLocalName() + " ---> Go hunt somewhere else");
			//System.out.println(this.myAgent.getLocalName() + " ---> I've found a stench direction at the position : " + ((FSMExploAgent)this.myAgent).getOwnStenchDirection());
			((AbstractDedaleAgent) this.myAgent).sendMessage(msgWumpusFound);
		}

		/************************************************
		 * 
		 * Receive wumpus found set direction to get out
		 * 
		 ************************************************/	

		inboxEmpty = false; 
		do {
			final MessageTemplate ansWumpusFoundTemplate = MessageTemplate.MatchProtocol("SHARE-WUMPUSFOUND");			
			final ACLMessage ansWumpusFound = this.myAgent.receive(ansWumpusFoundTemplate);
			if (ansWumpusFound != null) {
				try {
					String wumpusFoundReceived = (String) ansWumpusFound.getContent();
					//System.out.println(this.myAgent.getLocalName() + " <--- I received a stench direction of the explorer : " + stenchDirectionReceived);
					if (wumpusFoundReceived != null) {
						if (((FSMExploAgent) this.myAgent).getWumpusCnt()<10) {
							((FSMExploAgent) this.myAgent).increaseGetoutCnt();
							System.out.println(this.myAgent.getLocalName() + " <--- I'm leaving you with your wumpus");
							((FSMExploAgent) this.myAgent).setDest_wumpusfound(true);
							String myPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
							String pos = null;
							while (pos == null || (pos.equals(myPosition))) {
								pos = ((FSMExploAgent) this.myAgent).getMyMap().getRandomNode();
							}
							((FSMExploAgent) this.myAgent).setDestination(pos);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				inboxEmpty = true; 
			}
		} while(inboxEmpty == false);

		/************************************************
		 * 
		 * Send a signal if stench direction found
		 * 
		 ************************************************/

		if (!(((FSMExploAgent) this.myAgent).getOwnStenchDirection()==null)) {
			final ACLMessage msgStenchDirection = new ACLMessage(ACLMessage.INFORM);
			msgStenchDirection.setSender(this.myAgent.getAID());
			msgStenchDirection.setProtocol("SHARE-STENCHDIRECTION");
			receivers = ((FSMExploAgent) this.myAgent).getServices("Explorer");
			for(int i = 0; i < receivers.length; i++) {
				if (!receivers[i].equals(this.myAgent.getLocalName())) msgStenchDirection.addReceiver(receivers[i]);
			}
			msgStenchDirection.setContent(((FSMExploAgent)this.myAgent).getOwnStenchDirection());
			//System.out.println(this.myAgent.getLocalName() + " ---> I've found a stench direction at the position : " + ((FSMExploAgent)this.myAgent).getOwnStenchDirection());
			((AbstractDedaleAgent) this.myAgent).sendMessage(msgStenchDirection);
		}

		/************************************************
		 * 
		 * Send a signal if I'm in the stench
		 * 
		 ************************************************/

		if (!(((FSMExploAgent) this.myAgent).getOwnInsideStench()==null)) {
			final ACLMessage msgInsideStench = new ACLMessage(ACLMessage.INFORM);
			msgInsideStench.setSender(this.myAgent.getAID());
			msgInsideStench.setProtocol("SHARE-INSIDESTENCH");
			receivers = ((FSMExploAgent) this.myAgent).getServices("Explorer");
			for(int i = 0; i < receivers.length; i++) {
				if (!this.myAgent.getAID().getName().equals(receivers[i].getName())) msgInsideStench.addReceiver(receivers[i]);
			}
			msgInsideStench.setContent(((FSMExploAgent)this.myAgent).getOwnInsideStench());
			//System.out.println(this.myAgent.getLocalName() + " ---> I'm in the stench : " + ((FSMExploAgent) this.myAgent).getOwnInsideStench());
			((AbstractDedaleAgent) this.myAgent).sendMessage(msgInsideStench);
		}

		/************************************************
		 * 
		 * Receive stench direction
		 * 
		 ************************************************/	

		inboxEmpty = false; 
		do {
			final MessageTemplate ansStenchDirectionTemplate = MessageTemplate.MatchProtocol("SHARE-STENCHDIRECTION");			
			final ACLMessage ansStenchDirection = this.myAgent.receive(ansStenchDirectionTemplate);
			if (ansStenchDirection != null) {
				try {
					String stenchDirectionReceived = (String) ansStenchDirection.getContent();
					//System.out.println(this.myAgent.getLocalName() + " <--- I received a stench direction of the explorer : " + stenchDirectionReceived);
					if (stenchDirectionReceived != null) {
						((FSMExploAgent) this.myAgent).addStenchDirection(stenchDirectionReceived);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				inboxEmpty = true; 
			}
		} while(inboxEmpty == false);

		/************************************************
		 * 
		 * Receive inside stench 
		 * 
		 ************************************************/	

		inboxEmpty = false; 
		do {
			final MessageTemplate ansInsideStenchTemplate = MessageTemplate.MatchProtocol("SHARE-INSIDESTENCH");			
			final ACLMessage ansInsideStench = this.myAgent.receive(ansInsideStenchTemplate);
			if (ansInsideStench != null) {
				try {
					String insideStenchReceived = (String) ansInsideStench.getContent();
					//System.out.println(this.myAgent.getLocalName() + " <--- I received an inside stench position of an explorer : " + insideStenchReceived);
					if (insideStenchReceived != null) {
						((FSMExploAgent) this.myAgent).addInsideStench(insideStenchReceived);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				inboxEmpty = true; 
			}
		} while(inboxEmpty == false);


		/************************************************
		 * 
		 * Send my map
		 * 
		 ************************************************/

		if (((!((FSMExploAgent) this.myAgent).getPosition().isEmpty()) && (((FSMExploAgent) this.myAgent).getWait()==0)) && (((FSMExploAgent) this.myAgent).getMode()==0)) {
			((FSMExploAgent) this.myAgent).setWait(1);
			final ACLMessage msgMap = new ACLMessage(ACLMessage.INFORM);
			msgMap.setSender(this.myAgent.getAID());
			msgMap.setProtocol("SHARE-MAP");
			receivers = ((FSMExploAgent) this.myAgent).getServices("Explorer");
			for(int i = 0; i < receivers.length; i++) {
				if (!this.myAgent.getAID().getName().equals(receivers[i].getName())) msgMap.addReceiver(receivers[i]);
			}
			try {
				msgMap.setContentObject(((FSMExploAgent) this.myAgent).getMyMapSerial());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println(this.myAgent.getLocalName() + " ---> I send my map");
			((AbstractDedaleAgent) this.myAgent).sendMessage(msgMap);
		}


		/************************************************
		 * 
		 * Receive maps and merge them
		 * 
		 ************************************************/

		if (((FSMExploAgent) this.myAgent).getMode()==0) {
			inboxEmpty = false; 
			do {
				final MessageTemplate ansMapTemplate = MessageTemplate.MatchProtocol("SHARE-MAP");			
				final ACLMessage ansMap = this.myAgent.receive(ansMapTemplate);
				if (ansMap != null) {
					SerializableSimpleGraph<String, MapAttribute> sgMapReceived=  null;
					try {
						sgMapReceived = (SerializableSimpleGraph<String, MapAttribute>)ansMap.getContentObject();
						//System.out.println(this.myAgent.getLocalName() + " <--- I received a map");
						//System.out.println(this.myAgent.getLocalName() + " <--- I received a map of an explorer : " + sgMapReceived);
						((FSMExploAgent) this.myAgent).getMyMap().mergeMap(sgMapReceived);
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				}
				else {
					inboxEmpty = true; 
				}
			} while(inboxEmpty == false);

		}
	}
}
