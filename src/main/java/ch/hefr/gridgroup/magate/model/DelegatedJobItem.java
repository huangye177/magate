package ch.hefr.gridgroup.magate.model;

import ch.hefr.gridgroup.magate.env.MaGateMediator;

public class DelegatedJobItem {
	
	public String jobId;
	
	public String remodeId;
	
	public double promisedResponseTime;
	
	public double timestamp;

	public DelegatedJobItem(String jobId, String remoteId, double promisedResponseTime) {
		
		this.jobId                = jobId;
		this.remodeId             = remoteId;
		this.promisedResponseTime = promisedResponseTime;
		this.timestamp            = MaGateMediator.getSystemTime();
		
	}
}
