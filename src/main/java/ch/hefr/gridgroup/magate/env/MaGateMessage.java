package ch.hefr.gridgroup.magate.env;

import java.util.LinkedList;

public class MaGateMessage {
	
	////////////////////////////////////////////
    // Below are CONSTANTS attributes derived from GridSim
    /** The job has been created and added to the job object */
    public static final int JOB_CREATED = 0;

    /** The job has been assigned to a GridResource object as planned */
    public static final int JOB_READY = 1;

    /** The job has moved to a Grid node */
    public static final int JOB_QUEUED = 2;

    /** The job is in execution in a Grid node */
    public static final int JOB_INEXEC = 3;

    /** The job has been executed successfully */
    public static final int JOB_SUCCESS = 4;

    /** The job is failed */
    public static final int JOB_FAILED = 5;

    /** The job has been canceled.  */
    public static final int JOB_CANCELED = 6;

    /** The job has been paused. It can be resumed by changing the status
     * into <tt>RESUMED</tt>.
     */
    public static final int JOB_PAUSED = 7;

    /** The job has been resumed from <tt>PAUSED</tt> state. */
    public static final int JOB_RESUMED = 8;

    /** The job has failed due to a resource failure */
    public static final int JOB_FAILED_RESOURCE_UNAVAILABLE = 9;
    
    /// --- --- ---
    
    public static final String JOBINFO_REMOTE_REQUEST_SENT = "2048";
    
    public static final String REMTOE_MAGATE_REFUSE = "2049";
    
    public static final String REMTOE_MAGATE_FAIL = "2050";
    
    public static final String REMTOE_MAGATE_SUCCESS = "2051";
    
    public static final String ALL_REMTOE_MAGATE_REFUSE = "2052";
    
    public static final String Res_Discovery_From_Direct_Neighbors = "Res_Discovery_From_Direct_Neighbors";
    
    public static final String Res_Discovery_From_Community_Search = "Res_Discovery_From_Community_Search";
    
    public static final String Delegation_Neg_None = "Delegation_Neg_None";
    
    public static final String Delegation_Neg_Input_Accessable = "Delegation_Neg_Input_Accessable";
    
    public static final String Delegation_Neg_Input_Efficient_Accessable = "Delegation_Neg_Input_Efficient_Accessable";
    
    public static final String NegotiationAgreement_PE = "NegotiationAgreement_PE";
    
    public static final String NegotiationAgreement_None = "NegotiationAgreement_None";
    
    public static final String Interaction_ObjectCall = "Interaction_ObjectCall";
    
    public static final String Interaction_AgreementCall = "Interaction_AgreementCall";
    
    /// --- --- ---
    
	public static final int AskForLocalResource = 1025;
	
    public static final int RegisterResourceToLRMStorage = 1026;
    
    public static final int MaGateTest = 1028;
    
    public static final int JobToMatchMaker = 1029;
    
//    public static final int JobToMatchMakerFromCommunity = 1047;
    
    public static final int ScheduleMadeByMatchMaker = 1030;
    
    public static final int JobSubmittedToResource = 1031;
    
    public static final int AllJobProcessed = 1032;
    
    public static final String PolicyExistingSchedule = "PolicyExistingSchedule";
    
    public static final String PolicyFCFS = "FCFS";
    
    public static final String PolicyEasyBF = "EasyBF";
    
    public static final String PolicyFlexibleBF = "FlexibleBF";
    
    /** SJF: the scheduling order depends on the jobs’ ERT, with shorter jobs being executed first */
    public static final String PolicySJF = "SJF";  
    
    /** EDF: used only for deadline scheduling, this policy prioritizes jobs with an earlier
    deadline (as specified in their profile). */
    public static final String PolicyEDF = "PolicyEDF";  
    
    public static final int JobArrival = 1048;
    
    public static final int Heartbeat = 1049;
    
    public static final int SimEndPolicy_enforcedHeartbeat = 1050;
    
    public static final int SimEndPolicy_natureHeartbeat = 1051;
    
    
    /// --- --- ---
    
    public static final String MatchProfile_OS = "os";
    
    public static final String MatchProfile_CPUCount = "num_cpu";
    
    public static final String MatchProfile_ExePrice = "price";
    
    public static final String MatchProfile_VO = "vo";
    	
    /// --- --- ---
    
    public static final int SchedulingScheme_Independent   = 6016;
    public static final int SchedulingScheme_Centralized   = 6017;
    public static final int SchedulingScheme_Decentralized = 6018;
    
    /// --- --- ---
    
    public static final int JobDistMode_FairDist       = 7016;
    public static final int JobDistMode_CustomizedDist = 7027;
    
    /// --- --- ---
    
    public static final int systemIS_Partial_ACO = 7030;
    public static final int systemIS_Partial_SIM = 7031;
    public static final int systemIS_Global      = 7032; 
    
    /// --- --- ---
    
//    public static final int top500Profile_job = 8031;
//    public static final int top500Profile_res = 8032; 
    
    public static final double CFM_CFNode_Barrier = 0.6;
    
    public static final int CFM_SCORE_SUCCESS  = 1;
	
	public static final int CFM_SCORE_MODEST   = 0;
	
	public static final int CFM_SCORE_NEGATIVE = -1;
	
	
	
	/// --- --- ---
	/** all node are functioning correctly */
	public static final int TopologyNodeStatus_AllStable = 0;   
	
	/** half of the nodes are not able to deliver jobs back to their original nodes during day time (8am-8pm) */
	public static final int TopologyNodeStatus_HalfLazy  = 1;   
	
	public static final int NodeStatus_AllStable = 0;   
	
	public static final int NodeStatus_HalfLazy  = 1; 
}


