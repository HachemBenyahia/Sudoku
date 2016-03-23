package main;

import java.io.BufferedReader;
import java.io.FileReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

/**
 * La classe EnvironmentAgent représente l'agent d'environnement.
 */
public class EnvironmentAgent extends Agent
{
	/**
	 * La grille d'origine, elle ne change pas tout le long de l'exécution.
	 */
	int m_originalGrid[][];
	/**
	 * La grille actuelle, actualisée périodiquement par l'agent de simulation.
	 */
	String m_currentGrid[][];
	
	/**
	 * Méthode qui permet de mettre à jour {@link EnvironmentAgent#m_currentGrid} 
	 * via une grille linéarisée.
	 * @param linearGrid La grille linéarisée de référence.
	 */
	public void updateGrid(String linearGrid)
	{
		String[] parts = linearGrid.split(" ");
		
		int j = 0;
		
		for(int i = 0 ; i < 9 * 9 ; i++)
		{
			m_currentGrid[i][j] = parts[i];
			
			j++;
			
			if(j == 8)
				j = 0;
		}
	}
	
	public int[][] getStandardIntGrid()
	{
		int res[][] = {{0}};
		
		for(int i = 0 ; i < 9 ; i++)
			for(int j = 0 ; j < 9 ; j++)
			{
				if(m_currentGrid[i][j].length() > 1)
					res[i][j] = 0;
				else
					res[i][j] = Integer.parseInt(m_currentGrid[i][j]);
			}
		
		return res;
	}
	
	public void loadOriginalGrid(String path)
	{
		try 
		{
			BufferedReader br = new BufferedReader(new FileReader(path));
		    String linearGrid = "";
		    String line = br.readLine();

		    while (line != null)
		    {
		        linearGrid += (line + " ");
		        line = br.readLine();
		    }
		    
		    br.close();
		    
		    updateGrid(linearGrid);
		} 
		catch(Exception exception)
		{
		}
	}
	
	public void printGrid()
	{
		int currentGrid[][] = getStandardIntGrid();
		
		for(int i = 0 ; i < 9 ; i++)
		{
			for(int j = 0 ; j < 9 ; j++)
				System.out.print(currentGrid[i][j]);
			
			System.out.println();
		}
	}
	
	protected void setup()
	{
		addBehaviour(new ReceiveFromSimulationEnvironmentBehaviour());
		addBehaviour(new ConsoleBehaviour());
	}
}

class ConsoleBehaviour extends Behaviour
{
	EnvironmentAgent myAgent = ((EnvironmentAgent)this.myAgent);
	
	public void action()
	{
		ACLMessage message = this.myAgent.receive();

		if(message != null)
		{
			String[] args = message.getContent().split(" ");
			
			switch(args[0])
			{
				case "read" :
					if(args[1] != "")
						myAgent.loadOriginalGrid(args[1]);
				break;
				
				case "print" :
					myAgent.printGrid();
				break;
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

class ReceiveFromSimulationEnvironmentBehaviour extends Behaviour 
{
	@Override
	public void action()
	{
		ACLMessage message = this.myAgent.receive();

		if(message != null)
		{
			ObjectMapper mapper = new ObjectMapper();
			
			try 
			{
				JsonNode jRootNode = mapper.readValue(message.getContent(), JsonNode.class);
				String linearGrid = jRootNode.path("grid").textValue();
				((EnvironmentAgent)this.myAgent).updateGrid(linearGrid);
			}
			catch(Exception ex) 
			{	
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