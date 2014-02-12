package ch.hefr.gridgroup.magate.em;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.MaGateEntity;
import ch.hefr.gridgroup.magate.em.ssl.BlatAntNestController;
import ch.hefr.gridgroup.magate.em.ssl.IBlatAntNestController;
import ch.hefr.gridgroup.magate.em.ssl.IBlatAntNestObserver;
import ch.hefr.gridgroup.magate.env.MaGateMessage;
import ch.hefr.gridgroup.magate.env.MaGateParam;
import ch.hefr.gridgroup.magate.storage.*;

public class SSLService implements IResDiscovery {

	private static Log log = LogFactory.getLog(SSLService.class);
	
	private MaGateEntity maGate;
	private String maGateIdentity;
	
//	private int timeout = 500;
	private int counter = 0;
	
	private Map<String, String> results = new HashMap<String, String>();
	
	private HashMap<String,Object> maGateProfile = new HashMap<String,Object>();

	public SSLService(MaGateEntity maGate) {
		
		this.maGate         = maGate;
		this.maGateIdentity = maGate.getMaGateIdentity();
		
		// Create and preserve the profile of MaGate Node
		this.maGateProfile = new HashMap<String,Object>();
		
		maGateProfile.put(MaGateMessage.MatchProfile_OS, this.maGate.getLRM().getOsType());
		maGateProfile.put(MaGateMessage.MatchProfile_CPUCount, new Integer(this.maGate.getLRM().getNumOfPEPerResource()));
		maGateProfile.put(MaGateMessage.MatchProfile_VO, this.maGate.getLRM().getVO());
		
//		maGateProfile.put(MaGateMessage.MatchProfile_ExePrice, new Double(-1.0));
		
		this.maGate.setMaGateProfile(maGateProfile);
		
		// IMPORTANT: register a Nest for resource discovery
		try {
			
			this.maGate.getMaGateInfra().updateProfile(maGateIdentity, maGateProfile);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Send synchronous query for searching remote MaGates
	 */
	public String[] syncSearchRemoteNode(ConcurrentHashMap<String,Object> extQuery) {
		
		String queryId       = this.maGateIdentity + "_" + java.util.UUID.randomUUID().toString();
		String[] resultArray = null;
		
		this.results.put(queryId, "");
		
		HashMap<String,Object> query = new HashMap<String,Object>();
		query.put(MaGateMessage.MatchProfile_OS, extQuery.get(MaGateMessage.MatchProfile_OS));
		query.put(MaGateMessage.MatchProfile_CPUCount, extQuery.get(MaGateMessage.MatchProfile_CPUCount));
		
		try {
			
			// send the query
			this.maGate.getMaGateInfra().startQuery(this.maGateIdentity, queryId, query);
			
			// record the search issue
			int currentCommunitySearch = this.maGate.getStorage().getCommunitySearch().get();
			this.maGate.getStorage().setCommunitySearch(new AtomicInteger(currentCommunitySearch + 1));
			
			/***************************************************************************
			 * Sleep a while and collect the returned results while the time is reached
			 ***************************************************************************/
			Thread.sleep(MaGateParam.timeSearchCommunity);
			
			String result = this.results.get(queryId);
			resultArray = result.split("_");
			
			// Remove Redundancy  
			this.results.remove(queryId);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// In case new nodes discovered, put them into the cached netowrkNeighbor Map
		NetworkNeighborsManager.cacheNetworkNeighbors(this.maGate, resultArray);
		
		return resultArray;
	}
	
	/**
	 * Result found from the infrastructure
	 */
	public void onResultFound(String queryId, String matchedResult) {
		
		if(this.results.containsKey(queryId)) {
			String existResult = this.results.get(queryId);
			this.results.put(queryId, existResult + matchedResult + "_");
			
		} else {
			this.results.put(queryId, matchedResult + "_");
			
		}
	}
	
	public HashMap<String, Object> getMaGateProfile() {
		return maGateProfile;
	}
	
}


