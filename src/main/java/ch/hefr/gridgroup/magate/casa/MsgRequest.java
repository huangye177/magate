package ch.hefr.gridgroup.magate.casa;

public class MsgRequest {
	
	public String requesterNodeId = "";
	
	public int numOfPE = 0;
	
	public String typeOfOS = "";
	
	public String jobId = "";
	
	public double replicsOfMsgRequest = 0;
	
	public MsgRequest (String requesterNodeId, String jobId, int numOfPE, String typeOfOS, double replicsOfMsgRequest) {
		
		this.requesterNodeId = requesterNodeId;
		
		this.jobId    = jobId;
		this.numOfPE  = numOfPE;
		this.typeOfOS = typeOfOS;
		
		this.replicsOfMsgRequest = replicsOfMsgRequest;
		
	}

}
