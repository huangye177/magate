package ch.hefr.gridgroup.magate.model;

import java.util.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import ch.hefr.gridgroup.magate.env.MaGateMediator;
import ch.hefr.gridgroup.magate.env.*;

public class RemoteNodeReputationItem {
	
	public String remodeNodeId = "";
	
	/** remote node reputation considering both all completed and ongoing job delegations */
	private double remodeNodeReputation = 0;
	
	/** remote node reputation considering both recent completed and ongoing job delegations */
	private double remodeNodeRecentReputation = 0;

	private boolean isCFNode = false;
	
	
	private ConcurrentHashMap<String, Double> eventScoreMap     = new ConcurrentHashMap<String, Double>();

	private ConcurrentHashMap<String, Double> eventTimestampMap = new ConcurrentHashMap<String, Double>();

	public RemoteNodeReputationItem(String remodeNodeId) {
		
		this.remodeNodeId = remodeNodeId;
		
	}
	
	public void putScore(double score) {
		
		double currentSystemTime = MaGateMediator.getSystemTime();
		String eventId = UUID.randomUUID().toString().replace("-", "");
		
		this.eventTimestampMap.put(eventId, new Double(currentSystemTime));
		this.eventScoreMap.put(eventId, new Double(score));
		
	}
	
	
	
	public ConcurrentHashMap<String, Double> getEventScoreMap() {
		return eventScoreMap;
	}

	public ConcurrentHashMap<String, Double> getEventTimestampMap() {
		return eventTimestampMap;
	}
	
	public double getRemodeNodeReputation() {
		return remodeNodeReputation;
	}

	public void setRemodeNodeReputation(double remodeNodeReputation) {
		this.remodeNodeReputation = remodeNodeReputation;
	}
	
	public double getRemodeNodeRecentReputation() {
		return remodeNodeRecentReputation;
	}

	public void setRemodeNodeRecentReputation(double remodeNodeRecentReputation) {
		this.remodeNodeRecentReputation = remodeNodeRecentReputation;
	}

	public boolean isCFNode() {
		return isCFNode;
	}

	public void setCFNode(boolean isCFNode) {
		this.isCFNode = isCFNode;
	}

}


