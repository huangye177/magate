package ch.hefr.gridgroup.magate.storage;

import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.MaGateEntity;
import ch.hefr.gridgroup.magate.env.JobCenterManager;
import ch.hefr.gridgroup.magate.env.MaGateMediator;
import ch.hefr.gridgroup.magate.env.MaGateParam;
import ch.hefr.gridgroup.magate.input.*;

public class GlobalStorage {

	private static Log log = LogFactory.getLog(GlobalStorage.class);
	private static AtomicBoolean isMaGateCollectionReady = new AtomicBoolean(false);

	private static ConcurrentHashMap<String, MaGateEntity> maGateCollection = new ConcurrentHashMap<String, MaGateEntity>();
	private static Vector<ExpScenario> experimentScenarios = new Vector<ExpScenario>();
	
	public static ConcurrentHashMap<String, Integer> nodeWorkloadReport = new ConcurrentHashMap<String, Integer>();
	
	// Tech report counter
	public static AtomicInteger count_sql = new AtomicInteger(0);
	public static AtomicInteger count_sql2 = new AtomicInteger(0);
	public static AtomicInteger count_sql3 = new AtomicInteger(0);
	public static AtomicLong count_moduleController = new AtomicLong(0);
	public static AtomicLong count_caspController = new AtomicLong(0);
	public static AtomicLong count_casp_request_Controller = new AtomicLong(0);
	public static AtomicLong count_casp_inform_Controller = new AtomicLong(0);
	public static AtomicLong count_casp_accept_req_Controller = new AtomicLong(0);
	public static AtomicLong count_casp_accept_inf_Controller = new AtomicLong(0);
	public static AtomicLong count_casp_accept_null_Controller = new AtomicLong(0);
	public static AtomicLong count_casp_selectnode_Controller = new AtomicLong(0);
	
	public static AtomicInteger count_test1 = new AtomicInteger(0);
	public static AtomicInteger count_test2 = new AtomicInteger(0);
	public static AtomicInteger count_test3 = new AtomicInteger(0);
	public static AtomicInteger count_test4 = new AtomicInteger(0);
	public static AtomicInteger count_test5 = new AtomicInteger(0);
	public static AtomicInteger count_test6 = new AtomicInteger(0);
	public static AtomicInteger count_test7 = new AtomicInteger(0);
	public static AtomicInteger count_test8 = new AtomicInteger(0);
	public static AtomicInteger count_test9 = new AtomicInteger(0);
	
	// public counters per experiment iteration
	
	public static AtomicInteger count_communitySearch = new AtomicInteger(0);
	public static AtomicInteger count_communitySearchFeedback = new AtomicInteger(0);
	public static AtomicInteger count_updateNeighborhood = new AtomicInteger(0);
	public static AtomicInteger count_matchmakerDecision = new AtomicInteger(0);
	public static AtomicInteger count_queryNeighbors = new AtomicInteger(0);
	public static AtomicInteger count_queryCF = new AtomicInteger(0);
	
	
	public static AtomicInteger count_msgREQUEST = new AtomicInteger(0);
	public static AtomicInteger count_msgINFORM = new AtomicInteger(0);
	
	public static AtomicInteger count_msgACCEPT_REQUEST = new AtomicInteger(0);
	public static AtomicInteger count_msgACCEPT_INFORM = new AtomicInteger(0);
	
	public static AtomicInteger count_msgASSIGN_REQUEST = new AtomicInteger(0);
	public static AtomicInteger count_msgASSIGN_INFORM = new AtomicInteger(0);
	
	public static AtomicInteger count_errorCASP = new AtomicInteger(0);
	public static AtomicInteger count_unmatchedREQUEST = new AtomicInteger(0);
	public static AtomicInteger count_unmatchedINFORM = new AtomicInteger(0);
	
	public static AtomicInteger count_CFCFreePower = new AtomicInteger(0);
	public static AtomicInteger count_CFCNonfreepower = new AtomicInteger(0);
	
	// public counters per experiment scenario
	
	public static double total_count_communitySearch = 0.0;
	public static double total_count_communitySearchFeedback = 0.0;
	public static double total_count_updateNeighborhood = 0.0;
	public static double total_count_matchmakerDecision = 0.0;
	public static double total_count_queryNeighbors = 0.0;
	public static double total_count_queryCF = 0.0;
	
	
	public static AtomicInteger total_count_job_generated = new AtomicInteger(0);
	public static AtomicInteger total_count_job_submitted = new AtomicInteger(0);
	public static AtomicInteger total_count_job_scheduling = new AtomicInteger(0);
	public static AtomicInteger total_count_job_processing = new AtomicInteger(0);
	public static AtomicInteger total_count_job_transferred = new AtomicInteger(0);
	public static AtomicInteger total_count_job_executed = new AtomicInteger(0);
	public static AtomicInteger total_count_job_suspended = new AtomicInteger(0);
	public static AtomicInteger total_count_job_failed = new AtomicInteger(0);
	public static AtomicInteger total_count_job_db_stored = new AtomicInteger(0);
	
	
	public static AtomicInteger total_count_job_local_exed = new AtomicInteger(0);
	public static AtomicInteger total_count_job_community_exed = new AtomicInteger(0);
	
	
	public static double total_count_msgREQUEST = 0.0;
	public static double total_count_msgINFORM = 0.0;
	public static double total_count_msgACCEPT_REQUEST = 0.0;
	public static double total_count_msgACCEPT_INFORM = 0.0;
	public static double total_count_msgASSIGN_REQUEST = 0.0;
	public static double total_count_msgASSIGN_INFORM = 0.0;
	public static double total_count_errorCASP = 0.0;
	public static double total_count_unmatchedREQUEST = 0.0;
	public static double total_count_unmatchedINFORM = 0.0;
	
	public static double total_count_CFCFreePower = 0.0;
	public static double total_count_CFCNonfreepower = 0.0;
	
	public static AtomicInteger test_counter_1 = new AtomicInteger(0);
	public static AtomicInteger test_counter_2 = new AtomicInteger(0);
	public static AtomicInteger test_counter_3 = new AtomicInteger(0);
	public static AtomicInteger test_counter_4 = new AtomicInteger(0);
	
	public static AtomicInteger test_counter_5 = new AtomicInteger(0);
//	public static AtomicInteger test_counter_6 = new AtomicInteger(0);
//	public static AtomicInteger test_counter_6_2 = new AtomicInteger(0);
//	public static AtomicInteger test_counter_6_3 = new AtomicInteger(0);
	
//	public static AtomicInteger kkk = new AtomicInteger(0);
//	public static AtomicInteger ppp = new AtomicInteger(0);
	
	public static Date systemStartTime = new Date();
	public static String systemOutputXSLFile = "exp_results_" + systemStartTime.toString().replaceAll(" " , "_").replaceAll(":" , "_") + ".csv";
	public static String systemOutputNodeLoadXSLFile = "node_exedload_";
	public static String systemOutputTXTFile = "exp_results_" + systemStartTime.toString().replaceAll(" " , "_").replaceAll(":" , "_") + ".txt";
	public static String crossNodeInteractionFile = "crossNodeInteraction" + systemStartTime.toString().replaceAll(" " , "_").replaceAll(":" , "_") + ".txt";
	public static String trustTopologyLogFile = "trustTopologyLogFile" + systemStartTime.toString().replaceAll(" " , "_").replaceAll(":" , "_") + ".txt";
	public static String communityFlowOutputFile = "community_status_changes_";
	
	/**
	 * Clear all experiment relevant counters from previous execution (experimental iteration)
	 */
	public static void clearCounterPerScenarioIteration() {
		
		count_communitySearch.set(0);
		count_communitySearchFeedback.set(0);
		count_updateNeighborhood.set(0);
		count_matchmakerDecision.set(0);
		
		count_msgREQUEST.set(0);
		count_msgINFORM.set(0);
		count_msgACCEPT_REQUEST.set(0);
		count_msgACCEPT_INFORM.set(0);
		count_msgASSIGN_REQUEST.set(0);
		count_msgASSIGN_INFORM.set(0);
		count_unmatchedREQUEST.set(0);
		count_unmatchedINFORM.set(0);
		
		count_queryNeighbors.set(0);
		count_queryCF.set(0);
		
		count_errorCASP.set(0);
		
		count_CFCFreePower.set(0);
		count_CFCNonfreepower.set(0);
		
		test_counter_1.set(0);
		test_counter_2.set(0);
		test_counter_3.set(0);
	}
	
	/**
	 * Record all experiment relevant counters from previous execution (experimental iteration)
	 */
	public static void recordCounterPerScenarioIteration() {
		
		total_count_communitySearch += count_communitySearch.get();
		total_count_communitySearchFeedback += count_communitySearchFeedback.get();
		total_count_updateNeighborhood += count_updateNeighborhood.get();
		total_count_matchmakerDecision += count_matchmakerDecision.get();
		
		/**
		 * Update of below will happen in the ModuleController.finalizeMaGateStorage of EACH node
		 */
//		total_count_job_generated += count_job_generated.get();
//		total_count_job_submitted += count_job_submitted.get();
//		total_count_job_scheduling += count_job_scheduling.get();
//		total_count_job_processing += count_job_processing.get();
//		total_count_job_transferred += count_job_transferred.get();
//		total_count_job_executed += count_job_executed.get();
//		total_count_job_suspended += count_job_suspended.get();
//		total_count_job_failed += count_job_failed.get();
//		total_count_job_db_stored += count_job_db_stored.get();
		
		total_count_msgREQUEST += count_msgREQUEST.get();
		total_count_msgINFORM += count_msgINFORM.get();
		total_count_msgACCEPT_REQUEST += count_msgACCEPT_REQUEST.get();
		total_count_msgACCEPT_INFORM += count_msgACCEPT_INFORM.get();
		total_count_msgASSIGN_REQUEST += count_msgASSIGN_REQUEST.get();
		total_count_msgASSIGN_INFORM += count_msgASSIGN_INFORM.get();
		total_count_unmatchedREQUEST += count_unmatchedREQUEST.get();
		total_count_unmatchedINFORM += count_unmatchedINFORM.get();
		
		total_count_queryNeighbors += count_queryNeighbors.get();
		total_count_queryCF += count_queryCF.get();
		
		total_count_errorCASP += count_errorCASP.get();
		
		total_count_CFCFreePower += count_CFCFreePower.get();
		total_count_CFCNonfreepower += count_CFCNonfreepower.get();
		
	}
	
	/**
	 * Clear all experiment relevant counters from previous scenario (mutl-iterations)
	 */
	public static void clearCounterPerScenario() {
		
		nodeWorkloadReport = null;
		nodeWorkloadReport = new ConcurrentHashMap<String, Integer>();

		total_count_communitySearch = 0;
		total_count_communitySearchFeedback = 0;
		total_count_updateNeighborhood = 0;
		total_count_matchmakerDecision = 0;
		
		total_count_job_generated.set(0);
		total_count_job_submitted.set(0);
		total_count_job_scheduling.set(0);
		total_count_job_processing.set(0);
		total_count_job_transferred.set(0);
		total_count_job_executed.set(0);
		total_count_job_suspended.set(0);
		total_count_job_failed.set(0);
		total_count_job_db_stored.set(0);
		
		total_count_job_local_exed.set(0);
		total_count_job_community_exed.set(0);
		
		total_count_msgREQUEST = 0;
		total_count_msgINFORM = 0;
		total_count_msgACCEPT_REQUEST = 0;
		total_count_msgACCEPT_INFORM = 0;
		total_count_msgASSIGN_REQUEST = 0;
		total_count_msgASSIGN_INFORM = 0;
		total_count_unmatchedREQUEST = 0;
		total_count_unmatchedINFORM = 0;
		
		total_count_queryNeighbors = 0;
		total_count_queryCF = 0;
		
		total_count_errorCASP = 0;
		
		total_count_CFCFreePower = 0;
		total_count_CFCNonfreepower = 0;
		
	}
	
//	public static double getDynamicCommunityEfficiency() {
//		
//		Collection<MaGateEntity> magateCollection = maGateCollection.values();
//		
//		double activePE = 0;
//		double totalPE  = 0;
//		for(MaGateEntity maGate : magateCollection) {
//			activePE += maGate.getStorage().getTotalActivePEs().get();
//			totalPE += maGate.getStorage().getTotalAvailPEs().get();
//		}
//		
//		return (activePE/totalPE);
//	}
	
	/**
	 * Get dynamic dataset by looping the node collection
	 * 
	 * 1. Community Efficiency 
	 * 2. Network Degree
	 * 3. CFC Degree
	 * 
	 * @return
	 */
	public static double[] getDynamicDataset() {
		
		double[] dataset = new double[3];
		
		Collection<MaGateEntity> magateCollection = maGateCollection.values();
		
		double activePE           = 0;
		double totalPE            = 0;
		double totalNetworkDegree = 0.0;
		double totalCFCDegree     = 0.0;
		
		for(MaGateEntity maGate : magateCollection) {
			activePE += maGate.getStorage().getTotalActivePEs().get();
			totalPE += maGate.getStorage().getTotalNumOfPEs().get();
			totalNetworkDegree += maGate.getStorage().size_networkNeighbors();
			totalCFCDegree += maGate.getStorage().size_cfcNeighbors();
		}
		
		double ce            = activePE/totalPE;
		double networkDegree = totalNetworkDegree / MaGateParam.numberOfTotalNode;
		double cfcDegree     = totalCFCDegree / MaGateParam.numberOfTotalNode;
		
		dataset[0] = ce;
		dataset[1] = networkDegree;
		dataset[2] = cfcDegree;
		
		return dataset;
	}
	
	/**
	 * Writing community status snapshot for plotting 
	 */
	public static void recordLiveCommunityStatus() {
		
		// GENERATED = 0; SUBMITTED = 1; SCHEDULING = 2; PROCESSING = 3; TRANSFERRED = 4;
		// EXECUTED = 5; FAILED = 6; SUSPENDED = 7;
		int[] allStatus = JobCenterManager.sizeOfJob_allStatus();
		
		if(allStatus == null) {
			return;
		}
		
		double rjcPercentage = allStatus[5] / MaGateParam.totalNumberOfJob;
		
		double arrivedJob = MaGateParam.totalNumberOfJob - allStatus[0];
		double[] dynamicDataSet = GlobalStorage.getDynamicDataset();
		
		
		// Recording live data into DB for real-time plotting
		MaGateDB.insertLiveCommunity_DB(MaGateMediator.getSystemTime() + MaGateParam.systemStartTime, 
				arrivedJob, allStatus[2], 
				allStatus[3], allStatus[4], 
				allStatus[5], allStatus[7], 
				allStatus[6], 
				(rjcPercentage * 100), (dynamicDataSet[0] * 100), 
				dynamicDataSet[1], dynamicDataSet[2]);
		
		// Recording snapshot data into DB 
		// updateCommunityStatus_DB(generated_job, submitted_job, scheduling_job, 
		// processing_job, transferred_job, executed_job, suspended_job, failed_job) 
		//
		// IMPORTANT: suspended_job msgID: 7; failed_job msg_ID: 6
		MaGateDB.updateCommunityStatus_DB(allStatus[0], allStatus[1], allStatus[2], 
				allStatus[3], allStatus[4], allStatus[5], allStatus[7], allStatus[6]);
	}
	

	/**
	 * Generate a magateId-MaGateReference(key-value) pair 
	 * @param maGate
	 */
	public static void maGateJoin(MaGateEntity maGate) {
		
		String id = maGate.getMaGateIdentity();
		
		try {
			maGateCollection.put(id, maGate);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Remove a magateId-MaGateReference(key-value) pair according magateId(key)
	 * @param maGateId
	 */
	public static void maGateDetach(String maGateId) {
		
		try {
			maGateCollection.remove(maGateId);
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Remove all magateId-MaGateReference(key-value) pair 
	 */
	public static void allMaGateDetach() {
		
		try {
			maGateCollection.clear();
			setMaGateCollectionReady(false);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Searching for a magateId-MaGateReference(key-value) pair according to the magateId(key)
	 * @param maGateIdentity
	 * @return
	 */
	public static MaGateEntity findMaGateById(String maGateIdentity) {
		
		if(maGateIdentity == null || maGateIdentity.trim().equals("")) {
			return null;
		}
			
		if(maGateCollection.containsKey(maGateIdentity)) {
			return maGateCollection.get(maGateIdentity);
		} else {
			return null;
		}
		
	}
	
	public static int sizeOfMaGateCollection() {
		return maGateCollection.size();
	}
	
	public static boolean getMaGateCollectionReady() {
		return isMaGateCollectionReady.get();
	}

	public static void setMaGateCollectionReady(boolean maGateCollectionReady) {
		GlobalStorage.isMaGateCollectionReady = new AtomicBoolean(maGateCollectionReady);
	}
	
	public static int sizeOfExperimentScenarios() {
		return experimentScenarios.size();
	}
	
	public static Vector<ExpScenario> getExperimentScenarios() {
		return experimentScenarios;
	}

	public static void setExperimentScenarios(
			Vector<ExpScenario> experimentScenarios) {
		
		if(experimentScenarios == null) {
			log.error("Empty Scenarios");
			System.exit(0);
		} else {
			GlobalStorage.experimentScenarios = experimentScenarios;
		}
		
	}
	
	public static Collection<MaGateEntity> getAllMaGate() {
		return maGateCollection.values();
	}
	
	public static Enumeration<String> getAllMaGateIds() {
		return maGateCollection.keys();
	}
	
}
