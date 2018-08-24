package workflow;

import java.io.Serializable;

//chk 2014.10.18 ChongQing
public class Constraint implements Serializable
{
	private final String taskId; //The ID of linked task, such as processor and successor
	private final int dataSize; // The size of data between the linked tasks
	
	
	public Constraint(String taskId, int dataSize)
	{
		this.taskId = taskId;
		this.dataSize = dataSize;
	}
	
	public String getTaskId(){return taskId;}	
	public int getDataSize(){return dataSize;}
	
}
