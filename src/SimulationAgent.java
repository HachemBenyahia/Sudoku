package main;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.StringWriter;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SimulationAgent extends Agent
{	
	String m_grid[][];
	
	public String intersection(String x, String y)
	{
		String res = "";
		
		for(int i = 0 ; i < x.length() ; i++)
			for(int j = 0 ; j < y.length() ; j++)
			{
				if(x.charAt(i) == y.charAt(j))
				{
					res += x.charAt(i);
					break;
				}
			}
		
		return res;
	}
	
	public String intersection(String x, String y, String z)
	{
		String res = "";
		
		res = intersection(x, y);
		res = intersection(res, z);
		
		return res;
	}
	
	public void setGrid(String grid[][])
	{
		for(int i = 0 ; i < 9 ; i++)
			for(int j = 0 ; j < 9 ; j++)
				m_grid[i][j] = grid[i][j];
	}
	
	public String getLinearizedGrid()
	{
		String res = "";
		
		for(int i = 0 ; i < 9 ; i++)
			for(int j = 0 ; j < 9 ; j++)
				res += (m_grid[i][j] + " ");
		
		return res;
	}
	
	protected void setup()
	{
		addBehaviour(new SimulationToEnvironmentBehaviour(this, 1000));
		addBehaviour(new SimulationToAnalysisBehaviour());
	}
}

class SimulationToEnvironmentBehaviour extends TickerBehaviour 
{
	SimulationAgent myAgent = (SimulationAgent)this.myAgent;
	
	public SimulationToEnvironmentBehaviour(Agent a, long period) 
	{
		super(a, period);
	}
	
	public void onTick()
	{
		String content = "{grid : " + myAgent.getLinearizedGrid() + "}";

		ACLMessage message = new ACLMessage(ACLMessage.INFORM);
		message.addReceiver(new AID("Environment", AID.ISLOCALNAME));
		message.setContent(content);
		myAgent.send(message);	
	}
}

class SimulationToAnalysisBehaviour extends Behaviour
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