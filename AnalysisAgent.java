package main;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

public class AnalysisAgent extends Agent
{
	int numbers[];
	int possibles[][];
	
	protected void setup()
	{
		addBehaviour(new AnalysisAgentBehaviour());
	}
}

class AnalysisAgentBehaviour extends Behaviour 
{
	@Override
	public void action()
	{
		ACLMessage message = this.myAgent.receive();

		if(message != null)
		{
			String string = message.getContent();
			
			for(int i = 0 ; i < 9 ; i++)
			{
				((AnalysisAgent)this.myAgent).numbers[i] = string.charAt(i);
				
				
			}
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