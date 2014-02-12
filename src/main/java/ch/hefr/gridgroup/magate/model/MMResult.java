package ch.hefr.gridgroup.magate.model;

import java.util.LinkedList;

import ch.hefr.gridgroup.magate.env.MaGateMessage;

public class MMResult {

	private String maGateIdentity;
	
	/** Total time used for making schedule generation, 
	 * i.e. time += Sum(clockAfterMakingSchedule - clockBeforeMakingSchedule) */
    private double totalSchedulingTime = 0.0;
    
    /** Total resource running time */
    private double resourceUptime = 0.0;
    
    /** Total job weight: LOOP all job (numberOfCPU for execution * job actual CPU time) */
    private double totalJobWeight = 0.0;
    
    /** Total time used to execute all the jobs = execution time + I/O time + etc */
    private double totalJobResponseTime = 0.0;
    
    private double totalJobWaitingTime = 0.0;

	/** Total weighted response time 
     * = (job weight * job response time) 
     * = (job used cpu number * job actual cpu time * job response time) */
    private double totalWeightedResponseTime = 0.0;
    
    private double totalJobCPUTime = 0.0;

	private double totalWeightedJobCPUTTime = 0.0;
    
	/** Total slowdown of jobs = job response time / job actual execution time */
    private double totalSlowdown = 0.0;
    
    /** Total weighted slowdown 
     * = (job weight * job slowdown) 
     * = (job used cpu number * job actual cpu time * job slowdown)
     */
    private double totalWeightedSlowdown = 0.0;
    
    /** Total tardiness of jobs processed by this matchMaker; FOR checking scheduling results */
    private double totalJobTardiness = 0.0;
    
	/** denotes queue/schedule strategy */
    private String matchMakerPolicy = MaGateMessage.PolicyFCFS;
    
    // --- local job category ---
    
    /** job is considered as successful if job.getStatus() == MaGateMessage.SUCCESS */
    private int numOfSuccessProcessedLocalJob = 0;
    
    /** job is considered as failed if: 
     * (1) no resource to sent, or (2) job.getStatus() != MaGateMessage.SUCCESS 
     */
    private int numOfFailedProcessedLocalJob = 0;
    
    /** Number of jobs submitted through submitter */
    private int numOfReceivedLocalJobs = 0;
 
    /** Total number of nondelayed jobs processed by this matchmaker; FOR checking scheduling results */
    private int totalNumOfNondelayedLocalJobs = 0; // old comments: /** deadline score */
    
    /** Total number of delayed jobs processed by this matchmaker; FOR checking scheduling results */
    private int totalNumOfDelayedLocalJobs = 0;
    
    // --- Community job category ---
    
    private int numOfSuccessProcessedCommunityJob = 0;

	private int numOfFailedProcessedCommunityJob = 0;
    
    private int numOfReceivedCommunityJobs = 0;
 
    private int totalNumOfNondelayedCommunityJobs = 0; // old comments: /** deadline score */
    
    private int totalNumOfDelayedCommunityJobs = 0;
    
    /** Constructor */
    public MMResult() {
    	
	}

	public String getMaGateIdentity() {
		return maGateIdentity;
	}

	public void setMaGateIdentity(String maGateIdentity) {
		this.maGateIdentity = maGateIdentity;
	}
	
    public String getMatchMakerPolicy() {
		return matchMakerPolicy;
	}

	public void setMatchMakerPolicy(String matchMakerPolicy) {
		this.matchMakerPolicy = matchMakerPolicy;
	}

	public int getNumOfSuccessProcessedLocalJob() {
		return numOfSuccessProcessedLocalJob;
	}

	public void setNumOfSuccessProcessedLocalJob(int numOfSuccessProcessedLocalJob) {
		this.numOfSuccessProcessedLocalJob = numOfSuccessProcessedLocalJob;
	}

	public int getNumOfFailedProcessedLocalJob() {
		return numOfFailedProcessedLocalJob;
	}

	public void setNumOfFailedProcessedLocalJob(int numOfFailedProcessedLocalJob) {
		this.numOfFailedProcessedLocalJob = numOfFailedProcessedLocalJob;
	}

	public double getTotalSchedulingTime() {
		return totalSchedulingTime;
	}

	public void setTotalSchedulingTime(double totalSchedulingTime) {
		this.totalSchedulingTime = totalSchedulingTime;
	}

	public double getNumOfReceivedLocalJobs() {
		return numOfReceivedLocalJobs;
	}

	public void setNumOfReceivedLocalJobs(int numOfReceivedLocalJobs) {
		this.numOfReceivedLocalJobs = numOfReceivedLocalJobs;
	}

	public int getTotalNumOfNondelayedLocalJobs() {
		return totalNumOfNondelayedLocalJobs;
	}

	public void setTotalNumOfNondelayedLocalJobs(int totalNumOfNondelayedLocalJobs) {
		this.totalNumOfNondelayedLocalJobs = totalNumOfNondelayedLocalJobs;
	}

	public int getTotalNumOfDelayedLocalJobs() {
		return totalNumOfDelayedLocalJobs;
	}

	public void setTotalNumOfDelayedLocalJobs(int totalNumOfDelayedLocalJobs) {
		this.totalNumOfDelayedLocalJobs = totalNumOfDelayedLocalJobs;
	}

	public double getResourceUptime() {
		return resourceUptime;
	}

	public void setResourceUptime(double resourceMakespan) {
		this.resourceUptime = resourceMakespan;
	}

	public double getTotalJobWeight() {
		return totalJobWeight;
	}

	public void setTotalJobWeight(double totalJobWeight) {
		this.totalJobWeight = totalJobWeight;
	}

	public double getTotalJobResponseTime() {
		return totalJobResponseTime;
	}

	public void setTotalJobResponseTime(double totalJobResponseTime) {
		this.totalJobResponseTime = totalJobResponseTime;
	}
    
    public double getTotalJobWaitingTime() {
		return totalJobWaitingTime;
	}

	public void setTotalJobWaitingTime(double totalJobWaitingTime) {
		this.totalJobWaitingTime = totalJobWaitingTime;
	}

	public double getTotalSlowdown() {
		return totalSlowdown;
	}

	public void setTotalSlowdown(double totalSlowdown) {
		this.totalSlowdown = totalSlowdown;
	}

	public double getTotalWeightedResponseTime() {
		return totalWeightedResponseTime;
	}

	public void setTotalWeightedResponseTime(double totalWeightedResponseTime) {
		this.totalWeightedResponseTime = totalWeightedResponseTime;
	}

	public double getTotalWeightedSlowdown() {
		return totalWeightedSlowdown;
	}

	public void setTotalWeightedSlowdown(double totalWeightedSlowdown) {
		this.totalWeightedSlowdown = totalWeightedSlowdown;
	}

	public double getTotalJobTardiness() {
		return totalJobTardiness;
	}

	public void setTotalJobTardiness(double totalJobTardiness) {
		this.totalJobTardiness = totalJobTardiness;
	}

	/// community job's behavior
	
	public int getNumOfSuccessProcessedCommunityJob() {
		return numOfSuccessProcessedCommunityJob;
	}

	public void setNumOfSuccessProcessedCommunityJob(
			int numOfSuccessProcessedCommunityJob) {
		this.numOfSuccessProcessedCommunityJob = numOfSuccessProcessedCommunityJob;
	}

	public int getNumOfFailedProcessedCommunityJob() {
		return numOfFailedProcessedCommunityJob;
	}

	public void setNumOfFailedProcessedCommunityJob(
			int numOfFailedProcessedCommunityJob) {
		this.numOfFailedProcessedCommunityJob = numOfFailedProcessedCommunityJob;
	}

	public int getNumOfReceivedCommunityJobs() {
		return numOfReceivedCommunityJobs;
	}

	public void setNumOfReceivedCommunityJobs(int numOfReceivedCommunityJobs) {
		this.numOfReceivedCommunityJobs = numOfReceivedCommunityJobs;
	}

	public int getTotalNumOfNondelayedCommunityJobs() {
		return totalNumOfNondelayedCommunityJobs;
	}

	public void setTotalNumOfNondelayedCommunityJobs(
			int totalNumOfNondelayedCommunityJobs) {
		this.totalNumOfNondelayedCommunityJobs = totalNumOfNondelayedCommunityJobs;
	}

	public int getTotalNumOfDelayedCommunityJobs() {
		return totalNumOfDelayedCommunityJobs;
	}

	public void setTotalNumOfDelayedCommunityJobs(int totalNumOfDelayedCommunityJobs) {
		this.totalNumOfDelayedCommunityJobs = totalNumOfDelayedCommunityJobs;
	}
	
//	public double getTotalJobResponseTime_community() {
//		return totalJobResponseTime_community;
//	}
//
//	public void setTotalJobResponseTime_community(
//			double totalJobResponseTime_community) {
//		this.totalJobResponseTime_community = totalJobResponseTime_community;
//	}
//
//	public double getTotalWeightedResponseTime_community() {
//		return totalWeightedResponseTime_community;
//	}
//
//	public void setTotalWeightedResponseTime_community(
//			double totalWeightedResponseTime_community) {
//		this.totalWeightedResponseTime_community = totalWeightedResponseTime_community;
//	}
//
//	public double getTotalSlowdown_community() {
//		return totalSlowdown_community;
//	}
//
//	public void setTotalSlowdown_community(double totalSlowdown_community) {
//		this.totalSlowdown_community = totalSlowdown_community;
//	}
//
//	public double getTotalWeightedSlowdown_community() {
//		return totalWeightedSlowdown_community;
//	}
//
//	public void setTotalWeightedSlowdown_community(
//			double totalWeightedSlowdown_community) {
//		this.totalWeightedSlowdown_community = totalWeightedSlowdown_community;
//	}
	
	public double getTotalJobCPUTime() {
		return totalJobCPUTime;
	}

	public void setTotalJobCPUTime(double totalJobCPUTime) {
		this.totalJobCPUTime = totalJobCPUTime;
	}

	public double getTotalWeightedJobCPUTTime() {
		return totalWeightedJobCPUTTime;
	}

	public void setTotalWeightedJobCPUTTime(double totalWeightedJobCPUTTime) {
		this.totalWeightedJobCPUTTime = totalWeightedJobCPUTTime;
	}
	
}


