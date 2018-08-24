package workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TempWorkflow implements Serializable
{
	private final String workflowName;	
	private List<TempWTask> taskList; //The set of tasks in this workflow
	
	public TempWorkflow(String name)
	{
		this.workflowName = name;		
		this.taskList = new ArrayList<TempWTask>(); //Initialize a list to record the set of tasks
	}
	
	//Obtain the name of the workflow
	public String getWorkflowName(){return workflowName;}
	
	//Obtain the set of tasks in this workflow
	public List<TempWTask> getTaskList(){return taskList;}
	public void setTaskList(List<TempWTask> list)
	{
		this.taskList = list;
	}
}
