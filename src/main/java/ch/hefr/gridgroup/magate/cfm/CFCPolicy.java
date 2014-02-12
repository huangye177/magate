package ch.hefr.gridgroup.magate.cfm;

public class CFCPolicy {

	/**
	 * No CFC policy applied
	 */
	public static final String CFC_NONE = "CFC_NONE";
	
	public static final String CFC_RESTRICT_NETWORK = "CFC_R_NETWORK";
	
	/**
	 * Grab all "network neighbors" of a Critical Friend node (CF) as the hosting node's own CFs, 
	 * so that the critical friend community of a hosting node will get massive
	 * 
	 *  code affection:
	 *  CFCController.cacheCFCNeighbors(String remoteNodeId)
	 */
	public static final String CFC_MASSIVE_NETWORK = "CFC_M_NETWORK";
	
	/**
	 * Grab all "Critical Friend nodes" of a Critical Friend node (CF) as the hosting node's own CFs, 
	 * so that the critical friend community of a hosting node will get massive
	 */
	public static final String CFC_OPTIMIZED_NETWORK = "CFC_O_NETWORK";
	
	/**
	 * 
	 */
	public static final String CFC_SIMPLE = "CFC_SIMPLE";
	
	/**
	 * Once a CF(tier 1) of a host node is not able to satisfy the job delegation request, it will try to delegate
	 * such job to other remote node using his own approach (network neighbors, CF(tier 2) or direct community search)
	 */
	public static final String CFC_RESTRICT_DISPATCH = "CFC_R_DISPATCH";
	
	public static final String CFC_TOLERANT_DISPATCH = "CFC_T_DISPATCH";
	
	/**
	 * Once a host node has unused free resource, it can contact its CFs(tier 1) for asking job delegation
	 */
	public static final String CFC_PUSH = "CFC_PUSH";
	
	/**
	 * CF node selection based on CF-Weight
	 */
	public static final String CFC_WEIGHT_SELECTION = "CFC_W_SELECTION";
}
