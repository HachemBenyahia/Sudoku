package main;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;

import java.io.StringWriter;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SimulationAgent extends Agent
{	
	String m_grid[][];
	AID analysisAgents[];
	int m_counter = 0;
	String[] m_strings;
	boolean treated = true;
	
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
	
	// reconstitue 3 grilles 9x9 à partir des analyses des 27 agents de lignes, colonnes et blocs
	public String[][][] GetGridsFromParts()
	{
		String first[][] = {{""}}, second[][] = {{""}}, third[][] = {{""}}, string[] = {""};
		int k = 0;

		// lignes
		for(int i = 0 ; i < 9 ; i++)
		{
			string = m_strings[i].split(" ");
			
			for(int j = 0 ; j < 9 ; j++)
				first[i][j] = string[j];
		}
		
		// colonnes
		for(int i = 9 ; i < 18 ; i++)
		{
			string = m_strings[i].split(" ");
			
			for(int j = 0 ; j < 9 ; j++)
				second[j][i - 9] = string[j];
		}
		
		// blocs
		// 1er bloc
		string = m_strings[18].split(" ");
		for(int i = 0 ; i < 3 ; i++)
			for(int j = 0 ; j < 3 ; j++)
			{
				third[i][j] = string[k];
				k++;
			}
		
		// 2e bloc
		string = m_strings[19].split(" ");
		k = 0;
		for(int i = 0 ; i < 3 ; i++)
			for(int j = 3 ; j < 6 ; j++)
			{
				third[i][j] = string[k];
				k++;
			}
		
		// 3e bloc
		string = m_strings[20].split(" ");
		k = 0;
		for(int i = 0 ; i < 3 ; i++)
			for(int j = 6 ; j < 9 ; j++)
			{
				third[i][j] = string[k];
				k++;
			}
		
		// 4e bloc
		string = m_strings[21].split(" ");
		k = 0;
		for(int i = 3 ; i < 6 ; i++)
			for(int j = 0 ; j < 3 ; j++)
			{
				third[i][j] = string[k];
				k++;
			}
		
		// 5e bloc
		string = m_strings[22].split(" ");
		k = 0;
		for(int i = 3 ; i < 6 ; i++)
			for(int j = 3 ; j < 6 ; j++)
			{
				third[i][j] = string[k];
				k++;
			}
		
		// 6e bloc
		string = m_strings[23].split(" ");
		k = 0;
		for(int i = 3 ; i < 6 ; i++)
			for(int j = 6 ; j < 9 ; j++)
			{
				third[i][j] = string[k];
				k++;
			}
		
		// 7e bloc
		string = m_strings[24].split(" ");
		k = 0;
		for(int i = 6 ; i < 9 ; i++)
			for(int j = 0 ; j < 3 ; j++)
			{
				third[i][j] = string[k];
				k++;
			}
		
		// 8e bloc
		string = m_strings[25].split(" ");
		k = 0;
		for(int i = 6 ; i < 9 ; i++)
			for(int j = 3 ; j < 6 ; j++)
			{
				third[i][j] = string[k];
				k++;
			}
		
		// 9e bloc
		string = m_strings[26].split(" ");
		k = 0;
		for(int i = 6 ; i < 9 ; i++)
			for(int j = 6 ; j < 9 ; j++)
			{
				third[i][j] = string[k];
				k++;
			}
		
		return new String[][][] {first, second, third};
	}
	
	public int GetSenderId(AID aid)
	{
		int i;
		
		for(i = 0 ; i < 27 ; i++)
			if(aid == this.analysisAgents[i])
				return i;
		
		return -1;
	}
	
	public void updateGrid(String linearGrid)
	{
		String[] parts = linearGrid.split(" ");
		
		int j = 0;
		
		for(int i = 0 ; i < 9 * 9 ; i++)
		{
			m_grid[i][j] = parts[i];
			
			j++;
			
			if(j == 8)
				j = 0;
		}
	}
	
	protected void setup()
	{
		addBehaviour(new ReceiveFromAnalysisSimulationBehaviour());
		addBehaviour(new SimulationToEnvironmentBehaviour(this, 5000));
		addBehaviour(new SimulationToAnalysisBehaviour(this, 3000));
		addBehaviour(new ReceiveFromEnvironmentSimulationBehaviour());
	}
}

class ReceiveFromEnvironmentSimulationBehaviour extends CyclicBehaviour
{
	SimulationAgent myAgent = (SimulationAgent)this.myAgent;
	
	public void action()
	{
		ACLMessage message = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));

		if(message != null)
		{
			ObjectMapper mapper = new ObjectMapper();
			
			try 
			{
				JsonNode jRootNode = mapper.readValue(message.getContent(), JsonNode.class);
				String linearGrid = jRootNode.path("grid").textValue();
				myAgent.updateGrid(linearGrid);
			}
			catch(Exception ex) 
			{
			}
		}
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

class SimulationToAnalysisBehaviour extends TickerBehaviour
{	
	public SimulationToAnalysisBehaviour(Agent a, long period) 
	{
		super(a, period);
	}

	SimulationAgent myAgent = (SimulationAgent)this.myAgent;
	
	@Override
	public void onTick()
	{
		String[] parts = myAgent.GetGridParts();
	
		if((myAgent.m_counter == 0) && (myAgent.treated == true))
		{
			for(int i = 0 ; i < 27 ; i++)
			{
				String content = "{string : " + parts[i] + "}";
				
				ACLMessage message = new ACLMessage(ACLMessage.AGREE);
				message.addReceiver(myAgent.analysisAgents[i]);
				message.setContent(content);
				myAgent.send(message);
				
				myAgent.m_counter++;
			}
			
			myAgent.treated = false;
		}
	}
}

class ReceiveFromAnalysisSimulationBehaviour extends CyclicBehaviour
{	
	SimulationAgent myAgent = (SimulationAgent)this.myAgent;

	@Override
	public void action()
	{
		ACLMessage message = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.AGREE));
		
		if(message != null)
		{
			ObjectMapper mapper = new ObjectMapper();
			
			try 
			{
				JsonNode jRootNode = mapper.readValue(message.getContent(), JsonNode.class);
				String string = jRootNode.path("string").textValue();
				
				int i = myAgent.GetSenderId(message.getSender());
				
				myAgent.m_strings[i] = string;
				
				myAgent.m_counter--;
				
				// si on a reçu tous les messages qu'on a envoyé aux agents d'analyses, on peut updater la grille
				if((myAgent.m_counter == 0) && (myAgent.treated == false))
				{
					String[][] newGrid = {{""}}, first, second, third;
					String[][][] grids = myAgent.GetGridsFromParts();
					
					first = grids[0];
					second = grids[1];
					third = grids[2];
					
					for(int k = 0 ; k < 9 ; k++)
						for(int j = 0 ; j < 9 ; j++)
							newGrid[k][j] = myAgent.intersection(first[k][j], second[k][j], third[k][j]);
					
					myAgent.setGrid(newGrid);
					
					myAgent.treated = true;
				}
			}
			catch(Exception ex) 
			{	
			}
		}
		else
			block();
	}
}