package ch.hefr.gridgroup.magate.casa;

import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import com.apple.crypto.provider.Debug;

import sun.awt.GlobalCursorManager;

import ch.hefr.gridgroup.magate.MaGateEntity;
import ch.hefr.gridgroup.magate.cfm.CFMPolicy;
import ch.hefr.gridgroup.magate.em.NetworkNeighborsManager;
import ch.hefr.gridgroup.magate.env.JobCenterManager;
import ch.hefr.gridgroup.magate.env.MaGateMediator;
import ch.hefr.gridgroup.magate.env.MaGateMessage;
import ch.hefr.gridgroup.magate.env.MaGateParam;
import ch.hefr.gridgroup.magate.km.MaGateMonitor;
import ch.hefr.gridgroup.magate.model.JobInfo;
import ch.hefr.gridgroup.magate.model.MaGateAgreement;
import ch.hefr.gridgroup.magate.storage.GlobalStorage;

public class CASAController {

	private MaGateEntity maGate;
	private String maGateIdentity;
	private long previousRealClock;
	private double previousSimClock;
	
	private static Log log = LogFactory.getLog(CASAController.class);
	
	public CASAController(MaGateEntity maGate) {
		
		this.maGate         = maGate;
		this.maGateIdentity = maGate.getMaGateIdentity();
		this.previousRealClock = System.currentTimeMillis();
		this.previousSimClock  = MaGateMediator.getSystemTime();
		
	}
	
	/**
	 * Dispersing prepared CASP REQUEST message to the network for searching a remote candidate 
	 * The REQUEST messages are created for "submitted" jobs
	 */
	public void processREQUEST() {
		
		try {
			
			GlobalStorage.count_caspController.incrementAndGet();
			
			GlobalStorage.count_test6.incrementAndGet();
			int sizeOfSubmittedJob = JobCenterManager.sizeOfJob_processingNode(this.maGateIdentity, JobCenterManager.SUBMITTED); 
			
			if(sizeOfSubmittedJob < 1 || this.maGate.getResDiscovery() == null) {
				return;
			}

			String[] discoveredRemoteMaGateArray = null;
			
			Vector<JobInfo> submittedJobInfo = JobCenterManager.getJob_processingNode(this.maGateIdentity, JobCenterManager.SUBMITTED); 
			
			// Prepare Global Information System for Centralized Scheduling Scheme
			Vector<ShadowNode> shadowNodes = new Vector<ShadowNode>();
			if (MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Centralized) {
				
				Collection<MaGateEntity> nodes =  GlobalStorage.getAllMaGate();
				
				for(MaGateEntity node : nodes) {
					ShadowNode shadowNode = new ShadowNode(node.getMaGateIdentity(), node, 
							node.getStorage().getTotalActivePEs().get(), node.getStorage().getTotalNumOfPEs().get());
					shadowNodes.add(shadowNode);
				}
				
			}
			
			
			// Disseminating to-output REQUEST_job to the network
			for(JobInfo fetchedJobInfo : submittedJobInfo) {
				
				if(fetchedJobInfo == null) {
					continue;
				}
				
				if(fetchedJobInfo.getCommunityJobInfoProfile() == null) {
					log.warn("NO JobInfo Profile found, unacceptable for CASP REQUEST message dissemination");
					continue;
				}
				
				/************************************************
				 *             Scheduling Schemes 
				 ************************************************/
				
				if(MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Independent) {
					
					/**************** Independent Scheduling Schemes ****************/
					
					if(this.maGate.getMatchMaker().check_jobMatchResource(fetchedJobInfo.getJob().getOSRequired(), fetchedJobInfo.getNumPE())) {
						
						this.maGate.getModuleController().processJobArrival(fetchedJobInfo);
						
					} else {
						
						this.maGate.getModuleController().processJobUndelivered(fetchedJobInfo);
						JobCenterManager.jobSuspended(fetchedJobInfo.getGlobalJobID(), this.maGateIdentity, fetchedJobInfo);
						
					}
					
				} else if (MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Centralized) {
					
					/**************** Centralized Scheduling Schemes ****************/
					
					ShadowNode selectedInstantNode  = null;
					Vector<ShadowNode> selectedInstantNodeVector  = new Vector<ShadowNode>();
					ShadowNode selectedReservedNode = null;
					Vector<ShadowNode> selectedReservedNodeVector = new Vector<ShadowNode>();
					
					for(ShadowNode shadowNode : shadowNodes) {
						
						// try to find a node with instant available PEs
						if(shadowNode.node.getMatchMaker().check_jobInstantMatchResource(fetchedJobInfo.getJob().getOSRequired(), fetchedJobInfo.getNumPE())) {
							
							selectedInstantNodeVector.add(shadowNode);
							
//							if(selectedInstantNode == null) {
//								selectedInstantNode = shadowNode;
//							} else {
//								if(shadowNode.node.getMatchMaker().sizeOfLocalJobQueue() < selectedInstantNode.node.getMatchMaker().sizeOfLocalJobQueue()) {
//									selectedInstantNode = shadowNode;
//								}
//							}
						}
						
						// try to find a node with theoretical available PEs
						if(shadowNode.node.getMatchMaker().check_jobMatchResource(fetchedJobInfo.getJob().getOSRequired(), fetchedJobInfo.getNumPE())) {
							
							selectedReservedNodeVector.add(shadowNode);
							
//							if(selectedReservedNode == null) {
//								selectedReservedNode = shadowNode;
//							} else {
//								if(shadowNode.node.getMatchMaker().sizeOfLocalJobQueue() < selectedReservedNode.node.getMatchMaker().sizeOfLocalJobQueue()) {
//									selectedReservedNode = shadowNode;
//								}
//							}
							
						}
					}
					
					
					if(selectedInstantNodeVector.size() != 0) {
						
						for(ShadowNode candidateInstantNode : selectedInstantNodeVector) {
							
							if(selectedInstantNode == null) {
								selectedInstantNode = candidateInstantNode;
							} else {
								if(selectedInstantNode.node.getStorage().getTotalVirtualFreePEs() > 
										candidateInstantNode.node.getStorage().getTotalVirtualFreePEs()) {
									// select the node with most free PEs
									selectedInstantNode = candidateInstantNode;
								}
							}
						}
						
						selectedInstantNode.node.getModuleController().processJobArrival(fetchedJobInfo);
						
						// --- --- --- --- --- ---
						
//						Random r = new Random(System.currentTimeMillis());
//						selectedInstantNode = selectedInstantNodeVector.get(r.nextInt(selectedInstantNodeVector.size()));
//						
//						selectedInstantNode.node.getModuleController().processJobArrival(fetchedJobInfo);
						
					} else if (selectedReservedNodeVector.size() != 0) {
						
						for(ShadowNode candidateNode : selectedReservedNodeVector) {
							
							if(selectedReservedNode == null) {
								selectedReservedNode = candidateNode;
							} else {
								if(selectedReservedNode.node.getStorage().getTotalNumOfPEs().get() > 
										candidateNode.node.getStorage().getTotalNumOfPEs().get()) {
									// select the node with most free PEs
									selectedReservedNode = candidateNode;
								}
							}
						}
						
						selectedReservedNode.node.getModuleController().processJobArrival(fetchedJobInfo);
						
						// --- --- --- --- --- ---
						
//						Random r = new Random(System.currentTimeMillis());
//						selectedReservedNode = selectedReservedNodeVector.get(r.nextInt(selectedReservedNodeVector.size()));
//						
//						selectedReservedNode.node.getModuleController().processJobArrival(fetchedJobInfo);
						
					} else {
						this.maGate.getModuleController().processJobUndelivered(fetchedJobInfo);
						
					}
					
				} else if (MaGateParam.schedulingScheme == MaGateMessage.SchedulingScheme_Decentralized) {
					
					/**************** Decentralized Scheduling Schemes ****************/
					
					discoveredRemoteMaGateArray = this.maGate.getCommunityMonitor().discoverRemoteNodes();
					
					// create CASA request message
					MsgRequest msgRequest = new MsgRequest(this.maGateIdentity, fetchedJobInfo.getGlobalJobID(), fetchedJobInfo.getNumPE(), 
							fetchedJobInfo.getJob().getOSRequired(), discoveredRemoteMaGateArray.length);
					
					
					Vector<CandidateNode> candidateMaGates = new Vector<CandidateNode>();
					
					/************************************************************
					 * Sends REQUEST messages to the candidate MaGates, and waits for ACCEPT messages
					 ************************************************************/
					
					/** 1st: Gain ACCEPT from remote nodes */
					
					// For each job, sends CASP REQUEST to the discovered remote nodes
					for(String targetedMaGateId : discoveredRemoteMaGateArray) {
						
						MaGateEntity targetedMaGate = GlobalStorage.findMaGateById(targetedMaGateId);
						
						if(targetedMaGate == null) {
							continue;
						} 
						
						GlobalStorage.count_casp_request_Controller.incrementAndGet();
						
						// ask for ACCEPT from the contacted remote node
						// but the remote node is not supposed to take the job at this step
						MsgAccept msgAccept = this.maGate.getOutputRequester().processREQUEST(msgRequest, targetedMaGate);
						
						// Counter
						GlobalStorage.count_msgREQUEST.incrementAndGet();
						
						if(msgAccept != null && msgAccept.isAccepted && msgAccept.responderNodeId != null && !msgAccept.responderNodeId.equals("")) {
							
							// Make the calculation based on the received "ACCEPT" messages, and decide an ordered candidate list.
							// Try to delegate the job to the first candidate of aforementioned list, otherwise the second one, and so on.
							candidateMaGates.add(new CandidateNode(GlobalStorage.findMaGateById(msgAccept.responderNodeId), msgAccept.estTimeForJob, 
									msgAccept.estTimeForLoadOfLocalJobQueue, msgAccept.estTimeForLoadOfShadowJobQueue));
							
						} 
					}
					
					/** 2nd: Gain ACCEPT from local node */
					
					MsgAccept msgAccept = this.maGate.getCASPController().processACCEPT(msgRequest, this.maGateIdentity, CASAMessage.CASP_MSG_REQUEST);
					
					// The local node is also considered, if it satisfies the job requirement, put it into the candidate MaGate Array also
					if(msgAccept != null && msgAccept.isAccepted && msgAccept.responderNodeId != null && !msgAccept.responderNodeId.equals("")) {
						candidateMaGates.add(new CandidateNode(this.maGate, msgAccept.estTimeForJob, 
								msgAccept.estTimeForLoadOfLocalJobQueue, msgAccept.estTimeForLoadOfShadowJobQueue));
					}
					
					/************************************************************
					 * Send the job to a selected Assignee according to received ACCEPT messages
					 ************************************************************/
					
					MaGateEntity assigneeMaGate = this.selectCandidateRemoteNode(candidateMaGates, CASAMessage.CASP_MSG_ASSIGN_FOR_REQUEST);
					
					if(assigneeMaGate != null) {
						
						// Counter
						GlobalStorage.count_msgASSIGN_REQUEST.incrementAndGet();
						
//						// record the remotedNode Id which helps the job execution
//						this.maGate.getCFCController().cacheCFCNeighbors(assigneeMaGate.getMaGateIdentity());
						
						// deliver the job the Assignee as either a local job or a community job
						this.maGate.getOutputRequester().processASSIGN(fetchedJobInfo, assigneeMaGate);
//						if(fetchedJobInfo.getOriginalMaGateId().equals(assigneeMaGate.getMaGateIdentity())) {
//							
//							// local job identified
//							assigneeMaGate.getModuleController().processJobArrival(fetchedJobInfo);
//							
//						} else {
//							// community job identified
//							this.maGate.getOutputRequester().processASSIGN(fetchedJobInfo, assigneeMaGate);
////							assigneeMaGate.getModuleController().processCommunityJobArrive(fetchedJobInfo);
//						}
						
					} else {
						
						// NO remote/local candidate nodes can be discovered for such job
						// Fail to Assign job according to REQUEST message
						
						this.maGate.getModuleController().processJobUndelivered(fetchedJobInfo);
						JobCenterManager.jobSuspended(fetchedJobInfo.getGlobalJobID(), this.maGateIdentity, fetchedJobInfo);
						GlobalStorage.count_unmatchedREQUEST.incrementAndGet();
						
					}
					
					// revoke all (selected/unselected) nodes' accept messages
					this.revokeAcceptanceDecisions(candidateMaGates, fetchedJobInfo.getGlobalJobID());
					
				}
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);	
		}
		
	}
	
	
	/**
	 * Ask MatchMaker for jobs to re-schedule, and contact remote nodes by means of CASP INFORM
	 * INFORM Process won't rely on direct ResourceDiscovery Service, 
	 * instead, it contacts either KNOWN NEIGHBORS or KNOWN CRITICAL FRIENDS for disseminating the INFORM messages
	 */
	public void processINFORM() {
		
		try {
		
			GlobalStorage.count_caspController.incrementAndGet();
			
//			long realTimeSlot  = System.currentTimeMillis() - this.previousRealClock;
//			double simTimeSlot = MaGateMediator.getSystemTime() - this.previousSimClock;
//			
//			if((realTimeSlot < MaGateParam.INFORM_realTime_frequency) 
//					|| (simTimeSlot < MaGateParam.INFORM_simTime_frequency)) {
//				// To avoid too frequent INFORM
//				return;
//			} else {
//				this.previousRealClock = System.currentTimeMillis();
//				this.previousSimClock = MaGateMediator.getSystemTime();
//			}
			
			
			Vector<String> toRescheduleJobs = this.maGate.getMatchMaker().searchJobsForRescheduling();
			
			if(toRescheduleJobs == null) {
				return;
			}
			
//			double reschedulingCounter = 0;
//			double reschedulingLimit   = ((double) 1 / MaGateParam.systemReschedulingCoefficient) * this.maGate.getMatchMaker().sizeOfLocalJobQueue(); 
			
			for(String toRescheduleJobId : toRescheduleJobs) {
				
//				// make sure to try a certain proportion of the to-reschedule jobs
//				if(reschedulingCounter >= reschedulingLimit) {
//					break;
//				}
//				reschedulingCounter++;
				
				// fetch job details
				JobInfo jobInfo = JobCenterManager.getJobInfobyJobId(toRescheduleJobId);
				
				if(jobInfo == null) {
					continue;
				}
				
				// Get known neighboring node list
				String[] discoveredRemoteMaGateArray = this.maGate.getCommunityMonitor().discoverRemoteNodes();
				
				// Generate a INFORM message
				Vector<CandidateNode> candidateMaGates = new Vector<CandidateNode>();
				
				double estTimeForJob = jobInfo.getEstimatedComputationTime();
				double estTimeForLoadOfLocalJobQueue = this.maGate.getMatchMaker().estimatedTimeToExecuteLocalJobQueue(jobInfo.getGlobalJobID());
//				double estTimeForLoadOfShadowJobQueue = this.maGate.getMatchMaker().estimatedTimeToExecuteShadowJobQueue();
				
				// create CASA request message
				MsgInform msgInform = new MsgInform(this.maGateIdentity, jobInfo.getGlobalJobID(), jobInfo.getNumPE(), 
						jobInfo.getJob().getOSRequired(), (estTimeForJob + estTimeForLoadOfLocalJobQueue), 
						this.maGate.getMatchMaker().getAvgQueuingTime(), discoveredRemoteMaGateArray.length);
				
				/************************************************************
				 * Sends INFORM messages to the candidate MaGates, and waits for ACCEPT messages
				 ************************************************************/
				
				for(String targetedMaGateId : discoveredRemoteMaGateArray) {
					
					MaGateEntity targetedMaGate = GlobalStorage.findMaGateById(targetedMaGateId);
					
					if(targetedMaGate == null) {
						continue;
					} 
					
					GlobalStorage.count_casp_inform_Controller.incrementAndGet();
					
					// ask for ACCEPT from the contacted remote node
					// but the remote node is not supposed to take the job at this step
					MsgAccept msgAccept = this.maGate.getOutputRequester().processINFORM(msgInform, targetedMaGate);
//					String returnedNodeId = targetedMaGate.getCASPController().processACCEPT(jobInfo, this.maGateIdentity, CASPMessage.CASP_MSG_INFORM);
					
					// Counter
					GlobalStorage.count_msgINFORM.incrementAndGet();
					
					if(msgAccept != null && msgAccept.isAccepted && msgAccept.responderNodeId != null && !msgAccept.responderNodeId.equals("")) {
						if(!msgAccept.responderNodeId.equals(this.maGateIdentity)) {
							
							// Make the calculation based on the received "ACCEPT" messages, and decide an ordered candidate list.
							// Try to delegate the job to the first candidate of aforementioned list, otherwise the second one, and so on.
							candidateMaGates.add(new CandidateNode(GlobalStorage.findMaGateById(msgAccept.responderNodeId), 
									msgAccept.estTimeForJob, msgAccept.estTimeForLoadOfLocalJobQueue, msgAccept.estTimeForLoadOfShadowJobQueue));
						}
						
					}
					
				}
				
				/************************************************************
				 * Send the job to a selected Assignee according to received ACCEPT messages
				 ************************************************************/
				
				MaGateEntity assigneeMaGate = this.selectCandidateRemoteNode(candidateMaGates, CASAMessage.CASP_MSG_ASSIGN_FOR_INFORM);
				
//				log.info("discoveredRemoteMaGateArray.size(): " + discoveredRemoteMaGateArray.length + 
//						  "; candidateMaGates.size(): " + candidateMaGates.size() + 
//						  "; assigneeMaGate: " + assigneeMaGate);
				
				if(assigneeMaGate != null && this.maGate.getMatchMaker().jobReschedule(jobInfo.getGlobalJobID())) {
					
//					JobInfo fetchedJobInfo = jobInfo;
					
					if(jobInfo != null) {
						
						// Counter 
						GlobalStorage.count_msgASSIGN_INFORM.incrementAndGet();
						
//						// record the remotedNode Id which helps the job execution
//						this.maGate.getCFCController().cacheCFCNeighbors(assigneeMaGate.getMaGateIdentity());
						
						// deliver the job the Assignee as either a local job or a community job
						this.maGate.getOutputRequester().processASSIGN(jobInfo, assigneeMaGate);
//						if(jobInfo.getOriginalMaGateId().equals(assigneeMaGate.getMaGateIdentity())) {
//							
//							// local job identified
//							assigneeMaGate.getModuleController().processJobArrival(jobInfo);
//							
//						} else {
//							// community job identified
//							this.maGate.getOutputRequester().processASSIGN(jobInfo, assigneeMaGate);
////							assigneeMaGate.getModuleController().processCommunityJobArrive(fetchedJobInfo);
//							
//						}
					} else {
						// Fail to Assign job according to INFORM message
						log.error("Unexpected job re-scheduling error\n********\n********\n");
						GlobalStorage.count_unmatchedINFORM.incrementAndGet();
						GlobalStorage.count_errorCASP.incrementAndGet();
					}
						
				} else {
					// Target Assignee node for re-scheduling (INFORM) is NOT anymore for this JobItem
					// Don't retrieve corresponding JobInfo from local node's processingQueue
					
					// Counter
					GlobalStorage.count_unmatchedINFORM.incrementAndGet();
					   
				}
				
				// revoke all (selected/unselected) nodes' accept messages
				this.revokeAcceptanceDecisions(candidateMaGates, jobInfo.getGlobalJobID());
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);	
		}
		
	}
	
	
	/**
	 * Process incoming REQUEST messages (jobInfo) and prepare to-output ACCEPT messages
	 * 
	 * @param incomingJobInfo
	 * @return True if the JobInfo requirement on OS_Type and Num_of_PE can be satisfied 
	 */
	public MsgAccept processACCEPT(MsgRequest msgRequest, String requesterNodeId, int caspMsg) {
		
		return processACCEPT(msgRequest.typeOfOS, msgRequest.numOfPE, msgRequest.jobId, 
				msgRequest.replicsOfMsgRequest, requesterNodeId, caspMsg, -1, -1);
		
	}
	
	/**
	 * Process incoming INFORM messages (jobInfo) and prepare to-output ACCEPT messages
	 * 
	 * @param incomingJobInfo
	 * @return True if the JobInfo requirement on OS_Type and Num_of_PE can be satisfied 
	 */
	public MsgAccept processACCEPT(MsgInform msgInform, String requesterNodeId, int caspMsg) {
		
		return processACCEPT(msgInform.typeOfOS, msgInform.numOfPE, msgInform.jobId, 
				msgInform.replicsOfMsgRequest, requesterNodeId, caspMsg, msgInform.estResponseTime, msgInform.avgQueuingTime);
		
	}
	
	
	/**
	 * Process incoming REQUEST messages (agreement) and prepare to-output ACCEPT messages
	 * 
	 * @param jobOS
	 * @param jobNumPE
	 * @param incomingNodeId
	 * @param caspMsg
	 * @return
	 */
	public MsgAccept processACCEPT(String jobOS, int jobNumPE, String jobId, double replicsOfMsgRequest, 
			String requesterNodeId, int caspMsg, double estRemoteResponseTime, double avgRemoteQueuingTime) {
		
		MsgAccept msgAccept = null;
		
		try {
			
			GlobalStorage.count_caspController.incrementAndGet();
			
			boolean approvedBasic = false;
			boolean approvedAdvanced = false;
			
			/********************************************************************
			 *  CASP behavior: check whether the hardware profile can match job profile
			 ********************************************************************/
			if(this.maGate.getMatchMaker().check_jobMatchResource(jobOS, jobNumPE)) {
				
				// BASIC for all CASP Policies
				approvedBasic = true;
				
			} else {
				approvedBasic = false;
				
			}
			
			double estTimeForJob = JobCenterManager.getJobInfobyJobId(jobId).getEstimatedComputationTime();
			double estTimeForLoadOfLocalJobQueue = this.maGate.getMatchMaker().estimatedTimeToExecuteLocalJobQueue();
			double estTimeForLoadOfShadowJobQueue = this.maGate.getMatchMaker().estimatedTimeToExecuteShadowJobQueue();
			
			double estLocalResponseTime = estTimeForJob + estTimeForLoadOfLocalJobQueue + estTimeForLoadOfShadowJobQueue;
			
			
			// for INFORM messages, additional judgment needs to be given
			if(caspMsg == CASAMessage.CASP_MSG_REQUEST) {
				
				// Accept the incoming REQUEST message
				approvedAdvanced = true;
				
			} else if (caspMsg == CASAMessage.CASP_MSG_INFORM) {
				
				this.maGate.getMatchMaker().updateQueuingTime();
				double avgLocalQueuingTime = this.maGate.getMatchMaker().getAvgQueuingTime();
				
				double weightedAvgLocalQueuingTime = 
					avgLocalQueuingTime * MaGateParam.systemReschedulingOnAvgQueuingTime;
				
				if((estLocalResponseTime < estRemoteResponseTime) && (weightedAvgLocalQueuingTime < avgRemoteQueuingTime)) {
					
					// Accept the incoming INFORM message
					approvedAdvanced = true;
					
					GlobalStorage.count_casp_accept_inf_Controller.incrementAndGet();
					
				} else {
					approvedAdvanced = false;
				}
				
			}
			
			
//			/********************************************************************
//			 * CFC behavior: check CFC policy for facilitating job delegation 
//			 ********************************************************************/
//			if((!approved) && (this.maGate.getMatchMaker().check_jobMatchResource(jobOS, jobNumPE))) {
//				toReturnNodeId = 
//					this.maGate.getCFCController().resourceDiscoveryOnCFC(incomingNodeId);
//				
//				if((toReturnNodeId != null) && (!toReturnNodeId.equals(""))) {
//					approved = true;
//				}
//			}
			
			
			/********************************************************************
			 * determine the final to-return node Id, and trigger the counter 
			 ********************************************************************/
			if(approvedBasic && approvedAdvanced) {
				
				if(caspMsg == CASAMessage.CASP_MSG_REQUEST) {
					GlobalStorage.count_msgACCEPT_REQUEST.incrementAndGet();
				} else if (caspMsg == CASAMessage.CASP_MSG_INFORM) {
					GlobalStorage.count_msgACCEPT_INFORM.incrementAndGet();
				}
				
				GlobalStorage.count_casp_accept_req_Controller.incrementAndGet();
				
				// Return the candidate node Id
				msgAccept = new MsgAccept(true, this.maGateIdentity, jobId, 
						estTimeForJob, estTimeForLoadOfLocalJobQueue, estTimeForLoadOfShadowJobQueue);
				
				// mark the acceptance decision in MatchMaker
				double acceptanceApproveProbability = 0;
				if(replicsOfMsgRequest != 0) {
					acceptanceApproveProbability = (double) 1 / replicsOfMsgRequest;
				}
				
				this.maGate.getMatchMaker().appendAcceptanceDecision(jobId, acceptanceApproveProbability);
				
			} else {
//				msgAccept = null;
				// if CFM is enabled, apply the CFM topology expansion sub-model for CF nodes
				msgAccept = this.maGate.getCFMController().
					cfmTopologyExpansion(jobOS, jobNumPE, jobId, replicsOfMsgRequest, 
					requesterNodeId, caspMsg, estRemoteResponseTime, avgRemoteQueuingTime);
				
				GlobalStorage.count_casp_accept_null_Controller.incrementAndGet();
				
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
			System.exit(1);
		}
		
		return msgAccept;
	}
	
	
	/**
	 * Select an Assignee according to received ACCEPT messages based on CASA-probabilistic determination  
	 * 
	 * @param candidateMaGates
	 * @return
	 */
	public MaGateEntity selectCandidateRemoteNode(Vector<CandidateNode> candidateMaGates, int caspMsg) {
		
		MaGateEntity assigneeNode = null;
		
		try {
			
			GlobalStorage.count_caspController.incrementAndGet();
			GlobalStorage.count_casp_selectnode_Controller.incrementAndGet();
			
			double totalReciprocalResponseTime = 0;
			
			for(CandidateNode candidateNode : candidateMaGates) {
				
				double currentNodeEstResponseTime = candidateNode.estTimeForJob + 
					candidateNode.estTimeForLoadOfLocalJobQueue + candidateNode.estTimeForLoadOfShadowJobQueue;
				double currentNodeReciprocalEstResponseTime = (double) 1 / currentNodeEstResponseTime;
				
				candidateNode.nodeResProbability = currentNodeReciprocalEstResponseTime;
				totalReciprocalResponseTime += currentNodeReciprocalEstResponseTime;
				
			}
			
			for(CandidateNode candidateNode : candidateMaGates) {
				candidateNode.nodeResProbability = candidateNode.nodeResProbability / totalReciprocalResponseTime;
			}
			
			// calculating assignee node based on resource capability and trust probabilities
			Random generator = new Random();
			double pro = generator.nextDouble();
			
			double currentProbability = 0;
			
			if(this.maGate.getCFMController().getCFM_POLICY().contains(CFMPolicy.CFM_NONE)) {
				
				// only evaluate via resource capability probabilities
				for(CandidateNode candidateNode : candidateMaGates) {
					
					currentProbability += candidateNode.nodeResProbability;
					if(pro < currentProbability) {
						// decide the assignee node 
						assigneeNode = candidateNode.node;
						
						break;
					}
				}
				
			} else if (this.maGate.getCFMController().getCFM_POLICY().contains(CFMPolicy.CFM_ReSe)) {
				
				// calculate each node's trust probabilities
				this.maGate.getCFMController().cfmResourceSelection(candidateMaGates);
				
				// evaluate via both resource capability and trust probabilities
				for(CandidateNode candidateNode : candidateMaGates) {
					
					currentProbability += candidateNode.nodeMergedProbability;
					
//					currentProbability += candidateNode.nodeResProbability * MaGateParam.cfmResCapabilityWeight + 
//						candidateNode.nodeTrustProbability * (1 - MaGateParam.cfmResCapabilityWeight);
					
					if(pro < currentProbability) {
						// decide the assignee node 
						assigneeNode = candidateNode.node;
						
//						// logging debug ********
//						
//						String outputText = "Hosting node: " + this.maGateIdentity + "\n";
//						for(CandidateNode tempNode : candidateMaGates) {
//							double resPro = tempNode.nodeResProbability;
//							double trtPro = tempNode.nodeTrustProbability;
//							double merPro = tempNode.nodeMergedProbability;
//							outputText += "[" + tempNode.node.getMaGateIdentity() + 
//								"(" + tempNode.node.getNodeStatus() + ") /" + 
//								resPro + ":" + trtPro + ":" + merPro + "] ";
//						}
//						outputText += "\nSelectedNode's [res|trust] probabilities: " + 
//							candidateNode.node.getMaGateIdentity() + 
//							"(" + candidateNode.node.getNodeStatus() + ") /" +
//							candidateNode.nodeResProbability + " : " + candidateNode.nodeTrustProbability + 
//							" : " + candidateNode.nodeMergedProbability;
//						log.info(outputText);
//						
//						// logging debug ********
						
						break;
					}
				}
				
				
			} else {
				
				// only evaluate via resource capability probabilities
				for(CandidateNode candidateNode : candidateMaGates) {
					
					currentProbability += candidateNode.nodeResProbability;
					if(pro < currentProbability) {
						// decide the assignee node 
						assigneeNode = candidateNode.node;
						break;
					}
				}
				
			}
			
			
			
//			for(CandidateNode candidateNode : candidateMaGates) {
//				
//				currentProbability += candidateNode.nodeSelectedProbability;
//				if(pro < currentProbability) {
//					// decide the assignee node 
//					assigneeNode = candidateNode.node;
//					break;
//				}
//			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);	
		}
		
		return assigneeNode;
	}
	
	
	public void revokeAcceptanceDecisions(Vector<CandidateNode> candidateMaGates, String jobId) {
		
		try {
			
			for(CandidateNode candidateNode : candidateMaGates) {
				
				MaGateEntity currentMaGate = candidateNode.node;
				
				if(currentMaGate == null) {
					continue;
				}
				
				currentMaGate.getMatchMaker().revokeAcceptanceDecision(jobId);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);	
		}
		
	}
	
	
//	/**
//	 * Select an Assignee according to received ACCEPT messages, according to the best MIPS speed
//	 * 
//	 * @param candidateMaGates
//	 * @return
//	 */
//	public MaGateEntity selectCandidateRemoteNode(Vector<MaGateEntity> candidateMaGates, int caspMsg) {
//		
//		GlobalStorage.count_caspController.incrementAndGet();
//		GlobalStorage.count_casp_selectnode_Controller.incrementAndGet();
//		
//		return this.maGate.getCommunityMonitor().selectGoodRemoteNode(candidateMaGates);
//		
//	}
	
}



