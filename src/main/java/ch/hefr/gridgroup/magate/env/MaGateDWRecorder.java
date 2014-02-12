package ch.hefr.gridgroup.magate.env;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.model.MMResult;
import ch.hefr.gridgroup.magate.storage.GlobalStorage;
import ch.hefr.gridgroup.magate.storage.MaGateDB;
import ch.hefr.gridgroup.magate.storage.MaGateStorage;

public class MaGateDWRecorder {

	private static Log log = LogFactory.getLog(MaGateDWRecorder.class);
	
	/**
	 * Record history data of each Scenario + Iteration
	 * 
	 * @param storageList
	 */
	public static void recordScenarioStatistic() {
		
		MaGateDB.updateScenarioStatistic_DB();
		
	}
	
	/**
	 * Record history data from [each Iteration] of [each Scenario]
	 * @param storageList
	 */
	public static void recordScenarioIterationStatistic(LinkedList<MaGateStorage> resultPerIteration) {
		
		MMResult mmResult = null;
		
		double generated_job = 0;
		double submitted_job = 0;
		double scheduling_job = 0;
		double processing_job = 0;
		double transferred_job = 0;
		double executed_job = 0;
		double suspended_job = 0;
		double failed_job = 0;
		
		double localSearch = 0;
		double communitySearch = 0;
		double efficency = 0.0;
		double makespane = 0.0;
		
		double accomplishment = 0.0;
		
		int numOfMaGate = resultPerIteration.size();
		
		for(MaGateStorage storage : resultPerIteration) {
			
			mmResult = storage.getMMResult();
			
			// GENERATED = 0; SUBMITTED = 1; SCHEDULING = 2; PROCESSING = 3; TRANSFERRED = 4;
			// EXECUTED = 5; FAILED = 6; SUSPENDED = 7;
			int[] allStatus = JobCenterManager.sizeOfJob_allStatusByNode(storage.getMaGateIdentity());
			
			generated_job += allStatus[0];
			submitted_job += allStatus[1];
			scheduling_job += allStatus[2];
			processing_job += allStatus[3];
			transferred_job += allStatus[4];
			executed_job += allStatus[5];
			suspended_job += allStatus[7];
			failed_job += allStatus[6];
			
			efficency += MaGateToolkit.convertAtomicLongToDouble(storage.getResourceEfficency());  
			makespane += mmResult.getResourceUptime();  
			
//			localSearch += storage.getLocalSearch().get();
			localSearch += GlobalStorage.count_queryNeighbors.get();
			communitySearch += storage.getCommunitySearch().get();
		}
		
		generated_job = generated_job / numOfMaGate; 
		submitted_job = submitted_job / numOfMaGate; 
		scheduling_job = scheduling_job / numOfMaGate;  
		processing_job = processing_job / numOfMaGate;  
		transferred_job = transferred_job / numOfMaGate;
		executed_job = executed_job / numOfMaGate;
		suspended_job = suspended_job / numOfMaGate;
		failed_job = failed_job / numOfMaGate;
		
		efficency = efficency / numOfMaGate;
		makespane = makespane / numOfMaGate;
		
		localSearch = localSearch / (numOfMaGate * MaGateParam.numberOfTotalNode * MaGateParam.numOfInputJob_perNode);
		communitySearch = communitySearch / (numOfMaGate * MaGateParam.numberOfTotalNode * MaGateParam.numOfInputJob_perNode);
		
		accomplishment = executed_job / (MaGateParam.numberOfTotalNode * MaGateParam.numOfInputJob_perNode);
		
		
		MaGateDB.insertScenarioIterationStatistic_DB(MaGateParam.currentScenarioId, 
				generated_job, submitted_job, scheduling_job, processing_job, transferred_job, 
				executed_job, suspended_job, failed_job, 
				efficency, makespane, localSearch, communitySearch, accomplishment);
		
	}
	
	/**
	 * Recording community status of iterations into scenario DBs
	 */
	public static void recordPerScenarioIterationCommunityStatus() {
		
		System.out.println("Recording community status from iterations into each scenario... ");
		
		MaGateDB.insertScenarioCommunity_DB();
		
//		JobCenterManager.recordLiveCommunityFlowIntoCSV();
		
		System.out.println("Recording community status from iterations into each scenario complete! ");
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Recording community status changes into files
	 */
	public static void recordPerScenarioCommunityStatus() {
		
//		JobCenterManager.recordScenarioCommunityFlowIntoCSV();
//		JobCenterManager.recordScenarioCommunityFlowIntoCSV2();
//		
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
	
	/**
	 * Delete all data of Community-Status before each experiment iteration
	 */
	public static void flushPerIterationCommunityStatus() {
		
		MaGateDB.flushPerIterationCommunityStatusTB();
		
	}
	
	/**
	 * Delete all data of Community-Status for each experiment scenario
	 */
	public static void flushPerScenarioCommunityStatus() {
		
		MaGateDB.flushPerScenarioCommunityStatusTB();
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}






