package ch.hefr.gridgroup.magate.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.env.MaGateMediator;
import ch.hefr.gridgroup.magate.env.MaGateParam;
import ch.hefr.gridgroup.magate.env.MaGateToolkit;
import ch.hefr.gridgroup.magate.env.ResourceEngine;
import ch.hefr.gridgroup.magate.model.DelegatedJobItem;
import ch.hefr.gridgroup.magate.model.MMResult;
import ch.hefr.gridgroup.magate.model.NeighborItem;
import ch.hefr.gridgroup.magate.model.NodeConfidenceCard;
import ch.hefr.gridgroup.magate.model.RemoteNodeReputationItem;
import ch.hefr.gridgroup.magate.model.ResourceInfo;

/** 
 * Class Storage
 *  Storage/ Storage-access to preserve data used in MaGate 
 */
public class MaGateStorage {
    
	private static Log log = LogFactory.getLog(MaGateStorage.class);
	private final ReentrantReadWriteLock resInfoRWLock = new ReentrantReadWriteLock();
	private final Lock resInfoReadLock  = resInfoRWLock.readLock();
	private final Lock resInfoWriteLock = resInfoRWLock.writeLock();
	
//	private final Lock usageLock = new ReentrantLock();
	
	/** Constructor 
	 * @param res_osType */
    public MaGateStorage(String maGateIdentity, String maGateNickName, String res_osType, String res_vo) {
    	
    	this.maGateIdentity = maGateIdentity;
    	this.maGateNickName = maGateNickName;
    	this.res_osType     = res_osType;
    	this.res_vo         = res_vo;
    	
    	this.networkNeighbors  = new ConcurrentHashMap<String, NeighborItem>();
    	this.cfcNeighbors      = new ConcurrentHashMap<String, NeighborItem>();
    	this.freeCFCNeighbors  = new ConcurrentHashMap<String, NeighborItem>();
    	this.nodeConfidenceCards = new ConcurrentHashMap<String, NodeConfidenceCard>();
    	
    	this.remoteNodeReputationList = new ConcurrentHashMap<String, RemoteNodeReputationItem>();
    	this.delegatedjobList         = new ConcurrentHashMap<String, DelegatedJobItem>();
    	
    	this.mmResult = new MMResult();
    }

	private String maGateIdentity;
	
	private String maGateNickName;
	
	private String res_osType;
	
	private String res_vo;

//	private AtomicInteger maxResourcePE = new AtomicInteger(0);
	
	private AtomicInteger expectedNumOfLocalJob = new AtomicInteger(0);
	
	/** Total active PEs */
	private AtomicInteger totalActivePEs = new AtomicInteger(0);

	/** Total available PEs */
    private AtomicInteger totalNumOfPEs = new AtomicInteger(0);
    
    /** Total available PEs */
    private AtomicInteger totaPromisedPEs = new AtomicInteger(0);

	/** Number of PEs requested by currently present jobs */
    private AtomicInteger numOfRequestedPEs = new AtomicInteger(0);

	/** Local resource count */
    private AtomicInteger numOfLocalResource = new AtomicInteger(0);

	/** Best machine's MIPS rating */
	private AtomicInteger bestMachineMIPS = new AtomicInteger(0);
    
    private AtomicBoolean storageInitiaized = new AtomicBoolean(false);
    
    private AtomicBoolean jobPrepared = new AtomicBoolean(false);
    
    private AtomicBoolean simulatedResourceReady = new AtomicBoolean(false);
    
//    private AtomicInteger localSearch = new AtomicInteger(0);

	private AtomicInteger communitySearch = new AtomicInteger(0);
    
    /// --- --- ---

    private AtomicLong jobUsage = new AtomicLong();
    
    private AtomicLong resUsage = new AtomicLong();
    
    /** Resource load efficiency = resourceJobExecutionLoad / resourceMaxLoad; FOR checking scheduling results */
	private AtomicLong resourceUtilization = new AtomicLong();
//	private AtomicLong weightedResourceUtilization = new AtomicLong();
    
	/// --- seldom used (if not Unnecessary) synchronized attributes ---

	/** List of local resource IDs, used in simulation */
    private int resourceIdArray[];
    
	/** List of local resource names, used in simulation */
    private String resourceNameArray[];
    
    /** Carrier of statistic results */
    private MMResult mmResult;
    
    /** PE usage = activePE / totalPE */
    private double peUsage = 0.0;
    
    /// --- synchronized queue storage ---

	/** List of all sim-based resource characteristic profiles */
    private LinkedList<ResourceInfo> resourceInfoList;
    
////    /** List of community neighbors */
//    private List<String> networkNeighborList;
	
    /** List of neighbors due to network behaviours, e.g. neighbor update and resource discovery */
    private ConcurrentHashMap<String, NeighborItem> networkNeighbors;
    
    /** List of neighbors due to CFC behaviours, e.g. neighbor update and resource discovery */
    private ConcurrentHashMap<String, NeighborItem> cfcNeighbors;
    
    private ConcurrentHashMap<String, NeighborItem> freeCFCNeighbors;
    
    private ConcurrentHashMap<String, NodeConfidenceCard> nodeConfidenceCards;
    
    private ConcurrentHashMap<String, RemoteNodeReputationItem> remoteNodeReputationList;
    
    private ConcurrentHashMap<String, DelegatedJobItem> delegatedjobList;
    
	// --- --- --- --- ---
    
//    public AtomicInteger totalReceivedJob = new AtomicInteger(0);
    
    // --- --- --- --- ---
    // --- --- --- --- ---
    
    /**
     * Get remote node list of the SAME VO, including the "discovered network neighbors" and "Critical Friend nodes"
     */
	public synchronized List<String> getAllNeighborList() {
		
		// Counter
		GlobalStorage.count_queryNeighbors.incrementAndGet();
		
		List<String> knownRemoteNodes = new LinkedList<String>();
		
		Enumeration<String> networkNeighborIds = networkNeighbors.keys();
		
		while(networkNeighborIds.hasMoreElements()) {
			
			String nodeId = networkNeighborIds.nextElement();
			
			if(MaGateParam.isolatedVO) {
				if(this.res_vo.equals(GlobalStorage.findMaGateById(nodeId).getLRM().getVO())) {
					knownRemoteNodes.add(nodeId);
				}
			} else {
				knownRemoteNodes.add(nodeId);
			}
			
		}
		
//		if(this.cfcNeighbors != null && this.cfcNeighbors.size() != 0) {
//			
//			for(NeighborItem freeCFCNeighbor : this.cfcNeighbors.values()) {
//				
//				if(MaGateParam.isolatedVO) {
//					if(this.res_vo.equals(GlobalStorage.findMaGateById(freeCFCNeighbor.maGateId).getLRM().getVO())) {
//						knownRemoteNodes.add(freeCFCNeighbor.maGateId);
//					}
//				} else {
//					knownRemoteNodes.add(freeCFCNeighbor.maGateId);
//				}
//			}
//		}
		
//		if(this.freeCFCNeighbors != null && this.freeCFCNeighbors.size() != 0) {
//			for(NeighborItem freeNeighbor : this.freeCFCNeighbors.values()) {
//				knownRemoteNodes.add(freeNeighbor.maGateId);
//			}
//		}
		
		return knownRemoteNodes;
	}
	
	
	/**
	 * Get remote node list which are considered as the network neighbors due to adopted Information System
	 * @return
	 */
//	public synchronized List<String> getNetworkNeighborList() {
//		
//		// Counter
//		GlobalStorage.count_queryNeighbors.incrementAndGet();
//		
//		return networkNeighborList;
//	}
	
//	public synchronized List<String> getCFCNeighborList() {
//		
//		List<String> knownRemoteNodes = new LinkedList<String>();
//		
//		if(cfcNeighbors != null && cfcNeighbors.size() != 0) {
//			
//			for(NeighborItem cfcNeighbor : cfcNeighbors.values()) {
//				knownRemoteNodes.add(cfcNeighbor.maGateId);
//			}
//		}
//		
//		if(this.freeCFCNeighbors != null && this.freeCFCNeighbors.size() != 0) {
//			for(NeighborItem freeNeighbor : this.freeCFCNeighbors.values()) {
//				knownRemoteNodes.add(freeNeighbor.maGateId);
//			}
//		}
//		
//		return knownRemoteNodes;
//	}

//	public synchronized void setKnownNeighborList(List<String> neighborList) {
//		this.networkNeighborList = neighborList;
//	}

	public AtomicInteger getCommunitySearch() {
		return communitySearch;
	}

	public void setCommunitySearch(AtomicInteger communitySearch) {
		this.communitySearch = communitySearch;
	}

	public AtomicBoolean isSimulatedResourceReady() {
		return simulatedResourceReady;
	}

	public void setSimulatedResourceReady(AtomicBoolean simulatedResourceReady) {
		this.simulatedResourceReady = simulatedResourceReady;
	}

	public AtomicBoolean isJobPrepared() {
		return jobPrepared;
	}

	public void setJobPrepared(AtomicBoolean jobPrepared) {
		this.jobPrepared = jobPrepared;
	}

	public AtomicBoolean isStorageInitiaized() {
		return storageInitiaized;
	}

	public void setStorageInitiaized(AtomicBoolean storageInitiaized) {
		this.storageInitiaized = storageInitiaized;
	}

//	public AtomicInteger getMaxResourcePE() {
//		return maxResourcePE;
//	}
//
//	public void setMaxResourcePE(AtomicInteger maxResourcePE) {
//		this.maxResourcePE = maxResourcePE;
//	}

	public void setBestMachineMIPS(AtomicInteger bestMachineMIPS) {
		this.bestMachineMIPS = bestMachineMIPS;
	}
	
	public AtomicInteger getNumOfLocalResource() {
		return numOfLocalResource;
	}

	public void setNumOfLocalResource(AtomicInteger numOfLocalResource) {
		this.numOfLocalResource = numOfLocalResource;
	}
	
	/** Get best machine's MIPS rating */
	public AtomicInteger getBestMachineMIPS() {
		return bestMachineMIPS;
	}
	
	// --- --- --- --- --- ---
	
	// --- --- --- --- --- ---
	
	public synchronized MMResult getMMResult() {
		return mmResult;
	}

	public synchronized void setMMResult(MMResult mmResult) {
		this.mmResult = mmResult;
	}
	
    public synchronized int[] getResourceIdArray() {
		return resourceIdArray;
	}

	public synchronized void setResourceIdArray(int[] resourceIdArray) {
		this.resourceIdArray = resourceIdArray;
	}

	public synchronized String[] getResourceNameArray() {
		return resourceNameArray;
	}

	public synchronized void setResourceNameArray(String[] resourceNameArray) {
		this.resourceNameArray = resourceNameArray;
	}
	
    /** PE usage = activePE / totalPE */
    public double getPeUsage() {
		return peUsage;
	}

    /** PE usage = activePE / totalPE */
	public void setPeUsage(double peUsage) {
		this.peUsage = peUsage;
	}
	
	// --- shared queue operation ---
	/** 
	 * Get all sim-based resource characteristic profiles of the node 
	 */
	public LinkedList<ResourceInfo> getResourceInfoList() {
		this.resInfoReadLock.lock();
		try {
			return resourceInfoList;
			
		} finally {
			this.resInfoReadLock.unlock();
		}
	}

	/** 
	 * Set all sim-based resource characteristic profiles of the node 
	 */
	public void setResourceInfoList(LinkedList<ResourceInfo> resourceInfoList) {
		this.resInfoWriteLock.lock();
		try {
			this.resourceInfoList = resourceInfoList;
			
		} finally {
			this.resInfoWriteLock.unlock();
		}
	}
	
	/// --- --- --- --- --- ---

	public AtomicLong getResourceEfficency() {
		return resourceUtilization;
	}

	public void setResourceUtilization(AtomicLong resourceUtilization) {
		this.resourceUtilization = resourceUtilization;
	}
	
	public AtomicLong getJobUsage() {
		return jobUsage;
	}

	public void setJobUsage(AtomicLong jobUsage) {
		this.jobUsage = jobUsage;
	}
	
	public AtomicLong getResUsage() {
		return resUsage;
	}

	public void setResUsage(AtomicLong resUsage) {
		this.resUsage = resUsage;
	}
	
	/// --- --- --- --- --- ---
	
	/** Get total active PEs */
	public AtomicInteger getTotalActivePEs() {
		return totalActivePEs;
	}

	/** Get total available PEs */
	public AtomicInteger getTotalNumOfPEs() {
		return totalNumOfPEs;
	}
	
//	/** Get total number of non-promised free PEs */
//	public int getTotalPromisedPEs() {
//		return this.totaPromisedPEs.get();
//	}
	
	public int getTotalVirtualFreePEs() {
		return this.totalNumOfPEs.get() - this.totalActivePEs.get();
//		return this.totalNumOfPEs.get() - this.totalActivePEs.get() - this.totaPromisedPEs.get();
	}
	
	public void addPromisedPEs(int jobNumPE) {
		
	}
	
	public void reducePromisedPEs(int jobNumPE) {
		
	}

	/** Get number of PEs requested by currently present jobs */
	public AtomicInteger getNumOfRequestedPEs() {
		return numOfRequestedPEs;
	}
	
	public AtomicInteger getExpectedNumOfLocalJob() {
		return expectedNumOfLocalJob;
	}
	
	public void setTotalNumOfPEs(AtomicInteger totalNumOfPEs) {
		this.totalNumOfPEs = totalNumOfPEs;
	}

	public void setNumOfRequestedPEs(AtomicInteger numOfRequestedPEs) {
		this.numOfRequestedPEs = numOfRequestedPEs;
	}
	
	public void setTotalActivePEs(AtomicInteger totalActivePEs) {
		this.totalActivePEs = totalActivePEs;
	}

	public void setExpectedNumOfLocalJob(AtomicInteger expectedNumOfLocalJob) {
		this.expectedNumOfLocalJob = expectedNumOfLocalJob;
	}
	
	/// --- --- --- --- --- ---
	
	public synchronized String getMaGateIdentity() {
		return maGateIdentity;
	}

	public synchronized void setMaGateIdentity(String maGateIdentity) {
		this.maGateIdentity = maGateIdentity;
	}
	
	public String getMaGateNickName() {
		return maGateNickName;
	}
	
	public String getRes_osType() {
		return res_osType;
	}
	
	/// --- --- --- --- --- --- 
	
	/**
     * Update the Usage of job usage, resource usage efficiency and active PE rate
     */
	public synchronized void updateUsage(double appendedJobUsage) {
		
		// Overall job usage: jobWeight = job.getNumPE() * job.getActualCPUTime();
		double newJobUsage = MaGateToolkit.convertAtomicLongToDouble(this.jobUsage) + appendedJobUsage;
		this.jobUsage = MaGateToolkit.convertDoubleToAtomicLong(newJobUsage);
		
		// Resource utilization from the community start time till now, NOT real-time node utilization
        double newResUsage = this.totalNumOfPEs.get() * MaGateMediator.getSystemTime();
        double resourceUtilization = newJobUsage / newResUsage;
        
        this.resourceUtilization = MaGateToolkit.convertDoubleToAtomicLong(resourceUtilization);
        
        // Real-time PE usage 
        double currentActivePE = this.totalActivePEs.get();
        double currentTotalPE  = this.totalNumOfPEs.get();
        this.peUsage = currentActivePE / currentTotalPE;
	}
	
	// --- --- networkNeighbors --- ---
    public int size_networkNeighbors() {
    	return this.networkNeighbors.size();
    }
    
    public synchronized void join_networkNeighbors(NeighborItem item) {
    	
    	String id = item.maGateId;
    	if(id != null && (!id.equals(""))) {
    		this.networkNeighbors.put(id, item);
    	}
    }
    
    public synchronized void clear_networkNeighbors() {
    	
    	this.networkNeighbors.clear();
    }
    
    public boolean contain_networkNeighbors(String id) {
    	return this.networkNeighbors.containsKey(id);
    }
    
    public String[] get_networkNeighborIds() {
    	Enumeration<String> ids = this.networkNeighbors.keys();
    	String[] discoveredRemoteMaGateArray = new String[this.networkNeighbors.size()];
    	
    	int i = 0;
    	while(ids.hasMoreElements()) {
    		String nextId = ids.nextElement();
    		if(nextId != null) {
    			discoveredRemoteMaGateArray[i++] = nextId;
    		}
    	}
    	
    	return discoveredRemoteMaGateArray;
//    	
//    	// ----
//    	
//    	Iterator<Entry<String, NeighborItem>> networkNeighborIter = 
//    		this.networkNeighbors.entrySet().iterator();
//    	while(networkNeighborIter.hasNext()) {
//    		Map.Entry<String, NeighborItem> entry = networkNeighborIter.next();
//    		String currentRemoteNode = entry.getKey();
//    		discoveredRemoteMaGateArray[i] = currentRemoteNode;
//    	}
    	
    	
    }
    
//    public Enumeration<NeighborItem> get_networkNeighbors() {
//    	return this.networkNeighbors.elements();
//    }
    
    public ConcurrentHashMap<String, NeighborItem> get_networkNeighbors() {
    	return this.networkNeighbors;
    }
    
    
    
    
    // --- --- cfcNeighbors --- ---
    public int size_cfcNeighbors() {
    	return this.cfcNeighbors.size();
    }
    
    public void join_cfcNeighbors(NeighborItem item) {
    	String id = item.maGateId;
    	if(item.maGateId != null && (!item.maGateId.equals(""))) {
    		this.cfcNeighbors.putIfAbsent(id, item);
    	}
    }
    
    public boolean contain_cfcNeighbors(String remoteNodeid) {
    	return this.cfcNeighbors.containsKey(remoteNodeid);
    }
    
    public Enumeration<String> get_cfcNeighborIds() {
    	return  this.cfcNeighbors.keys();
    }
    
//    public Enumeration<NeighborItem> get_cfcNeighbors() {
//    	return this.cfcNeighbors.elements();
//    }
    
    public ConcurrentHashMap<String, NeighborItem> get_cfcNeighbors() {
    	return this.cfcNeighbors;
    }

    // --- --- freeCFCNeighbors --- ---
    public int size_freeCFCNeighbors() {
    	return this.freeCFCNeighbors.size();
    }
    
    public void join_freeCFCNeighbors(NeighborItem item) {
    	String id = item.maGateId;
    	if(item.maGateId != null && (!item.maGateId.equals(""))) {
    		this.freeCFCNeighbors.putIfAbsent(id, item);
    	}
    }
    
    public boolean contain_freeCFCNeighbors(String remoteNodeId) {
    	return this.freeCFCNeighbors.containsKey(remoteNodeId);
    }
    
    public void remove_freeCFCNeighbors(String remoteNodeId) {
    	this.freeCFCNeighbors.remove(remoteNodeId);
    }
    
    // --- --- nodeConfidenceMap --- ---
    public synchronized int add_nodeInteraction(String remoteNodeId) {
    	
    	if(this.nodeConfidenceCards == null) {
    		log.error("nodeConfidenceCards CANNOT be NULL!");
    		System.exit(0);
    	}
    	
    	if (this.nodeConfidenceCards.containsKey(remoteNodeId.trim())) {
    		
    		NodeConfidenceCard nodeConfidenceCard = this.nodeConfidenceCards.get(remoteNodeId);
    		int lastValue = nodeConfidenceCard.getNumOfInteraction();
    		nodeConfidenceCard.setNumOfInteraction(lastValue + 1);
    		
    		this.nodeConfidenceCards.put(remoteNodeId, nodeConfidenceCard);
    		
    	} else {
    		
    		NodeConfidenceCard nodeConfidenceCard = new NodeConfidenceCard(this.maGateIdentity, this.maGateNickName, remoteNodeId);
    		nodeConfidenceCard.setNumOfInteraction(1);
    		
    		this.nodeConfidenceCards.put(remoteNodeId, nodeConfidenceCard);
    		
    	}
    	
    	return this.nodeConfidenceCards.get(remoteNodeId).getNumOfInteraction();
    }
    
    public ConcurrentHashMap<String, NodeConfidenceCard> getNodeConfidenceCards() {
    	return this.nodeConfidenceCards;
    }
    
    
    // --- --- remoteNodeReputationItem --- ---
    public int size_remoteNodeReputationList() {
    	return this.remoteNodeReputationList.size();
    }
    
    public void join_remoteNodeReputationList(RemoteNodeReputationItem item) {
    	String id = item.remodeNodeId;
    	if(item.remodeNodeId != null && (!item.remodeNodeId.equals(""))) {
    		this.remoteNodeReputationList.putIfAbsent(id, item);
    	}
    }
    
    public boolean contain_remoteNodeReputationList(String remoteNodeid) {
    	return this.remoteNodeReputationList.containsKey(remoteNodeid);
    }
    
    public Enumeration<String> get_remoteNodeReputationListIds() {
    	return  this.remoteNodeReputationList.keys();
    }
    
    public RemoteNodeReputationItem get_remoteNodeReputationListItem(String remodeNodeId) {
    	return this.remoteNodeReputationList.get(remodeNodeId);
    }
    
    public ConcurrentHashMap<String, RemoteNodeReputationItem> get_remoteNodeReputationList() {
    	return this.remoteNodeReputationList;
    }
    
    // --- --- DelegatedJobItem --- ---
    public int size_delegatedjobList() {
    	return this.delegatedjobList.size();
    }
    
    public void join_delegatedjobList(DelegatedJobItem item) {
    	String id = item.jobId;
    	if(item.jobId != null && (!item.jobId.equals(""))) {
    		this.delegatedjobList.putIfAbsent(id, item);
    	}
    }
    
    public DelegatedJobItem get_delegatedJobListItem(String jobId) {
    	return this.delegatedjobList.get(jobId);
    }
    
    public void remove_delegatedJobListItem(String jobId) {
    	this.delegatedjobList.remove(jobId);
    }
    
    public boolean contain_delegatedjobList(String jobId) {
    	return this.delegatedjobList.containsKey(jobId);
    }
    
    public Enumeration<String> get_delegatedjobListIds() {
    	return  this.delegatedjobList.keys();
    }
    
    public ConcurrentHashMap<String, DelegatedJobItem> get_delegatedjobList() {
    	return this.delegatedjobList;
    }
    
	
	/********************  ********************  ********************
	 * variables for data access
	 ********************  ********************  ********************/
	
//    /** List of local Job generated(obtained) by Submitter */
//    private ConcurrentLinkedQueue<JobInfo> localJobQueue;
//    
//    /** List of remote Job accepted(obtained) by ExternalModule */
//    private ConcurrentLinkedQueue<JobInfo> outputJobQueue;
//    
//    /** List of remote input Job trasnferred by ExternalModule */
//    private ConcurrentLinkedQueue<JobInfo> inputJobQueue;
//    
//    /** List of casp Job to be dispersed */
//    private ConcurrentLinkedQueue<JobInfo> outputREQUESTJobQueue;
//    
//    // --- --- ---
//    
//    /** List of casp Jobs waiting for response */
//    private ConcurrentHashMap<String, JobInfo> archivedREQUESTJobMap;
//    
//    /** List of local failed Request-job */
//    private ConcurrentHashMap<String, JobInfo> failedREQUESTJobMap;
//    
//	/** List of local Jobs under processing 
//	 * A job is put into localProcessingJobMap when submitted by localSubmitter.
//	 * 
//	 * It will be removed from this map/list when:
//	 * (1) the job is scheduled FAILED and been appended to ArchivedFailedJob
//	 * (2) the job is returned from local resource
//	 * */
//    private ConcurrentHashMap<String, JobInfo> localProcessingJobMap;
//    
//	/** List of already received local job */
//    private ConcurrentHashMap<String, JobInfo> localExedJobMap;
//    
//    /** List of local unsuited job */
//    private ConcurrentHashMap<String, JobInfo> localUnsuitedJobMap;
//    
//    /** List of local failed job */
//    private ConcurrentHashMap<String, JobInfo> localFailedJobMap;
//    
//    /** List of local archived job */
//    private ConcurrentHashMap<String, JobInfo> localArchivedJobMap;
//    
//    /** List of remote jobs waiting for processing */
//    private ConcurrentHashMap<String, JobInfo> outputArchivedJobMap;
//    
//	/** List of already received remote job */
//    private ConcurrentHashMap<String, JobInfo> outputSentJobMap;
//    
//	/** List of remote failed job */
//    private ConcurrentHashMap<String, JobInfo> outputSentFailedJobMap;
//    
//	/** List of returned job from community */
//    private ConcurrentHashMap<String, JobInfo> outputReturnedExedJobMap;
//    
//    /** List of returned failed job from community */
//    private ConcurrentHashMap<String, JobInfo> outputReturnedFailedJobMap;
//    
//	/** List of remote input jobs waiting for processing */
//    private ConcurrentHashMap<String, JobInfo> inputProcessingJobMap;
//    
//	/** List of remote input failed job */
//    private ConcurrentHashMap<String, JobInfo> inputExedFailedJobMap;
//    
//    /** List of already received remote input job */
//    private ConcurrentHashMap<String, JobInfo> inputExedJobMap;
//    
//    /** List of remote input archived job */
//    private ConcurrentHashMap<String, JobInfo> inputArchivedJobMap;
//    
//    /** List of archive of successfully executed remote input job */
//    private ConcurrentHashMap<String, JobInfo> inputDeliveredExedJobMap;
//
//    /** List of archive of failed executed remote input job */
//    private ConcurrentHashMap<String, JobInfo> inputDeliveredExedFailedJobMap;
	
    
    
    /********************  ********************  ********************
	 * Methods for data access
	 ********************  ********************  ********************/
	
    // --- --- localJobQueue --- --- 
    
//    public void join_localJob(JobInfo jobInfo) {
//    	this.localJobQueue.offer(jobInfo);
//    }
//    
//    public ConcurrentLinkedQueue<JobInfo> find_localJobQueue() {
//    	return this.localJobQueue;
//    }
//    
//    public int size_localJob() {
//    	return this.localJobQueue.size();
//    }
//    
//    public void clear_localJob() {
//    	this.localJobQueue.clear();
//    }
//    
//    // --- --- outputJobQueue --- --- 
//    
//    public void join_outputJob(JobInfo jobInfo) {
//    	this.outputJobQueue.offer(jobInfo);
//    }
//    
//    public int size_outputJob() {
//    	return this.outputJobQueue.size();
//    }
//    
//    public boolean isEmpty_outputJob() {
//    	return this.outputJobQueue.isEmpty();
//    }
//    
//    public JobInfo poll_outputJob() {
//    	return this.outputJobQueue.poll();
//    }
//    
//    public JobInfo peek_outputJob() {
//    	return this.outputJobQueue.peek();
//    }
//    
//    public void remove_outputJob(JobInfo jobInfo) {
//    	this.outputJobQueue.remove(jobInfo);
//    }
//    
//    // --- --- inputJobQueue --- --- 
//    
//    public int size_inputJob() {
//    	return this.inputJobQueue.size();
//    }
//    
//    public void join_inputJob(JobInfo jobInfo) {
//    	this.inputJobQueue.offer(jobInfo);
//    }
//    
//    public boolean isEmpty_inputJob() {
//    	return this.inputJobQueue.isEmpty();
//    }
//    
//    public JobInfo poll_inputJob() {
//    	return this.inputJobQueue.poll();
//    }
//    
//    // --- --- outputREQUESTJob_Queue --- --- 
//    
//    public void join_outputREQUESTJob(JobInfo jobInfo) {
//    	this.outputREQUESTJobQueue.offer(jobInfo);
//    }
//    
//    public int size_outputREQUESTJob() {
//    	return this.outputREQUESTJobQueue.size();
//    }
//    
//    public boolean isEmpty_outputREQUESTJob() {
//    	return this.outputREQUESTJobQueue.isEmpty();
//    }
//    
//    public JobInfo poll_outputREQUESTJob() {
//    	return this.outputREQUESTJobQueue.poll();
//    }
//    
//    public JobInfo peek_outputREQUESTJob() {
//    	return this.outputREQUESTJobQueue.peek();
//    }
//    
//    public void remove_outputREQUESTJob(JobInfo jobInfo) {
//    	this.outputREQUESTJobQueue.remove(jobInfo);
//    }
//    
//    // --- --- archivedREQUESTJob_Map --- ---
//    
//    public int size_archivedREQUESTJob() {
//    	return this.archivedREQUESTJobMap.size();
//    }
//    
//    public void remove_archivedREQUESTJob(String gid) {
//    	this.archivedREQUESTJobMap.remove(gid);
//    }
//    
//	public void join_archivedREQUESTJob(JobInfo jobInfo) {
//		
//		String gid = jobInfo.getGlobalJobID();
//		archivedREQUESTJobMap.put(gid, jobInfo);
//	}
//	
//	/** 
//     * Get JobInfo from casp waiting job list
//     * @param MaGateJob
//     */
//    public JobInfo find_archivedREQUESTJob(String gid){
//    	return this.archivedREQUESTJobMap.get(gid);
//    }
//    
//    /** 
//     * Get MaGateJob from casp waiting job list
//     * @param JobInfo
//     */
//    public MaGateJob findJob_archivedREQUEST(String gid){
//    	
//    	JobInfo jobInfo = this.archivedREQUESTJobMap.get(gid);
//    	if(jobInfo == null) {
//    		return null;
//    	} else {
//    		return jobInfo.getJob();
//    	}
//    	
//    }
//    
//    // --- --- failedREQUESTJob_Map --- ---
//    
//    public int size_failedREQUESTJob() {
//    	return this.failedREQUESTJobMap.size();
//    }
//    
//    public void join_failedREQUESTJob(JobInfo jobInfo) {
//    	String gid = jobInfo.getGlobalJobID();
//    	this.failedREQUESTJobMap.put(gid, jobInfo);
//    	
//    	// Counter
//    	GlobalStorage.count_unmatchedREQUEST.incrementAndGet();
//    }
//    
//    // --- --- localProcessingJobMap --- ---
//    
//    public int size_localProcessingJob() {
//    	return this.localProcessingJobMap.size();
//    }
//    
//    public void remove_localProcessingJob(String gid) {
//    	this.localProcessingJobMap.remove(gid);
//    	
//    	GlobalStorage.count_localProcessingJob.decrementAndGet();
//    }
//    
//	public void join_localProcessingJob(JobInfo jobInfo) {
//		
//		String gid = jobInfo.getGlobalJobID();
//		localProcessingJobMap.put(gid, jobInfo);
//		
//		GlobalStorage.count_localProcessingJob.incrementAndGet();
//	}
//	
//	/** 
//     * Get JobInfo from local waiting job list
//     * @param MaGateJob
//     */
//    public JobInfo find_localProcessingJob(String gid){
//    	return this.localProcessingJobMap.get(gid);
//    }
//    
//    /** 
//     * Get MaGateJob from local waiting job list
//     * @param JobInfo
//     */
//    public MaGateJob findJob_localWaitingJob(String gid){
//    	
//    	JobInfo jobInfo = this.localProcessingJobMap.get(gid);
//    	if(jobInfo == null) {
//    		return null;
//    	} else {
//    		return jobInfo.getJob();
//    	}
//    	
//    }
//    
//    // --- --- localArchivedJobMap --- ---
//    
//    public int size_localArchivedJob() {
//    	return this.localArchivedJobMap.size();
//    }
//    
//    public void join_localArchivedJob(JobInfo jobInfo) {
//    	String gid = jobInfo.getGlobalJobID();
//    	this.localArchivedJobMap.put(gid, jobInfo);
//    	
//    	// Counter
//    	GlobalStorage.count_localArchivedJob.incrementAndGet();
//    	
//    }
//    
//    // --- --- localExedJobMap --- ---
//    
//    /** 
//     * Get JobInfo from local received job list
//     * @param MaGateJob
//     */
//    public JobInfo fetchJobInfo_from_localExedJob(MaGateJob job){
//    	
//    	String gid = job.getGlobalJobID();
//    	return this.localExedJobMap.get(gid);
//    }
//    
//    /** 
//     * Get MaGateJob from local received job list
//     * @param JobInfo
//     */
//    public MaGateJob fetchJob_from_localExedJobInfo(JobInfo ji){
//    	
//    	String gid = ji.getGlobalJobID();
//    	JobInfo jobInfo = this.localExedJobMap.get(gid);
//    	if(jobInfo == null) {
//    		return null;
//    	} else {
//    		return jobInfo.getJob();
//    	}
//    }
//    
//    public int size_localExedJob() {
//    	return this.localExedJobMap.size();
//    }
//    
//    public Collection<JobInfo> find_localExedJob() {
//    	return this.localExedJobMap.values();
//    }
//    
//    public void join_localExedJob(JobInfo jobInfo) {
//    	String gid = jobInfo.getGlobalJobID();
//    	this.localExedJobMap.put(gid, jobInfo);
//    	
//    	// Counter
//    	GlobalStorage.count_localExedJob.incrementAndGet();
//    }
//    
//    // --- --- localUnsuitedJobMap --- ---
//    
//    public int size_localUnsuitedJob() {
//    	return this.localUnsuitedJobMap.size();
//    }
//    
//    public void join_localUnsuitedJob(JobInfo jobInfo) {
//    	String gid = jobInfo.getGlobalJobID();
//    	this.localUnsuitedJobMap.put(gid, jobInfo);
//    	
//    	// Counter
//    	GlobalStorage.count_localUnsuitedJob.incrementAndGet();
//    }
//    
//    // --- --- localFailedJobMap --- ---
//    
//    public int size_localFailedJob() {
//    	return this.localFailedJobMap.size();
//    }
//    
//    public void join_localFailedJob(JobInfo jobInfo) {
//    	String gid = jobInfo.getGlobalJobID();
//    	this.localFailedJobMap.put(gid, jobInfo);
//    	
//    	// Counter
//    	GlobalStorage.count_localFailedJob.incrementAndGet();
//    }
//    
//    // --- --- outputArchivedJobMap --- --- 
//    
//    public int size_outputArchivedJob() {
//    	return this.outputArchivedJobMap.size();
//    }
//    
//    public void join_outputArchivedJob(JobInfo jobInfo) {
//    	String gid = jobInfo.getGlobalJobID();
//    	this.outputArchivedJobMap.put(gid, jobInfo);
//    	
//    	// Counter
//    	GlobalStorage.count_outputArchivedJob.incrementAndGet();
//    }
//    
//    // --- --- outputSentJobMap --- --- 
//    
//    public int size_outputSentJob() {
//    	return this.outputSentJobMap.size();
//    }
//    
//    public void join_outputSentJob(JobInfo jobInfo) {
//    	String gid = jobInfo.getGlobalJobID();
//    	this.outputSentJobMap.put(gid, jobInfo);
//    	
//    	// Counter
//    	GlobalStorage.count_outputSentJob.incrementAndGet();
//    }
//    
//    public JobInfo get_outputSentJob(String jobId) {
//    	return this.outputSentJobMap.get(jobId);
//    }
//    
//    public boolean isExist_outputSentJob(String jobId) {
//    	return this.outputSentJobMap.containsKey(jobId);
//    }
//    
//    // --- --- outputSentFailedJobMap --- --- 
//    
//    public int size_outputSentFailedJob() {
//    	return this.outputSentFailedJobMap.size();
//    }
//    
//    public void join_outputSentFailedJob(JobInfo jobInfo) {
//    	String gid = jobInfo.getGlobalJobID();
//    	this.outputSentFailedJobMap.put(gid, jobInfo);
//    	
//    	// Counter
//    	GlobalStorage.count_outputSentFailedJob.incrementAndGet();
//    }
//    
//    // --- --- outputReturnedExedJobMap --- --- 
//    
//    public int size_outputReturnedExedJob() {
//    	return this.outputReturnedExedJobMap.size();
//    }
//    
//    public void join_outputReturnedExedJob(JobInfo jobInfo) {
//    	String gid = jobInfo.getGlobalJobID();
//    	this.outputReturnedExedJobMap.put(gid, jobInfo);
//    	
//    	// Counter
//    	GlobalStorage.count_outputReturnedExedJob.incrementAndGet();
//    }
//    
//    // --- --- outputReturnedFailedJobMap --- --- 
//
//    public int size_outputReturnedFailedJob() {
//    	return this.outputReturnedFailedJobMap.size();
//    }
//    
//    public void join_outputReturnedFailedJob(JobInfo jobInfo) {
//    	String gid = jobInfo.getGlobalJobID();
//    	this.outputReturnedFailedJobMap.put(gid, jobInfo);
//    	
//    	// Counter
//    	GlobalStorage.count_outputReturnedFailedJob.incrementAndGet();
//    }
//    
//    // --- --- inputArchivedJobMap --- --- 
//    
//    public int size_inputArchivedJob() {
//    	return this.inputArchivedJobMap.size();
//    }
//    
//    public void join_inputArchivedJob(JobInfo jobInfo) {
//    	String gid = jobInfo.getGlobalJobID();
//    	this.inputArchivedJobMap.put(gid, jobInfo);
//    	
//    	// Counter
//    	GlobalStorage.count_inputArchivedJob.incrementAndGet();
//    }
//    
//    public boolean isEmpty_inputArchivedJob() {
//    	return this.inputArchivedJobMap.isEmpty();
//    }
//    
//    // --- --- inputProcessingJobMap --- --- 
//    
//    public int size_inputProcessingJob() {
//    	return this.inputProcessingJobMap.size();
//    }
//    
//    public void join_inputProcessingJob(JobInfo jobInfo) {
//    	String gid = jobInfo.getGlobalJobID();
//    	this.inputProcessingJobMap.put(gid, jobInfo);
//    	
//    	// Counter
//    	GlobalStorage.count_inputProcessingJob.incrementAndGet();
//    }
//    
//    public boolean isEmpty_inputProcessingJob() {
//    	return this.inputProcessingJobMap.isEmpty();
//    }
//    
//    public void remove_inputProcessingJob(String gid) {
//    	this.inputProcessingJobMap.remove(gid);
//    	
//    	// Counter
//    	GlobalStorage.count_inputProcessingJob.decrementAndGet();
//    }
//    
//    /** 
//     * Get JobInfo from remote input waiting job list
//     * @param MaGateJob
//     */
//    public JobInfo find_inputProcessingJob(String gid){
//    	return this.inputProcessingJobMap.get(gid);
//    }
//    
//    /** 
//     * Get MaGateJob from remote input waiting job list
//     * @param JobInfo
//     */
//    public MaGateJob findJob_inputProcessingJob(String gid){
//    	
//    	JobInfo jobInfo = this.inputProcessingJobMap.get(gid);
//    	if(jobInfo == null) {
//    		return null;
//    	} else {
//    		return jobInfo.getJob();
//    	}
//    }
//     
//    // --- --- inputExedJobMap --- --- 
//    
//    public boolean isEmpty_inputExedJob() {
//    	return this.inputExedJobMap.isEmpty();
//    }
//    
//    public Collection<JobInfo> fetch_inputExedJob() {
//    	
//    	Collection<JobInfo> collection = this.inputExedJobMap.values();
//    	Collection<JobInfo> result = new Vector<JobInfo>();
//    	
//    	for(JobInfo jobInfo : collection) {
//    		result.add(jobInfo);
//    	}
//    	
//    	collection = null;
//    	this.inputExedJobMap.clear();
//    	return result;
//    }
//    
//    public int size_inputExedJob() {
//    	return this.inputExedJobMap.size();
//    }
//    
//    public void join_inputExedJob(JobInfo jobInfo) {
//    	String gid = jobInfo.getGlobalJobID();
//    	this.inputExedJobMap.put(gid, jobInfo);
//    	
//    	// Counter
//    	GlobalStorage.count_inputExedJob.incrementAndGet();
//    }
//    
//    // --- --- inputExedFailedJobMap --- --- 
//    
//    public int size_inputExedFailedJob() {
//    	return this.inputExedFailedJobMap.size();
//    }
//    
//    public void join_inputExedFailedJob(JobInfo jobInfo) {
//    	String gid = jobInfo.getGlobalJobID();
//    	this.inputExedFailedJobMap.put(gid, jobInfo);
//    	
//    	// Counter 
//    	GlobalStorage.count_inputExedFailedJob.incrementAndGet();
//    	
//    }
//    
//    public boolean isEmpty_inputExedFailedJob() {
//    	return this.inputExedFailedJobMap.isEmpty();
//    }
//    
//    public Collection<JobInfo> fetch_inputExedFailedJob() {
//    	
//    	Collection<JobInfo> collection = this.inputExedFailedJobMap.values();
//    	Collection<JobInfo> result = new Vector<JobInfo>();
//    	
//    	for(JobInfo jobInfo : collection) {
//    		result.add(jobInfo);
//    	}
//    	collection = null;
//    	this.inputExedFailedJobMap.clear();
//    	return result;
//    }
//    
//    // --- --- inputDeliveredExedJobMap --- --- 
//
//    public int size_inputDeliveredExedJob() {
//    	return this.inputDeliveredExedJobMap.size();
//    }
//    
//    public void join_inputDeliveredExedJob(JobInfo jobInfo) {
//    	String gid = jobInfo.getGlobalJobID();
//    	this.inputDeliveredExedJobMap.put(gid, jobInfo);
//    	
//    	// Counter
//    	GlobalStorage.count_inputDeliveredExedJob.incrementAndGet();
//    }
//    
//    // --- --- inputDeliveredExedFailedJobMap --- --- 
//    
//    public int size_inputDeliveredExedFailedJob() {
//    	return this.inputDeliveredExedFailedJobMap.size();
//    }
//    
//    public void join_inputDeliveredExedFailedJob(JobInfo jobInfo) {
//    	String gid = jobInfo.getGlobalJobID();
//    	this.inputDeliveredExedFailedJobMap.put(gid, jobInfo);
//    	
//    	// Counter
//    	GlobalStorage.count_inputDeliveredExedFailedJob.incrementAndGet();
//    }
    
    
}


