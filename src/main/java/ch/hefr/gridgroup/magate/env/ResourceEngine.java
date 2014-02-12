package ch.hefr.gridgroup.magate.env;

import eduni.simjava.Sim_event;
import eduni.simjava.Sim_system;
import gridsim.GridSim;
import gridsim.GridSimTags;
import gridsim.ResourceCharacteristics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.MaGateEntity;
import ch.hefr.gridgroup.magate.km.MatchMaker;
import ch.hefr.gridgroup.magate.lm.ILRM;
import ch.hefr.gridgroup.magate.model.JobInfo;
import ch.hefr.gridgroup.magate.storage.MaGateStorage;

public class ResourceEngine extends GridSim {
	
	private static Log log = LogFactory.getLog(ResourceEngine.class);
    
    private String resEngineName;
    
    /** List of known nodes */
	private Vector<ILRM> totalLrmList;
	
	/** Number of resources for each node */
    private ArrayList<Integer> totalResourceList;
    
    private boolean ResourceEngineReady   = false;
	private boolean ResourceEngineWorking = false;
	
	public AtomicInteger counter = new AtomicInteger(0);

    public ResourceEngine(String name, double baudRate) throws Exception {
    	super(name, baudRate);
    	
    	this.resEngineName = name;
    	this.totalLrmList = new Vector<ILRM>();
    	this.totalResourceList = new ArrayList<Integer>();
    	
    }

	public void body() {
    	
    	while(this.isResourcePreparing()) {
    		super.gridSimHold(50);
    	}
    	
		LinkedList gridResourceList = super.getGridResourceList();
		
		System.out.println("ResourceEngine: Preparing grid resource");
		
		// preparing resource characteristics LIST for each MaGate node 
		synchronized (gridResourceList) {
			
			// start MaGate node loop 
			for (int index = 0; index < this.totalLrmList.size(); index++) {
				
				// current node
				ILRM currentLRM = (ILRM) this.totalLrmList.get(index);
				
				// number of resources on current node
				int currentNumOfResource = ((Integer) this.totalResourceList.get(index)).intValue();
				
				LinkedList<ResourceCharacteristics> resourceForLRM = new LinkedList<ResourceCharacteristics>();
				ResourceCharacteristics tempRes = null;
				
				// check all resources from underlying simulator, find resources affiliated to current node
				for (int rIndex = 0; rIndex < gridResourceList.size(); rIndex++) {
					
					int tempResId = ((Integer) gridResourceList.get(rIndex)).intValue();
					
					tempRes = super.getResourceCharacteristics(tempResId);
					
					if (super.getResourceCharacteristics(tempResId) == null) {
						log.warn("... ... ... resourceId: " + tempResId);
					}
					
					String tempResName = GridSim.getEntityName(tempResId);
					String[] tempArr = tempResName.split("_");
					String tempMaGateId = tempArr[0];

					if ((tempMaGateId.trim()).equals(currentLRM.getMaGateIdentity().trim())) {
						resourceForLRM.add(tempRes);
					}
				}
				
				tempRes = null;

				if (currentNumOfResource != resourceForLRM.size()) {
					log.error(" Warning:... requested number of resource doesn't match the created for MaGate: "
									+ currentLRM.getMaGateIdentity()
									+ "(request: "
									+ currentNumOfResource
									+ "/created: " + resourceForLRM.size() + ")");
					System.exit(0);
				} 

				// saving prepared resource characteristics list to the corresponding LRM of current node
				currentLRM.updateResCharacteristicsList(resourceForLRM);
				
			} // end MaGate node loop 
			
			this.setResourceEngineReady(true);
			this.setResourceEngineWorking(true);
			
			System.out.println("ResourceEngine ready: Totally " + gridResourceList.size() + " resources are ready for " + this.totalLrmList.size() + " MaGate LRMs!");
			
		}
		
		Sim_event ev = new Sim_event();
        
        while ( Sim_system.running() ) {
            super.sim_get_next(ev);
            
            // ye: job submitted from JobSubmitter, MatchMaker will response
            if (ev.get_tag() == GridSimTags.END_OF_SIMULATION) {
            	
            	log.info(" >>> >>> >>> Entity ResourceEngine: " + this.resEngineName + " is shuting down");
            	
            	shutdownUserEntity();
            	terminateIOEntities();
            }
            
        }
    }
    
	/**
     * Initial the resource from grid infrastructure
     */
	public void initResource() {
		
		for (int i = 0; i < this.totalResourceList.size(); i++) {
			
			ILRM targetLRM = (ILRM) this.totalLrmList.get(i);
			
			// Creating resource "magate by magate (one by one)"
			this.createSimResources(targetLRM.getMaGateIdentity(), targetLRM.getStorage(), 
					targetLRM.getArchType(), targetLRM.getOsType(), 
					targetLRM.getNumOfResource(), targetLRM.getNumOfPEPerResource(), 
					targetLRM.getPeMIPS());
		}

	}

    /**
     * Used to verify all simulated resource recognized by simuluation environment
     * @return false means ready, system continues
     * @return true means unprepared, system holds
     */
	private boolean isResourcePreparing() {
		
		LinkedList resList = null;
		int previousNum    = 0;
		int refreshedNum   = 0;
		
		super.gridSimHold(2);
		resList = super.getGridResourceList();
		refreshedNum = resList.size();
		
		while (previousNum < refreshedNum) {
			previousNum = refreshedNum;
			super.gridSimHold(10);
			resList = super.getGridResourceList();
			refreshedNum = resList.size();
		}
		
		resList = null;
		return false;
	}
	
	/**
     * Creates all Grid resources. A Grid resource contains one or more
     * Machines (now we suppose 1 GridResource = 1 Machine). Similarly, a Machine contains one or more PEs (Processing
     * Elements or CPUs). We suppose that PEs within the machine has the same MIPS rating.
	 * @param targetMaGateId 
     */
    private void createSimResources(String targetMaGateId, MaGateStorage storage, String archType, String osType, int numOfResource, 
    		int numOfPEPerMachine, int peMIPS) {
        
    	SimResourceFactory.createSimResourceList(targetMaGateId, storage, archType, osType, numOfResource, numOfPEPerMachine, peMIPS);
        
    }
    
    public synchronized void addObserver(ILRM lrm) {
        if (lrm == null)
            throw new NullPointerException();
        
        if (!totalLrmList.contains(lrm)) {
        	totalLrmList.add(lrm);
        	totalResourceList.add(new Integer(lrm.getNumOfResource()));
        }
    }
    
    public synchronized boolean isResourceEngineReady() {
		return ResourceEngineReady;
	}

	public synchronized void setResourceEngineReady(boolean resourceEngineReady) {
		ResourceEngineReady = resourceEngineReady;
	}

	public synchronized boolean isResourceEngineWorking() {
		return ResourceEngineWorking;
	}

	public synchronized void setResourceEngineWorking(boolean resourceEngineWorking) {
		ResourceEngineWorking = resourceEngineWorking;
	}
    
    public String getResEngineName() {
		return resEngineName;
	}
    
}


