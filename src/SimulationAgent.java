package main;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;

import java.io.StringWriter;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SimulationAgent extends Agent
{	
	String m_grid[][];
	AID analysisAgents[];
	String analysisAgentsName[];
	int m_counter = 0;
	String[] m_strings;
	
	// quand l'agent de simulation sera créé, son constructeur va créer tous les agents d'analyses et stocker leurs AIDs et leurs noms
	// dans un tableau
	SimulationAgent()
	{
		String name = "";
		
		for(int i = 0 ; i < 27 ; i++)
		{
			name = "AnalysisAgent" + Integer.toString(i);
			
			try
			{
				this.getContainerController().createNewAgent(name, "main.AnalysisAgent", null).start();
				analysisAgents[i] = new AID(name, AID.ISLOCALNAME);
				analysisAgentsName[i] = name;
			}
			catch(Exception ex)
			{
			}
		}
	}
	
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
	
	// on considère la convention suivante : toutes les lignes, puis les colonnes, puis les blocs
	// de telle sorte que l'agent 0 s'occupe de la 1ère ligne, l'agent 9 la première colonne et
	// l'agent 18 le premier bloc ; la fonction rend les parties de la grille dans cet ordre
	public String[] GetGridParts()
	{
		String res[] = {""}; // res.length = 27 par construction
		
		// les lignes : res[0] à res[8]
		for(int i = 0 ; i < 9 ; i++)	
			for(int j = 0 ; j < 9 ; j++)
				res[i] += (m_grid[i][j] + " ");
		
		// les colonnes : res[9] à res[17]
		for(int i = 0 ; i < 9 ; i++)			
			for(int j = 0 ; j < 9 ; j++)
				res[i] += (m_grid[j][i] + " ");
		
		// les blocs : res[18] à res[26]
		// 1er bloc
		for(int i = 0 ; i < 3 ; i++)	
			for(int j = 0 ; j < 3 ; j++)
				res[i] += (m_grid[i][j] + " ");
		
		// 2e bloc
		for(int i = 0 ; i < 3 ; i++)	
			for(int j = 3 ; j < 6 ; j++)
				res[i] += (m_grid[i][j] + " ");
		
		// 3e bloc
		for(int i = 0 ; i < 3 ; i++)	
			for(int j = 6 ; j < 9 ; j++)
				res[i] += (m_grid[i][j] + " ");
		
		// 4e bloc
		for(int i = 3 ; i < 6 ; i++)	
			for(int j = 0 ; j < 3 ; j++)
				res[i] += (m_grid[i][j] + " ");
		
		// 5e bloc
		for(int i = 3 ; i < 6 ; i++)	
			for(int j = 3 ; j < 6 ; j++)
				res[i] += (m_grid[i][j] + " ");
		
		// 6e bloc
		for(int i = 3 ; i < 6 ; i++)	
			for(int j = 6 ; j < 9 ; j++)
				res[i] += (m_grid[i][j] + " ");
		
		// 7e bloc
		for(int i = 6 ; i < 9 ; i++)	
			for(int j = 0 ; j < 3 ; j++)
				res[i] += (m_grid[i][j] + " ");
		
		// 8e bloc
		for(int i = 6 ; i < 9 ; i++)	
			for(int j = 3 ; j < 6 ; j++)
				res[i] += (m_grid[i][j] + " ");
		
		// 9e bloc
		for(int i = 6 ; i < 9 ; i++)	
			for(int j = 6 ; j < 9 ; j++)
				res[i] += (m_grid[i][j] + " ");
		
		return res;
	}
	
	protected void setup()
	{
		addBehaviour(new SimulationToEnvironmentBehaviour(this, 5000));
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
	SimulationAgent myAgent = (SimulationAgent)this.myAgent;

	@Override
	public void action()
	{
		String[] parts = myAgent.GetGridParts();
		
		for(int i = 0 ; i < 27 ; i++)
		{
			String content = "{string : " + parts[i] + "}";
			
			ACLMessage message = new ACLMessage(ACLMessage.INFORM);
			message.addReceiver(myAgent.analysisAgents[i]);
			message.setContent(content);
			myAgent.send(message);
		}
		
		myAgent.m_counter = 27;
	}
	
	public boolean done()
	{
		return false;
	}
}

class ReceiveFromAnalysisSimulationBehaviour extends Behaviour
{	
	SimulationAgent myAgent = (SimulationAgent)this.myAgent;

	@Override
	public void action()
	{
		ACLMessage message = myAgent.receive();
		
		if(message != null)
		{
			ObjectMapper mapper = new ObjectMapper();
			
			try 
			{
				JsonNode jRootNode = mapper.readValue(message.getContent(), JsonNode.class);
				String string = jRootNode.path("string").textValue();
				
				switch(message.getSender().getLocalName())
				{
					case "AnalysisAgent0" :
						myAgent.m_strings[0] = string;
						
						// mal aux yeux, je continuerai demain
					break;
				}
				
				myAgent.m_counter++;
			}
			catch(Exception ex) 
			{	
			}
		}
		else
			block();
	}
	
	public boolean done()
	{
		return false;
	}
}