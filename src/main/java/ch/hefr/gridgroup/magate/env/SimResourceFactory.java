package ch.hefr.gridgroup.magate.env;

import gridsim.*;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.ext.AdvancedPolicy;
import ch.hefr.gridgroup.magate.lm.SimLRM;
import ch.hefr.gridgroup.magate.storage.MaGateStorage;

/**
 * Creating a SimResource comprised of set of machines, each machine has the same number of processors (PEs)
 * 
 * @author Ye HUANG
 * @param numOfMachine the number of machines owned by this resource
 * @param numbOfPE the number of PEs owned by each machine
 * @param peMIPS the MIPS of each PE
 */
@SuppressWarnings({"unchecked", "unused"})
public class SimResourceFactory {
	
	private static Log log = LogFactory.getLog(SimResourceFactory.class);
	
	/**
	 * Create Simulated Resource list
	 *   (Recommended)
	 *   
	 * @param targetMaGateId
	 * @param storage
	 * @param archType
	 * @param osType
	 * @param numOfResource
	 * @param numOfPEPerMachine
	 * @param peMIPS
	 * @param minRange
	 * @param maxRange
	 */
	public static void createSimResourceList(String targetMaGateId, MaGateStorage storage, String archType, String osType, 
			int numOfResource, int numOfPEPerMachine, int peMIPS) {
		
		ArrayList resList = new ArrayList();
		AdvancedPolicy advancedPolicy = null;
		
		for (int i = 0; i < numOfResource; i++) {
			String resourceName = targetMaGateId + "_r_" + i;
			
			try {
	            // this is useful because we can define resources internal scheduling system (FCFS/RR/BackFilling,FairQueuing...)
	        	advancedPolicy = new AdvancedPolicy(resourceName, "AdvancedPolicy", storage);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	        
			GridResource gridResource = createSimResource(resourceName, advancedPolicy, 
					archType, osType, numOfPEPerMachine, peMIPS);
			resList.add(gridResource);
		}
		
	}
	
	/**
	 * Generating random GridResource with specific policy 
	 * which has one machine and a default FCFS advancedPolicy
	 *   (Recommended)
	 *   
	 * @param peMIPS 
	 * @param numOfPEPerMachine 
	 * @param resourceName
	 * @return
	 */
	public static GridResource createSimResource(String resourceNameParam, AdvancedPolicy advancedPolicy, 
			String archType, String osType, int numOfPEPerMachine, int peMIPS){
		
		MachineList machineList = new MachineList();
		
		GridResource resource = null;
		
		Random randomEngine = new Random();
		int localNumOfPE = numOfPEPerMachine; 
		//(int) GridSimRandom.real(numOfPEPerMachine, minRange, maxRange, randomEngine.nextDouble());
		
		int localMIPSRating = peMIPS; 
		//(int) GridSimRandom.real(peMIPS, minRange, maxRange, randomEngine.nextDouble());
		
		// Fullfil the PE list and Machine list
		PEList peList = new PEList();
		for (int j = 0; j < localNumOfPE; j++){
			peList.add(new PE(j, localMIPSRating));
		}
		
		machineList.add(new Machine(0, peList));

		// Setup resource characteristic attributes
		ResourceCharacteristics resourceConfig = new ResourceCharacteristics(archType, 
				osType, machineList, ResourceCharacteristics.SPACE_SHARED, 
				MaGateProfile.timeZone, MaGateProfile.useCost);
		
		// Setup resource calendar attributes
		ResourceCalendar resourceCalendar = new ResourceCalendar(MaGateProfile.timeZone, 
				MaGateProfile.peakLoad, MaGateProfile.offPeakLoad, 
				MaGateProfile.holidayLoad, MaGateParam.getWeekEnds(), 
				MaGateParam.getHolidays(), MaGateParam.getStaticSimSeed());
		
		try {
			resource = new GridResource(resourceNameParam, MaGateProfile.bandWidth, 
					resourceConfig, resourceCalendar, advancedPolicy);
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		machineList = null;
		return resource;
	}
	
    /// --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
	///     Unused methods
    /// --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
	
	
	public static void createGridResourceList(int numOfResource) {
		
		ArrayList resList = new ArrayList();
		for (int i = 0; i < numOfResource; i++) {
			GridResource gridResource = createGridResource("sg-resource-" + i);
			resList.add(gridResource);
		}
	}
	
	/**
	 * Creating Grid Resource with default parameters
	 * @param resourceName
	 * @return
	 */
	public static GridResource createGridResource(String resourceName){
		
		MachineList machineList = new MachineList();
		
		GridResource resource = null;
		
		// Fullfil the PE list and Machine list
		for (int i = 0; i < MaGateProfile.numOfMachine_perResource; i++){
			
			PEList peList = new PEList();
			for (int j = 0; j < MaGateProfile.numOfPE_eachResource; j++){
				peList.add(new PE(j, MaGateProfile.peMIPS));
			}
			
			machineList.add(new Machine(i, peList));
			peList = null;
		}
		
		// Setup resource characteristic attributes
		ResourceCharacteristics resourceConfig = new ResourceCharacteristics(MaGateProfile.res_archType, 
				MaGateProfile.osType, machineList, MaGateProfile.allocationPolicy, 
				MaGateProfile.timeZone, MaGateProfile.useCost);
		
		// Setup resource calendar attributes
		ResourceCalendar resourceCalendar = new ResourceCalendar(MaGateProfile.timeZone, 
				MaGateProfile.peakLoad, MaGateProfile.offPeakLoad, 
				MaGateProfile.holidayLoad, MaGateParam.getWeekEnds(), 
				MaGateParam.getHolidays(), MaGateParam.getStaticSimSeed());
		
		try {
			resource = new GridResource(resourceName, MaGateProfile.bandWidth, 
					resourceConfig, resourceCalendar);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		machineList = null;
		return resource;
	}
	
	
	/**
	 * @param resourceName
	 * @param simSeedParam
	 * @return
	 */
	public static GridResource createGridResource(String resourceName, long simSeedParam){
		
		MachineList machineList = new MachineList();
		
		GridResource resource = null;
		
		// Fullfil the PE list and Machine list
		for (int i = 0; i < MaGateProfile.numOfMachine_perResource; i++){
			
			PEList peList = new PEList();
			for (int j = 0; j < MaGateProfile.numOfPE_eachResource; j++){
				peList.add(new PE(j, MaGateProfile.peMIPS));
			}
			machineList.add(new Machine(i, peList));
			peList = null;
		}
		
		// Setup resource characteristic attributes
		ResourceCharacteristics resourceConfig = new ResourceCharacteristics(MaGateProfile.res_archType, 
				MaGateProfile.osType, machineList, MaGateProfile.allocationPolicy, 
				MaGateProfile.timeZone, MaGateProfile.useCost);
		
		// Setup resource calendar attributes
		ResourceCalendar resourceCalendar = new ResourceCalendar(MaGateProfile.timeZone, 
				MaGateProfile.peakLoad, MaGateProfile.offPeakLoad, 
				MaGateProfile.holidayLoad, MaGateParam.getWeekEnds(), 
				MaGateParam.getHolidays(), simSeedParam);
		
		try {
			resource = new GridResource(resourceName, MaGateProfile.bandWidth, 
					resourceConfig, resourceCalendar);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		machineList = null;
		return resource;
	}
	
	
	public static GridResource createGridResource(String resourceName, int numOfMachineParam, 
			int numOfPEParam){
		
		MachineList machineList = new MachineList();
		
		GridResource resource = null;
		
		// Fullfil the PE list and Machine list
		for (int i = 0; i < numOfMachineParam; i++){
			
			PEList peList = new PEList();
			for (int j = 0; j < numOfPEParam; j++){
				peList.add(new PE(j, MaGateProfile.peMIPS));
			}
			machineList.add(new Machine(i, peList));
			peList = null;
		}
		
		// Setup resource characteristic attributes
		ResourceCharacteristics resourceConfig = new ResourceCharacteristics(MaGateProfile.res_archType, 
				MaGateProfile.osType, machineList, MaGateProfile.allocationPolicy, 
				MaGateProfile.timeZone, MaGateProfile.useCost);
		
		// Setup resource calendar attributes
		ResourceCalendar resourceCalendar = new ResourceCalendar(MaGateProfile.timeZone, 
				MaGateProfile.peakLoad, MaGateProfile.offPeakLoad, 
				MaGateProfile.holidayLoad, MaGateParam.getWeekEnds(), 
				MaGateParam.getHolidays(), MaGateParam.getStaticSimSeed());
		
		try {
			resource = new GridResource(resourceName, MaGateProfile.bandWidth, 
					resourceConfig, resourceCalendar);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		machineList = null;
		return resource;
	}
	
}
