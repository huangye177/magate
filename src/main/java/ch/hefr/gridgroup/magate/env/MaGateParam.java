package ch.hefr.gridgroup.magate.env;

import gridsim.*;
import gridsim.net.Link;
import gridsim.net.SimpleLink;

import org.apache.commons.logging.LogFactory;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.OperatingSystemTypeEnumeration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.casa.CASAPolicy;
import ch.hefr.gridgroup.magate.cfm.CFCPolicy;
import ch.hefr.gridgroup.magate.input.ExpNode;
import ch.hefr.gridgroup.magate.model.Job;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class MaGateParam {

	private static Log log = LogFactory.getLog(MaGateParam.class);
	
	/******* private parameters needed to be controlled *******/
	private static long simSeed = 11L*12*13*14*15+34;
	
	public static double systemStartTime = -1.0;
//	public static AtomicBoolean systemStartTimeActivated = new AtomicBoolean(false);
	
	/******* Simulation setup *******/
	public static int numOfInputJob_perNode = 1;  // Default value
	public static int numOfRes_eachMaGate   = 1;  // Default value
	public static int numberOfTotalNode     = 1;  // Default value
	public static int countOfAlgorithm  = 0;
	
	/******* CASP parameters *******/
	public static boolean CASP_Enabled              = true;
	public static int INFORM_realTime_frequency     = 200;   // ms.
	public static int INFORM_simTime_frequency      = 60;   // s.
	
	/******* CFM parameters *******/
	public static boolean isolatedVO        = false;
	public static ConcurrentHashMap<String, String> CFM_POLICY = new ConcurrentHashMap<String, String>();
	
	/******* Experiment parameters *******/
	public static String currentScenarioId  = "unknown";
	public static String currentIterationId = "unknown";
	public static int currentExperimentIndex = 0;
	
	
	/******* SSL related parameters *******/
	public static int timeSearchCommunity = 250;
	public static String resDiscoveryProtocol = MaGateMessage.Res_Discovery_From_Direct_Neighbors;
//	public static String resDiscoveryProtocol = MaGateMessage.Res_Discovery_From_Community_Search;
	
	/******* Other processing/policy parameters *******/
	public static boolean Agreement_Enabled = false;
	public static int sizeOfCommunityJobQueue = 5;
//	public static int negotiationLimit = 3;
//	public static int delegationQueueLimit = 5;
	
	public static String delegationNegotiationProtocol = MaGateMessage.Delegation_Neg_None; 
//	public static String delegationNegotiationProtocol = MaGateMessage.Delegation_Neg_Input_Accessable;
//	public static String delegationNegotiationProtocol = MaGateMessage.Delegation_Neg_Input_Efficient_Accessable;
	
	public static String negotiationAgreement = MaGateMessage.NegotiationAgreement_None;
//	public static String negotiationAgreement = MaGateMessage.NegotiationAgreement_PE;
	
//	public static String interactionApproach  = MaGateMessage.Interaction_ObjectCall;
	public static String interactionApproach  = MaGateMessage.Interaction_AgreementCall;
	
	public static int totalNumberOfJob = 0;
	
	/** 
	 * Experiment iterations of each scenario 
	 * Passive mode, to be set by class ExpOrganizer during program running
	 */
	public static int countOfExperiment = 3;
	
	public static int scenario_overall = 0;
	public static int scenario_current = 0;
	
	/** 
	 * Adopted GWA name
	 * Passive mode, to be set by class ExpOrganizer during program running
	 */
	public static String gwaLoadName = "";  
	
	/** 
	 * GWA adoption load, percentage: 0 ~ 100 % 
	 * Passive mode, to be set by class ExpOrganizer during program running
	 */
	public static double gwaLoadinPercentage = 1;  
	
	/**
	 * Weight of estimated job execution time; 
	 * accordingly, the weight of estimated load execution time is (1-weightOfEstJobExecutionTime)
	 */
	public static double weightOfShadowJobQueue = 1;
	
	public static double systemReschedulingCoefficient = 1;
	
	public static double systemReschedulingOnAvgQueuingTime = 1;
	
	public static int systemIS = MaGateMessage.systemIS_Partial_ACO;
	
	public static int schedulingScheme = MaGateMessage.SchedulingScheme_Decentralized;
	
	public static int jobDistMode = MaGateMessage.JobDistMode_CustomizedDist;
	
	/** switch of maGate.getCASPController().processINFORM(); */
	public static boolean dynamicEnabled = true;
	
	public static int simEndPolicy = MaGateMessage.SimEndPolicy_natureHeartbeat;
	public static boolean screenPlotActivated = true;
	
	// --- --- --- --- --- --- --- --- ---
	// --- --- --- --- --- --- --- --- ---
	// --- --- --- --- --- --- --- --- ---
	
	public static String[] supportedMatchProfile = {MaGateMessage.MatchProfile_CPUCount, 
		MaGateMessage.MatchProfile_ExePrice, MaGateMessage.MatchProfile_OS};
	
	private static int index_available_os = 0;
	private static int index_available_mathmakerPolicy = 0;
	
	private static String[] available_os = {OperatingSystemTypeEnumeration.MACOS.toString()};
	
//	private static String[] available_os = {OperatingSystemTypeEnumeration.LINUX.toString(), 
//		OperatingSystemTypeEnumeration.WINDOWS_XP.toString(), 
//		OperatingSystemTypeEnumeration.MACOS.toString()};
	
//	private static int top500ResDistributionSeed = 0;
//	private static String[] top500ResOSDistribution = new String[] {
//			OperatingSystemTypeEnumeration.LINUX.toString(), 
//			OperatingSystemTypeEnumeration.MACOS.toString()};
	
	/// --- --- CFM general variables --- ---
	
	public static int topologyStatus = MaGateMessage.TopologyNodeStatus_AllStable;
	
	public static int cfmDelegationFailScore      = -3;
	public static double cfmJobReturnDeadline     = 3600 * 2;
	public static double cfmRecentTimeBarrier     = 3600 * 6;
	public static double cfmLocalReputationWeight = 0.5;
	public static double cfmResCapabilityWeight   = 0.5;
	
	
	/**
	 * Fetch a OS for new node profile fairly
	 */
	public static String fetchFairDistributedOS() { 
		
		int index = index_available_os % available_os.length;
		index_available_os++;
		
		return available_os[index];
		
	}
	
	
	public static void applyTOP500OSDistribution(Vector<ExpNode> nodeList) { 
		
		int rate = 0;
		
		for(ExpNode node : nodeList) {
			
			int probability = rate % 5;
			if(probability < 4) {
				node.setNodeOS(OperatingSystemTypeEnumeration.LINUX.toString());
			} else {
				node.setNodeOS(OperatingSystemTypeEnumeration.MACOS.toString());
			}
			
			rate++;
		}
	}
	
	public static void applyTOP500JobOSDistribution(Vector<Job> jobList) { 
		
		int rate = 0;
		
		for(Job job : jobList) {
			
			int probability = rate % 5;
			if(probability < 4) {
				job.setOSRequired(OperatingSystemTypeEnumeration.LINUX.toString());
			} else {
				job.setOSRequired(OperatingSystemTypeEnumeration.MACOS.toString());
			}
			
			rate++;
		}
	}
	
//	private static long top500JobDistributionSeed = 0;
	
//	public static String fetchTOP500JobOS() { 
//		
////		Random r = new Random(System.currentTimeMillis());
////		double rate = r.nextDouble();
//		
//		int rate = (int) (top500JobDistributionSeed++ % 10);
//		String returnedOS = "";
//		
//		if(rate < 8) {
//			returnedOS = top500ResOSDistribution[0];
//		} else {
//			returnedOS = top500ResOSDistribution[1];
//		}
//		
////		System.out.println("rate: " + rate + "; returned JOB OS: " + returnedOS);
//		
//		return returnedOS;
//		
//	}
	
//	public static String fetchFairDistributedOS() { 
//		int index = index_available_os % available_os.length;
//		index_available_os++;
//		
//		return available_os[index];
//	}
	
	/** All available MatchMaker policies */
	public static HashMap<String, Integer> availableMatchmakerPolicy = new HashMap<String, Integer>();
	
	/**
	 * Fetch a MatchMaker policy for installing new node profile fairly
	 * 
	 * @param available_mathmakerPolicy
	 * @return
	 */
	public static String fetchFairDistributedMatchmakerPolicy(String[] available_mathmakerPolicy) { 
		int index = index_available_mathmakerPolicy % available_mathmakerPolicy.length;
		index_available_mathmakerPolicy++;
		
		return available_mathmakerPolicy[index];
	}
	
	/**
	 * Get list of all launched MatchMaker policies during this experiment iteration
	 * 
	 * @return
	 */
	public static String getAvailableMatchMakerPolicies() {
		
		Object[] mmPolicyKey = availableMatchmakerPolicy.keySet().toArray();
		String outputText = mmPolicyKey.length + " MatchMaker Policies launched. ";
		
		for(int k = 0; k < mmPolicyKey.length; k++) {
			String tempMMPolicy = (String) mmPolicyKey[k];
			outputText += tempMMPolicy + ":" + availableMatchmakerPolicy.get(tempMMPolicy) + " / ";
		}
		
		return outputText + " \n";
	}
	
	
	@Deprecated 
	public static String getRandomOS() {
		
		Random r = new Random();
		double criteria = r.nextDouble();
		r = null;
		
		if(criteria < 0.33 ){
			return available_os[0];
		} else if (criteria > 0.66) {
			return available_os[1];
		} else {
			return available_os[2];
		}
		
	}
	
	@Deprecated 
	public static String getRandomOS(double criteria) {
		
		if(criteria < 0.33 ){
			return available_os[0];
		} else if (criteria > 0.66) {
			return available_os[1];
		} else {
			return available_os[2];
		}
		
	}
	
	public static long getStaticSimSeed(){
		return simSeed;
	}
	
	public static long getDynamicSimSeed(){
		Random randomEngine = new Random(System.currentTimeMillis());
		return (long) (randomEngine.nextDouble() * simSeed);
	}
	
	// incorporates weekends so the grid resource is on 7 days a week
	public static LinkedList getWeekEnds() {
		LinkedList<Integer> weekEnds = new LinkedList<Integer>();
		weekEnds.add(new Integer(Calendar.SATURDAY));
		weekEnds.add(new Integer(Calendar.SUNDAY));
		
		return weekEnds;
	}
	
	// incorporates holidays
	public static LinkedList getHolidays() {
		LinkedList<Integer> holidays = new LinkedList<Integer>();
		
		return holidays;
	}  
	

	public static Link getRandomLink() {
		
		Link randomLink = null;

		try {
			randomLink = new SimpleLink("link-" + UUID.randomUUID(), MaGateProfile.bandWidth, MaGateProfile.bandDelay, MaGateProfile.MTU);
			
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return randomLink;
	}
	
	
}


