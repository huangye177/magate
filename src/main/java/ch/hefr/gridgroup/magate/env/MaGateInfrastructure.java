package ch.hefr.gridgroup.magate.env;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.MaGateEntity; 
import ch.hefr.gridgroup.magate.em.ssl.BlatAntNestController;
import ch.hefr.gridgroup.magate.em.ssl.BlatAntNetworkController;
import ch.hefr.gridgroup.magate.em.ssl.IBlatAntNestController;
import ch.hefr.gridgroup.magate.em.ssl.IBlatAntNestObserver;
import ch.hefr.gridgroup.magate.em.ssl.IBlatAntNetworkController;
import ch.hefr.gridgroup.magate.em.ssl.IBlatAntNetworkObserver;
import ch.hefr.gridgroup.magate.storage.GlobalStorage;

public class MaGateInfrastructure implements IBlatAntNestObserver, IBlatAntNetworkObserver {

	private static Log log = LogFactory.getLog(MaGateInfrastructure.class);
//	private static boolean firstTime = true;
	
	private IBlatAntNestController nestc;
	private IBlatAntNetworkController netc;
	
	private List<String> maGateIdList = new ArrayList<String>();
	
	private static String hook = "MaGate-Hook";
	
	public MaGateInfrastructure() {
		
		if((MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Decentralized) && 
				(MaGateParam.systemIS == MaGateMessage.systemIS_Partial_ACO)) {
			
			this.nestc = new BlatAntNestController(this, "localhost", 56789, "localhost", 47334);
			this.netc  = new BlatAntNetworkController(this, "localhost", 56790, "localhost", 34567);
			
			try {
				
//				this.hook = "MaGate-Hook" + UUID.randomUUID().toString();
//				this.netc.attachNode(this.hook, "");
				
				// Important! The nest control in the simulator is started when the first
				// node gets added. So here we must wait a little bit for the nest control to start
				Thread.sleep(1000);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	
	/**
	 * Publish MaGate's profile to community
	 * @param maGateIdentity
	 * @param maGateProfile
	 */
	public void updateProfile(String maGateIdentity, HashMap<String,Object> maGateProfile) {
		
		// IMPORTANT: register a Nest for resource discovery
		try {
			if((MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Decentralized) && 
					(MaGateParam.systemIS == MaGateMessage.systemIS_Partial_ACO)) {
				
				this.nestc.updateProfile(maGateIdentity, maGateProfile);
			}
			
//			log.debug(" [ MaGate Profile register]: " + maGateIdentity + "; maGateProfile: " + maGateProfile);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void startQuery(String maGateIdentity, Object queryId, HashMap<String, Object> query) {

		// send the query
		try {
			this.nestc.startQuery(maGateIdentity, queryId.toString(), query);
			
			// count this behaviour
			GlobalStorage.count_communitySearch.incrementAndGet();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
			
	}
	
	/**
	 * Get discovered results from infrastructure, NOTICE: this method will be kept invoking when new nodes are found
	 * @param queryId: query id
	 * @param matchedMaGateIdentity: single MaGateIdentity
	 */
	public void addQueryResult(String queryId, String matchedResult) {
		
		if(!GlobalStorage.getMaGateCollectionReady()) {
			return;
		}
		
		String[] arr = queryId.split("_");
    	String maGateId = arr[0];
    	
    	MaGateEntity maGate = GlobalStorage.findMaGateById(maGateId); 
    	
    	maGate.getResDiscovery().onResultFound(queryId, matchedResult);
    	
    	// count this behaviour
    	GlobalStorage.count_communitySearchFeedback.incrementAndGet();
    	
	}
	
	/**
	 * Get the node list of entire network infrastructure
	 */
	public void setNodeList(List<String> list) {
		
		System.out.println("MaGateInfrastructure: Network self-checked\n");
//		for (String n : list) {
//			System.out.format("\t%s\n",n);
//		}
	}
	
	
	/**
	 * Check the network infrastructure
	 */
	public void selfCheck() {
		
		System.out.println("MaGateInfrastructure: Network prepared...\n");
		
		try {
			Thread.sleep(2000);
			
			if((MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Decentralized) && 
					(MaGateParam.systemIS == MaGateMessage.systemIS_Partial_ACO)) {
				
				this.netc.getNodelist();
				
				for (String n : maGateIdList) {
					this.netc.getNeighbors(n);
				}
				
			}
			
			Thread.sleep(3000);
			GlobalStorage.setMaGateCollectionReady(true);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * ASK FOR NEIGHBORS on level of Infrastructure
	 * Issue a call for neighborhood update from the MaGateCommunityMonitor, 
	 * and gets new neighborhood information
	 * NOTICE: the results won't be obtained here, but in setNodeNeighbors()
	 */
	public void updateCommunityForMaGate(String maGateId) {
		try {
			this.netc.getNeighbors(maGateId);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * GET NEIGHBORS on level of Infrastructure
	 * Get the neighborhood node list of a specific node from the adopted ResourceDiscoveryService
	 */
	public void setNodeNeighbors(String nodeId, List<String> neighbors) {
		
		if(!GlobalStorage.getMaGateCollectionReady()) {
			return;
		}
		
		try {
			GlobalStorage.findMaGateById(nodeId).getCommunityMonitor().foundCommunity(neighbors);
			
			// count this behaviour
			GlobalStorage.count_updateNeighborhood.incrementAndGet();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Generate a magateId-MaGateReference(key-value) pair 
	 * @param maGate
	 */
	public void maGateJoin(MaGateEntity maGate) {
		
		String id = maGate.getMaGateIdentity();
		
		try {
			
			if((MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Decentralized) && 
					(MaGateParam.systemIS == MaGateMessage.systemIS_Partial_ACO)) {
				this.netc.attachNode(id, hook);
			}
			
			// Important! The nest control in the simulator is started when the first
			// node gets added. So here we must wait a little bit for the nest control to start
			Thread.sleep(100);
			
			maGateIdList.add(id);
			GlobalStorage.maGateJoin(maGate);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void maGateDetach(String maGateId) {
		
		try {
			
			if((MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Decentralized) && 
					(MaGateParam.systemIS == MaGateMessage.systemIS_Partial_ACO)) {
				
				this.netc.detachNode(maGateId);
			}
			
			Thread.sleep(100);
			
			maGateIdList.remove(maGateId);
			GlobalStorage.maGateDetach(maGateId);
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Remove all magateId-MaGateReference(key-value) pair 
	 */
	public void allMaGateDetach() {
		
		try {
			
			if((MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Decentralized) && 
					(MaGateParam.systemIS == MaGateMessage.systemIS_Partial_ACO)) {
				
				for (String nodeId : this.maGateIdList) {
					this.netc.detachNode(nodeId);
					Thread.sleep(200);
				}
			}
			
			maGateIdList.clear();
			GlobalStorage.allMaGateDetach();
			
			Thread.sleep(10000);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}


