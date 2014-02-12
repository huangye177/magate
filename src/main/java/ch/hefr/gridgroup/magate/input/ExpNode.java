package ch.hefr.gridgroup.magate.input;

import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.env.MaGateMessage;
import ch.hefr.gridgroup.magate.model.Job;

public class ExpNode implements Cloneable {

	private static Log log = LogFactory.getLog(ExpNode.class);
	
	private String nodeId = "";
	
	private String nodeOS = "";
	private String nodeArch = "";
	private String voId = "";

	private int numberOfResource = 0;
	private int numberOfPEperResource = 0;
	private int peMIPS = 0;
	private String localMatchMakerPolicy = "";
	private int nodeStatus = MaGateMessage.NodeStatus_AllStable;

	private ConcurrentLinkedQueue<Job> jobQueue = new ConcurrentLinkedQueue<Job>();

	//
	public ExpNode(String nodeId, String nodeArch, String nodeOS, int numberOfResource, int numberOfPEperResource, 
			int peMIPS, String voId, String localMatchMakerPolicy, int nodeStatus) {
		
		if(nodeId == null || nodeId.trim().equals("")) {
			this.nodeId = java.util.UUID.randomUUID().toString();
		} else {
			this.nodeId = nodeId;
		}
		
		this.nodeArch = nodeArch;
		this.nodeOS = nodeOS;
		this.numberOfResource = numberOfResource;
		this.numberOfPEperResource = numberOfPEperResource;
		this.peMIPS = peMIPS;
		this.voId = voId;
		this.localMatchMakerPolicy = localMatchMakerPolicy;
		this.nodeStatus = nodeStatus;
		
	}
	
	public void addJob(Job job) {
		this.jobQueue.add(job);
	}
	
	public ExpNode clone() {
        
		ExpNode cloneNode = null;
        try {
        	cloneNode = (ExpNode) super.clone();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        return cloneNode;
    }
	
	
	public String getNodeId() {
		return nodeId;
	}


	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}


	public String getNodeOS() {
		return nodeOS;
	}


	public void setNodeOS(String nodeOS) {
		this.nodeOS = nodeOS;
	}


	public String getNodeArch() {
		return nodeArch;
	}


	public void setNodeArch(String nodeArch) {
		this.nodeArch = nodeArch;
	}


	public String getVoId() {
		return voId;
	}


	public void setVoId(String voId) {
		this.voId = voId;
	}


	public int getNumberOfResource() {
		return numberOfResource;
	}


	public void setNumberOfResource(int numberOfResource) {
		this.numberOfResource = numberOfResource;
	}


	public int getNumberOfPEperResource() {
		return numberOfPEperResource;
	}


	public void setNumberOfPEperResource(int numberOfPEperResource) {
		this.numberOfPEperResource = numberOfPEperResource;
	}


	public int getPeMIPS() {
		return peMIPS;
	}


	public void setPeMIPS(int peMIPS) {
		this.peMIPS = peMIPS;
	}
	
	public ConcurrentLinkedQueue<Job> getJobQueue() {
		return jobQueue;
	}

	public void setJobQueue(ConcurrentLinkedQueue<Job> jobQueue) {
		this.jobQueue = jobQueue;
	}

	public String getLocalMatchMakerPolicy() {
		return localMatchMakerPolicy;
	}

	public void setLocalMatchMakerPolicy(String localMatchMakerPolicy) {
		this.localMatchMakerPolicy = localMatchMakerPolicy;
	}
	
	public int getNodeStatus() {
		return nodeStatus;
	}
	
}



