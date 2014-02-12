package ch.hefr.gridgroup.magate.km;

import java.io.ObjectInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.MaGateEntity;
import ch.hefr.gridgroup.magate.env.JobCenterManager;
import ch.hefr.gridgroup.magate.env.MaGateMediator;
import ch.hefr.gridgroup.magate.env.MaGateMessage;
import ch.hefr.gridgroup.magate.env.MaGateParam;
import ch.hefr.gridgroup.magate.env.MaGatePlatform;
import ch.hefr.gridgroup.magate.env.MaGateToolkit;
import ch.hefr.gridgroup.magate.ext.JobComparator;
import ch.hefr.gridgroup.magate.ext.LengthComparator;
//import ch.hefr.gridgroup.magate.model.JobItem;
import ch.hefr.gridgroup.magate.model.MMResult;
import ch.hefr.gridgroup.magate.model.NeighborItem;
import ch.hefr.gridgroup.magate.model.ResourceInfo;
import ch.hefr.gridgroup.magate.model.JobInfo;
import ch.hefr.gridgroup.magate.storage.GlobalStorage;

/**
 * Class MatchMaker represents the Scheduler. It receives jobs sent from MatchMakerController, 
 * and launch corresponding algorithm(s) to make the scheduling decision.
 * Presently, implemented algorithms includes FCFS, Easy Backfilling, Flexible Backfilling, 
 * EDF, EG-EDF, and Tabu search, 
 * which are originally developed within <b>Alea simulator</b> written by Dalibor Klusacek
 * 
 * JobInfo represents a job with dynamic information. ResourceInfo represents a resource 
 * with dynamic information.
 * Scheduling decision is placed in ResourceInfo aussi.
 * 
 * @author Ye HUANG
 */
public class MatchMaker {
	
	private String maGateIdentity;
	
	private MaGateEntity maGate;

//	/** List of separated schedules of resources */
//    private LinkedList scheduleList;
    
    private int tempCount = 0;
    
    private int tempCount2 = 0;
    
    /** Number of already made schedulers by this MatchMaker */
    private int numOfExistingSchedules = 0;
    
	/** Total time used for making schedule generation, 
	 * i.e. time += Sum(clockAfterMakingSchedule - clockBeforeMakingSchedule) */
    private double totalSchedulingTime = 0.0;
    
    /** Clock/Time before making a single scheduling */
    private double clockBeforeMakingSchedule = 0.0;
    
    /** Clock/Time after making a scheduling */
    private double clockAfterMakingSchedule = 0.0;
    
    /** Tabu list of gridlets already moved by Tabu Search, EXISING in useSchedule() */
    private LinkedList tabuGridlets = new LinkedList();
    
    /** Total tardiness of jobs processed by this matchMaker; FOR checking scheduling results */
    private double totalJobTardiness = 0.0;
    
    /** incoming job queue */
//    private Vector<String> localJobQueue = new Vector<String>();
    private CopyOnWriteArrayList<String> localJobQueue = new CopyOnWriteArrayList<String>();
    private ConcurrentHashMap<String, Double> shadowJobQueue = new ConcurrentHashMap<String, Double>();

	/** Number of jobs waiting for scheduling decision
     *  It will be decrease only if JobSubmiiter inform MatchMaker 
     *  that the job is already sent to resource
     */
    private int numOfJobWaitingForSchedule = 0;
    
	/** Start time of the simulation; FOR checking scheduling results */
    private double simulationStartTime = -10.0;

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
    
    /** denotes queue/schedule strategy */
    private String matchMakerPolicy = MaGateMessage.PolicyFCFS;
    
    /** denotes  time required to select job*/
    String timeToSelectJobText = "";
    
    /** denotes time required to add job into queue/schedule */
    String timeToAddJobToScheduleQueueText = "";

    // --- local job category ---
    /** job is considered as successful if job.getStatus() == MaGateMessage.SUCCESS */
    private int numOfSuccessProcessedLocalJob = 0;
    
    /** job is considered as failed if: 
     * (1) no resource to sent, or (2) job.getStatus() != MaGateMessage.SUCCESS 
     */
    private int numOfFailedProcessedLocalJob = 0;
    
    /** Number of jobs submitted through submitter */
    private int numOfReceivedLocalJobs = 0;
    
    /** Number of jobs submitted through submitter */
    private int numOfReceivedCommunityJobs = 0;
    
    /** Total number of nondelayed jobs processed by this matchmaker; FOR checking scheduling results */
    private int totalNumOfNondelayedLocalJobs = 0; // old comments: /** deadline score */
    
    /** Total number of delayed jobs processed by this matchmaker; FOR checking scheduling results */
    private int totalNumOfDelayedLocalJobs = 0;
    
    // --- community job category ---
    /** job is considered as successful if job.getStatus() == MaGateMessage.SUCCESS */
    private int numOfSuccessProcessedCommunityJob = 0;
    
    /** job is considered as failed if: 
     * (1) no resource to sent, or (2) job.getStatus() != MaGateMessage.SUCCESS 
     */
    private int numOfFailedProcessedCommunityJob = 0;
    
    /** Total number of nondelayed jobs processed by this matchmaker; FOR checking scheduling results */
    private int totalNumOfNondelayedCommunityJobs = 0; // old comments: /** deadline score */
    
    /** Total number of delayed jobs processed by this matchmaker; FOR checking scheduling results */
    private int totalNumOfDelayedCommunityJobs = 0;
    
    private double avgQueuingTime = 0;
    
    private static Log log = LogFactory.getLog(MatchMaker.class);
    
//    private ConcurrentHashMap<String, String> receivedJobIdMap;
    private ConcurrentHashMap<String, String> undeliveredJobIdMap;
    
	/**
     * Creates a new instance of MatchMaker
     */
    public MatchMaker(MaGateEntity maGate, String localPolicy) throws Exception {
    	
    	this.maGate           = maGate;
    	this.maGateIdentity   = maGate.getMaGateIdentity();
    	
//        this.scheduleList     = new LinkedList();
        this.matchMakerPolicy = localPolicy;
//        this.receivedJobIdMap = new ConcurrentHashMap<String, String>();
        this.undeliveredJobIdMap = new ConcurrentHashMap<String, String>();
    }
    
    /**
     * Once end of batch of job processing, issued from JobSubmitter, through ModuleController 
     * (not necessary end of a simulation iteration, neither leave of all active JobSumitters)
     */
	public void caculateStatistic(double newArrivalTardiness) {
		
    	this.totalJobTardiness = newArrivalTardiness;
    	
    	double jobUsage = MaGateToolkit.convertAtomicLongToDouble(this.maGate.getStorage().getJobUsage());
        double resUsage = this.maGate.getStorage().getTotalNumOfPEs().get() * MaGateMediator.getSystemTime();
        
        this.maGate.getStorage().setResUsage(MaGateToolkit.convertDoubleToAtomicLong(resUsage));
        
        double resourceUtilization = jobUsage / resUsage;
        this.maGate.getStorage().setResourceUtilization(MaGateToolkit.convertDoubleToAtomicLong(resourceUtilization));
        
        
    	MMResult mmResult = new MMResult();
    	
    	mmResult.setMaGateIdentity(this.maGateIdentity);
        mmResult.setMatchMakerPolicy(this.matchMakerPolicy);
        mmResult.setTotalJobTardiness(this.totalJobTardiness);
        mmResult.setTotalJobWeight(this.totalJobWeight);
        
        mmResult.setTotalSchedulingTime(this.totalSchedulingTime);
        mmResult.setResourceUptime(MaGateMediator.getSystemTime());
        
        mmResult.setTotalJobResponseTime(this.totalJobResponseTime);
        mmResult.setTotalJobWaitingTime(this.totalJobWaitingTime);
        mmResult.setTotalWeightedResponseTime(this.totalWeightedResponseTime);
        mmResult.setTotalJobCPUTime(this.totalJobCPUTime);
        mmResult.setTotalWeightedJobCPUTTime(this.totalWeightedJobCPUTTime);
        mmResult.setTotalSlowdown(this.totalSlowdown);
        mmResult.setTotalWeightedSlowdown(this.totalWeightedSlowdown);
        
//        mmResult.setTotalJobResponseTime_community(this.totalJobResponseTime_community);
//        mmResult.setTotalWeightedResponseTime_community(this.totalWeightedResponseTime_community);
//        mmResult.setTotalSlowdown_community(this.totalSlowdown_community);
//        mmResult.setTotalWeightedSlowdown_community(this.totalWeightedSlowdown_community);
        
        // local job category
        mmResult.setNumOfFailedProcessedLocalJob(this.numOfFailedProcessedLocalJob);
        mmResult.setNumOfReceivedLocalJobs(this.numOfReceivedLocalJobs);
        mmResult.setNumOfSuccessProcessedLocalJob(this.numOfSuccessProcessedLocalJob);
        mmResult.setTotalNumOfDelayedLocalJobs(this.totalNumOfDelayedLocalJobs);
        mmResult.setTotalNumOfNondelayedLocalJobs(this.totalNumOfNondelayedLocalJobs);
        
        // community job category
        mmResult.setNumOfFailedProcessedCommunityJob(this.numOfFailedProcessedCommunityJob);
        mmResult.setNumOfReceivedCommunityJobs(this.numOfReceivedCommunityJobs);
        mmResult.setNumOfSuccessProcessedCommunityJob(this.numOfSuccessProcessedCommunityJob);
        mmResult.setTotalNumOfDelayedCommunityJobs(this.totalNumOfDelayedCommunityJobs);
        mmResult.setTotalNumOfNondelayedCommunityJobs(this.totalNumOfNondelayedCommunityJobs);
        
        this.maGate.getStorage().setMMResult(mmResult);
	}

	
    /******************************************
      access method for external invoking
     ******************************************/
	
	/** 
	 * Info event from ModuleController: job already sent to specific local resource, which is made by MatchMaker
	 */
	public void jobAlreadySentToLocalResource() {
		
		this.numOfJobWaitingForSchedule--;
        this.numOfExistingSchedules--;
        
        // do another scheduling round
        if(numOfExistingSchedules == 0){
            this.callSchedule();
        }
	}
	
	/** 
	 * Info event from ModuleController: local job can't be sent to specific resource (resource invalid or Id available), which is made by MatchMaker
	 */
	public void localJobNotSentToResource() {
		this.numOfFailedProcessedLocalJob += 1;
	}
	
	/** 
	 * Info event from ModuleController: community job can't be sent to specific resource (resource invalid or Id available), which is made by MatchMaker
	 */
	public void communityJobNotSentToResource() {
		this.numOfFailedProcessedCommunityJob += 1;
	}
	
	/** 
	 * Job is neither submitted to local resource nor to remote resources
	 */
	public void jobUndelivered(JobInfo gi) {
		
		if(this.undeliveredJobIdMap.containsKey(gi.getGlobalJobID())) {

			return;
			
		} else {
			
			if(gi.getOriginalMaGateId().equals(this.maGate.getMaGateIdentity())) {
				this.numOfReceivedLocalJobs++;
			} else {
				this.numOfReceivedCommunityJobs++;
			}
		}
		
	}

	/** 
	 * Job submitted from ModuleController to get scheduled
	 * It's also the place to calculate resource "execution load & max load" 
	 */
	public void jobScheduled(JobInfo gi) {
		
		if(gi.getOriginalMaGateId().equals(this.maGate.getMaGateIdentity())) {
			
			// Send local job's GlobalID to the MatchMaker's jobQueue
			this.localjobSubmitted(gi.getGlobalJobID());
			
		} else {
			
			// Send community job's GlobalID to the MatchMaker's jobQueue
			this.communityjobSubmitted(gi.getGlobalJobID());
		}
		
//		JobCenterManager.jobScheduling(gi.getGlobalJobID(), this.maGateIdentity, gi);
		
		
		// Regarding CASP callRequest() is followed by callINFORM(), a job just scheduled in callRequest() 
		// could be scheduled to the same node due to the delay of datastorage (database in current version) 
		// Therefore, it is necessary to check the local node's schedule to prevent duplicated job scheduling 
		// request processing
//		if(this.receivedJobIdMap.containsKey(gi.getGlobalJobID())) {
//			
//			int status = JobCenterManager.getJobStatusbyJobId(gi.getGlobalJobID());
//			if(status == JobCenterManager.SCHEDULING) {
//				return;
//			}
//			
//		} else {
//			
//			this.receivedJobIdMap.put(gi.getGlobalJobID(), gi.getGlobalJobID());
//			
//			if(gi.getOriginalMaGateId().equals(this.maGate.getMaGateIdentity())) {
//				
//				// Send local job's GlobalID to the MatchMaker's jobQueue
//				this.localjobSubmitted(gi.getGlobalJobID());
//				
//			} else {
//				
//				// Send community job's GlobalID to the MatchMaker's jobQueue
//				this.communityjobSubmitted(gi.getGlobalJobID());
//			}
//			
//			JobCenterManager.jobScheduling(gi.getGlobalJobID(), this.maGateIdentity, gi);
//		}
		
	}
	
	
	private void localjobSubmitted(String item) {
		
		this.numOfReceivedLocalJobs++;
        this.numOfJobWaitingForSchedule++;
        
        // ye: schedule phase 1: add job to queue
        // Put the job into job queue, the jobs in queue would be invoked by MatchMaker's scheduling policy
        this.insertJobToProcessingQueue(item);
        
    	// ye: schedule phase 2: making schedule based on specific algorithms for queued jobs
        this.callSchedule();
        
	}
	
	private void communityjobSubmitted(String item) {
		
		this.numOfReceivedCommunityJobs++;
        this.numOfJobWaitingForSchedule++;
        
        // ye: schedule phase 1: add job to queue
        // Put the job into job queue, the jobs in queue would be invoked by MatchMaker's scheduling policy
    	this.insertJobToProcessingQueue(item);
        
        // ye: schedule phase 2: making schedule based on specific algorithms for queued jobs
        this.callSchedule();
	}
	
	/**
	 * Insert a newly submitted job to the MatchMaker JobQueue, 
	 * with regard to the local policy, e.g., FCFS/SJF
	 * @param item
	 */
	private void insertJobToProcessingQueue(String item) {
		
		if(this.localJobQueue.contains(item)) {
			return;
		}
   		
		// Determine how the jobs are appended to queue according to adopted MatchMaker policy
		if(this.matchMakerPolicy.equals(MaGateMessage.PolicyFCFS)) {
			// FCFS
			this.addJobToLocalQueue(item);
			
		} else if(this.matchMakerPolicy.equals(MaGateMessage.PolicySJF)) {
			// SJF, jobQueue sorting appended 
			
			
			if(this.localJobQueue.size() == 0) {
				
				this.addJobToLocalQueue(item);
				
			} else {
				
				// fetch newjob's estimated execution time (EST)
				int index = -1;
				JobInfo newjobInfo = JobCenterManager.getJobInfobyJobId(item);
		   		double newEST = newjobInfo.getEstimatedComputationTime();
		   		
				for(int i = 0; i < this.localJobQueue.size(); i++) {
					
					JobInfo currentjobInfo = JobCenterManager.getJobInfobyJobId(this.localJobQueue.get(i));
			   		double currentEST = currentjobInfo.getEstimatedComputationTime();
					if(newEST <= currentEST){
						
						index = i;
						break;
					}
				} // end of comparison loop
				
				if(index == -1) {
					this.addJobToLocalQueue(item);
				} else {
					this.addJobToLocalQueue(index, item);
				}
			}
			
		} else if(this.matchMakerPolicy.equals(MaGateMessage.PolicyEasyBF)) {
			// FCFS-like queue for EASY Backfilling
			this.addJobToLocalQueue(item);
			
		} else {
			// FCFS queue is the default policy
			this.addJobToLocalQueue(item);
			
		}
		
	}
	
	private void addJobToLocalQueue(String jobId) {
		this.localJobQueue.add(jobId);
	}
	
	private void addJobToLocalQueue(int index, String jobId) {
//		this.localJobQueue.insertElementAt(jobId, index);
		this.localJobQueue.add(index, jobId);

	}

	private boolean removeJobFromLocalQueue(String jobId) {
		
		
		GlobalStorage.test_counter_3.incrementAndGet();
//		log.debug("GlobalStorage.test_counter_3.incrementAndGet(): " + 
//				GlobalStorage.test_counter_3.get());

//		JobInfo jobInfo = JobCenterManager.getJobInfobyJobId(jobId);
//		JobCenterManager.jobProcessing(jobInfo.getGlobalJobID(), this.maGateIdentity, jobInfo);
		

		return this.localJobQueue.remove(jobId);
	}
	
	/**
	 * Estimated processing time for already queued jobs on this node
	 * 
	 * @return
	 */
	public double estimatedTimeToExecuteLocalJobQueue() {
		
		double estimatedTime = 0;
		double load = 0;
		
		for(String jobId : localJobQueue) {
			JobInfo jobInfo = JobCenterManager.getJobInfobyJobId(jobId);
			load += jobInfo.getComputationalLength() * jobInfo.getNumPE();
		}
		
		estimatedTime = load / this.maGate.getStorage().getTotalNumOfPEs().get();
		
		return estimatedTime;
	}
	
	/**
	 * Estimated processing time for this node's queued jobs BEFORE the targeting job
	 * 
	 * @param targetjobId
	 * @return
	 */
	public double estimatedTimeToExecuteLocalJobQueue(String targetjobId) {
		
		double estimatedTime = 0;
		double load = 0;
		
		int index = localJobQueue.indexOf(targetjobId);
		
		for(int i = 0; i < index; i++) {
			JobInfo jobInfo = JobCenterManager.getJobInfobyJobId(localJobQueue.get(i));
			load += jobInfo.getComputationalLength() * jobInfo.getNumPE();
		}
		
		estimatedTime = load / this.maGate.getStorage().getTotalNumOfPEs().get();
		
		return estimatedTime;
	}
	
	
	/**
	 * Estimated processing time for already "promised" jobs by means of CASA-accept message on this node
	 * 
	 * @return
	 */
	public double estimatedTimeToExecuteShadowJobQueue() {
		
		double estimatedTime = 0;
		double load = 0;
		
		Iterator<Map.Entry<String, Double>> iter = shadowJobQueue.entrySet().iterator();
		
		while (iter.hasNext()) {
			Map.Entry<String, Double> entry = iter.next();
			String jobId = entry.getKey().trim();
			
			JobInfo jobInfo = JobCenterManager.getJobInfobyJobId(jobId);
			double acceptanceApproveProbability = entry.getValue().doubleValue();
			
			load += jobInfo.getComputationalLength() * jobInfo.getNumPE() * 
				acceptanceApproveProbability * MaGateParam.weightOfShadowJobQueue;
		}
		
		estimatedTime = load / this.maGate.getStorage().getTotalNumOfPEs().get();
		
		return estimatedTime;
	}
	
	/**
	 * reserve an acceptance decision
	 * 
	 * @param jobId
	 * @param acceptanceApproveProbability
	 */
	public void appendAcceptanceDecision(String jobId, double acceptanceApproveProbability) {
		this.shadowJobQueue.put(jobId, new Double(acceptanceApproveProbability));
	}
	
	/**
	 * revoke an acceptance decision
	 * 
	 * @param jobId
	 */
	public void revokeAcceptanceDecision(String jobId) {
		this.shadowJobQueue.remove(jobId);
	}
	
	/**
	 * Update the average queuing time of this node
	 */
	public void updateQueuingTime() {
		
		double queuingTime = 0;
		double numOfJobs   = 0;
		double systemTime  = MaGateMediator.getSystemTime();
		int sizeOfLocalJobQueue = localJobQueue.size();
		
		if(sizeOfLocalJobQueue > 0) {
			
			for(String jobId : localJobQueue) {
				
				JobInfo jobInfo = JobCenterManager.getJobInfobyJobId(jobId);
				
				double queuingStartTime = jobInfo.getQueuingStartTime();
				queuingTime += systemTime - queuingStartTime;
				
				jobInfo.setQueuingTime(systemTime - queuingStartTime);
			}
			numOfJobs += localJobQueue.size();
			
		} else {
			
			Vector<JobInfo> exedJobInfo = JobCenterManager.getJob_processingNode(this.maGateIdentity, JobCenterManager.EXECUTED); 
			for(JobInfo jobInfo : exedJobInfo) {
				queuingTime += jobInfo.getQueuingTime();
			}
			numOfJobs += exedJobInfo.size();
			
		}
		
		if(numOfJobs != 0) {
			this.avgQueuingTime = queuingTime / numOfJobs;
		} else {
			this.avgQueuingTime = 0;
		}
		
	}
	
	
	/**
     * Fetch one job, which may wait long time before getting scheduled and executed, for rescheduling processing
     * @return
     */
    public Vector<String> searchJobsForRescheduling() {
    	
    	int sizeOfLocalJobQueue = localJobQueue.size();
    	
    	if(sizeOfLocalJobQueue < 1) {
    		return null;
    	}
    	
    	double systemTime  = MaGateMediator.getSystemTime();
    	Vector<String> toRescheduleJobs = new Vector<String>();
    	
    	try {
    		
    		// update the this node's instant average job queuing time
        	this.updateQueuingTime();
        	
        	for(String jobId : localJobQueue) {
        		
    			JobInfo jobInfo = JobCenterManager.getJobInfobyJobId(jobId);
    			
    			// if job's requirement beyonds resource profile
    			if(!check_jobMatchResource(jobInfo)) {
    				toRescheduleJobs.add(jobId);
    				continue;
    			}
    			
    			double queuingStartTime = jobInfo.getQueuingStartTime();
    			double queuingTime = systemTime - queuingStartTime;
    			jobInfo.setQueuingTime(queuingTime);
    			
    			
    			if(this.avgQueuingTime != 0) {
    				double relativeQueuingDelay = queuingTime / this.avgQueuingTime;
    				
    				if(relativeQueuingDelay >= MaGateParam.systemReschedulingCoefficient) {
    					toRescheduleJobs.add(jobId);
    				}
    				
    			} else {
    				// if hosting node's avg queuing time is zero or unavailable, 
    				// then there is no need to reschedule jobs
    			}
    			
    		}
        	
        	for(int i = 0; i < toRescheduleJobs.size(); i++)  {  
        	
        		for (int j = i + 1; j < toRescheduleJobs.size(); j ++)  {
        			
        			if (JobCenterManager.getJobInfobyJobId(toRescheduleJobs.get(i)).getQueuingTime() < 
        					JobCenterManager.getJobInfobyJobId(toRescheduleJobs.get(j)).getQueuingTime())  {
        				
        				String tmpId = toRescheduleJobs.get(i);
        				toRescheduleJobs.set(i, toRescheduleJobs.get(j));
        				toRescheduleJobs.set(j, tmpId);
        			}
        		}  
        	}
        	
    	} catch (Exception e) {
    		e.printStackTrace();
    		System.exit(0);
    	}
    	
    	return toRescheduleJobs;
    	
    }
	
	
	/**
	 * Remove a jobItem from the MatchMaker and re-schedule it to a remote node
	 * @param jobId
	 * @return
	 */
	public synchronized boolean jobReschedule(String jobId) {
		
		if(this.localJobQueue.contains(jobId)) {
			
			if(jobId.equals(this.maGate.getMaGateIdentity())) {
				this.numOfReceivedLocalJobs--;
				
			} else {
				this.numOfReceivedCommunityJobs--;
			}
			
//			this.receivedJobIdMap.remove(jobId);
			this.numOfJobWaitingForSchedule--;
			
			return removeJobFromLocalQueue(jobId);
			
			
		} else {
			return false;
		}
	}
	
	/**
	 * Info event from ModuleController: job already finished by resource
	 */
	public void jobFinishedConfirmation(JobInfo jobInfo) {
		
		if(jobInfo.getOriginalMaGateId().equals(this.maGate.getMaGateIdentity())) {
			this.localJobFinishedConfirmation(jobInfo);
		} else {
			// Important: DO NOT trigger the communityJobFinishedConfirmation() method
			// Firstly, inform the original node with regards to the job successful completion
			// Then, the OutputRequester will invoke method communityJobFinishedConfirmation() with 
			// an appended queuedTime, in order to make a more accurate job response time
			this.maGate.getOutputRequester().JobDoneForRemoteNodes(jobInfo);
		}
	}
	
	/**
	 * Local Job executed
	 * @param jobInfo
	 */
    private void localJobFinishedConfirmation(JobInfo gi) {
    	
    	if(gi.getJobStatus() == MaGateMessage.JOB_SUCCESS) {
			this.numOfSuccessProcessedLocalJob += 1;
		} else {
			this.numOfFailedProcessedLocalJob += 1;
		}
		
		// single job tardiness 
        double jobTardiness = gi.getTardiness();
        
        // job response time
        // NOTICE: job is released at "gi.getJobStartTime()", doesn't mean the execution will start, it could be delayed
        double jobResponse = gi.getJob().getFinishTime() - gi.getArrivalTime();
        
        // ye: calculate & update total weighted and normal slow down
        // NOTICE: job getAcutalCPUTime reflect how much time used by a job (each required PE runs the same time)
        double jobWeight = gi.getNumPE() * gi.getJob().getActualCPUTime();
        double jobSlowdown = jobResponse / gi.getJob().getActualCPUTime();
        
        this.totalJobWeight 		   += jobWeight;
        
        this.totalJobResponseTime 	   += jobResponse;
        this.totalJobWaitingTime += jobResponse - gi.getJob().getActualCPUTime();
        
        this.totalWeightedResponseTime += jobWeight * jobResponse;
        this.totalJobCPUTime           += gi.getJob().getActualCPUTime();
        this.totalWeightedJobCPUTTime  += jobWeight * gi.getJob().getActualCPUTime();
        
        if(Double.isInfinite(jobSlowdown)) {
        	// to handling unexpected errors, e.g., no job actualCPUTime available, replace it by the mean (averaged) of job slowdown
        	// for example, after 500 job executed, if current total slowdown is 1000, 
        	// then the mean job slowdown is 2, thus the totalslowdown is add-up by 2 (another mean slowdown)
        	this.totalSlowdown 			   += this.totalSlowdown / (this.numOfSuccessProcessedLocalJob + this.numOfSuccessProcessedCommunityJob);
        	this.totalWeightedSlowdown     += this.totalWeightedSlowdown / (this.numOfSuccessProcessedLocalJob + this.numOfSuccessProcessedCommunityJob);
        } else {
        	this.totalSlowdown 			   += jobSlowdown;
        	this.totalWeightedSlowdown     += jobWeight * jobSlowdown;
        }
        
        this.maGate.getStorage().updateUsage(jobWeight);
        
        // update corresponding resource profile from the persist storage
        LinkedList<ResourceInfo> localResourceInfoList = this.maGate.getStorage().getResourceInfoList();
        
        for (ResourceInfo ri : localResourceInfoList){
        	
            if (gi.getJob().getResourceID() == ri.getResource().getResourceID()){
                // lower the load of resource, update info about overall resource tardiness and exit cycle
                ri.lowerResInExec(gi);
                ri.setTotalTardinessOfFinishedJobs(ri.getTotalTardinessOfFinishedJobs() + jobTardiness);
                
                if(jobTardiness <= 0.0){
                    ri.setNumOfPreviousFinishedNondelayedJobs(ri.getNumOfPreviousFinishedNondelayedJobs() + 1); 
                    totalNumOfNondelayedLocalJobs++;
                    
                }else{
                    totalNumOfDelayedLocalJobs++;
                }
                break;
            }
        }
    	
        // some resource is probably available - try send next job according to schedule
        if(numOfExistingSchedules == 0){
           this.callSchedule();
        }
    }
    
    /**
     * Community Job executed
     * @param jobInfo
     */
    public void communityJobFinishedConfirmation(JobInfo gi, double queuedTime) {
    	
    	if(gi.getJobStatus() == MaGateMessage.JOB_SUCCESS) {
			this.numOfSuccessProcessedCommunityJob += 1;
		} else {
			this.numOfFailedProcessedCommunityJob += 1;
		}
		
		// single job tardiness 
        double jobTardiness = gi.getTardiness();
        
        // job response time
        // NOTICE: job is released at "gi.getJobStartTime()", doesn't mean the execution will start, it could be delayed
        double jobResponse = gi.getJob().getFinishTime() - gi.getArrivalTime() + queuedTime;
        
        // calculate & update total weighted and normal slow down
        // NOTICE: job getAcutalCPUTime reflect how much time used by a job (each required PE runs the same time)
        double jobWeight = gi.getNumPE() * gi.getJob().getActualCPUTime();
        double jobSlowdown = jobResponse / gi.getJob().getActualCPUTime();
        
        this.totalJobWeight 		   += jobWeight;
        
        this.totalJobResponseTime 	   += jobResponse;
        this.totalJobWaitingTime += jobResponse - gi.getJob().getActualCPUTime();
        
        this.totalWeightedResponseTime += jobWeight * jobResponse;
        this.totalJobCPUTime           += gi.getJob().getActualCPUTime();
        this.totalWeightedJobCPUTTime  += jobWeight * gi.getJob().getActualCPUTime();
        
        if(Double.isInfinite(jobSlowdown)) {
        	// to handling unexpected errors, e.g., no job actualCPUTime available, replace it by the mean (averaged) of job slowdown
        	// for example, after 500 job executed, if current total slowdown is 1000, 
        	// then the mean job slowdown is 2, thus the totalslowdown is add-up by 2 (another mean slowdown)
        	this.totalSlowdown 			   += this.totalSlowdown / (this.numOfSuccessProcessedLocalJob + this.numOfSuccessProcessedCommunityJob);
        	this.totalWeightedSlowdown     += this.totalWeightedSlowdown / (this.numOfSuccessProcessedLocalJob + this.numOfSuccessProcessedCommunityJob);
        } else {
        	this.totalSlowdown 			   += jobSlowdown;
        	this.totalWeightedSlowdown     += jobWeight * jobSlowdown;
        }
        
        this.maGate.getStorage().updateUsage(jobWeight);
        
        // update corresponding resource profile from the persist storage
        LinkedList<ResourceInfo> localResourceInfoList = this.maGate.getStorage().getResourceInfoList();
        
        for (ResourceInfo ri : localResourceInfoList){
        	
            if (gi.getJob().getResourceID() == ri.getResource().getResourceID()){
                // lower the load of resource, update info about overall resource tardiness and exit cycle
                ri.lowerResInExec(gi);
                ri.setTotalTardinessOfFinishedJobs(ri.getTotalTardinessOfFinishedJobs() + jobTardiness);
                
                if(jobTardiness <= 0.0){
                    ri.setNumOfPreviousFinishedNondelayedJobs(ri.getNumOfPreviousFinishedNondelayedJobs() + 1); 
                    totalNumOfNondelayedCommunityJobs++;
                    
                }else{
                    totalNumOfDelayedCommunityJobs++;
                }
                break;
            }
        }
        
        // some resource is probably available - try send next job according to schedule
        if(numOfExistingSchedules == 0){
        	this.callSchedule();
        }
    }
	
    /**
     * When new response made, job owner (ModuleController) will be notified
     */
    private void sendResponseToJobOwner(JobInfo item) {
    	
    	this.maGate.getModuleController().updateFromMatchMaker(item);
    }
    
    
    /**
     * Starts scheduling according to prepared schedule/queue
     */
    private boolean scheduleJobs(){
        
        // pick up the corresponding local scheduling policy
        if(matchMakerPolicy.equals(MaGateMessage.PolicyExistingSchedule)){
//            this.numOfExistingSchedules = useSchedule();
            return true;
            
        } else if(matchMakerPolicy.equals(MaGateMessage.PolicyFCFS)){ 
        	this.numOfExistingSchedules = useFCFS();
        	return true;
            
        } else if(matchMakerPolicy.equals(MaGateMessage.PolicySJF)){
        	this.numOfExistingSchedules = useSJF();
            return true;
            
        } else if(matchMakerPolicy.equals(MaGateMessage.PolicyEasyBF)){
            numOfExistingSchedules = useEASY();
            return true;
            
        } else {
        	this.numOfExistingSchedules = useFCFS();
        	return true;
        }
        
    }
    
    
    /**
     * Call the existing schedule policy and record the used time
     */
    private void callSchedule() {
    	
		Date d = new Date();
        clockBeforeMakingSchedule = d.getTime();
        
        // ye: make next round schedules
        scheduleJobs();
        
        Date d2 = new Date();
        clockAfterMakingSchedule = d2.getTime();
        
        totalSchedulingTime += clockAfterMakingSchedule - clockBeforeMakingSchedule;
	}
    
    
    /************************
     * Scheduling Policies
     ************************/
    
    /**
     * FCFS algorithm managing incoming job queue
     */
    private int useFCFS(){
    	
        int successSched = 0;
        ResourceInfo selectedResourceInfo = null;
        
        while(!localJobQueue.isEmpty()){
            
            LinkedList<ResourceInfo> localResourceInfoList = this.maGate.getStorage().getResourceInfoList();
            
            // Refresh (update to latest numOfFreePE) the numOfFreVirtualPE for anticipating scheduling process
            for (ResourceInfo ri : localResourceInfoList){
            	ri.setNumOfVirtualFreePE(new AtomicInteger(ri.getNumOfFreePE()));
            }
            
            // Retrieve the job from jobQueue
            JobInfo jobInfo = JobCenterManager.getJobInfobyJobId(this.selectJobFromQueue());
        	
            // If job requirement exceeds resource capability, this job cannot be scheduled this time it is set to status FAILED directly
            if((jobInfo == null) || (!this.check_jobMatchResource(jobInfo))) {	
            	
            	
            	jobInfo.setTargetResourceID(-1);
            	removeJobFromLocalQueue(jobInfo.getGlobalJobID());
            	this.sendResponseToJobOwner(jobInfo);
                
                jobInfo = null;
                continue;
            }
            
            // ye: IMPORTANT: selected resourceInfo MUST be reset for each to-process jobInfo
            selectedResourceInfo = null;
            
            // ye: CASE 2: FCFS: select a resource (the first candidate), which match job's PE requirement and has the best MIPS
            for (ResourceInfo ri : localResourceInfoList){
            	
                if((ri.getNumOfVirtualFreePE().get() >= jobInfo.getNumPE()) && 
                		(ri.getNumOfTotalPE() >= jobInfo.getNumPE())) {
                	
                	selectedResourceInfo = ri;
                	ri.setNumOfVirtualFreePE(new AtomicInteger(ri.getNumOfVirtualFreePE().get() - jobInfo.getNumPE()));
                	
                	// resource "First Fit" selection
                	break;
                } 
            }
            
            if(selectedResourceInfo != null){
    			
            	// Current job marked to be sent to selected resource
                jobInfo.setTargetResourceID(selectedResourceInfo.getResource().getResourceID());
                
            	// Current job removed from queue
                this.removeJobFromLocalQueue(jobInfo.getGlobalJobID());
                
                // Important: resource profile notified with new job
                selectedResourceInfo.addJobInfoInExec(jobInfo);
                
                // ModulerController notified with new MatchMaker decision
                this.sendResponseToJobOwner(jobInfo);
                successSched += 1;

            } else {
            	
            	// Herewith the first element of the queue could be executed successfully in local resource
            	// but the corresponding resource is not ready yet
            	// therefore, the jobQueue checking will be blocked here, no matter whether another job inside the queue 
            	// could be executed now, it won't invoked in FCFS
            	break;
            	
            }
            
            jobInfo = null;
            
        } // exit loop jobQueue
        
        return successSched;
    }
    
    /**
     * SJF algorithm managing incoming job queue
     */
    private int useSJF(){
    	
    	// Difference between SJF and FCFS is in method selectJobFromQueue()
    	return this.useFCFS();
    }
    
    
    /**
     * EasyBackfilling algorithm managing incoming job queue
     */
    private int useEASY(){
    	
    	int successSched = 0;
        ResourceInfo selectedResourceInfo = null;
        boolean backfillingNeeded = false;
        
        while(!localJobQueue.isEmpty()){
            
            LinkedList<ResourceInfo> localResourceInfoList = this.maGate.getStorage().getResourceInfoList();
            
            // Refresh (update to latest numOfFreePE) the numOfFreVirtualPE for anticipating scheduling process
            for (ResourceInfo ri : localResourceInfoList){
            	ri.setNumOfVirtualFreePE(new AtomicInteger(ri.getNumOfFreePE()));
            }
            
            // Retrieve the job from jobQueue
            JobInfo jobInfo = JobCenterManager.getJobInfobyJobId(this.selectJobFromQueue());
        	
            // If job requirement exceeds resource capability, this job cannot be scheduled this time it is set to status FAILED directly
            if((jobInfo == null) || (!this.check_jobMatchResource(jobInfo))) {	
            	
            	jobInfo.setTargetResourceID(-1);
            	
            	this.removeJobFromLocalQueue(jobInfo.getGlobalJobID());
            	
                this.sendResponseToJobOwner(jobInfo);
                
                jobInfo = null;
                continue;
            }
            
            // ye: IMPORTANT: selected resourceInfo MUST be reset for each to-process jobInfo
            selectedResourceInfo = null;
            
            // ye: CASE 2: FCFS: select a resource (the first candidate), which match job's PE requirement and has the best MIPS
            for (ResourceInfo ri : localResourceInfoList){
            	
                if((ri.getNumOfVirtualFreePE().get() >= jobInfo.getNumPE()) && 
                		(ri.getNumOfTotalPE() >= jobInfo.getNumPE())) {
                	
                	selectedResourceInfo = ri;
                	ri.setNumOfVirtualFreePE(new AtomicInteger(ri.getNumOfVirtualFreePE().get() - jobInfo.getNumPE()));
                	
                	// resource "First Fit" selection
                	break;
                } 
            }
            
            if(selectedResourceInfo != null){
            	
            	// Current job marked to be sent to selected resource
                jobInfo.setTargetResourceID(selectedResourceInfo.getResource().getResourceID());
                
            	// Current job removed from queue
                this.removeJobFromLocalQueue(jobInfo.getGlobalJobID());
                
                // Important: resource profile notified with new job
                selectedResourceInfo.addJobInfoInExec(jobInfo);
                
                // ModulerController notified with new MatchMaker decision
                this.sendResponseToJobOwner(jobInfo);
                successSched += 1;

            } else {
            	
            	// Herewith the first element of the queue could be executed successfully in local resource
            	// but the corresponding resource is not ready yet
            	// therefore, the jobQueue checking will be blocked here, no matter whether another job inside the queue 
            	// could be executed now, it won't invoked in FCFS
            	backfillingNeeded = true;
            	break;
            	
            }
            
            jobInfo = null;
            
        } // exit loop jobQueue
        
        // starting backfilling phase
        if(backfillingNeeded && this.localJobQueue.size() > 1) {
        	
//        	String headJobId    = this.localJobQueue.firstElement();
        	String headJobId    = this.localJobQueue.get(0);
        	JobInfo headJobInfo = JobCenterManager.getJobInfobyJobId(headJobId);
        	ResourceInfo reservedResourceInfo = this.findReservedResource(headJobInfo);
        	
        	// looping all other jobs (except the first one) of MatchMaker's jobQueue
        	for(int j = 1; j < this.localJobQueue.size(); j++) {
        		String currentJobId    = this.localJobQueue.get(j);
        		JobInfo currentJobInfo = JobCenterManager.getJobInfobyJobId(currentJobId);
        		
        		// jump over jobs which will never success because of asking more PEs than resource's capability
        		if(currentJobInfo.getNumPE() >= reservedResourceInfo.getNumOfTotalPE()) {
        			continue;
        		}
        		
        		ResourceInfo resInfo = this.findResourceBF(currentJobInfo, headJobInfo, reservedResourceInfo);
        		
        		if(resInfo != null){
        			
        			// Current job marked to be sent to selected resource
                    currentJobInfo.setTargetResourceID(resInfo.getResource().getResourceID());
                    
                	// Current job removed from queue
                    this.removeJobFromLocalQueue(currentJobInfo.getGlobalJobID());
                    
                    // Important: resource profile notified with new job
                    resInfo.addJobInfoInExec(currentJobInfo);
                    
                    // ModulerController notified with new MatchMaker decision
                    this.sendResponseToJobOwner(currentJobInfo);
                    
                    backfillingNeeded = false;
                    successSched += 1;
                    // Important: one job has been backfilled, therefore MatchMaker's jobQueue size is decreased
                    j--;

                } 
        	}
        }
        
        return successSched;
    }
    
    /***********************************
     * Scheduling Auxiliary methods
     ***********************************/
    
    /**
     * Auxiliary method for EASY Backfilling
     */
    @SuppressWarnings("unchecked")
	private ResourceInfo findResourceBF(JobInfo newJob, JobInfo blockedFirstJob, ResourceInfo reservedResForBlockedFirstJob){
    	
        ResourceInfo r_cand = null;
        int r_cand_speed = 0;
        
        LinkedList localResourceInfoList = this.maGate.getStorage().getResourceInfoList();
        
        for (int j=0; j < localResourceInfoList.size(); j++) {
        	
            ResourceInfo ri = (ResourceInfo) localResourceInfoList.get(j);
            if(ri.getNumOfFreePE() < 1) {
            	continue;
            }
            
//            log.info("################# resource.Total.PE: " + ri.getNumOfTotalPE() +
//            		"; resource.Free.PE: " + ri.getNumOfFreePE() + 
//            		"; first.job.PE: " + blockedFirstJob.getNumPE() + 
//            		"; job.Requested.PE: " + newJob.getNumPE() + 
//            		"; jobId: " + newJob.getGlobalJobID());
            
            if(ri.getNumOfFreePE() >= newJob.getNumPE() && ri.getResource().getResourceID() != reservedResForBlockedFirstJob.getResource().getResourceID()){
            	
//            	log.info("*******************\n*******************");
            	
            	int speed = ri.getResource().getMIPSRatingOfOnePE();
                if(speed >= r_cand_speed){
                    r_cand = ri;
                    r_cand_speed = speed;
                }
                
            } else if (ri.getNumOfFreePE() >= newJob.getNumPE() && ri.getResource().getResourceID() == reservedResForBlockedFirstJob.getResource().getResourceID()){
                
            	// precondition:
            	// shadow time: when enough nodes will be available for the first queued(currently blocked) job
            	// extra PE: if the first job does not need all available PEs, the ones left over are the extra PEs
            	double newJobEstimatedFinishTime = MaGateMediator.getSystemTime() + (newJob.getComputationalLength()/ri.getResource().getMIPSRatingOfOnePE());
            	double shadowTime = ri.getEarliestStartTime();
            	int extraPE = ri.getNumOfTotalPE() - blockedFirstJob.getNumPE();
            	double minPE = Math.min(ri.getNumOfFreePE(), extraPE);
            	
//            	log.info("****************** shadowTime: " + shadowTime + 
//            			"; newJobEstimatedFinishTime: " + newJobEstimatedFinishTime + 
//            			"; extraPE: " + extraPE + "; minPE: " + minPE);
            	
            	// to determine whether a being checked job can be fit backfilling, need to check as follows:
            	// Either, it requires no more than currently free PEs on this resource, and will terminate by the shadow time
            	// Or, it requires no more than minimum of currently free PEs and extra PEs, namely it requires no more than min(freePEs_onResource, extra_PE) 

            	if(newJobEstimatedFinishTime <= shadowTime){
//            		log.info("*******************\n*******************");
                    int speed = ri.getResource().getMIPSRatingOfOnePE();
                    if(speed > r_cand_speed){
                        r_cand = ri;
                        r_cand_speed = speed;
                    }
                    
                } else if(newJob.getNumPE() <= minPE) {
//                	log.info("*******************\n*******************");
                	int speed = ri.getResource().getMIPSRatingOfOnePE();
                    if(speed > r_cand_speed){
                        r_cand = ri;
                        r_cand_speed = speed;
                    }
                }
            }
        }
        
        // save the ResourceInfo List information back
        this.maGate.getStorage().setResourceInfoList(localResourceInfoList);
        
        return r_cand;
    }
    
    
    /**
     * Find the reserved resource for the first job of the queue, which is blocked therefore need backfilling (if multi-resources on one node)
     * 
     * Auxiliary method for EASY Backfilling
     */
    @SuppressWarnings("unchecked")
    private ResourceInfo findReservedResource(JobInfo grsv){
    	
        double est = Double.MAX_VALUE;
        ResourceInfo found = null;
        
        LinkedList localResourceInfoList = this.maGate.getStorage().getResourceInfoList();
        
        for (int j=0; j < localResourceInfoList.size(); j++){
        	
            ResourceInfo ri = (ResourceInfo) localResourceInfoList.get(j);
            
            if(ri.getNumOfTotalPE() >= grsv.getNumPE()){
            	
            	// find the resource with earliest start time
                double ri_est = ri.getEarliestStartTimeForJobInfo(grsv, MaGateMediator.getSystemTime());
                // select minimal EST
                if(ri_est <= est){
                    est = ri_est;
                    found = ri;
                }
                
            } else {
            	continue; // this is not suitable machine
            }
        }
        
        // save the ResourceInfo List information back
        this.maGate.getStorage().setResourceInfoList(localResourceInfoList);
        
        return found;
    }
    
    /**
     * Select job from the jobQueue of MatchMaker
     * @return
     */
    private String selectJobFromQueue() {
    	
//    	if(this.matchMakerPolicy.equals(MaGateMessage.PolicyFCFS)) {
//			// FCFS
//			return this.localJobQueue.firstElement();
//			
//		} else if(this.matchMakerPolicy.equals(MaGateMessage.PolicySJF)) {
//			// SJF, jobQueue already sorted 
//			return this.localJobQueue.firstElement();
//			
//		} else if(this.matchMakerPolicy.equals(MaGateMessage.PolicyEasyBF)) {
//			// FCFS-like queue for EASY Backfilling
//			return this.localJobQueue.firstElement();
//			
//		} else {
//			// FCFS queue is the default policy
//			return this.localJobQueue.firstElement();
//			
//		}
    	
    	if(this.localJobQueue.size() > 0) {
    		return this.localJobQueue.get(0);
    	} else {
    		return null;
    	}
    	
    }
    
    /**
     * Check whether JobInfo can be satisfied by local resources (JobInfo) 
     * by receiving queries from MatchMaker itself
     * 
     * @param jobInfo
     * @return true IF matches!
     */
    private boolean check_jobMatchResource(JobInfo jobInfo) {
    	
    	if((!this.maGate.getMaGateOS().equals(jobInfo.getJob().getOSRequired())) || 
    			(jobInfo.getNumPE() > this.maGate.getStorage().getTotalNumOfPEs().get())) {
    		return false;
    		
    	} else {
    		return true;
    	}
    	
    }
    
    /**
     * Check whether JobInfo can be satisfied by local resources (OS_Type, NumOfPE) 
     * by receiving queries from external scheduling components, such as CASP Controller
     * 
     * @param jobOS
     * @param jobNumPE
     * @return
     */
    public boolean check_jobMatchResource(String jobOS, int jobNumPE) {
    	
    	if((!this.maGate.getMaGateOS().equals(jobOS)) || 
    			(jobNumPE > this.maGate.getStorage().getTotalNumOfPEs().get())) {
    		return false;
    		
    	} else {
    		return true;
    	}
    	
    }
    
    public boolean check_jobInstantMatchResource(String jobOS, int jobNumPE) {
    	
    	int numFreePE = this.maGate.getStorage().getTotalVirtualFreePEs(); 
    	
    	if((!this.maGate.getMaGateOS().equals(jobOS)) || (jobNumPE > numFreePE)) {
    		return false;
    		
    	} else {
    		return true;
    	}
    	
    }
    
    /**
     * Check whether still available resources(PEs) for incoming job request
     * 
     * @param jobInfo
     * @return true IF matches!
     */
    private boolean check_resourceAvailable(JobInfo jobInfo) {
    	
    	int freePE = this.maGate.getStorage().getTotalNumOfPEs().get() - this.maGate.getStorage().getTotalActivePEs().get();
    	
    	if(freePE > jobInfo.getNumPE()) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    
    /**
     * Check whether still available resources(PEs) for incoming job request 
     * by receiving queries from external components, such as CASP Controller
     * 
     * @param jobNumPE
     * @return
     */
    public boolean check_resourceAvailable(int jobNumPE) {
    	
    	int freePE = this.maGate.getStorage().getTotalNumOfPEs().get() - this.maGate.getStorage().getTotalActivePEs().get();
    	
    	if(freePE > jobNumPE) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    
//    /**
//     * Easy Backfilling algorithm managing incoming job queue
//     */
//    private int useEasyBF(){
//        int scheduled = 0;
//        boolean succ = false;
//        double est = 0.0;
//        ResourceInfo r_cand = null;
//        int r_cand_speed = 0;
//        
//        if(jobQueue.size() > 0){
//            JobInfo gi = (JobInfo) jobQueue.getFirst();
//            
//            LinkedList localResourceInfoList = this.maGate.getStorage().getResourceInfoList();
//            for (int j=0; j < localResourceInfoList.size(); j++){
//                ResourceInfo ri = (ResourceInfo) localResourceInfoList.get(j);
//                if(ri.getNumOfFreePE() >= gi.getNumPE() && ri.isPrevSelectedJobSentSuccessful()){
//                    int speed = ri.getResource().getMIPSRatingOfOnePE();
//                    if(speed > r_cand_speed){
//                        r_cand = ri;
//                        r_cand_speed = speed;
//                    }
//                }
//            }
//            // ye: save the ResourceInfo List information back
//            this.maGate.getStorage().setResourceInfoList(localResourceInfoList);
//            
//            if(r_cand != null){
//                gi = (JobInfo) jobQueue.removeFirst();
//                r_cand.addJobInfoInExec(gi);
//                // set the resource ID for this JobInfo (this is the final scheduling decision)
//                gi.setResourceID(r_cand.getResource().getResourceID());
//                
//                this.sendResponseToJobOwner(gi);
//                
//                succ = true;
//                r_cand.setPrevSelectedJobSentSuccessful(true);
//                return 1;
//            } 
//        } else {
//            return 0;
//        }
//        
//        // try backfilling procedure
//        if(!succ && jobQueue.size() > 1){
//            // head of queue - gridlet with reservation
//            JobInfo grsv = (JobInfo) jobQueue.get(0);
//            // reserved machine (i.e. Earliest Available)
//            ResourceInfo rsv_res = findReservedResource(grsv);
//            
//            // try backfilling on all jobs in queue except for head (grsv)
//            for(int j=1; j < jobQueue.size(); j++){
//                JobInfo gi = (JobInfo) jobQueue.get(j);
//                if(gi.getNumPE() >= grsv.getNumPE()) continue; // such job will never succeed
//                ResourceInfo ri = findResourceBF(gi, grsv, rsv_res);
//                if(ri != null){
//                    jobQueue.remove(j);
//                    ri.addJobInfoInExec(gi);
//                    // set the resource ID for this JobInfo (this is the final scheduling decision)
//                    gi.setResourceID(ri.getResource().getResourceID());
//                    
//                    this.sendResponseToJobOwner(gi);
//                    
//                    ri.setPrevSelectedJobSentSuccessful(true);
//                    scheduled++;
//                    succ = true;
//                    j--; //to get correct job from queue in next round. The queue was shortened...
//                    
//                } 
//            }
//        }
//        return scheduled;
//    }
//    
//    
    
    /**********************************************
	 * Obsolete MatchMaker scheduling policies
	 **********************************************/
    
//  /**
//  * Schedule job from prepared schedule if some machine is available
//  * Useful for policies such as EDF, EG-EDF, etc.
//  */
// private int useSchedule(){
// 	
//     int scheduled = 0;
//     LinkedList localResourceInfoList = this.maGate.getStorage().getResourceInfoList();
//     for (int j=0; j < localResourceInfoList.size(); j++){
//         ResourceInfo ri = (ResourceInfo) localResourceInfoList.get(j);
//         
//         if (ri.getJobListInSchedule().size() > 0) {
//             JobInfo gi = (JobInfo) ri.getJobListInSchedule().getFirst();
//             
//             // sends job to Resource only if there is any job in the schedule and the Resource is about to be free
//             if (ri.getNumOfFreePE() >= gi.getNumPE() && ri.isPrevSelectedJobSentSuccessful().get()) {
//                 ri.removeFirstJobInfo();
//                 ri.addJobInfoInExec(gi);
//                 // set the resource ID for this jobInfo (this is the final scheduling decision)
//                 gi.setTargetResourceID(ri.getResource().getResourceID());
//                 
//                 // tell the user where to send which gridlet
//                 this.sendResponseToJobOwner(gi);
//                 
//                 // sent JobInfo is no more in Tabu list
//                 // TO BE FIXED: the value of ri.setPrevSelectedJobSentSuccessful
//                 tabuGridlets.remove(gi);
//                 ri.setPrevSelectedJobSentSuccessful(new AtomicBoolean(true));
////                 ri.setPrevSelectedJobSentSuccessful(new AtomicBoolean(false));
//                 
//                 scheduled++;
//             }
//         }
//     }
//     // ye: save the ResourceInfo List information back
//     this.maGate.getStorage().setResourceInfoList(localResourceInfoList);
//     
//     return scheduled;
// }
    
//    /**
//     * Flexible Backfilling algorithm managing incoming job queue
//     */
//    private int useFlexBF(){
//        int scheduled = 0;
//        boolean succ = false;
//        double est = 0.0;
//        ResourceInfo r_cand = null;
//        int r_cand_speed = 0;
//        
//        if(jobQueue.size() > 0){
//            updateJobPriority(jobQueue, MaGateAdaptor.getSystemTime());
//            // sort the queue according to the job - priority
//            Collections.sort(jobQueue, new JobComparator());
//            
//            JobInfo gi = (JobInfo) jobQueue.getFirst();
//            
//            LinkedList localResourceInfoList = this.maGate.getStorage().getResourceInfoList();
//            for (int j=0; j < localResourceInfoList.size(); j++){
//                ResourceInfo ri = (ResourceInfo) localResourceInfoList.get(j);
//                if(ri.getNumOfFreePE() >= gi.getNumPE() && ri.isPrevSelectedJobSentSuccessful()){
//                    int speed = ri.getResource().getMIPSRatingOfOnePE();
//                    if(speed > r_cand_speed){
//                        r_cand = ri;
//                        r_cand_speed = speed;
//                    }
//                }
//            }
//            
//            // ye: save the ResourceInfo List information back
//            this.maGate.getStorage().setResourceInfoList(localResourceInfoList);
//            
//            if(r_cand != null){
//                gi = (JobInfo) jobQueue.removeFirst();
//                r_cand.addJobInfoInExec(gi);
//                // set the resource ID for this JobInfo (this is the final scheduling decision)
//                gi.setResourceID(r_cand.getResource().getResourceID());
//                
//                this.sendResponseToJobOwner(gi);
//                
//                succ = true;
//                r_cand.setPrevSelectedJobSentSuccessful(true);
//                return 1;
//                
//            } 
//            
//        } else {
//            return 0;
//            
//        }
//        // try backfilling procedure
//        if(!succ && jobQueue.size() > 1){
//            // head of queue - gridlet with reservation
//            JobInfo grsv = (JobInfo) jobQueue.get(0);
//            // reserved machine (i.e. Earliest Available)
//            ResourceInfo rsv_res = findReservedResource(grsv);
//            
//            // try backfilling on all jobs in queue except for head (grsv)
//            for(int j=1; j < jobQueue.size(); j++){
//                JobInfo gi = (JobInfo) jobQueue.get(j);
//                if(gi.getNumPE() >= grsv.getNumPE()) continue; // such jobs will never succeed
//                ResourceInfo ri = findResourceBF(gi, grsv, rsv_res);
//                if(ri != null){
//                    jobQueue.remove(j);
//                    ri.addJobInfoInExec(gi);
//                    // set the resource ID for this JobInfo (this is the final scheduling decision)
//                    gi.setResourceID(ri.getResource().getResourceID());
//                    
//                    this.sendResponseToJobOwner(gi);
//                    
//                    ri.setPrevSelectedJobSentSuccessful(true);
//                    scheduled++;
//                    succ = true;
//                    j--; //to get correct job from queue in next round. The queue was shortened...
//                }
//            }
//        }
//        return scheduled;
//    }
    
    
    /**
     * This method updates job priority P_j according to Flexible Backfilling strategy.
     * @param queue Incoming queue of jobs
     * @param time Current simulation time
     * @deprecated
     */
    private void updateJobPriority(LinkedList queue, double time){
    	
        int bm = this.maGate.getStorage().getBestMachineMIPS().get();
        
        // sort the queue according to estimated exec. time
        Collections.sort(queue, new LengthComparator());
        
        // compute new priorities
        for(int i = 0; i < queue.size(); i++){
        	JobInfo gi = (JobInfo) queue.get(i);
            // Aging
            double age_factor = 0.01;
            double p = 0.0;
            double age = time - gi.getArrivalTime();
            p += age_factor * age;
            
            // Deadline
            double deadline = gi.getDeadline();
            double estimated = gi.getEstimatedComputationTime();
            double nxtime = 0.0;
            double extime = 0.0;
            double k = 2.0; // reset
            double bme = gi.getEstimatedComputationalMIPS();
            double t = 0.0;
            nxtime = estimated * (bme/bm);
            extime = time + nxtime;
            t = deadline - k*nxtime;
            double max = 20.0;
            double min = 0.1;
            double a = (max - min)/(deadline - t);
            //double a = 1.0; // reset
            if(extime <= t) p+= min;
            if(t < extime && extime <= deadline) p += a * (extime - t) + min;
            if(extime > deadline) p += min;
            
            // Wait Minimization
            double boostvalue = 2.0; // reset
            // get the shortest gridlet according to "estimated" parameter
            JobInfo shortest = (JobInfo) queue.getLast();
            double minext = shortest.getEstimatedComputationTime();
            p += (boostvalue * minext)/estimated;
            gi.setJobPriority(p);
        }
        
    }
    
    
//  /**
//  * Add job into queue - EDF (Earliest Deadline First) technique
//  */
// private void addToQueueEDF(SimJobInfo gi){
//     global_policy = 2;
//     boolean succ = false;
//     for (int j=0; j < jobQueue.size(); j++){
//         SimJobInfo gj = (SimJobInfo) jobQueue.get(j);
//         if(gi.getDeadline() < gj.getDeadline()){
//             jobQueue.add(j, gi);
//             succ = true;
//             break;
//         }
//     }
//     if(jobQueue.size() == 0 || succ == false){
//         jobQueue.addLast(gi);
//     }
// }
    
	/************************
	 * Get / Set methods
	 ************************/
    
	/** Get number of already made schedulers by this MatchMaker */
	public int getNumOfExistingSchedules() {
		return numOfExistingSchedules;
	}

	/**
	 * Get total time used for making schedule generation, i.e. time =
	 * Sum(clockAfterMakingSchedule - clockBeforeMakingSchedule)
	 */
	public double getTotalSchedulingTime() {
		return totalSchedulingTime;
	}

	/** Get total number of nondelayed jobs processed by this matchmaker */
	public int getTotalNumOfNondelayedLocalJobs() {
		return totalNumOfNondelayedLocalJobs;
	}

	/** Get total number of delayed jobs processed by this matchmaker */
	public int getTotalNumOfDelayedLocalJobs() {
		return totalNumOfDelayedLocalJobs;
	}

	/**
	 * Get number of jobs waiting for scheduling decision It will be decrease
	 * only if JobSubmiiter inform MatchMaker that the job is already sent to
	 * resource
	 */
	public int getNumOfJobWaitingForSchedule() {
		return numOfJobWaitingForSchedule;
	}

	/** Get total time used to execute all the jobs */
	public double getTotalJobExecutionTime() {
		return totalJobResponseTime;
	}
	
	public double getTotalJobWaitingTime() {
		return this.totalJobWaitingTime;
	}
	
	/** Get start time of the simulation */
	public double getSimulationStartTime() {
		return simulationStartTime;
	}

	/**
	 * Get total job weight: LOOP all job (numberOfCPU for execution * job
	 * actual CPU time)
	 */
	public double getTotalJobWeight() {
		return totalJobWeight;
	}

	/**
	 * Get total slowdown of jobs = job response time / job actual execution
	 * time
	 */
	public double getTotalSlowdown() {
		return totalSlowdown;
	}

	/**
	 * Get total weighted response time = (job weight * job response time) =
	 * (job used cpu number * job actual cpu time * job response time)
	 */
	public double getTotalWeightedResponseTime() {
		return totalWeightedResponseTime;
	}

	/**
	 * Get total weighted slowdown = (job weight * job slowdown) = (job used cpu
	 * number * job actual cpu time * job slowdown)
	 */
	public double getTotalWeightedSlowdown() {
		return totalWeightedSlowdown;
	}

	/** Get number of jobs submitted through submitter */
	public int getNumOfReceivedLocalJobs() {
		return numOfReceivedLocalJobs;
	}
	
	public double getAvgQueuingTime() {
		return avgQueuingTime;
	}
	
	public int sizeOfLocalJobQueue() {
		return this.localJobQueue.size();
	}
	
}

