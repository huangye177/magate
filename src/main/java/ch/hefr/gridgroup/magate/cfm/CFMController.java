package ch.hefr.gridgroup.magate.cfm;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.MaGateEntity;
import ch.hefr.gridgroup.magate.casa.CASAMessage;
import ch.hefr.gridgroup.magate.casa.CandidateNode;
import ch.hefr.gridgroup.magate.casa.MsgAccept;
import ch.hefr.gridgroup.magate.env.JobCenterManager;
import ch.hefr.gridgroup.magate.env.MaGateMediator;
import ch.hefr.gridgroup.magate.env.MaGateMessage;
import ch.hefr.gridgroup.magate.env.MaGateParam;
import ch.hefr.gridgroup.magate.model.DelegatedJobItem;
import ch.hefr.gridgroup.magate.model.NeighborItem;
import ch.hefr.gridgroup.magate.model.RemoteNodeReputationItem;
import ch.hefr.gridgroup.magate.storage.GlobalStorage;

public class CFMController {

	private static Log log = LogFactory.getLog(CFMController.class);
	
	private MaGateEntity maGate;
	private String maGateIdentity;
	
//	private Vector<String> availableRemoteNodes = new Vector<String>();
	private long previousTimestamp = 0;
	
	private ConcurrentHashMap<String, String> CFM_POLICY = new ConcurrentHashMap<String, String>();

	public CFMController(MaGateEntity maGate, ConcurrentHashMap<String, String> cfmPolicy) {
		this.maGate = maGate;
		this.maGateIdentity = maGate.getMaGateIdentity();
		
		this.CFM_POLICY = cfmPolicy;
	}
	
	/**
	 * CFM topology expansion sub-model
	 * 
	 * @param jobOS
	 * @param jobNumPE
	 * @param jobId
	 * @param replicsOfMsgRequest
	 * @param requesterNodeId
	 * @param caspMsg
	 * @param estRemoteResponseTime
	 * @param avgRemoteQueuingTime
	 * @return
	 */
	public MsgAccept cfmTopologyExpansion(String jobOS, int jobNumPE, String jobId, double replicsOfMsgRequest, 
			String requesterNodeId, int caspMsg, double estRemoteResponseTime, double avgRemoteQueuingTime) {
		
		MsgAccept msgAccept      = null;
		String recommendedNodeId = null;
		double bestEstResponseTime = -1;
		
		// check whether CFM is enabled
		if(this.CFM_POLICY.contains(CFMPolicy.CFM_NONE)) {
			return null;
		} 
		
		if(!this.CFM_POLICY.contains(CFMPolicy.CFM_ToEx)) {
			return null;
		}
		
		// check whether the requesting node is a CF for the hosting node
		this.refreshRemoteNodeReputation(requesterNodeId);
		if(this.maGate.getStorage().contain_remoteNodeReputationList(requesterNodeId)) {
			RemoteNodeReputationItem correspondingReputationItem = this.maGate.getStorage().get_remoteNodeReputationListItem(requesterNodeId);
			if(!correspondingReputationItem.isCFNode()) {
				return null;
			}
			
		} else {
			return null;
		}
		
		// find or refresh all available remote nodes for CFM operations
		Vector<String> availableRemoteNodes = this.findAllAvailableRemoteNodeIds();
		if(availableRemoteNodes == null || availableRemoteNodes.size() == 0) {
			return null;
		}
		
		for(String remoteNodeId : availableRemoteNodes) {
			
			MaGateEntity remoteNode = GlobalStorage.findMaGateById(remoteNodeId);
			
			if(!remoteNode.getMatchMaker().check_jobMatchResource(jobOS, jobNumPE)) {
				continue;
			}
			
			double estTimeForJob = JobCenterManager.getJobInfobyJobId(jobId).getEstimatedComputationTime();
			double estTimeForLoadOfLocalJobQueue = remoteNode.getMatchMaker().estimatedTimeToExecuteLocalJobQueue();
			double estTimeForLoadOfShadowJobQueue = remoteNode.getMatchMaker().estimatedTimeToExecuteShadowJobQueue();
			
			double estResponseTime = estTimeForJob + estTimeForLoadOfLocalJobQueue + estTimeForLoadOfShadowJobQueue;
			
			if(caspMsg == CASAMessage.CASP_MSG_REQUEST) {
				
				// find the best candidate node in the scheme of CASA_REQUEST
				if((bestEstResponseTime < 0) || (estResponseTime < bestEstResponseTime)) {
					recommendedNodeId = remoteNodeId;
					bestEstResponseTime = estResponseTime;
				} 
				
			} else if (caspMsg == CASAMessage.CASP_MSG_INFORM) {
				
				// additional work to ensure the necessity of job re-scheduling 
				remoteNode.getMatchMaker().updateQueuingTime();
				double avgLocalQueuingTime = remoteNode.getMatchMaker().getAvgQueuingTime();
				
				double weightedAvgLocalQueuingTime = 
					avgLocalQueuingTime * MaGateParam.systemReschedulingOnAvgQueuingTime;
				
				if((estResponseTime < estRemoteResponseTime) && (weightedAvgLocalQueuingTime < avgRemoteQueuingTime)) {
					
					// find the best candidate node in the scheme of CASA_REQUEST
					if((bestEstResponseTime < 0) || (estResponseTime < bestEstResponseTime)) {
						recommendedNodeId = remoteNodeId;
						bestEstResponseTime = estResponseTime;
					} 
					
				}
				
			}
		}
		
		if(recommendedNodeId == null || recommendedNodeId.equals("")) {
			msgAccept = null;
			
		} else {
			
			if(caspMsg == CASAMessage.CASP_MSG_REQUEST) {
				GlobalStorage.count_msgACCEPT_REQUEST.incrementAndGet();
			} else if (caspMsg == CASAMessage.CASP_MSG_INFORM) {
				GlobalStorage.count_msgACCEPT_INFORM.incrementAndGet();
			}
			
			GlobalStorage.count_casp_accept_req_Controller.incrementAndGet();
			
			MaGateEntity remoteNode = GlobalStorage.findMaGateById(recommendedNodeId);
			double estTimeForJob    = JobCenterManager.getJobInfobyJobId(jobId).getEstimatedComputationTime();
			double estTimeForLoadOfLocalJobQueue  = remoteNode.getMatchMaker().estimatedTimeToExecuteLocalJobQueue();
			double estTimeForLoadOfShadowJobQueue = remoteNode.getMatchMaker().estimatedTimeToExecuteShadowJobQueue();
			
			// Return the candidate node Id
			msgAccept = new MsgAccept(true, recommendedNodeId, jobId, 
					estTimeForJob, estTimeForLoadOfLocalJobQueue, estTimeForLoadOfShadowJobQueue);
			
			// mark the acceptance decision in MatchMaker
			double acceptanceApproveProbability = 0;
			if(replicsOfMsgRequest != 0) {
				acceptanceApproveProbability = (double) 1 / replicsOfMsgRequest;
			}
			
			remoteNode.getMatchMaker().appendAcceptanceDecision(jobId, acceptanceApproveProbability);
		}
		
		return msgAccept;
	}
	
	/**
	 * CFM resource selection sub-model, giving each candidate node a job delegation trust (probability) value
	 * 
	 * @param candidateMaGates
	 */
	public void cfmResourceSelection(Vector<CandidateNode> candidateMaGates) {
		
		if(this.CFM_POLICY.contains(CFMPolicy.CFM_NONE)) {
			return;
		} 
		
		if(!this.CFM_POLICY.contains(CFMPolicy.CFM_ReSe)) {
			return;
		}
		
		if(candidateMaGates.size() == 0) {
			return;
		}
		
		// get a set of remote nodes for consulting recommended reputation
		Vector<String> knownRemoteNodeIds = new Vector<String>();
		String[] discoveredRemoteNodes            = this.maGate.getCommunityMonitor().discoverRemoteNodes();
		Enumeration<String> historicalRemoteNodes = this.maGate.getStorage().get_remoteNodeReputationListIds();
		
		for(int i = 0; i < discoveredRemoteNodes.length; i++) {
			knownRemoteNodeIds.add(discoveredRemoteNodes[i]);
		}
		while(historicalRemoteNodes.hasMoreElements()) {
			String nextNodeId = historicalRemoteNodes.nextElement();
			if(!knownRemoteNodeIds.contains(nextNodeId)) {
				knownRemoteNodeIds.add(nextNodeId);
			}
		}
		
		// 1st round process: calculate each node's current job delegation trust
		double lowestTrustScore     = 0.0;
		boolean lowestTrustScoreSet = false;
		
		for(CandidateNode candidateNode : candidateMaGates) {
			
			// the current to-process candidate node 
			String candidateNodeId      = candidateNode.node.getMaGateIdentity();
			double trustOfCandidateNode = 0.0;
			
			double candidateLocalReputation       = 0.0;
			double candidateRecommendedReputation = 0.0;
			
			double candidateAllRemoteReputation  = 0.0;
			int candidateRemoteReputationCounter = 0;
			
			// calculate the local reputation
			// the candidate node cannot be the hosting node, otherwise no meaning to continue the calculation of current candidate node
			if(!this.maGateIdentity.equals(candidateNodeId)) {
				this.maGate.getCFMController().refreshRemoteNodeReputation(candidateNodeId);
				RemoteNodeReputationItem candidateReputationItem = this.maGate.getStorage().get_remoteNodeReputationListItem(candidateNodeId);
				if(candidateReputationItem == null) {
					// no inter-node record found between the hosting node and the candidate node
					candidateLocalReputation = 0;
				} else {
					// hosting node has QoS knowledge with regard to the candidate node
					candidateLocalReputation = candidateReputationItem.getRemodeNodeRecentReputation();
				}
			} else {
				// the candidate node is the hosting node itself
				// to simplify the process, the reputation of the hosting node itself is only determined by the number of successfully/failed executed jobs
				int[] nodeJobExecutionStatus = JobCenterManager.sizeOfJob_allStatusByNode(this.maGateIdentity);
				int numExedJobs      = nodeJobExecutionStatus[5];
				int numFailedJobs    = nodeJobExecutionStatus[6] + nodeJobExecutionStatus[7];
				int numProcessedJobs = numExedJobs + numFailedJobs;
				double score = (double) numExedJobs * MaGateMessage.CFM_SCORE_SUCCESS + numFailedJobs * MaGateParam.cfmDelegationFailScore;
				if(numProcessedJobs == 0) {
					candidateLocalReputation = 0;
				} else {
					candidateLocalReputation = score / numProcessedJobs;
				}
				
			}
			
			for(String knownRemoteId : knownRemoteNodeIds) {
				
				if(!knownRemoteId.equals(candidateNodeId)) {
					
					MaGateEntity knownRemoteNode = GlobalStorage.findMaGateById(knownRemoteId);
					knownRemoteNode.getCFMController().refreshRemoteNodeReputation(candidateNodeId);
					RemoteNodeReputationItem candidateReputationItem = knownRemoteNode.getStorage().get_remoteNodeReputationListItem(candidateNodeId);
					
					double candidateReputation = 0.0;
					if(candidateReputationItem == null) {
						// no inter-node record found between the known remote node and the candidate node
						
					} else {
						// remote node has QoS knowledge with regard to the candidate node
						candidateReputation = candidateReputationItem.getRemodeNodeRecentReputation();
						candidateAllRemoteReputation += candidateReputation;
						candidateRemoteReputationCounter++;
					}
					
				} else {
					// the known remote node is the candidate node as well, then its evaluation of itself is no more useful
					
				}
			}
			
			// calculate the recommended reputation
			if(candidateRemoteReputationCounter == 0) {
				candidateRecommendedReputation = 0.0;
			} else {
				candidateRecommendedReputation = candidateAllRemoteReputation / candidateRemoteReputationCounter;
			}
			
			trustOfCandidateNode = candidateLocalReputation * MaGateParam.cfmLocalReputationWeight + 
				candidateRecommendedReputation * (1 - MaGateParam.cfmLocalReputationWeight);
			
			candidateNode.nodeTrustProbability = trustOfCandidateNode;
			
			if(!lowestTrustScoreSet) {
				lowestTrustScore = trustOfCandidateNode;
				lowestTrustScoreSet = true;
				
			} else {
				
				// find the lowest trust score, specially designed for negative trust scores
				if(trustOfCandidateNode < lowestTrustScore) {
					lowestTrustScore = trustOfCandidateNode;
				}
				
			}
		}
		
		// 2nd round process: adjust all negative trust value of all candidate nodes
		// meanwhile, all candidate nodes' trust scores are larger than 0
		double compensatoinValue = Math.abs(lowestTrustScore) + 0.0001;
		double totalTrust        = 0.0;
		
		for(CandidateNode candidateNode : candidateMaGates) {
			
			// calculate the probability of job delegation trust
			candidateNode.nodeTrustProbability += compensatoinValue;
			totalTrust += candidateNode.nodeTrustProbability;
			
		}
		
		// 3rd round process, get the trust probabilities for all candidate nodes
		for(CandidateNode candidateNode : candidateMaGates) {
			
			// re-calculate job delegation trust probability
			candidateNode.nodeTrustProbability = candidateNode.nodeTrustProbability / totalTrust;
			
		}
		
		// 4th round process: obtain a merged selection probability based on node resource capability and trust
		double totalSelectionProbability = 0.0;
		
		for(CandidateNode candidateNode : candidateMaGates) {
			candidateNode.nodeMergedProbability = candidateNode.nodeResProbability * candidateNode.nodeTrustProbability;
			totalSelectionProbability += candidateNode.nodeMergedProbability;
		}
		
		// 5th round process: update nodes' merged selection probabilities
		if(totalSelectionProbability != 0) {
			for(CandidateNode candidateNode : candidateMaGates) {
				candidateNode.nodeMergedProbability = candidateNode.nodeMergedProbability / totalSelectionProbability;
			}
		}
	}
	
	/**
	 * Refresh remote node's overall and recent reputation
	 */
	public synchronized void refreshRemoteNodeReputation(String remodeNodeId) {
		
		if(!this.maGate.getStorage().contain_remoteNodeReputationList(remodeNodeId)) {
			return;
		}
		
		RemoteNodeReputationItem remoteNodeReputation = this.maGate.getStorage().get_remoteNodeReputationListItem(remodeNodeId);
		ConcurrentHashMap<String, DelegatedJobItem> delegatedJobList = this.maGate.getStorage().get_delegatedjobList();
		
		double overallScore = 0.0;
		double recentScore  = 0.0;
		
		int overallCounter = 0;
		int recentCounter  = 0;
		
		// Step 1: calculating already completed job interactions
		
		ConcurrentHashMap<String, Double> eventScoreMap     = remoteNodeReputation.getEventScoreMap();
		ConcurrentHashMap<String, Double> eventTimestampMap = remoteNodeReputation.getEventTimestampMap();
		
		double currentTime = MaGateMediator.getSystemTime();
		
		Iterator<Map.Entry<String, Double>> eventTimestampMapIterator = eventTimestampMap.entrySet().iterator();
		
		while(eventTimestampMapIterator.hasNext()) {
			
			Map.Entry<String, Double> entry1 = eventTimestampMapIterator.next();
			
			String eventId   = entry1.getKey();
			double timestamp = entry1.getValue().doubleValue();
			double score     = eventScoreMap.get(eventId);
			
			// put scores in different calculators
			
			double timeDifference = currentTime - timestamp;
			if(timeDifference < MaGateParam.cfmRecentTimeBarrier) {
				recentScore += score;
				recentCounter++;
			}
			overallScore += score;
			overallCounter++;
		}
		
		
		// Step 2: calculating scores of ongoing job delegations, only give -1 for those jobs having used up their promised response time
		
		Iterator<Map.Entry<String, DelegatedJobItem>> ongoingDelegationIterator = delegatedJobList.entrySet().iterator();
		
		while(ongoingDelegationIterator.hasNext()) {
			
			Map.Entry<String, DelegatedJobItem> entry2 = ongoingDelegationIterator.next();
			
			DelegatedJobItem delegationItem = entry2.getValue();
			String remoteId = delegationItem.remodeId;
			
			// only check the delegation wherein current remote node is the responding node
			if(remodeNodeId.equals(remoteId)) {
				double dueTime = delegationItem.timestamp + delegationItem.promisedResponseTime;
				
				// only action if the job's promised response time is delayed
				if(currentTime > dueTime) {
					
					double score = MaGateMessage.CFM_SCORE_NEGATIVE;
					
					// if the job return is delayed for too much time, then such a job delegation is considered as failed
					if(currentTime - dueTime > MaGateParam.cfmJobReturnDeadline) {
						score = MaGateParam.cfmDelegationFailScore;
					}
					
					// check whether it is recent records
					double timeDifference = currentTime - delegationItem.timestamp;
					if(timeDifference < MaGateParam.cfmRecentTimeBarrier) {
						recentScore += score;
						recentCounter++;
					}
					
					overallScore += score;
					overallCounter++;
				}
			}
		}
		
		
		// Step 3: deciding a CF node and write information back to the RemoteNodeReputationItem
		
		if(overallCounter == 0) {
			overallScore = 0;
		} else {
			overallScore = overallScore / overallCounter;
		}
		
		
		if(recentCounter == 0) {
			recentScore = 0;
		} else {
			recentScore  = recentScore / recentCounter;
		}
		
		
		boolean isCFNode = false;
		
		if(overallScore > MaGateMessage.CFM_CFNode_Barrier) {
			isCFNode = true;
		}
		
		remoteNodeReputation.setCFNode(isCFNode);
		remoteNodeReputation.setRemodeNodeReputation(overallScore);
		remoteNodeReputation.setRemodeNodeRecentReputation(recentScore);
		
	}
	
	/**
	 * Find or refresh all available remote nodes' ids for CFM related operations
	 */
	private Vector<String> findAllAvailableRemoteNodeIds() {
		
		Vector<String> availableRemoteNodes = null;
		
		if(this.needToRefreshData()) {
			
//			// clean up remote nodes found from previous operation
//			this.availableRemoteNodes.clear();
			
			availableRemoteNodes = new Vector<String>();
			
			String[] discoveredRemoteNodes            = this.maGate.getCommunityMonitor().discoverRemoteNodes();
			Enumeration<String> historicalRemoteNodes = this.maGate.getStorage().get_remoteNodeReputationListIds();
			
			for(int i = 0; i < discoveredRemoteNodes.length; i++) {
				availableRemoteNodes.add(discoveredRemoteNodes[i]);
			}
			while(historicalRemoteNodes.hasMoreElements()) {
				String nextNodeId = historicalRemoteNodes.nextElement();
				if(!availableRemoteNodes.contains(nextNodeId)) {
					availableRemoteNodes.add(nextNodeId);
				}
			}
		}
		
		return availableRemoteNodes;
		
	}
	
	/**
	 * Determine last update is still valid or not, in order to prevent too frequent data fresh operations
	 * @return
	 */
	private boolean needToRefreshData() {
		
		boolean needToRefreshData = false;
		
		long currentTimestamp = System.currentTimeMillis();
		if((currentTimestamp - this.previousTimestamp) > 100) {
			// should refresh the data because current data was 100ms ago
			needToRefreshData = true;
		} else {
			needToRefreshData = false;
		}
		
		this.previousTimestamp = currentTimestamp;
		
		return needToRefreshData;
		
	}
	
	
	/**
	 * Once the resource usage is too low, inform CFs of the host node that it has a lot of free power for 
	 * processing community job
	 */
	public void cfmPushMonitoring() {
		
		if(this.CFM_POLICY.contains(CFMPolicy.CFM_NONE)){ 
			return;
		}
		
		// Activate "jobDiscoveryOnCFC" if CFCPolicy.CFC_POLICY_AGGRESSIVENESS
		if(this.CFM_POLICY.contains(CFMPolicy.CFM_Push)) {
			
			double freePE = this.maGate.getStorage().getTotalNumOfPEs().doubleValue()- this.maGate.getStorage().getTotalActivePEs().doubleValue();
			double usage = freePE / this.maGate.getStorage().getTotalNumOfPEs().get();
			
			
			if(usage < 0.5) {
				
				NeighborItem currentMaGateItem = new NeighborItem(this.maGateIdentity, this.maGate.getMaGateOS(), 
						this.maGate.getStorage().getTotalNumOfPEs().get(), false, true);
				
				// inform the CFs that the host node has too much used power currently
				Enumeration<String> cfcNeighbors = this.maGate.getStorage().get_cfcNeighborIds();
				MaGateEntity remoteMaGate = null;
				
				while (cfcNeighbors.hasMoreElements()) {
				    String key = (String) cfcNeighbors.nextElement();
				    remoteMaGate = GlobalStorage.findMaGateById(key);
				    
				    // put the hosting node in the (1) normal cf-list; and (2) priority cf-list; of the remote CF nodes
				    remoteMaGate.getStorage().join_cfcNeighbors(currentMaGateItem);
					remoteMaGate.getStorage().join_freeCFCNeighbors(currentMaGateItem);
				}
				
				GlobalStorage.count_CFCFreePower.incrementAndGet();
				
			} else if (usage > 0.95 || (freePE < 2)) {
				
				// inform the CFs that the host node has limited free power currently
				Enumeration<String> cfcNeighbors = this.maGate.getStorage().get_cfcNeighborIds();
				MaGateEntity remoteMaGate = null;
				
				while (cfcNeighbors.hasMoreElements()) {
				    String key = (String) cfcNeighbors.nextElement();
				    remoteMaGate = GlobalStorage.findMaGateById(key);
				    
				    // only remove the host node from the (1) normal cf-list of remote cf nodes
					remoteMaGate.getStorage().remove_freeCFCNeighbors(this.maGateIdentity);
				}
				
				GlobalStorage.count_CFCNonfreepower.incrementAndGet();
			}
		}
	}
	
	public ConcurrentHashMap<String, String> getCFM_POLICY() {
		return CFM_POLICY;
	}
	
}
