package ch.hefr.gridgroup.magate.km;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.MaGateEntity;
import ch.hefr.gridgroup.magate.env.JobCenterManager;
import ch.hefr.gridgroup.magate.env.MaGateMediator;
import ch.hefr.gridgroup.magate.env.MaGateToolkit;
import ch.hefr.gridgroup.magate.storage.GlobalStorage;
import ch.hefr.gridgroup.magate.storage.MaGateDB;

public class MaGateMonitor {

	private static Log log = LogFactory.getLog(MaGateMonitor.class);
	
	private MaGateEntity maGate;
	private String maGateId;
	
	private String resultFile;
	private long presTimeStamp;
	
	public MaGateMonitor(MaGateEntity maGate) {
		
		this.maGate = maGate;
		this.maGateId = maGate.getMaGateIdentity();
		
		this.presTimeStamp = System.currentTimeMillis();
		
		this.initDB();
		// this.initFile();
	}
	
	public void routineCheck() {
		
//		this.logDB();
		
		long currentTime = System.currentTimeMillis();
		
		if(currentTime - this.presTimeStamp > 500) {
			
			this.presTimeStamp = currentTime;
			this.logDB();
			// this.logFile();
		}
		
	}

	// --- --- --- --- --- ---
	// Private Methods
	// --- --- --- --- --- --- 
	
	private void initDB() {
		
//		// Insert blank information into CommunityStatus for each [Scenario/Iteration/MaGate] 
//		MaGateDB.insertCommunityStatus_DB(this.maGate.getMaGateIdentity(), this.maGate.getMaGateNickName());
		
	}
	
	private void logDB() {
		
		// Update latest status of the running community (per scenario, per experiment, per MaGate)
		// Record individual MaGate queue information into DB
		
		GlobalStorage.count_test7.addAndGet(8);
		
//		// GENERATED = 0; SUBMITTED = 1; SCHEDULING = 2; PROCESSING = 3; TRANSFERRED = 4;
//		// EXECUTED = 5; FAILED = 6; SUSPENDED = 7;
//		int[] allStatus = JobCenterManager.sizeOfJob_allStatusByNode(maGateId);
//		
//		MaGateDB.updateCommunityStatus_DB(this.maGate.getMaGateIdentity(), this.maGate.getMaGateNickName(), 
//				allStatus[0], allStatus[1], allStatus[2], allStatus[3], 
//				allStatus[4], allStatus[5], allStatus[7], allStatus[6]
//				);
		
	}
	
	private void initFile() {
		
		this.resultFile     = this.maGate.getMaGateNickName() + "_original.log";
		
		String initContent = "#Resource: (Iteration[" + this.maGate.getIndexOfExperimentIteration() + "])_" + this.maGate.getMaGateNickName() + 
			"\n#MaGateIdentity: (Iteration[" + this.maGate.getIndexOfExperimentIteration() + "])_" + this.maGate.getMaGateIdentity() +
			"\n#TotalPE: (Iteration[" + this.maGate.getIndexOfExperimentIteration() + "])_" + this.maGate.getStorage().getTotalNumOfPEs()
			;
		
		MaGateToolkit.generateResult(this.resultFile);
		MaGateToolkit.writeResult(this.resultFile, initContent);
		
	}
	
	
//	private void logFile() {
//		
//		String value = 
//			this.maGate.getIndexOfExperimentIteration() + "\t" + 
//			MaGateMediator.getSystemTime() + "\t" +                            // statistic line 1
//			
//			this.maGate.getStorage().size_localArchivedJob() + "\t" +         // 2
//			this.maGate.getStorage().size_localProcessingJob() + "\t" +          // 3
//			this.maGate.getStorage().size_localExedJob() + "\t" +       // 4
//			this.maGate.getStorage().size_localUnsuitedJob() + "\t" +   // 5
//			
//			this.maGate.getStorage().size_outputSentJob() + "\t" +      // 6
//			this.maGate.getStorage().size_outputReturnedExedJob() + "\t" +      // 7
//			this.maGate.getStorage().size_outputReturnedFailedJob() + "\t" +  // 8
//			
//			this.maGate.getStorage().size_inputArchivedJob() + "\t" +         // 9
//			this.maGate.getStorage().size_inputDeliveredExedJob() + "\t" +  // 10
//			this.maGate.getStorage().size_inputDeliveredExedFailedJob() + "\t" +   // 11
//			
//			MaGateToolkit.storageToApp(this.maGate.getStorage().getResourceEfficency()) + "\t"  // resource usage line 12
//			;
//		
//		MaGateToolkit.writeResult(this.resultFile, value);
//		
//	}
	
}

