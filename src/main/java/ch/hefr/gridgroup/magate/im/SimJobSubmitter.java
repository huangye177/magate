package ch.hefr.gridgroup.magate.im;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.MaGateEntity;
import ch.hefr.gridgroup.magate.env.JobCenterManager;
import ch.hefr.gridgroup.magate.env.MaGateParam;
import ch.hefr.gridgroup.magate.env.SimJobFactory;
import ch.hefr.gridgroup.magate.input.ExpNode;
import ch.hefr.gridgroup.magate.model.*;
import ch.hefr.gridgroup.magate.storage.GlobalStorage;

import gridsim.Gridlet;
import gridsim.GridletList;

/**
 * Class SimJobSubmitter behaves as a JobSubmitter for simulated jobs.
 * It interacts with grid resources and matchmaker.
 * @author Ye HUANG
 */
public class SimJobSubmitter implements IJobSubmitter {
	
	private static Log log = LogFactory.getLog(SimJobSubmitter.class);
	
    /** ProgramSubmitter id */
    private int programSubmitterId;
    
	/** Number of SimJobs in Job Submitter */
    private int numOfTotalJob = 0;
    
    /** Total tardiness for this Job Submitter */
    private double totalTardiness = 0.0;

    private MaGateEntity maGate;
    
    private String maGateIdentity;
    
	private int tempCount = 0;
    
    /**
     * Constructor - creates SimJobSubmitter to create programmed simulated jobs
     */
    public SimJobSubmitter(MaGateEntity maGate, ConcurrentLinkedQueue<Job> jobQueue) throws Exception {
//    public SimJobSubmitter(MaGateEntity maGate, int numOfJobs, int numOfPERequestedByBadJob, 
//    		String archType, String osType, double badRate) throws Exception {
    		
        this.maGate          = maGate;
        this.maGateIdentity  = maGate.getMaGateIdentity();
        
        // ye: generate a list of SimJobs for this Job Submitter, 
        // the owner of SimJobs is NOT specified here
        this.simJobGenerate(jobQueue);
    }
    
    
    /* (non-Javadoc)
     * TO FIX: job executed on each node should be: localExedJobs + inputExedJobs
	 * @see ch.hefr.gridgroup.magate.im.impl.JobSubmitter#sumTotalJobTardiness()
	 */
    public int sumTotalJobTardiness() {
    	
    	//  TO FIX: job executed on each node should be: localExedJobs + inputExedJobs
    	
    	Vector<JobInfo> receivedLocalJobList = JobCenterManager.getJob_processingNode(this.maGateIdentity, JobCenterManager.EXECUTED); 
//    	Collection<JobInfo> receivedLocalJobList = this.maGate.getStorage().find_localExedJob(); 
    	
        int succ = 0;
        double exec = 0.0;
        JobInfo jobInfo = null;
        Job job = null;
        
        Iterator<JobInfo> it = receivedLocalJobList.iterator();
        while(it.hasNext()) {
        	jobInfo = (JobInfo) it.next();
        	job = jobInfo.getJob();
        	
        	if(job.getJobStatus() == Gridlet.SUCCESS){
                totalTardiness += Math.max(0.0, job.getFinishTime() - job.getDeadline());
                succ++;
                exec += job.getActualCPUTime();
            }
        }
        return succ;
    }
    
    
    /**
     * This method creates a standard random SimJob list with pre-defined "bad rate"
     */
    private void simJobGenerate(ConcurrentLinkedQueue<Job> jobQueue) {
    	
    	this.numOfTotalJob = jobQueue.size();
        
        // update the expected number of local jobs
        int tmp = this.maGate.getStorage().getExpectedNumOfLocalJob().get();
        tmp    += this.numOfTotalJob;
        this.maGate.getStorage().setExpectedNumOfLocalJob(new AtomicInteger(tmp));
        
        Iterator<Job> it = jobQueue.iterator();
        while(it.hasNext()) {
        	
        	// Important: make sure the job generation is a clone(), instead of a reference copy
        	Job arrivedlJob = it.next();
        	
        	Job localJob = arrivedlJob.clone();
        	JobInfo jobInfo = new JobInfo(localJob);
        	
//        	MaGateJob localJob = it.next().clone();
//        	JobInfo jobInfo = new SimJobInfo(localJob).clone();
        	
        	// Notice! set both (1) JobInfo original MaGate Id 
        	// (2) SimJob's globalJobID = originalMaGateId + "_" + this.getJobLocalID();
        	jobInfo.setOriginalMaGateId(this.maGate.getMaGateIdentity());
        	
        	JobCenterManager.jobGenerated(jobInfo);
        }
        
        // ye: mark the SimJobs are generated to be submit/manage
        this.maGate.getStorage().setJobPrepared(new AtomicBoolean(true));
//        log.debug(this.numOfTotalJob + " jobs have been created!");
        
    }
    
//    /**
//     * This method creates a standard random SimJob list with workload trace file based parameters within one Job Submitter.
//     */
//    private void archivedSimJobGenerator(String fileName) {
//        
//    	log.debug(this.jobSubmitterName + "Starting to generate jobs from workload trace file...");
//    	
//    	ConcurrentLinkedQueue jobQueue = SimJobFactory.createWorkloadBasedSimJobListWithoutUser(fileName, MaGateParam.peMIPS, MaGateParam.osType, MaGateParam.archType);   
//        this.numOfTotalJob = jobQueue.size();
//        
//        Iterator<JobInfo> it = jobQueue.iterator();
//        while(it.hasNext()) {
//        	MaGateJob localJob = (MaGateJob) it.next();
//        	
//        	JobInfo jobInfo = new SimJobInfo(localJob);
//    	    jobInfo.setOriginalMaGateId(this.maGate.getMaGateIdentity());
//        	LocalJobStorage.join_localQueue(jobInfo);
////        	this.maGate.getStorage().getLocalJobQueue().offer(jobInfo);
//        }
//        
//        // ye: mark the SimJobs are generated to be submit/manage
//        this.maGate.getStorage().setJobPrepared(new AtomicBoolean(true));
//        log.debug(this.numOfTotalJob + " jobs from workload trace file have been generated!");
//        
//    }
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.im.impl.JobSubmitter#getNumOfTotalJob()
	 */
	public int getNumOfTotalJob() {
		return numOfTotalJob;
	}
    
	/* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.im.impl.JobSubmitter#getTotalTardiness()
	 */
	public double getTotalTardiness() {
		return totalTardiness;
	}
	
}


