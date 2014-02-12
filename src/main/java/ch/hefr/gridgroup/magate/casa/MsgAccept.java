package ch.hefr.gridgroup.magate.casa;

public class MsgAccept {

	public boolean isAccepted = false;
	
	public String responderNodeId = "";
	
	public String jobId = "";
	
	public double estTimeForJob = 0;
	
	public double estTimeForLoadOfLocalJobQueue = 0;
	
	public double estTimeForLoadOfShadowJobQueue = 0;
	
	public MsgAccept(boolean isAccepted, String responderNodeId, String jobId, 
			double estTimeForJob, double estTimeForLoadOfLocalJobQueue, double estTimeForLoadOfShadowJobQueue) {
		
		this.isAccepted      = isAccepted;
		this.responderNodeId = responderNodeId;
		this.jobId           = jobId;
		this.estTimeForJob   = estTimeForJob;
		this.estTimeForLoadOfLocalJobQueue  = estTimeForLoadOfLocalJobQueue;
		this.estTimeForLoadOfShadowJobQueue = estTimeForLoadOfShadowJobQueue;
	}
}
