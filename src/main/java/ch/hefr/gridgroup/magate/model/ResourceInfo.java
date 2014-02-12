package ch.hefr.gridgroup.magate.model;

import gridsim.ResourceCharacteristics;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import ch.hefr.gridgroup.magate.model.JobInfo;

public interface ResourceInfo {

	/** Resource char. object - used to get information about Resource*/
	public abstract ResourceCharacteristics getResource();

	/** Resource char. object - used to get information about Resource*/
	public abstract void setResource(ResourceCharacteristics resource);

	/** Returns number of unfinished jobs on Resource */
	public abstract int getNumOfUnfinishedCommittedJob();

	/** 
	 * Check whether the Resource has same architecture and OS as JobInfo requires
	 */
	public abstract boolean checkResourceSuitableForJob(JobInfo gi);

	/** 
	 * Removes JobInfo from list of "JobInfo list on Resource" (only JobInfo there not Job)
	 * @param gi jobInfo to be removed
	 */
	public abstract void lowerResInExec(JobInfo gi);

	/** 
	 * Removes JobInfo from Resource schedule
	 */
	public abstract void lowerResScheduleList(JobInfo gi);

	/**
	 * Gets the number of free CPUs on a resource
	 */
	public abstract int getNumOfFreePE();

	/**
	 * This method update information about schedule such as - tardiness and number of nondelayed jobs.
	 * It can be easily modified to provide information about makespan etc. If there is no change since last computation
	 * it is not performed to save time.
	 *
	 */
	public abstract void update(double currentTime);

	/**
	 * This method updates all information about schedule even when no change appear - more time overhead
	 *
	 */
	public abstract void forceUpdate(double current_time);

	/**
	 * Get the earliest available start time of a specific JobInfo
	 */
	public abstract double getEarliestStartTimeForJobInfo(JobInfo gi,
			double currentTime);

	/**
	 * Schedule only method (not to be used with queue-based algorithms) - finds first available (gap) hole for JobInfo.
	 * If the hole is found, the JobInfo is moved there to increase the resource usage.
	 * @param JobInfo - the JobInfo to be moved.
	 */
	public abstract boolean findHoleForJobInfo(JobInfo gi);

	/**
	 * Remove a specific JobInfo from job-in-schedule list 
	 * Once schedule is changed it is not stable until update method is called
	 */
	public abstract boolean removeJobInfo(JobInfo gi);

	/**
	 * Remove a JobInfo from job-in-schedule list with specific index
	 * Once schedule is changed it is not stable until update method is called
	 */
	public abstract JobInfo removeJobInfoIndex(int index);

	/**
	 * Remove the first JobInfo from job-in-schedule list 
	 * Once schedule is changed it is not stable until update method is called
	 */
	public abstract JobInfo removeFirstJobInfo();

	/**
	 * Append a job to the last position of job-in-schedule list 
	 * Once schedule is changed it is not stable until update method is called
	 */
	public abstract void addLastJobInfo(JobInfo gi);

	/**
	 * Append a job position of job-in-schedule list with specific index
	 * Once schedule is changed it is not stable until update method is called
	 */
	public abstract void addJobInfo(int index, JobInfo gi);

	/**
	 * Add job to resource's job-in-exec queue
	 * Once schedule is changed it is not stable until update method is called
	 */
	public abstract void addJobInfoInExec(JobInfo gi);

	/** List representing schedule for this resource (jobInfos) */
	public abstract LinkedList getJobListInSchedule();

	/** denotes Earliest Start Time */
	public abstract double getEarliestStartTime();

	/** Sum of tardiness of all finished jobs */
	public abstract double getTotalTardinessOfFinishedJobs();

	/** Sum of tardiness of all finished jobs */
	public abstract void setTotalTardinessOfFinishedJobs(
			double totalTardinessOfFinishedJobs);

	/** denotes number of previous finished non-delayed jobs */
	public abstract void setNumOfPreviousFinishedNondelayedJobs(
			int numOfPreviousFinishedNondelayedJobs);

	/** denotes number of previous finished non-delayed jobs */
	public abstract int getNumOfPreviousFinishedNondelayedJobs();

	/** 
     * Denotes if previously selected job was succesfully sent by JobSubmitter - prevents anticipating of jobs 
     * Will be set "true" in MatchMaker.jobAlreadySentToResource(),
     * and set "false" when jobinfo are simply scheduled
     */
	public abstract AtomicBoolean isPrevSelectedJobSentSuccessful();

	/** 
     * Denotes if previously selected job was succesfully sent by JobSubmitter - prevents anticipating of jobs 
     * Will be set "true" in MatchMaker.jobAlreadySentToResource(),
     * and set "false" when jobinfo are simply scheduled
     */
	public abstract void setPrevSelectedJobSentSuccessful(
			AtomicBoolean prevSelectedJobSentSuccessful);

	/** Denotes the total number of PE on Resource */
	public abstract int getNumOfTotalPE();

    public AtomicInteger getNumOfVirtualFreePE();

	public void setNumOfVirtualFreePE(AtomicInteger numOfVirtualFreePE);
}


