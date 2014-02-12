package ch.hefr.gridgroup.magate.env;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.MaGateTrunk;
import ch.hefr.gridgroup.magate.model.MMResult;
import ch.hefr.gridgroup.magate.storage.GlobalStorage;
import ch.hefr.gridgroup.magate.storage.MaGateStorage;
import ch.hefr.gridgroup.magate.MaGateEntity;
import eduni.simjava.Sim_system;

import gridsim.GridSim;

public class MaGatePlatform extends GridSim {
	
	private static Log log = LogFactory.getLog(MaGatePlatform.class);
	
	public MaGatePlatform(String name) throws Exception {
		super(name);
	}
	
	/**
	 * @param numOfUser
	 */
	public static void initSimulation(int numOfUser) {
		
		Calendar calendar               = Calendar.getInstance();   
		
		boolean traceFlag               = false;     // whether to trace the GridSim events/activities
		String [] excludeFromFile       = { "" };    // list of files to be excluded from any statistical measures
		String [] excludeFromProcessing = { "" };    // list of processing to be excluded from any statistical measures
		String reportName               = null;      //  name of report file to be writtern
		
		GridSim.init(numOfUser, calendar, traceFlag, excludeFromFile,
					excludeFromProcessing, reportName);
	}

	
	public void shutSimulation(){
		
		// Tells all user entities to shut down the simulation.
        shutdownUserEntity();
        
	    // Tells the <tt>GridStatistics</tt> entity the end of the simulation
        shutdownGridStatisticsEntity();
        
        // It terminates Entities managing NETWORK communication channels.
        // It can be invoked explicity to shutdown NETWORK communication channels.
        // It is advisable for all entities extending GridSim class, explicitly
        // invoke this method to terminate <tt>Input</tt> and
        // <tt>Output</tt> entities created
        terminateIOEntities();
        
        // Stops Grid Simulation (based on SimJava Sim_system.run_stop()).
        // * This should be only called if any of the user defined entities
        // * <b>explicitly</b> want
        // * to terminate simulation during execution.
        stopGridSimulation();
	}
	
	/**
	 * Clean the system environment for getting experiment results
	 */
	public static void systemFlush() {
		
		MaGateRecorder.filesystemClean();
		MaGateRecorder.prepareOutputFile();
		
	}
	
	/**
	 * Clean the system environment before each scenario
	 */
	public static void scenarioFlush() {
		
		MaGateDWRecorder.flushPerScenarioCommunityStatus();
		
	}
	
}

	
	