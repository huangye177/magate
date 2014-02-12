package ch.hefr.gridgroup.magate.em;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.MaGateEntity;
import ch.hefr.gridgroup.magate.env.MaGateParam;
import ch.hefr.gridgroup.magate.model.NeighborItem;
import ch.hefr.gridgroup.magate.storage.GlobalStorage;


public class NetworkNeighborsManager {

	private static Log log = LogFactory.getLog(NetworkNeighborsManager.class);
	
	/** 
	 * Once a remote node discovered due to "network neighborhood update policy" OR "community search policy", 
	 * it will be put into the networkNeighbor list of the hosting node's storage
	 * 
	 * @param maGate
	 * @param remoteMaGateArray
	 */
	public static void cacheNetworkNeighbors(MaGateEntity maGate, String[] remoteMaGateArray) {
		
		for(int i = 0; i < remoteMaGateArray.length; i++) {
			
			String remoteId = remoteMaGateArray[i];
			
			if(remoteId == null) {
				log.error("cacheNetworkNeighbors method found node with id \"NULL\" for hosting node: " 
						+ maGate.getMaGateIdentity() + "by using remoteDiscoveryPolicy 'communitysearch'. " +
						"member of the discovered remote id list: \n");
				for(String id : remoteMaGateArray) {
					System.out.println("###: " + id);
				}
				System.exit(0);
			}
			
			cacheSingleNetworkNeighbor(maGate, remoteId);
		}
	}
	
	/**
	 * Once a set of remote nodes discovered due to "network neighborhood update policy" OR "community search policy", 
	 * it will be put(replace) into the networkNeighbor list of the hosting node's storage
	 * 
	 * @param maGate
	 * @param remoteMaGateList
	 */
	public static void cacheNetworkNeighbors(MaGateEntity maGate, List<String> remoteMaGateList) {
		
		maGate.getStorage().clear_networkNeighbors();
		
		for(String remoteId : remoteMaGateList) {
			
			if(remoteId == null) {
				log.error("cacheNetworkNeighbors method found node with id \"NULL\" for hosting node: " 
						+ maGate.getMaGateIdentity() + "by using remoteDiscoveryPolicy 'neighborupdate'. " +
						"member of the discovered remote id list: \n");
				for(String id : remoteMaGateList) {
					log.debug("###: " + id);
				}
				System.exit(0);
			}
			
			cacheSingleNetworkNeighbor(maGate, remoteId);
		}
	}
	
	/**
	 * Cache a single node as network neighbor
	 * 
	 * @param maGate
	 * @param remoteId
	 */
	public static void cacheSingleNetworkNeighbor(MaGateEntity maGate, String remoteId) {
		
		if(remoteId == null) {
			log.error("cacheSingleNetworkNeighbor method found node with id \"NULL\" for hosting node: " + maGate.getMaGateIdentity() + "; ");
			System.exit(0);
		}
		
		String hostNodeId = maGate.getMaGateIdentity();
		
		// Check the obtained remote Id, and cache them if necessary
		if( (!hostNodeId.equals(remoteId)) && (!maGate.getStorage().contain_networkNeighbors(remoteId))) {
			
			MaGateEntity remoteNode = GlobalStorage.findMaGateById(remoteId);
			if(remoteNode != null) {
				
				if(MaGateParam.isolatedVO) {
					if(maGate.getLRM().getVO().equals(remoteNode.getLRM().getVO())) {
						NeighborItem item = new NeighborItem(remoteId, remoteNode.getLRM().getOsType(), 
								remoteNode.getLRM().getNumOfPEPerResource(), true, false);
						
						maGate.getStorage().join_networkNeighbors(item);
					}
					
				} else {
					
					NeighborItem item = new NeighborItem(remoteId, remoteNode.getLRM().getOsType(), 
							remoteNode.getLRM().getNumOfPEPerResource(), true, false);
					
					maGate.getStorage().join_networkNeighbors(item);
				}
				
			} else {
				// remote node is unavailable, such as the hook node
			}
			
		} else {
			
		}
	}
	
}
