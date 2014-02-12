/**
 * 
 */
package ch.hefr.gridgroup.magate.cm;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType;
import org.ogf.schemas.graap.wsAgreement.ServiceDescriptionTermType;

import ch.hefr.gridgroup.magate.MaGateEntity;
import ch.hefr.gridgroup.magate.casa.CASAMessage;
import ch.hefr.gridgroup.magate.casa.MsgAccept;
import ch.hefr.gridgroup.magate.casa.MsgInform;
import ch.hefr.gridgroup.magate.casa.MsgRequest;
import ch.hefr.gridgroup.magate.env.JobCenterManager;
import ch.hefr.gridgroup.magate.env.MaGateMediator;
import ch.hefr.gridgroup.magate.env.MaGateMessage;
import ch.hefr.gridgroup.magate.env.MaGateParam;
import ch.hefr.gridgroup.magate.model.JobInfo;
import ch.hefr.gridgroup.magate.model.MaGateAgreement;

/**
 * @author yehuang
 *
 */
public class InputRequester {

	private static Log log = LogFactory.getLog(InputRequester.class);
	
	private MaGateEntity maGate;
	private String maGateIdentity;
	
	public InputRequester(MaGateEntity maGate) {
		this.maGate  = maGate;
		this.maGateIdentity = maGate.getMaGateIdentity();
	}
	
	public MsgAccept processREQUEST(MsgRequest msgRequest, String requesterNodeId, int MessageTag) {
			
		// Agreement specification is DISABLED
		return this.maGate.getCASPController().processACCEPT(msgRequest, requesterNodeId, MessageTag);
		
	}
	
	public MsgAccept processINFORM(MsgInform msgInform, String requesterNodeId, int MessageTag) {
		
		// Agreement specification is DISABLED
		return this.maGate.getCASPController().processACCEPT(msgInform, requesterNodeId, MessageTag);
		
	}
	
	
	public MsgAccept processREQUEST(MaGateAgreement incomingAgreement, String requesterNodeId, int MessageTag, double replicsOfMsgRequest) {
		
		// Agreement specification is ENABLED
		
		JobDefinitionType jsdlJobDefinition = null;
        
		String jobGlobalId = "";
        String requestedOS = "";
        double cpuCount = 0.0;
        
        try {
			/** 
			 * Convert information from JobInfo into Agreement, including:
			 * jobId, OS, CPUCount, ExePrice
			 */
			ServiceDescriptionTermType sdt = incomingAgreement.getTerms().getAll().getServiceDescriptionTermArray(0);
			jsdlJobDefinition = MaGateMediator.getJSDLJobDefinitionFromSDT(sdt);
			
			jobGlobalId = jsdlJobDefinition.getJobDescription()
					.getJobIdentification().getJobName();
			
			// mandatory parameter
			requestedOS = jsdlJobDefinition.getJobDescription().getResources()
					.getOperatingSystem().getOperatingSystemType()
					.getOperatingSystemName().toString();
			
			if(jsdlJobDefinition.getJobDescription().getResources().getTotalCPUCount() != null) {
				
				RangeValueType cpuCountType = jsdlJobDefinition.getJobDescription().getResources().getTotalCPUCount();
		
				cpuCount = cpuCountType.getExactArray(0).getDoubleValue();
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.toString());
		}
		
		return this.maGate.getCASPController().processACCEPT(requestedOS, (int)cpuCount, 
				jobGlobalId, replicsOfMsgRequest, requesterNodeId, MessageTag, -1, -1);
		
	}
	
	
	public MsgAccept processINFORM(MaGateAgreement incomingAgreement, String requesterNodeId, int MessageTag, double replicsOfMsgRequest, 
			double estRemoteResponseTime, double avgRemoteQueuingTime) {
		
		// Agreement specification is ENABLED
		
		JobDefinitionType jsdlJobDefinition = null;
        
		String jobGlobalId = "";
        String requestedOS = "";
        double cpuCount = 0.0;
        
        try {
			/** 
			 * Convert information from JobInfo into Agreement, including:
			 * jobId, OS, CPUCount, ExePrice
			 */
			ServiceDescriptionTermType sdt = incomingAgreement.getTerms().getAll().getServiceDescriptionTermArray(0);
			jsdlJobDefinition = MaGateMediator.getJSDLJobDefinitionFromSDT(sdt);
			
			jobGlobalId = jsdlJobDefinition.getJobDescription()
					.getJobIdentification().getJobName();
			
			// mandatory parameter
			requestedOS = jsdlJobDefinition.getJobDescription().getResources()
					.getOperatingSystem().getOperatingSystemType()
					.getOperatingSystemName().toString();
			
			if(jsdlJobDefinition.getJobDescription().getResources().getTotalCPUCount() != null) {
				
				RangeValueType cpuCountType = jsdlJobDefinition.getJobDescription().getResources().getTotalCPUCount();
		
				cpuCount = cpuCountType.getExactArray(0).getDoubleValue();
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.toString());
		}
		
		return this.maGate.getCASPController().processACCEPT(requestedOS, (int)cpuCount, 
				jobGlobalId, replicsOfMsgRequest, requesterNodeId, MessageTag, estRemoteResponseTime, avgRemoteQueuingTime);
		
	}

	
	public void processASSIGN(JobInfo jobInfo) {
		
		this.maGate.getModuleController().processJobArrival(jobInfo);
		
	}

}
