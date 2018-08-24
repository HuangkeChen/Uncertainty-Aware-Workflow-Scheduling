package share;

/**
 * @author  hkchen--2014.10.12
 */
public final class StaticfinalTags 
{
	/**The index of the algorithm*/
	public static int choose = 0;
	
	/**The number of the workflows*/	
	public static int workflowNum = 4000;
	
	/**The arrival rate*/
	public static double arrivalLamda = 0.1;
	
	/**The deadline of the workflow*/
	public static double deadlineBase = 10.0; // 2.0
	
	/**Variance of task execution time*/
	public static double standardDeviation = 0.2;
	
	/**Variance of data size*/
	public static double standardDeviationData = 0.1; 
	
	/**The current time*/
	public static double currentTime = -1;
	
	/**The number of service instance when initializing*/
	public static int initialVmNum = 5;  //
	
	/**The overheads of creating a new service instance*/
	public static int createVmTime = 30;
	
	public static double confidency = 0.85;
	
	/**bandwidth among service instances*/
	public static double bandwidth = 1000000;
	
	/**The maximal allowable idle time for service instances*/
	public static int maxIdleTime = 2200;
	
	/**The number of template of service instance*/
	public static int vmTempNum = 4;
}
