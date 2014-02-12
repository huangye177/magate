package ch.hefr.gridgroup.magate.im;

import java.util.LinkedList;

/**
 * Interface JobSubmitter. The expected behaviors of a implemented Job Submitter.
 */
public interface IJobSubmitter {

	/**
	 * Calculate this JobSubmitter's total tardiness of all finished Jobs.
	 * This method should be only executed once, after all the Jobs are finished
	 * 
	 * @return numOfSUCCESS jobs
	 */
	public abstract int sumTotalJobTardiness();

	/** Number of total jobs */
	public abstract int getNumOfTotalJob();

	/** Total job tardiness time */
	public abstract double getTotalTardiness();

}


