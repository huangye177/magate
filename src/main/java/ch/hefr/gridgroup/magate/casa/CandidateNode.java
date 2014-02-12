package ch.hefr.gridgroup.magate.casa;

import ch.hefr.gridgroup.magate.MaGateEntity;

public class CandidateNode {
	
	public MaGateEntity node = null;
	
	public double estTimeForJob = 0;
	
	public double estTimeForLoadOfLocalJobQueue = 0;
	
	public double estTimeForLoadOfShadowJobQueue = 0;
	
	public CandidateNode(MaGateEntity node, double estTimeForJob, 
			double estTimeForLoadOfLocalJobQueue, double estTimeForLoadOfShadowJobQueue) {
		
		this.node = node;
		this.estTimeForJob = estTimeForJob;
		this.estTimeForLoadOfLocalJobQueue  = estTimeForLoadOfLocalJobQueue;
		this.estTimeForLoadOfShadowJobQueue = estTimeForLoadOfShadowJobQueue;
		
	}
	
	public double nodeResProbability    = 0.0001;
	
	public double nodeTrustProbability  = 0.0001;
	
	public double nodeMergedProbability = 0.0001;

}
