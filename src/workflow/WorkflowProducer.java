package workflow;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import share.StaticfinalTags;

public class WorkflowProducer 
{
	private static List<Workflow> workflowList; //The set of workflows
	
	public static void main(String[] args) throws ClassNotFoundException, IOException 
	{
		workflowList = new ArrayList<Workflow>(); //Initialize a list to record the set of workflows
		int workflowNum = StaticfinalTags.workflowNum; // The number of workflows in the set
		workflowList = produceWorkflow(workflowNum); // Generate the set of the workflows
		
		int num = workflowList.size();
		System.out.println("Arrival Time: "+ workflowList.get(num-1).getArrivalTime());
		
		//Write the set of workflows to the file tempWorkflow.txt, such that different algorithms can be tested on the same workflow set
		FileOutputStream fos = new FileOutputStream("producedWorkflow.txt"); 
		ObjectOutputStream os = new ObjectOutputStream(fos);
		try
		{
			for(int i=0; i<workflowList.size(); i++)
			{
				os.writeObject(workflowList.get(i));
			}
			os.close();
		}catch(IOException e){System.out.println(e.getMessage());}
		workflowList.clear();
		
	}// end main()
	
	/**On the basis of the real-world workflow templates, the set of workflows are generated*/
	private static List<Workflow> produceWorkflow(int workflowNum) throws IOException, ClassNotFoundException
	{	
		//Read the workflow templates, which are stored in the file 'tempWorkflow.txt'
		List<Workflow> templateWList = new ArrayList<Workflow>();
		FileInputStream fi = new FileInputStream("tempWorkflow.txt");
		ObjectInputStream si = new ObjectInputStream(fi);
		try
		{
			for(int i=0; i<15; i++) // The 15 is the number of the templates
			{
				TempWorkflow readWorkflow = (TempWorkflow)si.readObject(); // Obtain a workflow template from the file 'tempWorkflow.txt'
				
				List<WTask> taskList = new ArrayList<WTask>(); //The set of tasks in a workflow
				List<TempWTask> tempWTaskList = readWorkflow.getTaskList();// The set of tasks in a workflow template
				for(TempWTask task: tempWTaskList)
				{//Copy each task in readWorkflow
					String taskId = task.getTaskId(); //ID of a task
					int workflowId = -1; //ID of this workflow
					double baseExecutionTime =  task.getTaskRunTime(); // Base execution time of the task
					if(baseExecutionTime < 0)
					{
						throw new IllegalArgumentException("Task base execution time is less than zero!");
					}
					
					//The set of immediate predecessors of a task
					List<Constraint> parentConstraintList = new ArrayList<Constraint>();
					for(Constraint con: task.getParentTaskList())
					{
						String parentTaskId = con.getTaskId();
						int dataSize = con.getDataSize();
						Constraint tempParentConstraint = new Constraint(parentTaskId, dataSize);
						parentConstraintList.add(tempParentConstraint);
					}
					
					//The set of immediate successors of a task
					List<Constraint> successorConstraintList = new ArrayList<Constraint>();
					for(Constraint con: task.getSuccessorTaskList())
					{
						String parentTaskId = con.getTaskId();
						int dataSize = con.getDataSize();
						Constraint tempParentConstraint = new Constraint(parentTaskId, dataSize);
						successorConstraintList.add(tempParentConstraint);
					}
					
					//New a task
					WTask wTask = new WTask(taskId, workflowId, baseExecutionTime);
					wTask.getParentIDList().addAll(parentConstraintList);
					wTask.getSuccessorIDList().addAll(successorConstraintList);
					
					taskList.add(wTask);
				}//end for(TempWTask task: tempWTaskList) //读取任务信息结束				
								
				String name = readWorkflow.getWorkflowName();
				// New a workflow
				Workflow workflow = new Workflow(-1, name, -1, -1, -1);
				workflow.setTaskList(taskList); // Add all the tasks to this workflow
				
				templateWList.add(workflow);
			}			
			si.close(); 
		}catch(IOException e){System.out.println(e.getMessage());}
		
		if(templateWList.size() != 15)
		{
			System.out.println("Error: the template workflow is not right");
		}
		
		List<Workflow> deleteWorkflows = new ArrayList<Workflow>();
		for(Workflow workflow: templateWList)
		{//The experiments do not include the series of Epigenomics workflows
			if(workflow.getWorkflowName().equals("Epigenomics_24.xml")||
					workflow.getWorkflowName().equals("Epigenomics_46.xml")||
					workflow.getWorkflowName().equals("Epigenomics_100.xml"))
			{
				deleteWorkflows.add(workflow);
			}
		}
		templateWList.removeAll(deleteWorkflows);
				
		//Caculate the makespan for each template workflow
		for(Workflow workflowMakespan: templateWList)
		{
			double makespan = CalculateMakespan(workflowMakespan);									
			workflowMakespan.setMakespan(makespan);
		}
		
		
		
		
		
		// Start to generate the workflow set
		List<Workflow> wList = new ArrayList<Workflow>();
		int workflowId = 0;
		int arrivalTime = 0;		
		while(wList.size() < workflowNum) //workflowNum denotes the number of generated workfolws
		{
			int temNum = PoissValue(StaticfinalTags.arrivalLamda); // The number of workflows generated in this second
			if(temNum == 0) // If the number of arrival workflows is zero, go to the next second
			{
				arrivalTime++; //Go to the next second
				continue;
			}
			else
			{
				boolean flag = false; // Check if the number of workflows reaches the prefixed number
				for(int i=0; i<temNum; i++)
				{
					int templateNum = (int)(Math.random()*12); // Determine which workflow is submitted
					
					Workflow findWorkflow = templateWList.get(templateNum);
					
					//Obtain the parameters of this workflow template
					List<WTask> tempTaskList = new ArrayList<WTask>();// Copy the tasks in this workflow template									
					for(WTask task: findWorkflow.getTaskList())
					{
						//Copy the base execution time of this task
						WTask copyTask = new WTask(task.getTaskId(), workflowId, task.getBaseExecutionTime());
						copyTask.setBaseStartTime(task.getBaseStartTime());
						copyTask.setBaseFinishTime(task.getBaseFinishTime());
						
						//Determine the real execution time of the task
						double standardDeviation = copyTask.getBaseExecutionTime()*StaticfinalTags.standardDeviation;
						//Calculate the real execution time of this task
						double realBaseExecutionTime = NormalDistributionCos(copyTask.getBaseExecutionTime(), standardDeviation);
						if(realBaseExecutionTime < 0)
						{
							realBaseExecutionTime = copyTask.getBaseExecutionTime();
							//System.out.println("Error Real Execution Time: "+realBaseExecutionTime);
						}						
						copyTask.setRealBaseExecutionTime(realBaseExecutionTime);
						
						
						//Link the predecessors by ID
						List<Constraint> parentConstraintList = new ArrayList<Constraint>();
						for(Constraint con: task.getParentIDList())
						{
							String parentTaskId = con.getTaskId();
							int dataSize = con.getDataSize();
							Constraint tempParentConstraint = new Constraint(parentTaskId, dataSize);
							parentConstraintList.add(tempParentConstraint);
						}
						
						//Link the successor by ID
						List<Constraint> successorConstraintList = new ArrayList<Constraint>();
						for(Constraint con: task.getSuccessorIDList())
						{
							String parentTaskId = con.getTaskId();
							int dataSize = con.getDataSize();
							Constraint tempSuccessorConstraint = new Constraint(parentTaskId, dataSize);
							successorConstraintList.add(tempSuccessorConstraint);
						}
																		
						copyTask.getParentIDList().addAll(parentConstraintList);
						copyTask.getSuccessorIDList().addAll(successorConstraintList);
						
						tempTaskList.add(copyTask);
					}
					
					//Link all the immediate predecessors for each task
					for(WTask connectedTask: tempTaskList)
					{
						for(Constraint parentCon: connectedTask.getParentIDList())
						{//Link all the immediate predecessors for one task
							String parentID = parentCon.getTaskId();
							int dataSize = parentCon.getDataSize();
							//Calculate the real size of the data between them
							double standardDeviationDS = dataSize*StaticfinalTags.standardDeviationData;							
							int realDataSize = (int)NormalDistributionCos(dataSize, standardDeviationDS);
							for(WTask parentTask: tempTaskList)
							{
								if(parentID.equals(parentTask.getTaskId()))
								{//Find the predecessor
									ConstraintWTask parent = new ConstraintWTask(parentTask, dataSize);
									parent.setRealDataSize(realDataSize); 
									
									connectedTask.getParentTaskList().add(parent);
									break;
								}
							}
						}
					}
					
					//Link all the immediate successors for each task
					for(WTask connectedTask: tempTaskList)
					{
						for(Constraint successorCon: connectedTask.getSuccessorIDList())
						{//Link all the immediate successors for one task
							String successorID = successorCon.getTaskId();
							int dataSize = successorCon.getDataSize();
							for(WTask successorTask: tempTaskList)
							{
								if(successorID.equals(successorTask.getTaskId()))
								{//Find the successor
									ConstraintWTask successor = new ConstraintWTask(successorTask, dataSize);
									
									String thisTaskID = connectedTask.getTaskId();
									for(ConstraintWTask parentCon: successorTask.getParentTaskList())
									{																				
										if(thisTaskID.equals(parentCon.getWTask().getTaskId()))
										{
											int realDataSize = parentCon.getRealDataSize();
											successor.setRealDataSize(realDataSize);
											break;
										}
									}
									
									connectedTask.getSuccessorTaskList().add(successor);								
									break;
								}
							}
						}
					} 
					
					
					
					String name = findWorkflow.getWorkflowName();					
					int deadline = (int)(arrivalTime + findWorkflow.getMakespan()*StaticfinalTags.deadlineBase); // Generate the deadline for this workflow
					
					//Generate a workflow
					Workflow newWorkflow = new Workflow(workflowId, name, arrivalTime, findWorkflow.getMakespan(), deadline);
					newWorkflow.setTaskList(tempTaskList);
					
					wList.add(newWorkflow);
					
					if(wList.size() == workflowNum)
					{
						flag = true;
						break;
					}					
					workflowId++; //Update the ID of the workflows
				}				
				if(flag)
				{
					break; 
				}				
				arrivalTime++; // Go to the next second
			}						
		}//end while(wList.size() < workflowNum)
		return wList;
	}
	
	
	/**Calculate the makespan for a workflow*/
	public static double CalculateMakespan(Workflow cWorkflow)
	{
		List<WTask> calculatedTaskList = new ArrayList<WTask>();	
		while(true)
		{
			for(WTask task: cWorkflow.getTaskList()) //Check all the tasks in this workflow
			{
				if(task.getBaseFinishTime() > -1) // If this task have been calculated, then continue
				{
					continue;
				}
				
				double executionTime = task.getBaseExecutionTime(); // The base execution time of a task
				if(task.getParentIDList().size() == 0)
				{//Entry task
					task.setBaseStartTime(0);
					task.setBaseFinishTime(executionTime);
					calculatedTaskList.add(task);
				}
				else //For the tasks with predecessors
				{	
					double maxStartTime = -1; // The latest start time
					boolean unCalculatedParent = false; // Check if all its predecessors have been calculated
					
					double maxDataSize = 0; // The maximal size of data
					for(Constraint con: task.getParentIDList())
					{
						if(con.getDataSize() > maxDataSize)
						{
							maxDataSize = con.getDataSize();							
						}
					}
					double commDelay = (1.2*maxDataSize)/StaticfinalTags.bandwidth;
					
					// Check if all its predecessors have been calculated
					for(Constraint con: task.getParentIDList())
					{
						unCalculatedParent = true;						
						String parentId = con.getTaskId(); // Obtain the ID of the predecessor
												
						for(WTask parTask: calculatedTaskList)
						{
							if(parentId.equals(parTask.getTaskId()))
							{
								unCalculatedParent = false;
								double startTime = parTask.getBaseFinishTime() + commDelay;
								if(startTime > maxStartTime)
								{
									maxStartTime = startTime;
								}
								break;
							}
						}
						if(unCalculatedParent == true)
						{
							break;
						}					
					}
					
					if(unCalculatedParent == false)
					{// If all the predecessors of task have been finished, then calculate the finish time of this task
						task.setBaseStartTime(maxStartTime);
						task.setBaseFinishTime(maxStartTime + executionTime);
						calculatedTaskList.add(task);
					}																															
				}//end else
			}//end for(Task task: cWorkflow.getTaskList()) 
			
			if(calculatedTaskList.size() == cWorkflow.getTaskList().size())//计算完的条件
			{
				break;
			}
		}//end while
		
		double makespan = 0; //The makespan of this workflow is the maximal finish time among all the tasks in this workflow
		for(WTask cTask: cWorkflow.getTaskList())
		{
			if(cTask.getBaseFinishTime() > makespan)
			{
				makespan = cTask.getBaseFinishTime();
			}
			if(cTask.getBaseFinishTime() < 0)
			{
				throw new IllegalArgumentException("Error: there exists tasks is not calculated in calculate makespan!");
			}
		}
		return makespan;
	}
	
	
	/**Calculate the real base execution time of tasks*/
	public static void calculateRealTaskBaseExecutionTime(List<Workflow> list)
	{
		for(Workflow tempWorkflow: list)
		{
			for(WTask tempTask: tempWorkflow.getTaskList())
			{
				double standardDeviation = tempTask.getBaseExecutionTime()*StaticfinalTags.standardDeviation;
				//Generate the real base execution time of a task
				double realBaseExecutionTime = NormalDistributionCos(tempTask.getBaseExecutionTime(), standardDeviation);
				
				if(realBaseExecutionTime < 0)
				{
					realBaseExecutionTime = -realBaseExecutionTime;
				}				
				if(realBaseExecutionTime == 0)
				{
					throw new IllegalArgumentException("The execution time of a task is zero!");
				}
				
				tempTask.setRealBaseExecutionTime(realBaseExecutionTime);
			}
		}
	}
	
	//Sort the workflows based on their ID
	public static class WorkflowComparatorById implements Comparator<Workflow>
	{
		public int compare(Workflow w1, Workflow w2)
		{
			return (w1.getWorkflowId() - w2.getWorkflowId());
		}
	}
	
	/**Poisson Distribution*/
	public static int PoissValue(double Lamda)
	{
		 int value=0;
		 double b=1;
		 double c=0;
		 c=Math.exp(-Lamda); 
		 double u=0;
		 do 
		 {
			 u=Math.random();
			 b*=u;
			 if(b>=c)
				 value++;
		  }while(b>=c);
		 return value;
	}
	
	/**
	 * Generator for normal distribution
	 * @param average
	 * @param deviance
	 * @return 
	 */
	public static double NormalDistributionCos(double average,double deviance)
	{
		double Pi=3.1415926535;
		double r1=Math.random();
		Math.random();Math.random();Math.random();Math.random();Math.random();
		Math.random();Math.random();
		double r2=Math.random();
		double u=Math.sqrt((-2)*Math.log(r1))*Math.cos(2*Pi*r2);
		double z=average+u*Math.sqrt(deviance);
		return z;
	}	
}
