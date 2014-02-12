package ch.hefr.gridgroup.magate.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.jfree.util.Log;

import ch.hefr.gridgroup.magate.MaGateEntity;
import ch.hefr.gridgroup.magate.env.MaGateMediator;
import ch.hefr.gridgroup.magate.env.MaGateMessage;


/**
 * Class SimJobInfo<p>
 * This class behaves like "Simulated job information exchange format" between 
 * Job Owner(submitter) and MatchMaker. 
 * It use set / get methods to set / get information about MaGateJob. 
 * It stores various information of real Job. 
 * 
 * @author Dalibor Klusacek (Alea simulator)
 * @author Ye HUANG (refacting)
 */
public class JobInfo implements Cloneable, Serializable {
	
    /** owner id */
    private int userID;
    
    /** job id */
    private int jobLocalID;
    
    /** jobInfo global id */
    private String globalJobID;

	/** link to original job */
    private Job job;
    
    /** selected (targetted) resource id */
    private int targetResourceID;
    
    /** computational length */
    private double computationalLength;
    
    /** not used */
    private double jobFinishedSoFar;
    
    /** not used */
    private double cost;
    
    /** not used */
    private double completitionFactor;
    
    /** architecture required by the job */
    private String archRequired;
    
    /** OS required by the job */
    private String osRequired;
    
    /** release date (start time) */
    private double jobStartTime;
    
    /** arrival time i.e. time of Job arrival in the system */
    private double arrivalTime;
    
    /** start time of job queuing upon a specific node */
    private double queuingStartTime;
    
    /** end time of job queuing upon a specific node
     * start time of job processing */
    private double queuingEndTime;
    
    private double queuingTime;
    
    /** due date (deadline) */
    private double deadline;
    
    /** Expected tardiness calculated by the scheduler */
    private double tardiness;
    
    /** It denotes this dynamicaly changing information: dynamicRealeaseTime = max(0.0, (arrivalTime + jobStartTime) - currentTime) */
    private double dynamicRealeaseTime;
    
    /** job priority */
    private double jobPriority;
    
    /** number of PEs to run this job */
    private int numPE;
    
    /** estimated execution finish time */
    private double expectedFinishTime;
    
    /** estimated computational length */
    private double estimatedComputationTime;
    
    /** MIPS rating of a machine used to compute estimated comp. length */
    private double estimatedComputationalMIPS;
    
    /** job status */
    private int jobStatus;

	/** id of original MaGate where the job is submitted */
    private String originalMaGateId = "";
    
    /** id of MaGate where the job is executed */
    private String executionMaGateId = "";
    
    /** job profile for community execution */
    private ConcurrentHashMap<String,Object> communityJobInfoProfile   = null;
    
    /** job negotiation counter */
    private AtomicInteger jobNegotiationCounter = new AtomicInteger(0);
    

	/** 
	 * Creates a new instance of JobInfo object based on the MaGateJob
     */
    public JobInfo(Job job) {
        
    	this.userID = job.getUserID();
        this.setJobLocalID(job.getGridletID());
        this.setJobStatus(job.getJobStatus());
        this.setComputationalLength(job.getJobLength());
        this.setJobFinishedSoFar(job.getJobFinishedSoFar());
        this.setCompletitionFactor(job.getJobFinishedSoFar() / job.getJobLength());
        this.setJob(job);
        this.setOsRequired(job.getOSRequired());
        this.setArchRequired(job.getArchRequired());
        this.setDeadline(job.getDeadline());
        this.setTardiness(0.0);
        this.setDynamicRealeaseTime(0.0);
        this.setJobPriority(job.getJobPriority());
        this.setNumPE(job.getNumPE());
        this.setExpectedFinishTime(0);
        this.setEstimatedComputationTime(job.getEstimatedComputationTime());
        this.setEstimatedComputationalMIPS(job.getEstimatedComputationalMIPS());
        this.setArrivalTime(job.getArrivalTime());
        this.setQueuingStartTime(job.getArrivalTime());
        this.setQueuingEndTime(-1);
        this.setQueuingTime(MaGateMediator.getSystemTime() - this.queuingStartTime);
        
        
        /**
         * The adopted Ant Interface only search: target.value >= current.value
         * Therefore:
         *   1) for numOfPE, if current value is 2, machines with 5, 6, ... will match the query
         *   2) for price, if current value is -2, machines require -0.5, -1 ... will match, but in reality, the price
             of job should be greater than machine's requirment, e.g. jobReward(2) > machineAsk(1) 
         */
        
        this.communityJobInfoProfile = new ConcurrentHashMap<String,Object>();
        this.communityJobInfoProfile.put(MaGateMessage.MatchProfile_OS, job.getOSRequired());
        
        this.communityJobInfoProfile.put(MaGateMessage.MatchProfile_CPUCount, new Integer(job.getNumPE()));
//        this.communityJobInfoProfile.put(MaGateMessage.MatchProfile_ExePrice, new Double(-2.0));
        
        
    }
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#getOwnerID()
	 */
    public int getUserID() {
        return userID;
    }
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#setOwnerID(int)
	 */
    public void setUserID(int userID) {
//    	MaGateJob job = this.getJob();
//    	job.setUserID(userID);
        this.userID = userID;
        this.getJob().setUserID(userID);
    }

    
    public String getOriginalMaGateId() {
		return originalMaGateId;
	}

	public void setOriginalMaGateId(String originalMaGateId) {
		this.getJob().setOriginalMaGateId(originalMaGateId);
		this.globalJobID = this.getJob().getGlobalJobID();
		this.originalMaGateId = originalMaGateId;
	}

	public String getExecutionMaGateId() {
		return executionMaGateId;
	}

	public void setExecutionMaGateId(String executionMaGateId) {
		this.executionMaGateId = executionMaGateId;
	}
	
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#getJobID()
	 */
    public int getJobLocalID() {
        return jobLocalID;
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#setJobID(int)
	 */
    public void setJobLocalID(int jobLocalID) {
        this.jobLocalID = jobLocalID;
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#getResourceID()
	 */
    public int getTargetResourceID() {
        return targetResourceID;
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#setResourceID(int)
	 */
    public void setTargetResourceID(int targetResourceID) {
        this.targetResourceID = targetResourceID;
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#getJobStatus()
	 */
    public int getJobStatus() {
    	this.jobStatus = getJob().getJobStatus(); // essential for fresh information
        return jobStatus;
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#setJobStatus(int)
	 */
    public void setJobStatus(int jobStatus) {
        this.jobStatus = jobStatus;
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#getComputationalLength()
	 */
    public double getComputationalLength() {
        return computationalLength;
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#setComputationalLength(double)
	 */
    public void setComputationalLength(double computationalLength) {
        this.computationalLength = computationalLength;
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#getJobFinishedSoFar()
	 */
    public double getJobFinishedSoFar() {
    	this.jobFinishedSoFar = getJob().getJobFinishedSoFar(); // essential for fresh information
        return jobFinishedSoFar; 
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#setJobFinishedSoFar(double)
	 */
    public void setJobFinishedSoFar(double jobFinishedSoFar) {
        this.jobFinishedSoFar = jobFinishedSoFar;

    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#getCompletitionFactor()
	 */
    public double getCompletitionFactor() {
        return completitionFactor;
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#setCompletitionFactor(double)
	 */
    public void setCompletitionFactor(double completitionFactor) {
        this.completitionFactor = completitionFactor;
    }
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#getArchRequired()
	 */
    public String getArchRequired() {
        return archRequired;
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#setArchRequired(java.lang.String)
	 */
    public void setArchRequired(String osRequired) {
        this.archRequired = osRequired;
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#getOsRequired()
	 */
    public String getOsRequired() {
        return osRequired;
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#setOsRequired(java.lang.String)
	 */
    public void setOsRequired(String osRequired) {
        this.osRequired = osRequired;
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#getJob()
	 */
    public Job getJob() {
        return job;
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#setJob(ch.hefr.gridgroup.magate.model.MaGateJob)
	 */
    public void setJob(Job job) {
        this.job = job;
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#getDeadline()
	 */
    public double getDeadline() {
        return deadline;
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#setDeadline(double)
	 */
    public void setDeadline(double deadline) {
        this.deadline = deadline;
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#getTardiness()
	 */
    public double getTardiness() {
        return tardiness;
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#setTardiness(double)
	 */
    public void setTardiness(double tardiness) {
        this.tardiness = tardiness;
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#getDynamicRealeaseTime()
	 */
    public double getDynamicRealeaseTime() {
        return dynamicRealeaseTime;
    }
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#setDynamicRealeaseTime(double)
	 */
    public void setDynamicRealeaseTime(double dynamicRealeaseTime) {
        this.dynamicRealeaseTime = dynamicRealeaseTime;
    }
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#getJobPriority()
	 */
    public double getJobPriority() {
        return jobPriority;
    }
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#setJobPriority(double)
	 */
    public void setJobPriority(double jobPriority) {
        this.jobPriority = jobPriority;
    }

    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#getNumPE()
	 */
    public int getNumPE() {
        return numPE;
    }

    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#setNumPE(int)
	 */
    public void setNumPE(int numPE) {
        this.numPE = numPE;
    }

    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#getExpectedFinishTime()
	 */
    public double getExpectedFinishTime() {
        return expectedFinishTime;
    }

    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#setExpectedFinishTime(double)
	 */
    public void setExpectedFinishTime(double expectedFinishTime) {
        this.expectedFinishTime = expectedFinishTime;
    }

    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#getEstimatedComputationTime()
	 */
    public double getEstimatedComputationTime() {
        return estimatedComputationTime;
    }

    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#setEstimatedComputationTime(double)
	 */
    public void setEstimatedComputationTime(double estimatedComputationTime) {
        this.estimatedComputationTime = estimatedComputationTime;
    }

    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#getEstimatedComputationalMIPS()
	 */
    public double getEstimatedComputationalMIPS() {
        return estimatedComputationalMIPS;
    }

    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.JobInfo#setEstimatedComputationalMIPS(double)
	 */
    public void setEstimatedComputationalMIPS(double estimatedComputationalMIPS) {
        this.estimatedComputationalMIPS = estimatedComputationalMIPS;
    }
	
    /** job negotiation counter */
	public AtomicInteger getJobNegotiationCounter() {
		return jobNegotiationCounter;
	}

	/** job negotiation counter */
	public void setJobNegotiationCounter(AtomicInteger jobNegotiationCounter) {
		this.jobNegotiationCounter = jobNegotiationCounter;
	}
	
    /** job profile for community execution */
    public ConcurrentHashMap<String, Object> getCommunityJobInfoProfile() {
		return communityJobInfoProfile;
	}

    /** job profile for community execution */
	public void setCommunityJobInfoProfile(
			ConcurrentHashMap<String, Object> extCommunityJobInfoProfile) {
		
		try {
//			this.communityJobInfoProfile = new ConcurrentHashMap<String,Object>();
	        this.communityJobInfoProfile.replace(MaGateMessage.MatchProfile_OS, extCommunityJobInfoProfile.get(MaGateMessage.MatchProfile_OS));
	        
	        this.communityJobInfoProfile.replace(MaGateMessage.MatchProfile_CPUCount, extCommunityJobInfoProfile.get(MaGateMessage.MatchProfile_CPUCount));
	        
		} catch(Exception e) {
			e.printStackTrace();
		}
		
//		this.communityJobInfoProfile = communityJobInfoProfile;
	}
	
    /** update job profile for community execution */
	public void updateCommunityJobInfoProfile(String matchProfile_OS, Integer matchProfile_CPUCount) {
		
		this.communityJobInfoProfile.replace(MaGateMessage.MatchProfile_OS, matchProfile_OS);
        
        this.communityJobInfoProfile.replace(MaGateMessage.MatchProfile_CPUCount, matchProfile_CPUCount);
        
	}
	
    public String getGlobalJobID() {
		return globalJobID;
	}

	public double getArrivalTime() {
		// TODO Auto-generated method stub
		return this.arrivalTime;
	}

	public void setArrivalTime(double startTime) {
		// TODO Auto-generated method stub
		this.arrivalTime = startTime;
		this.getJob().setArrivalTime(startTime);
	}
	
	public double getQueuingStartTime()  {
		return this.queuingStartTime;
	}
	
	public void setQueuingStartTime(double queuingStartTime) {
		this.queuingStartTime = queuingStartTime;
	}
	
	public double getQueuingEndTime()  {
		return this.queuingEndTime;
	}
	
	public void setQueuingEndTime(double queuingEndTime) {
		this.queuingEndTime = queuingEndTime;
	}
	
	
	public double getQueuingTime() {
		return this.queuingTime;
	}
	
	public void setQueuingTime(double queuingTime) {
		this.queuingTime = queuingTime;
	}
	
	public JobInfo clone() {
        
		JobInfo jobInfo = null;
        try {
        	jobInfo = (JobInfo) super.clone();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        return jobInfo;
    }
}


