package ch.hefr.gridgroup.magate.env;

import gridsim.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.input.ExpNode;
import ch.hefr.gridgroup.magate.km.ModuleController;
import ch.hefr.gridgroup.magate.model.JobInfo;
import ch.hefr.gridgroup.magate.model.Job;

/**
 * Creating a SimJob comprised of job length, I/O length, etc.
 * 
 * @author Ye HUANG
 */
@SuppressWarnings("unused")
public class SimJobFactory {
	
	private static Log log = LogFactory.getLog(SimJobFactory.class);
	
	private static int JOB_NUM            = 0;      // job number
	private static int SUBMIT_TIME        = 1;      // submit time of a job
	private static int WAIT_TIME          = 2;
	private static int RUN_TIME           = 3;      // running time of a job
	private static int NUM_PROC           = 4;      // number of processors needed for a job
	private static int AVG_CPU_TIME       = 5;
	private static int AVG_MEMORY         = 6;
	private static int REQ_NUM_PROC       = 7;      // required number of processors
	private static int REQ_RUN_TIME       = 8;      // required running time
	private static int REQ_MEMORY         = 9;
	private static int JOB_STATUS         = 10;
	private static int USER_ID            = 11;
	private static int GROUP_ID           = 12;
	private static int EXE_APP_NUM        = 13;
	private static int QUEUE_NUM          = 14;
	private static int PARTITION_NUM      = 15;
	private static int PRE_JOB_NUM        = 16;
	private static int TIME_FROM_PRE_JOB  = 17;
	
	private static int MAX_FIELD          = 18;     // max number of field in the trace file
	private static String COMMENT         = ";";    // a string that denotes the start of a comment
	private static final int IRRELEVANT   = -1;     // irrelevant number
	private static final int INTERVAL     = 10;     // number of intervals

	// ---
	private static int GlobalJobId = 0; 
	
	public static void appendJobListToUser(GridletList jobList, int userId) {
		
		Gridlet singleJob = null;
		
		for (int i = 0; i < jobList.size(); i++) {
			singleJob = (Gridlet) jobList.get(i);
			singleJob.setUserID(userId);
		}
	}
	
	/**
	 * Generate SimJob List, job distinguished by badRate(Number of PE) and type of OS
	 * (Recommended)
	 * @param numOfJobs
	 * @param userId
	 * @return
	 */
	public static ConcurrentLinkedQueue<Job> createSimJobListWithoutUser(int numOfJobs,  
			String archType, String osType, double badRate){
		
		int jobId = 0;
		ConcurrentLinkedQueue<Job> gridJobQueue = new ConcurrentLinkedQueue<Job>();
		
		Random randomEngine = new Random();
		
		long singleInputSize  = 1000;
		long singleOutputSize = 1000;
		
		// estimated computational length
		// it stands for how much time a cpu (with "estimatedMachine' MIPS") may use 
        double localEstimatedSec = 0;
        
        // MIPS rating of a machine used to compute estimated comp. length 
        double localEstimatedMachine = 0;
        
        // job size 
        double jobSize = MaGateProfile.jobSize;
        
        // other job characters
        int arrivalTime = 100;
        int deadLine    = 0;
        int numOfCPU    = 1;
        int priority    = 1;
        double badHappens  = 0.0;
        
		for (int i = 0; i < numOfJobs; i ++){
			
			// random job i/o size
			singleInputSize  = (long) GridSimRandom.real(MaGateProfile.jobInputSize, 
					MaGateProfile.job_minRange, MaGateProfile.job_maxRange, randomEngine.nextDouble()); 
			singleOutputSize = (long) GridSimRandom.real(MaGateProfile.jobOutputSize, 
					MaGateProfile.job_minRange, MaGateProfile.job_maxRange, randomEngine.nextDouble()); 
			
			// random job size & individual MIPS 
			localEstimatedSec = (long) GridSimRandom.real(MaGateProfile.estimatedSec, 
					MaGateProfile.job_minRange, MaGateProfile.job_maxRange, randomEngine.nextDouble());
			
			localEstimatedMachine = (long) GridSimRandom.real(MaGateProfile.estimatedMIPS, 
					MaGateProfile.job_minRange, MaGateProfile.job_maxRange, randomEngine.nextDouble()); 
		
			// random job size
			jobSize = Math.round(localEstimatedSec * localEstimatedMachine);
			
			// job arrival time 
			arrivalTime = (int) GridSimRandom.real(MaGateProfile.jobArrival, 0.999, 
					0.0, randomEngine.nextDouble());
			arrivalTime += + MaGateProfile.jobArrivalDelay;

			// job deadline
			deadLine = (int) GridSimRandom.real((localEstimatedSec * 5), MaGateProfile.job_minRange, 
					MaGateProfile.job_maxRange, randomEngine.nextDouble()); 
			
			badHappens = randomEngine.nextDouble();
			
			// job numOfCPU
			if(badHappens < badRate) {
				// bad job
				numOfCPU = (int) GridSimRandom.real(MaGateProfile.numOfPE_requestedByJob, 0.5, 
						0.0, randomEngine.nextDouble()); 
				
			} else {
				// good guy
				numOfCPU = (int) GridSimRandom.real(MaGateProfile.numOfPE_requestedByJob, 
						MaGateProfile.job_minRange, MaGateProfile.job_maxRange, randomEngine.nextDouble()); 
			}
			
			Job gl = new Job(jobId++, jobSize, jobSize, singleInputSize, 
					singleOutputSize, osType, archType, 
					arrivalTime, deadLine, priority, numOfCPU, 
					localEstimatedSec, localEstimatedMachine);
			
			gridJobQueue.offer(gl);
					
		}
		return gridJobQueue;
	}
	
	
	public static int createGWAbasedJobDataset(double loadinDecimal, Vector<ExpNode> inputNodeList, 
			String gwaURL, int numOfInputNode, int maxNumPE) {
		
		System.out.println("\nPreparing job input from GWA file...");
		int test = 0;
		
		Statement stmtCounter   = null;	
		Statement stmtData  = null;	
		ResultSet rsCounter     = null;
		ResultSet rsData    = null;
		Connection conn  = null;
		int maxInputNodeLimit = inputNodeList.size();
		int nodeIndex = 0;
		
		double initialJobSubmitTime = -1;
		
//		Random randomEngine = new Random();
		int createdJob = 0;
		Job nowJob = null;
		Vector<Job> jobList = new Vector<Job>();
		
		// Start test and statistic calculation
		try {
			
			Class.forName("org.sqlite.JDBC");
//			conn = DriverManager.getConnection("jdbc:sqlite:magateinput/workloadtrace/GWA-Grid5000.db3");
			conn = DriverManager.getConnection(gwaURL);
			
			// Count total number of jobs
			String counterRecord = "select count(*) as counter from Jobs;";
			
			stmtCounter = conn.createStatement();
			rsCounter = stmtCounter.executeQuery(counterRecord);
			
			double totalJob = 0;
			while (rsCounter.next()) {
				totalJob = rsCounter.getInt("counter");
				System.out.print("Amount of job of GWA " + MaGateParam.gwaLoadName + ": " + totalJob);
			}
			
			stmtCounter.close();
			rsCounter.close();
			stmtCounter = null;
			rsCounter   = null;
			
			double counter = totalJob / 10000;
			int jobIdex = 0;
			int jobToLoad = (int) (totalJob * loadinDecimal);
			int jobMultipler = (int) (totalJob / jobToLoad);
			System.out.println("; Loaded Job count: " + jobToLoad);
			
			// LOOP all jobs
			for(int i = 0; i <= counter; i++) {
				
				int j = i * 10000;
				int k = (i+1)  * 10000;
				
				// Fetch 10000 archived jobs
				String dataRecord = "select * from Jobs where JobID >= " +
					j + " and JobID < " + k + ";";
				
				stmtData = conn.createStatement();
				rsData = stmtData.executeQuery(dataRecord);
				
				int jobID      = 0;
				double reqTime = 0.0;
				int submitTime = 0;
				int reqNProcs  = 0;
				int submitTimewithLifeDuration = 0;
				
				
				// LOOP the fetched jobs 
				while (rsData.next()) {
					
					jobIdex++;
//					double toInputJob = randomEngine.nextDouble();
					
//					if(toInputJob < load) {
					if((jobIdex % jobMultipler) == 0) {
						
						jobID = rsData.getInt("JobID");
						submitTime = rsData.getInt("SubmitTime");
						
						if(rsData.getDouble("NProc") > 0) {
							reqNProcs = rsData.getInt("NProc");
						} else if (rsData.getDouble("ReqNProcs") > 0) {
							reqNProcs = rsData.getInt("ReqNProcs");
						} else {
							reqNProcs = 1;
						}
						
						// don't creat a job larger than maxNum of all resources
						// BUT IF SO, the outstanding performance out of the comparision between 
						// FCFS and EASY will not appear
//						if(reqNProcs >= (maxNumPE)) {
//							test++;
////							log.debug("*********\n*********\n*********: [" + test + 
////									"] reqNProcs: " + reqNProcs + "; maxNumPE: " + maxNumPE);
//							continue;
//						}
						
						if(rsData.getDouble("RunTime") > 0) {
							reqTime = Math.max(0, rsData.getDouble("RunTime"));
						} else {
							reqTime = Math.max(0, rsData.getDouble("ReqTime"));
						}
						
						submitTimewithLifeDuration = arrangeJobArrivalTime(submitTime, loadinDecimal);
						
						int currentIndex = nodeIndex % maxInputNodeLimit;
						nodeIndex++;
						
						/////////////////////////////
						// Generate a fake simulation job
						/////////////////////////////
						
						createdJob++;
						
						nowJob = createSimJobFromGWA(jobID, reqTime, submitTimewithLifeDuration, reqNProcs, 
								MaGateParam.fetchFairDistributedOS(), MaGateProfile.job_archType);
						
						jobList.add(nowJob);
						inputNodeList.get(currentIndex).addJob(nowJob);
						
					}
					
				}
			}
			
			// apply OS setting
			MaGateParam.applyTOP500JobOSDistribution(jobList);
			
			// Print statistic records
			
			stmtData.close();
			rsData.close();
			stmtData = null;
			rsData = null;
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			
		} finally {
			
			try {
				stmtCounter  = null;
				stmtData = null;
				rsCounter    = null;
				rsData   = null;
				conn.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("Job input from GWA file prepared! In total " + createdJob + " jobs are generated.");
		
		return createdJob;
		
	}
	
	private static int arrangeJobArrivalTime(int originalArrivalTime, double loadinDecimal) {
		
//		int arrangedArrivalTime = 0;
		
		if(MaGateParam.systemStartTime < 0) {
			MaGateParam.systemStartTime = originalArrivalTime;
		}
		
		double distanceTime = originalArrivalTime - MaGateParam.systemStartTime;
		double processedDistanceTime = distanceTime * loadinDecimal;
//		arrangedArrivalTime = (int) (MaGateParam.systemStartTime + processedDistanceTime);
		
		return (int) processedDistanceTime;
//		return arrangedArrivalTime;
	}
	
	
	private static Job createSimJobFromGWA(int jobId, double localEstimatedSec, int arrivalTime, int numOfCPU, String osType, String archType) {
		
		Random randomEngine = new Random();
		
		long singleInputSize  = 1000;
		long singleOutputSize = 1000;
		double localEstimatedMachine = MaGateProfile.estimatedMIPS;
		
		// random job i/o size
//		singleInputSize  = (long) GridSimRandom.real(MaGateProfile.jobInputSize, 
//				MaGateProfile.job_minRange, MaGateProfile.job_maxRange, randomEngine.nextDouble()); 
//		singleOutputSize = (long) GridSimRandom.real(MaGateProfile.jobOutputSize, 
//				MaGateProfile.job_minRange, MaGateProfile.job_maxRange, randomEngine.nextDouble()); 
        
        // MIPS rating of a machine used to compute estimated comp. length 
        
//        localEstimatedMachine = (long) GridSimRandom.real(MaGateProfile.estimatedMIPS, 
//				MaGateProfile.job_minRange, MaGateProfile.job_maxRange, randomEngine.nextDouble()); 
        
        // random job size & individual MIPS 
		// estimated computational length
		// it stands for how much time a cpu (with "estimatedMachine' MIPS") may use   
        
//		localEstimatedSec = localEstimatedSec * 100;
//		localEstimatedMachine = localEstimatedMachine * 10;
		
//		log.debug(arrivalTime);
		
		
        // job size 
        double jobSize = Math.round(localEstimatedSec * localEstimatedMachine);
        
        // other job characters
        
        int deadLine    = 0;
        int priority    = 1;
        
        // job deadline
        deadLine = (int) (arrivalTime + localEstimatedSec * 5);
//		deadLine = (int) GridSimRandom.real((localEstimatedSec * 5), MaGateParam.minRange, 
//				MaGateParam.maxRange, randomEngine.nextDouble()); 
		
		Job gl = new Job(jobId, jobSize, jobSize, singleInputSize, 
				singleOutputSize, osType, archType, 
				arrivalTime, deadLine, priority, numOfCPU, 
				localEstimatedSec, localEstimatedMachine);
			
		return gl;
	}
	
	
	// ------------------------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------------------
	
	/**
	 * Generate SimJob List, job distinguished by min-max Number of request PE  
	 * @param numOfJobs
	 * @param userId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static ConcurrentLinkedQueue createSimJobListWithoutUser(int numOfJobs, int numOfRequestedPE, 
			String archType, String osType, double minRange, double maxRange){
		
		int jobId = 0;
		ConcurrentLinkedQueue gridJobQueue = new ConcurrentLinkedQueue();
		
		Random randomEngine = new Random();
//		GridSimStandardPE.setRating(MaGateParam.peMIPS);
		
		long singleInputSize  = 1000;
		long singleOutputSize = 1000;
		
		// estimated computational length
		// it stands for how much time a cpu (with "estimatedMachine' MIPS") may use 
        double localEstimatedSec = 0;
        
        // MIPS rating of a machine used to compute estimated comp. length 
        double localEstimatedMachine = 0;
        
        // job size 
        double jobSize = MaGateProfile.jobSize;
        
        // other job characters
        int arrivalTime = 100;
        int deadLine    = 0;
        int numOfCPU    = 1;
        int priority    = 1;
		
		for (int i = 0; i < numOfJobs; i ++){
			
			// random job i/o size
			singleInputSize  = (long) GridSimRandom.real(MaGateProfile.jobInputSize, 
					minRange, maxRange, randomEngine.nextDouble()); 
			singleOutputSize = (long) GridSimRandom.real(MaGateProfile.jobOutputSize, 
					minRange, maxRange, randomEngine.nextDouble()); 
			
			// random job size & individual MIPS 
			localEstimatedSec = (long) GridSimRandom.real(MaGateProfile.estimatedSec, 
					minRange, maxRange, randomEngine.nextDouble());
			
			localEstimatedMachine = (long) GridSimRandom.real(MaGateProfile.estimatedMIPS, 
					minRange, maxRange, randomEngine.nextDouble()); 
		
			// random job size
			jobSize = Math.round(localEstimatedSec * localEstimatedMachine);
			
			// job arrival time 
			arrivalTime = (int) GridSimRandom.real(MaGateProfile.jobArrival, 0.999, 
					0.0, randomEngine.nextDouble());
			arrivalTime += + MaGateProfile.jobArrivalDelay;

			// job deadline
			deadLine = (int) GridSimRandom.real((localEstimatedSec * 5), minRange, 
					maxRange, randomEngine.nextDouble()); 
			
			// job numOfCPU
			numOfCPU = (int) GridSimRandom.real(numOfRequestedPE, minRange, 
					maxRange, randomEngine.nextDouble()); 
			
			Job gl = new Job(jobId++, jobSize, jobSize, singleInputSize, 
					singleOutputSize, osType, archType, 
					arrivalTime, deadLine, priority, numOfCPU, 
					localEstimatedSec, localEstimatedMachine);
			
			gridJobQueue.offer(gl);
					
		}
		return gridJobQueue;
	}
	
	
	@SuppressWarnings("unchecked")
	public static ConcurrentLinkedQueue createWorkloadBasedSimJobListWithoutUser(String fileName, int peMIPS, String osType, String archType){
		
		BufferedReader reader = MaGateToolkit.readGZIPFile(fileName);
		ConcurrentLinkedQueue gridJobQueue = new ConcurrentLinkedQueue();
		
        try {
			// read one line at the time
			int currentLine = 1;
			while ( reader.ready() )
			{
			    parseWorkloadLineValue(reader.readLine(), currentLine, gridJobQueue, peMIPS, osType, archType);
			    currentLine++;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e.toString());
		}
		
		// close the bufferedReader after finishing the data processing
		MaGateToolkit.closeFile(reader);
		return gridJobQueue;
		
	}
	
    /**
     * Breaks a line of string into many fields.
     * @param currentLine  a line of string
     * @param lineNum   a line number
     * @pre line != null
     * @pre lineNum > 0
     * @post $none
     * @since GridSim
     */
    private static void parseWorkloadLineValue(String currentLine, int lineNum, ConcurrentLinkedQueue gridJobQueue, int peMIPS, String osType, String archType)
    {
        // skip a comment line
        if (currentLine.startsWith(COMMENT) == true) {
            return;
        }

        String[] fieldArray = new String[MAX_FIELD];
        String[] arrayOfSplitedLine = currentLine.split("\\s+");  // split the fields based on a space
        int i;              // a counter
        int len = 0;        // length of a string
        int index = 0;      // the index of an array

        // check for each field in the array
        for (i = 0; i < arrayOfSplitedLine.length; i++)
        {
        	try {
        	
            len = arrayOfSplitedLine[i].length();  // get the length of a string
            
            // if it is empty then ignore
            if (len == 0) {
                continue;
            }
            // if not, then put into the array
            else
            {
                fieldArray[index] = arrayOfSplitedLine[i];
                index++;
            }
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }

        if (index == MAX_FIELD) {
            generateSingleWorkloadJobToJobList(fieldArray, lineNum, gridJobQueue, peMIPS, osType, archType);
        }
    }

    /**
     * Generate a SimJob from extracted relevant information from a given array, and append it to jobList
     * @param workloadLineArray  an array of String
     * @param numOfLine   a line number
     * @pre array != null
     * @pre line > 0
     * @since GridSim
     */
    private static void generateSingleWorkloadJobToJobList(String[] workloadLineArray, int numOfLine, ConcurrentLinkedQueue gridJobQueue, int peMIPS, String osType, String archType)
    {
        try
        {
            Integer obj = null;

            // ye: get the job number
            int jobId = 0;
            obj = new Integer(workloadLineArray[JOB_NUM].trim());
            jobId = obj.intValue();

            // ye: get the submit time
            Long tempSubmit = new Long( workloadLineArray[SUBMIT_TIME].trim() );
            long submitTime = tempSubmit.intValue();
            // check the submit time
            if (submitTime < 0) {
                submitTime = 0;
            }

            // ye: get the run time
            obj = new Integer( workloadLineArray[REQ_RUN_TIME].trim() );
            int runTime = obj.intValue();
            // if the required run time field is ignored, then use
            // the actual run time
            if (runTime == IRRELEVANT) {
                obj = new Integer( workloadLineArray[RUN_TIME].trim() );
                runTime = obj.intValue();
            }
            // according to the SWF manual, runtime of 0 is possible due
            // to rounding down. E.g. runtime is 0.4 seconds -> runtime = 0
            if (runTime == 0) {
                runTime = 1;    // change to 1 second
            }

            // ye: get the number of allocated processors
            obj = new Integer( workloadLineArray[REQ_NUM_PROC].trim() );
            int numProc = obj.intValue();

            // if the required num of allocated processors field is ignored
            // or zero, then use the actual field
            if (numProc == IRRELEVANT || numProc == 0) {
                obj = new Integer( workloadLineArray[NUM_PROC].trim() );
                numProc = obj.intValue();
            }
            // finally, check if the num of PEs required is valid or not
            if (numProc <= 0) {
                numProc = 1;
            }
            
            double jobLength = runTime * peMIPS;
            long   deadline  = submitTime + runTime * 2;
            int priority     = 1;

            Job gl = new Job(jobId, jobLength, jobLength, MaGateProfile.jobInputSize, 
					MaGateProfile.jobOutputSize, osType, archType, 
					submitTime, deadline, priority, numProc, 
					runTime, peMIPS);
			
            gridJobQueue.offer(gl);
            
        }
        catch (Exception e)
        {
        	log.error("Exception in reading file at line #" + numOfLine);
            e.printStackTrace();
        }
    }

    /// --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
	///     Unused methods
    /// --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
    
	/**
	 * @param jobListSize
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static GridletList createJobListWithoutUser(int jobListSize){
		GridletList gridJobList = new GridletList();
		
		int jobId = 0;
		Random randomEngine = new Random();
		GridSimStandardPE.setRating(MaGateProfile.peMIPS);
		
		double singleJobSize  = MaGateProfile.jobSize;
		long singleInputSize  = MaGateProfile.jobInputSize;
		long singleOutputSize = MaGateProfile.jobOutputSize;
		
		for (int i = 0; i < jobListSize; i ++){
			singleJobSize    = GridSimStandardPE.toMIs(randomEngine.nextDouble() * MaGateProfile.jobSize);
			singleInputSize  = (long) GridSimRandom.real(MaGateProfile.jobInputSize, MaGateProfile.job_minRange, MaGateProfile.job_maxRange, randomEngine.nextDouble()); 
			singleOutputSize = (long) GridSimRandom.real(MaGateProfile.jobOutputSize, MaGateProfile.job_minRange, MaGateProfile.job_maxRange, randomEngine.nextDouble()); 
		
			Gridlet gridJob = new Gridlet(jobId++, singleJobSize, singleInputSize, singleOutputSize);
			
			gridJobList.add(gridJob);
		}
		
		System.out.println("job list created with size: " + gridJobList.size());
		return gridJobList;
	}
	
	/**
	 * @param jobListSize
	 * @param userId
	 * @param jobSizeParam
	 * @param inputSizeParam
	 * @param outputSizeParam
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static GridletList createJobListWithoutUser(int jobListSize, double jobSizeParam, 
			long inputSizeParam, long outputSizeParam){
		GridletList gridJobList = new GridletList();
		
		int jobId = 0;
		Random randomEngine = new Random();
		GridSimStandardPE.setRating(MaGateProfile.peMIPS);
		
		double singleJobSize  = jobSizeParam;
		long singleInputSize  = inputSizeParam;
		long singleOutputSize = outputSizeParam;
		
		for (int i = 0; i < jobListSize; i ++){
			singleJobSize    = GridSimStandardPE.toMIs(randomEngine.nextDouble() * jobSizeParam);
			singleInputSize  = (long) GridSimRandom.real(inputSizeParam, MaGateProfile.job_minRange, MaGateProfile.job_maxRange, randomEngine.nextDouble()); 
			singleOutputSize = (long) GridSimRandom.real(outputSizeParam, MaGateProfile.job_minRange, MaGateProfile.job_maxRange, randomEngine.nextDouble()); 
		
			Gridlet gridJob = new Gridlet(jobId++, singleJobSize, singleInputSize, singleOutputSize);
			
			gridJobList.add(gridJob);
		}
		
		return gridJobList;
	}
	
	/**
	 * @param jobListSize
	 * @param userId
	 * @param jobSizeParam
	 * @param inputSizeParam
	 * @param outputSizeParam
	 * @param minRangeParam
	 * @param maxRangeParam
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static GridletList createJobListWithoutUser(int jobListSize, double jobSizeParam, 
			long inputSizeParam, long outputSizeParam, double minRangeParam, double maxRangeParam){
		GridletList gridJobList = new GridletList();
		
		int jobId = 0;
		Random randomEngine = new Random();
		GridSimStandardPE.setRating(MaGateProfile.peMIPS);
		
		double singleJobSize  = jobSizeParam;
		long singleInputSize  = inputSizeParam;
		long singleOutputSize = outputSizeParam;
		
		for (int i = 0; i < jobListSize; i ++){
			singleJobSize    = GridSimStandardPE.toMIs(randomEngine.nextDouble() * jobSizeParam);
			singleInputSize  = (long) GridSimRandom.real(inputSizeParam, minRangeParam, maxRangeParam, randomEngine.nextDouble()); 
			singleOutputSize = (long) GridSimRandom.real(outputSizeParam, minRangeParam, maxRangeParam, randomEngine.nextDouble()); 
		
			Gridlet gridJob = new Gridlet(jobId++, singleJobSize, singleInputSize, singleOutputSize);
			
			gridJobList.add(gridJob);
		}
		
		return gridJobList;
	}
	
	/**
	 * @param jobListSize
	 * @param userId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static GridletList createJobListWithUser(int jobListSize, int userId){
		GridletList gridJobList = new GridletList();
		
		int jobId = 0;
		Random randomEngine = new Random();
		GridSimStandardPE.setRating(MaGateProfile.peMIPS);
		
		double singleJobSize  = MaGateProfile.jobSize;
		long singleInputSize  = MaGateProfile.jobInputSize;
		long singleOutputSize = MaGateProfile.jobOutputSize;
		
		for (int i = 0; i < jobListSize; i ++){
			singleJobSize    = GridSimStandardPE.toMIs(randomEngine.nextDouble() * MaGateProfile.jobSize);
			singleInputSize  = (long) GridSimRandom.real(MaGateProfile.jobInputSize, MaGateProfile.job_minRange, MaGateProfile.job_maxRange, randomEngine.nextDouble()); 
			singleOutputSize = (long) GridSimRandom.real(MaGateProfile.jobOutputSize, MaGateProfile.job_minRange, MaGateProfile.job_maxRange, randomEngine.nextDouble()); 
		
			Gridlet gridJob = new Gridlet(jobId++, singleJobSize, singleInputSize, singleOutputSize);
			
			gridJob.setUserID(userId);
			gridJobList.add(gridJob);
		}
		
		return gridJobList;
	}
	
	/**
	 * @param jobListSize
	 * @param userId
	 * @param jobSizeParam
	 * @param inputSizeParam
	 * @param outputSizeParam
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static GridletList createJobListWithUser(int jobListSize, int userId, double jobSizeParam, 
			long inputSizeParam, long outputSizeParam){
		GridletList gridJobList = new GridletList();
		
		int jobId = 0;
		Random randomEngine = new Random();
		GridSimStandardPE.setRating(MaGateProfile.peMIPS);
		
		double singleJobSize  = jobSizeParam;
		long singleInputSize  = inputSizeParam;
		long singleOutputSize = outputSizeParam;
		
		for (int i = 0; i < jobListSize; i ++){
			singleJobSize    = GridSimStandardPE.toMIs(randomEngine.nextDouble() * jobSizeParam);
			singleInputSize  = (long) GridSimRandom.real(inputSizeParam, MaGateProfile.job_minRange, MaGateProfile.job_maxRange, randomEngine.nextDouble()); 
			singleOutputSize = (long) GridSimRandom.real(outputSizeParam, MaGateProfile.job_minRange, MaGateProfile.job_maxRange, randomEngine.nextDouble()); 
		
			Gridlet gridJob = new Gridlet(jobId++, singleJobSize, singleInputSize, singleOutputSize);
			
			gridJob.setUserID(userId);
			gridJobList.add(gridJob);
		}
		
		return gridJobList;
	}
	
	/**
	 * @param jobListSize
	 * @param userId
	 * @param jobSizeParam
	 * @param inputSizeParam
	 * @param outputSizeParam
	 * @param minRangeParam
	 * @param maxRangeParam
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static GridletList createJobListWithUser(int jobListSize, int userId, double jobSizeParam, 
			long inputSizeParam, long outputSizeParam, double minRangeParam, double maxRangeParam){
		GridletList gridJobList = new GridletList();
		
		int jobId = 0;
		Random randomEngine = new Random();
		GridSimStandardPE.setRating(MaGateProfile.peMIPS);
		
		double singleJobSize  = jobSizeParam;
		long singleInputSize  = inputSizeParam;
		long singleOutputSize = outputSizeParam;
		
		for (int i = 0; i < jobListSize; i ++){
			singleJobSize    = GridSimStandardPE.toMIs(randomEngine.nextDouble() * jobSizeParam);
			singleInputSize  = (long) GridSimRandom.real(inputSizeParam, minRangeParam, maxRangeParam, randomEngine.nextDouble()); 
			singleOutputSize = (long) GridSimRandom.real(outputSizeParam, minRangeParam, maxRangeParam, randomEngine.nextDouble()); 
		
			Gridlet gridJob = new Gridlet(jobId++, singleJobSize, singleInputSize, singleOutputSize);
			
			gridJob.setUserID(userId);
			gridJobList.add(gridJob);
		}
		
		return gridJobList;
	}

}


