package workflowScheduleAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import share.PerformanceValue;
import share.StaticfinalTags;
import vmInfo.SaaSVm;
import workflow.ConstraintWTask;
import workflow.WTask;
import workflow.Workflow;


public class ROSA_ProposedAlgorithm 
{
	private List<SaaSVm> vmList; //The set of used service instances
	private List<Workflow> workflowList; //The set of workflows
	
	
	public ROSA_ProposedAlgorithm() throws Exception
	{
		this.vmList = new ArrayList<SaaSVm>(); 
		initialVmList(StaticfinalTags.initialVmNum); //Initialize the set of service instances
		this.workflowList = new ArrayList<Workflow>(); //
	}
	
	/////////////////////////////////////////////////////////////////////////////////
	/**The proposed algorithm ROSA*/
	public void scheduleDynamicWorkflowToSiROSA()
	{
		System.out.println("Algorithm UOSA is Started");
		List<Workflow> workflowList = getWorkflowList();
		
		
		//Record the runtime of scheduling algorithm
		long totalScheduleTime = 0;
		long startTime01 = System.currentTimeMillis();
		
		//Calculate the \alpha-quantile of the execution time for each workflow tasks
		calculateTaskBaseExecutionTimeWithConfidency(workflowList);
		//Calculate the \beta-quantile of data size among workflow tasks
		calculateBaseDataSizeWithConfidency(workflowList);
		
		long endTime01 = System.currentTimeMillis();
		totalScheduleTime = totalScheduleTime + (endTime01 - startTime01);
				
		int vmId = getVmList().size(); //Record the ID of the service instances
		List<SaaSVm> activeVmList = new ArrayList<SaaSVm>(); //The set of active service instances
		activeVmList = getVmList();		
		List<SaaSVm> offVmList = new ArrayList<SaaSVm>(); //The set of off service instances		
		List<WTask> RH_WTask = new ArrayList<WTask>(); //The task pool, place all the un-scheduled tasks
		
		for(int i=0; i<workflowList.size(); i++) //Schedule all the workflows
		{
			//Search all the workflows having the same arrival time
			List<Workflow>  workflowsArriveSynchronously = new ArrayList<Workflow>();
			workflowsArriveSynchronously.add(workflowList.get(i));
			StaticfinalTags.currentTime = workflowList.get(i).getArrivalTime(); //Update the current time
			for(int k=i+1; k<workflowList.size(); k++) //Search other workflows having the same arrival time
			{				
				if(workflowList.get(k).getArrivalTime() == workflowList.get(i).getArrivalTime())
				{
					workflowsArriveSynchronously.add(workflowList.get(k));
					i++;
				}
				else
				{
					break;
				}
			}
			
						
			//Assign ranks for workflow tasks
			for(Workflow rankWorkflow: workflowsArriveSynchronously)
			{
				CaculateLeastFinishTimeForWTask(rankWorkflow);	//Calculate the least finish time for each task			
				rankWTasksForPRS(rankWorkflow); //The rank of a task is set as its least finish time
			}
			
			
			//Search ready tasks from new workflows
			List<WTask> readyWTaskList = new ArrayList<WTask>();
			readyWTaskList = getReadyWTaskFromNewWorkflows(workflowsArriveSynchronously);
			
			long startTime02 = System.currentTimeMillis();
			//Schedule ready tasks to service instances
			scheduleReadyWTaskToSaaSVM(readyWTaskList, activeVmList, vmId);			
			
			long endTime02 = System.currentTimeMillis();
			totalScheduleTime = totalScheduleTime + (endTime02 - startTime02);			
			
			vmId = activeVmList.size() + offVmList.size();
			
			//Add the un-scheduled tasks to the task pool, i.e., RH_WTask
			for(Workflow addWorkflow: workflowsArriveSynchronously)
			{
				for(WTask addTask: addWorkflow.getTaskList())
				{
					if(!addTask.getAllocatedFlag()) //If a task is not scheduled, add it to the task pool
					{
						RH_WTask.add(addTask);
					}
				}
			}
			
			
			//The arrival time of next workflow
			int nextArrivalTime = Integer.MAX_VALUE; 
			if(i != workflowList.size()-1)
			{
				nextArrivalTime = workflowList.get(i+1).getArrivalTime();
			}
			
			
			SaaSVm nextFinishVm = null; //The service instance that its executing task has the earliest completion time
			double nextFinishTime = Integer.MAX_VALUE; //The completion time		
			SaaSVm turnOffVm = null; //The service instances that being closed earliest
			double turnOffVmTime = Integer.MAX_VALUE; //The turn-off time of the earliest one						
			for(SaaSVm initiatedVm: activeVmList) //Check all the service instances to update the above four parameters
			{
				double tempFinishTime = initiatedVm.getExecutingWTask().getRealFinishTime();
				if(tempFinishTime != -1)//The service instance has executing task
				{
					if (tempFinishTime < nextFinishTime)//Update the earliest completion time
					{
						nextFinishTime = tempFinishTime;
						nextFinishVm = initiatedVm;
					}
				}
				else //The service instance is idle
				{
					//The possible turn-off time of this service instance
					double tempTurnOffTime = Integer.MAX_VALUE;
					if((StaticfinalTags.currentTime-initiatedVm.getVmStartWorkTime())%3600 == 0)
					{
						tempTurnOffTime = StaticfinalTags.currentTime;
					}
					else
					{
						int round = (int)((StaticfinalTags.currentTime-initiatedVm.getVmStartWorkTime())/3600);
						tempTurnOffTime = initiatedVm.getVmStartWorkTime()+3600*(round+1);
					}
					if (tempTurnOffTime < turnOffVmTime)//Update the turn-off service instance
					{
						turnOffVmTime = tempTurnOffTime;
						turnOffVm = initiatedVm;
					}
					
				}
			}
			
			
			//Update the system status before next workflow arrives, including:£¨1£©the executing tasks on service instances£¬£¨2£©turn off idle service instances.
			while(nextArrivalTime >= nextFinishTime || 
					nextArrivalTime > turnOffVmTime)
			{	
				// Update the executing task having the earliest completion time and the corresponding service instance
				if(nextFinishTime <= turnOffVmTime)
				{
					WTask finishTask = nextFinishVm.getExecutingWTask();					
					//Update the current time, which is set as the completion time of the executing task
					StaticfinalTags.currentTime = nextFinishVm.getExecutingWTask().getRealFinishTime();
					if(nextFinishVm.getWaitWTaskList().size() != 0) //When the service instance has waiting tasks
					{
						nextFinishVm.getExecutingWTask().setFinishFlag(true);
						WTask nextExecutionTask = nextFinishVm.getWaitWTaskList().get(0);			
						nextFinishVm.setExecutingWTask(nextExecutionTask);
						nextFinishVm.getWaitWTaskList().remove(nextExecutionTask); //Remove the executing task from the waiting queue		
					}
					else //There is not waiting task on this service instance, i.e., nextFinishVm
					{
						nextFinishVm.getExecutingWTask().setFinishFlag(true);
						nextFinishVm.setExecutingWTask(new WTask());
					}
					
					
					//Search the successors of the finishTask that have just become ready
					List<WTask> readySucessorList = getReadySucessorsInRH(finishTask);					
					
					long startTime03 = System.currentTimeMillis();
					
					//Schedule the ready tasks to service instances
					scheduleReadyWTaskToSaaSVM(readySucessorList, activeVmList, vmId);
					
					long endTime03 = System.currentTimeMillis();
					totalScheduleTime = totalScheduleTime + (endTime03 - startTime03);
					
					vmId = activeVmList.size() + offVmList.size();					
					//Remove the scheduled tasks from the task pool
					RH_WTask.removeAll(readySucessorList);					
				}
				
				
				// Turn off service instance
				if(turnOffVmTime < nextFinishTime)
				{
					StaticfinalTags.currentTime = turnOffVmTime; //Update the current time of the system
					double workTime = turnOffVmTime - turnOffVm.getVmStartWorkTime();					
					double cost = (workTime*turnOffVm.getVmPrice())/3600; // Calculate the cost for this service instance
					
					turnOffVm.setEndWorkTime(turnOffVmTime);
					turnOffVm.setTotalCost(cost);
					turnOffVm.setVmStatus(false);
					activeVmList.remove(turnOffVm);
					offVmList.add(turnOffVm);																									
				}																								
								
				//Update the next service instance that completing executing tasks at the earliest time, and the next off service instance
				nextFinishVm = null; //The service instance that its executing task has the earliest completion time
				nextFinishTime = Integer.MAX_VALUE; 
				
				turnOffVm = null; //The service instances that being closed earliest
				turnOffVmTime = Integer.MAX_VALUE; 				
				for(SaaSVm initiatedVm: activeVmList)// Check all the active service instances to 
				{
					double tempFinishTime = initiatedVm.getExecutingWTask().getRealFinishTime();
					if(tempFinishTime != -1)//The exist an executing task
					{
						if (tempFinishTime < nextFinishTime)//Update
						{
							nextFinishTime = tempFinishTime;
							nextFinishVm = initiatedVm;
						}
					}
					else //The service instance is idle
					{
						//The possible turn-off time
						double tempTurnOffTime = Integer.MAX_VALUE;
						if((StaticfinalTags.currentTime-initiatedVm.getVmStartWorkTime())%3600 == 0)
						{
							tempTurnOffTime = StaticfinalTags.currentTime;
						}
						else
						{
							int round = (int)((StaticfinalTags.currentTime-initiatedVm.getVmStartWorkTime())/3600);
							tempTurnOffTime = initiatedVm.getVmStartWorkTime()+3600*(round+1);
						}
						if (tempTurnOffTime < turnOffVmTime)//Update
						{
							turnOffVmTime = tempTurnOffTime;
							turnOffVm = initiatedVm;
						}
					}
				}
				
				if(nextArrivalTime==Integer.MAX_VALUE && nextFinishTime==Integer.MAX_VALUE 
						&& turnOffVmTime==Integer.MAX_VALUE)
				{
					break;
				}
			}
			
			
		}
								
		//Calculate the experimental results
		countUpExperimentResult(offVmList, workflowList, totalScheduleTime);
		
		workflowList.clear();
		offVmList.clear();
		activeVmList.clear();		
	}
	
	
	/**Calculate and output the experimental results*/
	public void countUpExperimentResult(List<SaaSVm> offVmList, List<Workflow> workflowList, long totalScheduleTime)
	{
		java.text.DecimalFormat fd = new java.text.DecimalFormat("0.0000");
		double totalCost = 0; // Total cost
		double totalExecutionTime = 0;
		double totalTime = 0;
		
		
		double RUeaVM[] = new double [offVmList.size()]; // Record the resource utilization of each service instance
		double sumRU = 0; 
		int vmIndex = 0; 
		for(SaaSVm offVm: offVmList)
		{			
			//Record the cost
			totalCost = totalCost + offVm.getTotalCost();
			
			//Record the work time of a service instance
			double workTime = 0;
			for(WTask task: offVm.getWTaskList())
			{
				workTime = workTime + task.getRealExecutionTime();		
			}
			totalExecutionTime = totalExecutionTime + workTime;
			
			//Record the total run time of a service instance
			double runTimeForVM = offVm.getEndWorkTime()-offVm.getVmStartWorkTime();
			totalTime = totalTime + runTimeForVM;
			
			
			//The resource utilization of this service instance
			double RUforThisVM = 0;
			if(runTimeForVM > 0)
			{
				RUforThisVM = workTime/runTimeForVM;
			}
			
			RUeaVM[vmIndex] = RUforThisVM; //Record the resource utilization
			sumRU = sumRU + RUforThisVM;
			vmIndex = vmIndex + 1;
		}		 
		double reUtilization = (double)totalExecutionTime/totalTime;
		
		
		//Calculate the fairness
		double averageRU = sumRU/offVmList.size(); //average vale
		double squareSum = 0;
		for(double tempRU: RUeaVM)
		{
			squareSum = squareSum +  Math.sqrt((tempRU-averageRU)*(tempRU-averageRU));
		}
		double StandardDevitionOfRU = squareSum/(offVmList.size() - 1); //Variance of resource utilization
		double fairness = 1/StandardDevitionOfRU; //fairness
		
		double workflowDeviation = 0;
		int totalTaskCount = 0; //Record the number of tasks
		for(Workflow tempWorkflow: workflowList)
		{
			double maxRealFinishTime = 0;
			double maxFinishTimeWC = 0;
			for(WTask task: tempWorkflow.getTaskList())
			{
				if(task.getRealFinishTime() > maxRealFinishTime)
				{
					maxRealFinishTime = task.getRealFinishTime();
				}
				if(task.getFinishTimeWithConfidency() > maxFinishTimeWC)
				{
					maxFinishTimeWC = task.getFinishTimeWithConfidency();
				}
			}
			workflowDeviation = workflowDeviation + (double)Math.abs((maxFinishTimeWC - maxRealFinishTime))/tempWorkflow.getMakespan();	
			
			totalTaskCount = totalTaskCount + tempWorkflow.getTaskList().size();
		}
		workflowDeviation = workflowDeviation/workflowList.size();
		
		//The scheduling time per task
		double averageScheduleTime = (double)totalScheduleTime/totalTaskCount;
		
		//Record the results	
		PerformanceValue.TotalCost = totalCost;
		PerformanceValue.ResourceUtilization = reUtilization;
		PerformanceValue.deviation = workflowDeviation;
		PerformanceValue.fairness = fairness;
		PerformanceValue.averageTimePerWTask = averageScheduleTime;
		
		
		//Output the results
		System.out.println("Total Cost: "+fd.format(totalCost)+" Resource Utilization: "+fd.format(reUtilization)+" Deviation: "+fd.format(workflowDeviation)+" Fairness:"+fd.format(fairness) +" ScheduleTime:"+fd.format(averageScheduleTime) );
	}
	
	
	/**Search the successors of the finishTask that just become ready*/
	public List<WTask> getReadySucessorsInRH(WTask finishTask)
	{
		List<WTask> readyWTaskList = new ArrayList<WTask>();
		for(ConstraintWTask succ: finishTask.getSuccessorTaskList())
		{						
			WTask succWTask = succ.getWTask(); //The successor			
			
			boolean ready = true;
			for(ConstraintWTask parent: succWTask.getParentTaskList())
			{//If all the predecessors have been finished, this successor become ready
				if(!parent.getWTask().getFinishFlag())
				{
					ready = false;
					break;
				}
			}
			if(ready)
			{
				readyWTaskList.add(succWTask);
			}
			
		}
		return readyWTaskList;		
	}
	
	
	/**Schedule ready tasks to service instances*/
	public void scheduleReadyWTaskToSaaSVM(List<WTask> taskList, List<SaaSVm> vmList, int vmID)
	{
		Collections.sort(taskList, new WTaskComparatorByLeastStartTimeIncrease()); // Sort the ready tasks
		for(WTask scheduleTask: taskList) // Schedule each task 
		{
			double minCost = Double.MAX_VALUE;
			double startTimeWithConfi = Double.MAX_VALUE;
			SaaSVm targetVm = null;			
			for(SaaSVm vm: vmList) // Schedule task to an active service instance
			{							
				double startTime = StaticfinalTags.currentTime; //Update current time
				// Calculate the predicated start time
				for(ConstraintWTask parentCon: scheduleTask.getParentTaskList()) //Check all its predecessors
				{
					SaaSVm parentTaskVm = parentCon.getWTask().getAllocateVm(); // The service instance that its predecessor is allocated
					double minFinishTransTime = Double.MAX_VALUE;
					
					if(!parentTaskVm.equals(vm)) // Not on the same service instance, data transfer time cannot be ignored
					{						
						double startCommTime = StaticfinalTags.currentTime;
						if(parentCon.getWTask().getRealFinishTime() > startCommTime)
						{
							startCommTime = parentCon.getWTask().getFinishTimeWithConfidency();
						}
						minFinishTransTime = startCommTime + parentCon.getDataSizeWithConfiden()/StaticfinalTags.bandwidth;
						
					}
					else //On the same service instance, the data transfer time can be ignored
					{
						if(parentCon.getWTask().getFinishFlag())
						{
							minFinishTransTime = StaticfinalTags.currentTime;
						}
						else
						{
							minFinishTransTime = parentCon.getWTask().getFinishTimeWithConfidency();
						}							
					}
															
					if(minFinishTransTime > startTime)
					{//Update the start time
						startTime = minFinishTransTime;														
					}
					
				}	
								
				
				double tempAvailableTime = StaticfinalTags.currentTime;
				if(!vm.getExecutingWTask().getTaskId().equals("initial")) // The case that the service instance has executing tasks
				{
					if(startTime < vm.getExecutingWTask().getFinishTimeWithConfidency())
					{
						startTime = vm.getExecutingWTask().getFinishTimeWithConfidency();
					}										
					tempAvailableTime = vm.getExecutingWTask().getFinishTimeWithConfidency();
					
					
					int taskCount = vm.getWaitWTaskList().size();
					if(taskCount > 0)
					{
						tempAvailableTime = vm.getWaitWTaskList().get(taskCount-1).getFinishTimeWithConfidency();
						if(tempAvailableTime > startTime)
						{
							startTime = tempAvailableTime;
						}
						
					}
					
					
				}
				
				if((startTime-tempAvailableTime) > StaticfinalTags.maxIdleTime)
				{// If the idle time is larger than a preset value, then ingore this service instance
					continue;
				} 
								
				double executionTimeWithConfidency = scheduleTask.getExecutionTimeWithConfidency()*vm.getVmFactor();
				double finishTimeWithConfidency = startTime + executionTimeWithConfidency;	
				
				
				double predTime = finishTimeWithConfidency - vm.getVmStartWorkTime();
				int predUnits = (int)Math.ceil(predTime/3600); //The working period of service instance if allocating the task
				//The working period of service instance if not allocating the task
				double originalTime = vm.getReadyTime() - vm.getVmStartWorkTime();
				int originalUnits = (int)Math.ceil(originalTime/3600);
				//The increased cost by allocating this task
				double cost = (predUnits - originalUnits)*vm.getVmPrice();
				
				if(finishTimeWithConfidency <= scheduleTask.getLeastFinishTime())
				{
					if(cost < minCost)
					{
						minCost = cost;
						targetVm = vm;
						startTimeWithConfi = startTime;
					}
				}				
			}
			
			if(minCost > 0 || targetVm==null) // Consider to add new service instance
			{
				double startTime = StaticfinalTags.currentTime; //start time
				for(ConstraintWTask parentCon: scheduleTask.getParentTaskList()) // Check all its predecessors
				{		
					double startCommTime = StaticfinalTags.currentTime; //The start time of data transfer
					if(parentCon.getWTask().getRealFinishTime() > startCommTime)
					{
						startCommTime = parentCon.getWTask().getFinishTimeWithConfidency();
					}
					double minFinishTransTime = startCommTime + parentCon.getDataSizeWithConfiden()/StaticfinalTags.bandwidth;
					
					if(minFinishTransTime > startTime)
					{//Update the start time of the task
						startTime = minFinishTransTime;														
					}
					
				}
				
				double tempCost = Double.MAX_VALUE;
				int typeI = 0;
				if(startTime + scheduleTask.getExecutionTimeWithConfidency()*8.0 <= scheduleTask.getLeastFinishTime())
				{
					typeI = 3;
					int originalUnits = (int)Math.ceil(scheduleTask.getExecutionTimeWithConfidency()*8.0/3600);
					//Cost of this type of service instance
					tempCost = originalUnits*0.023;
				}
				else if(startTime + scheduleTask.getExecutionTimeWithConfidency()*4.0 <= scheduleTask.getLeastFinishTime())
				{
					typeI = 2;
					int originalUnits = (int)Math.ceil(scheduleTask.getExecutionTimeWithConfidency()*4.0/3600);
					//Cost of this type of service instance
					tempCost = originalUnits*0.0464;
				}
				else if(startTime + scheduleTask.getExecutionTimeWithConfidency()*2.0 <= scheduleTask.getLeastFinishTime())
				{
					typeI = 1;
					int originalUnits = (int)Math.ceil(scheduleTask.getExecutionTimeWithConfidency()*2.0/3600);
					//Cost of this type of service instance
					tempCost = originalUnits*0.0928;
				}
				else if(startTime + scheduleTask.getExecutionTimeWithConfidency() <= scheduleTask.getLeastFinishTime())
				{
					typeI = 0;
					int originalUnits = (int)Math.ceil(scheduleTask.getExecutionTimeWithConfidency()/3600);
					//Cost of this type of service instance
					tempCost = originalUnits*0.1856;
				}
				
				if(minCost > tempCost || targetVm==null)
				{//Add a new service instance with the type of typeI
					targetVm = scaleUpVm(vmID, startTime, typeI);
					vmID++;
					vmList.add(targetVm);
					
					startTimeWithConfi = startTime;
				}
			}
			
			
			
			//Update the parameters for the scheduled task				
			double realExecutionTime = scheduleTask.getRealBaseExecutionTime()*targetVm.getVmFactor();				
			//The real start time, is used to update the status of the system
			double realStartTime = calculateRealStartTimeForWTask(scheduleTask, targetVm);
			
			double realFinishtTime = realStartTime + realExecutionTime;
			double executionTimeWithConfidency = scheduleTask.getExecutionTimeWithConfidency()*targetVm.getVmFactor();
			double finishTimeWithConfidency = startTimeWithConfi + executionTimeWithConfidency;	
			
			scheduleTask.setAllocatedFlag(true);
			scheduleTask.setAllocateVm(targetVm);
			scheduleTask.setRealStartTime(realStartTime);
			scheduleTask.setRealExecutionTime(realExecutionTime);
			scheduleTask.setRealFinishTime(realFinishtTime);
			
			scheduleTask.setStartTimeWithConfidency(startTimeWithConfi);
			scheduleTask.setFinishTimeWithConfidency(finishTimeWithConfidency);
								
			//Update the status of the service instance		
			targetVm.setRealFinishTime(realFinishtTime);			
			targetVm.setReadyTime(finishTimeWithConfidency);
			targetVm.setFinishTime(finishTimeWithConfidency);			
			targetVm.getWTaskList().add(scheduleTask);
			
			if(targetVm.getExecutingWTask().getTaskId().equals("initial"))
			{//Allocate the task to service instance as executing task
				targetVm.setExecutingWTask(scheduleTask);
			}
			else
			{//Allocate the task to service instance as waiting task
				targetVm.getWaitWTaskList().add(scheduleTask);
			}
			
		}
	}
	
	/**Calculate the real start time of task on service instance*/
	public double calculateRealStartTimeForWTask(WTask task, SaaSVm targetVm)
	{
		double realStartTime = StaticfinalTags.currentTime;		
		//Sort all the predecessors of task
		Collections.sort(task.getParentTaskList(), new PredWTaskComparatorByRealFinishTimeIncrease());
		for(ConstraintWTask parentCon: task.getParentTaskList()) //Check all its predecessors
		{
			SaaSVm parentTaskVm = parentCon.getWTask().getAllocateVm(); // The service instance that the predecessor is allocated
			double minFinishTransTime = Double.MAX_VALUE;
			
			if(!parentTaskVm.equals(targetVm)) // The case that the task and its predecessor are not on the same service instance
			{//Consider the data transfer time
				double startCommTime = StaticfinalTags.currentTime;
				if(parentCon.getWTask().getRealFinishTime() > startCommTime)
				{
					startCommTime = parentCon.getWTask().getRealFinishTime();
				}
				minFinishTransTime = startCommTime + parentCon.getRealDataSize()/StaticfinalTags.bandwidth;				
			}
			else //The case that the task and its predecessor are on the same service instance
			{//The data transfer time is ignored
				if(parentCon.getWTask().getFinishFlag())
				{
					minFinishTransTime = StaticfinalTags.currentTime;
				}
				else
				{
					minFinishTransTime = parentCon.getWTask().getRealFinishTime();
				}				
			}
													
			if(minFinishTransTime > realStartTime)
			{//Update the real start time
				realStartTime = minFinishTransTime;														
			}
		}	
		
		if(!targetVm.getExecutingWTask().getTaskId().equals("initial"))
		{
			if(realStartTime < targetVm.getExecutingWTask().getRealFinishTime())
			{
				realStartTime = targetVm.getExecutingWTask().getRealFinishTime();
			}
			
			for(WTask waitTask: targetVm.getWaitWTaskList())
			{
				if(waitTask.getRealFinishTime() > realStartTime)
				{
					realStartTime = waitTask.getRealFinishTime();
				}				
			}
		}
		
		if(realStartTime < targetVm.getVmStartWorkTime())
		{
			realStartTime = targetVm.getVmStartWorkTime();
		}
				
		return realStartTime;
	}
	
	/**Obtain the ready tasks in the new arrival workflows*/
	public List<WTask> getReadyWTaskFromNewWorkflows(List<Workflow> newWorkFlows)
	{
		List<WTask> readyTaskList = new ArrayList<WTask>();
		for(Workflow newWorkflow: newWorkFlows) // Check all the workflows
		{				
			for(WTask tempWTask: newWorkflow.getTaskList()) // Check each task in a workflow
			{
				if(tempWTask.getParentTaskList().size() == 0) // If a task has no precessor, it is defined as ready
				{
					readyTaskList.add(tempWTask);
				}
			}
		}
		return readyTaskList;
	}
	
	/**Set the rank for each task as the least start time, and the task with lower rank should be considered first*/
	public void rankWTasksForPRS(Workflow aWorkflow)
	{
		for(WTask rankTask: aWorkflow.getTaskList())
		{
			double rank = rankTask.getLeastFinishTime();
			rankTask.setPriority(rank);
		}
	}
	
	/**Calculate the least finish time for each workflow task*/
	public static void CaculateLeastFinishTimeForWTask(Workflow tempWorkflow)
	{
		double[] canFactor = new double [] {8.0, 4.0, 2.0, 1.0};
		
		//Test from the type of service instance with lower capacity
		for(int f=0; f<canFactor.length; f++)
		{
			double factor = canFactor[f];
			
			//Calculate the least start and finish time of each task
			CalculateTaskLeastStartAndFinishTime(tempWorkflow, factor);
			
			double leastFinishTime = 0; //The least finish time of the workflow
			for(WTask cTask: tempWorkflow.getTaskList())
			{
				if(cTask.getLeastFinishTime() > leastFinishTime)
				{
					leastFinishTime = cTask.getLeastFinishTime();
				}
			}
			
			//If the least finish time of the workflow is less than its daedline
			if(leastFinishTime <= tempWorkflow.getDeadline())
			{
				//The laxity time between the daedline and the least finish time
				double laxity = tempWorkflow.getDeadline() - leastFinishTime;
				for(WTask task: tempWorkflow.getTaskList())
				{
					double tempExecutionTime = task.getLeastFinishTime() - task.getLeastStartTime();
					tempExecutionTime = tempExecutionTime + laxity*((factor*task.getExecutionTimeWithConfidency())/(leastFinishTime-StaticfinalTags.currentTime));
					task.setWTaskAllowExecutionTime(tempExecutionTime);
					
				}
				
				
				List<WTask> calculatedTaskList = new ArrayList<WTask>();
				double currentTime = StaticfinalTags.currentTime; //Current time
				while(true)
				{
					for(WTask task: tempWorkflow.getTaskList())// Check all the tasks
					{
						if(task.getWTaskSubDeadline() > -1) 
						{
							continue;
						}
						
						double executionTime = task.getWTaskAllowExecutionTime(); 	
						if(task.getParentTaskList().size() == 0)
						{// The task without predecessor
							task.setWTaskSubDeadline(currentTime+executionTime);
							calculatedTaskList.add(task);
						}
						else 
						{// The task has predecessors
							double maxDataSize = 0; // The size of data that needs to be transfered
							for(ConstraintWTask pareCon: task.getParentTaskList())
							{
								if(pareCon.getDataSizeWithConfiden() > maxDataSize)
								{
									maxDataSize = pareCon.getDataSizeWithConfiden();							
								}
							}
							//Data transfer time
							double commDelay = maxDataSize/StaticfinalTags.bandwidth;
							
							double maxStartTime = -1; //Least start time
							boolean unCalculatedParent = false; // Whether all the predecessors have been calculated
							for(ConstraintWTask pareCon: task.getParentTaskList())
							{//If all the predecessors have been calculated, calculate the start time
								if(pareCon.getWTask().getWTaskSubDeadline() > -1)
								{
									double startTime = pareCon.getWTask().getWTaskSubDeadline() + commDelay;
									if(startTime > maxStartTime)
									{
										maxStartTime = startTime;
									}
								}
								else
								{//There exist predecessors that are not calculated
									unCalculatedParent = true;
									break;
								}						
							}
							
							if(unCalculatedParent == false)
							{								
								task.setWTaskSubDeadline(maxStartTime + executionTime);
								calculatedTaskList.add(task);
							}																															
						}//end else
					}//end for(Task task: cWorkflow.getTaskList()) 
					
					if(calculatedTaskList.size() == tempWorkflow.getTaskList().size())
					{
						break;
					}
				}//end while
				
				break;
			}
			else // If the least finish time of the workflow is larger than its daedline
			{
				// Try next type of service instance with higher capacity
				if(f == canFactor.length-1)
				{// If this type of service instance has the highest capacity
					for(WTask task: tempWorkflow.getTaskList())
					{
						task.setWTaskSubDeadline(task.getLeastFinishTime());
					}
				}
				else
				{//Clear the least start/finish time
					for(WTask task: tempWorkflow.getTaskList())//Check all the tasks
					{
						task.setLeastStartTime(-1);
						task.setLeastFinishTime(-1);
					}
				}	
			}	
		}
		
		for(WTask task: tempWorkflow.getTaskList()) // Set the least finish time for each task
		{
			task.setLeastFinishTime(task.getWTaskSubDeadline());
		}		
	} 
	
	
	/**Calculate the least finish and start time for each workflow task*/
	public static void CalculateTaskLeastStartAndFinishTime(Workflow cWorkflow, double factor)
	{
		List<WTask> calculatedTaskList = new ArrayList<WTask>();
		double currentTime = StaticfinalTags.currentTime; //Current time
		while(true)
		{
			for(WTask task: cWorkflow.getTaskList())//Check each task
			{
				if(task.getLeastFinishTime() > -1) // If a task has been calculated, then ignores it
				{
					continue;
				}
				
				double executionTime = factor*task.getExecutionTimeWithConfidency(); //The \alpha-quantile of the execution time
				if(task.getParentTaskList().size() == 0)
				{// For a task without predecessor
					task.setLeastStartTime(currentTime); //Its start time is set as current time
					task.setLeastFinishTime(currentTime+executionTime);
					calculatedTaskList.add(task);
				}
				else //For a task having predecessors
				{	
					//Determine the size of data that needs to be transfered
					double maxDataSize = 0;
					for(ConstraintWTask pareCon: task.getParentTaskList())
					{
						if(pareCon.getDataSizeWithConfiden() > maxDataSize)
						{
							maxDataSize = pareCon.getDataSizeWithConfiden();							
						}
					}
					//The data transfer time
					double commDelay = maxDataSize/StaticfinalTags.bandwidth;
					
					double maxStartTime = -1; //The least start time
					boolean unCalculatedParent = false; //Whether exists un-calculated predecessors
					for(ConstraintWTask pareCon: task.getParentTaskList())
					{//Check whether all the predecessors of this task have been calculated
						if(pareCon.getWTask().getLeastFinishTime()>-1)
						{
							double startTime = pareCon.getWTask().getLeastFinishTime() + commDelay;
							if(startTime > maxStartTime)
							{
								maxStartTime = startTime;
							}
						}
						else
						{//If there exist un-calculated predecessors
							unCalculatedParent = true;
							break;
						}						
					}
					
					if(unCalculatedParent == false)
					{//If all the predecessors of this task have been calculated, then calculate the start/finish time for this task
						task.setLeastStartTime(maxStartTime);
						task.setLeastFinishTime(maxStartTime+executionTime);
						calculatedTaskList.add(task);
					}																															
				}//end else
			}//end for(Task task: cWorkflow.getTaskList()) 
			
			if(calculatedTaskList.size() == cWorkflow.getTaskList().size())
			{// If all the tasks in this workflow have been calculated, then stop
				break;
			}
		}//end while
		
	}	
	
	
	/**Calculate and set the \alpha-quantile of the base execution time*/
	public static void calculateTaskBaseExecutionTimeWithConfidency(List<Workflow> list)
	{
		for(Workflow tempWorkflow: list) // For all the workflows in list
		{
			for(WTask tempTask: tempWorkflow.getTaskList()) // For each task in a workflow
			{
				double standardDeviation = tempTask.getBaseExecutionTime()*StaticfinalTags.standardDeviation;				
				double executionTimeWithConfidency = tempTask.getBaseExecutionTime() + standardDeviation; // *getQuantile(StaticfinalTags.confidency);
				tempTask.setExecutionTimeWithConfidency(executionTimeWithConfidency);
			}
		}
	}
	
	/**Calculate and set the \beta-quantile of data size among tasks*/
	public static void calculateBaseDataSizeWithConfidency(List<Workflow> list)
	{
		for(Workflow tempWorkflow: list)//for all the workflows in the list
		{
			for(WTask tempTask: tempWorkflow.getTaskList()) // Check each task in a workflow
			{				
				for(ConstraintWTask parentCon: tempTask.getParentTaskList())
				{// The input data
					double standardDeviationDS = parentCon.getBaseDataSize()*StaticfinalTags.standardDeviation;
					int dataSizeWithConfidency = (int)(parentCon.getBaseDataSize() + standardDeviationDS); // *getQuantile(StaticfinalTags.confidency));
					parentCon.setDataSizeWithConfiden(dataSizeWithConfidency);
				}
			}
			
			for(WTask tempTask: tempWorkflow.getTaskList()) // Check each task in a workflow
			{				
				for(ConstraintWTask successorCon: tempTask.getSuccessorTaskList())
				{// The output data
					String thisTaskID = tempTask.getTaskId();
					for(ConstraintWTask parentCon: successorCon.getWTask().getParentTaskList())
					{
						if(thisTaskID.equals(parentCon.getWTask().getTaskId()))
						{
							int dataSizeWithConfi = parentCon.getDataSizeWithConfiden();
							successorCon.setDataSizeWithConfiden(dataSizeWithConfi);
						}
					}
				}
			}
			
		}
	}
	
	/**Sort workflow tasks in an increasing order according to their least start time*/
	private class WTaskComparatorByLeastStartTimeIncrease implements Comparator<WTask>
	{
		public int compare(WTask cl1, WTask cl2)
		{
			if(cl1.getLeastStartTime() > cl2.getLeastStartTime())
			{
				return 1;
			}
			else if(cl1.getLeastStartTime() < cl2.getLeastStartTime())
			{
				return -1;
			}
			else
			{
				return 0;
			}
		}
	}
	
	/**Sort predecessor tasks in an increasing order according to their real finish time*/
	private class PredWTaskComparatorByRealFinishTimeIncrease implements Comparator<ConstraintWTask>
	{
		public int compare(ConstraintWTask cl1, ConstraintWTask cl2)
		{
			if(cl1.getWTask().getRealFinishTime() > cl2.getWTask().getRealFinishTime())
			{
				return 1;
			}
			else if(cl1.getWTask().getRealFinishTime() < cl2.getWTask().getRealFinishTime())
			{
				return -1;
			}
			else
			{
				return 0;
			}
		}
	}
	
	/**Obtian the set of service instance*/
	public List<SaaSVm>  getVmList() {return vmList; }
	
	/**Obtain the set of workflows*/
	public List<Workflow> getWorkflowList() {return workflowList; }
			
	/**Submit the set of workflows*/
	public void submitWorkflowList(List<Workflow> list)
	{
		getWorkflowList().addAll(list);
	}
	
	/**Initialize */
	public void initialVmList(int num)
	{
		List<SaaSVm> initialVms = new ArrayList<SaaSVm>();
		for(int i=0; i<num; i++)
		{
			int level = i%4;
			SaaSVm initialVm = scaleUpVm(i, 0, level);
			initialVms.add(initialVm);
		}
		getVmList().addAll(initialVms);		
	}
	
	/**
	 * Generate a service instance based on the selected type
	 * @param level refers to different types of service instance£¬For instance, 0: t2.xlarge; 1: t2.large; 2: t2.medium; 3: t2.small;
	 * @return Create a new service instance
	 */
	public SaaSVm scaleUpVm(int vmId, double startTime, int level)
	{
		SaaSVm tempVm = null;
		switch(level)
		{
		case 0: //Generate a service instance with the type of t2.xlarge
			tempVm = new SaaSVm(vmId, "t2.xlarge", startTime, 0.1856, 1.0);
			break;
		case 1:		
			tempVm = new SaaSVm(vmId, "t2.large", startTime, 0.0928, 2.0);
			break;
		case 2:		
			tempVm = new SaaSVm(vmId, "t2.medium", startTime, 0.0464, 4.0);
			break;
		case 3: 
			tempVm = new SaaSVm(vmId, "t2.small", startTime, 0.023, 8.0);	
			break;	
		default:
			System.out.println("Warming: Only level= 0 1 2 3 are valid!");
		}		
		return tempVm;
	}
	
	/**Obtain quantile£ºonly level = 0.7 0.75 0.8 0.85 0.9 0.95 0.99 can be gained£¡*/
	public static double getQuantile(double level)
	{
		double quantile = 0;
		int aa = (int)(level*100);
		switch(aa)
		{
		case 70:
			quantile = 0.53;
			break;
		case 75:
			quantile = 0.68;
			break;
		case 80:
			quantile = 0.85;
			break;
		case 85:
			quantile = 1.04;
			break;
		case 90:
			quantile = 1.29;
			break;
		case 95:
			quantile = 1.65;
			break;
		case 99:
			quantile = 2.33;
			break;			
		default:
			System.out.println("Warming: Only level= 0.7 0.75 0.8 0.85 0.9 0.95 0.99 are valid!");
		}		
		return quantile;
	}
}
