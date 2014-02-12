package ch.hefr.gridgroup.magate.model;

import gridsim.*;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import ch.hefr.gridgroup.magate.ext.Hole;

/**
 * Class SimResourceInfo<p>
 * This class stores <b>dynamic information</b> about each resource. 
 * E.g. prepared schedule for this resource, list of jobDescriptions of jobs in execution. 
 * It also provides methods to calculate various parameters based on
 * the knowledge of the schedule and resource status, 
 * e.g. expected makespan, tardiness or number of delayed jobs. 
 * It contains methods that are able to find hole (gap) in the schedule 
 * and fill it with suitable job.
 *
 * @author Dalibor Klusacek (Alea simulator)
 * @author Ye HUANG (refacting)
 */


public class SimResourceInfo implements ResourceInfo {
	
    /** Resource char. object - used to get information about Resource*/
    private ResourceCharacteristics resource;

	/** List of jobInfos "on Resource" */
    @SuppressWarnings("unchecked")
	private LinkedList jobListInExecution;   
    
    /** List representing schedule for this resource (jobInfos) */
    @SuppressWarnings("unchecked")
    private LinkedList jobListInSchedule;    
    
	/** denotes Earliest Start Time */
    private double earliestStartTime = Double.MAX_VALUE;

	/** Sum of tardiness of all finished jobs */
    private double totalTardinessOfFinishedJobs = 0.0;    
    
	/** 
     * Denotes if previously selected job was succesfully sent by JobSubmitter - prevents anticipating of jobs 
     * Will be set "true" in MatchMaker.jobAlreadySentToResource(),
     * and set "false" when jobinfo are simply scheduled
     */
    private AtomicBoolean prevSelectedJobSentSuccessful = new AtomicBoolean(true);   

	/** denotes number of previous finished non-delayed jobs */
    private int numOfPreviousFinishedNondelayedJobs = 0;  

	/** denotes resource tardiness */
    private double resourceTardiness = 0.0;
    
    /** denotes resource makespan */
    private double resourceMakespan = 0.0;
    
    /** denotes expected number of nondelayed jobs so far */
    private int numOfExpectedNondelayedJobs = 0;   
    
    /** denotes total length of holes in seconds */    
    private double totalHolesLength = 0.0;   
    
    /** denotes size of holes in MIPS */
    private double holesMIPS = 0.0;   
    
    /** denotes expected number of delayed jobs according to prepared schedule only (no "already finished nondelay jobs" included) */
    private int numOfExpectedDelayedJobsForPreparedSched = 0;   
    
    /** Denotes the total number of PE on Resource */
    private int numOfTotalPE;
    
    /** denotes expected number of nondelayed jobs according to prepared schedule only (no "already finished nondelay jobs" included) */
    private int numOfExpectedNondelayedJobsForPreparedSched = 0;   
    
    /** Total MIPS actually available on a resource */
    private double totalAvailableMIPS;
    
    /** denotes stable state = information about schedule are correct, no update needed */
    private boolean stable = false;
    
    /** denotes last time the information were stable */
    private double prevClock = 0.0;   
    
    /** denotes finish time of some job on some PE (CPU) */
    private double finishTimeOnPE[] = null;
    
    /** denotes start time of some job on some PE (CPU) */
    private double startTimeOnPE[] = null;
    
    /** denotes list of known holes (gaps) in the schedule */
    @SuppressWarnings("unchecked")
    private LinkedList knownHolesInSched = new LinkedList();   
    
    private AtomicInteger numOfVirtualFreePE = new AtomicInteger(0);

	/** 
     * Creates a new instance of ResourceInfo with "in schedule" and "on resource" lists of jobs
     * @param resource Resource characteristics (number of CPU, rating, etc.)
     */
    @SuppressWarnings("unchecked")
	public SimResourceInfo(ResourceCharacteristics resource)     {
        this.resource = resource;
        this.numOfTotalPE = resource.getNumPE();
        
        this.finishTimeOnPE     = new double[resource.getNumPE()];
        this.startTimeOnPE      = new double[resource.getNumPE()];
        this.jobListInExecution = new LinkedList();
        this.jobListInSchedule  = new LinkedList();
        
        // the total MIPS rating, which is the sum of MIPS rating of all machines in a resource
        this.totalAvailableMIPS = resource.getMIPSRating();
    }
    
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#getNumOfUnfinishedCommittedJob()
	 */
    public int getNumOfUnfinishedCommittedJob() {
        int num = getUnFinishedJobLengthAccumulator().getCount();
        return num;
    }
    
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#checkResourceSuitableForJob(ch.hefr.gridgroup.magate.model.JobInfo)
	 */
    public boolean checkResourceSuitableForJob(JobInfo gi){
        if (gi.getArchRequired().equals(resource.getResourceArch()) &&
                gi.getOsRequired().equals(resource.getResourceOS())) {
            return true;
        }else{
            return false;
        }
    }
    
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#lowerResInExec(ch.hefr.gridgroup.magate.model.JobInfo)
	 */
    public void lowerResInExec(JobInfo gi){
    	
    	// Removes JobInfo from list of "JobInfo list on this Resource" (only JobInfo there not Job)
    	// lower the load of resource
        for (int j = 0; j < jobListInExecution.size(); j++){
        	JobInfo giRes = (JobInfo) jobListInExecution.get(j);
            
            if (giRes.getJobLocalID() == gi.getJobLocalID() && giRes.getUserID() == gi.getUserID()){
                jobListInExecution.remove(j);
                stable = false;
                break;
            }
        }
    }
    
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#lowerResScheduleList(ch.hefr.gridgroup.magate.model.JobInfo)
	 */
    public void lowerResScheduleList(JobInfo gi){
        for (int j = 0; j < jobListInSchedule.size(); j++){
        	JobInfo giRes = (JobInfo) jobListInSchedule.get(j);
            if (giRes.getJobLocalID() == gi.getJobLocalID() && giRes.getUserID() == gi.getUserID()){
                jobListInSchedule.remove(j);
                stable = false;
                break;
            }
        }
    }
    
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#getNumOfFreePE()
	 */
    public int getNumOfFreePE(){
        int freePE = this.numOfTotalPE;
        for (int j = 0; j < jobListInExecution.size(); j++){
        	JobInfo gi = (JobInfo) jobListInExecution.get(j);
            if(gi.getJobStatus() != Gridlet.SUCCESS){
                freePE = freePE - gi.getNumPE();
            }
        }
        return Math.max(0, freePE);
    }
    
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#update(double)
	 */
    public void update(double currentTime){
        double min_time = Double.MAX_VALUE;
        double total_time = 0.0;
        double time_to_release = 0.0;
        double total_tardiness = 0.0;
        double tardiness = 0.0;
        int nondelayed = 0;
        
        if(prevClock == currentTime && stable){
            // no change - so save computational time
            return;
        }else{
            // setup the field representing CPUs earliest free slot times
            knownHolesInSched.clear();
            totalHolesLength = 0.0;
            holesMIPS = 0.0;
            predictFinishTimeOfAssignedMultiJobs(currentTime); //OK works
            
            // calculate expected tardiness for jobs in schedule
            for (int j = 0; j < jobListInSchedule.size(); j++){
            	JobInfo gi = (JobInfo) jobListInSchedule.get(j);
                // simulate the FCFS attitude of LRM on the resource
                int index = selectMultiMinCompletionTime(finishTimeOnPE, gi);
                //int index = selectMinCompletionTime(finishTimeOnPE);
                gi.setExpectedFinishTime(finishTimeOnPE[index]);
                double glFinishTime = gi.getComputationalLength()/resource.getMIPSRatingOfOnePE();
                if (glFinishTime < 1.0) {
                    glFinishTime = 1.0;
                }
                int roundUpTime = (int) (glFinishTime+1);
                // time when the job will be probably finished on CPU #index
                double earliestNextTime = finishTimeOnPE[index];
                
                finishTimeOnPE[index] += roundUpTime;
                
                // tardiness of this job in this schedule
                tardiness = Math.max(0.0, finishTimeOnPE[index] - gi.getDeadline());
                //gi.setExpectedFinishTime(finishTimeOnPE[index]);
                gi.setTardiness(tardiness); // after this method we know each gridlet's tardiness
                if(tardiness <= 0.0) nondelayed++;
                total_tardiness += tardiness;
                
                // update also the rest of PEs finish-time required to run this job
                for (int k = 0; k < finishTimeOnPE.length; k++){
                    if ( finishTimeOnPE[k] < -998){
                        finishTimeOnPE[k] = finishTimeOnPE[index];
                    }else if(finishTimeOnPE[k] < earliestNextTime){
                        // since it is FCFS resource, do no allow earlier starts
                        finishTimeOnPE[k] = earliestNextTime;
                    }
                }
            }
            
            numOfExpectedDelayedJobsForPreparedSched = jobListInSchedule.size() - nondelayed;
            
            // add expected tardiness of running jobs
            for (int j = 0; j < jobListInExecution.size(); j++){
            	JobInfo gi = (JobInfo) jobListInExecution.get(j);
                total_tardiness += gi.getTardiness();
                if(gi.getTardiness() <= 0.0) nondelayed++;
            }
            
            // calculate makespan
            double makespan = 0.0;
            for (int j = 0; j < finishTimeOnPE.length; j++){
                if ( finishTimeOnPE[j] > makespan ){
                    makespan = finishTimeOnPE[j];
                }
            }
            
            // add tardiness and score of already finished jobs
            numOfExpectedNondelayedJobsForPreparedSched = nondelayed;
            total_tardiness += totalTardinessOfFinishedJobs;
            nondelayed += numOfPreviousFinishedNondelayedJobs;
            
            // set the variables to new values
            resourceTardiness = total_tardiness;
            numOfExpectedNondelayedJobs = nondelayed;
            resourceMakespan = makespan;
            
            stable = true;
            prevClock = currentTime;
        }
        //System.out.println(this.resource.getResourceName()+" - "+holes.size()+" | "+resSchedule.size());
    }
    
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#forceUpdate(double)
	 */
    public void forceUpdate(double current_time){
        double min_time = Double.MAX_VALUE;
        double total_time = 0.0;
        double finishTimeOnPE[] = new double[numOfTotalPE];
        double time_to_release = 0.0;
        double total_tardiness = 0.0;
        double tardiness = 0.0;
        int nondelayed = 0;
        //System.out.println("---- update ----");
        // setup the field representing CPUs earliest free slot times
        knownHolesInSched.clear();
        totalHolesLength = 0.0;
        holesMIPS = 0.0;
        predictFinishTimeOfAssignedMultiJobs(current_time); //OK works
        
        // calculate expected tardiness for jobs in schedule
        for (int j = 0; j < jobListInSchedule.size(); j++){
        	JobInfo gi = (JobInfo) jobListInSchedule.get(j);
            // simulate the FCFS attitude of LRM on the resource
            int index = selectMultiMinCompletionTime(finishTimeOnPE, gi);
            //int index = selectMinCompletionTime(finishTimeOnPE);
            gi.setExpectedFinishTime(finishTimeOnPE[index]);
            double glFinishTime = gi.getComputationalLength()/resource.getMIPSRatingOfOnePE();
            if (glFinishTime < 1.0) {
                glFinishTime = 1.0;
            }
            int roundUpTime = (int) (glFinishTime+1);
            // time when the gridlet will be probably finished on CPU #index
            double earliestNextTime = finishTimeOnPE[index];
            
            finishTimeOnPE[index] += roundUpTime;
            
            // tardiness of this gridlet in this schedule
            tardiness = Math.max(0.0, finishTimeOnPE[index] - gi.getDeadline());
            //gi.setExpectedFinishTime(finishTimeOnPE[index]);
            gi.setTardiness(tardiness); // after this method we know each job's tardiness
            if(tardiness <= 0.0) nondelayed++;
            total_tardiness += tardiness;
            
            // update also the rest of PEs finish-time required to run this job
            for (int k = 0; k < finishTimeOnPE.length; k++){
                if ( finishTimeOnPE[k] < -998){
                    finishTimeOnPE[k] = finishTimeOnPE[index];
                }else if(finishTimeOnPE[k] < earliestNextTime){
                    // since it is FCFS resource, do no allow earlier starts
                    finishTimeOnPE[k] = earliestNextTime;
                }
            }
        }
        //System.out.println(holes.size()+"\t"+resSchedule.size());
        
        numOfExpectedDelayedJobsForPreparedSched = jobListInSchedule.size() - nondelayed;
        
        // add expected tardiness of running jobs
        for (int j = 0; j < jobListInExecution.size(); j++){
        	JobInfo gi = (JobInfo) jobListInExecution.get(j);
            total_tardiness += gi.getTardiness();
            if(gi.getTardiness() <= 0.0) nondelayed++;
        }
        
        // add tardiness and score of already finished jobs
        numOfExpectedNondelayedJobsForPreparedSched = nondelayed;
        total_tardiness += totalTardinessOfFinishedJobs;
        nondelayed += numOfPreviousFinishedNondelayedJobs;
        
        // set the variables to new values
        resourceTardiness = total_tardiness;
        numOfExpectedNondelayedJobs = nondelayed;
        
        stable = true;
        prevClock = current_time;
        
    }
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#getEarliestStartTimeForJobInfo(ch.hefr.gridgroup.magate.model.JobInfo, double)
	 */
    public double getEarliestStartTimeForJobInfo(JobInfo gi, double currentTime){
        // updates finishTimeOnPE
        this.updateFinishTimeOfAssignedJobs(currentTime);
        // get EST according to gi PE count
        int index = selectMultiMinCompletionTimeOfAssignedJobs(finishTimeOnPE, gi);
        this.earliestStartTime = finishTimeOnPE[index]; // Earl. Start Time for head of queue / schedule
        return finishTimeOnPE[index];
    }
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#findHoleForJobInfo(ch.hefr.gridgroup.magate.model.JobInfo)
	 */
    public boolean findHoleForJobInfo(JobInfo gi){
        if(gi.getNumPE() > this.numOfTotalPE) return false;
        
        double mips = gi.getComputationalLength();
        Hole candidate = null;
        double prev_end = Double.MAX_VALUE;
        
        for (int i = 0; i < knownHolesInSched.size(); i++){
            Hole h = (Hole) knownHolesInSched.get(i);
            
            if(h.getSize()>=gi.getNumPE() && h.getStart() <= prev_end){
                if(candidate == null){
                    // new candidate hole
                    candidate = h;
                }
                // next hole has to start right after this hole
                prev_end = h.getEnd();
                
                // hole(s) are large enough
                if(mips <= h.getMips()){
                    // what is the candidate position in schedule
                	JobInfo nextGi = (JobInfo) candidate.getPosition(); // because of this Gi the hole(s) were created
                    int index = jobListInSchedule.indexOf(nextGi);
                    // add gi at the candidate position - i.e. shifts the rest of jobs
                    this.addJobInfo(index, gi);
                    //System.out.println("Hole gi = "+gi.getID());
                    return true;
                    
                }else{
                    // hole(s) are still small
                    // decrease remaining length of hole
                    mips = mips - h.getMips();
                }
            }else{
                // restart search for hole - this one is not good (small PEs size)
                candidate = null;
                mips = gi.getComputationalLength();
                prev_end = Double.MAX_VALUE;
            }
        }
        return false;
    }
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#removeJobInfo(ch.hefr.gridgroup.magate.model.JobInfo)
	 */
    public boolean removeJobInfo(JobInfo gi){
        stable = false;
        /*for (int i = 0; i < holes.size(); i++){
            Hole h = (Hole) holes.get(i);
            GridletInfo gh = (GridletInfo) h.getPosition();
            if(gh.equals(gi)){
                holes.remove(i);
                i--;
            }
        }*/
        knownHolesInSched.clear();
        return jobListInSchedule.remove(gi);
    }
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#removeJobInfoIndex(int)
	 */
    public JobInfo removeJobInfoIndex(int index){
        stable = false;
        knownHolesInSched.clear();
        return (JobInfo) jobListInSchedule.remove(index);
    }
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#removeFirstJobInfo()
	 */
    public JobInfo removeFirstJobInfo(){
        stable = false;
        JobInfo gi = (JobInfo) jobListInSchedule.removeFirst();
        /*for (int i = 0; i < holes.size(); i++){
            Hole h = (Hole) holes.get(i);
            GridletInfo gh = (GridletInfo) h.getPosition();
            if(gh.equals(gi)){
                holes.remove(i);
                i--;
            }
        }*/
        knownHolesInSched.clear();
        return gi;
    }
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#addLastJobInfo(ch.hefr.gridgroup.magate.model.JobInfo)
	 */
    public void addLastJobInfo(JobInfo gi){
        stable = false;
        jobListInSchedule.addLast(gi);
        knownHolesInSched.clear();
    }
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#addJobInfo(int, ch.hefr.gridgroup.magate.model.JobInfo)
	 */
    public void addJobInfo(int index, JobInfo gi){
        stable = false;
        jobListInSchedule.add(index, gi);
        knownHolesInSched.clear();
    }
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#addJobInfoInExec(ch.hefr.gridgroup.magate.model.JobInfo)
	 */
    public void addJobInfoInExec(JobInfo gi){
        stable = false;
        jobListInExecution.add(gi);
        knownHolesInSched.clear();
    }
    
    /// --- private methods ---
    
    /** 
     * Selects index of the last CPU necessary to run multi-CPU job. Auxiliary method
     * that does not create holes list! Use only when holes are not necessary (Local machine Queue or strictly 
     * Queue-based algorithms).
     * 
     * @param finishTimeOnPE[] field representing earliest free slot of each CPU on machine
     * @param gi gridletInfo describing the multi-CPU job
     */
    private int selectMultiMinCompletionTimeOfAssignedJobs(double finishTimeOnPE[], JobInfo gi){
    	
        int index = 0;
        double min = Double.MAX_VALUE;
        
        for (int i = 0; i < gi.getNumPE(); i++){
        	
        	// Loop finish time of all PEs
            for (int j = 0; j < finishTimeOnPE.length; j++){
            	
                // if other PE needed to run job - be carefully when comparing 2 double values
            	// Notice: all PE's finish time has been set to current time in method "predictFinishTimeOfAssignedMultiJobs(currentTime)"
                if ( finishTimeOnPE[j] <= min && finishTimeOnPE[j] > -998){
                	
                	// candidate PE found for current PE request (for current multi-PE job)
                	// then variable "min" is set to current PE's expected finish time
                	// which means if next PE's expected finish time is later than this one, next PE won't be considered for this multi-PE job
                    min = finishTimeOnPE[j];
                    index = j;
                }
            }
            
            //reset min value if not the last PE allocated
            min = Double.MAX_VALUE; //here remember hole
            
            // last required PE by current job is NOT reached yet
            // therefore, current PE is reserved for this job by marking its "finishTimeOnPE[index]" to -999
            if(i != (gi.getNumPE()-1) ){
            	// IMPORTANT: current PE is set to -999, which is less than -998 (< -998)
            	// it will be notified by method predictFinishTimeOfAssignedMultiJobs
                finishTimeOnPE[index] = -999;
            }
        }
        //System.out.println(holes.size());
        return index;
    }
    
    /** Selects index of the last CPU necessary to run multi-CPU job. Auxiliary method
     * @param finishTimeOnPE[] field representing earliest free slot of each CPU on machine
     * @param gi gridletInfo describing the multi-CPU gridlet
     */
    private int selectMultiMinCompletionTime(double finishTimeOnPE[], JobInfo gi){
        int index = 0;
        double min = Double.MAX_VALUE;
        double hole_start = -1.0;
        int hole_size = 1;
        
        for (int i = 0; i < gi.getNumPE(); i++){
        	
        	// Loop finish time of all PEs
            for (int j = 0; j < finishTimeOnPE.length; j++){
                // if other PE needed to run job - be carefully when comparing 2 double values
                if ( finishTimeOnPE[j] <= min && finishTimeOnPE[j] > -998){
                    min = finishTimeOnPE[j];
                    index = j;
                }
            }
            
            // processing hole list for queue search
            if(hole_start <= 0.0){ // hole not started yet
                hole_start = min; // possible hole start
                
            } else { // finish hole or continue creating it?
                
            	if (hole_start != min) {
                    double length = min - hole_start;
                    Hole h = new Hole(hole_start, min, length, (length*this.resource.getMIPSRatingOfOnePE()), hole_size, gi);
                    totalHolesLength += length*hole_size;
                    holesMIPS += length*this.resource.getMIPSRatingOfOnePE()*hole_size;                    
                    
                    knownHolesInSched.addLast(h);
                    
                    hole_size++;
                    hole_start = min;
                } else {
                    hole_size++;
                    //ok continue
                }
            }
            
            //reset min value if not the last PE allocated
            min = Double.MAX_VALUE; //here remember hole
            
         // last required PE by current job is NOT reached yet
            // therefore, current PE is reserved for this job by marking its "finishTimeOnPE[index]" to -999
            if(i != (gi.getNumPE()-1) ){
                finishTimeOnPE[index] = -999;
            }
        }
        //System.out.println(holes.size());
        return index;
        
    }
    
    
    /** 
     * Predicts finish time of already running or assigned multi-CPU jobs (i.e. INEXEC, QUEUED, CREATED)
     * @param finishTimeOnPE[] field representing earliest free slot of each CPU on machine
     * @param currentTime the current simulation time
     */
    private void predictFinishTimeOfAssignedMultiJobs(double currentTime){
    	
    	// index of array finishTimeOnPE[], to reset expected finish time of all PEs
    	// firstly, set finish time of PEs (1st part of finishTimeOnPE[]) for InExec jobs
    	// then, set finish time of PEs (2nd part of finishTimeOnPE[]) for Queued jobs
    	// at last, set finish time of PEs (3rd part of finishTimeOnPE[]) for jobs Neither Successfully Exed nor InExec/Queued
        int peIndex = 0;
        
        // Important: update all PE's finishtime to current time, which is "> 0" and "< Double.Max"
        for (int j = 0; j < finishTimeOnPE.length; j++){
            finishTimeOnPE[j] = currentTime;
        }
        
        for (int j = 0; j < jobListInExecution.size(); j++){
        	JobInfo gi = (JobInfo) jobListInExecution.get(j);
            if(gi.getJobStatus() == Gridlet.INEXEC){
            	
                double run_time = currentTime - ((Job) (gi.getJob())).getExecStartTime();
                double length = gi.getComputationalLength() - (run_time * resource.getMIPSRatingOfOnePE());
                
                // update all PE-finish-time that will run this job
                for(int k = 0; k < gi.getNumPE(); k++){
                    finishTimeOnPE[peIndex] += length / resource.getMIPSRatingOfOnePE();
                    peIndex++;
                }
                double giTard = Math.max(0.0, finishTimeOnPE[peIndex-1] - gi.getDeadline());
                gi.setExpectedFinishTime(finishTimeOnPE[peIndex-1]);
                gi.setTardiness(giTard);
            }
        }
        
        for (int j = 0; j < jobListInExecution.size(); j++){
        	
        	JobInfo gi = (JobInfo) jobListInExecution.get(j);
            if(gi.getJobStatus() == Gridlet.QUEUED){
            	
                // return the last needed PE index (others finish-time set to -999)
            	// peIndex here refers to the last PE necessary to run multi-CPU job
                peIndex = selectMultiMinCompletionTimeOfAssignedJobs(finishTimeOnPE, gi);
                
                finishTimeOnPE[peIndex] += (gi.getComputationalLength() / resource.getMIPSRatingOfOnePE());
                double giTard = Math.max(0.0, finishTimeOnPE[peIndex] - gi.getDeadline());
                gi.setExpectedFinishTime(finishTimeOnPE[peIndex]);
                gi.setTardiness(giTard);
                
                // update all PE-finish-time that will run this job
                // "peIndex" is the LAST index of all PEs reserved for handling current job
                // other PEs are marked by assigning the "finishTimeOnPE" to -999
                for (int k = 0; k < finishTimeOnPE.length; k++){
                    if ( finishTimeOnPE[k] < -998){
                        finishTimeOnPE[k] = finishTimeOnPE[peIndex];
                    }
                }
            }
        }
        
        for (int j = 0; j < jobListInExecution.size(); j++){
        	JobInfo gi = (JobInfo) jobListInExecution.get(j);
            if(gi.getJobStatus() != Gridlet.SUCCESS && gi.getJobStatus() != Gridlet.INEXEC && gi.getJobStatus() != Gridlet.QUEUED){

                // return the last needed PE index (others finish-time set to -999)
            	// peIndex here refers to the last PE necessary to run multi-CPU job
                peIndex = selectMultiMinCompletionTimeOfAssignedJobs(finishTimeOnPE, gi);
                
                finishTimeOnPE[peIndex] += (gi.getComputationalLength() / resource.getMIPSRatingOfOnePE());
                double giTard = Math.max(0.0, finishTimeOnPE[peIndex] - gi.getDeadline());
                gi.setExpectedFinishTime(finishTimeOnPE[peIndex]);
                gi.setTardiness(giTard);
                
                // update all PE-finish-time that will run this job
                // "peIndex" is the LAST index of all PEs reserved for handling current job
                // other PEs are marked by assigning the "finishTimeOnPE" to -999
                for (int k = 0; k < finishTimeOnPE.length; k++){
                    if ( finishTimeOnPE[k] < -998){
                        finishTimeOnPE[k] = finishTimeOnPE[peIndex];
                    }
                }
            }
        }
        
        System.arraycopy(finishTimeOnPE,0,startTimeOnPE,0,finishTimeOnPE.length);
        
    }
    
    
    /**
     * This method force re-computation of jobs-on-resource status. It also updates information about their
     * expected finish time, tardiness etc.
     */
    private void updateFinishTimeOfAssignedJobs(double currentTime){
        // setup the field representing CPUs earliest free slot times
        predictFinishTimeOfAssignedMultiJobs(currentTime); //OK works
    }
    
    
    /** Returns Accumulator of unfinished job lengths */
    private Accumulator getUnFinishedJobLengthAccumulator() {
        Accumulator accLength = new Accumulator();
        for (int i = 0; i < jobListInExecution.size(); i++) {
            int status = ( (JobInfo) jobListInExecution.get(i) ).getJob().getJobStatus();
            if ( status != Gridlet.SUCCESS ) {
                accLength.add( ((JobInfo) jobListInExecution.get(i)).getComputationalLength() );
            }
        }
        return accLength;
    }
    
    /**
     * Auxiliary method
     */
    private void printHoles(){
        if(knownHolesInSched.size()>0 && jobListInSchedule.size()==1){
            Hole h = (Hole) knownHolesInSched.getFirst();
            JobInfo gi = (JobInfo) jobListInSchedule.getFirst();
            System.out.println(knownHolesInSched.size()+"\t"+jobListInSchedule.size()+" | "+h.getPosition().getJobLocalID()+
                    ","+gi.getJobLocalID()+" | "+h.getLength());
        }
    }
    /**
     * Auxiliary method
     */
    private void printPEs(){
        if(knownHolesInSched.size()>0 && jobListInSchedule.size()==1){
            Hole h = (Hole) knownHolesInSched.getFirst();
            JobInfo gi = (JobInfo) jobListInSchedule.getFirst();
            for(int j = 0; j < startTimeOnPE.length; j++){
                System.out.println(Math.round(startTimeOnPE[j])+"\t"+Math.round(finishTimeOnPE[j]));
            }
            System.out.println(resource.getResourceID()+" ---------------- "+Math.round(h.getLength())+" | "+
                    gi.getNumPE()+" | "+Math.round(gi.getComputationalLength()/resource.getMIPSRatingOfOnePE()));
        }
    }
    
    /**
     * Auxiliary method
     */
    private void printSchedule(){
        for(int j = 0; j < jobListInSchedule.size(); j++){
        	JobInfo gi = (JobInfo) jobListInSchedule.get(j);
            System.out.print(gi.getJobLocalID()+",");
        }
    }
    
    
    /*****************************************
     * Obsolete and Getter/Setter methods
     *****************************************/
    
    /** 
     * Updates start times in the "CPU field" according to release date of a multi-CPU job - auxiliary method
     * 
     *@param finishTimeOnPE[] field representing <b>Earliest Free Slot</b> of each CPU on machine
     *@param startTime either current time or job release date - according to what is higher
     */
    private void updateMultiStartTime(double finishTimeOnPE[], double startTime){
        
        for (int j = 0; j < finishTimeOnPE.length; j++){
            if(finishTimeOnPE[j] < startTime && finishTimeOnPE[j] > -998.0)
                finishTimeOnPE[j] = startTime;
        }
    }
    
    /** Calculate expected makespan of this resource (based on the current schedule
     * knowledge and resource status monitoring) when scheduling multi-CPU jobs.
     *
     * Obsolete - use update(currentTime) method.
     *
     * @param currentTime current simulation time
     * @deprecated
     */
    private double predictResourceMakespanForMultiJobs(double currentTime){
        double min_time = Double.MAX_VALUE;
        double total_time = 0.0;
        double finishTimeOnPE[] = new double[numOfTotalPE];
        double time_to_release = 0.0;
        
        // setup the field representing CPUs earliest free slot times
        predictFinishTimeOfAssignedMultiJobs(currentTime); //OK works
        
        // calculate expected makespan for jobs in schedule
        for (int j = 0; j < jobListInSchedule.size(); j++){
        	JobInfo gi = (JobInfo) jobListInSchedule.get(j);
            // simulate the FCFS attitude of LRM on the resource
            int index = selectMultiMinCompletionTime(finishTimeOnPE, gi);
            
            time_to_release = Math.max(0.0, gi.getArrivalTime() - finishTimeOnPE[index]);
            if(time_to_release > 0.0){
                finishTimeOnPE[index] += time_to_release;
                updateMultiStartTime(finishTimeOnPE, finishTimeOnPE[index]);
            }
            double glFinishTime = gi.getComputationalLength()/resource.getMIPSRatingOfOnePE();
            if (glFinishTime < 1.0) {
                glFinishTime = 1.0;
            }
            int roundUpTime = (int) (glFinishTime+1);
            // time when the gridlet will be probably finished on CPU #index
            double earliestNextTime = finishTimeOnPE[index];
            finishTimeOnPE[index] += roundUpTime;
            // update also the rest of PEs finish-time required to run this gridlet
            for (int k = 0; k < finishTimeOnPE.length; k++){
                if ( finishTimeOnPE[k] < -998){
                    finishTimeOnPE[k] = finishTimeOnPE[index];
                }else if(finishTimeOnPE[k] < earliestNextTime){
                    // since it is FCFS resource, do no allow earlier starts
                    finishTimeOnPE[k] = earliestNextTime;
                }
            }
        }
        
        // return the max. time - it represents finish time for the Resource (last gridlet finishes)
        for (int j = 0; j < finishTimeOnPE.length; j++){
            if ( finishTimeOnPE[j] > total_time ){
                total_time = finishTimeOnPE[j];
            }
        }
        return total_time;
    }
    
    /**
     * Obsolete - use update(currentTime) method.
     * @deprecated
     */
    private double predictResourceTardinessForMultiJobs(double currentTime){
        double min_time = Double.MAX_VALUE;
        double total_time = 0.0;
        double finishTimeOnPE[] = new double[numOfTotalPE];
        double time_to_release = 0.0;
        double total_tardiness = 0.0;
        double tardiness = 0.0;
        int nondelayed = 0;
        
        
        // setup the field representing CPUs earliest free slot times
        predictFinishTimeOfAssignedMultiJobs(currentTime); //OK works
        
        // calculate expected tardiness for jobs in schedule
        for (int j = 0; j < jobListInSchedule.size(); j++){
        	JobInfo gi = (JobInfo) jobListInSchedule.get(j);
            // simulate the FCFS attitude of LRM on the resource
            int index = selectMultiMinCompletionTime(finishTimeOnPE, gi);
            
            double glFinishTime = gi.getComputationalLength()/resource.getMIPSRatingOfOnePE();
            if (glFinishTime < 1.0) {
                glFinishTime = 1.0;
            }
            int roundUpTime = (int) (glFinishTime+1);
            // time when the gridlet will be probably finished on CPU #index
            double earliestNextTime = finishTimeOnPE[index];
            finishTimeOnPE[index] += roundUpTime;
            
            // tardiness of this gridlet in this schedule
            tardiness = Math.max(0.0, finishTimeOnPE[index] - gi.getDeadline());
            gi.setTardiness(tardiness); // after this method we know each gridlet's tardiness
            if(tardiness <= 0.0) nondelayed++;
            total_tardiness += tardiness;
            
            // update also the rest of PEs finish-time required to run this gridlet
            for (int k = 0; k < finishTimeOnPE.length; k++){
                if ( finishTimeOnPE[k] < -998){
                    finishTimeOnPE[k] = finishTimeOnPE[index];
                }else if(finishTimeOnPE[k] < earliestNextTime){
                    // since it is FCFS resource, do no allow earlier starts
                    finishTimeOnPE[k] = earliestNextTime;
                }
            }
        }
        
        // add expected tardiness of running jobs
        for (int j = 0; j < jobListInExecution.size(); j++){
        	JobInfo gi = (JobInfo) jobListInExecution.get(j);
            total_tardiness += gi.getTardiness();
            if(gi.getTardiness() <= 0.0) nondelayed++;
        }
        
        // add tardiness and score of already finished jobs
        total_tardiness += totalTardinessOfFinishedJobs;
        nondelayed += numOfPreviousFinishedNondelayedJobs;
        
        resourceTardiness = total_tardiness;
        numOfExpectedNondelayedJobs = nondelayed;
        return total_tardiness;
    }
    
/// --- public attribute methods ---
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#getResource()
	 */
	public ResourceCharacteristics getResource() {
		return resource;
	}

	/* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#setResource(gridsim.ResourceCharacteristics)
	 */
	public void setResource(ResourceCharacteristics resource) {
		this.resource = resource;
	}
	
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#getJobListInSchedule()
	 */
    public LinkedList getJobListInSchedule() {
		return jobListInSchedule;
	}
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#getEarliestStartTime()
	 */
    public double getEarliestStartTime() {
		return earliestStartTime;
	}
    
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#getTotalTardinessOfFinishedJobs()
	 */
    public double getTotalTardinessOfFinishedJobs() {
		return totalTardinessOfFinishedJobs;
	}

    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#setTotalTardinessOfFinishedJobs(double)
	 */
	public void setTotalTardinessOfFinishedJobs(double totalTardinessOfFinishedJobs) {
		this.totalTardinessOfFinishedJobs = totalTardinessOfFinishedJobs;
	}
	
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#setNumOfPreviousFinishedNondelayedJobs(int)
	 */
    public void setNumOfPreviousFinishedNondelayedJobs(
			int numOfPreviousFinishedNondelayedJobs) {
		this.numOfPreviousFinishedNondelayedJobs = numOfPreviousFinishedNondelayedJobs;
	}

    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#getNumOfPreviousFinishedNondelayedJobs()
	 */
	public int getNumOfPreviousFinishedNondelayedJobs() {
		return numOfPreviousFinishedNondelayedJobs;
	}
	
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#isPrevSelectedJobSentSuccessful()
	 */
    public AtomicBoolean isPrevSelectedJobSentSuccessful() {
		return prevSelectedJobSentSuccessful;
	}

    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#setPrevSelectedJobSentSuccessful(boolean)
	 */
	public void setPrevSelectedJobSentSuccessful(
			AtomicBoolean prevSelectedJobSentSuccessful) {
		this.prevSelectedJobSentSuccessful = prevSelectedJobSentSuccessful;
	}
	
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.model.ResourceInfo#getNumOfTotalPE()
	 */
    public int getNumOfTotalPE() {
		return numOfTotalPE;
	}
    
    public AtomicInteger getNumOfVirtualFreePE() {
		return numOfVirtualFreePE;
	}


	public void setNumOfVirtualFreePE(AtomicInteger numOfVirtualFreePE) {
		this.numOfVirtualFreePE = numOfVirtualFreePE;
	}
}

