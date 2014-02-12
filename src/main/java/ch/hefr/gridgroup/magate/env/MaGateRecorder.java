package ch.hefr.gridgroup.magate.env;

import java.io.BufferedReader;
import java.io.File;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import ch.hefr.gridgroup.magate.MaGateEntity;
import ch.hefr.gridgroup.magate.model.InnerResult;
import ch.hefr.gridgroup.magate.model.MMResult;
import ch.hefr.gridgroup.magate.model.NeighborItem;
import ch.hefr.gridgroup.magate.model.NodeConfidenceCard;
import ch.hefr.gridgroup.magate.model.RemoteNodeReputationItem;
import ch.hefr.gridgroup.magate.model.ResultComparator;
import ch.hefr.gridgroup.magate.storage.GlobalStorage;
import ch.hefr.gridgroup.magate.storage.MaGateDB;
import ch.hefr.gridgroup.magate.storage.MaGateStorage;
import ch.hefr.gridgroup.magate.casa.CASAPolicy;
import ch.hefr.gridgroup.magate.cfm.CFCPolicy;
import ch.hefr.gridgroup.magate.env.MaGateParam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MaGateRecorder {

	private static Log log = LogFactory.getLog(MaGateRecorder.class);
	
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> nodeInteractoins = null;
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> nodeNetwork = null;
	
	/**
	 * Clean the output location for (re)generating logs and plots
	 */
	public static void filesystemClean() {
		
		// Clean-up records from last time
		File mainOutput = new File(MaGateProfile.outputLocation());
		MaGateToolkit.deleteDir(mainOutput);
		
		// Create a new output directory
		mainOutput.mkdirs();
		
		File outputResult = new File(MaGateProfile.outputLocation(), MaGateProfile.resultLocation());
		File outputPlotResult = new File(MaGateProfile.outputLocation(), MaGateProfile.plotResultLocation());
		File outputChartResult = new File(MaGateProfile.chartLocation());
		
		Vector<File> candidateDir = new Vector<File>();
		candidateDir.add(outputResult);
		candidateDir.add(outputPlotResult);
		candidateDir.add(outputChartResult);
		
		for (File outputDir : candidateDir)  {
			
			boolean outputDirExist = outputDir.exists();
			
			// Only need to create output dir if not exists
			if (!outputDirExist) {
				outputDir.mkdirs();
				
			} 
		}
	}
	
	
	/** 
	 * Print results after each scenario for prospective of entire community
	 */
	public static void printCommunityStatus_perScenario(LinkedList<MaGateStorage> resultPerScenario, 
			String scenarioId) {
		
		MMResult mmResult = null;

		double total_availablePE   = 0;
		// nodeWeightForGrid = number of PEs of this node / number of PEs of the grid
		double nodeWeightForGrid   = 0;   
		
//		double total_exedJobs = 0;
//		double total_suspendedJobs = 0;
//		double total_failedJobs = 0;
		
		double total_locallyExedJobs = 0;
		double total_remotelyExedJobs = 0;
		double total_locallyReceivedJobs = 0;
		double total_remotelyReceivedJobs = 0;
		
//		int localArchivedJob   = 0;
//		int localExedJob       = 0;
//		int localUnsuitedJob   = 0;
//		int localProcessingJob = 0;
//		
//		int outputArchivedJob       = 0;
//		int outputSentJob           = 0;
//		int outputSentFailedJob     = 0;
//		int outputReturnedExedJob   = 0;
//		int outputReturnedFailedJob = 0;
//		
//		int inputArchivedJob            = 0;
//		int inputDeliveredExedJob       = 0;
//		int inputDeviveredExedFailedJob = 0;
//		int inputProcessingJob          = 0;
		
		double total_nondelayedLocalJob     = 0;
		double total_delayedLocalJob        = 0;
		double total_nondelayedCommunityJob = 0;
		double total_delayedCommunityJob    = 0;
		
		double total_jobWeight = 0;
//		double total_resourceWeight = 0;
		
		double total_jobResponseTime = 0.0;
		double total_weightedJobResponseTime = 0.0;
		
		double total_jobWaitingTime = 0.0;
		
		double total_jobCPUTime = 0.0;
		double total_weightedJobCPUTime = 0.0;
		
		double total_jobSlowdown = 0.0;    
		double total_weightedJobSlowdown = 0.0;  
		
//		double total_jobResponseTime_overCommunity = 0.0;
//		double total_weightedJobResponseTime_overCommunity = 0.0;
		
//		double total_jobSlowdown_community = 0.0;    
//		double total_weightedJobSlowdown_community = 0.0;  

//      " \n Total tardiness time of processed Local Jobs: " + mmResult.getTotalJobTardiness() + " s." +
//      " | avg. tardiness time of processed Local Jobs: " + mmResult.getTotalJobTardiness() / mmResult.getNumOfReceivedLocalJobs() +
		double total_jobTardiness = 0.0;  // Local tardiness? 
		
		double total_schedulingTime = 0.0;
		
		// Overall job usage: jobusage = SUM(jobWeight), wherein jobWeight = job.getNumPE() * job.getActualCPUTime();
		double total_jobUsage = 0.0;
		double total_resourceUsage = 0.0;
//		double total_resourceUtilization = 0.0;
		double total_weightedResourceUtilization = 0.0;
		
		double total_resourceUptime = 0.0;
		
		double total_networkDegree = 0.0;
		double total_cfcDegree = 0.0;
		
		int total_numOfInteractedNode   = 0;
		double total_rateOfCFNode       = 0;
		double total_avgRemoteNodeScore = 0.0;
		
		Format formatter = new SimpleDateFormat("yyyy.MM.dd 'at' HH.mm.ss");
		
		int overallCounter = MaGateParam.numberOfTotalNode * MaGateParam.countOfExperiment;
		
		
		/****************************************************
		 * LOOP 1: get all PEs of the community
		 ****************************************************/
		// Get total number of PEs of the entire grid
		double totalPEofGrid = 0;
		for(MaGateStorage perMaGateStorage : resultPerScenario) {
			totalPEofGrid += perMaGateStorage.getTotalNumOfPEs().get();
		}
		totalPEofGrid /= MaGateParam.countOfExperiment;
		
		/****************************************************
		 * LOOP 2: Logging cross-node interactions  
		 ****************************************************/
		
		nodeTopologyAnalysis(resultPerScenario);
		nodeTrustAnalysis(resultPerScenario);
		
		/***************************************************************
		 * LOOP 3: Collecting data from multi- experiment iterations
		 ***************************************************************/
		for(MaGateStorage perMaGateStorage : resultPerScenario) {
			
			int availablePE = 0;
			
			double numOf_cachedNetowrkNeighbors = 0;
			double numOf_cachedCFCNeighbors = 0;
			
//			String maGateId = perMaGateStorage.getMaGateIdentity();
			mmResult = perMaGateStorage.getMMResult();
			
			availablePE = perMaGateStorage.getTotalNumOfPEs().get();
			total_availablePE += availablePE;
			nodeWeightForGrid = (double) availablePE / totalPEofGrid;
			
			// record exed local/communtiy jobs from MatchMaker
			total_locallyExedJobs += mmResult.getNumOfSuccessProcessedLocalJob();
			total_remotelyExedJobs += mmResult.getNumOfSuccessProcessedCommunityJob();
			total_locallyReceivedJobs += mmResult.getNumOfReceivedLocalJobs();
			total_remotelyReceivedJobs += mmResult.getNumOfReceivedCommunityJobs();
			
			// delayed(non-delayed) local(community) job
			total_nondelayedLocalJob += mmResult.getTotalNumOfNondelayedLocalJobs();
			total_delayedLocalJob += mmResult.getTotalNumOfDelayedLocalJobs();
			
			total_nondelayedCommunityJob += mmResult.getTotalNumOfNondelayedCommunityJobs();
			total_delayedCommunityJob += mmResult.getTotalNumOfDelayedCommunityJobs();
			
			// each job's weight = job.getNumPE() * job.getActualCPUTime()
			total_jobWeight += mmResult.getTotalJobWeight();
			
			// job response time = job.getFinishTime() - job.getJobStartTime()
			// job weighted response time = jobWeight * jobResponse
			total_jobResponseTime += mmResult.getTotalJobResponseTime();
			total_weightedJobResponseTime += mmResult.getTotalWeightedResponseTime();
			
			total_jobWaitingTime += mmResult.getTotalJobWaitingTime();
			
			// job actual CPU time
			total_jobCPUTime += mmResult.getTotalJobCPUTime();
			total_weightedJobCPUTime += mmResult.getTotalWeightedJobCPUTTime();
			
			// job slow down = jobResponseTime / job.getActualCPUTime()
			// job weighted slow down = jobWeight * job slow down
			total_jobSlowdown += mmResult.getTotalSlowdown();
			total_weightedJobSlowdown += mmResult.getTotalWeightedSlowdown();
			
			// total scheduling time used on each node
			total_schedulingTime += mmResult.getTotalSchedulingTime();
			
			// total jobUsage, resourceUsage, resouceUtilization, uptime
			total_jobUsage += MaGateToolkit.convertAtomicLongToDouble(perMaGateStorage.getJobUsage());
			total_resourceUsage += MaGateToolkit.convertAtomicLongToDouble(perMaGateStorage.getResUsage());
			
			total_weightedResourceUtilization += 
				nodeWeightForGrid * MaGateToolkit.convertAtomicLongToDouble(perMaGateStorage.getResourceEfficency());
			
			total_resourceUptime += mmResult.getResourceUptime();
			
			// Network coverage
			numOf_cachedNetowrkNeighbors = perMaGateStorage.size_networkNeighbors();
			numOf_cachedCFCNeighbors = perMaGateStorage.size_cfcNeighbors();
			total_networkDegree += numOf_cachedNetowrkNeighbors / MaGateParam.numberOfTotalNode;
			total_cfcDegree += numOf_cachedCFCNeighbors / MaGateParam.numberOfTotalNode;
			
			// CFM related values
			double currentNodenumOfCFNode        = 0.0;
			double currentNodeallRemoteNodeScore = 0.0;
			
			ConcurrentHashMap<String, RemoteNodeReputationItem> currentRemoteNodeReputation = perMaGateStorage.get_remoteNodeReputationList();
			for(Map.Entry<String, RemoteNodeReputationItem> entry: currentRemoteNodeReputation.entrySet()) {
				
				double remoteNodeReputation = entry.getValue().getRemodeNodeReputation();
				currentNodeallRemoteNodeScore += remoteNodeReputation;
				
				if(remoteNodeReputation >= 0.6) {
					currentNodenumOfCFNode++;
				}
			}
			
			// number of all interacted remote nodes of current hosting node
			int currentNodenumOfRemoteNode = currentRemoteNodeReputation.size();
			
			// update the collector of rate of CF nodes
			total_rateOfCFNode        += currentNodenumOfCFNode / currentNodenumOfRemoteNode;
			total_numOfInteractedNode += currentNodenumOfRemoteNode;
			total_avgRemoteNodeScore  += currentNodeallRemoteNodeScore / currentNodenumOfRemoteNode;
			
		} // exit loop of storages
		
		
		/*******************************************************
		 * Data from multi-experiment iterations are collected; 
		 * now ready to prepare statistic data for each scenario
		 *******************************************************/
		
//		double perSce_exedJobs = total_exedJobs / MaGateParam.countOfExperiment;  
//		double perSce_suspendedJobs = total_suspendedJobs / MaGateParam.countOfExperiment;
//		double perSce_failedJobs = total_failedJobs / MaGateParam.countOfExperiment;
		
		double perSce_totalJobs_storageCount = GlobalStorage.total_count_job_db_stored.doubleValue() / MaGateParam.countOfExperiment;
		double perSce_exedLocalJobs_storageCount = GlobalStorage.total_count_job_local_exed.doubleValue() / MaGateParam.countOfExperiment;
		double perSce_exedCommunityJobs_storageCount = GlobalStorage.total_count_job_community_exed.doubleValue() / MaGateParam.countOfExperiment;
		double perSce_exedJobs_storageCount = GlobalStorage.total_count_job_executed.doubleValue() / MaGateParam.countOfExperiment;
		double perSce_suspendedJobs_storageCount = GlobalStorage.total_count_job_suspended.doubleValue() / MaGateParam.countOfExperiment;
		double perSce_failedJobs_storageCount = GlobalStorage.total_count_job_failed.doubleValue() / MaGateParam.countOfExperiment;
		
		double perSce_availablePE   = total_availablePE / MaGateParam.countOfExperiment;
		
		double perSce_exedLocalJobs_objectCount = total_locallyExedJobs / MaGateParam.countOfExperiment;
		double perSce_exedCommunityJobs_objectCount = total_remotelyExedJobs / MaGateParam.countOfExperiment;
		double perSce_receivedLocalJobs_objectCount = total_locallyReceivedJobs / MaGateParam.countOfExperiment;
		double perSce_receivedCommunityJobs_objectCount = total_remotelyReceivedJobs / MaGateParam.countOfExperiment;
		double perSce_receivedJobs_objectCount = 
			perSce_receivedLocalJobs_objectCount + perSce_receivedCommunityJobs_objectCount;
		
		double perSce_nondelayedLocalJob     = total_nondelayedLocalJob / MaGateParam.countOfExperiment;
		double perSce_delayedLocalJob        = total_delayedLocalJob / MaGateParam.countOfExperiment;
		double perSce_nondelayedCommunityJob = total_nondelayedCommunityJob / MaGateParam.countOfExperiment;
		double perSce_delayedCommunityJob    = total_delayedCommunityJob / MaGateParam.countOfExperiment;
		
		double perSce_jobWeight = total_jobWeight / MaGateParam.countOfExperiment;
//		double perSce_resourceWeight = total_resourceWeight / MaGateParam.countOfExperiment;
		
		double perSce_jobResponseTime = total_jobResponseTime / MaGateParam.countOfExperiment;
		double perSce_jobWaitingTime = total_jobWaitingTime / MaGateParam.countOfExperiment; 
		
		double perSce_weightedJobResponseTime = total_weightedJobResponseTime / MaGateParam.countOfExperiment;
		
		double perSce_jobCPUTime = total_jobCPUTime / MaGateParam.countOfExperiment;
		double perSce_weightedJobCPUTime = total_weightedJobCPUTime / MaGateParam.countOfExperiment;
		
		double perSce_jobSlowdown = total_jobSlowdown / MaGateParam.countOfExperiment;    
		double perSce_weightedJobSlowdown = total_weightedJobSlowdown / MaGateParam.countOfExperiment; 
		
			
		double perSce_schedulingTime = total_schedulingTime / MaGateParam.countOfExperiment;
		
		double perSce_jobUsage = total_jobUsage / MaGateParam.countOfExperiment;
		double perSce_resourceUsage = total_resourceUsage / MaGateParam.countOfExperiment;
		double perSce_weightedResourceUtilization = total_weightedResourceUtilization / MaGateParam.countOfExperiment;
		
		double perSce_resourceUptime = total_resourceUptime / MaGateParam.countOfExperiment;
	
		// Network Coverage
		double perSce_networkCoverage = total_networkDegree / MaGateParam.countOfExperiment;
		double perSce_cfcCoverage = total_cfcDegree / MaGateParam.countOfExperiment;
		
		// CFM related values
		double perSce_rateOfCFNode        = total_rateOfCFNode / MaGateParam.countOfExperiment;
		double perSce_numOfInteractedNode = total_numOfInteractedNode / MaGateParam.countOfExperiment;
		double perSce_avgRemoteNodeScore  = total_avgRemoteNodeScore / MaGateParam.countOfExperiment;
		
		/***********************************************
		 * Printing statistic data for each scenario
		 ***********************************************/
		
		String txtContent = "\n\n===================== SCENARIO COMPLETE! =============================";
		
		txtContent += "\n ScenarioId: " + MaGateParam.currentScenarioId + " | Number of experiments: " + MaGateParam.countOfExperiment +  
			"; Real-SysStartTime: " + formatter.format(GlobalStorage.systemStartTime) + 
			"; Real-SystEndTime: " + formatter.format(new Date()) + 
			" \n\n Number of MaGate nodes within the community: " + MaGateParam.numberOfTotalNode + 
			" ; | Total available PEs: " + perSce_availablePE;
		
		
		// JSC calculation by reading dbStorage
		txtContent += " \n\n JSC [by storage data]:" +
			"\n Percentage of executed jobs (local/community) " + 
			((perSce_exedJobs_storageCount / perSce_totalJobs_storageCount) * 100) + 
			"% (" + ((perSce_exedLocalJobs_storageCount / perSce_totalJobs_storageCount) * 100) + 
			"% / " + ((perSce_exedCommunityJobs_storageCount / perSce_totalJobs_storageCount) * 100) + 
			"%) " + 
			" \n Num of jobs in total: " + perSce_totalJobs_storageCount + 
			"\n Number of executed jobs (local/community) " + 
			perSce_exedJobs_storageCount + 
			" (" + perSce_exedLocalJobs_storageCount  + 
			" / " + perSce_exedCommunityJobs_storageCount + 
			") " + 
			" \n Num of jobs (executed/suspended/failed jobs): " + 
			"" + perSce_exedJobs_storageCount + 
			"/" + perSce_suspendedJobs_storageCount + 
			"/" + perSce_failedJobs_storageCount
			;
		
		// JSC calculation by accumulating each objects from the loop
		txtContent += " \n\n JSC [by object accumulated data]: " +
			"\n Percentage of executed jobs (local/community) " + 
			(((perSce_exedLocalJobs_objectCount + perSce_exedCommunityJobs_objectCount) / 
					perSce_receivedJobs_objectCount) * 100) + 
			"% (" + ((perSce_exedLocalJobs_objectCount / perSce_receivedJobs_objectCount) * 100) + 
			"% / " + ((perSce_exedCommunityJobs_objectCount / perSce_receivedJobs_objectCount) * 100) + 
			"%) " +
			" \n Num of jobs in total: " + perSce_receivedJobs_objectCount + 
			"\n Number of executed jobs (local/community) " + 
			(perSce_exedLocalJobs_objectCount + perSce_exedCommunityJobs_objectCount)  + 
			" (" + perSce_exedLocalJobs_objectCount + 
			" / " + perSce_exedCommunityJobs_objectCount + 
			") " + 
			" \n LocalExedJob (Delayed/Non-delayed Local jobs): " + 
			perSce_exedLocalJobs_objectCount + 
			"(" + perSce_delayedLocalJob + "|" + perSce_nondelayedLocalJob + ")" +
			"\n CommunityExedJob (Delayed/Non-delayed Community jobs): " + 
			perSce_exedCommunityJobs_objectCount + 
			"(" + perSce_delayedCommunityJob + "|" + perSce_nondelayedCommunityJob + ")";
	
		
		// job load, life duration
		txtContent += "\n\n" + " GWA trace load: " + MaGateParam.gwaLoadinPercentage + "%; ";
	
		// community efficiency; avg node efficiency; avg weighted node efficiency
		txtContent += "\n\n" +
			" Community Resource Utilization:\n [by jobUsage/resourceUsage]: " + ((perSce_jobUsage / perSce_resourceUsage) * 100) + "% |" +
			" \n [by adding up each node's weighted utilization]: " + (perSce_weightedResourceUtilization * 100) + "%";
		
		// avg resouce make span
		txtContent += "\n\n" + 
			" Community Uptime: " + (perSce_resourceUptime / MaGateParam.numberOfTotalNode) + " s.";
		
		// CASP
		if(MaGateParam.CASP_Enabled) {
			txtContent += " \n\n CASP-Enabled";
		} else {
			txtContent += " \n\n CASP-Disabled";
		}
		
		// Isolated VO
		if(MaGateParam.isolatedVO) {
			txtContent += " VO-Boundary-Enabled";
		} else {
			txtContent += " VO-Boundary-Disabled";
		}
		
		// Agreement specification
		if(MaGateParam.Agreement_Enabled) {
			txtContent += " Agreement-Enabled";
		} else {
			txtContent += " Agreement-Disabled";
		}
		
		
		// CASA Variables 
		txtContent += " \n\n CASA Variables: ";
		if(MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Independent) {
			txtContent += "[Scheme]Independent  ";
		} else if (MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Centralized) {
			txtContent += "[Scheme]Centralized  ";
		} else if (MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Decentralized) {
			
			if(MaGateParam.systemIS == MaGateMessage.systemIS_Global) {
				txtContent += "[Scheme]Decentralized-[IS]Global  ";
			} else if(MaGateParam.systemIS == MaGateMessage.systemIS_Partial_ACO) {
				txtContent += "[Scheme]Decentralized-[IS]Partial-ACO  ";
			} else if(MaGateParam.systemIS == MaGateMessage.systemIS_Partial_SIM) {
				txtContent += "[Scheme]Decentralized-[IS]Partial-SIM  ";
			}
			
			if(MaGateParam.jobDistMode == MaGateMessage.JobDistMode_CustomizedDist) {
				txtContent += "[jobDistMode]CustomizedDist  ";
			} else if(MaGateParam.jobDistMode == MaGateMessage.JobDistMode_FairDist) {
				txtContent += "[jobDistMode]FairDist  ";
			}
		}
		
		txtContent += "[Rescheduling on Avg QueuingTime]: " + MaGateParam.systemReschedulingOnAvgQueuingTime + "";
		txtContent += "  [Coefficient of Rescheduling]: " + MaGateParam.systemReschedulingCoefficient + "";
		
		// CFC Policy
//		txtContent += " \n CFC_Policy: ";
//		Enumeration<String> currentCFCPolicies = MaGateParam.CFC_POLICY.elements();
//		while(currentCFCPolicies.hasMoreElements()) {
//			String currentPolicy = currentCFCPolicies.nextElement();
//			txtContent += currentPolicy + "; ";
//		}
		
		txtContent += " \n\n CFC_Policy: ";
		Iterator<Map.Entry<String, String>> iter2 = MaGateParam.CFM_POLICY.entrySet().iterator();
		while (iter2.hasNext()) {
			Map.Entry<String, String> entry2 = iter2.next();
			String currentPolicy = entry2.getValue();
			txtContent += currentPolicy + "; ";
		}
		
		
		DecimalFormat twoDFormat = new DecimalFormat("#.##");

		txtContent += " \n [FailScore]" + Double.valueOf(MaGateParam.cfmDelegationFailScore);
		txtContent += " [D-Due]" + Double.valueOf(twoDFormat.format((MaGateParam.cfmJobReturnDeadline / 3600)));
		txtContent += " [RecentTimeBarrier]" + Double.valueOf(twoDFormat.format(MaGateParam.cfmRecentTimeBarrier / 3600));
		txtContent += " [L-RepWeight]" + Double.valueOf(twoDFormat.format(MaGateParam.cfmLocalReputationWeight));
		txtContent += " [ResWeight]" + Double.valueOf(twoDFormat.format(MaGateParam.cfmResCapabilityWeight));
		
		// Network message overhead
		txtContent += "\n\n" + 
			" Adopted Information System: SSL-CommunitySearch (query/feedback/update): " + 
			(GlobalStorage.total_count_communitySearch / MaGateParam.countOfExperiment) + 
			"/" + (GlobalStorage.total_count_communitySearchFeedback / MaGateParam.countOfExperiment) + 
			"/" + (GlobalStorage.total_count_updateNeighborhood / MaGateParam.countOfExperiment); 
		
		
		// All local matchmaker policies
		txtContent += " \n\n All local Matchmaker Policies: " + MaGateParam.getAvailableMatchMakerPolicies();
		Object[] mmPolicyKey = MaGateParam.availableMatchmakerPolicy.keySet().toArray();
		String mmPolicies = "";
		for(int k = 0; k < mmPolicyKey.length; k++) {
			String tempMMPolicy = (String) mmPolicyKey[k];
			mmPolicies += tempMMPolicy.charAt(0);
		}
		
		/*******************
		 * Test code block 
		 *******************/
//		// Job storage statistic
//		outputText += "\n\n <Test data section below:> \n" +
//			" [PerScenario(logged-event)]Num.Generated jobs: " + (GlobalStorage.total_count_job_generated.doubleValue() / MaGateParam.countOfExperiment) + 
//			" | Num.Submitted jobs: " + (GlobalStorage.total_count_job_submitted.doubleValue() / MaGateParam.countOfExperiment) + 
//			" | Num.Scheduling jobs: " + (GlobalStorage.total_count_job_scheduling.doubleValue() / MaGateParam.countOfExperiment) + 
//			"\n [PerScenario(logged-event)]Num.Processing jobs: " + (GlobalStorage.total_count_job_processing.doubleValue() / MaGateParam.countOfExperiment) + 
//			" | Num.Transferring jobs: " + (GlobalStorage.total_count_job_transferred.doubleValue() / MaGateParam.countOfExperiment) + 
//			" | Num.Executed jobs: " + (GlobalStorage.total_count_job_executed.doubleValue() / MaGateParam.countOfExperiment) + 
//			"\n [PerScenario(logged-event)]Num.Suspended jobs: " + (GlobalStorage.total_count_job_suspended.doubleValue() / MaGateParam.countOfExperiment) + 
//			" | Num.Failed jobs: " + (GlobalStorage.total_count_job_failed.doubleValue() / MaGateParam.countOfExperiment);
		
		
		// CASP message statistic
		txtContent += "\n" + " INFORM-msg realTime Frequency: " + MaGateParam.INFORM_realTime_frequency + " ms. " +
			" INFORM-msg simTime Frequency: " + MaGateParam.INFORM_simTime_frequency + " s. " +
			"\n [In total]REQUEST-msg: " + (GlobalStorage.total_count_msgREQUEST / MaGateParam.countOfExperiment) + 
			" ; ACCEPT(for REQUEST)-msg: " + (GlobalStorage.total_count_msgACCEPT_REQUEST / MaGateParam.countOfExperiment) +
			" ; ASSIGN(for REQUEST)-msg: " + (GlobalStorage.total_count_msgASSIGN_REQUEST / MaGateParam.countOfExperiment) + 
			"\n [In total]INFORM-msg: " + (GlobalStorage.total_count_msgINFORM / MaGateParam.countOfExperiment) + 
			" ; ACCEPT(for INFORM)-msg: " + (GlobalStorage.total_count_msgACCEPT_INFORM / MaGateParam.countOfExperiment) +
			" ; ASSIGN(for INFORM)-msg: " + (GlobalStorage.total_count_msgASSIGN_INFORM / MaGateParam.countOfExperiment);
		
		// CFC message
		txtContent += "\n\n" +
			" [In total]CFC-FreeNodeAdvertisment: " + (GlobalStorage.total_count_CFCFreePower / MaGateParam.countOfExperiment) + 
			"; CFC-NonFreeNodeInformation: " + (GlobalStorage.total_count_CFCNonfreepower / MaGateParam.countOfExperiment);
		
		// Errors
		txtContent += "\n\n [CASP Exceptions]: \n" +
			" [In total]Un-matched REQUEST Message: " + (GlobalStorage.total_count_unmatchedREQUEST / MaGateParam.countOfExperiment) +  
			"\n Un-matched INFORM Message: " + (GlobalStorage.total_count_unmatchedINFORM / MaGateParam.countOfExperiment) + 
			"\n Unexpected CASP behaviors: " + (GlobalStorage.total_count_errorCASP / MaGateParam.countOfExperiment);
		
		
		// Job total weight; Job avg- weight
		/***************************************************************************
		 * avg-weight value = (w1v1 + w2v2 + ... + w100v100) / (w1 + w2 + ... w100)
		 ***************************************************************************/
		txtContent += "\n\n" +
			" TotalJobWeight(NumOfRequestedPE * ActualCPUTime): " + perSce_jobWeight + 
			"\n TotalResourceWeight(NumOfAvailablePE): " + perSce_availablePE;
		
		
		// Avg job response time; Weighted Avg job response time
		txtContent += "\n\n" +
			" [Total]JobResponseTime: " + perSce_jobResponseTime + 
			" [Total]JobWaitingTime: " + perSce_jobWaitingTime + 
			" [Weighted Total]JobResponseTime: " + perSce_weightedJobResponseTime +
			"\n [Average]JobResponseTime: " + (perSce_jobResponseTime / perSce_exedJobs_storageCount) + " s.|" +
			"\n [Average]JobWaitingTime: " + (perSce_jobWaitingTime / perSce_exedJobs_storageCount) + " s.|" +
			" [Weighted Average]JobResponseTime: " + (perSce_weightedJobResponseTime / perSce_jobWeight) + " s.";
		
		
		// Avg job actual CPU time; weighted Avg job actual CPU time
		txtContent += "\n\n" +
			" [Total]JobActualCPUTime: " + perSce_jobCPUTime + 
			" [Weighted Total]JobActualCPUTime: " + perSce_weightedJobCPUTime +
			"\n [Average]JobActualCPUTime: " + (perSce_jobCPUTime / perSce_exedJobs_storageCount) + " s.|" +
			" [Weighted Average]JobActualCPUTime: " + (perSce_weightedJobCPUTime / perSce_jobWeight) + " s.";
	
		
		// Avg job slowdown; Weighted Avg job slowdown
		txtContent += "\n\n" +
			" [Total]JobSlowdown: " + perSce_jobSlowdown + 
			" [Weighted Total]JobSlowdown: " + perSce_weightedJobSlowdown +
			"\n [Average]JobSlowdown: " + (perSce_jobSlowdown / perSce_exedJobs_storageCount) + "|" +
			" [Weighted Average]JobSlowdown: " + (perSce_weightedJobSlowdown / perSce_jobWeight);
		
		// scheduling time; scheduling counter
		txtContent += "\n\n" +
			" [Per-Job]Scheduling-Time: " + (perSce_schedulingTime / perSce_exedJobs_storageCount) + " ms.|" + 
			" [Per-Node]Scheduling-Decisions: " + (GlobalStorage.total_count_matchmakerDecision / overallCounter);
		
        
		// Network Coverage
		txtContent += "\n\n" + 
			" [Per-Node]Network(Neighbors) Degree: " + ((perSce_networkCoverage / MaGateParam.numberOfTotalNode) * 100) + "%" + 
			"  [Per-Node]Critical Friend Degree (CFC): " + ((perSce_cfcCoverage /  MaGateParam.numberOfTotalNode) * 100) + "%";
		
		// test counter
//		outputText += "\n\n" +
//			" [Total]Platform Test Counter 1: " + GlobalStorage.test_counter_1.doubleValue() / MaGateParam.countOfExperiment + 
//			"; Test Counter 2: " + GlobalStorage.test_counter_2.doubleValue() / MaGateParam.countOfExperiment;
		
		log.info(txtContent);
		MaGateToolkit.writeResult(GlobalStorage.systemOutputTXTFile, txtContent);
		
		
		/*******************
		 * Print results
		 *******************/
    	
//		String caspPolicyText = "-";
//		if(MaGateParam.CASP_POLICY == CASPPolicy.CASP_NONTHRESHOLD) {
//			caspPolicyText = "CASP_NONTHRESHOLD";
//		} else if (MaGateParam.CASP_POLICY == CASPPolicy.CASP_STRICT){
//			caspPolicyText = "CASP_STRICT";
//		} else if (MaGateParam.CASP_POLICY == CASPPolicy.CASP_TOLERANT) {
//			caspPolicyText = "CASP_TOLERANT";
//		}
		
//		String cfcPolicyText = "";
//		Enumeration<String> currentCFCPolicies2 = MaGateParam.CFC_POLICY.elements();
//		while(currentCFCPolicies2.hasMoreElements()) {
//			String currentPolicy = currentCFCPolicies2.nextElement().trim();
//			cfcPolicyText += currentPolicy + "; ";
//		}
		
		
		
		
		
		String workloadName = MaGateParam.gwaLoadName + "-" + MaGateParam.gwaLoadinPercentage;
		
		String casaVariables = "";
		
		casaVariables += "[MM]" + mmPolicies + "-";
		// CASA Variables 
		if(MaGateParam.dynamicEnabled) {
			casaVariables += "[DySchd]true" + "-";
		} else {
			casaVariables += "[DySchd]false" + "-";
		}
		
		if(MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Independent) {
			casaVariables += "[Scheme]Ind-";
		} else if (MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Centralized) {
			casaVariables += "[Scheme]Cen-";
		} else if (MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Decentralized) {
			
			if(MaGateParam.systemIS == MaGateMessage.systemIS_Global) {
				casaVariables += "[Scheme]Dec-[IS]Glo-";
			} else if(MaGateParam.systemIS == MaGateMessage.systemIS_Partial_ACO) {
				casaVariables += "[Scheme]Dec-[IS]ACO-";
			} else if(MaGateParam.systemIS == MaGateMessage.systemIS_Partial_SIM) {
				casaVariables += "[Scheme]Dec-[IS]SIM-";
			}
			
			if(MaGateParam.jobDistMode == MaGateMessage.JobDistMode_CustomizedDist) {
				casaVariables += "[jDist]Cust-";
			} else if(MaGateParam.jobDistMode == MaGateMessage.JobDistMode_FairDist) {
				casaVariables += "[jDist]Fair-";
			}
		}
		
		casaVariables += "[ResAvgQT]" + MaGateParam.systemReschedulingOnAvgQueuingTime + "";
		casaVariables += "[C:Res]" + MaGateParam.systemReschedulingCoefficient + "";
		
		String cfcPolicyText = "";
		Iterator<Map.Entry<String, String>> iter3 = MaGateParam.CFM_POLICY.entrySet().iterator();
		while (iter3.hasNext()) {
			Map.Entry<String, String> entry3 = iter3.next();
			String currentPolicy = entry3.getValue().trim();
			cfcPolicyText += currentPolicy + "; ";
		}
		
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		// Double.valueOf(twoDForm.format(double))

		cfcPolicyText += "[FailScore]" + Double.valueOf(MaGateParam.cfmDelegationFailScore);
		cfcPolicyText += "[D-Due]" + Double.valueOf(twoDForm.format((MaGateParam.cfmJobReturnDeadline / 3600)));
		cfcPolicyText += "[RecentTimeBarrier]" + Double.valueOf(twoDForm.format(MaGateParam.cfmRecentTimeBarrier / 3600));
		cfcPolicyText += "[L-RepWeight]" + Double.valueOf(twoDForm.format(MaGateParam.cfmLocalReputationWeight));
		cfcPolicyText += "[ResWeight]" + Double.valueOf(twoDForm.format(MaGateParam.cfmResCapabilityWeight));
		
			
    	String xslContent = workloadName + ", " + casaVariables + ", " + cfcPolicyText + ", " + 
    		// JSC, CU, A-WJS, A-JS, A-JWT
    		((perSce_exedJobs_storageCount / perSce_totalJobs_storageCount) * 100) + ", " + 
    		((perSce_jobUsage / perSce_resourceUsage) * 100)  + ", " + 
    		(perSce_weightedJobSlowdown / perSce_jobWeight) + ", " + 
    		(perSce_jobSlowdown / perSce_exedJobs_storageCount) + ", " + 
    		(perSce_jobWaitingTime / perSce_exedJobs_storageCount) + ", " + 
    		// REQUEST, ACCEPT-R, ASSIGN-R
    		(GlobalStorage.total_count_msgREQUEST / (MaGateParam.countOfExperiment * perSce_totalJobs_storageCount)) + ", " + 
    		(GlobalStorage.total_count_msgACCEPT_REQUEST / (MaGateParam.countOfExperiment * perSce_totalJobs_storageCount)) + ", " + 
    		(GlobalStorage.total_count_msgASSIGN_REQUEST / (MaGateParam.countOfExperiment * perSce_totalJobs_storageCount))+ ", " + 
    		// INFORM, INFORM-R, INFORM-R
    		(GlobalStorage.total_count_msgINFORM / (MaGateParam.countOfExperiment * perSce_totalJobs_storageCount)) + ", " + 
    		(GlobalStorage.total_count_msgACCEPT_INFORM / (MaGateParam.countOfExperiment * perSce_totalJobs_storageCount))+ ", " + 
    		(GlobalStorage.total_count_msgASSIGN_INFORM / (MaGateParam.countOfExperiment * perSce_totalJobs_storageCount)) + ", " + 
    		// unmatched REQUEST, unmatched INFORM
    		(GlobalStorage.total_count_unmatchedREQUEST / (MaGateParam.countOfExperiment * perSce_totalJobs_storageCount)) + ", " + 
    		(GlobalStorage.total_count_unmatchedINFORM / (MaGateParam.countOfExperiment * perSce_totalJobs_storageCount)) + ", " + 
    		// total nodes, total PEs, total jobs
    		MaGateParam.numberOfTotalNode + ", " + perSce_availablePE + ", " + perSce_totalJobs_storageCount + ", " + 
    		// L-JSC, R-JSC
    		((perSce_exedLocalJobs_storageCount / perSce_totalJobs_storageCount) * 100) + ", " + 
    		((perSce_exedCommunityJobs_storageCount / perSce_totalJobs_storageCount) * 100) + ", " + 
    		// community utilization, community uptime
    		(perSce_resourceUptime / MaGateParam.numberOfTotalNode) + ", " + 
    		// A-JRT, A-WJRT, A-JS, WA-JS
    		(perSce_jobResponseTime / perSce_exedJobs_storageCount) + ", " + 
    		(perSce_weightedJobResponseTime / perSce_jobWeight)  + ", " + 
    		// network degree, CFC degree
    		((perSce_networkCoverage / MaGateParam.numberOfTotalNode) * 100) + ", " + 
    		((perSce_cfcCoverage /  MaGateParam.numberOfTotalNode) * 100)  + ", " + 
    		// IS update frequency, CASP INFORM frequency (.ms)
    		(GlobalStorage.total_count_updateNeighborhood / MaGateParam.countOfExperiment) + ", " + 
    		MaGateParam.INFORM_realTime_frequency + ", " + 
    		// CF free-resource notification, CF non-free-resource notification
    		(GlobalStorage.total_count_CFCFreePower / MaGateParam.countOfExperiment) + ", " + 
    		(GlobalStorage.total_count_CFCNonfreepower / MaGateParam.countOfExperiment) + ", " + 
    		// avg scheduling time per job, avg scheduling decisions per node
    		(perSce_schedulingTime / perSce_exedJobs_storageCount) + ", " + 
    		(GlobalStorage.total_count_matchmakerDecision / overallCounter) + ", " + 
    		// CFRate(%), num-Interacted-Node, avgRemoteNodeScore
    		((perSce_rateOfCFNode / MaGateParam.numberOfTotalNode) * 100) + ", " + 
    		(perSce_numOfInteractedNode / MaGateParam.numberOfTotalNode) + ", " + 
    		(perSce_avgRemoteNodeScore / MaGateParam.numberOfTotalNode)
    		;
    	
    	MaGateRecorder.printNodeJobExecutionReport(scenarioId);
    	
    	MaGateToolkit.writeResult(GlobalStorage.systemOutputXSLFile, xslContent);
		
	}
	
	
	public static void prepareOutputFile() {
		
		MaGateToolkit.generateResult(GlobalStorage.systemOutputXSLFile);
		
		String titleText = "Workload, CASA-Variables, CFC-Variables, " +
				"JSC(%), CU(%), A-WJS, A-JS, A-JWT, " + 
				"REQUEST(/j), ACCEPT-R(/j), ASSIGN-R(/j), " +
				"INFORM(/j), ACCEPT-I(/j), ASSIGN-I(/j), " +
				"UNMATCH-REQUEST(/j), UNMATCH-INFORM(/j), " +
				"TotalNodes, TotalPEs, TotalJobs, " +
				"L-JSC(%), R-JSC(%), " +
				"A-ResourceUptime(s), " +
				"A-JRT, A-WJRT, " +
				"A-Network-Degree(%), A-CFC-Degree(%), " + 
				"IS-update, INFORM-FREQUENCY(ms), " +
				"CF-Free-Ad, CF-NonFree-Ad, " +
				"A-Scheduling-Time(/Job), " +
				"A-Scheduling-Decisions(/Node), " +
				"CFRate(%), num-Interacted-Node, avgRemoteNodeScore "
				;
    	
//		String titleText = "Workload, MMPolicy, CASP-Policy, CFC-Policy, " +
//		"TotalNodes, TotalPEs, TotalJobs, " +
//		"JSC(%), L-JSC(%), R-JSC(%), " +
//		"Community-Utilization(%), A-ResourceUptime(s), " +
//		"A-JRT, WA-JRT, A-JS, WA-JS, " +
//		"A-Network-Degree(%), A-CFC-Degree(%), " + 
//		"IS-update, INFORM-FREQUENCY(ms), " +
//		"REQUEST, ACCEPT-R, ASSIGN-R, " +
//		"INFORM, ACCEPT-I, ASSIGN-I, " +
//		"UNMATCH-REQUEST, UNMATCH-INFORM, " +
//		"CF-Free-Ad, CF-NonFree-Ad, " +
//		"A-Scheduling-Time(/Job), " +
//		"A-Scheduling-Decisions(/Node) "
//		;
		
    	MaGateToolkit.writeResult(GlobalStorage.systemOutputXSLFile, titleText);
    	
	}
	
	private static void printNodeJobExecutionReport(String scenarioId) {
		
		GlobalStorage.nodeWorkloadReport.entrySet();
		String txt = "";
		
		Iterator<Map.Entry<String, Integer>> iter = GlobalStorage.nodeWorkloadReport.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Integer> entry = iter.next();
			String nodeId = entry.getKey().trim();
			
			double executedJobs = (double) entry.getValue().intValue() / MaGateParam.countOfExperiment;
			txt = nodeId + ", " + executedJobs;

			String str = MaGateParam.currentScenarioId.replaceAll("/", ":").replaceAll(":", "-").
				replaceAll("%", "").replaceAll(";", "");
			String nodeReportId = GlobalStorage.systemOutputNodeLoadXSLFile + str + ".csv";
			
			MaGateToolkit.writeResult(nodeReportId, txt);
			
		}
		
	}
	
	/**
	 * Logging remote-node reputation list of each participating node
	 * 
	 * @param resultPerScenario
	 */
	private static void nodeTrustAnalysis(LinkedList<MaGateStorage> resultPerScenario) {
		
		Format formatter = new SimpleDateFormat("yyyy.MM.dd 'at' HH.mm.ss");
		
		String nodeTrustRecord = "\n\n\n------------------------ \n\n" +
			"ScenarioId: " + MaGateParam.currentScenarioId + " | Number of experiments: " + MaGateParam.countOfExperiment +  
			"; Real-SysStartTime: " + formatter.format(GlobalStorage.systemStartTime) + 
			"; Real-SystEndTime: " + formatter.format(new Date()) + "\n";


		// print statistic of node networking
		nodeTrustRecord += "\n------------------------\n   each participating node's remote-node reputation list   \n------------------------\n\n";

		for(MaGateStorage perMaGateStorage : resultPerScenario) {
			
			String currentNodeId = perMaGateStorage.getMaGateIdentity();
			double numOfGoodNode = 0.0;
			ConcurrentHashMap<String, RemoteNodeReputationItem> currentRemoteNodeReputation = perMaGateStorage.get_remoteNodeReputationList();
			
			String reputationLogString = "Node: " + currentNodeId + "\n";
			for(Map.Entry<String, RemoteNodeReputationItem> entry: currentRemoteNodeReputation.entrySet()) {
				String remoteNodeId = entry.getKey();
				double remoteNodeReputation = entry.getValue().getRemodeNodeReputation();
				
				reputationLogString += remoteNodeId + ": " + remoteNodeReputation + "; ";
				if(remoteNodeReputation >= 0.6) {
					numOfGoodNode++;
				}
			}
			
			nodeTrustRecord += reputationLogString;
			nodeTrustRecord += "\n[CF node probability (reputation >= 0.6)]: " + (numOfGoodNode / currentRemoteNodeReputation.size());
			nodeTrustRecord += "\nNumber of interacted nodes: " + currentRemoteNodeReputation.size();
			nodeTrustRecord +=  "\n\n\n";
		}
		
		MaGateToolkit.writeResult(GlobalStorage.trustTopologyLogFile, nodeTrustRecord);
	}
	
	/**
	 * Node networking and interaction records analysis
	 * 
	 * @param resultPerScenario
	 */
	private static void nodeTopologyAnalysis(LinkedList<MaGateStorage> resultPerScenario) {
		
		Format formatter = new SimpleDateFormat("yyyy.MM.dd 'at' HH.mm.ss");
		
		// Get total number of PEs of the entire grid
		nodeInteractoins = null;
		nodeInteractoins = new ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>();
		
		nodeNetwork = null;
		nodeNetwork = new ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>();
		
		// Collecting node networking and interaction records
		for(MaGateStorage perMaGateStorage : resultPerScenario) {
			recordNodeInteraction(perMaGateStorage.getMaGateIdentity(), perMaGateStorage.getNodeConfidenceCards());
			recordNodeNetwork(perMaGateStorage.getMaGateIdentity(), perMaGateStorage.get_networkNeighborIds());
		}
		
		String crossNodeInteractions = "\n\n\n------------------------ \n\n" +
				"ScenarioId: " + MaGateParam.currentScenarioId + " | Number of experiments: " + MaGateParam.countOfExperiment +  
				"; Real-SysStartTime: " + formatter.format(GlobalStorage.systemStartTime) + 
				"; Real-SystEndTime: " + formatter.format(new Date()) + "\n";
		
		
		// print statistic of node networking
		crossNodeInteractions += "\n------------------------\n   node networking   \n------------------------\n\n";
		
		Iterator<Map.Entry<String, ConcurrentHashMap<String, Integer>>> iterNetwork = nodeNetwork.entrySet().iterator(); 
		
		while(iterNetwork.hasNext()) {
			Map.Entry<String, ConcurrentHashMap<String, Integer>> entryNetwork = iterNetwork.next();
			String currentNodeId = entryNetwork.getKey();
			ConcurrentHashMap<String, Integer> currentNodeNetwork = entryNetwork.getValue();
			
			Iterator<Map.Entry<String, Integer>> iterRemotes = currentNodeNetwork.entrySet().iterator();
			while(iterRemotes.hasNext()) {
				Map.Entry<String, Integer> entryRemote = iterRemotes.next();
				String remoteNodeId = entryRemote.getKey();
				Integer remoteConnectCounter = entryRemote.getValue();
				
				double connectCounter = (double) remoteConnectCounter.intValue() / MaGateParam.countOfExperiment;
				crossNodeInteractions += "\n" + currentNodeId + "    " + remoteNodeId + 
				"    " + connectCounter;
			}
		}
		
		
		// print statistic of node interactions
		crossNodeInteractions += "\n\n------------------------\n   node interactions   \n------------------------\n\n";
		
		Iterator<Map.Entry<String, ConcurrentHashMap<String, Integer>>> iterInteraction = nodeInteractoins.entrySet().iterator(); 
		
		while (iterInteraction.hasNext()) {
			Map.Entry<String, ConcurrentHashMap<String, Integer>> entry = iterInteraction.next();
			String currentHostingNode = entry.getKey();
			ConcurrentHashMap<String, Integer> remoteNodeInteractionRecords = entry.getValue();
			
			Iterator<Map.Entry<String, Integer>> iter2 = remoteNodeInteractionRecords.entrySet().iterator(); 
			while (iter2.hasNext()) {
				Map.Entry<String, Integer> entry2 = iter2.next();
				String currentRemoteNode = entry2.getKey();
				Integer interactionsWithCurrentRemoteNode = entry2.getValue();
				
				double interactions = (double) interactionsWithCurrentRemoteNode.intValue() / MaGateParam.countOfExperiment;
				
				crossNodeInteractions += "\n" + currentHostingNode + "    " + currentRemoteNode + 
					"    " + interactions;
			}
		}
		
		MaGateToolkit.writeResult(GlobalStorage.crossNodeInteractionFile, crossNodeInteractions);
		
	}
	
	
	
	/**
	 * Each visiting node is considered as the hosting node, wherein each hosting node knows a set of remote nodes with corresponding confidence records, 
	 * including the number of interactions (NoI).
	 * 
	 * Each hosting node then loops its known remote nodes, extracts the NoI, and puts it into a accumulator. If the hosting node is not exist in the accumulator, 
	 * then all extracted data is complete and new; otherwise, the hosting node needs to append the newly extracted data with its old data in the accumulator.
	 * 
	 * Accumulator: nodeInteractoins: ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>()
	 * 
	 * @param hostingNodeId
	 * @param nodeConfidenceCards
	 */
	private static void recordNodeInteraction(String hostingNodeId, ConcurrentHashMap<String, NodeConfidenceCard> nodeConfidenceCards) {
		
		// Each visiting node has a set of Confidence Records (ConfidenceCards) in terms of remote nodes, 
		// therefore each visiting node has a set of Interaction Records with such remote nodes
		
		ConcurrentHashMap<String, Integer> currentNodeInteractionCards = null;
		
		if(nodeInteractoins.containsKey(hostingNodeId)) {
			
			// if hosting node already registered 
			
			Iterator<Map.Entry<String, NodeConfidenceCard>> iter = nodeConfidenceCards.entrySet().iterator(); 
			currentNodeInteractionCards = nodeInteractoins.get(hostingNodeId);
			
			while (iter.hasNext()) {
			    Map.Entry<String, NodeConfidenceCard> entry = iter.next();
			    String remoteNodeId = entry.getKey();
			    int counter = entry.getValue().getNumOfInteraction();
			    
			    if(currentNodeInteractionCards.containsKey(remoteNodeId)) {
					int previousValue = currentNodeInteractionCards.get(remoteNodeId);
					currentNodeInteractionCards.put(remoteNodeId, (counter + previousValue));
				} else {
					// Hosting node already exists in the accumulator, but the knowledge of current remote node is still unknown yet
					currentNodeInteractionCards.put(remoteNodeId, counter);
				}
			    
			} 
			nodeInteractoins.put(hostingNodeId, currentNodeInteractionCards);
			
			
		} else {
			
			// hosting node first time recorded
			
			Iterator<Map.Entry<String, NodeConfidenceCard>> iter = nodeConfidenceCards.entrySet().iterator(); 
			currentNodeInteractionCards = new ConcurrentHashMap<String, Integer>();
			
			while (iter.hasNext()) {
			    Map.Entry<String, NodeConfidenceCard> entry = iter.next();
			    String remoteNodeId = entry.getKey();
			    int counter = entry.getValue().getNumOfInteraction();
			    currentNodeInteractionCards.put(remoteNodeId, counter);
			} 
			nodeInteractoins.put(hostingNodeId, currentNodeInteractionCards);
			
		}
	}
	
	
	private static void recordNodeNetwork(String hostingNodeId, String[] remoteNodeIdList) {
		
		ConcurrentHashMap<String, Integer> currentNodeNetwork = null;
		
		if(nodeNetwork.containsKey(hostingNodeId)) {
			
			currentNodeNetwork = nodeNetwork.get(hostingNodeId);
			for(String remoteId : remoteNodeIdList) {
				if(remoteId != null) {
					if(currentNodeNetwork.containsKey(remoteId)) {
						int previousCounter = currentNodeNetwork.get(remoteId);
						currentNodeNetwork.put(remoteId, new Integer(previousCounter + 1));
					} else {
						currentNodeNetwork.put(remoteId, new Integer(1));
					}
				}
			}
			
			nodeNetwork.put(hostingNodeId, currentNodeNetwork);
			
		} else {
			
			currentNodeNetwork = new ConcurrentHashMap<String, Integer>();
			for(String remoteId : remoteNodeIdList) {
				if(remoteId != null) {
					currentNodeNetwork.put(remoteId, new Integer(1));
				}
			}
			
			nodeNetwork.put(hostingNodeId, currentNodeNetwork);
			
		}
		
	}
	
	
	/**
	 * Suf- process of data made during the execution
	 * @deprecated 
	 */
	public static void processSystemLog() {
		
		String commendMark = "#";
		log.info("\nresDiscoveryProtocol: " + MaGateParam.resDiscoveryProtocol + 
				"\ndelegationNegotiationProtocol: "+ MaGateParam.delegationNegotiationProtocol);
		
		File outputDir_1 = new File(MaGateProfile.outputLocation(), MaGateProfile.resultLocation());
		boolean outputDirExist_1 = outputDir_1.exists();
		
		if (!outputDirExist_1) {
			return;
		} 
		
		String[] outputResults_1 = outputDir_1.list();
        
		// STEP 1: process all "original results" => "refined results", by removing the duplicated rows
        for (int i=0; i< outputResults_1.length; i++) {
        	
        	LinkedHashSet<String> refinedResultSet = new LinkedHashSet<String>();
            File originalResultFile = new File(outputResults_1[i]);
            
            if(!originalResultFile.getName().endsWith("_original.log")) {
            	continue;
            }
            
            String originalFileName = MaGateProfile.outputLocation() + MaGateProfile.resultLocation() + originalResultFile.getName();
            BufferedReader reader = MaGateToolkit.readFile(originalFileName);
            
            try {
				while (reader.ready()) {
					// duplicate rows will be "oversight"
					refinedResultSet.add(reader.readLine());
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String[] originaResultName = originalResultFile.getName().split("_");
			String refinedResultName = originaResultName[0] + "_refined.log";

			// generate the refined file for preserve non-duplicate data
			MaGateToolkit.generateResult(refinedResultName);
			
			// write the non-duplicate data file
			Iterator<String> it = refinedResultSet.iterator();
			while(it.hasNext()) {
				MaGateToolkit.writeResult(refinedResultName, it.next());
			}
			
        }
        
        // STEP 2: process all refined results into a statistic result file
        File outputDir_2 = new File(MaGateProfile.outputLocation(), MaGateProfile.resultLocation());

        String[] outputResults_2 = outputDir_2.list();
                
        // ye: process each refined results
        for (int i = 0; i< outputResults_2.length; i++) {
        	
            File refinedRecord = new File(outputResults_2[i]);
                
            if(!refinedRecord.getName().endsWith("_refined.log")) {
                continue;
            }
            
            // for each *_refined file
            LinkedHashMap<String, InnerResult> statisticResultMap = new LinkedHashMap<String, InnerResult>();
            String[] refinedFileNameArray = refinedRecord.getName().split("_");
            String resName = refinedFileNameArray[0];
            
            // get a statistic data file
            String refinedRecordPath = MaGateProfile.outputLocation() + MaGateProfile.resultLocation() + refinedRecord.getName();
            BufferedReader reader = MaGateToolkit.readFile(refinedRecordPath);
            
            String resNickName = "";
            String maGateId    = "";
            String totalPE     = "";
            
            try {
            	
				while (reader.ready()) {
					
					String recordFromReader = reader.readLine();
					
					if(recordFromReader == null || recordFromReader.trim().equals("") || recordFromReader.startsWith(commendMark)) {
						
						if(recordFromReader.startsWith("#Resource")) {
							resNickName = recordFromReader.split("_")[1];
							
						} else if (recordFromReader.startsWith("#MaGateIdentity")) {
							maGateId = recordFromReader.split("_")[1];
								
						} else if (recordFromReader.startsWith("#TotalPE")) {
							totalPE = recordFromReader.split("_")[1];
								
						}
						continue;
					}
					
					// ye: if key already exists, increase the array and counter, or put the new value
					String[] currentArray = recordFromReader.split("\t");
					
					// fetch the clock mark (column: 2)
					String clockMark = currentArray[1];
					
					int currentIteration = Integer.parseInt(currentArray[0]);
					
					// transfer data from refined set to statistic map
					double[] proccessedArray = MaGateToolkit.arrayTransfer(currentArray);
					
					if(statisticResultMap.containsKey(clockMark)) {
						InnerResult existRecord = statisticResultMap.get(clockMark);
						
						// increase the inner data, starting from column: 3
						for(int innerIndex = 2; innerIndex < proccessedArray.length; innerIndex++) {
							existRecord.record[innerIndex] += proccessedArray[innerIndex];
						}
						existRecord.counter += 1;
						
					} else {
						
						InnerResult newRecord = new InnerResult(currentArray.length);
						newRecord.record = proccessedArray;
						newRecord.counter += 1;
						
						statisticResultMap.put(clockMark, newRecord);
					}
					
				} // exit LOOP: reader.ready() for each "*_refined" file
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// sort the statistic result 
			Collection<InnerResult> collection = statisticResultMap.values();
			
			Object[] array = collection.toArray();
			Comparator<Object> resultComparator = new ResultComparator();
			Arrays.sort(array, resultComparator);
			
			// prepare the statistic file, title, etc
			String staticResultName = resName + "_statistic.log";
			String initContent = "#Resource: " + resNickName + 
				"\n#MaGateIdentity: " + maGateId +
				"\n#TotalPE: " + totalPE;
			
			MaGateToolkit.generateResult(staticResultName);
			MaGateToolkit.writeResult(staticResultName, initContent);
			
			// ye: suf-process the obtained statistic file by dividing counter
			for(int arrayIndex = 0; arrayIndex < array.length; arrayIndex++) {
				InnerResult currentInnerResult = (InnerResult) array[arrayIndex];
				
				String stringFromDoubleArray = "";
				stringFromDoubleArray += currentInnerResult.record[1] + "\t";
				
				for(int j = 2; j < currentInnerResult.record.length; j++) {
					currentInnerResult.record[j] = currentInnerResult.record[j] / currentInnerResult.counter;
					stringFromDoubleArray += currentInnerResult.record[j] + "\t";
				}
				
				MaGateToolkit.writeResult(staticResultName, stringFromDoubleArray);
			}
			
        } // exit LOOP: outputDir2.list() for getting all statistic files
        
        
        // STEP 3: process all statistic results into a total statistic result file
        File outputDir_3 = new File(MaGateProfile.outputLocation(), MaGateProfile.resultLocation());
        String[] outputResults_3 = outputDir_3.list();
        
        // for each total statistic file
        LinkedHashMap<String, InnerResult> totalStatisticResultMap = new LinkedHashMap<String, InnerResult>();
        
        // ye: process statistic refined results
        for (int i = 0; i< outputResults_3.length; i++) {
        	
            File singleStatisticRecord = new File(outputResults_3[i]);
                
            if(!singleStatisticRecord.getName().endsWith("_statistic.log")) {
                continue;
            }
            
            String singleStatisticRecordPath = MaGateProfile.outputLocation() + MaGateProfile.resultLocation() + singleStatisticRecord.getName();
            BufferedReader reader = MaGateToolkit.readFile(singleStatisticRecordPath);
            
            try {
            	
        		while (reader.ready()) {
        			
        			String recordFromReader = reader.readLine();
        			
        			if(recordFromReader == null || recordFromReader.trim().equals("") || recordFromReader.startsWith(commendMark)) {
        				continue;
        			}
        			
        			// ye: if key already exists, increase the array and counter, or put the new value
        			String[] currentArray = recordFromReader.split("\t");
        			
        			// fetch the clock mark (column: 1)
        			String clockMark = currentArray[0];
        			
        			// transfer data from refined set to statistic map
        			double[] proccessedArray = MaGateToolkit.arrayTransfer(currentArray);
        			
        			if(totalStatisticResultMap.containsKey(clockMark)) {
        				InnerResult existRecord = totalStatisticResultMap.get(clockMark);
        				
        				// increase the inner data, starting from column: 2
        				for(int innerIndex = 1; innerIndex < proccessedArray.length; innerIndex++) {
        					existRecord.record[innerIndex] += proccessedArray[innerIndex];
        				}
        				existRecord.counter += 1;
        				
        			} else {
        				
        				InnerResult newRecord = new InnerResult(currentArray.length);
        				newRecord.record = proccessedArray;
        				newRecord.counter += 1;
        				
        				totalStatisticResultMap.put(clockMark, newRecord);
        			}
        			
        		} // exit LOOP: reader.ready() for each "*_refined" file
        		
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
        
        // sort the total statistic result 
    	Collection<InnerResult> collection2 = totalStatisticResultMap.values();
    	
    	Object[] array2 = collection2.toArray();
    	Comparator<Object> resultComparator = new ResultComparator();
    	Arrays.sort(array2, resultComparator);
    	
    	String totalStaticResultName = "statistic_total.log";
    	
    	MaGateToolkit.generateResult(totalStaticResultName);
    	
    	// ye: suf-process the obtained statistic file by dividing counter
    	for(int arrayIndex = 0; arrayIndex < array2.length; arrayIndex++) {
    		InnerResult currentInnerResult = (InnerResult) array2[arrayIndex];
    		
    		String stringFromDoubleArray = "";
    		stringFromDoubleArray += currentInnerResult.record[0] + "\t";
    		
    		for(int j = 1; j < currentInnerResult.record.length; j++) {
    			currentInnerResult.record[j] = currentInnerResult.record[j] / currentInnerResult.counter;
    			stringFromDoubleArray += currentInnerResult.record[j] + "\t";
    		}
    		
    		MaGateToolkit.writeResult(totalStaticResultName, stringFromDoubleArray);
    	}
	}
	
	
}



