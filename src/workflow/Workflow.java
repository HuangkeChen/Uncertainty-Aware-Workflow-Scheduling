package workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Workflow implements Serializable, Cloneable
{
	private int workflowId; // The ID of the workflow
	private final String workflowName; // The name of the workflow
	private int arrivalTime; // The arrival time of the workflow
	private double makespan; // The makespan of the workflow based on the base execution time of all its tasks
	private int deadline; // The deadline of the workflow
	
	private List<WTask> taskList; //The set of tasks
	private boolean startedFlag;  //Whether this workflow start to be allocated
	private double finishTime;       //The finish time the workflow
	private boolean successfulOrNot; //Whether this workflow is finished before deadline
	
	public Workflow(int workFlowId, String name, int arrivalTime, double makespan, int deadline)
	{
		this.workflowId = workFlowId;
		this.workflowName = name;
		this.arrivalTime = arrivalTime;
		this.makespan = makespan;
		this.deadline = deadline;
		
		this.taskList = new ArrayList<WTask>(); 
		this.startedFlag = false;
		this.finishTime = -1;
		this.successfulOrNot = false;
	}
		
	/**Obtain the ID of this workflow*/
	public int getWorkflowId(){return workflowId;}
	/**Set the ID of this workflow*/
	public void setWorkflowId(int workflowId)
	{
		this.workflowId = workflowId;
	}
	
	/**Obtain the nameof this workflow*/
	public String getWorkflowName(){return workflowName;}
	
	/**Obtain the arrival time of this workflow*/
	public int getArrivalTime(){return arrivalTime;}
	/**Set the arrival time of this workflow*/
	public void setArrivalTime(int arrivalTime)
	{
		this.arrivalTime = arrivalTime;
	}
	
	/**Obtain the makespan of this workflow*/
	public double getMakespan(){return makespan;}
	/**Set the makespan of this workflow*/
	public void setMakespan(double makespan)
	{
		this.makespan = makespan;
	}
	
	/**Obtain the deadline of this workflow*/
	public int getDeadline(){return deadline;}
	/**Set the deadline of this workflow*/
	public void setDeadline(int deadline)
	{
		this.deadline = deadline;
	}
	
	/**Obtain the set of tasks in this workflow*/
	public List<WTask> getTaskList(){return taskList;}
	/**Set the set of tasks in this workflow*/
	public void setTaskList(List<WTask> list)
	{
		this.taskList = list;
	}
	
	public boolean getStartedFlag(){return startedFlag;}
	public void setStartedFlag(boolean startedFlag)
	{
		this.startedFlag = startedFlag;
	}
	
	/**Obtain the finish time of this workflow*/
	public double getFinishTime(){return finishTime;}
	/**Set the finish time of this workflow*/
	public void setFinishTime(double finishTime)
	{
		this.finishTime = finishTime;
	}
		
	public boolean getSuccessfulOrNot(){return successfulOrNot;}
	public void setSuccessfulOrNot(boolean successfulOrNot)
	{
		this.successfulOrNot = successfulOrNot;
	}
}