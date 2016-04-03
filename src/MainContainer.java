package main;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class MainContainer
{
	public static String properties = "properties";
	
	public static void main(String[] args)
	{
		Runtime runtime = Runtime.instance();
		Profile profile = null;
		
		try
		{
			profile = new ProfileImpl(properties);
			AgentContainer container = runtime.createMainContainer(profile);

			AgentController environment = container.createNewAgent("Environment", 
			"main.EnvironmentAgent", null);
			environment.start();
			
			AgentController simulation = container.createNewAgent("Simulation", 
			"main.SimulationAgent", null);
			simulation.start();
		}
		catch(Exception exception) 
		{
		}
	}
}
