package main;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

public class AnalysisAgent extends Agent
{
	protected void setup()
	{
		addBehaviour(new AnalysisToSimulationBehaviour());
	}
}

class AnalysisToSimulationBehaviour extends Behaviour 
{
	@Override
	public void action()
	{
		ACLMessage message = this.myAgent.receive();

		if(message != null)
		{
		}
		else
			block();
	}
		
	@Override
	public boolean done() 
	{
		return false;
	}
}