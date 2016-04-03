package main;

import java.io.BufferedReader;
import java.io.FileReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * La classe {@link EnvironmentAgent} repr�sente l'agent d'environnement.
 */
public class EnvironmentAgent extends Agent
{
	/**
	 * La grille d'origine, elle ne change pas tout le long de l'ex�cution.
	 */
	int m_originalGrid[][];
	/**
	 * La grille actuelle, actualis�e p�riodiquement par l'agent de simulation.
	 */
	String m_currentGrid[][];
	
	/**
	 * Permet de mettre � jour {@link EnvironmentAgent#m_currentGrid} 
	 * via une grille lin�aris�e.
	 * @param linearGrid La grille lin�aris�e de r�f�rence.
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
	
	/**
	 * Convertit la grille {@link EnvironmentAgent#m_currentGrid} en grille standard 
	 * (c'est-�-dire une grille d'entiers de 0 � 9).
	 * @return Retourne la grille standardis�e.
	 */
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
	
	/**
	 * Charge la grille d'origine dans {@link EnvironmentAgent#m_currentGrid} � partir d'un
	 * fichier texte.
	 * @param path Le chermin vers la grille.
	 */
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
	
	/**
	 * Affiche la grille standardis�e.
	 */
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
	
	/**
	 * Renvoit vrai si la grille est r�solue et faux sinon.
	 */
	public boolean IsOver()
	{
		int currentGrid[][] = getStandardIntGrid();
		boolean over = true;
		
		for(int i = 0 ; i < 9 ; i++)
			for(int j = 0 ; j < 9 ; j++)
				if(currentGrid[i][j] == 0)
					over = false;
		
		return over;
	}
	
	public String getLinearizedGrid()
	{
		String res = "";
		
		for(int i = 0 ; i < 9 ; i++)
			for(int j = 0 ; j < 9 ; j++)
				res += (m_currentGrid[i][j] + " ");
		
		return res;
	}
	
	protected void setup()
	{
		addBehaviour(new ReceiveFromConsoleEnvironmentBehaviour());
		addBehaviour(new ReceiveFromSimulationEnvironmentBehaviour());
	}
}

/**
 * Behaviour pour les requ�tes venant de la console (chargement d'une grille, acffichage de la
 * grille actuelle, etc).
 */
class ReceiveFromConsoleEnvironmentBehaviour extends CyclicBehaviour
{
	EnvironmentAgent myAgent = (EnvironmentAgent)this.myAgent;
	
	public void action()
	{
		ACLMessage message = myAgent.receive();

		if(message != null)
		{
			String[] args = message.getContent().split(" ");
			
			switch(args[0])
			{
				case "read" :
					if(args[1] != "")
					{
						myAgent.loadOriginalGrid(args[1]);
						
						// envoi de la grille � l'agent de simulation
						String content = "{grid : " + myAgent.getLinearizedGrid() + "}";

						ACLMessage newGridMessage = new ACLMessage(ACLMessage.INFORM);
						newGridMessage.addReceiver(new AID("Simulation", AID.ISLOCALNAME));
						newGridMessage.setContent(content);
						myAgent.send(newGridMessage);	
					}
				break;
				
				case "print" :
					myAgent.printGrid();
				break;
			}
		}
		else
			block();
	}
}

/**
 * Behaviour pour les mises � jour venant du {@link SimulationAgent}.
 */
class ReceiveFromSimulationEnvironmentBehaviour extends CyclicBehaviour 
{
	EnvironmentAgent myAgent = (EnvironmentAgent)this.myAgent;
	
	@Override
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
				
				// test si la grille est compl�t�e, c'est-� dire qu'il n'y a pas de 0 dans la grille standardis�e
				if(myAgent.IsOver())
				{
					System.out.println("------------ Grid solved ------------");
					myAgent.printGrid();
					
					//System.exit(0);
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