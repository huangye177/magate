package ch.hefr.gridgroup.magate.cm;

import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.MaGateEntity;
import ch.hefr.gridgroup.magate.casa.ShadowNode;
import ch.hefr.gridgroup.magate.cfm.CFCPolicy;
import ch.hefr.gridgroup.magate.em.NetworkNeighborsManager;
import ch.hefr.gridgroup.magate.env.MaGateMediator;
import ch.hefr.gridgroup.magate.env.MaGateMessage;
import ch.hefr.gridgroup.magate.env.MaGateParam;
import ch.hefr.gridgroup.magate.env.MaGateToolkit;
import ch.hefr.gridgroup.magate.model.DelegatedJobItem;
import ch.hefr.gridgroup.magate.model.Job;
import ch.hefr.gridgroup.magate.model.JobInfo;
import ch.hefr.gridgroup.magate.model.RemoteNodeReputationItem;
import ch.hefr.gridgroup.magate.storage.GlobalStorage;

/**
 * Class CommunityMonitor bridges all CommunityModule's services to other part of MaGate 
 * @author Ye HUANG
 */
public class CommunityMonitor {

	private MaGateEntity maGate;
	private String       maGateIdentity;
	private long         previousClock;
	
	private static Log log = LogFactory.getLog(CommunityMonitor.class);

	public CommunityMonitor(MaGateEntity maGate) {
		
		this.maGate         = maGate;
		this.maGateIdentity = maGate.getMaGateIdentity();
		this.previousClock  = System.currentTimeMillis();
		
	}
	
	/**
	 * ASK FOR NEIGHBORS on level of individual MaGate
	 * Update neighboring-node list of the host node
	 */
	public void updateCommunity() {
		
		long timeSlot = System.currentTimeMillis() - this.previousClock;
		
		if(timeSlot > 1000) {
			
			this.maGate.getMaGateInfra().updateCommunityForMaGate(maGateIdentity);
			this.previousClock = System.currentTimeMillis();
		}
	}
	
	/**
	 * GET NEIGHBORS on level of individual MaGate
	 * Update the neighboring node list
	 * @param neighbors
	 */
	public void foundCommunity(List<String> neighbors) {
		
		// In case new nodes discovered, put them into the cached netowrkNeighbor Map
		NetworkNeighborsManager.cacheNetworkNeighbors(this.maGate, neighbors);
		
//		// Reserve the discovered/returned neighboring information
//		this.maGate.getStorage().setKnownNeighborList(neighbors);
	}
	
	
	/**
	 * Discover remote nodes for incoming JobInfo profile
	 * @param fetchedJobInfo
	 * @return String[] discoveredRemoteMaGateArray
	 */
//	public String[] discoverRemoteNodes(JobInfo fetchedJobInfo) {
//	
//		String[] discoveredRemoteMaGateArray = null;
//		
//		// Getting remote node list from the adopted resource discovery system
//		if(MaGateParam.resDiscoveryProtocol.equals(MaGateMessage.Res_Discovery_From_Direct_Neighbors)) {
//			
//			discoveredRemoteMaGateArray = this.maGate.getStorage().getAllNeighborList().toArray(new String[]{});
////			discoveredRemoteMaGateArray = this.maGate.getStorage().get_networkNeighborIds();
//			
//			if(this.maGate.getCFCController().getCFC_POLICY().contains(CFCPolicy.CFC_NONE)){ 
//				// CFM is disabled, fetch neighboring nodes only from network_neighborhood
//				discoveredRemoteMaGateArray = this.maGate.getStorage().get_networkNeighborIds();
//				
//			} else {
//				discoveredRemoteMaGateArray = this.maGate.getStorage().getAllNeighborList().toArray(new String[]{});
//			
//			}
//			
//		} else if (MaGateParam.resDiscoveryProtocol.equals(MaGateMessage.Res_Discovery_From_Community_Search)) {
//			discoveredRemoteMaGateArray = this.maGate.getResDiscovery().syncSearchRemoteMaGateIdentity(fetchedJobInfo.getCommunityJobInfoProfile()); 
//			
//		} else {
//			discoveredRemoteMaGateArray = this.maGate.getResDiscovery().syncSearchRemoteMaGateIdentity(fetchedJobInfo.getCommunityJobInfoProfile()); 
//			
//		}
//		
//		return discoveredRemoteMaGateArray;
//	}
	
	
	
	/**
	 * Discover remote nodes for incoming JobInfo profile
	 * @return String[] discoveredRemoteMaGateArray
	 */
	public String[] discoverRemoteNodes() {
		
		String[] discoveredRemoteMaGateArray = null;
		
		if(MaGateParam.systemIS == MaGateMessage.systemIS_Partial_ACO) {
			
			discoveredRemoteMaGateArray = this.maGate.getStorage().getAllNeighborList().toArray(new String[]{});
			
//			if(this.maGate.getCFCController().getCFC_POLICY().contains(CFCPolicy.CFC_NONE)){ 
//				// CFM is disabled, fetch neighboring nodes only from network_neighborhood
//				discoveredRemoteMaGateArray = this.maGate.getStorage().get_networkNeighborIds();
//				
//			} else {
//				discoveredRemoteMaGateArray = this.maGate.getStorage().getAllNeighborList().toArray(new String[]{});
//			
//			}
			
		} else if (MaGateParam.systemIS == MaGateMessage.systemIS_Partial_SIM) {
			
			Enumeration<String> nodeIds =  GlobalStorage.getAllMaGateIds();
			Vector<String> idHosters = new Vector<String>();
			
			while(nodeIds.hasMoreElements()) {
	            String value = nodeIds.nextElement();
	            idHosters.add(value);
	        }
		    
			int sizeofNodes = idHosters.size();
			int numOfNeighbors = Math.round((float)Math.min(sizeofNodes * ((double) 1 / 3), 6));
			
			int[] selectedNodeIndex = MaGateToolkit.getRandom(numOfNeighbors, sizeofNodes);
			
			discoveredRemoteMaGateArray = new String[selectedNodeIndex.length];
			
			for(int i = 0; i < selectedNodeIndex.length; i++) {
				discoveredRemoteMaGateArray[i] = idHosters.get(selectedNodeIndex[i]);
			}
			
			
		} else if (MaGateParam.systemIS == MaGateMessage.systemIS_Global) {
			
			Enumeration<String> nodeIds =  GlobalStorage.getAllMaGateIds();
			Vector<String> idHosters = new Vector<String>();
			
			while(nodeIds.hasMoreElements()) {
	            String value = nodeIds.nextElement();
	            idHosters.add(value);
	        }
			
			discoveredRemoteMaGateArray = idHosters.toArray(new String[]{});
			
		}
		
//		String test = "*********** [Node]" + this.maGateIdentity + "'s " +
//				"discoveredRemoteMaGateArray: " + discoveredRemoteMaGateArray.length + "\n";
//		for(int i = 0; i < discoveredRemoteMaGateArray.length; i++) {
//			test += discoveredRemoteMaGateArray[i] + "; ";
//		}
//		log.debug(test);
		
		return discoveredRemoteMaGateArray;
	}
	
	
	/**
	 * Select a better remote node from a set of candidates
	 * Priority 1: node recognized as free CF
	 * Priority 2: node has better MIPS
	 * 
	 * @param candidateMaGates
	 * @return
	 */
	public MaGateEntity selectProperRemoteNode(Vector<MaGateEntity> candidateMaGates) {
		
		MaGateEntity selectedMaGate = null;
		boolean highLevel = false;
		int bestMIPS = 0;
		
			
		for(MaGateEntity currentMaGate : candidateMaGates) {
			
			if(currentMaGate == null) {
				continue;
			}
			
			if(this.maGate.getStorage().contain_freeCFCNeighbors(currentMaGate.getMaGateIdentity())) {
				highLevel = true;
			}
			
			if(highLevel && (!this.maGate.getStorage().contain_freeCFCNeighbors(currentMaGate.getMaGateIdentity()))) {
				continue;
			}
			
			if(selectedMaGate != null) {
				if(highLevel &&  (!this.maGate.getStorage().contain_freeCFCNeighbors(selectedMaGate.getMaGateIdentity()))) {
					bestMIPS = currentMaGate.getStorage().getBestMachineMIPS().get();
					selectedMaGate = currentMaGate;
				}
			}
			
			if(currentMaGate.getStorage().getBestMachineMIPS().get() > bestMIPS) {
				bestMIPS = currentMaGate.getStorage().getBestMachineMIPS().get();
				selectedMaGate = currentMaGate;
			}
			
		}
		
		return selectedMaGate;
	}
	
	
	/**
	 * Select a better remote node from a set of candidates
	 * Priority 1: node recognized as free CF
	 * Priority 2: node has better MIPS
	 * 
	 * @param candidateMaGateIds
	 * @return
	 */
	public String selectProperRemoteNode(Enumeration<String> candidateMaGateIds) {
		
		MaGateEntity selectedMaGate = null;
		boolean highLevel = false;
		int bestMIPS = 0;
		
		while(candidateMaGateIds.hasMoreElements()) {
			
			String targetNodeId = candidateMaGateIds.nextElement();
			MaGateEntity currentMaGate = GlobalStorage.findMaGateById(targetNodeId);
			
			if(currentMaGate == null) {
				continue;
			}
			
			if(this.maGate.getStorage().contain_freeCFCNeighbors(currentMaGate.getMaGateIdentity())) {
				highLevel = true;
			}
			
			if(highLevel && (!this.maGate.getStorage().contain_freeCFCNeighbors(currentMaGate.getMaGateIdentity()))) {
				continue;
			}
			
			if(selectedMaGate != null) {
				if(highLevel &&  (!this.maGate.getStorage().contain_freeCFCNeighbors(selectedMaGate.getMaGateIdentity()))) {
					bestMIPS = currentMaGate.getStorage().getBestMachineMIPS().get();
					selectedMaGate = currentMaGate;
				}
			}
			
			if(currentMaGate.getStorage().getBestMachineMIPS().get() > bestMIPS) {
				bestMIPS = currentMaGate.getStorage().getBestMachineMIPS().get();
				selectedMaGate = currentMaGate;
			}
		}
		
		if(selectedMaGate == null) {
			return null;
		} else {
			return selectedMaGate.getMaGateIdentity();
		}
	}
	
	/**
	 * Post processing when job completed by remote nodes, specially, interaction logging and CFM processing
	 * @param gi
	 */
	public void jobDelegationCompleteByRemoteNode(JobInfo gi) {
		
		String executionNodeId = gi.getExecutionMaGateId();
		Job receivedJob        = gi.getJob();
		String jobId           = gi.getGlobalJobID();
		
		
		// Action 1: record the interaction in a log file
		int kk = this.maGate.getStorage().add_nodeInteraction(executionNodeId);
		
		
		// Action 2: score the interaction
		// remove current job delegation record
		
		double currentTime = MaGateMediator.getSystemTime();
		double dueTime     = 0.0;
		double score       = 0.0;
		
		// try to fetch the corresponding ongoing job-delegation record
		DelegatedJobItem delegationItem = this.maGate.getStorage().get_delegatedJobListItem(jobId);
		if(delegationItem == null) {
			
			if (receivedJob.getJobStatus() == MaGateMessage.JOB_FAILED) {
				score = MaGateParam.cfmDelegationFailScore;
			} else {
				score = MaGateMessage.CFM_SCORE_SUCCESS;
			}
			
		} else {
			
			// calculate the promised response time for the corresponding job
			dueTime = delegationItem.timestamp + delegationItem.promisedResponseTime;
			
			if (receivedJob.getJobStatus() == MaGateMessage.JOB_FAILED) {
				score = MaGateParam.cfmDelegationFailScore;
			} else {
				
				// a job delegation is considered not only because its job execution status
				// but also the response time
				if(currentTime - dueTime > MaGateParam.cfmJobReturnDeadline) {
					score = MaGateParam.cfmDelegationFailScore;
					
				} else {
					score = MaGateMessage.CFM_SCORE_SUCCESS;
				}
				
			}
		}
		
		this.maGate.getStorage().remove_delegatedJobListItem(jobId);
		
		// score the remote node
		boolean existingRemoteNodeCooperationRecord = this.maGate.getStorage().contain_remoteNodeReputationList(executionNodeId);
		if(existingRemoteNodeCooperationRecord) {
			RemoteNodeReputationItem remoteNodeReputationItem = this.maGate.getStorage().get_remoteNodeReputationListItem(executionNodeId);
			remoteNodeReputationItem.putScore(score);
		} else {
			RemoteNodeReputationItem remoteNodeReputationItem = new RemoteNodeReputationItem(executionNodeId);
			remoteNodeReputationItem.putScore(score);
			this.maGate.getStorage().join_remoteNodeReputationList(remoteNodeReputationItem);   // method: putIfAbsent
		}
		
		// update hosting node's remote-node-reputation-list
		this.maGate.getCFMController().refreshRemoteNodeReputation(executionNodeId);
		
	}
	
}




