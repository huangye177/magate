package ch.hefr.gridgroup.magate;

import java.util.*;
import java.io.*;
import java.net.URL;

import eduni.simjava.Sim_system;
import gridsim.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import ch.hefr.gridgroup.magate.em.ssl.*;
import ch.hefr.gridgroup.magate.env.*;
import ch.hefr.gridgroup.magate.input.*;
import ch.hefr.gridgroup.magate.km.MaGateMonitor;
import ch.hefr.gridgroup.magate.model.*;
import ch.hefr.gridgroup.magate.storage.*;
import ch.hefr.gridgroup.magate.model.*;
import ch.hefr.gridgroup.magate.plot.PlotManager;

/**
 * Class MaGateTrunk
 * <p>
 * This is the main class of a MaGate node, it creates all the necessary
 * entities, including JobSubmitter, LRM, LRMStorage, MatchMaker, etc. <br>
 * 
 * Each MaGateTrunk is allowed to run multiple times with diverse parameters,
 * such as imported job files, specific scheduling algorithm, etc. After each
 * loop of finished jobs, information are collected, including resource
 * makespan, total tardiness, number of delayed/non-delayed jobs, etc.
 * 
 * @author Ye HUANG
 */
@SuppressWarnings("unused")
public class MaGateTrunk {

	private static Log log = LogFactory.getLog(MaGateTrunk.class);
	
	/** @deprecated */
	private static String selectedWorkload = "";
	/** @deprecated */
	private static double selectedJobload = 0;
	
	/**
	 * The main method - create all entities and starts the simulation. <br>
	 * It is also capable of multiple starts of the simulation with different
	 * setup (machine count, job parameters, time distribution interval).
	 */
	public static void main(String[] args) {
		
		/****************************************************************
		 * Prepare the experiment scenarios and launch the simulation
		 ****************************************************************/
		MaGateDB.dbSetup();
		MaGateDB.initTBs();
		
		/***************************************************************************************************
		 * Start MaGate Simulation Platform with parameters defined via workload Grid5000
		 ***************************************************************************************************/
		GlobalStorage.setExperimentScenarios(ExpOrganizer.prepareGWADataset());
		MaGateTrunk trunk = new MaGateTrunk();
		
	} 
	
	/**
	 * Launching the simulation
	 */
	public MaGateTrunk() {
		
		log.info("MaGate experiment Starting...");
		GlobalStorage.systemStartTime = new Date();
		
		// Initialize the network infrastructure 
		MaGateInfrastructure maGateInfra = new MaGateInfrastructure();
		
		// prepare for recording statistic data of each scenario
		MaGateDB.initScenarioStatisticTB(GlobalStorage.getExperimentScenarios());
		
		// clean the results generate from previous execution
		MaGatePlatform.systemFlush();
		
		/**************************************************************************************************
		 *  Setup repositories to make sure the same MaGate configuration in all scenarios and iterations
		 **************************************************************************************************/
		
		for(ExpScenario scenario : GlobalStorage.getExperimentScenarios()) {
		
			/***********************************
			 * Enter loop of individual scenario 
			 ***********************************/
			
			scenario.lauchScenarioData();
			
			// initialize JobQueue Manager which holds all jobs's information of each experiment scenario
			JobCenterManager.setup();
			MaGatePlatform.scenarioFlush();
			
			
			try {
				// (Re) Initialize the parameters
				MaGateParam.numberOfTotalNode     = scenario.getNumberOfNode();
				MaGateParam.numOfInputJob_perNode = scenario.getNumberOfTotalJob() / scenario.getNumberOfNode();
				MaGateParam.currentScenarioId     = scenario.getScenarioId();
				
				MaGateParam.resDiscoveryProtocol    = scenario.getResDiscoveryProtocol();
				MaGateParam.sizeOfCommunityJobQueue = scenario.getSizeOfCommunityJobQueue();
				MaGateParam.timeSearchCommunity     = scenario.getTimeSearchCommunity();
				MaGateParam.interactionApproach     = scenario.getInteractionApproach();
				
				MaGateParam.CFM_POLICY              = scenario.getCFMPolicy();
				
				MaGateParam.dynamicEnabled      = scenario.isCaspDynamicEnabled();
				MaGateParam.simEndPolicy            = scenario.getSimEndPolicy();
				
				MaGateParam.systemReschedulingOnAvgQueuingTime = scenario.systemReschedulingOnAvgQueuingTime;
				MaGateParam.systemReschedulingCoefficient = scenario.systemReschedulingCoefficient;
				MaGateParam.schedulingScheme = scenario.schedulingScheme;
				MaGateParam.jobDistMode = scenario.jobDistMode;
				MaGateParam.systemIS = scenario.systemIS;
				
				MaGateParam.topologyStatus = scenario.getTopologyNodeStatus();
				
				// sent CFM variables into MaGateParam as the general variables
				MaGateParam.cfmRecentTimeBarrier     = scenario.getCfmRecentTimeBarrier();
				MaGateParam.cfmLocalReputationWeight = scenario.getCfmLocalReputationWeight();
				MaGateParam.cfmResCapabilityWeight   = scenario.getCfmResCapabilityWeight();
				
				MaGateParam.cfmDelegationFailScore = scenario.getCfmFailedDelegationScores();
				MaGateParam.cfmJobReturnDeadline   = scenario.getCfmDelegationDueDefinitions();
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			/************************************************
			 *  ye: One Scenario (Multi-iterations) Started!
			 ************************************************/
			
			LinkedList<MaGateStorage> resultPerScenario = simulationScenario(maGateInfra, scenario);
			
			/************************************************
			 *  ye: One Scenario (Multi-iterations) Finished!
			 ************************************************/
			
			// Print experiment results for each scenario
			log.info("MaGate experiment completed! Caculating the result & statistic data... ...");
			
//			log.info("####### 2 gwa Name: " + MaGateParam.gwaLoadName);
			MaGateRecorder.printCommunityStatus_perScenario(resultPerScenario, scenario.getScenarioId());
			MaGateDWRecorder.recordPerScenarioCommunityStatus();
			
			PlotManager plotManager = new PlotManager();
			plotManager.printPlotter_perScenario();
			plotManager = null;
			
			// Record community statistic data of each Scenario into DB
			MaGateDWRecorder.recordScenarioStatistic();
			resultPerScenario = null;
			GlobalStorage.clearCounterPerScenario();
			
			// shutdown JobQueueManager
			JobCenterManager.shutdown();
			
			/***********************************
			 * Exit loop of individual scenario 
			 ***********************************/
		}
		
		maGateInfra = null;
		
		double infraFeedbackPerIteration = GlobalStorage.count_communitySearch.get() / MaGateParam.countOfExperiment;
		log.info("Infrastructure feedback counter: " + GlobalStorage.count_communitySearch.get() + 
				" ( " + infraFeedbackPerIteration + " per Experiment Iteration). ");
		log.info("MaGate experiment result & statistic data COMPLETE !");
		
		MaGateDB.dbShutdown();
		System.exit(0);
		
	}
	
	
	
	/** 
	 * Scenario simulated job remotely execution simple success 
	 * EACH scenario contains a set of experiments
	 */
	private static LinkedList<MaGateStorage> simulationScenario(MaGateInfrastructure maGateInfra, ExpScenario scenario) {
	
		// NOTICE: in case of only one JobSubmitter in the Grid, the delay
		// is not very important, otherwise for synch.
		
		LinkedList<MaGateStorage> resultPerScenario = new LinkedList<MaGateStorage>();
		
		/**************************************************
		 * LOOP START : multi-experiments of each scenario
		 **************************************************/
		// ye: repeat the experiment for avg. result
		for (int indexOfExperiment = 0; indexOfExperiment < MaGateParam.countOfExperiment; indexOfExperiment++) {
			
			MaGateParam.availableMatchmakerPolicy.clear();
			
			MaGateParam.currentExperimentIndex = indexOfExperiment;
			MaGateParam.currentIterationId = MaGateParam.currentScenarioId + 
				"-[Ite]" + (indexOfExperiment + 1) + ":" + MaGateParam.countOfExperiment;
			
			scenario.lauchScenarioData();
			
			// clean database for new communtiy-status data
			// clean JobCenter on memory and database storage
			// clear counters from previous experiment(s)
//			JobCenterManager.setup();
			MaGateDWRecorder.flushPerIterationCommunityStatus();
			JobCenterManager.flush();
			GlobalStorage.clearCounterPerScenarioIteration();
			
			// start live screen for dynamic running status demonstration
			// Initialize the GUI handler
			PlotManager plotManager = new PlotManager();
			plotManager.animatorSetup();
			plotManager.screenPlotterSetup_perIteration();
			
			
			// initialize the simulation
			MaGatePlatform.initSimulation(MaGateParam.numberOfTotalNode);
			
			// result collector for each experiment iteration
			LinkedList<MaGateStorage> resultPerIteration = new LinkedList<MaGateStorage>();
			
			// ye: resource engine used for each simulation
			ResourceEngine resEngine = null;
			try {
				resEngine = new ResourceEngine("SimResourceEngine", MaGateProfile.bandWidth);
				
			} catch (Exception e1) {
				log.error("Unwanted errors happen in MaGateTrunk while preparing resource Engine");
				log.error(e1.toString());
				e1.printStackTrace();
			}
			
			// LOOP: multi-simultaneous MaGates
			// ye: generating all parallel MaGates within each experiment iteration
			System.out.println("\nGenerating " + scenario.getNumberOfNode() + " resources...");
			for (int index = 0; index < scenario.getNumberOfNode(); index++) {
				
				String t1 = new Integer(indexOfExperiment).toString();
				String t2 = new Integer(index).toString();
				String currentExperimentMark = t1 + "_" + t2;
				
				// fetch node profile
				ExpNode node = scenario.getNodeList().get(index);
				
				// calculate/update count of nodes using variety of localMatchMakerPolicies
				String nodeMMPolicy = node.getLocalMatchMakerPolicy();
				if(MaGateParam.availableMatchmakerPolicy.containsKey(nodeMMPolicy)) {
					Integer currentCounter = MaGateParam.availableMatchmakerPolicy.get(nodeMMPolicy);
					MaGateParam.availableMatchmakerPolicy.put(nodeMMPolicy, new Integer(currentCounter.intValue() + 1));
				} else {
					MaGateParam.availableMatchmakerPolicy.put(nodeMMPolicy, new Integer(1));
				}
				
				// generate MaGate node
				MaGateEntity maGateInstance = new MaGateEntity(node.getNodeId(), index, indexOfExperiment, 
						resEngine, resultPerIteration, currentExperimentMark, maGateInfra, 
						node, scenario.getCFMPolicy(), nodeMMPolicy, node.getNodeStatus());
			}
			
			System.out.print("\nResources generated: " + MaGateParam.getAvailableMatchMakerPolicies());
			
			// Resource engine prepares resources for ALL participant MaGates.
			// This method has to be called after all MaGateEntities are created.
			resEngine.initResource();
			
			// Check global node network
			maGateInfra.selfCheck();
			
			// Start system monitor
			SystemMonitor systemMonitor = new SystemMonitor(scenario.getScenarioId() + MaGateParam.currentExperimentIndex);
			
			try {
				
				// Waiting for first system monitoring information print
				System.gc();
				Thread.sleep(5000);
				
				/********************************
				 * Start  simulation with multi-parallel MaGates
				 ********************************/
				GridSim.startGridSimulation();
				
			} catch (Exception e) {
				log.error("Unexpected errors happen in MaGateTrunk while running simultion");
				log.error(e.getMessage());
				
				// reset inner variables of the simulator
				Sim_system.setInComplete(true);
				resultPerIteration = null;
				resEngine.setResourceEngineReady(false);
				
				continue;
			}
			
			// Shutdown System monitor
			systemMonitor.terminateSystemMonitor();
			
			// Record monitoring data of each experimental iterations for each scenario
			GlobalStorage.recordCounterPerScenarioIteration();
			
			// Record community statistic data of each [Scenario-Iteration] into DB
			MaGateDWRecorder.recordPerScenarioIterationCommunityStatus();
			MaGateDWRecorder.recordScenarioIterationStatistic(resultPerIteration);
			
			// generate community status chart as archive
			plotManager.animatorShutdown();
			plotManager.screenPlotterShutdown_perIteration();
			plotManager.printPlotter_perIteration();
			plotManager = null;
			
			// preserve the results
			for(MaGateStorage callBackStorage : resultPerIteration) {
				resultPerScenario.offer(callBackStorage);
			}
			
			// Shut down Job and Resource storage/engine
//			JobCenterManager.shutdown();
			resEngine.setResourceEngineReady(false);
			
			resultPerIteration = null;
			resEngine = null;
			systemMonitor = null;
			
			// reset inner variables of the simulator
			Sim_system.setInComplete(true);
			
			// node leave network infrastructure 
			maGateInfra.allMaGateDetach();
			
			log.info("======================================== END OF TEST Iteration: " + indexOfExperiment + " =============================================");
			
		} // exit current experiment iteration
		
		/************************************************
		 * LOOP END : multi-experiments of each scenario
		 ************************************************/
		
		log.info("\n\n\n======================================== END OF SIMULATION (Per Scenario) " + "======================================== \n\n\n");
		
		log.info("=============== Final Simulation results ====================");
		log.info("Number of running MaGates : " + resultPerScenario.size() + "; Num of experiment: " 
				+ MaGateParam.countOfExperiment + "; Num of MaGates of each experiment: " + MaGateParam.numberOfTotalNode);
		
		return resultPerScenario;
		
	}
	
}


