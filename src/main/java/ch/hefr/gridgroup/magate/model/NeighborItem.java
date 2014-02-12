package ch.hefr.gridgroup.magate.model;

public class NeighborItem {

	public String maGateId;
	
	public String operatingSystem;
	
	public int numOfPE;
	
	public boolean isNetworkNeighbor = false;
	
	public boolean isCFCNeighbor = false;
	
	public NeighborItem(String maGateId, String operatingSystem, int numOfPE, 
			boolean isNetworkNeighbor, boolean isCFCNeighbor) {
		
		this.maGateId = maGateId;
		this.operatingSystem = operatingSystem;
		this.numOfPE = numOfPE;
		this.isNetworkNeighbor = isNetworkNeighbor;
		this.isCFCNeighbor = isCFCNeighbor;
	}
}
