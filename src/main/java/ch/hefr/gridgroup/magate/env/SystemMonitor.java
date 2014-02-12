package ch.hefr.gridgroup.magate.env;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.MaGateEntity;
import ch.hefr.gridgroup.magate.casa.CASAPolicy;
import ch.hefr.gridgroup.magate.cfm.CFCPolicy;
import ch.hefr.gridgroup.magate.input.ExpScenario;
import ch.hefr.gridgroup.magate.storage.GlobalStorage;
import ch.hefr.gridgroup.magate.storage.MaGateDB;


public class SystemMonitor {
	
	private ExecutorService executor;

	private ScheduledExecutorService scheduledExecutor;
	private MonitorTextEvent monitorTextEvent;
	private MonitorPlotEvent monitorPlotEvent;
	private MonitorLifecycleEvent monitorLifecycleEvent;
	
	private long   previousClock;
	
	private static Log log = LogFactory.getLog(SystemMonitor.class);
	
	private int currentIteration = 0;
	private String iterationId = "";
	

	public SystemMonitor(String iterationId) {
		
		this.iterationId = iterationId;
		this.previousClock  = System.currentTimeMillis();
		
		this.executor = Executors.newFixedThreadPool(3);
		this.scheduledExecutor = Executors.newScheduledThreadPool(3);
		
		this.monitorTextEvent = new MonitorTextEvent(iterationId);
		this.monitorPlotEvent = new MonitorPlotEvent();
		this.monitorLifecycleEvent = new MonitorLifecycleEvent();
		
		// ye: run timer tasks
//		this.scheduledExecutor.scheduleAtFixedRate(monitorEvent, 10, 20, TimeUnit.MILLISECONDS);
		this.scheduledExecutor.scheduleAtFixedRate(monitorTextEvent, 50, 6000, TimeUnit.MILLISECONDS);
		this.scheduledExecutor.scheduleAtFixedRate(monitorPlotEvent, 0, 500, TimeUnit.MILLISECONDS);
		this.scheduledExecutor.scheduleAtFixedRate(monitorLifecycleEvent, 0, 1000, TimeUnit.MILLISECONDS);
//		this.scheduledExecutor.scheduleWithFixedDelay(monitorEvent, 50, 5000, TimeUnit.MILLISECONDS);
		
	}
	
	
	public void terminateSystemMonitor() {
		
		if(this.executor != null && !this.executor.isTerminated()) {
			this.executor.shutdown();
		}
		
		if(this.scheduledExecutor != null && !this.scheduledExecutor.isTerminated()) {
			this.scheduledExecutor.shutdown();
		}
		
	}
	
	
	protected class MonitorTextEvent implements Runnable {

		private Format formatter;
		private double iteration = 0.0;
		private double dynamicThreshold = 0.0;
//		private String ajceFile = "";
		private String iterationId = "";
		private boolean stopSimulator = false;
		
		public MonitorTextEvent(String iterationId) {
			this.iterationId = iterationId;
			this.formatter = new SimpleDateFormat("yyyy.MM.dd 'at' HH.mm.ss");
			this.iteration = MaGateParam.totalNumberOfJob / 100;
//			this.ajceFile = "gwa-grid5000-ajce-" + this.iterationId + ".csv";
			
//			MaGateToolkit.generateResult(ajceFile);
//			String titleText = "currentSysTime, currentSysDate, arrivedJob, schedulingJob, dynamicCE";
//			MaGateToolkit.writeResult(ajceFile, titleText);
		}
		
		public void run() {
			
			currentIteration++;
			
			// GENERATED = 0; SUBMITTED = 1; SCHEDULING = 2; PROCESSING = 3; TRANSFERRED = 4;
			// EXECUTED = 5; FAILED = 6; SUSPENDED = 7;
			int[] allStatus = JobCenterManager.sizeOfJob_allStatus();
			int numOfNode   = GlobalStorage.sizeOfMaGateCollection();
			
			// General information about experiment scenario
			String outputText = "\n\n-------------- Community Monitoring Status -------------- \n<" + currentIteration + 
				">  Sim-Time: " + MaGateMediator.getSystemTime() + 
				" Real-SysStartTime: " + this.formatter.format(new Date());;
			
			outputText += "\n\n Current Scenario Id: " + MaGateParam.currentScenarioId + "\n";
			
			outputText += "\n  Nodes: " + numOfNode + 
				"  Num-Scenarios: " + GlobalStorage.sizeOfExperimentScenarios() + 
				"  [Avg]Num-MM-Decisions: " + (GlobalStorage.count_matchmakerDecision.doubleValue() / numOfNode);
			
			if(MaGateParam.CASP_Enabled) {
				outputText += " \n CASP-Enabled";
			} else {
				outputText += " \n CASP-Disabled";
			}
			
			// Isolated VO
			if(MaGateParam.isolatedVO) {
				outputText += " VO-Boundary-Enabled";
			} else {
				outputText += " VO-Boundary-Disabled";
			}
			
			// Agreement specification
			if(MaGateParam.Agreement_Enabled) {
				outputText += " Agreement-Enabled";
			} else {
				outputText += " Agreement-Disabled";
			}
			
//			// Scenario counter
//			outputText += "\n [Scenario Counter]: " + MaGateParam.scenario_current + ":" + MaGateParam.scenario_overall + "-";
			
			// CASA Variables 
			outputText += " \n\n CASA Variables: ";
			if(MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Independent) {
				outputText += "[Scheme]Independent  ";
			} else if (MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Centralized) {
				outputText += "[Scheme]Centralized  ";
			} else if (MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Decentralized) {
				
				if(MaGateParam.systemIS == MaGateMessage.systemIS_Global) {
					outputText += "[Scheme]Decentralized-[IS]Global  ";
				} else if(MaGateParam.systemIS == MaGateMessage.systemIS_Partial_ACO) {
					outputText += "[Scheme]Decentralized-[IS]Partial-ACO  ";
				} else if(MaGateParam.systemIS == MaGateMessage.systemIS_Partial_SIM) {
					outputText += "[Scheme]Decentralized-[IS]Partial-SIM  ";
				}
				
				if(MaGateParam.jobDistMode == MaGateMessage.JobDistMode_CustomizedDist) {
					outputText += "[jobDistMode]CustomizedDist  ";
				} else if(MaGateParam.jobDistMode == MaGateMessage.JobDistMode_FairDist) {
					outputText += "[jobDistMode]FairDist  ";
				}
			}
			
			outputText += "[Rescheduling on AvgQueuingTime]: " + MaGateParam.systemReschedulingOnAvgQueuingTime + "";
			outputText += "  [Coefficient of Rescheduling]: " + MaGateParam.systemReschedulingCoefficient + "";
			
			// CFC Policy
//			outputText += " \n CFC_Policy: ";
//			Enumeration<String> currentCFCPolicies = MaGateParam.CFC_POLICY.elements();
//			while(currentCFCPolicies.hasMoreElements()) {
//				String currentPolicy = currentCFCPolicies.nextElement();
//				outputText += currentPolicy + "; ";
//			}
			
			outputText += " \n\n CFC_Policy: ";
			Iterator<Map.Entry<String, String>> iter5 = MaGateParam.CFM_POLICY.entrySet().iterator();
			while (iter5.hasNext()) {
				Map.Entry<String, String> entry5 = iter5.next();
				String currentPolicy = entry5.getValue();
				outputText += currentPolicy + "; ";
			}
			
			DecimalFormat twoDFormat = new DecimalFormat("#.##");

			outputText += " \n [FailScore]" + Double.valueOf(MaGateParam.cfmDelegationFailScore);
			outputText += " [D-Due]" + Double.valueOf(twoDFormat.format((MaGateParam.cfmJobReturnDeadline / 3600)));
			outputText += " [RecentTimeBarrier]" + Double.valueOf(twoDFormat.format(MaGateParam.cfmRecentTimeBarrier / 3600));
			outputText += " [L-RepWeight]" + Double.valueOf(twoDFormat.format(MaGateParam.cfmLocalReputationWeight));
			outputText += " [ResWeight]" + Double.valueOf(twoDFormat.format(MaGateParam.cfmResCapabilityWeight));
			
			
			// All local matchmaker policies
			outputText += " \n All local Matchmaker Policies: " + MaGateParam.getAvailableMatchMakerPolicies();

			// job load, life duration
			outputText += "\n" + " [PerExp]Load: " + MaGateParam.gwaLoadinPercentage + "%";
			
			// dynamic arrived of jobs
			int jobArrived = MaGateParam.totalNumberOfJob - allStatus[0];
			// dynamic CE
			double dynamicCE = GlobalStorage.getDynamicDataset()[0];
			dynamicCE = dynamicCE * 100;
			
			outputText += "\n" + 
				" [PerExp]Number of arrived jobs/total jobs: " + jobArrived + 
				"/" + MaGateParam.totalNumberOfJob + 
				"; Real-time Community Utilization: " + dynamicCE + "%";
			
			outputText += "\n\n" +
				" [Avg]Jobs to process per node: " + (MaGateParam.totalNumberOfJob / numOfNode) + 
				"\n [Avg]Num.Arrived jobs: " + (jobArrived / numOfNode) + 
				" | [Avg]Num.Submitted jobs: " + (allStatus[1] / numOfNode) + 
				" | [Avg]Num.Scheduling jobs: " + (allStatus[2] / numOfNode) + 
				" | [Avg]Num.Processing jobs: " + (allStatus[3] / numOfNode) + 
				"\n [Avg]Num.Transferring jobs: " + (allStatus[4] / numOfNode) + 
				" | [Avg]Num.Executed jobs: " + (allStatus[5] / numOfNode) + 
				" | [Avg]Num.Suspended jobs: " + (allStatus[7] / numOfNode) + 
				" | [Avg]Num.Failed jobs: " + (allStatus[6] / numOfNode) 
				;
			
			// Network message overhead
			outputText += "\n\n" + 
				" CommunitySearch(query/feedback/update): " + GlobalStorage.count_communitySearch.get() + 
				"/" + GlobalStorage.count_communitySearchFeedback.get() + 
				"/" + GlobalStorage.count_updateNeighborhood.get();
			
			// CASP message statistic
			outputText += "\n Generated messages from this Sce-Iteration\n " +
				" [AvgNode]REQUEST-msg: " + (GlobalStorage.count_msgREQUEST.doubleValue() / numOfNode) + 
				" ACCEPT(for REQUEST)-msg: " + (GlobalStorage.count_msgACCEPT_REQUEST.doubleValue() / numOfNode) +
				" ASSIGN(for REQUEST)-msg: " + (GlobalStorage.count_msgASSIGN_REQUEST.doubleValue() / numOfNode) + 
				"\n [AvgNode]INFORM-msg: " + (GlobalStorage.count_msgINFORM.doubleValue() / numOfNode) + 
				" ACCEPT(for INFORM)-msg: " + (GlobalStorage.count_msgACCEPT_INFORM.doubleValue() / numOfNode) +
				" ASSIGN(for INFORM)-msg: " + (GlobalStorage.count_msgASSIGN_INFORM.doubleValue() / numOfNode);
			
			// CASP Errors
			outputText += "\n" +
				" [PerExp]Un-matched REQUEST Message: " + GlobalStorage.count_unmatchedREQUEST +  
				" [PerExp]Un-matched INFORM Message: " + GlobalStorage.count_unmatchedINFORM + 
				" [PerExp]Unexpected CASP behaviors: " + GlobalStorage.count_errorCASP;
			
			// CFC message
			outputText += "\n\n" +
				" [AvgNode]CFC-FreeNodeAdvertisment: " + (GlobalStorage.count_CFCFreePower.doubleValue() / numOfNode) + 
				"; CFC-NonFreeNodeInformation: " + (GlobalStorage.count_CFCNonfreepower.doubleValue() / numOfNode);
			
			// SQL test
			outputText += "\n\n [Tech-Total]\n" +
				" [Last-iteration]ModuleController visit: " + GlobalStorage.count_moduleController.get() + 
				"\n [Last-iteration]CASPController visit: " + GlobalStorage.count_caspController.get() + 
				"\n [Last-iteration]CASP REQUEST: " + GlobalStorage.count_casp_request_Controller.get() + 
				" ; CASP INFORM: " + GlobalStorage.count_casp_inform_Controller.get() + 
				" ; CASP NODE SELECTION: " + GlobalStorage.count_casp_selectnode_Controller.get() + 
				"\n [Last-iteration]CASP ACCEPT-REQUEST: " + GlobalStorage.count_casp_accept_req_Controller.get() + 
				" ; CASP ACCEPT-INFORM: " + GlobalStorage.count_casp_accept_inf_Controller.get() + 
				" ; CASP ACCEPT-NULL: " + GlobalStorage.count_casp_accept_null_Controller.get() + 
				"\n\n [Last-iteration]Update SQL from JobCenterManager: " + GlobalStorage.count_sql + 
				"; [Last-iteration]Query SQL from JobCenterManager: " + GlobalStorage.count_sql3 + 
				"; [Last-iteration]SQL operation from MaGateDB: " + GlobalStorage.count_sql2;
				
			
			GlobalStorage.count_moduleController.set(0);
			GlobalStorage.count_caspController.set(0);
			GlobalStorage.count_casp_request_Controller.set(0);
			GlobalStorage.count_casp_inform_Controller.set(0);
			GlobalStorage.count_casp_accept_req_Controller.set(0);
			GlobalStorage.count_casp_accept_inf_Controller.set(0);
			GlobalStorage.count_casp_accept_null_Controller.set(0);
			GlobalStorage.count_casp_selectnode_Controller.set(0);
			GlobalStorage.count_sql.set(0);
			GlobalStorage.count_sql2.set(0);
			GlobalStorage.count_sql3.set(0);
			
			
			outputText += "\n\n [Test-Count-Last-iteration]\n" + 
				" test 1: " + GlobalStorage.count_test1.get() + 
				" test 2: " + GlobalStorage.count_test2.get() + 
				" test 3: " + GlobalStorage.count_test3.get() + 
				" test 4: " + GlobalStorage.count_test4.get() + 
				"\n test 5: " + GlobalStorage.count_test5.get() + 
				" || test 6: " + GlobalStorage.count_test6.get() + 
				" test 7: " + GlobalStorage.count_test7.get() + 
				"\n Snapshot test 8: " + GlobalStorage.count_test8.get() +
				" TEMP Snapshot test 9: " + GlobalStorage.count_test9.get();
			
			GlobalStorage.count_test1.set(0);
			GlobalStorage.count_test2.set(0);
			GlobalStorage.count_test3.set(0);
			GlobalStorage.count_test4.set(0);
			GlobalStorage.count_test5.set(0);
			GlobalStorage.count_test6.set(0);
			GlobalStorage.count_test7.set(0);
				
			
			outputText += "\n\n-------------- ";
			System.out.println(outputText);
			
//			log.debug("GlobalStorage.test_counter_5 " + GlobalStorage.test_counter_5.get() + 
//					"\nGlobalStorage.test_counter_6 " + GlobalStorage.test_counter_6.get() + 
//					"\nGlobalStorage.test_counter_6_2 " + GlobalStorage.test_counter_6_2.get() + 
//					"\nGlobalStorage.test_counter_6_3 " + GlobalStorage.test_counter_6_3.get());
			
//			// Logging dynamic job arrival information
			this.printJobArrivalCE_dynamic();
			
		}
		
		
		/**
		 * Logging dynamic job arrival information into ./magateoutput/results/gwa-*.csv
		 * @deprecated 
		 */
		private void printJobArrivalCE_dynamic() {
			
//			double currentSysTime = 0.0;
//
//			// dynamic arrived of jobs
//			int arrivedJob = MaGateParam.totalNumberOfJob - JobCenterManager.sizeOfJob_byStatus(JobCenterManager.GENERATED);
//			int schedulingJob = JobCenterManager.sizeOfJob_byStatus(JobCenterManager.SCHEDULING);
//			// dynamic CE
//			double dynamicCE = GlobalStorage.getDynamicDataset()[0];
//			dynamicCE = dynamicCE * 100;
//			
//			// Print a mark after each 100 jobs
//			if(arrivedJob >= this.dynamicThreshold) {
//				
//				this.dynamicThreshold += this.iteration;
//				
//				currentSysTime = MaGateMediator.getSystemTime();
//				
//				double currentSysDate = currentSysTime / 86400.0;
//				
//				String dynamicOutputText = currentSysTime + ", " + currentSysDate + ", " + 
//					arrivedJob + ", " + schedulingJob + ", " + dynamicCE;
//				
//				MaGateToolkit.writeResult(ajceFile, dynamicOutputText);	
//				
//			}
			
		}

	}
	
	
	/**
	 * Recording real-time grid-scope community status 
	 * @author yehuang
	 */
	protected class MonitorPlotEvent implements Runnable {
		
		public MonitorPlotEvent() {
		}
		
		public void run() {
			GlobalStorage.recordLiveCommunityStatus();
		}

	}
	
	/**
	 * Simulator lifecycle controller (prolongation & termination for MaGateMessage.SimEndPolicy_enforcedHeartbeat)
	 * @author yehuang
	 */
	protected class MonitorLifecycleEvent implements Runnable {
		
		public MonitorLifecycleEvent() {
		}
		
		public void run() {
			this.lifecycleControl();
		}
		
		public void lifecycleControl() {
			
			int[] allStatus = JobCenterManager.sizeOfJob_allStatus();
			
			// dynamic arrived of jobs
			int jobArrived = MaGateParam.totalNumberOfJob - allStatus[0];
			
			int jobWithDeterminedStatus = allStatus[5] + allStatus[7] + allStatus[6];
			
			if(allStatus[0] == 0 && (jobArrived == jobWithDeterminedStatus)) {
				// indicate the ModuleController to stop the simulator
				Collection<MaGateEntity> magates = GlobalStorage.getAllMaGate();
				for(MaGateEntity magate : magates) {
					magate.getModuleController().terminateLifecycle();
				}
				
				return;
				
			} else if(allStatus[0] == 0 && allStatus[2] == 0 && allStatus[3] == 0) {
				// indicate the ModuleController to stop the simulator
				Collection<MaGateEntity> magates = GlobalStorage.getAllMaGate();
				for(MaGateEntity magate : magates) {
					magate.getModuleController().terminateLifecycle();
				}
				
				return;
			}
			
			if(MaGateParam.simEndPolicy == MaGateMessage.SimEndPolicy_enforcedHeartbeat) {
				
				// GENERATED = 0; SUBMITTED = 1; SCHEDULING = 2; PROCESSING = 3; TRANSFERRED = 4;
				// EXECUTED = 5; FAILED = 6; SUSPENDED = 7;
				
				
				Collection<MaGateEntity> nodes = GlobalStorage.getAllMaGate();
				
				if(allStatus[0] > 0 || allStatus[1] > 0 || allStatus[3] > MaGateParam.numberOfTotalNode) {
					
					for(MaGateEntity node : nodes) {
						node.getModuleController().prolongLifecycle();
					}
				}
				
				
				if(allStatus[0] == 0 && allStatus[1] == 0 && allStatus[3] == 0 ) {
					
					for(MaGateEntity node : nodes) {
						node.getModuleController().terminateLifecycle();
					}
				}
				
			}
			
		}

	}

}
