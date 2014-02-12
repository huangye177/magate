package ch.hefr.gridgroup.magate.casa;

public class MsgInform {

public String requesterNodeId = "";
	
	public int numOfPE = 0;
	
	public String typeOfOS = "";
	
	public String jobId = "";
	
	public double estResponseTime = 0;
	
	public double avgQueuingTime = 0;
	
	public double replicsOfMsgRequest = 0;
	
	public MsgInform (String requesterNodeId, String jobId, int numOfPE, String typeOfOS, 
			double estResponseTime, double avgQueuingTime, double replicsOfMsgRequest) {
		
		this.requesterNodeId = requesterNodeId;
		this.jobId    = jobId;
		this.numOfPE  = numOfPE;
		this.typeOfOS = typeOfOS;
		
		this.estResponseTime     = estResponseTime;
		this.avgQueuingTime      = avgQueuingTime;
		this.replicsOfMsgRequest = replicsOfMsgRequest;
		
	}
}
