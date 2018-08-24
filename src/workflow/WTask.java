package workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import vmInfo.SaaSVm;

public class WTask implements Serializable
{
	private final String taskId; //The ID of this task
	private int taskWorkFlowId; //The ID of the workflow, where this task belongs to
	
	private final double baseExecutionTime; //The base execution time of this task
	private double baseStartTime; //Is used to calculate the makespan of workflow
	private double baseFinishTime; 
	
	private double realBaseExecutionTime; //The real base execution time of the task, which is used to update the status of the system and cannot be used when scheduling workflows
	
	private double realExecutionTime; //The real execution time of task, since one task has different execution time on different service instances
	private double executionTimeWithConfidency; // The \alpha-quantile of the execution time of task
		
	private double realStartTime; //The real start time
	private double startTimeWithConfidency; //The \alpha-quantile of the start time
	private double earliestStartTime; // The earliest start time based on the \alpha-quantile of execution time
	private double leastStartTime; // The least start time based on the \alpha-quantile of execution time
	
	private double realFinishTime; //The real finish time
	private double finishTimeWithConfidency; //The \alpha-quantile of the finish time
	private double earliestFinishTime;
	private double leastFinishTime;
			
	private boolean allocatedFlag; //Record whether this task has been allocated
	private SaaSVm allocateVm; // The service instance where this task is allocated to
	private boolean finishFlag; //Record whether this task has been finished
			
	private double priority;
	
	private int PCPNum;
	private double allowExecutionTime;
	private double subDeadline; //The sub-deadline of this task
			
	private List<ConstraintWTask> parentTaskList;   //The set of immediate predecessors of this task
	private List<ConstraintWTask> sucessorTaskList; //The set of immediate successors of this task
	
	private List<Constraint> parentIDList;    //The IDs for the set of immediate predecessors
	private List<Constraint> successorIDList; //The IDs for the set of immediate successors
	
	public WTask(String taskId, int workflowId, double executionTime)
	{// Initialize an instance	
		this.taskId = taskId;
		this.taskWorkFlowId = workflowId;
		
		this.baseExecutionTime = executionTime; // Is used to calculated the makespan of the corresponding workflow
		this.baseStartTime = -1;
		this.baseFinishTime = -1;
		
		this.realBaseExecutionTime = 0;
		
		this.realExecutionTime = 0;
		this.executionTimeWithConfidency = 0;
		
		this.realStartTime = -1;
		this.startTimeWithConfidency = -1;
		this.earliestStartTime = -1;		
		this.leastStartTime = -1;
		
		this.realFinishTime = -1;
		this.finishTimeWithConfidency = -1;
		this.earliestFinishTime = -1;
		this.leastFinishTime = -1;
								
		this.allocatedFlag = false;
		this.allocateVm = null;
		this.finishFlag = false;
										
		this.priority = -1;			
		this.PCPNum = -1;
		this.allowExecutionTime = -1;
		this.subDeadline = -1;
								
		parentTaskList = new ArrayList<ConstraintWTask>(); //Initialize
		sucessorTaskList = new ArrayList<ConstraintWTask>(); //Initialize
		
		parentIDList = new ArrayList<Constraint>();    //Initialize
		successorIDList = new ArrayList<Constraint>(); //Initialize
	}
	
	public WTask()
	{
		this.taskId = "initial";
		this.baseExecutionTime = -1;
		this.baseFinishTime = -1;
		this.realFinishTime = -1;
	}
	
	/**Obtain the ID of this task*/
	public String getTaskId(){return taskId;}
	
	/**Obtain the ID of the workflow where this task belongs to*/
	public int getTaskWorkFlowId(){return taskWorkFlowId;}
	/**Set the ID of the workflow where this task belongs to*/
	public void setTaskWorkFlowId(int workFlowId)
	{
		this.taskWorkFlowId = workFlowId;
	}
	
	public double getBaseExecutionTime(){return baseExecutionTime;}
	
	public double getBaseStartTime(){return baseStartTime;}
	public void setBaseStartTime(double startTime)
	{
		this.baseStartTime = startTime;
	}
	
	public double getBaseFinishTime(){return baseFinishTime;}
	public void setBaseFinishTime(double bFinishTime)
	{
		this.baseFinishTime = bFinishTime;
	}
	
	public double getRealBaseExecutionTime(){return realBaseExecutionTime;}
	public void setRealBaseExecutionTime(double realBaseTime)
	{
		this.realBaseExecutionTime = realBaseTime;
	}
	
	public double getRealExecutionTime(){return realExecutionTime;}
	public void setRealExecutionTime(double realTime)
	{
		this.realExecutionTime = realTime;
	}
	
	public double getExecutionTimeWithConfidency(){return executionTimeWithConfidency;}
	public void setExecutionTimeWithConfidency(double eTimeWithConfidency)
	{
		this.executionTimeWithConfidency = eTimeWithConfidency;
	}
	
	public double getRealStartTime(){return realStartTime;}
	public void setRealStartTime(double realStartTime)
	{
		this.realStartTime = realStartTime;
	}
	
	public double getStartTimeWithConfidency(){return startTimeWithConfidency;}
	public void setStartTimeWithConfidency(double startTime)
	{
		this.startTimeWithConfidency = startTime;
	}
	
	public double getEarliestStartTime(){return earliestStartTime;}
	public void setEarliestStartTime(double earliestStartTime)
	{
		this.earliestStartTime = earliestStartTime;
	}
	
	public double getLeastStartTime(){return leastStartTime;}
	public void setLeastStartTime(double leastStartTime)
	{
		this.leastStartTime = leastStartTime;
	}
	
	public double getRealFinishTime(){return realFinishTime;}
	public void setRealFinishTime(double realFinishTime)
	{
		this.realFinishTime = realFinishTime;
	}
		
	public double getFinishTimeWithConfidency(){return finishTimeWithConfidency;}
	public void setFinishTimeWithConfidency(double finishTime)
	{
		this.finishTimeWithConfidency = finishTime;
	}
	
	public double getEarliestFinishTime(){return earliestFinishTime;}
	public void setEarliestFinishTime(double earliestFinishTime)
	{
		this.earliestFinishTime = earliestFinishTime;
	}
	
	public double getLeastFinishTime(){return leastFinishTime;}
	public void setLeastFinishTime(double leastFinishTime)
	{
		this.leastFinishTime = leastFinishTime;
	}
			
	public boolean getAllocatedFlag(){return allocatedFlag;}
	public void setAllocatedFlag(boolean allocatedFlag)
	{
		this.allocatedFlag = allocatedFlag;
	}
	
	public SaaSVm getAllocateVm(){return allocateVm;}
	public void setAllocateVm(SaaSVm vm)
	{
		this.allocateVm = vm;
	}
	
	public boolean getFinishFlag(){return finishFlag;}
	public void setFinishFlag(boolean flag)
	{
		this.finishFlag = flag;
	}
			
	public double getPriority(){return priority;}
	public void setPriority(double priority)
	{
		this.priority = priority;
	}
	
	public int getPCPNum(){return PCPNum;}
	public void setPCPNum(int num)
	{
		this.PCPNum = num;
	}
	
	public double getWTaskAllowExecutionTime(){return allowExecutionTime;}
	public void setWTaskAllowExecutionTime(double allowET)
	{
		this.allowExecutionTime = allowET;
	}
	
	
	public double getWTaskSubDeadline(){return subDeadline;}
	public void setWTaskSubDeadline(double subDeadline)
	{
		this.subDeadline = subDeadline;
	}
	
	public List<ConstraintWTask> getParentTaskList(){return parentTaskList;}
	
	public List<ConstraintWTask> getSuccessorTaskList(){return sucessorTaskList;}
	
	public List<Constraint> getParentIDList(){return parentIDList; }
	
	public List<Constraint> getSuccessorIDList(){return successorIDList; }	
}
