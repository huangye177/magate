package ch.hefr.gridgroup.magate.km;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.MaGateEntity;
import ch.hefr.gridgroup.magate.casa.CASAController;
import ch.hefr.gridgroup.magate.casa.CASAMessage;
import ch.hefr.gridgroup.magate.env.JobCenterManager;
import ch.hefr.gridgroup.magate.env.MaGateMediator;
import ch.hefr.gridgroup.magate.env.MaGateMessage;
import ch.hefr.gridgroup.magate.env.MaGateParam;
import ch.hefr.gridgroup.magate.env.MaGatePlatform;
import ch.hefr.gridgroup.magate.model.JobInfo;
import ch.hefr.gridgroup.magate.model.ResourceInfo;
import ch.hefr.gridgroup.magate.model.Job;
import ch.hefr.gridgroup.magate.storage.GlobalStorage;
import ch.hefr.gridgroup.magate.storage.MaGateDB;
import ch.hefr.gridgroup.magate.storage.MaGateStorage;
import eduni.simjava.Sim_event;
import eduni.simjava.Sim_system;
import eduni.simjava.Sim_type_p;
import gridsim.GridSim;
import gridsim.GridSimTags;

/**
 * Class ModuleController works as a communicative proxy to all the other MaGate modules.
 * @author Ye HUANG
 */
public class ModuleController extends GridSim {

	private String moduleControllerName;
	private long         previousClock;
	
	private static Log log = LogFactory.getLog(ModuleController.class);
	
	private MaGateEntity maGate;
	private MaGateMonitor maGateMonitor;
//	private long presTimeStamp;
	
	private String maGateId;
	
//	private int tempCount  = 0;
//	private int tempCount2 = 0; 
	
	public ModuleController(String moduleControllerName, double baudRate, 
			MaGateEntity maGate) throws Exception {
		
		super(moduleControllerName, baudRate);
		
		this.moduleControllerName = moduleControllerName;
		this.maGate = maGate;
		this.maGateId = maGate.getMaGateIdentity();
		this.previousClock  = System.currentTimeMillis();
	}

	/**
	 * Processing MaGate internal events
	 */
	public void body() {
		
		// ye: wait until ResourceEngine is ready
		while(!this.maGate.getResEngine().isResourceEngineReady()) {
			super.gridSimHold(10.0);
		}
		
		// ye: hold until the local resources are prepared(resource created) and 
		// registered(access channel created) before all the events
		this.maGate.getLRM().registerStorage();
		
		// ye: generate output file for getting experiment results
		this.maGateMonitor = new MaGateMonitor(maGate);
		
		// ye: hold until the jobs are prepared, and set SimJob owner to itself
		this.localJobSubmit();
		
//		log.debug("\n--- --- --- \nREADY FOR Sim_System \n--- --- --- ");
        // Accept User Commands and Process while not END_OF_SIMULATION for ALL JobSubmitters
        Sim_event ev = new Sim_event();
        
        while (Sim_system.running()) {
        	
        	GlobalStorage.count_moduleController.incrementAndGet();
            
            // system monitoring
            this.maGateMonitor.routineCheck();
            
            // check community events
            // Important to isolate these operations because threads sleep might be needed
            this.processCommunityEvents();
            
            // process CASP events
            // Important to isolate these operations because threads sleep might be needed
            if(MaGateParam.CASP_Enabled) {
            	this.processCASAEvents();
            }
            
            // process Critical Friend Model related events
            this.processCFMEvents();
            
            super.sim_get_next(ev);
            
            // ye: if CASP is enabled, such job needs to be disseminated to the network 
        	// in order to search candidate node 
            // The local MatchMaker will NOT response directly, the "SUBMITTED" job will be suspended here until processed by a CASP.processREQUEST behavior 
            if (ev.get_tag() == MaGateMessage.JobArrival) {
            	
            	JobInfo jobInfo = (JobInfo) ev.get_data();
            	
            	JobCenterManager.jobSubmitted(jobInfo.getGlobalJobID(), maGateId, jobInfo); 
                
        		// In next iteration, the CASP_Request will be invoked BY CASPController: 
        		// (1) resource discovery service
        		// (2) cached neighboring nodes  
            	
//            	GlobalStorage.recordLiveCommunityStatus();
            	
            	continue;
            }
            
            // ye: job submitted from JobSubmitter, MatchMaker will response
            if (ev.get_tag() == MaGateMessage.JobToMatchMaker) {
            	
            	JobInfo jobInfo = (JobInfo) ev.get_data();
            	jobInfo.setQueuingStartTime(MaGateMediator.getSystemTime());
            	// JobCenterManager.jobTransferred(jobInfo.getGlobalJobID(), maGateId, jobInfo); 
                
            	// transfer the request to MatchMaker
                this.maGate.getMatchMaker().jobScheduled(jobInfo);
                
//                GlobalStorage.recordLiveCommunityStatus();
                continue;
            }
            
//            // ye: job submitted from JobSubmitter, MatchMaker will response
//            if (ev.get_tag() == MaGateMessage.JobToMatchMakerFromCommunity) {
//            	
//            	JobInfo jobInfo = (JobInfo) ev.get_data();
//                JobCenterManager.jobTransferred(jobInfo.getGlobalJobID(), maGateId, jobInfo);
//                
//                // transfer the request to MatchMaker
//                this.maGate.getMatchMaker().jobScheduled(jobInfo);
//                
//                continue;
//            }
            
            // ye: schedule made by MatchMaker arrives, the JobSubmitter will response
            if (ev.get_tag() == MaGateMessage.ScheduleMadeByMatchMaker) {
            	this.onScheduleMadeByMatchMaker(ev.get_data());
            	
            	continue;
            }
            
            // ye: jobs/jobinfos are sent back after complete execution
            if (ev.get_tag() == GridSimTags.GRIDLET_RETURN){
            	this.onJobReturnFromResource(ev.get_data());
            	
            	continue;
            }
            
            if (ev.get_tag() == GridSimTags.JUNK_PKT){
            	log.warn("Ooops, an internal event arrives with JUNK_PKT tag");
            	
            	continue;
            }
            
            if (ev.get_tag() == MaGateMessage.Heartbeat){
//            	log.debug("HeartBeat waiting for unprocessed jobs");
            	
            	continue;
            }
            
            // ye: if the simulation finishes then exit the loop
            if (ev.get_tag() == GridSimTags.END_OF_SIMULATION) {
            	
            	// if a continuous envrionment, the simulation may never be halted.
//            	log.debug("MaGate simulation to be finished.");
            	break;
            }
            
        }
		
		this.terminateMaGate();
		
		// CHECK for ANY INTERNAL EVENTS WAITING TO BE PROCESSED
        while (super.sim_waiting() > 0) {
            // wait for event 
            super.sim_get_next(ev);
//            log.warn("unexpected internal events received.");
        }
        
        // system monitoring
        this.maGateMonitor.routineCheck();
    }
	
	
	/**
	 * process the community event checking routine
	 * It is important to isolate these operations because threads sleep might be needed
	 */
	private void processCommunityEvents() {
		
		if((MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Decentralized) && 
				(MaGateParam.systemIS == MaGateMessage.systemIS_Partial_ACO)) {
			
			this.maGate.getCommunityMonitor().updateCommunity();
			
//			this.maGate.getOutputRequester().processOutputJobQueue();
//		
//			this.maGate.getInputRequester().processInputJobQueue();
//		
//			this.maGate.getOutputResponser().processToReturnCommunityJobQueue();
			
		}
		
	}
	
	/**
	 * process the Community-Aware Scheduling Protocol(CASP) related events
	 * It is important to isolate these operations because threads sleep might be needed
	 */
	private void processCASAEvents() {
		
		// processing CASP relative affairs 
		this.maGate.getCASPController().processREQUEST();
		
		if((MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Decentralized) && 
				(MaGateParam.dynamicEnabled)) {
			this.maGate.getCASPController().processINFORM();
		}
		
//		long currentTime = System.currentTimeMillis();
//		
//		if(currentTime - this.previousClock > 100) {
//			
//			// update previous clock time
//			this.previousClock = currentTime;
//			
//			// processing CASP relative affairs 
//			this.maGate.getCASPController().processREQUEST();
//			
//			if((MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Decentralized) && (MaGateParam.caspDynamicEnabled)) {
//				this.maGate.getCASPController().processINFORM();
//			}
//		}
		
	}
	
	private void processCFMEvents() {
		
		// CFM Push sub-model
		this.maGate.getCFMController().cfmPushMonitoring();
	}
	
	/**
	 * Terminate this maGate
	 */
	private void terminateMaGate() {
		
		// ye: inform the MatchMaker all jobs sent from Submitter have been processed
		// the total tardiness of all jobs is appended for MatchMaker statistic calculation
		
		this.maGate.getJobSubmitter().sumTotalJobTardiness();
		double newArrivalTardiness = this.maGate.getJobSubmitter().getTotalTardiness();
		
    	this.maGate.getMatchMaker().caculateStatistic(newArrivalTardiness);
    	
    	if (this.maGate.getResEngine().isResourceEngineWorking()) {
    		super.send(this.maGate.getResEngine().getResEngineName(), 0.0, GridSimTags.END_OF_SIMULATION);
    		this.maGate.getResEngine().setResourceEngineWorking(false);
    	}
    	
    	this.finalizeMaGateStorage();
    	
		shutdownUserEntity();
        terminateIOEntities();
        
	}
	
	private void finalizeMaGateStorage() {
		
		GlobalStorage.total_count_job_db_stored.addAndGet(JobCenterManager.sizeOfJob_ProcessingNode(maGateId));
		GlobalStorage.total_count_job_local_exed.addAndGet(JobCenterManager.sizeOfLocalExedJob_ProcessingNode(maGateId));
		GlobalStorage.total_count_job_community_exed.addAndGet(JobCenterManager.sizeOfCommunityExedJob_ProcessingNode(maGateId));
		
		// GENERATED = 0; SUBMITTED = 1; SCHEDULING = 2; PROCESSING = 3; TRANSFERRED = 4;
		// EXECUTED = 5; FAILED = 6; SUSPENDED = 7;
		int[] allStatus = JobCenterManager.sizeOfJob_allStatusByNode(maGateId);
		
		GlobalStorage.total_count_job_generated.addAndGet(allStatus[0]);
		GlobalStorage.total_count_job_submitted.addAndGet(allStatus[1]);
		GlobalStorage.total_count_job_scheduling.addAndGet(allStatus[2]);
		GlobalStorage.total_count_job_processing.addAndGet(allStatus[3]);
		GlobalStorage.total_count_job_transferred.addAndGet(allStatus[4]);
		
		GlobalStorage.total_count_job_executed.addAndGet(allStatus[5]);
		GlobalStorage.total_count_job_suspended.addAndGet(allStatus[7]);
		GlobalStorage.total_count_job_failed.addAndGet(allStatus[6]);
		
		
		if(GlobalStorage.nodeWorkloadReport.containsKey(this.maGateId)) {
			int newExedJobs = GlobalStorage.nodeWorkloadReport.get(this.maGateId).intValue() + allStatus[5];
			GlobalStorage.nodeWorkloadReport.replace(this.maGateId, new Integer(newExedJobs));
		} else {
			GlobalStorage.nodeWorkloadReport.put(this.maGateId, new Integer(allStatus[5]));
		}
		
		int sizeOflocaljobqueue = this.maGate.getMatchMaker().sizeOfLocalJobQueue();
		
		
//		log.debug("GlobalStorage.test_counter_3.incrementAndGet(): " + 
//				GlobalStorage.test_counter_3.get());
//		
//		log.debug("GlobalStorage.test_counter_4.incrementAndGet(): " + 
//				GlobalStorage.test_counter_4.get());
//		
//		log.debug("GlobalStorage.test_counter_5.incrementAndGet(): [CASA.REQUEST] " + 
//				GlobalStorage.test_counter_5.get());
//		
//		log.debug("GlobalStorage.test_counter_6.incrementAndGet(): " + 
//				GlobalStorage.test_counter_6.get());
//		
//		log.debug("GlobalStorage.test_counter_6_2.incrementAndGet(): " + 
//				GlobalStorage.test_counter_6_2.get());
//		
//		log.debug("GlobalStorage.test_counter_6_3.incrementAndGet(): " + 
//				GlobalStorage.test_counter_6_3.get());
		
//		if(sizeOflocaljobqueue == allStatus[2]) {
//			log.info("\n############\n" + this.maGateId + ": " +
//					" Generated: " + allStatus[0] +
//					"; SUBMITTED: " + allStatus[1] +
//					"; SCHEDULING: " + allStatus[2] +
//					"; PROCESSING: " + allStatus[3] +
//					"; TRANSFERRED: " + allStatus[4] +
//					"; EXECUTED: " + allStatus[5] +
//					"; FAILED: " + allStatus[6] +
//					"; SUSPENDED: " + allStatus[7] + 
//					"\n; sizeOflocaljobqueue: " + sizeOflocaljobqueue);
//		} else {
//			log.info("\n*************\n*************\n*************\n" + this.maGateId + ": " +
//					" Generated: " + allStatus[0] +
//					"; SUBMITTED: " + allStatus[1] +
//					"; SCHEDULING: " + allStatus[2] +
//					"; PROCESSING: " + allStatus[3] +
//					"; TRANSFERRED: " + allStatus[4] +
//					"; EXECUTED: " + allStatus[5] +
//					"; FAILED: " + allStatus[6] +
//					"; SUSPENDED: " + allStatus[7] + 
//					"\n; sizeOflocaljobqueue: " + sizeOflocaljobqueue);
//		}
			
	}
	
	/**
	 * Action as LRM: receive jobs returned from resources
	 */
	private void onJobReturnFromResource(Object getData) {
		
		Job receivedJob = (Job) getData;
		
		String jobId = receivedJob.getGlobalJobID();
		
		JobInfo receivedJobInfo = JobCenterManager.getJobInfobyJobId(jobId);

		if (receivedJobInfo != null) {
			
			// ye: mark job already been returned from resource, set job status and delayed time
			receivedJobInfo.setJobStatus(receivedJob.getJobStatus());
			
			receivedJobInfo.setExecutionMaGateId(this.maGate.getMaGateIdentity());
			receivedJobInfo.setTardiness(Math.max(0, receivedJob.getFinishTime() - receivedJob.getDeadline()));
			
			// if local job fails, mark it failed and to be output
			if (receivedJob.getJobStatus() == MaGateMessage.JOB_FAILED) {
				
				JobCenterManager.jobFailed(jobId, this.maGate.getMaGateIdentity(), receivedJobInfo);
			
			} else {
				
				JobCenterManager.jobExecuted(jobId, this.maGate.getMaGateIdentity(), receivedJobInfo);
				
				
//				// Inform the original Node in terms of new possible Critical Friend nodes
//				if(this.maGateId.equals(receivedJobInfo.getOriginalMaGateId())) {
//					GlobalStorage.findMaGateById(receivedJobInfo.getOriginalMaGateId()).getCFCController().cacheCFCNeighbors(this.maGateId);
//				}
			}
			
	    	this.maGate.getMatchMaker().jobFinishedConfirmation(receivedJobInfo);
		} 
	}

	/**
	 * Action as LRM: receive jobs from MatchMaker
	 */
	private void onScheduleMadeByMatchMaker(Object getData) {
		
		JobInfo receivedJobInfo = (JobInfo) getData;
		
		// if jobinfo already scheduled, simply exit
		if(receivedJobInfo == null) {
			return;
		}
		
		Job jobToResource = receivedJobInfo.getJob();
		
		if(jobToResource.isFinished()) {
			this.onJobReturnFromResource(jobToResource);
			return;
		}
		
		// ye: since the job submit requests might not be processed in time, 
		// need to oversight duplicate scheduling response
		if (jobToResource != null) {

			// Sends one job to a grid resource specified in "resourceID"
			if (receivedJobInfo.getTargetResourceID() != -1) {
				
				receivedJobInfo.setQueuingEndTime(MaGateMediator.getSystemTime());
				receivedJobInfo.setQueuingTime(receivedJobInfo.getQueuingEndTime() - receivedJobInfo.getQueuingStartTime());
				
				this.maGate.getMatchMaker().jobAlreadySentToLocalResource();
				
				JobCenterManager.jobProcessing(jobToResource.getGlobalJobID(), this.maGateId, receivedJobInfo);
				
				
				super.send(receivedJobInfo.getTargetResourceID(), 0.0, GridSimTags.GRIDLET_SUBMIT, jobToResource);
				
				// testing code
				GlobalStorage.test_counter_4.incrementAndGet();
				
				
			} else {
				
				try {
					
					log.info("********\n********\n********\n");
					
					// No suitable Resource found for job
					jobToResource.setJobStatus(MaGateMessage.JOB_FAILED);  
					receivedJobInfo.setJobStatus(MaGateMessage.JOB_FAILED);
					
					JobCenterManager.jobSuspended(receivedJobInfo.getGlobalJobID(), this.maGateId, receivedJobInfo);
					
					if(receivedJobInfo.getOriginalMaGateId().equals(this.maGate.getMaGateIdentity())) {
						this.maGate.getMatchMaker().localJobNotSentToResource();
					} else {
						this.maGate.getMatchMaker().communityJobNotSentToResource();
					}
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} else {
			log.warn("unnecessary duplicate sceduled local-job response appeares");
		}
		
	}

	/**
	 * check whether all the local jobs have been prepared from JobSubmitter, 
	 * and set itself as the job owner
	 */
	private void localJobSubmit() {
		
		// ye: wait until the jobs are prepared
		while(!this.maGate.getStorage().isJobPrepared().get()) {
			super.gridSimHold(10.0);
		}
		
		// ye: (1) loop the submitted jobs, (2) set user Id, 
		// (3) submit with different clock, and (4) leave job to waiting list
		Vector<JobInfo> generatedJobs = JobCenterManager.getGeneratedJobs(this.maGate.getMaGateIdentity());

		for(JobInfo jobInfo : generatedJobs) {
			
			Job job = jobInfo.getJob();
			
            // ye: set the original MaGate which released this job
            // Furthermore, for simulated job, set the job responser to a gridsim thread
            jobInfo.setOriginalMaGateId(this.maGateId);
            job.setUserID(this.maGate.getModuleController().getModuleControllerId());
            
            // If CASP is enabled, submitted jobs should be sent to the network;
            // otherwise, jobs should be sent to the MatchMaker (disabled in current version, needed to be extended by users for new self-defined scheduling process)
            if(MaGateParam.CASP_Enabled) {
        		
//            	// IMPORTANT: the job status in the JobCenter Database must be updated now. 
//        		// Namely before simulator's next MaGateMessage.JobToMatchMaker() event!
//        		// Otherwise, the possible delay of the simulator may lead the next CASA reqeust/inform method 
//        		// to consider such job is still not "submitted" yet!
//            	JobCenterManager.jobSubmitted(jobInfo.getGlobalJobID(), maGateId, jobInfo); 
            	
            	// Process the CASP jobs arrival
        		if (jobInfo.getArrivalTime() > MaGateMediator.getSystemTime()){
        			
        			super.send(this.get_id(), jobInfo.getArrivalTime(), MaGateMessage.JobArrival, jobInfo);
                    
                }else{
                	
                	super.send(this.get_id(), 1.0, MaGateMessage.JobArrival, jobInfo);
                	
                }
            	
            } else {
            	log.error("Community-Aware Scheduling Protocol (CASP) is the default scheduling framework of MaGate, " +
            			"users need to put their own code here to define new scheduling process.");;
            	System.exit(0);	
            	// this.processLocalJobArrive(jobInfo);
            }
            
		}
	}
	
	/**
	 * Process the LOCAL jobs arrival
	 * IMPORTANT NOTICE: a little delay (e.g. clock(1.0)) is NECESSARY to make sure current run() iteration to 
	 * terminate normally!
	 * @param jobInfo
	 */
	public void processJobArrival(JobInfo jobInfo) {
		
		jobInfo.setUserID(this.maGate.getModuleController().getModuleControllerId());
		
		// IMPORTANT: the job status in the JobCenter Database must be updated now. 
		// Namely before simulator's next MaGateMessage.JobToMatchMaker() event!
		// Otherwise, the possible delay of the simulator may lead the next CASA request/inform method 
		// to consider such job is still not "scheduled" yet!
		JobCenterManager.jobScheduling(jobInfo.getGlobalJobID(), this.maGateId, jobInfo);
		
		if (jobInfo.getArrivalTime() > MaGateMediator.getSystemTime()){
        	super.send(this.get_id(), jobInfo.getArrivalTime(), MaGateMessage.JobToMatchMaker, jobInfo);
            
        }else{
        	super.send(this.get_id(), 1.0, MaGateMessage.JobToMatchMaker, jobInfo);
        	
        }
	}
	
//	/**
//	 * Process the community jobs arrival
//	 * IMPORTANT NOTICE: a little delay (e.g. clock(1.0)) is NECESSARY to make sure current run() iteration to 
//	 * terminate normally!
//	 * @param jobInfo
//	 */
//	public void processCommunityJobArriveFromCASP(JobInfo jobInfo) {
//
//		jobInfo.setUserID(this.maGate.getModuleController().getModuleControllerId());
//		
//		if (jobInfo.getArrivalTime() > MaGateMediator.getSystemTime()){
//        	
//        	super.send(this.get_id(), jobInfo.getArrivalTime(), MaGateMessage.JobToMatchMaker, jobInfo);
//            
//        }else{
//        	
//        	super.send(this.get_id(), 1.0, MaGateMessage.JobToMatchMaker, jobInfo);
//        	
//        }
//	}
	
	/**
	 * Job is neither allocated on local resource, nor delegated to remote resources
	 * Then the local node's Controller and MatchMake needs to record this event
	 * 
	 * @param jobInfo
	 */
	public void processJobUndelivered(JobInfo jobInfo) {
		
		JobCenterManager.jobSuspended(jobInfo.getGlobalJobID(), this.maGateId, jobInfo);
		
		 this.maGate.getMatchMaker().jobUndelivered(jobInfo);
		 
//         GlobalStorage.recordLiveCommunityStatus();
	}
	
	/**
	 * ModuleController is notified when new decisions have been made from the MatchMaker
	 */
	public void updateFromMatchMaker(JobInfo jobInfo) {
		
		super.send(super.get_id(), 0.0, MaGateMessage.ScheduleMadeByMatchMaker, jobInfo);
		
		// Count this behaviour
		GlobalStorage.count_matchmakerDecision.incrementAndGet();
		
	}
	
	
	public int getModuleControllerId() {
		return super.get_id(); 
	}

	public String getModuleControllerName() {
		return super.get_name();
	}
	
	public void prolongLifecycle() {
		super.send(this.get_id(), MaGateMediator.getSystemTime() + 3600, MaGateMessage.Heartbeat, null);
	}
	
	public void terminateLifecycle() {
		super.send(this.get_id(), 0.0, GridSimTags.END_OF_SIMULATION, null);
	}
}


