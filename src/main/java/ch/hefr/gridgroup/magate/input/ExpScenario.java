package ch.hefr.gridgroup.magate.input;

import gridsim.GridSimRandom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.casa.CASAPolicy;
import ch.hefr.gridgroup.magate.env.MaGateMessage;
import ch.hefr.gridgroup.magate.env.MaGateParam;
import ch.hefr.gridgroup.magate.env.MaGateProfile;
import ch.hefr.gridgroup.magate.env.SimJobFactory;
import ch.hefr.gridgroup.magate.gui.StartGUI;
import ch.hefr.gridgroup.magate.model.Job;

public class ExpScenario {

	private static Log log = LogFactory.getLog(ExpScenario.class);

	private String scenarioId;
	
	private int numberOfNode;
	private int numberOfVO;
	private int inputNodePerVO;
	private int numberOfTotalJob;

	private String resDiscoveryProtocol;
	private int sizeOfCommunityJobQueue;
	private int timeSearchCommunity;
	private String interactionApproach;
	
	private ConcurrentHashMap<String, String> cfmPolicy;
//	private int caspPolicy;
	
	private Vector<ExpNode> nodeList = new Vector<ExpNode>();
	private Vector<ExpNode> nodeWithInputList = new Vector<ExpNode>();
	
	private boolean caspDynamicEnabled = true;
	private int simEndPolicy = MaGateMessage.SimEndPolicy_enforcedHeartbeat;

	private String gwaFlag = "customized";
	private double loadinDecimal;


	private String[] available_mathmakerPolicy;
	
	public double systemReschedulingOnAvgQueuingTime = 1;
	public double systemReschedulingCoefficient = 1;
	public int schedulingScheme = MaGateMessage.SchedulingScheme_Decentralized;
	public int jobDistMode = MaGateMessage.JobDistMode_CustomizedDist;
	public int systemIS    = MaGateMessage.systemIS_Partial_ACO;
	
	private int topologyNodeStatus  = MaGateMessage.TopologyNodeStatus_AllStable;
	private int nodeStatusGenerator = 0;

	private double cfmRecentTimeBarrier     = 3600 * 6;
	private double cfmLocalReputationWeight = 0.5;
	private double cfmResCapabilityWeight   = 0.5;
	private int cfmFailedDelegationScores   = -3;
	private int cfmDelegationDueDefinitions = 3600 * 1;

	/**
	 * Get scenarios from predefined database
	 * 
	 * @deprecated
	 * 
	 * @param scenarioId
	 * @param numberOfNode
	 * @param numberOfVO
	 * @param inputNodePerVO
	 * @param numberOfTotalJob
	 * @param resDiscoveryProtocol
	 * @param sizeOfCommunityJobQueue
	 * @param timeSearchCommunity
	 * @param interactionApproach
	 * @param cfcPolicy
	 * @param caspPolicy
	 */
	public ExpScenario(String scenarioId, int numberOfNode, int numberOfVO, 
			int inputNodePerVO, int numberOfTotalJob, 
			
			String resDiscoveryProtocol, int sizeOfCommunityJobQueue, int timeSearchCommunity, String interactionApproach, 
			ConcurrentHashMap<String, String> cfcPolicy) {
		
	}
	
	
	/**
	 * Customized Scenario with standard MaGateParam parameters
	 * 
	 * @deprecated
	 * 
	 * @param numberOfNode
	 * @param numberOfVO
	 * @param numberOfNodeWithInput
	 * @param resDiscoveryProtocol
	 * @param sizeOfCommunityJobQueue
	 * @param timeSearchCommunity
	 * @param interactionApproach
	 * @param cfcPolicy
	 * @param caspPolicy
	 */
//	public ExpScenario(int numberOfNode, int numberOfVO, 
//			int inputNodePerVO, int numberOfTotalJob, 
//			
//			String resDiscoveryProtocol, int sizeOfCommunityJobQueue, int timeSearchCommunity, String interactionApproach, 
//			ConcurrentHashMap<String, String> cfcPolicy,
//			boolean caspDynamicEnabled, int simEndPolicy, String[] available_mathmakerPolicy) {
//		
//		this.completeScenarioId(numberOfNode, numberOfTotalJob, numberOfVO, inputNodePerVO);
//		
//		this.resDiscoveryProtocol = resDiscoveryProtocol;
//		this.sizeOfCommunityJobQueue = sizeOfCommunityJobQueue;
//		this.timeSearchCommunity = timeSearchCommunity;
//		this.interactionApproach = interactionApproach;
//		
//		this.numberOfNode = numberOfNode;
//		this.numberOfVO = numberOfVO;
//		this.inputNodePerVO = inputNodePerVO;
//		this.numberOfTotalJob = numberOfTotalJob;
//		
//		this.cfcPolicy = cfcPolicy;
////		this.caspPolicy = caspPolicy;
//		
//		this.caspDynamicEnabled = caspDynamicEnabled;
//		this.simEndPolicy = simEndPolicy;
//		this.available_mathmakerPolicy = available_mathmakerPolicy;
//		
//		this.generateSimulatedScenario();
//		
//	}
	
	/**
	 * GWA based scenario
	 * 
	 * @param gwaFlag
	 * @param loadinPercentage
	 * @param exactEST
	 * @param resDiscoveryProtocol
	 * @param sizeOfCommunityJobQueue
	 * @param timeSearchCommunity
	 * @param interactionApproach
	 * @param cfcPolicy
	 * @param caspPolicy
	 * @param caspDynamicEnabled
	 * @param simEndPolicy
	 * @param available_mathmakerPolicy
	 */
	public ExpScenario(String gwaFlag, double loadinPercentage, 
			String resDiscoveryProtocol, int sizeOfCommunityJobQueue, int timeSearchCommunity, String interactionApproach, 
			ConcurrentHashMap<String, String> cfmPolicy, 
			boolean caspDynamicEnabled, int simEndPolicy, String[] available_mathmakerPolicy, 
			double reschedulingOnAvgQueuingTime, double systemReschedulingCoefficient, 
			int schedulingScheme, int jobDistMode, int systemIS, int topologyNodeStatus, 
			double cfmRecentTimeBarrier, double cfmLocalReputationWeight, double cfmResCapabilityWeight, 
			int cfmFailedDelegationScores, int cfmDelegationDueDefinitions) {
	
		this.resDiscoveryProtocol    = resDiscoveryProtocol;
		this.sizeOfCommunityJobQueue = sizeOfCommunityJobQueue;
		this.timeSearchCommunity     = timeSearchCommunity;
		this.interactionApproach     = interactionApproach;
	
		this.cfmPolicy  = cfmPolicy;
		
		this.caspDynamicEnabled = caspDynamicEnabled;
		this.simEndPolicy       = simEndPolicy;
		this.available_mathmakerPolicy = available_mathmakerPolicy;
		
		this.gwaFlag  = gwaFlag;
		this.loadinDecimal = loadinPercentage * 0.01;
		
		this.systemReschedulingOnAvgQueuingTime  = reschedulingOnAvgQueuingTime;
		this.systemReschedulingCoefficient = systemReschedulingCoefficient;
		this.schedulingScheme   = schedulingScheme;
		this.jobDistMode        = jobDistMode;
		this.systemIS           = systemIS;
		this.topologyNodeStatus = topologyNodeStatus;
		
		this.cfmRecentTimeBarrier        = cfmRecentTimeBarrier;
		this.cfmLocalReputationWeight    = cfmLocalReputationWeight;
		this.cfmResCapabilityWeight      = cfmResCapabilityWeight;
		this.cfmFailedDelegationScores   = cfmFailedDelegationScores;
		this.cfmDelegationDueDefinitions = cfmDelegationDueDefinitions;
		
		
		//Preparing scenario ID
		String idString = "";
		
		idString += "[Sce]" + MaGateParam.scenario_current + "_" + MaGateParam.scenario_overall + "-";
		// GWAFlag-ExactEST
		idString += "[Flag]" + this.gwaFlag + "-";
		
		// GWA Load
		idString += "[Load]" + (this.loadinDecimal * 100) + "%-";
		
		// Dynamic scheduling
		idString += "[DySch]" + this.caspDynamicEnabled + "-";
		
		// MatchMaker Policies
		String mmPolicies = "";
		for(int k = 0; k < available_mathmakerPolicy.length; k++) {
			String tempMMPolicy = (String) available_mathmakerPolicy[k];
			mmPolicies += tempMMPolicy.charAt(0);
		}
		idString += "[MM]" + mmPolicies + "-";
		
		// CASA Variables 
		if(this.schedulingScheme == MaGateMessage.SchedulingScheme_Independent) {
			idString += "[Scheme]Ind-";
		} else if (this.schedulingScheme == MaGateMessage.SchedulingScheme_Centralized) {
			idString += "[Scheme]Cen-";
		} else if (this.schedulingScheme == MaGateMessage.SchedulingScheme_Decentralized) {
			
			if(this.systemIS == MaGateMessage.systemIS_Global) {
				idString += "[Scheme]Dec-[IS]Glo-";
			} else if(this.systemIS == MaGateMessage.systemIS_Partial_ACO) {
				idString += "[Scheme]Dec-[IS]ACO-";
			} else if(this.systemIS == MaGateMessage.systemIS_Partial_SIM) {
				idString += "[Scheme]Dec-[IS]SIM-";
			}
		}
		
		idString += "[ResAvQ]" + this.systemReschedulingOnAvgQueuingTime + "-";
		idString += "[C_Res]" + this.systemReschedulingCoefficient + "-";
		
		// CFM Policy
		String cfmPolicyText = "";
		Iterator<Map.Entry<String, String>> iter = cfmPolicy.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String> entry = iter.next();
			String currentPolicy = entry.getValue().trim();
			cfmPolicyText += currentPolicy.substring(4) + ";";
		}
		idString += "[CFM]" + cfmPolicyText + "-";
	
		this.scenarioId = idString;
	}
	
	/**
	 * Load GWA data according to predefined GWA name and load
	 */
	public void lauchScenarioData() {
		
		this.nodeList.clear();
		this.nodeWithInputList.clear();
		this.nodeList = null;
		this.nodeWithInputList = null;
		System.gc();
		
		this.nodeList          = new Vector<ExpNode>();
		this.nodeWithInputList = new Vector<ExpNode>();
		
//		this.numberOfNode = 26;
//		this.numberOfVO = 1;
//		this.inputNodePerVO = 9;
//		this.numberOfTotalJob = (int) loadinDecimal * 1020195;
		
		
		// Generating corresponding jobs
		if(this.gwaFlag.equals(MaGateProfile.gwaCustomized)) {
			
			
		} else if (this.gwaFlag.equals(MaGateProfile.gwaCustomizedGUI)) {
			
			StartGUI.launch();
			
		} else if (this.gwaFlag.equals(MaGateProfile.gwaAuverGrid)) {
			
			this.numberOfVO = 1;
			this.numberOfTotalJob = (int) loadinDecimal * 404176;
			
			int maxNumPE = this.generateAuverGridResourceTopology();
			
			this.numberOfNode   = this.nodeList.size();
			this.inputNodePerVO = this.nodeWithInputList.size();
			
			this.generateAuverGridJobInput(loadinDecimal, maxNumPE);
			
		} else if (this.gwaFlag.equals(MaGateProfile.gwaSHARCNET)) {
			
			this.numberOfVO = 1;
			this.numberOfTotalJob = (int) loadinDecimal * 1195242;
			
			int maxNumPE = this.generateSHARCNETResourceTopology();
			
			this.numberOfNode   = this.nodeList.size();
			this.inputNodePerVO = this.nodeWithInputList.size();
			
			this.generateSHARCNETJobInput(loadinDecimal, maxNumPE);
			
		} else if (this.gwaFlag.equals(MaGateProfile.gwaGrid5000)) {
			
			this.numberOfVO = 1;
			this.numberOfTotalJob = (int) loadinDecimal * 1020195;
			
			int maxNumPE = this.generateGrid5000ResourceTopology();
			
			this.numberOfNode   = this.nodeList.size();
			this.inputNodePerVO = this.nodeWithInputList.size();
			
			this.generateGrid5000JobInput(loadinDecimal, maxNumPE);
			
		} else if (this.gwaFlag.equals(MaGateProfile.gwaNorduGrid)) {
			
			this.numberOfVO = 1;
			this.numberOfTotalJob = (int) loadinDecimal * 781370;
			
			int maxNumPE = this.generateNorduGridResourceTopology();
			
			this.numberOfNode   = this.nodeList.size();
			this.inputNodePerVO = this.nodeWithInputList.size();
			
			this.generateNorduGridJobInput(loadinDecimal, maxNumPE);
			
		} else {
			
			System.out.println("HALT: UNKNOWN GWA OR SIMULATED DATA SET");
			System.exit(0);
			
		}
		
		this.completeScenarioId(this.numberOfNode, this.numberOfTotalJob, this.numberOfVO, this.inputNodePerVO);
		String text = "\nTopology information (in total): " + this.nodeList.size() + " nodes; "  
			+ this.nodeWithInputList.size() + " nodes have job input (" + this.inputNodePerVO + " nodes per VO).";
		System.out.println(text);
		
	}
	
	
	/**
	 * Prepare jobs input for AuverGrid 
	 */
	private void generateAuverGridJobInput(double loadinDecimal, int maxNumPE) {
		
		this.numberOfTotalJob = 
			SimJobFactory.createGWAbasedJobDataset(loadinDecimal, this.nodeWithInputList, 
					MaGateProfile.gwaLocationAuverGrid, this.inputNodePerVO, maxNumPE);
		
		MaGateParam.totalNumberOfJob = this.numberOfTotalJob;
	}
	
	/**
	 * Prepare resource topology for AuverGrid 
	 * 
	 * AuverGrid workload, Date: Jan. 2006 - Jan. 2007
	 * 5 nodes, 475 PEs
	 * full load submitted jobs: 404176												
	 * 
	 *     NODE            | Number of PEs
	 * ---------------------------------------------------------------------------
	 *   clrlcgce01        |   112
	 *   clrlcgce02        |   84
	 *   clrlcgce03        |   186
	 *   iut15             |   38
	 *   obc               |   55   
	 * ---------------------------------------------------------------------------
	 *         5           |   475
	 *  
	 * @param numberOfTotalJob
	 */
	private int generateAuverGridResourceTopology() {
		
		this.clearNodeStatusGenerator();
		int maxNumPE = 186;
		
		ExpNode node_1 = new ExpNode("clrlcgce01", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 112, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_2 = new ExpNode("clrlcgce02", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 84, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_3 = new ExpNode("clrlcgce03", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 186, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_4 = new ExpNode("iut15", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 38, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_5 = new ExpNode("obc", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 55, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		this.nodeList.add(node_1);
		this.nodeList.add(node_2);
		this.nodeList.add(node_3);
		this.nodeList.add(node_4);
		this.nodeList.add(node_5);
		
		// OS setting
		MaGateParam.applyTOP500OSDistribution(this.nodeList);
		
		if(this.schedulingScheme == MaGateMessage.SchedulingScheme_Independent) {
			
			for(ExpNode node : this.nodeList) {
				this.nodeWithInputList.add(node);
			}
			
		} else if (this.schedulingScheme == MaGateMessage.SchedulingScheme_Centralized) {
			
			this.nodeWithInputList.add(node_1);
			
		} else if (this.schedulingScheme == MaGateMessage.SchedulingScheme_Decentralized) {
			
			if(this.jobDistMode == MaGateMessage.JobDistMode_FairDist) {
				
				for(ExpNode node : this.nodeList) {
					this.nodeWithInputList.add(node);
				}
				
			} else if (this.jobDistMode == MaGateMessage.JobDistMode_CustomizedDist) {
				
				this.nodeWithInputList.add(node_1);
				this.nodeWithInputList.add(node_2);
				
			}
		}
		
		int totalPE = 0;
		for(ExpNode node : this.nodeList) {
			totalPE += node.getNumberOfPEperResource();
		}
		maxNumPE = totalPE /this.nodeList.size();
		
		return maxNumPE;
		
	}
	
	
	/**
	 * Prepare jobs input for SHARCNET 
	 */
	private void generateSHARCNETJobInput(double loadinDecimal, int maxNumPE) {
		
		this.numberOfTotalJob = 
			SimJobFactory.createGWAbasedJobDataset(loadinDecimal, this.nodeWithInputList, 
					MaGateProfile.gwaLocationSHARCNET, this.inputNodePerVO, maxNumPE);
		
		MaGateParam.totalNumberOfJob = this.numberOfTotalJob;
	
	}
	
	/**
	 * Prepare resource topology for SHARCNET 
	 * 
	 * SHARCNET workload, Date: Dec. 2005 - Jan. 2007
	 * 10 nodes, 6828 PEs
	 * full load submitted jobs: 1195242
	 * 
	 *     NODE            | Number of PEs
	 * ---------------------------------------------------------------------------
	 *   bruce             |   128
	 *   narwhal           |   1068
	 *   tiger             |   128
	 *   bull              |   384
	 *   megaladon         |   128   
	 *   dolphin           |   128
	 *   requin            |   1536
	 *   whale             |   3072
	 *   zebra             |   128
	 *   bala              |   128   
	 * ---------------------------------------------------------------------------
	 *         10           |   6828
	 *  
	 * @param numberOfTotalJob
	 */
	private int generateSHARCNETResourceTopology() {
		
		this.clearNodeStatusGenerator();
		int maxNumPE = 3072;
		
		ExpNode node_1 = new ExpNode("bruce", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 128, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_2 = new ExpNode("narwhal", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 1068, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_3 = new ExpNode("tiger", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 128, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_4 = new ExpNode("bull", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 384, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_5 = new ExpNode("megaladon", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 128, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_6 = new ExpNode("dolphin", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 128, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_7 = new ExpNode("requin", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 1536, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_8 = new ExpNode("whale", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 3072, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_9 = new ExpNode("zebra", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 128, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_10 = new ExpNode("bala", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 128, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		this.nodeList.add(node_1);
		this.nodeList.add(node_2);
		this.nodeList.add(node_3);
		this.nodeList.add(node_4);
		this.nodeList.add(node_5);
		this.nodeList.add(node_6);
		this.nodeList.add(node_7);
		this.nodeList.add(node_8);
		this.nodeList.add(node_9);
		this.nodeList.add(node_10);
		
		// OS setting
		MaGateParam.applyTOP500OSDistribution(this.nodeList);
		
		if(this.schedulingScheme == MaGateMessage.SchedulingScheme_Independent) {
			
			for(ExpNode node : this.nodeList) {
				this.nodeWithInputList.add(node);
			}
			
		} else if (this.schedulingScheme == MaGateMessage.SchedulingScheme_Centralized) {
			
			this.nodeWithInputList.add(node_1);
			
		} else if (this.schedulingScheme == MaGateMessage.SchedulingScheme_Decentralized) {
			
			if(this.jobDistMode == MaGateMessage.JobDistMode_FairDist) {
				
				for(ExpNode node : this.nodeList) {
					this.nodeWithInputList.add(node);
				}
				
			} else if (this.jobDistMode == MaGateMessage.JobDistMode_CustomizedDist) {
				
				this.nodeWithInputList.add(node_1);
				this.nodeWithInputList.add(node_2);
				this.nodeWithInputList.add(node_3);
				this.nodeWithInputList.add(node_4);
				this.nodeWithInputList.add(node_5);
				
			}
			
		}
		
		int totalPE = 0;
		for(ExpNode node : this.nodeList) {
			totalPE += node.getNumberOfPEperResource();
		}
		maxNumPE = totalPE /this.nodeList.size();
		
		return maxNumPE;
	}
	
	/**
	 * Prepare jobs input for Grid5000 
	 */
	private void generateGrid5000JobInput(double loadinDecimal, int maxNumPE) {
		
		this.numberOfTotalJob = 
			SimJobFactory.createGWAbasedJobDataset(loadinDecimal, this.nodeWithInputList, 
					MaGateProfile.gwaLocationGrid5000, this.inputNodePerVO, maxNumPE);
		
		MaGateParam.totalNumberOfJob = this.numberOfTotalJob;
	}
	
	/**
	 * Prepare resource topology for Grid5000 
	 * 
	 * Grid 5000 workload, Date: 1 Apr. 2010	
	 * 9 sites, 26 nodes
	 * full load submitted jobs: 1020195
	 * ref: https://www.grid5000.fr/mediawiki/index.php/Special:G5KHardware				
	 * 
	 * Processors total: 3194 (Intel Xeon 1152 / AMD Opteron 2042), 
	 * Some conflicts happen between statistic data and detailed site data.							
	 * Muti-core is NOT considered. Service nodes and storage nodes are NOT considered										
	 * 
	 * 	        AMD	| INTEL  | TOTAL |          PE	NODE(PE)         | NumOfNode
	 * ---------------------------------------------------------------------------
	 * Orsay	684	|   x    |  684  |	   60(A), 372(A), 252(A)     |   3
	 * Grenoble	 x  |   68   |	68	 |            68(I)              |   1
	 * Lyon	    252	|	x    |  252  |	       112(A), 140(A)	     |   2
	 * Rennes	 x  |	376	 |  376	 |  50(I), 128(I), 66(I), 132(I) |   4
	 * Sophia	310	|   90	 |  400	 |  98(A), 112(A), 100(A), 90(I) |	 4
	 * Bordeaux	322	|  102	 |  424	 |  96(A), 102(I), 186(A), 40(A) |	 4
	 * Lille	198	|   92	 |  290	 |  106(A), 40(A), 52(A), 92(I)	 |   4
	 * Nancy	 x  |   424  |	424	 |         184(I), 240(I)	     |   2
	 * Toulouse	276	|    x	 |  276	 |         116(A), 160(A)	     |   2
	 * ---------------------------------------------------------------------------
	 *         2042 |   1152 |  3194 |		                         |  26
	 *  
	 * @param numberOfTotalJob
	 */
	private int generateGrid5000ResourceTopology() {
		
		this.clearNodeStatusGenerator();
		int maxNumPE = 372;
		
		// Generating resource infrastructure
		// site 1
		ExpNode node_Orsay1 = new ExpNode("Orsay1", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 60, MaGateProfile.peMIPS, "vo1", MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_Orsay2 = new ExpNode("Orsay2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 372, MaGateProfile.peMIPS, "vo1", MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_Orsay3 = new ExpNode("Orsay3", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 252, MaGateProfile.peMIPS, "vo1", MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		this.nodeList.add(node_Orsay1);
		this.nodeList.add(node_Orsay2);
		this.nodeList.add(node_Orsay3);
		
		
		// site 2
		ExpNode node_Grenoble1 = new ExpNode("Grenoble1", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 68, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		this.nodeList.add(node_Grenoble1);
		
		
		// site 3
		ExpNode node_Lyon1 = new ExpNode("Lyon1", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 112, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_Lyon2 = new ExpNode("Lyon2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 140, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		this.nodeList.add(node_Lyon1);
		this.nodeList.add(node_Lyon2);
		
		
		// site 4
		ExpNode node_Rennes1 = new ExpNode("Rennes1", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 50, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_Rennes2 = new ExpNode("Rennes2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 128, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_Rennes3 = new ExpNode("Rennes3", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 66, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_Rennes4 = new ExpNode("Rennes4", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 132, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		this.nodeList.add(node_Rennes1);
		this.nodeList.add(node_Rennes2);
		this.nodeList.add(node_Rennes3);
		this.nodeList.add(node_Rennes4);
		
		
		// site 5
		ExpNode node_Sophia1 = new ExpNode("Sophia1", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 98, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_Sophia2 = new ExpNode("Sophia2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 112, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_Sophia3 = new ExpNode("Sophia3", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 100, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_Sophia4 = new ExpNode("Sophia4", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 90, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		this.nodeList.add(node_Sophia1);
		this.nodeList.add(node_Sophia2);
		this.nodeList.add(node_Sophia3);
		this.nodeList.add(node_Sophia4);
		
		
		// site 6
		ExpNode node_Bordeaux1 = new ExpNode("Bordeaux1", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 96, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_Bordeaux2 = new ExpNode("Bordeaux2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 102, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_Bordeaux3 = new ExpNode("Bordeaux3", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 186, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_Bordeaux4 = new ExpNode("Bordeaux4", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 40, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		this.nodeList.add(node_Bordeaux1);
		this.nodeList.add(node_Bordeaux2);
		this.nodeList.add(node_Bordeaux3);
		this.nodeList.add(node_Bordeaux4);
		
		
		// site 7
		ExpNode node_Lille1 = new ExpNode("Lille1", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 106, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_Lille2 = new ExpNode("Lille2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 40, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_Lille3 = new ExpNode("Lille3", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 52, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_Lille4 = new ExpNode("Lille4", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 92, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		this.nodeList.add(node_Lille1);
		this.nodeList.add(node_Lille2);
		this.nodeList.add(node_Lille3);
		this.nodeList.add(node_Lille4);
		
		
		// site 8
		ExpNode node_Nancy1 = new ExpNode("Nancy1", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 184, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_Nancy2 = new ExpNode("Nancy2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 240, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		this.nodeList.add(node_Nancy1);
		this.nodeList.add(node_Nancy2);
		
		
		// site 9
		ExpNode node_Toulouse1 = new ExpNode("Toulouse1", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 116, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_Toulouse2 = new ExpNode("Toulouse2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 160, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		this.nodeList.add(node_Toulouse1);
		this.nodeList.add(node_Toulouse2);
		
		
		// OS setting
		MaGateParam.applyTOP500OSDistribution(this.nodeList);
		
		
		if(this.schedulingScheme == MaGateMessage.SchedulingScheme_Independent) {
			
			for(ExpNode node : this.nodeList) {
				this.nodeWithInputList.add(node);
			}
			
		} else if (this.schedulingScheme == MaGateMessage.SchedulingScheme_Centralized) {
			
			this.nodeWithInputList.add(node_Orsay1);
			
		} else if (this.schedulingScheme == MaGateMessage.SchedulingScheme_Decentralized) {
			
			if(this.jobDistMode == MaGateMessage.JobDistMode_FairDist) {
				
				for(ExpNode node : this.nodeList) {
					this.nodeWithInputList.add(node);
				}
				
			} else if (this.jobDistMode == MaGateMessage.JobDistMode_CustomizedDist) {
				
				this.nodeWithInputList.add(node_Orsay1);
				this.nodeWithInputList.add(node_Grenoble1);
				this.nodeWithInputList.add(node_Lyon1);
				this.nodeWithInputList.add(node_Rennes1);
				this.nodeWithInputList.add(node_Sophia1);
				this.nodeWithInputList.add(node_Bordeaux1);
				this.nodeWithInputList.add(node_Lille1);
				this.nodeWithInputList.add(node_Nancy1);
				this.nodeWithInputList.add(node_Toulouse1);
				
			}
			
		}
		
		int totalPE = 0;
		for(ExpNode node : this.nodeList) {
			totalPE += node.getNumberOfPEperResource();
		}
		maxNumPE = totalPE /this.nodeList.size();
		
		return maxNumPE;
	}
	
	
	/**
	 * Prepare jobs input for NorduGrid 
	 */
	private void generateNorduGridJobInput(double loadinDecimal, int maxNumPE) {
		
		this.numberOfTotalJob = 
			SimJobFactory.createGWAbasedJobDataset(loadinDecimal, this.nodeWithInputList, 
					MaGateProfile.gwaLocationNorduGrid, this.inputNodePerVO, maxNumPE);
		
		MaGateParam.totalNumberOfJob = this.numberOfTotalJob;
	}
	
	/**
	 * Prepare resource topology for NorduGrid 
	 * 
	 * AuverGrid workload, Date: 2004-2006 (unclear)
	 * 68 nodes, 4454 PEs
	 * full load submitted jobs: 781370												
	 * 
	 *     NODE            | Number of PEs
	 * ---------------------------------------------------------------------------
	 * 
	 * ATLAS 30 DistLab 9 Benedict 32 Horsehoe 561
	 * ATLAS Rep 30 DistLab Rep 9 Benedict Rep 32 Horsehoe Rep 561
	 * NBI 4 HEPAX1 1 Morpheus 18 Theory 104
	 * NBI Rep 4 HEPAX1 Rep 1 Morpheus Rep 18 Theory Rep 104
	 * VCR 1 CMS CERN 1 CMS test 1 Kirppu 1
	 * VCR Rep 1 CMS CERN Rep 1 CMS test Rep 1 Kirppu Rep 1
	 * Hirmu 16 Alpha 1 Parallab 58 Bergen 4
	 * Hirmu Rep 16 Alpha Rep 1 Parallab Rep 58 Bergen Rep 4
	 * Oslo 36 Gjovik 2 UPJS 1 SiGNET 42
	 * Oslo Rep 36 Gjovik Rep 2 UPJS Rep 1 SiGNET Rep 42
	 * Bluesmoke 100 Kosufy 66 Grendel 14 ISV 4
	 * Bluesmoke Rep 100 Kosufy Rep 66 Grendel Rep 14 ISV Rep 4
	 * Hagrid 94 Hive 99 Ingrid 101 Ingvar 31
	 * Hagrid Rep 94 Hive Rep 99 Ingrid Rep 101 Ingvar Rep 31
	 * Monolith 394 Quark 7 Seth 202 Beppe 92
	 * Monolith Rep 394 Quark Rep 7 Seth Rep 202 Beppe Rep 92
	 * Sigrid 99 HIP 1 Sigrid Rep 99 HIP Rep 1
	 * ---------------------------------------------------------------------------
	 *        68          |   4454
	 *  
	 * @param numberOfTotalJob
	 */
	private int generateNorduGridResourceTopology() {
		
		this.clearNodeStatusGenerator();
		int maxNumPE = 561;
		
		ExpNode node_01 = new ExpNode("ALTAS", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 30, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_02 = new ExpNode("DistLab", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 9, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_03 = new ExpNode("Benedict", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 32, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_04 = new ExpNode("Horsehoe", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 561, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_05 = new ExpNode("NBI", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 4, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_06 = new ExpNode("HEPAX1", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 1, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_07 = new ExpNode("Morpheus", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 18, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_08 = new ExpNode("Theory", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 104, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_09 = new ExpNode("VCR", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 1, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_10 = new ExpNode("CMSCERN", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 1, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_11 = new ExpNode("CMSTEST", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 1, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_12 = new ExpNode("Kirppu", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 1, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_13 = new ExpNode("Hirmu", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 16, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_14 = new ExpNode("Alpha", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 1, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_15 = new ExpNode("Parallab", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 58, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_16 = new ExpNode("Bergen", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 4, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_17 = new ExpNode("Oslo", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 36, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_18 = new ExpNode("Gjovik", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 2, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_19 = new ExpNode("UPJS", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 1, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_20 = new ExpNode("SiGNET", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 42, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_21 = new ExpNode("Bluesmoke", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 100, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_22 = new ExpNode("Kosufy", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 66, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_23 = new ExpNode("Grendel", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 14, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_24 = new ExpNode("ISV", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 4, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_25 = new ExpNode("Hagrid", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 94, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_26 = new ExpNode("Hive", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 99, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_27 = new ExpNode("Ingrid", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 101, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_28 = new ExpNode("Ingvar", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 31, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_29 = new ExpNode("Monolith", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 394, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_30 = new ExpNode("Quark", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 7, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_31 = new ExpNode("Seth", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 202, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_32 = new ExpNode("Beppe", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 92, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_33 = new ExpNode("Sigrid", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 99, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_34 = new ExpNode("HIP", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 1, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		
		
		
		//--- duplicated resoruces as below to reach a grid with 68 nodes
		
		ExpNode node_01_rep = new ExpNode("ALTAS2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 30, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_02_rep = new ExpNode("DistLab2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 9, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_03_rep = new ExpNode("Benedict2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 32, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_04_rep = new ExpNode("Horsehoe2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 561, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_05_rep = new ExpNode("NBI2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 4, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_06_rep = new ExpNode("HEPAX12", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 1, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_07_rep = new ExpNode("Morpheus2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 18, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_08_rep = new ExpNode("Theory2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 104, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_09_rep = new ExpNode("VCR2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 1, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_10_rep = new ExpNode("CMSCERN2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 1, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_11_rep = new ExpNode("CMSTEST2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 1, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_12_rep = new ExpNode("Kirppu2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 1, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_13_rep = new ExpNode("Hirmu2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 16, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_14_rep = new ExpNode("Alpha2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 1, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_15_rep = new ExpNode("Parallab2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 58, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_16_rep = new ExpNode("Bergen2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 4, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_17_rep = new ExpNode("Oslo2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 36, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_18_rep = new ExpNode("Gjovik2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 2, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_19_rep = new ExpNode("UPJS2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 1, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_20_rep = new ExpNode("SiGNET2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 42, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_21_rep = new ExpNode("Bluesmoke2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 100, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_22_rep = new ExpNode("Kosufy2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 66, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_23_rep = new ExpNode("Grendel2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 14, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_24_rep = new ExpNode("ISV2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 4, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		
		
		//----
		
		ExpNode node_25_rep = new ExpNode("Hagrid2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 94, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		
		ExpNode node_26_rep = new ExpNode("Hive2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 99, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_27_rep = new ExpNode("Ingrid2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 101, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_28_rep = new ExpNode("Ingvar2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 31, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_29_rep = new ExpNode("Monolith2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 394, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_30_rep = new ExpNode("Quark2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 7, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_31_rep = new ExpNode("Seth2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 202, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_32_rep = new ExpNode("Beppe2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 92, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_33_rep = new ExpNode("Sigrid2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 99, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());
		ExpNode node_34_rep = new ExpNode("HIP2", MaGateProfile.res_archType, MaGateParam.fetchFairDistributedOS(), 
				MaGateParam.numOfRes_eachMaGate, 1, MaGateProfile.peMIPS, "vo1", 
				MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy), this.generateNodeStatus());

		
		this.nodeList.add(node_01);
		this.nodeList.add(node_02);
		this.nodeList.add(node_03);
		this.nodeList.add(node_04);
		this.nodeList.add(node_05);
		this.nodeList.add(node_06);
		this.nodeList.add(node_07);
		this.nodeList.add(node_08);
		this.nodeList.add(node_09);
		this.nodeList.add(node_10);
		this.nodeList.add(node_11);
		this.nodeList.add(node_12);
		this.nodeList.add(node_13);
		this.nodeList.add(node_14);
		this.nodeList.add(node_15);
		this.nodeList.add(node_16);
		this.nodeList.add(node_17);
		this.nodeList.add(node_18);
		this.nodeList.add(node_19);
		this.nodeList.add(node_20);
		this.nodeList.add(node_21);
		this.nodeList.add(node_22);
		this.nodeList.add(node_23);
		this.nodeList.add(node_24);
		this.nodeList.add(node_25);
		this.nodeList.add(node_26);
		this.nodeList.add(node_27);
		this.nodeList.add(node_28);
		this.nodeList.add(node_29);
		this.nodeList.add(node_30);
		this.nodeList.add(node_31);
		this.nodeList.add(node_32);
		this.nodeList.add(node_33);
		this.nodeList.add(node_34);
		this.nodeList.add(node_01_rep);
		this.nodeList.add(node_02_rep);
		this.nodeList.add(node_03_rep);
		this.nodeList.add(node_04_rep);
		this.nodeList.add(node_05_rep);
		this.nodeList.add(node_06_rep);
		this.nodeList.add(node_07_rep);
		this.nodeList.add(node_08_rep);
		this.nodeList.add(node_09_rep);
		this.nodeList.add(node_10_rep);
		this.nodeList.add(node_11_rep);
		this.nodeList.add(node_12_rep);
		this.nodeList.add(node_13_rep);
		this.nodeList.add(node_14_rep);
		this.nodeList.add(node_15_rep);
		this.nodeList.add(node_16_rep);
		this.nodeList.add(node_17_rep);
		this.nodeList.add(node_18_rep);
		this.nodeList.add(node_19_rep);
		this.nodeList.add(node_20_rep);
		this.nodeList.add(node_21_rep);
		this.nodeList.add(node_22_rep);
		this.nodeList.add(node_23_rep);
		this.nodeList.add(node_24_rep);
		this.nodeList.add(node_25_rep);
		this.nodeList.add(node_26_rep);
		this.nodeList.add(node_27_rep);
		this.nodeList.add(node_28_rep);
		this.nodeList.add(node_29_rep);
		this.nodeList.add(node_30_rep);
		this.nodeList.add(node_31_rep);
		this.nodeList.add(node_32_rep);
		this.nodeList.add(node_33_rep);
		this.nodeList.add(node_34_rep);
		
		
		// OS setting
		MaGateParam.applyTOP500OSDistribution(this.nodeList);
		
		
		if(this.schedulingScheme == MaGateMessage.SchedulingScheme_Independent) {
			
			for(ExpNode node : this.nodeList) {
				this.nodeWithInputList.add(node);
			}
			
		} else if (this.schedulingScheme == MaGateMessage.SchedulingScheme_Centralized) {
			
			this.nodeWithInputList.add(node_01);
			
		} else if (this.schedulingScheme == MaGateMessage.SchedulingScheme_Decentralized) {
			
			if(this.jobDistMode == MaGateMessage.JobDistMode_FairDist) {
				
				for(ExpNode node : this.nodeList) {
					this.nodeWithInputList.add(node);
				}
				
			} else if (this.jobDistMode == MaGateMessage.JobDistMode_CustomizedDist) {
				
				this.nodeWithInputList.add(node_01);
				this.nodeWithInputList.add(node_02);
				this.nodeWithInputList.add(node_03);
				this.nodeWithInputList.add(node_04);
				this.nodeWithInputList.add(node_05);
				this.nodeWithInputList.add(node_06);
				this.nodeWithInputList.add(node_07);
				this.nodeWithInputList.add(node_08);
				this.nodeWithInputList.add(node_09);
				this.nodeWithInputList.add(node_10);
				this.nodeWithInputList.add(node_11);
				this.nodeWithInputList.add(node_12);
				this.nodeWithInputList.add(node_13);
				this.nodeWithInputList.add(node_14);
				this.nodeWithInputList.add(node_15);
				this.nodeWithInputList.add(node_16);
				this.nodeWithInputList.add(node_17);
				this.nodeWithInputList.add(node_18);
				this.nodeWithInputList.add(node_19);
				this.nodeWithInputList.add(node_20);
				this.nodeWithInputList.add(node_21);
				this.nodeWithInputList.add(node_22);
				this.nodeWithInputList.add(node_23);
				this.nodeWithInputList.add(node_24);
				this.nodeWithInputList.add(node_25);
				this.nodeWithInputList.add(node_26);
				this.nodeWithInputList.add(node_27);
				this.nodeWithInputList.add(node_28);
				this.nodeWithInputList.add(node_29);
				this.nodeWithInputList.add(node_30);
				this.nodeWithInputList.add(node_31);
				this.nodeWithInputList.add(node_32);
				this.nodeWithInputList.add(node_33);
				this.nodeWithInputList.add(node_34);
				
			}
			
		}
		
		int totalPE = 0;
		for(ExpNode node : this.nodeList) {
			totalPE += node.getNumberOfPEperResource();
		}
		maxNumPE = totalPE /this.nodeList.size();
		
		return maxNumPE;
		
	}
	
	private void clearNodeStatusGenerator() {
		this.nodeStatusGenerator = 0;
	}
	
	private int generateNodeStatus() {
		
		if(this.topologyNodeStatus == MaGateMessage.TopologyNodeStatus_AllStable) {
			return MaGateMessage.NodeStatus_AllStable;
			
		} else if (this.topologyNodeStatus == MaGateMessage.TopologyNodeStatus_HalfLazy) {
			
			if(this.nodeStatusGenerator % 2 == 0) {
				this.nodeStatusGenerator++;
				return MaGateMessage.NodeStatus_AllStable;
				
			} else {
				this.nodeStatusGenerator++;
				return MaGateMessage.NodeStatus_HalfLazy;
				
			}
			
		} else {
			return MaGateMessage.NodeStatus_AllStable;
		}
	}
	
	
	/********************************************************************************************************/
	/********************************************************************************************************/
	/********************************************************************************************************/
	
	
	/**
	 * Get the scenario id by compositing parameters
	 * 
	 * @param numTotalNode
	 * @param numTotalJob
	 * @param numTotalVO
	 * @param numInputNodePerVO
	 * @param mathmakerPolicy
	 * @param caspPolicy
	 * @param cfcPolicy
	 */
	private void completeScenarioId(int numTotalNode, int numTotalJob, int numTotalVO, int numInputNodePerVO) {
		
		// Format: GWAFlag-GWALoad-MatchMakerPolicy(MM)-CASP-CFM-N*-J*-V*-Vi*-Ite*
		String idString = "";
						
		// nodes, jobs, VOs, inputNodeofVOs
		idString += "[N]" + numTotalNode + "-";
		idString += "[J]" + numTotalJob + "-";
		idString += "[V]" + numTotalVO + "-";
		idString += "[Vi]" + numInputNodePerVO;
		
		this.scenarioId = this.scenarioId + idString;
		
	}
	
	/**
	 * Determine VoID according to total number of nodes, and total number of VOs
	 * Used while generating simulated resource topology
	 * 
	 * @param currentNode
	 * @param numberOfNode
	 * @param numberOfVO
	 * @return
	 */
	private String getVOId(int currentNode, int numberOfNode, int numberOfVO) {
		
		int numberOfNode_perSmallVO = numberOfNode / numberOfVO;
		int numberOfNode_perLargeVO = numberOfNode_perSmallVO + numberOfNode % numberOfVO;
		
		int voCouter = 0;
		String voId = "";
		if(currentNode % numberOfNode_perSmallVO == 0) {
			voCouter++;
		}
		
		if(voCouter <= numberOfVO) {
			voId = "vo" + voCouter;
		} else {
			voId = "vo1";
		}
		
		return voId;
		
	}
	
	/********************* Obstacle methods (Aug. 29th, 2010) **********************************/
	
	/**
	 * Generate simulated scenario dataset
	 */
//	private void generateSimulatedScenario() {
//
//		Random randomEngine = new Random();
//		
//		// check whether reasonable number of nodes/nodesWithJobInput
//		int allowedMaxNodePerVO = numberOfNode / numberOfVO;
//		if(allowedMaxNodePerVO < inputNodePerVO) {
//			log.error("Not enough node per VO for job input");
//			System.exit(0);
//		}
//		
//		HashMap<String, Vector<ExpNode>> voList = new HashMap<String, Vector<ExpNode>>();
//		
//		// Preparing node list 
//		for(int i = 0; i < numberOfNode; i++) {
//			
//			// set MaGate Id
//			String nodeId = java.util.UUID.randomUUID().toString();
//			
//			// set number of PEs on each MaGate resource
//			int numberOfPEperResource = (int) GridSimRandom.real(MaGateProfile.numOfPE_eachResource, 
//					MaGateProfile.numOfPE_perResource_minRage, 
//					MaGateProfile.numOfPE_perResource_maxRage, 
//					randomEngine.nextDouble());
//			
//			String voId = this.getVOId(i, numberOfNode, numberOfVO);
//			
//			// Generate node profile
//			ExpNode node = new ExpNode(nodeId, MaGateProfile.res_archType, MaGateParam.fetchTOP500ResOS(), 
//					MaGateParam.numOfRes_eachMaGate, numberOfPEperResource, MaGateProfile.peMIPS, voId, 
//					MaGateParam.fetchFairDistributedMatchmakerPolicy(this.available_mathmakerPolicy));
//			
//			if(!voList.containsKey(voId)) {
//				Vector<ExpNode> tempVt = new Vector<ExpNode>();
//				tempVt.add(node);
//				voList.put(voId, tempVt);
//				
//			} else {
//				Vector<ExpNode> tempVt = voList.get(voId);
//				tempVt.add(node);
//				voList.put(voId, tempVt);
//				
//			}
//			
//			this.nodeList.add(node);
//		}
//		
//		// Preparing node (with job input) list 
//		Collection<Vector<ExpNode>> nodeListperVO = voList.values();
//		
//		// List of VO(with multi-nodes) of the overall grid
//		for(Vector<ExpNode> nodeVector : nodeListperVO) {
//			
//			int inputNodeCounter = 0;
//			
//			// Vector of nodes of each VO
//			for(ExpNode node : nodeVector) {
//				
//				inputNodeCounter++;
//				
//				if(inputNodeCounter <= inputNodePerVO) {
//					this.nodeWithInputList.add(node);
//				} 
//				
//			}
//		}
//		
//		String text = "\nTopology information (in total): " + this.nodeList.size() + " nodes; " 
//			+ nodeListperVO.size() + " VOs; " 
//			+ this.nodeWithInputList + " nodes have job input (" + inputNodePerVO + " nodes per VO).";
//		
//		System.out.println(text);
//		
//		this.generateSimulateJobInput(numberOfTotalJob);
//	}
	
	
	/**
	 * Prepare jobs for nodes of the entire network
	 * 
	 * @param numberOfTotalJob
	 */
//	private void generateSimulateJobInput(int numberOfTotalJob) {
//		
//		int numberOfNodeWithJobInput = this.nodeWithInputList.size();
//		int numberOfJobPerNode = numberOfTotalJob / numberOfNodeWithJobInput;
//		
//		for(ExpNode node : this.nodeWithInputList) {
//			
//			ConcurrentLinkedQueue<Job> jobQueue = SimJobFactory.createSimJobListWithoutUser(numberOfJobPerNode, 
//					MaGateProfile.job_archType, MaGateParam.fetchTOP500ResOS(), MaGateProfile.job_badRate);
//			
//			node.setJobQueue(jobQueue);
//		}
//		
//		String text = "Job information (in total): " + numberOfTotalJob + " jobs are generated for ; " 
//			+ this.nodeWithInputList.size() + " nodes (approx. " + numberOfJobPerNode + " jobs per node)";
//	
//		System.out.println(text);
//	
//	}
	
	// -------- Getter / Setter --------
	
	
	public String getScenarioId() {
		return scenarioId;
	}


	public void setScenarioId(String scenarioId) {
		this.scenarioId = scenarioId;
	}


	public String getResDiscoveryProtocol() {
		return resDiscoveryProtocol;
	}


	public void setResDiscoveryProtocol(String resDiscoveryProtocol) {
		this.resDiscoveryProtocol = resDiscoveryProtocol;
	}


	public int getSizeOfCommunityJobQueue() {
		return sizeOfCommunityJobQueue;
	}


	public void setSizeOfCommunityJobQueue(int sizeOfCommunityJobQueue) {
		this.sizeOfCommunityJobQueue = sizeOfCommunityJobQueue;
	}


	public int getTimeSearchCommunity() {
		return timeSearchCommunity;
	}


	public void setTimeSearchCommunity(int timeSearchCommunity) {
		this.timeSearchCommunity = timeSearchCommunity;
	}


	public String getInteractionApproach() {
		return interactionApproach;
	}


	public void setInteractionApproach(String interactionApproach) {
		this.interactionApproach = interactionApproach;
	}


	public ConcurrentHashMap<String, String> getCFMPolicy() {
		return cfmPolicy;
	}


	public void setCFMPolicy(ConcurrentHashMap<String, String> cfmPolicy) {
		this.cfmPolicy = cfmPolicy;
	}

	public Vector<ExpNode> getNodeList() {
		return nodeList;
	}


	public void setNodeList(Vector<ExpNode> nodeList) {
		this.nodeList = nodeList;
	}


	public Vector<ExpNode> getNodeWithInputList() {
		return nodeWithInputList;
	}


	public void setNodeWithInputList(Vector<ExpNode> nodeWithInputList) {
		this.nodeWithInputList = nodeWithInputList;
	}

	public int getNumberOfNode() {
		return numberOfNode;
	}


	public void setNumberOfNode(int numberOfNode) {
		this.numberOfNode = numberOfNode;
	}


	public int getNumberOfVO() {
		return numberOfVO;
	}


	public void setNumberOfVO(int numberOfVO) {
		this.numberOfVO = numberOfVO;
	}
	
	public int getNumberOfTotalJob() {
		return numberOfTotalJob;
	}


	public void setNumberOfTotalJob(int numberOfTotalJob) {
		this.numberOfTotalJob = numberOfTotalJob;
	}
	
	public boolean isCaspDynamicEnabled() {
		return caspDynamicEnabled;
	}


	public int getSimEndPolicy() {
		return simEndPolicy;
	}
	
	public String[] getAvailableMathmakerPolicy() {
		return available_mathmakerPolicy;
	}


	public void setAvailableMathmakerPolicy(String[] availableMathmakerPolicy) {
		this.available_mathmakerPolicy = availableMathmakerPolicy;
	}
	
	public int getTopologyNodeStatus() {
		return topologyNodeStatus;
	}
	
	public double getCfmRecentTimeBarrier() {
		return cfmRecentTimeBarrier;
	}


	public double getCfmLocalReputationWeight() {
		return cfmLocalReputationWeight;
	}


	public double getCfmResCapabilityWeight() {
		return cfmResCapabilityWeight;
	}


	public int getCfmFailedDelegationScores() {
		return cfmFailedDelegationScores;
	}


	public int getCfmDelegationDueDefinitions() {
		return cfmDelegationDueDefinitions;
	}
	
}


