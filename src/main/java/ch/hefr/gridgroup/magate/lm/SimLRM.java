package ch.hefr.gridgroup.magate.lm;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import gridsim.GridResource;
import gridsim.GridSim;
import gridsim.GridSimTags;
import gridsim.Machine;
import gridsim.MachineList;
import gridsim.PE;
import gridsim.PEList;
import gridsim.ResourceCalendar;
import gridsim.ResourceCharacteristics;
import ch.hefr.gridgroup.magate.MaGateEntity;
import ch.hefr.gridgroup.magate.env.MaGateMessage;
import ch.hefr.gridgroup.magate.env.MaGateProfile;
import ch.hefr.gridgroup.magate.env.ResourceEngine;
import ch.hefr.gridgroup.magate.env.SimResourceFactory;
import ch.hefr.gridgroup.magate.ext.AdvancedPolicy;
import ch.hefr.gridgroup.magate.km.*;
import ch.hefr.gridgroup.magate.lm.*;
import ch.hefr.gridgroup.magate.model.ResourceInfo;
import ch.hefr.gridgroup.magate.model.SimResourceInfo;
import ch.hefr.gridgroup.magate.storage.MaGateStorage;
import eduni.simjava.Sim_event;
import eduni.simjava.Sim_system;

/**
 * Class SimLRM works as a Local Resource Manager to local (simulated) resources.
 * 
 * Once SimLRM is created, the (simulated)resources are created also, 
 * and waiting to be discovered by simulation infrastructure mechanism
 * 
 * @author Ye HUANG
 */
public class SimLRM implements ILRM {

	private MaGateEntity maGate;
    
    private String maGateIdentity;

	private int numOfResource = 0;
	
	private int numOfPEPerResource = MaGateProfile.numOfPE_eachResource;
	
	private int peMIPS = MaGateProfile.peMIPS;
	
	private String archType = MaGateProfile.res_archType;

	private String osType   = MaGateProfile.osType;
	
	private String vo = "";
	
	/** Receiving resource passed from resource engine */
	private LinkedList<ResourceCharacteristics> lrmResCharacteristicsList;
    
    private static Log log = LogFactory.getLog(SimLRM.class);
    
    /**
     * Generating workload trace based simulation resource
     */
	public SimLRM(MaGateEntity maGate, String archType, String osType, 
			int numOfResource, int numOfPEPerResource, int peMIPS, String vo) throws Exception {
		
		this.maGate         = maGate;
		this.maGateIdentity = maGate.getMaGateIdentity();
		
		this.peMIPS 		    = peMIPS;
		this.archType           = archType;
		this.osType             = osType;
		this.numOfResource      = numOfResource;
		this.numOfPEPerResource = numOfPEPerResource;
		this.peMIPS             = peMIPS;
    	this.vo                 = vo;
    	
//    	int largestPE = numOfResource * numOfPEPerResource;
//    	this.maGate.getStorage().setMaxResourcePE(new AtomicInteger(largestPE));
    	
	}
	
	/* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.lm.impl.LRM#registerLRMStorage()
	 */ 
	public void registerStorage() {
		
		int resSize = 0;
		
		if(this.lrmResCharacteristicsList == null || this.lrmResCharacteristicsList.size() == 0) {
			log.error("Warning:... ... no resource prepared for LRM: " + this.maGateIdentity);
			System.exit(0);
			
		} else {
			resSize = this.lrmResCharacteristicsList.size();
			this.maGate.getStorage().setNumOfLocalResource(new AtomicInteger(resSize));
		}
		
		// retrieve lrmStorage data, which will be sorted by CPU performance later
		// NOTICE: the resourceInfoList contains all resource characteristic info of current node
		LinkedList<ResourceInfo> localResourceInfoList = this.maGate.getStorage().getResourceInfoList();
		if (localResourceInfoList == null) {
			localResourceInfoList = new LinkedList<ResourceInfo>();
		}
		
		// generate up-to-date status data
		// two [] won't be updated unless size > 0
		if (resSize > 0) {
			
			int [] resourceIdArray = new int[resSize];
			String [] resourceNameArray = new String[resSize];
			
			this.maGate.getStorage().setTotalNumOfPEs(new AtomicInteger(0));
			int maxResourcePE = 0;
			
			// loop, to check all the available resources of current node (current MaGate-LRM-Node)
			for(int i = 0; i < resSize; i++) {
				
				// Get current GridSim based "Resource Characteristic"
				ResourceCharacteristics currentResourceCharacteristics = (ResourceCharacteristics) this.lrmResCharacteristicsList.get(i);
				int currentSimResourceId = currentResourceCharacteristics.getResourceID();
				
				// GridSim based approach to find resource key-name pair
				resourceIdArray[i]   = currentSimResourceId;
				resourceNameArray[i] = GridSim.getEntityName(currentSimResourceId);
				
				// Generate novel "Resource Info", original from "Alea Simulator"
				ResourceInfo currentSimResourceInfo = new SimResourceInfo(currentResourceCharacteristics);
				
				// add-up all PEs of resources of each node, in order to get the total PEs of this node
				this.maGate.getStorage().setTotalNumOfPEs(
						new AtomicInteger(this.maGate.getStorage().getTotalNumOfPEs().get() + currentResourceCharacteristics.getNumPE()));
				
				if(maxResourcePE < currentResourceCharacteristics.getNumPE()) {
					maxResourcePE = currentResourceCharacteristics.getNumPE();
				}
				
				// add-up the sim-based ResourceInfo into the resourceInfoList of this MaGate node
				// then sort the list by CPU performance
				// NOTICE: For the first resource of current node, since the localResourceInfoList.size() == 0, this the loop won't be entered
				if (localResourceInfoList.size() > 0) {
					
					for (int j = 0; j < localResourceInfoList.size(); j++){
						ResourceInfo tempSimResourceInfo = (ResourceInfo) localResourceInfoList.get(j);
						
						if(currentSimResourceInfo.getResource().getNumPE() >= tempSimResourceInfo.getResource().getNumPE()){
                            if(currentSimResourceInfo.getResource().getNumPE() == tempSimResourceInfo.getResource().getNumPE() && currentSimResourceInfo.getResource().getMIPSRatingOfOnePE() > tempSimResourceInfo.getResource().getMIPSRatingOfOnePE()){
                            	localResourceInfoList.add(j,currentSimResourceInfo);
                                break;
                            }
                            if(currentSimResourceInfo.getResource().getNumPE() > tempSimResourceInfo.getResource().getNumPE()){
                            	localResourceInfoList.add(j,currentSimResourceInfo);
                                break;
                            }
                        }
                        if(j == localResourceInfoList.size() - 1){
                        	localResourceInfoList.addLast(currentSimResourceInfo);
                            break;
                        }
                    }
					
				} else {
					
					// the first resourceInfo of this node is being handled 
					localResourceInfoList.add(currentSimResourceInfo);
					
				}
				
			} // end the loop of "all-resource-check"
			
			ResourceInfo bestMIPSResource  = (ResourceInfo) localResourceInfoList.getFirst();
			ResourceInfo worstMIPSResource = (ResourceInfo) localResourceInfoList.getLast();
			
			// save the up-to-date data to LRMStorage
//			this.maGate.getStorage().setMaxResourcePE(new AtomicInteger(maxResourcePE));
			this.maGate.getStorage().setResourceIdArray(resourceIdArray);
			this.maGate.getStorage().setResourceNameArray(resourceNameArray);
			this.maGate.getStorage().setBestMachineMIPS(new AtomicInteger(bestMIPSResource.getResource().getMIPSRatingOfOnePE()));
		}
		
		// save the up-to-date data to LRMStorage
		this.maGate.getStorage().setResourceInfoList(localResourceInfoList);
		
		// mark the LRMStorage been initialized
		this.maGate.getStorage().setStorageInitiaized(new AtomicBoolean(true));
		
	}
	
    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.lm.impl.LRM#resourceFromGridInfrastructure(java.util.LinkedList)
	 */
	public void updateResCharacteristicsList(LinkedList<ResourceCharacteristics> argResList) {
		this.lrmResCharacteristicsList = argResList;
	}
	

	public void onJobReturnFromResource(Object getData) {
		// In simulation, the work is done directly in km.ModuleController
		
	}

	public void onScheduleMadeByMatchMaker(Object getData) {
		// In simulation, the work is done directly in km.ModuleController
		
	}
	
	/* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.lm.impl.LRM#getLrmStorage()
	 */
    public synchronized MaGateStorage getStorage() {
		return maGate.getStorage();
	}
    
	/* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.lm.impl.LRM#getMaGateIdentity()
	 */
	public String getMaGateIdentity() {
		return maGateIdentity;
	}

    /* (non-Javadoc)
	 * @see ch.hefr.gridgroup.magate.lm.impl.LRM#getNumOfResource()
	 */
	public int getNumOfResource() {
		return numOfResource;
	}
	

	public int getNumOfPEPerResource() {
		return numOfPEPerResource;
	}

	public int getPeMIPS() {
		return peMIPS;
	}
	
	public String getArchType() {
		return archType;
	}

	public String getOsType() {
		return osType;
	}
	
	public String getVO() {
		return this.vo;
	}

}
