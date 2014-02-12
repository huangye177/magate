package ch.hefr.gridgroup.magate.cfm;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.MaGateEntity;
import ch.hefr.gridgroup.magate.casa.CASAMessage;
import ch.hefr.gridgroup.magate.casa.CASAPolicy;
import ch.hefr.gridgroup.magate.env.MaGateParam;
import ch.hefr.gridgroup.magate.model.NeighborItem;
import ch.hefr.gridgroup.magate.storage.GlobalStorage;

public class CFCController {

	private static Log log = LogFactory.getLog(CFCController.class);
	
	private MaGateEntity maGate;
	private String maGateIdentity;
	
	private ConcurrentHashMap<String, String> CFC_POLICY = new ConcurrentHashMap<String, String>();


	public CFCController(MaGateEntity maGate, ConcurrentHashMap<String, String> cfcPolicy) {
		this.maGate = maGate;
		this.maGateIdentity = maGate.getMaGateIdentity();
		
		this.CFC_POLICY = cfcPolicy;
	}
	
//	/**
//	 * Once the resource usage is too low, inform CFs of the host node that it has a lot of free power for 
//	 * processing community job
//	 */
//	public void cfmPushMonitoring() {
//		
//		if(this.CFC_POLICY.contains(CFCPolicy.CFC_NONE)){ 
//			return;
//		}
//		
//		// Activate "jobDiscoveryOnCFC" if CFCPolicy.CFC_POLICY_AGGRESSIVENESS
//		if(this.CFC_POLICY.contains(CFCPolicy.CFC_PUSH)) {
//			
//			double freePE = this.maGate.getStorage().getTotalNumOfPEs().doubleValue()- this.maGate.getStorage().getTotalActivePEs().doubleValue();
//			double usage = freePE / this.maGate.getStorage().getTotalNumOfPEs().get();
//			
//			
//			if(usage < 0.5) {
//				
//				NeighborItem currentMaGateItem = new NeighborItem(this.maGateIdentity, this.maGate.getMaGateOS(), 
//						this.maGate.getStorage().getTotalNumOfPEs().get(), false, true);
//				
//				// inform the CFs that the host node has too much used power currently
//				Enumeration<String> cfcNeighbors = this.maGate.getStorage().get_cfcNeighborIds();
//				MaGateEntity remoteMaGate = null;
//				
//				while (cfcNeighbors.hasMoreElements()) {
//				    String key = (String) cfcNeighbors.nextElement();
//				    remoteMaGate = GlobalStorage.findMaGateById(key);
//				    
//				    // put the hosting node in the (1) normal cf-list; and (2) priority cf-list; of the remote CF nodes
//				    remoteMaGate.getStorage().join_cfcNeighbors(currentMaGateItem);
//					remoteMaGate.getStorage().join_freeCFCNeighbors(currentMaGateItem);
//				}
//				
//				GlobalStorage.count_CFCFreePower.incrementAndGet();
//				
//			} else if (usage > 0.95 || (freePE < 2)) {
//				
//				// inform the CFs that the host node has limited free power currently
//				Enumeration<String> cfcNeighbors = this.maGate.getStorage().get_cfcNeighborIds();
//				MaGateEntity remoteMaGate = null;
//				
//				while (cfcNeighbors.hasMoreElements()) {
//				    String key = (String) cfcNeighbors.nextElement();
//				    remoteMaGate = GlobalStorage.findMaGateById(key);
//				    
//				    // only remove the host node from the (1) normal cf-list of remote cf nodes
//					remoteMaGate.getStorage().remove_freeCFCNeighbors(this.maGateIdentity);
//				}
//				
//				GlobalStorage.count_CFCNonfreepower.incrementAndGet();
//			}
//		}
//	}
	
	
	/**
	 * Launch a resource discovery within the scope of host node's CFC, for an incoming job from a CF
	 * of such host node
	 * 
	 * @param incomingJobInfo
	 * @param incomingNodeId
	 * @return
	 */
	public String resourceDiscoveryOnCFC(String incomingNodeId) {
		
		String foundNodeId = null;
		
		if(this.CFC_POLICY.contains(CFCPolicy.CFC_NONE)) {
			return null;
		} 
		
		if (this.CFC_POLICY.contains(CFCPolicy.CFC_RESTRICT_DISPATCH)) {
			
			// Activate "resourceDiscoveryOnCFC" if "CFCPolicy.CFC_POLICY_RESTRICT_DISPATCH"
			// The incoming node is a Critical Friend of the hosting node
			if (this.maGate.getStorage().contain_cfcNeighbors(incomingNodeId)) {

				/**
				 * IMPORTANT: the match of remote nodes and incoming jobs
				 * should be completed here, NOT via issuing another REQUEST
				 * message to the remote node; OR it may lead to another
				 * CFC-based dispatching process and dead-lock possibility
				 */
				Enumeration<String> remoteNodeIds = this.maGate.getStorage().get_cfcNeighborIds();
				foundNodeId = this.maGate.getCommunityMonitor().selectProperRemoteNode(remoteNodeIds);

			} else {
				// The incoming node is NOT a Critical Friend of the hosting
				// node
			}

		} else if (this.CFC_POLICY.contains(CFCPolicy.CFC_TOLERANT_DISPATCH)) {
			
			// Activate "resourceDiscoveryOnCFC" if "CFCPolicy.CFC_POLICY_TOLERANT_DISPATCH"
			/**
			 * IMPORTANT: the match of remote nodes and incoming jobs
			 * should be completed here, NOT via issuing another REQUEST
			 * message to the remote node; OR it may lead to another
			 * CFC-based dispatching process and dead-lock possibility
			 */
			Enumeration<String> remoteNodeIds = this.maGate.getStorage().get_cfcNeighborIds();
			foundNodeId = this.maGate.getCommunityMonitor().selectProperRemoteNode(remoteNodeIds);
			
		} else {
			// unknown CFC policy
		}
		
		return foundNodeId;
	}
	
	
//	/**
//	 * Identify a known remote as a Critical Friend Node
//	 * @param remoteId
//	 */
//	public void cacheCFCNeighbors(String remoteId) {
//		
//		if(this.CFC_POLICY.contains(CFCPolicy.CFC_NONE)){ 
//			return;
//		}
//		
//		// Check the obtained remote Id, and cache them if necessary
//		MaGateEntity remoteNode = GlobalStorage.findMaGateById(remoteId);
//		
//		// 1st: check the responding remote node.
//		// If such a remote node was not discovered before and neither the host node,
//		// it will be cached into "KnownCFCNode" list.
//		
//		if( (!this.maGate.getMaGateIdentity().equals(remoteId)) && 
//				(!this.maGate.getStorage().contain_cfcNeighbors(remoteId)) && 
//				(remoteNode != null)) {
//			
//			NeighborItem item = new NeighborItem(remoteId, remoteNode.getMaGateOS(), 
//					remoteNode.getStorage().getTotalNumOfPEs().get(), false, true);
//				
//			this.maGate.getStorage().join_cfcNeighbors(item);
//			
//		}
//		
//		if(this.CFC_POLICY.contains(CFCPolicy.CFC_RESTRICT_NETWORK)) {
//			
//			// Only put node who confirm the job delegation as CF (done in previous steps), then quits
//			return;
//			
//		} else if (this.CFC_POLICY.contains(CFCPolicy.CFC_OPTIMIZED_NETWORK)) {
//			
//			// Consider the CFs of the incoming node (CF-tier1) also as the CFs (tier 2) of the hosting node
//			Enumeration<NeighborItem> remoteNodeNeighbors = (Enumeration<NeighborItem>) remoteNode.getStorage().get_cfcNeighbors();
//			
//			while(remoteNodeNeighbors.hasMoreElements()) {
//				this.maGate.getStorage().join_cfcNeighbors(remoteNodeNeighbors.nextElement());
//			}
//			
//		} else if(this.CFC_POLICY.contains(CFCPolicy.CFC_MASSIVE_NETWORK)) {
//			
//			// collect CF neighbors of remote node
//			Enumeration<NeighborItem> remoteCFCNeighbors = (Enumeration<NeighborItem>) remoteNode.getStorage().get_cfcNeighbors();
//			
//			while(remoteCFCNeighbors.hasMoreElements()) {
//				this.maGate.getStorage().join_cfcNeighbors(remoteCFCNeighbors.nextElement());
//			}
//			
//			// collect network neighbors of remote node
//			Enumeration<NeighborItem> remoteNetworkNeighbors = (Enumeration<NeighborItem>) remoteNode.getStorage().get_networkNeighbors();
//			
//			while(remoteNetworkNeighbors.hasMoreElements()) {
//				this.maGate.getStorage().join_cfcNeighbors(remoteNetworkNeighbors.nextElement());
//			}
//			
//		}
//		
//	}
	
	
	public ConcurrentHashMap<String, String> getCFC_POLICY() {
		return CFC_POLICY;
	}
	
}
