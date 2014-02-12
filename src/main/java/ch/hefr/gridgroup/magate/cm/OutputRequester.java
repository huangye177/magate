package ch.hefr.gridgroup.magate.cm;

import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

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
import ch.hefr.gridgroup.magate.model.DelegatedJobItem;
import ch.hefr.gridgroup.magate.model.JobInfo;
import ch.hefr.gridgroup.magate.model.MaGateAgreement;
import ch.hefr.gridgroup.magate.model.RemoteNodeReputationItem;
import ch.hefr.gridgroup.magate.storage.GlobalStorage;

public class OutputRequester {

	private static Log log = LogFactory.getLog(OutputRequester.class);
	
	private MaGateEntity maGate;
	private String maGateIdentity;
	private Vector<JobInfo> cachedToDeliverJobInfo;
	
	/** decay rate of the accuracy of neighborhood remote MaGates, basic unit: 0.00001 */
	private double decayRate;
	
	public OutputRequester(MaGateEntity maGate) {
		this.maGate                 = maGate;
		this.maGateIdentity         = maGate.getMaGateIdentity();
		this.cachedToDeliverJobInfo = new Vector<JobInfo>();
		
		this.decayRate = 0.1;
	}
	
	public MsgAccept processREQUEST(MsgRequest msgRequest, MaGateEntity targetedMaGate) {
		
		int messageTag = CASAMessage.CASP_MSG_REQUEST;
		return this.caspMsgRequestOutput(msgRequest, targetedMaGate, messageTag);
		
	}
	
	
	public MsgAccept processINFORM(MsgInform msgInform, MaGateEntity targetedMaGate) {
		
		int messageTag = CASAMessage.CASP_MSG_INFORM;
		return this.caspMsgInformOutput(msgInform, targetedMaGate, messageTag);
		
	}
	
	// --- --- ---
	
	private MsgAccept caspMsgRequestOutput(MsgRequest msgRequest, MaGateEntity targetedMaGate, int messageTag) {
		
		if(!MaGateParam.Agreement_Enabled) {
			
			// Agreement specification is DISABLED
			return targetedMaGate.getInputRequester().processREQUEST(msgRequest, this.maGateIdentity, messageTag);
			
		} else {
			
			JobInfo jobInfo = JobCenterManager.getJobInfobyJobId(msgRequest.jobId);
			
			// Agreement specification is ENABLED
			//  transfer the Agreement (jobinfo reference attached as stage-in element) to remote MaGate 
			MaGateAgreement agreement = MaGateMediator.convertJobInfoToAgreement(this.maGate.getMaGateIdentity(), jobInfo);
			
			if(agreement == null) {
				return null;
			} else {
				return targetedMaGate.getInputRequester().processREQUEST(agreement, this.maGateIdentity, messageTag, msgRequest.replicsOfMsgRequest);
			}
			
		}
	}
	
	
	private MsgAccept caspMsgInformOutput(MsgInform msgInform, MaGateEntity targetedMaGate, int messageTag) {
		
		if(!MaGateParam.Agreement_Enabled) {
			
			// Agreement specification is DISABLED
			return targetedMaGate.getInputRequester().processINFORM(msgInform, this.maGateIdentity, messageTag);
			
		} else {
			
			JobInfo jobInfo = JobCenterManager.getJobInfobyJobId(msgInform.jobId);
			
			// Agreement specification is ENABLED
			//  transfer the Agreement (jobinfo reference attached as stage-in element) to remote MaGate 
			MaGateAgreement agreement = MaGateMediator.convertJobInfoToAgreement(this.maGate.getMaGateIdentity(), jobInfo);
			
			if(agreement == null) {
				return null;
			} else {
				return targetedMaGate.getInputRequester().processINFORM(agreement, this.maGateIdentity, messageTag, msgInform.replicsOfMsgRequest, 
						msgInform.estResponseTime, msgInform.avgQueuingTime);
			}
			
		}
	}
	
	
	public void processASSIGN(JobInfo jobInfo, MaGateEntity targetedMaGate) {
		
		String jobId    = jobInfo.getGlobalJobID();
		String remoteId = targetedMaGate.getMaGateIdentity();
		
		String originalNodeId     = jobInfo.getOriginalMaGateId();
		MaGateEntity originalNode = GlobalStorage.findMaGateById(originalNodeId);
		
		// Action 1: assign the job to the selected assign node
		targetedMaGate.getInputRequester().processASSIGN(jobInfo);
		 
		
		// Action 2: update hosting node's DelegatedJobTable
		double estTimeForJob = JobCenterManager.getJobInfobyJobId(jobId).getEstimatedComputationTime();
		double estTimeForLoadOfLocalJobQueue = targetedMaGate.getMatchMaker().estimatedTimeToExecuteLocalJobQueue();
		
		double estResponseTime = estTimeForJob + estTimeForLoadOfLocalJobQueue;
		
		// is it an existing job delegation record in the original node's storage?
		boolean alreadyDelegatedJob = originalNode.getStorage().contain_delegatedjobList(jobId);
		
		if(alreadyDelegatedJob) {
			
			// existing results, need to update JobId based job delegation history
			DelegatedJobItem delegatedJobRecord = originalNode.getStorage().get_delegatedJobListItem(jobId);
			
			double previousPromisedResponseTime = delegatedJobRecord.promisedResponseTime;
			double previousTimestamp = delegatedJobRecord.timestamp;
			double timeDifference    = MaGateMediator.getSystemTime() - previousTimestamp;
			
			double score = 0;
			if((estResponseTime < previousPromisedResponseTime) && (previousPromisedResponseTime - estResponseTime > timeDifference)) {
				// better results recommended from the job re-delegation
				score = MaGateMessage.CFM_SCORE_SUCCESS;
			} else {
				score = MaGateParam.cfmDelegationFailScore;
			}
			
			// remove current job delegation record, and put the latest job delegation record (putIfAbsent)
			originalNode.getStorage().remove_delegatedJobListItem(jobId);
			
			// if the newly selected assignee node is not the job's original node
			if(remoteId != originalNodeId) {
				DelegatedJobItem newDelegatedJobRecord = new DelegatedJobItem(jobId, remoteId, estResponseTime);
				originalNode.getStorage().join_delegatedjobList(newDelegatedJobRecord);
			}
			
			
			// score the previous job delegation record
			boolean existingRemoteNodeCooperationRecord = originalNode.getStorage().contain_remoteNodeReputationList(this.maGateIdentity);
			if(existingRemoteNodeCooperationRecord) {
				RemoteNodeReputationItem remoteNodeReputationItem = originalNode.getStorage().get_remoteNodeReputationListItem(this.maGateIdentity);
				remoteNodeReputationItem.putScore(score);
			} else {
				RemoteNodeReputationItem remoteNodeReputationItem = new RemoteNodeReputationItem(this.maGateIdentity);
				remoteNodeReputationItem.putScore(score);
				originalNode.getStorage().join_remoteNodeReputationList(remoteNodeReputationItem);
			}
			
		} else {
			
			// new job delegation record
			// if the newly selected assignee node is not the job's original node
			if(remoteId != originalNodeId) {
				DelegatedJobItem newDelegatedJobRecord = new DelegatedJobItem(jobId, remoteId, estResponseTime);
				originalNode.getStorage().join_delegatedjobList(newDelegatedJobRecord);
			}
			
		}
		
	}
	
	/**
	 * Inform the delegated job's original node for a job completion notification
	 * 
	 * @param jobInfo
	 */
	public void JobDoneForRemoteNodes(JobInfo jobInfo) {
		
		if(this.maGate.getNodeStatus() == MaGateMessage.NodeStatus_AllStable) {
			
			this.jobSent(jobInfo);
			
		} else if (this.maGate.getNodeStatus() == MaGateMessage.NodeStatus_HalfLazy) {
			
			
			// behave differently depending the current system time
			if(MaGateMediator.isDayTime()) {
				this.cachedToDeliverJobInfo.add(jobInfo);
				
			} else {
				// send the job completion information back immediately
				this.jobSent(jobInfo);
				
				// send all cached un-delivered jobs out, then clean the "cached Job Vector" up
				if(this.cachedToDeliverJobInfo.size() != 0) {
					
					for(JobInfo cachedJobInfo : this.cachedToDeliverJobInfo) {
						this.jobSent(cachedJobInfo);
					}
					this.cachedToDeliverJobInfo.clear();
				}
			}
		}
		
	}
	
	/**
	 * Send the job to its original node and then update hosting node's community job execution confirmation
	 * @param jobInfo
	 */
	private void jobSent(JobInfo jobInfo) {
		
		// update job's response time
		double jobFinishTime = jobInfo.getJob().getFinishTime();
		double queuedTime    = MaGateMediator.getSystemTime() - jobFinishTime;
		
		// trigger hosting node's MatchMaker.communityJobFinishedConfirmation() method
		// with an appending queuedTime to make an accurate job response time
		this.maGate.getMatchMaker().communityJobFinishedConfirmation(jobInfo, queuedTime);
		
		// send the job to its original node
		GlobalStorage.findMaGateById(jobInfo.getOriginalMaGateId()).getCommunityMonitor().jobDelegationCompleteByRemoteNode(jobInfo);
		
	}
	
	
	/**
	 * Modify the jobInfo for re-negotiation
	 * @param jobInfo
	 * @return
	 * @deprecated
	 */
	private JobInfo modifyForRenegotiation(JobInfo fetchedJobInfo) {
		
		ConcurrentHashMap<String, Object> currentProfile = fetchedJobInfo.getCommunityJobInfoProfile();
		
		if(currentProfile.containsKey(MaGateMessage.MatchProfile_ExePrice)) {
			
//			Double currentPrice = (Double) currentProfile.get(MaGateMessage.MatchProfile_ExePrice);
			
			Object priceObject = currentProfile.get(MaGateMessage.MatchProfile_ExePrice);
			
			if(priceObject instanceof Double) {
				Double currentPrice = (Double) currentProfile.get(MaGateMessage.MatchProfile_ExePrice);
				currentProfile.put(MaGateMessage.MatchProfile_ExePrice, new Double(currentPrice * 2.0));
				
			} else if (priceObject instanceof Integer) {
				Integer currentPrice = (Integer) currentProfile.get(MaGateMessage.MatchProfile_ExePrice);
				currentProfile.put(MaGateMessage.MatchProfile_ExePrice, new Double(currentPrice * 2.0));
				
			}
			
			
			
		} else {
			currentProfile.put(MaGateMessage.MatchProfile_ExePrice, new Double(-1.0));
			
		}
		
		fetchedJobInfo.setCommunityJobInfoProfile(currentProfile);
		
		// update re-negotiation counter
		fetchedJobInfo.setJobNegotiationCounter(new AtomicInteger(fetchedJobInfo.getJobNegotiationCounter().get() + 1));
		
		return fetchedJobInfo;
	}
	
}




