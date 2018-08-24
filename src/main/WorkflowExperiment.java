package main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import share.StaticfinalTags;
import workflow.Workflow;
import workflowScheduleAlgorithm.ROSA_ProposedAlgorithm;

public class WorkflowExperiment 
{
	private static List<Workflow> workflowList; //The set of workflows
	private static int workflowNum = 0; //The number of workflows
	
	public static void main(String[] args) throws Exception 
	{
		workflowNum = StaticfinalTags.workflowNum;
		workflowList = getWorkflowListFromFile("producedWorkflow.txt");// Obtain the set of workflows				
		if(StaticfinalTags.choose == 0)
		{
			StaticfinalTags.confidency = 0.9;
			
			ROSA_ProposedAlgorithm ROSA = new ROSA_ProposedAlgorithm();
			ROSA.submitWorkflowList(workflowList);
			ROSA.scheduleDynamicWorkflowToSiROSA();
					
			workflowList.clear();
			Runtime.getRuntime().gc(); //Clear  
		}	
		
	}
	
	/**Obtain the set of workflows
	 * @throws IOException 
	 * @throws ClassNotFoundException */
	public static List<Workflow> getWorkflowListFromFile(String filename) throws IOException, ClassNotFoundException
	{
		List<Workflow> w_List = new ArrayList<Workflow>();
		Workflow w = null;
		FileInputStream fi = new FileInputStream(filename);
		ObjectInputStream si = new ObjectInputStream(fi);
		try
		{
			for(int i=0; i<workflowNum; i++)
			{
				w = (Workflow)si.readObject();
				w_List.add(w);
			}			
			si.close();
		}catch(IOException e){System.out.println(e.getMessage());}		
		return w_List;
	}
}
