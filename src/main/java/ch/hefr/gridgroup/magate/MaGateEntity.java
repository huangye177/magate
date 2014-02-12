package ch.hefr.gridgroup.magate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.casa.CASAController;
import ch.hefr.gridgroup.magate.cfm.CFCController;
import ch.hefr.gridgroup.magate.cfm.CFMController;
import ch.hefr.gridgroup.magate.cm.*;
import ch.hefr.gridgroup.magate.em.*;
import ch.hefr.gridgroup.magate.env.MaGateInfrastructure;
import ch.hefr.gridgroup.magate.env.MaGateMessage;
import ch.hefr.gridgroup.magate.env.MaGateParam;
import ch.hefr.gridgroup.magate.env.MaGateProfile;
import ch.hefr.gridgroup.magate.env.ResourceEngine;
import ch.hefr.gridgroup.magate.im.*;
import ch.hefr.gridgroup.magate.input.ExpNode;
import ch.hefr.gridgroup.magate.km.*;
import ch.hefr.gridgroup.magate.lm.*;
import ch.hefr.gridgroup.magate.storage.MaGateStorage;


/**
 * Class MaGateEntity generates an individual MaGate instance, 
 * which includes: Kernel Module, Interface Module, Community Module, LRM Module and External Module
 * 
 * @author Ye HUANG
 */
@SuppressWarnings("unused")
public class MaGateEntity {
	
	private static Log log = LogFactory.getLog(MaGateEntity.class);
	
	private ResourceEngine resEngine;

	private String maGateIdentity;
	
	private String maGateNickName;
	
	private int maGateSerial;

	private MaGateStorage storage;
	
	private HashMap<String,Object> maGateProfile = new HashMap<String,Object>();

	private IResDiscovery resDiscovery;
	
	private ILRM lrm;
	
	private IJobSubmitter jobSubmitter;
	
	private MatchMaker matchMaker;
	
	private ModuleController moduleController;
	
	private CommunityMonitor communityMonitor;
	
	private OutputRequester outputRequester;
	
	private InputRequester inputRequester;

	private OutputResponser outputResponser;
	
	private InputResponser inputResponser;
	
	private CASAController caspController;
	
	private CFMController cfmController;

	private int indexOfExperimentIteration;
	
	private MaGateInfrastructure maGateInfra;

	private double communtiyPrice; // 0..2

	private String maGateOS;
	
	private int nodeStatus;


	public MaGateEntity(String maGateIdentity, int maGateSerial, int indexOfExperiment, ResourceEngine resEngine, LinkedList<MaGateStorage> resultPerIteration, 
			String currentExperimentMark, MaGateInfrastructure maGateInfra, 
			ExpNode node, ConcurrentHashMap<String, String> cfmPolicy, String localPolicy, int nodeStatus) {
		
		nodeInitialization(maGateIdentity, maGateSerial, indexOfExperiment, resEngine, resultPerIteration, currentExperimentMark, maGateInfra,  
				node, cfmPolicy, localPolicy, nodeStatus);
		
	}

	
	private void nodeInitialization(String maGateIdentity, int maGateSerial, int indexOfExperiment, ResourceEngine resEngine, LinkedList<MaGateStorage> resultPerIteration, 
			String currentExperimentMark, MaGateInfrastructure maGateInfra, 
			ExpNode node, ConcurrentHashMap<String, String> cfmPolicy, String localPolicy, int nodeStatus) {
		
		try {
			
			this.maGateIdentity = maGateIdentity;
			this.maGateSerial   = maGateSerial;
			this.maGateNickName = "M" + maGateSerial;
			this.maGateOS       = node.getNodeOS();
			
			this.indexOfExperimentIteration = indexOfExperiment;
			
			
			this.maGateInfra = maGateInfra;
			this.maGateInfra.maGateJoin(this);
			
			// price for community job execution
			Random rd           = new Random();
			this.communtiyPrice = rd.nextFloat() * 2;
			this.resEngine      = resEngine;
			
			// ye: PHASE 1
			// ye: initialize place to storage simulated local resources
			// ye: register lrmStorage to outside result collector
			this.storage = new MaGateStorage(this.maGateIdentity, this.maGateNickName, this.maGateOS, node.getVoId());
			resultPerIteration.offer(storage);
			
			// ye: PHASE 2
			// ye: initialize LRM with [amount] resource, with system
			// default MIPS and PE setting
			this.lrm = new SimLRM(this, node.getNodeArch(), node.getNodeOS(), 
					node.getNumberOfResource(), node.getNumberOfPEperResource(), 
					node.getPeMIPS(), node.getVoId());
			
			// ye: register lrm to outside resource engine
			// NOTICE: the simulation resource creation could be processed within SimLRM, 
			// however, it requires SimLRM to extends GridSim for using "super.getResourceCharacteristics"
			// In order to reduce the threads workload in GridSim/SimJava, 
			// The resource-creation related operations are all put into resEngine
			this.resEngine.addObserver(lrm);
			
			// ye: PHASE 3
			// ye: create [amount] programmed simulation jobs, with default system setting
			this.jobSubmitter = new SimJobSubmitter(this, node.getJobQueue());
			
			// ye: PHASE 4
			// ye: prepare the MatchMaker
			this.matchMaker = new MatchMaker(this, localPolicy);
			
			// ye: PHASE 5
			// ye: generate MaGate.KernelModule.ModuleController which manages the interaction of 
			// other MaGate modules
			String moduleControllerName = maGateIdentity + "_ModuleController_" + currentExperimentMark;
			this.moduleController = new ModuleController(moduleControllerName, MaGateProfile.bandWidth, this);
			
			// ye: PHASE 6
			// ye: preparing the resource discovery service, which will be used by 
			// CommunityModule and ModuleController
			this.resDiscovery = new SSLService(this);
			
			// ye: PHASE 7
			// ye: setup community component
			this.communityMonitor = new CommunityMonitor(this);
			this.outputRequester  = new OutputRequester(this);
			this.outputResponser  = new OutputResponser(this);
			this.inputRequester   = new InputRequester(this);
			this.inputResponser   = new InputResponser(this);
			
			// ye: PHASE 8
			// ye: prepare the MaGateEmitter for sending/getting messages
			
			// ye: PHASE 9
			this.caspController = new CASAController(this);
			
			// ye: PHASE 10
			this.cfmController = new CFMController(this, cfmPolicy);
			
			// ye: PHASE 11
			this.nodeStatus = nodeStatus;
			
		} catch (Exception e) {
			log.error("Unwanted errors happen in MaGateTrunk, while preparing components");
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public String getMaGateIdentity() {
		return maGateIdentity;
	}
	
	public String getMaGateNickName() {
		return maGateNickName;
	}

	public ILRM getLRM() {
		return lrm;
	}

	public IJobSubmitter getJobSubmitter() {
		return jobSubmitter;
	}

	public MatchMaker getMatchMaker() {
		return matchMaker;
	}

	public ModuleController getModuleController() {
		return moduleController;
	}
	
	public IResDiscovery getResDiscovery() {
		return resDiscovery;
	}

	public MaGateStorage getStorage() {
		return storage;
	}
	
	public ResourceEngine getResEngine() {
		return resEngine;
	}
	
	public CommunityMonitor getCommunityMonitor() {
		return communityMonitor;
	}
	
	public OutputRequester getOutputRequester() {
		return outputRequester;
	}

	public InputRequester getInputRequester() {
		return inputRequester;
	}
	
	public OutputResponser getOutputResponser() {
		return outputResponser;
	}

	public InputResponser getInputResponser() {
		return inputResponser;
	}
	
	public int getIndexOfExperimentIteration() {
		return indexOfExperimentIteration;
	}
	
	public int getMaGateSerial() {
		return maGateSerial;
	}	
	
	public HashMap<String, Object> getMaGateProfile() {
		return maGateProfile;
	}

	public void setMaGateProfile(HashMap<String, Object> maGateProfile) {
		this.maGateProfile = maGateProfile;
	}
	
	public double getCommuntiyPrice() {
		return communtiyPrice;
	}

	public void setCommuntiyPrice(double communtiyPrice) {
		this.communtiyPrice = communtiyPrice;
	}
	
	public MaGateInfrastructure getMaGateInfra() {
		return maGateInfra;
	}
	
	public String getMaGateOS() {
		return maGateOS;
	}
	
	public CASAController getCASPController() {
		return this.caspController;
	}
	
	public CFMController getCFMController() {
		return cfmController;
	}
	
	public int getNodeStatus() {
		return nodeStatus;
	}
}


