package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * This  behaviour do the setup for an Agent in Yellow Pages
 * @author dl
 *
 */
public class YellowSetupBehaviour extends SimpleBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -674588737131272988L;
	
	private boolean finished = false;
	
	private String behaviour = null;

	/**
	 * Setup the yellow pages for an agent
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	
	public YellowSetupBehaviour (Agent myAgent, String behaviour) {
		super(myAgent);
		this.behaviour = behaviour;
	}

	@Override
	public void action() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(this.myAgent.getAID()); // The agent AID
		ServiceDescription sd = new ServiceDescription();
		sd.setType(this.behaviour); // You have to give a name to each service your agent offers
		sd.setName(this.myAgent.getLocalName());//(local)name of the agent
		dfd.addServices(sd);
		//Register the service
		try {
			DFService.register(this.myAgent,dfd);
			System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : " + this.behaviour);
		} catch (FIPAException fe) {
			fe.printStackTrace(); 
		}
		finished = true;
	}

	@Override
	public boolean done() {
		return finished;
	}
}