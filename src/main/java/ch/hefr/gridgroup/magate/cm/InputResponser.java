/**
 * 
 */
package ch.hefr.gridgroup.magate.cm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.MaGateEntity;
import ch.hefr.gridgroup.magate.em.NetworkNeighborsManager;
import ch.hefr.gridgroup.magate.env.MaGateMessage;
import ch.hefr.gridgroup.magate.model.JobInfo;

/**
 * @author yehuang
 *
 */
public class InputResponser {

	private static Log log = LogFactory.getLog(InputResponser.class);
	
	private MaGateEntity maGate;
	private String maGateIdentity;
	
	public InputResponser(MaGateEntity maGate) {
		this.maGate = maGate;
		this.maGateIdentity = maGate.getMaGateIdentity();
	}
	
//	public void processJobDone(JobInfo jobInfo, int jobDoneStatus) {
//		
//		if(jobDoneStatus == MaGateMessage.JOB_SUCCESS) {
//			this.maGate.getCFCController().cacheCFCNeighbors(jobInfo.getExecutionMaGateId());
//		}
//		
//	}

}
