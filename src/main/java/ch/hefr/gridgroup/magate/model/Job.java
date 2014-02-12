package ch.hefr.gridgroup.magate.model;

import java.io.Serializable;
//import ch.eif.gridgroup.magate.env.*;
import gridsim.*;

/**
 * Class SimJob<p>
 * This class represents one simulated job, i.e. one job and its parameters. By now,
 * job can may require 1 or more CPUs for its run.
 *
 * @author Dalibor Klusacek (Alea simulator)
 * @author Ye HUANG (refacting)
 */
public class Job extends Gridlet implements Serializable, Cloneable {
	
	/** id of original MaGate where the job is submitted */
    private String originalMaGateId;

	/** required architecture */
    private String archRequired;
    
    private String globalJobID;

	/** required OS  */
    private String osRequired;
    
    /** arrival time i.e. time of Job arrival in the system */
    private double arrivalTime;
    
    /** deadline of SimJob */
    private double deadline;
    
    /** SimJob priority */
    private int jobPriority;
    
    /** required number of CPU */
    private int numPE;    
    
    /** real computational length */
    private double realComputationalLength;
    
    /** estimated computational length */
    private double estimatedComputationTime;
    
    /** MIPS rating of a machine used to compute estimated comp. length */
    private double estimatedComputationalMIPS;
    
    
    /**
     * Creates a new instance of SimJob representing one Job
     * 
     * @param jobId
     * @param jobLength
     * @param jobRealLength
     * @param jobFileSize, size in Bytes
     * @param jobOutputSize, output size in Bytes
     * @param oSrequired, Operating System required to run this job
     * @param archRequired, required architecture
     * @param jobArrivalTime, release/arrival time of this job
     * @param jobDeadline
     * @param jobPriority
     * @param numOfRequestedPE
     * @param estimatedComputationLength
     * @param estimatedComputationalMIPS
     */
    public Job (int jobId, double jobLength, double jobRealLength, long jobFileSize,
            long jobOutputSize, String oSrequired, String archRequired,
            double jobArrivalTime, double jobDeadline, int jobPriority, int numOfRequestedPE, 
            double estimatedComputationLength, double estimatedComputationalMIPS) {
    	
        super(jobId, jobLength, jobFileSize, jobOutputSize);
        this.setOSRequired(oSrequired);
        this.setRealComputationalLength(jobRealLength);
        this.setArchRequired(archRequired);
        this.setArrivalTime(jobArrivalTime);
        this.setDeadline(jobDeadline);
        this.setJobPriority(jobPriority);
        this.setNumPE(numOfRequestedPE);
        this.setEstimatedComputationTime(estimatedComputationLength);
        this.setEstimatedComputationalMIPS(estimatedComputationalMIPS);
    }
    
	
    /* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#getJobIdLocalID()
	 */
	public int getJobLocalID() {
		return super.getGridletID();
		
	}

    public String getGlobalJobID() {
		return globalJobID;
	}

	public void setGlobalJobID(String globalJobID) {
		this.globalJobID = globalJobID;
	}
	
    /** id of original MaGate where the job is submitted */
	public String getOriginalMaGateId() {
		return originalMaGateId;
	}

	/** id of original MaGate where the job is submitted */
	public void setOriginalMaGateId(String originalMaGateId) {
		this.originalMaGateId = originalMaGateId;
		this.globalJobID = originalMaGateId + "_" + this.getJobLocalID();
	}
	
    /* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#getJobStatus()
	 */
	public int getJobStatus() {
		return super.getGridletStatus();
	}
	
    /* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#setJobStatus()
	 */
	public void setJobStatus(int newStatus) {
		try {
			super.setGridletStatus(newStatus);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    /* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#getJobFinishedSoFar()
	 */	
	public double getJobFinishedSoFar() {
		return super.getGridletFinishedSoFar();
	}
	
	/* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#getJobLength()
	 */	
	public double getJobLength() {
		return super.getGridletLength();
	}

	public double getActualCPUTime() {
		return super.getActualCPUTime();
	}
	
	public double getFinishTime() {
		return super.getFinishTime();
	}

    /// --- --- --- Getter and Setter methods
    
    /* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#getOpSystemRequired()
	 */
    public String getOSRequired() {
        return osRequired;
    }
    /* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#setOpSystemRequired(java.lang.String)
	 */
    public void setOSRequired(String osRequired) {
        this.osRequired = osRequired;
    }
    /* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#getArrivalTime()
	 */
    public double getArrivalTime() {
        return arrivalTime;
    }
    /* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#setArrivalTime(double)
	 */
    public void setArrivalTime(double startTime) {
        this.arrivalTime = startTime;
    }
    /* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#getArchRequired()
	 */
    public String getArchRequired() {
        return archRequired;
    }
    /* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#setArchRequired(java.lang.String)
	 */
    public void setArchRequired(String archRequired) {
        this.archRequired = archRequired;
    }
    /* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#getDeadline()
	 */
    public double getDeadline() {
        return deadline;
    }
    /* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#setDeadline(double)
	 */
    public void setDeadline(double deadline) {
        this.deadline = deadline;
    }
    /* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#getJobPriority()
	 */
    public int getJobPriority() {
        return jobPriority;
    }
    /* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#setJobPriority(int)
	 */
    public void setJobPriority(int jobPriority) {
        this.jobPriority = jobPriority;
    }    

    /* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#getRealComputationalLength()
	 */
    public double getRealComputationalLength() {
        return realComputationalLength;
    }

    /* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#setRealComputationalLength(double)
	 */
    public void setRealComputationalLength(double realComputationalLength) {
        this.realComputationalLength = realComputationalLength;
    }

    /* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#getEstimatedComputationTime()
	 */
    public double getEstimatedComputationTime() {
        return estimatedComputationTime;
    }

    /* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#setEstimatedComputationTime(double)
	 */
    public void setEstimatedComputationTime(double estimatedComputationTime) {
        this.estimatedComputationTime = estimatedComputationTime;
    }

    /* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#getEstimatedComputationalMIPS()
	 */
    public double getEstimatedComputationalMIPS() {
        return estimatedComputationalMIPS;
    }

    /* (non-Javadoc)
	 * @see ch.eif.gridgroup.magate.model.MaGateJob#setEstimatedComputationalMIPS(double)
	 */
    public void setEstimatedComputationalMIPS(double estimatedComputationalMIPS) {
        this.estimatedComputationalMIPS = estimatedComputationalMIPS;
    }
    
    public boolean isFinished() {
    	return super.isFinished();
    }

    public Job clone() {
        
    	Job job = null;
        try {
        	job = (Job) super.clone();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        return job;
    }
}
