package ch.hefr.gridgroup.magate.lm;

import gridsim.ResourceCharacteristics;

import java.util.LinkedList;

import ch.hefr.gridgroup.magate.model.ResourceInfo;
import ch.hefr.gridgroup.magate.storage.MaGateStorage;

public interface ILRM {

	/** 
	 * Queue LRMStorage and get the ResourceInfo with a internal resource Id
	 */
//	public abstract ResourceInfo getResourceInfo(int resourceId);

	/** 
	 * Initialize LRMStorage from "ResourceCharacteristics List", which is a resource-format used within grid Infrastructure 
	 * This operation only update the corresponding imported LRMStorage parameter, instead of re-generating a new LRMStorage
	 * 
	 * Including parameters: resourceIdArray, resourceNameArray, resourceInfoList, bestMachineMIPS, totalAvailPEs
	 */
	public abstract void registerStorage();

	/**
	 * Get reference/access of LRMStorage
	 */
	public abstract MaGateStorage getStorage();

	/**
	 * Get resource passed from grid infrastructure
	 */
	public abstract void updateResCharacteristicsList(LinkedList<ResourceCharacteristics> argResList);

	/**
	 * Get identity of MaGate
	 */
	public abstract String getMaGateIdentity();

	/**
	 * Get number of available resources
	 */
	public abstract int getNumOfResource();
	
	public abstract int getNumOfPEPerResource();
	
	public abstract int getPeMIPS();

	/** receive jobs returned from resources */
	public abstract void onJobReturnFromResource(Object getData);
	
	/** receive jobs from MatchMaker */
	public abstract void onScheduleMadeByMatchMaker(Object getData);
	
	public abstract String getArchType();

	public abstract String getOsType();
	
	public abstract String getVO();
}

