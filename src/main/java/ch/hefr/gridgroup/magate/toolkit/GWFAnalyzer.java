package ch.hefr.gridgroup.magate.toolkit;

import gridsim.GridSimRandom;

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.OperatingSystemTypeEnumeration;

import ch.hefr.gridgroup.magate.env.MaGateProfile;
import ch.hefr.gridgroup.magate.env.MaGateToolkit;
import ch.hefr.gridgroup.magate.model.Job;
import ch.hefr.gridgroup.magate.storage.GlobalStorage;

public class GWFAnalyzer {

	private static final Logger log = Logger.getLogger(GWFAnalyzer.class);
	
	private String targettedGWFFile;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
//		try {
//			Thread.sleep(20000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		GWFAnalyzer gwfAnalyzer = new GWFAnalyzer();
		
	}
	
	public GWFAnalyzer() {
		
		// TODO Auto-generated method stub
		log.info("GWF Analyzer started.");
		
		File gwfFile = MaGateToolkit.getWorkLoadTraceDir();
		
		if (gwfFile.isDirectory()) {
			
            File[] children = gwfFile.listFiles();
            
            if(children != null && children.length > 0) {
            	
            	for(int k = 0; k < children.length; k++) {
            		if(children[k].isFile() && !children[k].isHidden()) {
                    	this.targettedGWFFile = children[k].getName();
//                    	break;
                    	if(!this.targettedGWFFile.equals("")) {
                			testGWF();
                		}
                    	continue;
            		}
            	}
            }
        }
		
//		if(!this.targettedGWFFile.equals("")) {
//			testGWF();
//		}
		
		
//		log.info(gwfFile.toString());
//		log.info(gwfFile.getAbsolutePath());
		
	}
	
	private void processSingleGWF(File singleGWF) {
		
		BufferedReader reader = MaGateToolkit.readFile(singleGWF);
		log.info("file buffered...");
		
		try {
			// read one line at the time
			int currentLine = 1;
			while ( reader.ready() )
			{
			    System.out.println(reader.readLine());
			    currentLine++;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e.toString());
		}
		
		reader = null;
		
	}
	
	
	private void testGWF() {
		
		Statement stmt   = null;	
		Statement stmt2  = null;	
		ResultSet rs     = null;
		ResultSet rs2    = null;
		Connection conn  = null;
		
		////////////////
		/// Parameters for recording GWF characteristic
		////////////////
		HashMap<String, String> origSiteID = new HashMap<String, String>();
		HashMap<String, String> lastRunSiteID = new HashMap<String, String>();
		int jobExecutedRemotely = 0;
		
		int startSecond = -1;
		int endSecond   = 0;
		
		double totalJobLoad = 0;
		double totalResLoad = 0;
		
		double nProcCount = 0;
		double nProcSingleCount = 0;
		int maxNProc = 0;
		
		double lessThanTenCount = 0;
		double lessThanHundrendCount = 0;
		
		double reqNProcsCount = 0;
		double reqNProcsSingleCount = 0;
		int maxReqNProcs = 0;
		
		HashMap<String, String>  executableID = new HashMap<String, String>();
		HashMap<String, String>  queueID = new HashMap<String, String>();
		
		int successJob = 0;
		int cancelledJob = 0;
		int failedJob = 0;
		
		int reqTimeCount = 0;
		int runTimeCount = 0;
		
		int originalTimeStart = -1;
		
		double osLinux = 0.0;
		double osSolaris = 0.0;
		double osUnix = 0.0;
		double osWindows = 0.0;
		double osBSD = 0.0;
		
		int numOfArrivedJob = 0;
		int onePercentageLoadNumOfArrivedJob = 0;
		int tenPercentageLoadNumOfArrivedJob = 0;
		
		Job nowJob = null;
		ConcurrentLinkedQueue<Job> gridJobQueue = new ConcurrentLinkedQueue<Job>();
		System.out.println("Targetted GWF File: " + this.targettedGWFFile);
		
		// Start test and statistic calculation
		try {
			
			Class.forName("org.sqlite.JDBC");
//			conn = DriverManager.getConnection("jdbc:sqlite:magateinput/workloadtrace/GWA-Grid5000.db3");
			String sqlConnection = "jdbc:sqlite:magateinput/workloadtrace/" + this.targettedGWFFile;
			conn = DriverManager.getConnection(sqlConnection);
			
			// Count total number of jobs
			String countRecord = "select count(*) as counter from Jobs;";
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery(countRecord);
			
			double totalJob = 0;
			while (rs.next()) {
				totalJob = rs.getInt("counter");
				System.out.println("Total Job count " + totalJob);
			}
			
			stmt.close();
			rs.close();
			stmt = null;
			rs   = null;
			
			double counter = totalJob / 10000;
			
			// LOOP all jobs
			for(int i = 0; i <= counter; i++) {
				
				int j = i * 10000;
				int k = (i+1)  * 10000;
				
				// Fetch 10000 archived jobs
				String selectRecord = "select * from Jobs where JobID >= " +
					j + " and JobID < " + k + ";";
				
				stmt2 = conn.createStatement();
				rs2 = stmt2.executeQuery(selectRecord);
				
				int jobID = 0;
				double reqTime = 0.0;
				int submitTime = 0;
				int reqNProcs = 0;
				
				// LOOP the fetched jobs 
				while (rs2.next()) {
					
					numOfArrivedJob++;
					if(numOfArrivedJob % 100 == 0) {
						onePercentageLoadNumOfArrivedJob++;
					}
					
					if(numOfArrivedJob % 10 == 0) {
						tenPercentageLoadNumOfArrivedJob++;
					}
					
					jobID = rs2.getInt("JobID");
					reqTime = rs2.getDouble("ReqTime");
					submitTime = rs2.getInt("SubmitTime");
					reqNProcs = rs2.getInt("ReqNProcs");
					
					// get the beginning time (in seconds) and end time of the workload trace
					if(startSecond < 0) {
						startSecond = submitTime;
					} else if(startSecond > submitTime) {
						startSecond = submitTime;
					}
					
					if(endSecond < submitTime) {
						endSecond = submitTime;
					} 
					
					//
					
					if(originalTimeStart < 0) {
						originalTimeStart = submitTime;
						submitTime = 0;
					} else {
						submitTime -= originalTimeStart;
					}
					
					
					// Print a mark after each 100 jobs
					if(jobID % 100000 == 0) {
						
						double submitDate = (double) submitTime / 86400.0;
						
						recordJobArrivalTime(submitTime, submitDate, numOfArrivedJob, onePercentageLoadNumOfArrivedJob, tenPercentageLoadNumOfArrivedJob);
						
//						System.out.println("System time: " + submitTime + " JobID: " + jobID + "  Original-SubmitTime: " + rs2.getInt("SubmitTime") + 
//								"  OrigSiteID: " + rs2.getString("OrigSiteID") + "  LastRunSiteID: " + rs2.getString("LastRunSiteID"));
						
					}
					
					/////////////////////////////
					// Generate a fake simulation job
					/////////////////////////////
					nowJob = createSimJobFromGWA(jobID, reqTime, submitTime, reqNProcs);
					gridJobQueue.offer(nowJob);
					
					if(nowJob.getOSRequired().equals("BSD")) {
						osBSD += 1;
					} else if(nowJob.getOSRequired().equals("WINDOWS")) {
						osWindows += 1;
					} else if (nowJob.getOSRequired().equals("UNIX")) {
						osUnix += 1;
					} else if (nowJob.getOSRequired().equals("SOLARIS")) {
						osSolaris += 1;
					} else if(nowJob.getOSRequired().equals("LINUX")) {
						osLinux += 1;
					} 
					
					/////////////////////////////
					// Job statistic process
					/////////////////////////////
					if(!origSiteID.containsKey(rs2.getString("origSiteID"))) {
						origSiteID.put(rs2.getString("origSiteID"), rs2.getString("origSiteID"));
					}
					
					if(!lastRunSiteID.containsKey(rs2.getString("lastRunSiteID"))) {
						lastRunSiteID.put(rs2.getString("lastRunSiteID"), rs2.getString("lastRunSiteID"));
					}
					
					if(this.targettedGWFFile.endsWith("Grid5000-db3")) {
						if(!rs2.getString("origSiteID").equals(rs2.getString("lastRunSiteID").substring(0, 8))) {
							jobExecutedRemotely++;
						}
					} else {
						if(!rs2.getString("origSiteID").equals(rs2.getString("lastRunSiteID"))) {
							jobExecutedRemotely++;
						}
					}
					
					int currNProc = rs2.getInt("NProc");
					if(currNProc > 0) {
						nProcCount++;
						
						if(currNProc < 2) {
							nProcSingleCount++;
						} 
						
						if(currNProc > maxNProc) {
							maxNProc = currNProc;
						}
						
						if(currNProc <= 10) {
							lessThanTenCount++;
						}
						
						if(currNProc <= 100) {
							lessThanHundrendCount++;
						}
					}
					
					int currReqNProc = rs2.getInt("reqNProcs");
					if(currReqNProc > 0) {
						reqNProcsCount++;
						
						if(currReqNProc < 2) {
							reqNProcsSingleCount++;
						}
						
						if(currReqNProc > maxReqNProcs) {
							maxReqNProcs = currReqNProc;
						}
					}
					
					if(rs2.getString("ExecutableID") != null && !rs2.getString("ExecutableID").equals("-1")) {
						if(!executableID.containsKey(rs2.getString("ExecutableID"))) {
							executableID.put(rs2.getString("ExecutableID"), rs2.getString("ExecutableID"));
						}
					}
					
					if(rs2.getString("QueueID") != null && !rs2.getString("QueueID").equals("-1")) {
						if(!queueID.containsKey(rs2.getString("QueueID"))) {
							queueID.put(rs2.getString("QueueID"), rs2.getString("QueueID"));
						}
					}
					
					int jobStatus = rs2.getInt("Status");
//					if(jobStatus == 1) {
					if(jobStatus == 1 || jobStatus == -1) {
						successJob++;
					} else if (jobStatus == 0) {
						failedJob++;
					} else if (jobStatus == 5) {
						cancelledJob++;
					}
					
					
					if(rs2.getInt("RunTime") != -1) {
						runTimeCount++;
					}
					
					if(rs2.getDouble("ReqTime") != -1.0) {
						reqTimeCount++;
					}
					
					// sum total job load
					if(jobStatus == 1 || jobStatus == -1) {
						if((rs2.getInt("RunTime") > 0) && (rs2.getInt("NProc") > 0)) {
							totalJobLoad += rs2.getInt("RunTime") * rs2.getInt("NProc");
						}
					}
				}
			}
			
			// running time
			double runningSecond = endSecond - startSecond;
			double runningHours = runningSecond / 3600;
			double runningDays = runningHours / 24;
			double jobsperhour = totalJob / runningHours;
			
			// Print statistic records
			
			System.out.println("\n\n--------------------------------------");
			System.out.println("Targetted GWF File: " + this.targettedGWFFile);
			
			System.out.println("Total Job: " + totalJob);
			System.out.println("Total Job Load: " + totalJobLoad);
			System.out.println("Total GWF Duration: " + runningHours + " hours; (" + runningDays + " days); " + jobsperhour + " jobs per hour.");
			System.out.println("Size of GridJobQueue: " + gridJobQueue.size());
			
			System.out.println("BSD: " + (osBSD / totalJob) + 
					"; WINDOWS: " + (osWindows / totalJob) + 
					"; UNIX: " + (osUnix / totalJob) + 
					"; SOLARIS: " + (osSolaris / totalJob) + 
					"; LINUX: " + (osLinux / totalJob));
			
			System.out.println("Number of [OrigSite]: " + origSiteID.size() + 
					"; Number of [LastRunSite]: " + lastRunSiteID.size());
			
			double rateJobExecutedRemotely = (double) jobExecutedRemotely / totalJob;
			System.out.println("Number of job executed on a DIFFERENT node: " + jobExecutedRemotely + 
					"; rate: " + rateJobExecutedRemotely);
			
			System.out.println("Jobs with known RunTime: " + runTimeCount + "; Rate: " + (runTimeCount / totalJob) + 
					"; Jobs with known ReqTime: " + reqTimeCount + "; Rate: " + (reqTimeCount / totalJob));
			
			System.out.println("Jobs with specific REQUESTED Processors: " + reqNProcsCount + "; rate: " +  (reqNProcsCount / totalJob) + 
					"; requested Single-Processors: " + reqNProcsSingleCount + "; rate: " + (reqNProcsSingleCount / totalJob));
			
			System.out.println("Jobs with specific USED Processors: " + nProcCount + "; rate: " + (nProcCount / totalJob) + 
					"; used Single-Processors: " + nProcSingleCount + "; rate: " + (nProcSingleCount / totalJob));
			
			System.out.println("MaxNProc: " + maxNProc + "; MaxReqNProcs: " + maxReqNProcs);
			
			System.out.println("Number of [ExecutableID]: " + executableID.size() + 
					"; Number of [QueueID]: " + queueID.size());
			
			System.out.println("Successful jobs : " + successJob + "; rate: " + (successJob / totalJob) + 
					"; failed jobs: " + failedJob + "; rate: " + (failedJob / totalJob) + 
					"; cancelled jobs: " + cancelledJob + "; rate: " + (cancelledJob / totalJob));
			
			System.out.println("job with <=10 PE requirement: " + lessThanTenCount + "; rate: " + (lessThanTenCount / totalJob) +
					"; job with <=100 PE requirement: " + lessThanHundrendCount + "; rate: " + (lessThanHundrendCount / totalJob) );
			
			stmt2.close();
			rs2.close();
			stmt2 = null;
			rs2 = null;
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			
		} finally {
			
			try {
				stmt  = null;
				stmt2 = null;
				rs    = null;
				rs2   = null;
				conn.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private Job createSimJobFromGWA(int jobId, double localEstimatedSec, int arrivalTime, int numOfCPU) {
		
		Random randomEngine = new Random();
		
		long singleInputSize  = 100;
		long singleOutputSize = 100;
		
		// random job i/o size
//		singleInputSize  = (long) GridSimRandom.real(MaGateParam.jobInputSize, 
//				MaGateParam.minRange, MaGateParam.maxRange, randomEngine.nextDouble()); 
//		singleOutputSize = (long) GridSimRandom.real(MaGateParam.jobOutputSize, 
//				MaGateParam.minRange, MaGateParam.maxRange, randomEngine.nextDouble()); 
        
        // MIPS rating of a machine used to compute estimated comp. length 
        double localEstimatedMachine = MaGateProfile.estimatedMIPS;
        
        // random job size & individual MIPS 
		// estimated computational length
		// it stands for how much time a cpu (with "estimatedMachine' MIPS") may use   
        
        // job size 
        double jobSize = Math.round(localEstimatedSec * localEstimatedMachine);
        
        // other job characters
        
        int deadLine    = 0;
        int priority    = 1;
        double cpuDistributionRate  = 0.0;
        
        // job deadline
        deadLine = (int) (arrivalTime + localEstimatedSec * 5);
//		deadLine = (int) GridSimRandom.real((localEstimatedSec * 5), MaGateParam.minRange, 
//				MaGateParam.maxRange, randomEngine.nextDouble()); 
			
		cpuDistributionRate = randomEngine.nextDouble();
		
		String osType = "";
		if(cpuDistributionRate < 0.002) {
			osType = "BSD";
//			osType = OperatingSystemTypeEnumeration.BSDUNIX.toString();
				
		} else if((cpuDistributionRate >= 0.002) && (cpuDistributionRate < 0.012)) {
			osType = "WINDOWS";
//			osType = OperatingSystemTypeEnumeration.WINNT.toString();
				
		} else if((cpuDistributionRate >= 0.012) && (cpuDistributionRate < 0.056)) {
			osType = "UNIX";
			
		} else if((cpuDistributionRate >= 0.056) && (cpuDistributionRate < 0.114)) {
			osType = "SOLARIS";
			
		} else {
			osType = "LINUX";
			
		}
			
		Job gl = new Job(jobId, jobSize, jobSize, singleInputSize, 
				singleOutputSize, osType, "NONE", 
				arrivalTime, deadLine, priority, numOfCPU, 
				localEstimatedSec, localEstimatedMachine);
			
			return gl;
			
	}
	
	
	private void recordJobArrivalTime(int submitTime, double submitDate, int totalSubmittedJob, int oneLoadJob, int tenLoadJob) {
		
		String contentText = submitTime + ", " + submitDate + ", " + totalSubmittedJob + ", " + oneLoadJob + ", " + tenLoadJob;
		
		MaGateToolkit.writeResult("gwa-job-arrival.csv", contentText);
		
	}
	
	private void writeResult(String file, String value) {
		
		String outputFile = MaGateProfile.outputLocation() + MaGateProfile.resultLocation() + file;
		PrintWriter pw = null;
		
		try {
			pw = new PrintWriter(new FileWriter(outputFile, true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			pw.println(value);
	        pw.close();
		}
        
	}

}
