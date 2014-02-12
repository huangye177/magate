package ch.hefr.gridgroup.magate.casa;

import ch.hefr.gridgroup.magate.MaGateEntity;

public class ShadowNode {

	public String nodeId = "";
	
	public MaGateEntity node = null;
	
	int activePE = 0;
	
	int availablePE = 0;
	
//	int justAssignedJobReservation = 0;
	
	public ShadowNode(String nodeId, MaGateEntity node, 
			int activePE, int availablePE) {
		
		this.nodeId   = nodeId;
		this.node     = node;
		this.activePE = activePE;
		this.availablePE      = availablePE;
		
	}
}
