package workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TempWTask implements Serializable
{
	private final String taskId;  // ID of this task
	private int taskWorkFlowId;   // The ID of the workflow where this task belongs to
	private final double runTime; // The run time of the task
	
	private List<Constraint> parentTaskList;   //The set of immediate predecessors using ID
	private List<Constraint> sucessorTaskList; //The set of immediate successors using ID
	
	public TempWTask(String taskId, int workflowId, double runTime)
	{	
		this.taskId = taskId;
		this.taskWorkFlowId = workflowId;
		this.runTime = runTime;				
		parentTaskList = new ArrayList<Constraint>();   //Initialize
		sucessorTaskList = new ArrayList<Constraint>(); //Initialize
	}
	
	//Obtain the ID of the task
	public String getTaskId(){return taskId;}
	
	//Obtain the ID of the workflow
	public int getTaskWorkFlowId(){return taskWorkFlowId;}
	//Set the ID of the workflow
	public void setTaskWorkFlowId(int workFlowId)
	{
		this.taskWorkFlowId = workFlowId;
	}
	
	public double getTaskRunTime(){return runTime;}
	
	public List<Constraint> getParentTaskList(){return parentTaskList;}
	
	public List<Constraint> getSuccessorTaskList(){return sucessorTaskList;}
}
