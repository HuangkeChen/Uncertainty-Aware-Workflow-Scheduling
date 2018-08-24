package main;

import share.*;
import workflow.WorkflowProducer;

public class AutoExperiment 
{
	private static final int n = 18; // number of the variables
	private static final int AlCount = 1; // number of algorithms
	
	
	/**Record the Results of Cost*/
	private static double totalCost[][]= new double [AlCount][n];// average value
	private static double costUpper[][]= new double [AlCount][n];// upper value
	private static double costLower[][]= new double [AlCount][n];// lower value
	
	/**Record the Results of Deviation*/
	private static double deviation[][] = new double [AlCount][n]; // average value	
	private static double deviationUpper[][] = new double [AlCount][n]; // upper value
	private static double deviationLower[][] = new double [AlCount][n]; // lower value
	
	/**Record the Results of Resource Utilization */
	private static double resourceUtilization[][] = new double [AlCount][n]; // average value	
	private static double RUUpper[][] = new double [AlCount][n]; // upper value
	private static double RULower[][] = new double [AlCount][n]; // lower value
	
		
	/**Record the Results of fairness*/
	private static double fariRU[][] = new double [AlCount][n]; // average value	
	private static double fariRUUpper[][] = new double [AlCount][n]; // upper value
	private static double fariRULower[][] = new double [AlCount][n]; // lower value
	
	/**Record the Results of schedule time*/
	private static double scheduleTime[][] = new double [AlCount][n]; // average value
	private static double scheduleTimeUpper[][] = new double [AlCount][n]; // upper value
	private static double scheduleTimeLower[][] = new double [AlCount][n]; // lower value
	
	
	public static void main(String[] args) throws Exception
	{				
		int repeatTime = 50; // Repeat times for each group of experiments
		int variableIndex = -1; // record of the index of the variable

//		for (int count = 1000; count <= 6001; count = count + 1000) // Test the impact of workflow count
//		for(double variance=0.2; variance<=0.51; variance=variance+0.05) // Test the impact of variance of task execution time
//		for(double varianceNetW=0.1; varianceNetW<=0.41; varianceNetW=varianceNetW+0.05) // Test the impact of variance of data transfer time
//		for(double deadline=1.5; deadline<=10.001; deadline=deadline+0.5) // Test the impact of deadlineBase	
		for(double arrivalRate=0.01; arrivalRate<=0.1801; arrivalRate=arrivalRate+0.01) // Test the impact of arrival rate
		{
			variableIndex++; // Update the index of the variable
//			StaticfinalTags.workflowNum = count;			
//			StaticfinalTags.standardDeviation = variance;
//			StaticfinalTags.standardDeviationData = varianceNetW;
//			StaticfinalTags.deadlineBase = deadline;
			StaticfinalTags.arrivalLamda = arrivalRate;
			
			// Initialize the space for storing results
			for (int i=0; i<AlCount; i++) 
			{
				// Cost
				totalCost[i][variableIndex] = 0; 
				costUpper[i][variableIndex] = 0;
				costLower[i][variableIndex] = Double.MAX_VALUE;
				
				// Resource Utilization
				resourceUtilization [i][variableIndex] = 0; 
				RUUpper [i][variableIndex] = 0; 
				RULower [i][variableIndex] = 1;
				
				// Deviation
				deviation [i][variableIndex] = 0;
				deviationUpper [i][variableIndex] = 0;
				deviationLower [i][variableIndex] = Double.MAX_VALUE;
				
				// fairness
				fariRU [i][variableIndex] = 0;
				fariRUUpper [i][variableIndex] = 0;
				fariRULower [i][variableIndex] = Double.MAX_VALUE;
				
				// Schedule time
				scheduleTime [i][variableIndex] = 0;
				scheduleTimeUpper [i][variableIndex] = 0;
				scheduleTimeLower [i][variableIndex] = Double.MAX_VALUE;
			}
			
			
			for (int r=0; r<repeatTime; r++)// Repeat many times for each group of parameters
			{	
				System.out.println("Workflow Count: "+StaticfinalTags.workflowNum+" DeadlineBase: " + StaticfinalTags.deadlineBase + " Lamda: " + StaticfinalTags.arrivalLamda + " Variance: " + StaticfinalTags.standardDeviation + " repeat "+(r+1)+" time START START START");
				System.out.println();
				
				// Generate workflows and write to the file 'produceWorkflow.txt'
				WorkflowProducer.main(null); 	
				
				for (int i=0; i<AlCount; i++) // Test the algorithm
				{
					
					
					
					StaticfinalTags.choose = i; // Choose the algorithm
					WorkflowExperiment.main(null); // Run the algorithm
					
					
					
					
					totalCost[i][variableIndex] += PerformanceValue.TotalCost;					
					resourceUtilization [i][variableIndex] += PerformanceValue.ResourceUtilization;
					deviation [i][variableIndex] += PerformanceValue.deviation;
					fariRU [i][variableIndex] += PerformanceValue.fairness;
					scheduleTime [i][variableIndex] += PerformanceValue.averageTimePerWTask;
					
					//Cost
					if(costUpper [i][variableIndex] < PerformanceValue.TotalCost)
					{//Update the upper
						costUpper [i][variableIndex] = PerformanceValue.TotalCost;
					}
					if(costLower [i][variableIndex] > PerformanceValue.TotalCost)
					{//Update the lower
						costLower [i][variableIndex] = PerformanceValue.TotalCost;
					}
					
					//Resource utilization
					if(RUUpper [i][variableIndex] < PerformanceValue.ResourceUtilization)
					{// Update the upper
						RUUpper [i][variableIndex] = PerformanceValue.ResourceUtilization;
					}
					if(RULower [i][variableIndex] > PerformanceValue.ResourceUtilization)
					{//Update the lower
						RULower [i][variableIndex] = PerformanceValue.ResourceUtilization;
					}
					
					// Deviation
					if(deviationUpper [i][variableIndex] < PerformanceValue.deviation)
					{// Update the upper
						deviationUpper [i][variableIndex] = PerformanceValue.deviation;
					}
					if(deviationLower [i][variableIndex] > PerformanceValue.deviation)
					{//Update the lower
						deviationLower [i][variableIndex] = PerformanceValue.deviation;
					}
					
					//fairness
					if(fariRUUpper [i][variableIndex] < PerformanceValue.fairness)
					{ // Update the upper
						fariRUUpper [i][variableIndex] = PerformanceValue.fairness;
					}
					if(fariRULower [i][variableIndex] > PerformanceValue.fairness)
					{//Update the lower
						fariRULower [i][variableIndex] = PerformanceValue.fairness;
					}
					
					//Schedule time
					if(scheduleTimeUpper [i][variableIndex] < PerformanceValue.averageTimePerWTask)
					{ // Update the upper
						scheduleTimeUpper [i][variableIndex] = PerformanceValue.averageTimePerWTask;
					}
					if(scheduleTimeLower [i][variableIndex] > PerformanceValue.averageTimePerWTask)
					{//Update the lower
						scheduleTimeLower [i][variableIndex] = PerformanceValue.averageTimePerWTask;
					}
					
					System.out.println();
				}				
				
				System.out.println("Workflow Count: "+StaticfinalTags.workflowNum+" DeadlineBase: " + StaticfinalTags.deadlineBase + " Lamda: " + StaticfinalTags.arrivalLamda + " Variance: " + StaticfinalTags.standardDeviation + " repeat "+(r+1)+" time END END END");
				System.out.println();
			}
			
			for (int i=0; i < AlCount; i++) //Calculate the average values for different indicators
			{
				totalCost[i][variableIndex] = totalCost[i][variableIndex]/repeatTime;
				resourceUtilization[i][variableIndex] = resourceUtilization[i][variableIndex]/repeatTime;
				deviation[i][variableIndex] = deviation[i][variableIndex]/repeatTime;
				fariRU [i][variableIndex] = fariRU [i][variableIndex]/repeatTime;
				scheduleTime [i][variableIndex] = scheduleTime [i][variableIndex]/repeatTime;
				
			}
		}
		
		
		//Output the experimental results	
		java.text.DecimalFormat fd = new java.text.DecimalFormat("0.0000");
		
		System.out.println("Average Cost:");
		for (int i=0; i<AlCount; i++)
		{
			System.out.println("Results for "+i+"-th algorithm:");
			for (int j=0; j<n; j++)
			{
				System.out.print(fd.format(totalCost[i][j] )+"  ");
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println("Cost Upper:");
		for (int i=0; i<AlCount; i++)
		{
			System.out.println("Results for "+i+"-th algorithm:");
			for (int j=0; j<n; j++)
			{
				System.out.print(fd.format(costUpper[i][j] )+"  "); 
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println("Cost Lower:");
		for (int i=0; i<AlCount; i++)
		{
			System.out.println("Results for "+i+"-th algorithm:");
			for (int j=0; j<n; j++)
			{
				System.out.print(fd.format(costLower[i][j] )+"  "); 
			}
			System.out.println();
		}
		
		
		System.out.println();
		System.out.println();
		System.out.println("Average Resource Utilization:");
		for (int i=0; i<AlCount; i++)
		{
			System.out.println("Results for "+i+"-th algorithm:");
			for (int j=0; j<n; j++)
			{
				System.out.print(fd.format( resourceUtilization[i][j] )+"  ");
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println();
		System.out.println("Resource Utilization Upper:");
		for (int i=0; i<AlCount; i++)
		{
			System.out.println("Results for "+i+"-th algorithm:");
			for (int j=0; j<n; j++)
			{
				System.out.print(fd.format( RUUpper[i][j] )+"  ");
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println();
		System.out.println("Resource Utilization Lower:");
		for (int i=0; i<AlCount; i++)
		{
			System.out.println("Results for "+i+"-th algorithm:");
			for (int j=0; j<n; j++)
			{
				System.out.print(fd.format( RULower[i][j] )+"  ");
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println();
		System.out.println("Average Deviation:");
		for (int i=0; i < AlCount; i++)
		{
			System.out.println("Results for "+i+"-th algorithm:");
			for (int j=0; j<n; j++)
			{
				System.out.print(fd.format( deviation[i][j] )+"  ");
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println("Deviation Upper:");
		for (int i=0; i < AlCount; i++)
		{
			System.out.println("Results for "+i+"-th algorithm:");
			for (int j=0; j<n; j++)
			{
				System.out.print(fd.format( deviationUpper[i][j] )+"  ");
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println("Deviation Lower:");
		for (int i=0; i < AlCount; i++)
		{
			System.out.println("Results for "+i+"-th algorithm:");
			for (int j=0; j<n; j++)
			{
				System.out.print(fd.format( deviationLower[i][j] )+"  ");
			}
			System.out.println();
		}
		
		
		
		System.out.println();
		System.out.println();
		System.out.println("Average Fairness:");
		for (int i=0; i < AlCount; i++)
		{
			System.out.println("Results for "+i+"-th algorithm:");
			for (int j=0; j<n; j++)
			{
				System.out.print(fd.format( fariRU[i][j] )+"  ");
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println("Fairness Upper:");
		for (int i=0; i < AlCount; i++)
		{
			System.out.println("Results for "+i+"-th algorithm:");
			for (int j=0; j<n; j++)
			{
				System.out.print(fd.format( fariRUUpper[i][j] )+"  ");
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println("Fairness Lower:");
		for (int i=0; i < AlCount; i++)
		{
			System.out.println("Results for "+i+"-th algorithm:");
			for (int j=0; j<n; j++)
			{
				System.out.print(fd.format( fariRULower[i][j] )+"  ");
			}
			System.out.println();
		}
		
		
		
		System.out.println();
		System.out.println();
		System.out.println("Average Schedule Time:");
		for (int i=0; i < AlCount; i++)
		{
			System.out.println("Results for "+i+"-th algorithm:");
			for (int j=0; j<n; j++)
			{
				System.out.print(fd.format( scheduleTime[i][j] )+"  ");
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println("Schedule Time Upper:");
		for (int i=0; i < AlCount; i++)
		{
			System.out.println("Results for "+i+"-th algorithm:");
			for (int j=0; j<n; j++)
			{
				System.out.print(fd.format( scheduleTimeUpper[i][j] )+"  ");
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println("Schedule Time Lower:");
		for (int i=0; i < AlCount; i++)
		{
			System.out.println("Results for "+i+"-th algorithm:");
			for (int j=0; j<n; j++)
			{
				System.out.print(fd.format( scheduleTimeLower[i][j] )+"  ");
			}
			System.out.println();
		}

	}
}