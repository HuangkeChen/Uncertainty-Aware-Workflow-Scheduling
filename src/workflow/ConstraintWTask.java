package workflow;

import java.io.Serializable;

/**Link tasks with storage location*/
public class ConstraintWTask implements Serializable
{
	private final WTask wTask; //The linked tasks
	private final int dataSize; //The size of data between the linked tasks
	
	private final int baseDataSize; // Average size of data
	private int dataSizeWithConfiden; // The \belta-quantile of the data size
	private int realDataSize; // Real size of data
	
	/**The constraints between tasks using task instance, rather than the ID*/
	public ConstraintWTask(WTask task, int dataSize)
	{
		this.wTask = task;
		this.dataSize = dataSize;
		
		this.baseDataSize = dataSize;
		this.dataSizeWithConfiden = -1;
		this.realDataSize = -1;
	}
	
	public WTask getWTask(){return wTask;}
	
	public int getDataSize(){return dataSize;}
	
	/**Obtain the average data size*/
	public int getBaseDataSize(){return baseDataSize;}
	
	/**Obtain the \belta-quantile of data size*/
	public int getDataSizeWithConfiden(){return dataSizeWithConfiden;}
	/**Set the \belta-quantile of data size*/
	public void setDataSizeWithConfiden(int rDS)
	{
		this.dataSizeWithConfiden = rDS;
	}
	
	/**Obtain the real size of data*/
	public int getRealDataSize(){return realDataSize;}
	/**Set the real size of data*/
	public void setRealDataSize(int rDS)
	{
		this.realDataSize = rDS;
	}
	
}
