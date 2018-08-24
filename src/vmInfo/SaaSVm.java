package vmInfo;

import java.util.ArrayList;
import java.util.List;
import workflow.WTask;

public class SaaSVm 
{
	private final int vmID; //The ID of the service instance
	private final String vmType; //The type of this service instance
	
	private final double vmPrice; //Price
	private double totalCost; // The cost of this service instance during executing this set of workflows
	
	private final double startWorkTime; //The start time
	private double endWorkTime; //The turn-off time
	private double realWorkTime; //The real working time
	private double idleTime; //Idle time
	
	private final double executionTimeFactor; //The factor
	private double finishTime; //The predicted finish time of the executing task on this service instance
	private double realFinishTime; //The real finish time
	private double readyTime; //The ready time
	
	private boolean status; //The status (on or off) of this service instance
		
	private List<WTask> WTaskList; //The set of tasks allocated to this service instance	
	private List<WTask> waitWTaskList; //The set of waiting tasks	
	private WTask waitingWTask;     
	private WTask executingWTask; //The executing task
	
	public SaaSVm(int id, String type, double startTime, 
			double price, double factor)
	{
		this.vmID = id;
		this.vmType = type;
		this.vmPrice = price;
		this.executionTimeFactor = factor;
		
		this.totalCost = 0;
		this.startWorkTime = startTime;
		this.endWorkTime = startTime + 3600; //The unit of time is second
		this.realWorkTime = 0;
		this.idleTime = 0;
		
		this.finishTime = -1;
		this.realFinishTime = -1;
		this.readyTime = startTime; //When starting a service instance, the ready time is the start time
		
		this.status = true;
		this.WTaskList = new ArrayList<WTask>();
		this.waitWTaskList = new ArrayList<WTask>();
		this.waitingWTask = new WTask();
		this.executingWTask = new WTask();
	}	
	
	/**Obtain the ID*/
	public int getVmID(){return vmID;}
	
	/**Obtain the type of this service instance*/
	public String getVmType(){return vmType;}
	
	/**Obtain the price of this service instance*/
	public double getVmPrice(){return vmPrice;}		
	
	/**Obtain the factor (i.e., F(u)) of this service instance*/
	public double getVmFactor(){return executionTimeFactor;}
	
	/**Obtain the start time*/
	public double getVmStartWorkTime(){return startWorkTime;}
	
	/**Obtain the cost of this service instance*/
	public double getTotalCost(){return totalCost;}
	/**Update the cost of this service instance*/
	public void setTotalCost(double add)
	{
		this.totalCost = add;
	}
		
	/**Obtain the end time*/
	public double getEndWorkTime(){return endWorkTime;}
	/**Set the end time*/
	public void setEndWorkTime(double endTime)
	{
		this.endWorkTime = endTime;
	}
	
	/**Obtain the real work time*/
	public double getRealWorkTime(){return realWorkTime;}
	/**Set the real work time*/
	public void updateRealWorkTime(double workTime)
	{
		this.realWorkTime += workTime;
	}
	
	/**Obtain the idle time*/
	public double getIdleTime(){return idleTime;}
	/**Update the idle time*/
	public void updateIdleTime(double idleTime)
	{
		this.idleTime += idleTime;
	}
	
	public double getFinishTime(){return finishTime;}
	public void setFinishTime(double finishTime)
	{
		this.finishTime = finishTime;
	}
	
	public double getRealFinishTime(){return realFinishTime;}
	public void setRealFinishTime(double rFinishTime)
	{
		this.realFinishTime = rFinishTime;
	}
	
	public double getReadyTime(){return readyTime;}
	public void setReadyTime(double readyTime)
	{
		this.readyTime = readyTime;
	}
	
	public boolean getVmStatus(){return status;}
	public void setVmStatus(boolean status)
	{
		this.status = status;
	}
	
	public List<WTask> getWTaskList(){return WTaskList;}
	
	public List<WTask> getWaitWTaskList(){return waitWTaskList;}
	
	public WTask getWaitingWTask(){return waitingWTask;}
	public void setWaitingWTask(WTask task)
	{
		this.waitingWTask = task;
	}
	
	public WTask getExecutingWTask(){return executingWTask;}
	public void setExecutingWTask(WTask task)
	{
		this.executingWTask = task;
	}
		
}
