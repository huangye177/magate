package ch.hefr.gridgroup.magate.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.cfm.CFCPolicy;
import ch.hefr.gridgroup.magate.cfm.CFMPolicy;
import ch.hefr.gridgroup.magate.env.MaGateMessage;
import ch.hefr.gridgroup.magate.env.MaGateParam;
import ch.hefr.gridgroup.magate.env.MaGateProfile;
import ch.hefr.gridgroup.magate.storage.MaGateDB;

public class ExpOrganizer {

	private static Log log = LogFactory.getLog(ExpOrganizer.class);
	
	
	/**
	 * Get Grid5000 based scenario 
	 *         
	 * @param load
	 * @return
	 */
	public static Vector<ExpScenario> prepareGWADataset() {
	
		String gwaName = "";
		double gwaLoad = 0;
		
		Vector<ExpScenario> scenarios = new Vector<ExpScenario>();
		
		/****** Set up simulator configuration ******/
		MaGateParam.countOfExperiment = 1;
		
//		boolean referenceScenarioEnabled = true;     // To evaluate independent and centralized scenarios
		boolean referenceScenarioEnabled = false;    // NOT to evaluate reference scenarios
		
		/****** Topology node status ******/
//		int topologyNodeStatus = MaGateMessage.TopologyNodeStatus_AllStable;
		int topologyNodeStatus = MaGateMessage.TopologyNodeStatus_HalfLazy;
		
		/****** Set up CFM Policy Vectors ******/
		ConcurrentHashMap<String, String> cfmPolicy = null;
		Vector<ConcurrentHashMap<String, String>> cfmPolicyOptions = new Vector<ConcurrentHashMap<String, String>>();
		
//		//
//		cfmPolicy = null;
//		cfmPolicy = new ConcurrentHashMap<String, String>();
//		cfmPolicy.put(CFMPolicy.CFM_NONE, CFMPolicy.CFM_NONE);
//		cfmPolicyOptions.add(cfmPolicy);
//		
//		
//		//
//		cfmPolicy = null;
//		cfmPolicy = new ConcurrentHashMap<String, String>();
//		cfmPolicy.put(CFMPolicy.CFM_ToEx, CFMPolicy.CFM_ToEx);
//		cfmPolicyOptions.add(cfmPolicy);
		
		
		//
		cfmPolicy = null;
		cfmPolicy = new ConcurrentHashMap<String, String>();
		cfmPolicy.put(CFMPolicy.CFM_ReSe, CFMPolicy.CFM_ReSe);
		cfmPolicyOptions.add(cfmPolicy);
		
		
//		//
//		cfmPolicy = null;
//		cfmPolicy = new ConcurrentHashMap<String, String>();
//		cfmPolicy.put(CFMPolicy.CFM_ToEx, CFMPolicy.CFM_ToEx);
//		cfmPolicy.put(CFMPolicy.CFM_ReSe, CFMPolicy.CFM_ReSe);
//		cfmPolicyOptions.add(cfmPolicy);
		
		
		/****** Set up CFM parameters ******/
		
		double[] cfmRecentTimeBarrier     = new double[]{3600 * 6};
		double[] cfmLocalReputationWeight = new double[]{0.5};
		double[] cfmResCapabilityWeight   = new double[]{0.5};
//		double[] cfmLocalReputationWeight = new double[]{0.1, 0.5, 0.9};
//		double[] cfmResCapabilityWeight   = new double[]{0.1, 0.5, 0.9};
		
		
		int[] cfmFailedDelegationScores   = new int[]{-3};
		int[] cfmDelegationDueDefinitions = new int[]{3600 * 6};
//		int[] cfmFailedDelegationScores   = new int[]{-3, -10};
//		int[] cfmDelegationDueDefinitions = new int[]{3600 * 1, 3600 * 6};
		
		
		/****** Set up MatchMaker Policy Vectors ******/
		Vector<String[]> mmPolicyOptions = new Vector<String[]>();
		String[] available_mmPolicy = null;
		
//		MaGateMessage.PolicyEasyBF,
//		MaGateMessage.PolicySJF
//		MaGateMessage.PolicyFCFS
		
		// One LRMS algorithm
		available_mmPolicy = new String[] {
				MaGateMessage.PolicyFCFS
		};
		mmPolicyOptions.add(available_mmPolicy);
		
//		available_mmPolicy = new String[] {
//				MaGateMessage.PolicyEasyBF
//		};
//		mmPolicyOptions.add(available_mmPolicy);
//		
//		available_mmPolicy = new String[] {
//				MaGateMessage.PolicySJF
//		};
//		mmPolicyOptions.add(available_mmPolicy);
//		
//		// Two LRMS algorithms
//		available_mmPolicy = new String[] {
//				MaGateMessage.PolicyFCFS,
//				MaGateMessage.PolicyEasyBF
//		};
//		mmPolicyOptions.add(available_mmPolicy);
//		
//		available_mmPolicy = new String[] {
//				MaGateMessage.PolicyFCFS,
//				MaGateMessage.PolicySJF
//		};
//		mmPolicyOptions.add(available_mmPolicy);
//		
//		
//		available_mmPolicy = new String[] {
//				MaGateMessage.PolicyEasyBF,
//				MaGateMessage.PolicySJF
//		};
//		mmPolicyOptions.add(available_mmPolicy);
//		
//		// Three LRMS algorithms
//		available_mmPolicy = new String[] {
//				MaGateMessage.PolicyFCFS,
//				MaGateMessage.PolicyEasyBF,
//				MaGateMessage.PolicySJF
//		};
//		mmPolicyOptions.add(available_mmPolicy);

		
		/****** Set up dynamic scheduling parameters ******/
		boolean[] caspDynamicEnabled = new boolean[]{true};
//		boolean[] caspDynamicEnabled = new boolean[]{false, true};
		
		/****** Set up workload trace parameters ******/
		String[] gwaNameArray = new String[]{
//			MaGateProfile.gwaAuverGrid,
//			MaGateProfile.gwaSHARCNET,
//			MaGateProfile.gwaNorduGrid,
			MaGateProfile.gwaGrid5000
		};
		
		gwaLoad = 1;
		
		/****** Set up CASA Variables ******/
		double[] reschedulingOnAvgQueuingTime  = new double[]{1};
		double[] systemReschedulingCoefficient = new double[]{1};
		
		int[] jobDistMode = new int[]{
//				MaGateMessage.JobDistMode_CustomizedDist
				MaGateMessage.JobDistMode_FairDist
				};
		int[] systemIS = new int[]{
//				MaGateMessage.systemIS_Global
//				MaGateMessage.systemIS_Partial_ACO, 
				MaGateMessage.systemIS_Partial_SIM
				};
		
		
		/****** Set up global parameter for global read-only purpose ******/
		MaGateParam.gwaLoadName = gwaName;
		MaGateParam.gwaLoadinPercentage = gwaLoad;
		
		
		// Hint the user regarding the start of simulation
		System.out.print("MaGate simulation workload selection.\n" + "Selected workload: \"" + gwaName + 
				"\", selected load percentage: \"" + gwaLoad + "%\".");
		
		
		// Initialize scenario counter
		if(referenceScenarioEnabled) {
			MaGateParam.scenario_overall = gwaNameArray.length * 
				cfmPolicyOptions.size() * cfmRecentTimeBarrier.length * cfmLocalReputationWeight.length * cfmResCapabilityWeight.length *
				cfmFailedDelegationScores.length * cfmDelegationDueDefinitions.length * 
				mmPolicyOptions.size() * caspDynamicEnabled.length * (2 + reschedulingOnAvgQueuingTime.length * 
						systemReschedulingCoefficient.length * jobDistMode.length * systemIS.length);
		} else {
			MaGateParam.scenario_overall = gwaNameArray.length * 
				cfmPolicyOptions.size() * cfmRecentTimeBarrier.length * cfmLocalReputationWeight.length * cfmResCapabilityWeight.length *
				cfmFailedDelegationScores.length * cfmDelegationDueDefinitions.length * 
				mmPolicyOptions.size() * caspDynamicEnabled.length * (reschedulingOnAvgQueuingTime.length * 
						systemReschedulingCoefficient.length * jobDistMode.length * systemIS.length);
		}
		
		// GWA dataset loop start
		for(int gwaIndex = 0; gwaIndex < gwaNameArray.length; gwaIndex++) {
			
			// For different CFM variables
			for(int k1 = 0; k1 < cfmRecentTimeBarrier.length; k1++) {
				for(int k2 = 0; k2 < cfmLocalReputationWeight.length; k2++) {
					for(int k3 = 0; k3 < cfmResCapabilityWeight.length; k3++) {
						
						for(int k4 = 0; k4 < cfmFailedDelegationScores.length; k4++) {
							for(int k5 = 0; k5 < cfmDelegationDueDefinitions.length; k5++) {
								
								// For different CFM policies
								for(ConcurrentHashMap<String, String> cfm : cfmPolicyOptions) {
									
									// LRM policy loop start 
									for(String[] mmPolicy : mmPolicyOptions) {
										
										// Dynamic loop start
										for(int inDySch = 0; inDySch < caspDynamicEnabled.length; inDySch++) {
											
											// set GWA parameters
											gwaName = gwaNameArray[gwaIndex];
											MaGateParam.gwaLoadName = gwaName;
											
											/************************************ Reference Scenario (independent/centralized) Unit Start ************************************/
											
											if(referenceScenarioEnabled) {
												
												MaGateParam.scenario_current += 1;
												
												scenarios.add(new ExpScenario(gwaName, gwaLoad, 
														MaGateMessage.Res_Discovery_From_Direct_Neighbors, 5, 0, 
														MaGateMessage.Interaction_AgreementCall, cfm, 
														caspDynamicEnabled[inDySch], MaGateMessage.SimEndPolicy_natureHeartbeat, mmPolicy, 
														reschedulingOnAvgQueuingTime[0], systemReschedulingCoefficient[0], 
														MaGateMessage.SchedulingScheme_Independent, jobDistMode[0], systemIS[0], topologyNodeStatus, 
														cfmRecentTimeBarrier[k1], cfmLocalReputationWeight[k2], cfmResCapabilityWeight[k3], 
														cfmFailedDelegationScores[k4], cfmDelegationDueDefinitions[k5]));
												
												
												MaGateParam.scenario_current += 1;
												
												scenarios.add(new ExpScenario(gwaName, gwaLoad, 
														MaGateMessage.Res_Discovery_From_Direct_Neighbors, 5, 0, 
														MaGateMessage.Interaction_AgreementCall, cfm, 
														caspDynamicEnabled[inDySch], MaGateMessage.SimEndPolicy_natureHeartbeat, mmPolicy, 
														reschedulingOnAvgQueuingTime[0], systemReschedulingCoefficient[0], 
														MaGateMessage.SchedulingScheme_Centralized, jobDistMode[0], systemIS[0], topologyNodeStatus, 
														cfmRecentTimeBarrier[k1], cfmLocalReputationWeight[k2], cfmResCapabilityWeight[k3], 
														cfmFailedDelegationScores[k4], cfmDelegationDueDefinitions[k5]));
											}
											
											/************************************ Reference Scenario (independent/centralized) Unit End ************************************/
											
											
											/************************************ CASA-related Scenario Unit Start ************************************/
											
											for(int i1 = 0; i1 < reschedulingOnAvgQueuingTime.length; i1++) {
												for(int i2 = 0; i2 < systemReschedulingCoefficient.length; i2++) {
													for(int i3 = 0; i3 < jobDistMode.length; i3++) {
														for(int i4 = 0; i4 < systemIS.length; i4++) {
															
															MaGateParam.scenario_current += 1;
															
															scenarios.add(new ExpScenario(gwaName, gwaLoad, 
																	MaGateMessage.Res_Discovery_From_Direct_Neighbors, 5, 0, 
																	MaGateMessage.Interaction_AgreementCall, cfm, 
																	caspDynamicEnabled[inDySch], MaGateMessage.SimEndPolicy_natureHeartbeat, mmPolicy, 
																	reschedulingOnAvgQueuingTime[i1], systemReschedulingCoefficient[i2], 
																	MaGateMessage.SchedulingScheme_Decentralized, jobDistMode[i3], systemIS[i4], topologyNodeStatus, 
																	cfmRecentTimeBarrier[k1], cfmLocalReputationWeight[k2], cfmResCapabilityWeight[k3], 
																	cfmFailedDelegationScores[k4], cfmDelegationDueDefinitions[k5]));
															
														}
													}
												}
											}
											
											/************************************ CASA-related Scenario Unit End ************************************/
											
										} // Dynamic array loop end
										
									} // LRM policy array loop end 
									
								} // CFM policies loop end
							}
						}
					}
				} 
			} // end of CFM variables
			
		} // GWA dataset array loop end
		
		return scenarios;
	}
	
	/**
	 * Get customized scenario
	 * @return
	 */
//	public static Vector<ExpScenario> getCumtomizedDataset() {
//		
//		Vector<ExpScenario> scenarios = new Vector<ExpScenario>();
//		
//		// define parameters
//		ConcurrentHashMap<String, String> cfcPolicy = null;
//		
//		/************************************ Scenario Unit Start ************************************/
//		cfcPolicy = new ConcurrentHashMap<String, String>();
//		cfcPolicy.put(CFCPolicy.CFC_NONE, CFCPolicy.CFC_NONE);
//		boolean caspEnabled = true;
//		int simEndPolicy = MaGateMessage.SimEndPolicy_enforcedHeartbeat;
//		String[] available_mathmakerPolicy = {
////				MaGateMessage.PolicyEDF,
////				MaGateMessage.PolicySJF,
//				MaGateMessage.PolicyFCFS
//		};
//		
//		// numberOfNode, numberOfVO, inputNodePerVO, numberOfTotalJob, 
//		// resDiscoveryProtocol, sizeOfCommunityJobQueue, timeSearchCommunity, interactionApproach, cfcPolicy, caspPolicy
//		scenarios.add(new ExpScenario(75, 1, 5, 7500, 
//				MaGateMessage.Res_Discovery_From_Direct_Neighbors, 5, 0, 
//				MaGateMessage.Interaction_AgreementCall, 
//				cfcPolicy, caspEnabled, simEndPolicy, available_mathmakerPolicy));
//		/************************************ Scenario Unit End ************************************/
//		
//		return scenarios;
//	}
	
	
	/**
	 * Initialize the system environment for getting experiment results
	 */
	public static void systemInitialization() {
		
		// getting parameter for simulation
		Properties props = new Properties();
		
		boolean inputReceived = false;
		while (!inputReceived) {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("\nPlease input the configuration file name within directory: " +
					"[configurationFile].properties (withOUT the suffix .properties), or simply return with default setting: ");
			
			try {
				String inputContent = in.readLine();
				
				if(inputContent == null || inputContent.trim().equals("")) {
					log.info("Using default setting of program");
					inputReceived = true;
					break;
				}
				
				File conf = new File(MaGateProfile.inputLocation() + inputContent + ".properties");
				
				boolean confExist = conf.exists();
				
				// Only need to create output dir if not exists
				if (confExist) {
					System.out.println("Using configuration file: " + conf.getPath());
					props.load(new FileInputStream(conf));
					
					// set parameters for simulation
					
//					MaGateParam.baudRate = Long.parseLong(props.getProperty("baudRate"));
//					MaGateParam.totalMachinePerResource = Integer.parseInt(props.getProperty("totalMachinePerResource"));
					MaGateParam.countOfExperiment       = Integer.parseInt(props.getProperty("countOfExperiment"));
					MaGateParam.countOfAlgorithm        = Integer.parseInt(props.getProperty("countOfAlgorithm"));
					MaGateParam.numberOfTotalNode = Integer.parseInt(props.getProperty("numOfSimultaneousMaGate"));
//					MaGateParam.numOfPE_eachResource    = Integer.parseInt(props.getProperty("numOfPE_eachResource"));
					MaGateParam.numOfRes_eachMaGate     = Integer.parseInt(props.getProperty("numOfRes_eachMaGate"));
//					MaGateParam.numOfPE_requestedByBadJob = Integer.parseInt(props.getProperty("numOfPE_requestedByBadJob"));
					
//					MaGateParam.numOfJob = Integer.parseInt(props.getProperty("numOfJob"));
//					MaGateParam.peMIPS   = Integer.parseInt(props.getProperty("peMIPS"));
					
//					MaGateParam.res_archType = props.getProperty("res_archType");
//					MaGateParam.res_osType   = props.getProperty("res_osType");
//					MaGateParam.job_archType = props.getProperty("job_archType");
//					MaGateParam.job_osType   = props.getProperty("job_osType");
					MaGateParam.resDiscoveryProtocol          = props.getProperty("resDiscoveryProtocol");
					MaGateParam.delegationNegotiationProtocol = props.getProperty("delegationNegotiationProtocol");
					MaGateParam.negotiationAgreement          = props.getProperty("negotiationAgreement");
					
					
//					MaGateParam.job_badRate      = Double.parseDouble(props.getProperty("job_badRate"));
//					MaGateParam.delay            = Double.parseDouble(props.getProperty("delay"));
					
					
//					MaGateParam.numOfPE_perResource_minRage = Double.parseDouble(props.getProperty("numOfPE_eachResource_ParallelMaGate_minRage"));
//					MaGateParam.numOfPE_perResource_maxRage = Double.parseDouble(props.getProperty("numOfPE_eachResource_ParallelMaGate_maxRage"));
					
					MaGateParam.timeSearchCommunity       = Integer.parseInt(props.getProperty("timeSearchCommunity"));
					
					log.info("Using specific configuration properties");
					inputReceived = true;
					
				} else {
					System.out.println("The input file doesn't exist, please check and try again");
				}
			
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @deprecated 
	 */
	public static Vector<ExpScenario> getScenarioFromDB() {
		
		return MaGateDB.getExperimentScenariosFromDB();
		
	}
	
	/**
	 * @deprecated
	 */
	public static void getScenarioFromProfile() {
		
//		// Method 1
//		File propertyFile = new File("./", "magate.properties");
//		System.out.println("properties location: " + propertyFile.getAbsolutePath());
//		PropertyConfigurator.configure(propertyFile.getPath());
//		
//		String sConfigFile = "log4j.properties";
//		
//		
//		// Method 2
//		InputStream in = MaGateTrunk.class.getClassLoader().getResourceAsStream(sConfigFile);
//		if (in == null) {
//			log.warn("magate.properties not found.");
//		}
//		Properties props = new java.util.Properties();
//		try {
//			props.load(in);
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
	}
	
}
