package main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

public class AnalysisAgent extends Agent
{
	String[] parts; // parts.length vaut obligatoirement 9 par construction
	
	public String DiffStrings(String toKeep, String toLose)
	{
		String res = "";
		
		for(int i = 0 ; i < toKeep.length() ; i++)
		{
			if(toLose.contains(String.valueOf(toKeep.charAt(i))))
				break;
			else
				res += toKeep.charAt(i);
		}
		
		// res = toKeep - toLose
		return res;
	}
	
	// premier algo à appeler, vrai s'ils y avait au moins un 0, faux sinon
	// si vrai, renvoyer le message avec "parts"
	public boolean InitAlgo()
	{
		boolean modified = false;
		
		for(int i = 0 ; i < 9 ; i++)
		{
			// s'il y a au moins un 0 (donc 1ère itération de l'agent de simulation)
			if(parts[i] == "0")
			{
				String numbers = "";
				
				for(int j = 0 ; j  < 9 ; j++)
					if(parts[j] != "")
						numbers += parts[j];
				
				numbers = this.DiffStrings("123456789", numbers);
				
				for(int j = 0 ; j < 9 ; j++)
					if(parts[j] == "0")
						parts[j] = numbers;
				
				modified = true;
			}
		}
		
		return modified;
	}
	
	// 2e algo à appeler, vrai s'il y avait une valeur isolée qui se trouvait dans d'autres
	// cases, par exemple 235 2 4 1 ... => ici on voit que 2 doit être retiré de "235", donc on
	// aura 35 2 4 1
	public boolean FirstSecondThirdAlgo()
	{
		boolean modified = false;
		
		for(int i = 0 ; i < 9 ; i++)
			if(parts[i].length() == 1)
				for(int j = 0 ; j  < 9 ; j++)
					if(parts[j].contains(String.valueOf(parts[i])) && (j != i))
					{
						parts[j] = this.DiffStrings(parts[j], parts[i]);
						
						modified = true;
					}

		return modified;
	}
	
	public int[] CountValueInArray(String string, String[] array)
	{
		int count = 0, position = 0;
		
		for(int i = 0 ; i < array.length ; i++)
			if(string.equals(array[i]))
			{
				count++;
				position = i;
			}
		
		return new int[] {count, position};
	}
	
	// 3e algo à appeler, qui correspond à la condition 4 du sujet de TP (si 2 cases exactement on les 2 mêmes possibilités, ces
	// possibilités sont exclues des autres cases)
	// renvoie vrai s'il a changé quelque chose, faux sinon
	public boolean FourthAlgo()
	{
		boolean modified = false;
		
		for(int i = 0 ; i < 9 ; i++)
		{
			int count = this.CountValueInArray(parts[i], parts)[0];
			int position = this.CountValueInArray(parts[i], parts)[1];
			
			if(count == 2)
				for(int j = 0 ; j < 9 ; j++)
					if(parts[j].contains(String.valueOf(parts[i])) && (j != i) && (j != position))
					{
						parts[j] = this.DiffStrings(parts[j], parts[i]);
						
						modified = true;
					}
		}
		
		return modified;
	}
	
	public String PartsToString()
	{
		String res = "";
		
		for(int i = 0 ; i < 9 ; i++)
		{
			res += parts[i];
			
			if(i != 8)
				res += " ";
		}
		
		return res;
	}
	
	protected void setup()
	{
		addBehaviour(new ReceiveFromSimulationAnalysisBehaviour());
	}
}

class ReceiveFromSimulationAnalysisBehaviour extends Behaviour 
{
	AnalysisAgent myAgent = (AnalysisAgent)this.myAgent;
	
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
				
				// string est de la forme "523 10 25 13 2 ..."
				myAgent.parts = string.split(" ");
				
				// traitement de la chaîne : le premier qui modifie la chaine fait sortir du if..else
				if(myAgent.InitAlgo())
				{
				}
				else if(myAgent.FirstSecondThirdAlgo())
				{
				}
				else if(myAgent.FourthAlgo())
				{
				}
				
				// on renvoit le message modifié
				String content = "{string : " + myAgent.PartsToString() + "}";

				ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
			    reply.addReceiver(message.getSender());
			    reply.setContent(content);
			    myAgent.send(reply);
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