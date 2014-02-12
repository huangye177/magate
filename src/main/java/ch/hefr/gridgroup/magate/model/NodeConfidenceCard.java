package ch.hefr.gridgroup.magate.model;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NodeConfidenceCard {

	private static Log log = LogFactory.getLog(NodeConfidenceCard.class);
	
	private int numOfInteraction = 0;

//	private AtomicInteger numOfSuccInteraction = new AtomicInteger(0);
//	
//	private AtomicInteger numOfFailInteraction = new AtomicInteger(0);
//	
//	private AtomicInteger numOfCFInteraction = new AtomicInteger(0);
//	
//	private AtomicInteger numOfCFSuccInteraction = new AtomicInteger(0);
//	
//	private AtomicInteger numOfCFFailInteraction = new AtomicInteger(0);
//	
//	private double nodeWeight = 0;
	
	private String hostingNodeId;
	
	private String hostingNodeName;
	
	private String remoteNodeId;
	
	public NodeConfidenceCard(String hostingNodeId, String hostingNodeName, String remoteNodeId) {
		this.hostingNodeId   = hostingNodeId;
    	this.hostingNodeName = hostingNodeName;
    	this.remoteNodeId    = remoteNodeId;
	}
	
	public int getNumOfInteraction() {
		return numOfInteraction;
	}

	public void setNumOfInteraction(int numOfInteraction) {
		this.numOfInteraction = numOfInteraction;
	}
	
//	public int addNumOfInteraction() {
//		return this.numOfInteraction.incrementAndGet();
//	}

//	public int getNumOfSuccInteraction() {
//		return numOfSuccInteraction.get();
//	}
//
//	public void setNumOfSuccInteraction(int numOfSuccInteraction) {
//		this.numOfSuccInteraction.set(numOfSuccInteraction);
//	}
//	
//	public int addNumOfSuccInteraction(int delta) {
//		return this.numOfSuccInteraction.addAndGet(delta);
//	}
//
//	public int getNumOfFailInteraction() {
//		return numOfFailInteraction.get();
//	}
//
//	public void setNumOfFailInteraction(int numOfFailInteraction) {
//		this.numOfFailInteraction.set(numOfFailInteraction);
//	}
//	
//	public int addNumOfFailInteraction(int delta) {
//		return this.numOfFailInteraction.addAndGet(delta);
//	}
//
//	public int getNumOfCFInteraction() {
//		return numOfCFInteraction.get();
//	}
//
//	public void setNumOfCFInteraction(int numOfCFInteraction) {
//		this.numOfCFInteraction.set(numOfCFInteraction);
//	}
//	
//	public int addNumOfCFInteraction(int delta) {
//		return this.numOfCFInteraction.addAndGet(delta);
//	}
//
//	public int getNumOfCFSuccInteraction() {
//		return numOfCFSuccInteraction.get();
//	}
//
//	public void setNumOfCFSuccInteraction(int numOfCFSuccInteraction) {
//		this.numOfCFSuccInteraction.set(numOfCFSuccInteraction);
//	}
//	
//	public int addNumOfCFSuccInteraction(int delta) {
//		return this.numOfCFSuccInteraction.addAndGet(delta);
//	}
//
//	public int getNumOfCFFailInteraction() {
//		return numOfCFFailInteraction.get();
//	}
//
//	public void setNumOfCFFailInteraction(int numOfCFFailInteraction) {
//		this.numOfCFFailInteraction.set(numOfCFFailInteraction);
//	}
//	
//	public int addNumOfCFFailInteraction(int delta) {
//		return this.numOfCFFailInteraction.addAndGet(delta);
//	}
//
//	public synchronized double getNodeWeight() {
//		return nodeWeight;
//	}
//
//	public synchronized void setNodeWeight(double nodeWeight) {
//		this.nodeWeight = nodeWeight;
//	}

	public String getHostingNodeId() {
		return hostingNodeId;
	}

	public String getHostingNodeName() {
		return hostingNodeName;
	}
	
	public String getRemoteNodeId() {
		return remoteNodeId;
	}
}


