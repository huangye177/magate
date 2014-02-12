package ch.hefr.gridgroup.magate.env;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.data.time.TimeSeries;

import ch.hefr.gridgroup.magate.model.JobInfo;
import ch.hefr.gridgroup.magate.storage.GlobalStorage;
import ch.hefr.gridgroup.magate.storage.MaGateDB;

public class JobCenterManager {

	private static Log log = LogFactory.getLog(JobCenterManager.class);
	private static Connection conn = null;
	
	public static int GENERATED = 0;
	public static int SUBMITTED = 1;
	public static int SCHEDULING = 2;
	public static int PROCESSING = 3;
	public static int TRANSFERRED = 4;
	public static int EXECUTED = 5;
	public static int FAILED = 6;
	public static int SUSPENDED = 7;
	
	private static ConcurrentHashMap<String, JobInfo> jobCenter = null;
	
	/**
	 * Initialize the JobQueueManager, e.g., get database connection
	 */
	public static void setup() {
		
		conn = null;
		
		try {
			conn = MaGateDB.getConnectionPool().getConnection();
//			conn.setTransactionIsolation(Connection.TRANSACTION_NONE);
//			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
//			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE); 
//			conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ); 
//			conn.setAutoCommit(false); 
			
			// clean the JobCenter DB storage via drop & create table
			flush();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void shutdown() {
		
		try {
			if(conn != null && (!conn.isClosed())) {
				conn.close();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			
		} finally {
			conn = null;
			
		}
	}
	
	/**
	 * create jobInfo_storage; clean old data from jobCenter_database.
	 */
	public static void flush() {
		
		flushJobCenter();
		jobCenter = null;
		
		System.gc();
		jobCenter = new ConcurrentHashMap<String, JobInfo>();
		
	}
	
	/** 
	 * Job created due to JubSubmitter (via customized scenario or GWA scenario)
	 * @param globalJobId
	 * @param jobInfo
	 */
	public static void jobGenerated(JobInfo jobInfo) {
		
		// put job into memory storage 
		String globalJobId = jobInfo.getGlobalJobID();
		jobCenter.put(globalJobId, jobInfo);
		
		
		// put job into DB storage
		PreparedStatement psInsertRecord = null;
		String sqlInsertRecord = "INSERT INTO job_center " +
				"(job_id, original_node, processing_node, status) VALUES (?,?,?,?)";
		
		try {
			
			// Insert Table
			psInsertRecord = conn.prepareStatement(sqlInsertRecord);
			psInsertRecord.setString(1, globalJobId);
			psInsertRecord.setString(2, jobInfo.getOriginalMaGateId());
			psInsertRecord.setString(3, jobInfo.getOriginalMaGateId());
			psInsertRecord.setInt(4, GENERATED);
			
			psInsertRecord.executeUpdate();
//			conn.commit(); 
			
			// Counting
			GlobalStorage.count_sql.incrementAndGet();
			
		} catch (Exception e) {
			// TODO: handle exception
//			try {
//				conn.rollback();
//				
//			} catch (SQLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			
			e.printStackTrace();
			
		} finally {
			
			try {
				psInsertRecord.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Job is submitted from LOCAL users 
	 * 
	 * @param globalJobId
	 * @param originalMaGate
	 * @param processingMaGate
	 */
	public static void jobSubmitted(String globalJobId, String processingMaGate, JobInfo jobInfo) { 
		
		// put job into memory storage 
		jobCenter.put(globalJobId, jobInfo);
		
		// put job into DB storage
		
		PreparedStatement psUpdateRecord = null;
		
		String updateCommunityStatusTB = "UPDATE job_center SET " +
			"processing_node = ?, status = ? " +
			" WHERE job_id = '" + globalJobId + "'; ";
		
		try {
			
			// Insert Table
			psUpdateRecord = conn.prepareStatement(updateCommunityStatusTB);
			psUpdateRecord.setString(1, processingMaGate);
			psUpdateRecord.setInt(2, SUBMITTED);
			
			psUpdateRecord.executeUpdate();
//			conn.commit(); 
			
			// Counting
			GlobalStorage.count_sql.incrementAndGet();
			
		} catch (Exception e) {
			// TODO: handle exception
			
//			try {
//				conn.rollback();
//			} catch (SQLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			e.printStackTrace();
			
		} finally {
			
			try {
				psUpdateRecord.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Job is transfered (submitted) from REMOTE nodes 
	 *  
	 * @param globalJobId
	 * @param processingMaGate
	 * @param previousMaGate
	 */
	public static void jobTransferred(String globalJobId, String processingMaGate, 
			String previousMaGate, JobInfo jobInfo) {
		
		// put job into memory storage 
		jobCenter.put(globalJobId, jobInfo);
		
		// put job into DB storage
		
		PreparedStatement psUpdateRecord = null;
		
		String updateCommunityStatusTB = "UPDATE job_center SET " +
			"processing_node = ?, previous_node = ?, status = ? " +
			" WHERE job_id = '" + globalJobId + "'; ";
		
		try {
			
			// Insert Table
			psUpdateRecord = conn.prepareStatement(updateCommunityStatusTB);
			psUpdateRecord.setString(1, processingMaGate);
			psUpdateRecord.setString(2, previousMaGate);
			psUpdateRecord.setInt(3, TRANSFERRED);
			
			psUpdateRecord.executeUpdate();
//			conn.commit(); 
			
			// Counting
			GlobalStorage.count_sql.incrementAndGet();
			
		} catch (Exception e) {
			
//			try {
//				conn.rollback();
//			} catch (SQLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			// TODO: handle exception
			e.printStackTrace();
			
		} finally {
			
			try {
				psUpdateRecord.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Job is transfered (submitted) from REMOTE nodes 
	 *  
	 * @param globalJobId
	 * @param processingMaGate
	 * @param previousMaGate
	 */
	public static void jobTransferred(String globalJobId, String processingMaGate, JobInfo jobInfo) { 
		
		// put job into memory storage 
		jobCenter.put(globalJobId, jobInfo);
		
		// put job into DB storage
		PreparedStatement psUpdateRecord = null;
		
		String updateCommunityStatusTB = "UPDATE job_center SET " +
			"processing_node = ?, status = ? " +
			" WHERE job_id = '" + globalJobId + "'; ";
		
		try {
			
			// Insert Table
			psUpdateRecord = conn.prepareStatement(updateCommunityStatusTB);
			psUpdateRecord.setString(1, processingMaGate);
			psUpdateRecord.setInt(2, TRANSFERRED);
			
			psUpdateRecord.executeUpdate();
//			conn.commit(); 
			
			// Counting
			GlobalStorage.count_sql.incrementAndGet();
			
		} catch (Exception e) {
			
//			try {
//				conn.rollback();
//			} catch (SQLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			// TODO: handle exception
			e.printStackTrace();
			
		} finally {
			
			try {
				psUpdateRecord.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void jobScheduling(String globalJobId, String processingMaGate, JobInfo jobInfo) { 
		
		// put job into memory storage 
		jobCenter.put(globalJobId, jobInfo);
		
		// put job into DB storage
		PreparedStatement psUpdateRecord = null;
		
		String updateCommunityStatusTB = "UPDATE job_center SET " +
			"processing_node = ?, status = ? " +
			" WHERE job_id = '" + globalJobId + "'; ";
		
		try {
			
			// Insert Table
			psUpdateRecord = conn.prepareStatement(updateCommunityStatusTB);
			psUpdateRecord.setString(1, processingMaGate);
			psUpdateRecord.setInt(2, SCHEDULING);
			
			psUpdateRecord.executeUpdate();
//			conn.commit(); 
			
			// Counting 
			GlobalStorage.count_sql.incrementAndGet();
			
		} catch (Exception e) {
			
//			try {
//				conn.rollback();
//			} catch (SQLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			// TODO: handle exception
			e.printStackTrace();
			
		} finally {
			
			try {
				psUpdateRecord.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public static void jobProcessing(String globalJobId, String processingMaGate, JobInfo jobInfo) { 

		// put job into memory storage 
		jobCenter.put(globalJobId, jobInfo);
		
		// put job into DB storage
		PreparedStatement psUpdateRecord = null;
		
		String updateCommunityStatusTB = "UPDATE job_center SET " +
			"processing_node = ?, status = ? " +
			" WHERE job_id = '" + globalJobId + "'; ";
		
		try {
			
			// Insert Table
			psUpdateRecord = conn.prepareStatement(updateCommunityStatusTB);
			psUpdateRecord.setString(1, processingMaGate);
			psUpdateRecord.setInt(2, PROCESSING);
			
			psUpdateRecord.executeUpdate();
			
			// Counting 
			GlobalStorage.count_sql.incrementAndGet();
			
			// Record live community status
//			GlobalStorage.recordLiveCommunityStatus();
			GlobalStorage.test_counter_1.incrementAndGet();
			
			
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			
			try {
				psUpdateRecord.close();
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void jobExecuted(String globalJobId, String processingMaGate, JobInfo jobInfo) {
		
		// Counting 
		GlobalStorage.count_sql.incrementAndGet();
		
		// put job into memory storage 
		jobCenter.put(globalJobId, jobInfo);
		
		// put job into DB storage
		PreparedStatement psUpdateRecord = null;
		
		String updateCommunityStatusTB = "UPDATE job_center SET " +
			"processing_node = ?, executor_node = ?, status = ? " +
			" WHERE job_id = '" + globalJobId + "'; ";
		
		try {
			
			// Insert Table
			psUpdateRecord = conn.prepareStatement(updateCommunityStatusTB);
			psUpdateRecord.setString(1, processingMaGate);
			psUpdateRecord.setString(2, processingMaGate);
			psUpdateRecord.setInt(3, EXECUTED);
			
			psUpdateRecord.executeUpdate();
			
//			conn.commit(); 
			
		} catch (Exception e) {
			
//			try {
//				conn.rollback();
//			} catch (SQLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			// TODO: handle exception
			e.printStackTrace();
			
		} finally {
			
			try {
				psUpdateRecord.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void jobFailed(String globalJobId, String processingMaGate, JobInfo jobInfo) {
		
		// Counting 
		GlobalStorage.count_sql.incrementAndGet();
		
		// put job into memory storage 
		jobCenter.put(globalJobId, jobInfo);
		
		// put job into DB storage
		PreparedStatement psUpdateRecord = null;
		
		String updateCommunityStatusTB = "UPDATE job_center SET " +
			"processing_node = ?, executor_node = ?, status = ? " +
			" WHERE job_id = '" + globalJobId + "'; ";
		
		try {
			
			// Insert Table
			psUpdateRecord = conn.prepareStatement(updateCommunityStatusTB);
			psUpdateRecord.setString(1, processingMaGate);
			psUpdateRecord.setString(2, processingMaGate);
			psUpdateRecord.setInt(3, FAILED);
			
			psUpdateRecord.executeUpdate();
//			conn.commit(); 
			
		} catch (Exception e) {
			
//			try {
//				conn.rollback();
//			} catch (SQLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			// TODO: handle exception
			e.printStackTrace();
			
		} finally {
			
			try {
				psUpdateRecord.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void jobSuspended(String globalJobId, String processingMaGate, JobInfo jobInfo) {
		
		// Counting 
		GlobalStorage.count_sql.incrementAndGet();
		
		// put job into memory storage 
		jobCenter.put(globalJobId, jobInfo);
		
		// put job into DB storage
		PreparedStatement psUpdateRecord = null;
		
		String updateCommunityStatusTB = "UPDATE job_center SET " +
			"processing_node = ?, status = ? " +
			" WHERE job_id = '" + globalJobId + "'; ";
		
		try {
			
			// Insert Table
			psUpdateRecord = conn.prepareStatement(updateCommunityStatusTB);
			psUpdateRecord.setString(1, processingMaGate);
			psUpdateRecord.setInt(2, SUSPENDED);
			
			psUpdateRecord.executeUpdate();
//			conn.commit(); 
			
		} catch (Exception e) {
			
//			try {
//				conn.rollback();
//			} catch (SQLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			// TODO: handle exception
			e.printStackTrace();
			
		} finally {
			
			try {
				psUpdateRecord.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void flushJobCenter() {
		
		Statement stmt = null;
		
        String dropJobCenterTB = "DROP TABLE IF EXISTS job_center;";
	    String createJobCenterTB = "CREATE TABLE job_center (job_id VARCHAR(225), " +
	    		"original_node VARCHAR(225), executor_node VARCHAR(225), processing_node VARCHAR(225), previous_node VARCHAR(225)," +
	    		"status INT default -1);";
        
        try {
			stmt = conn.createStatement();
			stmt.executeUpdate(dropJobCenterTB);
			stmt.executeUpdate(createJobCenterTB);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	/******************************************
	 * Operation on JobCenter (Memory/Database)
	 ******************************************/
	
	public static JobInfo getJobInfobyJobId(String jobId) {
		if(jobId == null) {
			return null;
		} else {
			return jobCenter.get(jobId);
		}
	}
	
	public static int getJobStatusbyJobId(String jobId) {
		
		int status = -1;
		
		String selectRecord = "SELECT status FROM job_center " + 
			" WHERE job_id = '" + jobId + "'; ";
		
		Statement stmt = null;	
		ResultSet rs = null;
		
		try {
			
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(selectRecord);
//			conn.commit(); 
			
			rs.next();
			status = rs.getInt("status");
			
			// Counting 
			GlobalStorage.count_sql3.incrementAndGet();
			GlobalStorage.count_test1.incrementAndGet();
				
		} catch (Exception e) {
			
//			try {
//				conn.rollback();
//			} catch (SQLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			// TODO: handle exception
			e.printStackTrace();
			
		} finally {
			
			try {
				stmt.close();
				rs.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return status;
	}
	
	public static Vector<JobInfo> getGeneratedJobs(String magateId) {
		
		Vector<String> jobIdList = new Vector<String>();
		Vector<JobInfo> jobList = new Vector<JobInfo>();
		
		String selectRecord = "SELECT job_id FROM job_center " + 
			" WHERE processing_node = '" + magateId + "' and " + 
			" status = " + GENERATED + ";";
		
		Statement stmt = null;	
		ResultSet rs = null;
		
		try {
			
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(selectRecord);
//			conn.commit(); 
				
			while (rs.next()) {
				jobIdList.add(rs.getString("job_id"));
			}
			
			// Counting 
			GlobalStorage.count_sql3.incrementAndGet();
			GlobalStorage.count_test2.incrementAndGet();
				
		} catch (Exception e) {
			
			e.printStackTrace();
			
		} finally {
			
			try {
				stmt.close();
				rs.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		JobInfo jobInfo = null;
		
		for(String jobId : jobIdList) {
			jobInfo = jobCenter.get(jobId);
			
			if(jobInfo != null) {
				jobList.add(jobInfo);
			}
		}
		
		return jobList;
		
	}

	public static Vector<JobInfo> getJob_processingNode(String maGateId, int status) {
		
		Vector<String> jobIdList = new Vector<String>();
		Vector<JobInfo> jobList = new Vector<JobInfo>();

		String selectRecord = "SELECT job_id FROM job_center " +
			" WHERE processing_node = '" + maGateId + "' and " + 
			" status = " + status + ";";

		Statement stmt = null;
		ResultSet rs = null;

		try {

			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(selectRecord);
//			conn.commit(); 
			
			while (rs.next()) {
				jobIdList.add(rs.getString("job_id"));
			}
			
			// Counting 
			GlobalStorage.count_sql3.incrementAndGet();
			GlobalStorage.count_test3.incrementAndGet();

		} catch (Exception e) {
			
//			try {
//				conn.rollback();
//			} catch (SQLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			// TODO: handle exception
			e.printStackTrace();

		} finally {

			try {
				stmt.close();
				rs.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		JobInfo jobInfo = null;

		for (String jobId : jobIdList) {
			jobInfo = jobCenter.get(jobId);

			if (jobInfo != null) {
				jobList.add(jobInfo);
			}
		}

		return jobList;

	}
	
	
	public static int sizeOfJob_originalNode(String maGateId, int status) {
		
		String selectRecord = "SELECT count(*) as size_counter FROM job_center " + 
			" WHERE original_node = '" + maGateId + "' and " + 
			" status = " + status + ";";

		Statement stmt = null;
		ResultSet rs = null;
		
		int targetSize = 0;

		try {

			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(selectRecord);
//			conn.commit(); 
			
			rs.next();
			
			targetSize = rs.getInt("size_counter");
			
			// Counting 
			GlobalStorage.count_sql3.incrementAndGet();
			GlobalStorage.count_test4.incrementAndGet();

		} catch (Exception e) {
			
//			try {
//				conn.rollback();
//			} catch (SQLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			// TODO: handle exception
			e.printStackTrace();

		} finally {

			try {
				stmt.close();
				rs.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return targetSize;

	}
	
	public static int sizeOfJob_processingNode(String maGateId, int status) {
		
		String selectRecord = "SELECT count(*) as size_counter FROM job_center " + 
			" WHERE processing_node = ? and status = ? ;";

		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		int targetSize = 0;

		try {

			stmt = conn.prepareStatement(selectRecord);
			stmt.setString(1, maGateId);
			stmt.setInt(2, status);
			
			rs = stmt.executeQuery();
//			conn.commit(); 
			
			rs.next();
			
			targetSize = rs.getInt("size_counter");
			
			// Counting 
			GlobalStorage.count_sql3.incrementAndGet();
			GlobalStorage.count_test5.incrementAndGet();

		} catch (Exception e) {
			
//			try {
//				conn.rollback();
//			} catch (SQLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			// TODO: handle exception
			e.printStackTrace();

		} finally {

			try {
				stmt.close();
				rs.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return targetSize;

	}
	
	public static int sizeOfJob_ProcessingNode(String maGateId) {
		
		String selectRecord = "SELECT count(*) as size_counter FROM job_center " + 
			" WHERE processing_node = '" + maGateId + "';";

		Statement stmt = null;
		ResultSet rs = null;
		
		int targetSize = 0;

		try {

			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(selectRecord);
//			conn.commit(); 
			
			rs.next();
			
			targetSize = rs.getInt("size_counter");
			
			// Counting 
			GlobalStorage.count_sql3.incrementAndGet();

		} catch (Exception e) {
			
//			try {
//				conn.rollback();
//			} catch (SQLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			// TODO: handle exception
			e.printStackTrace();

		} finally {

			try {
				stmt.close();
				rs.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return targetSize;

	}
	
	public static int sizeOfLocalExedJob_ProcessingNode(String maGateId) {
		
		String selectRecord = "SELECT count(*) as size_counter FROM job_center " + 
			" WHERE original_node = executor_node and processing_node = '" + maGateId + "';";

		Statement stmt = null;
		ResultSet rs = null;
		
		int targetSize = 0;

		try {

			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(selectRecord);
//			conn.commit(); 
			
			rs.next();
			
			targetSize = rs.getInt("size_counter");
			
			// Counting 
			GlobalStorage.count_sql3.incrementAndGet();

		} catch (Exception e) {
			
//			try {
//				conn.rollback();
//			} catch (SQLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			// TODO: handle exception
			e.printStackTrace();

		} finally {

			try {
				stmt.close();
				rs.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return targetSize;

	}
	
	public static int sizeOfCommunityExedJob_ProcessingNode(String maGateId) {
		
		String selectRecord = "SELECT count(*) as size_counter FROM job_center " + 
			" WHERE original_node != executor_node and processing_node = '" + maGateId + "';";

		Statement stmt = null;
		ResultSet rs = null;
		
		int targetSize = 0;

		try {

			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(selectRecord);
//			conn.commit(); 
			
			rs.next();
			
			targetSize = rs.getInt("size_counter");
			
			// Counting 
			GlobalStorage.count_sql3.incrementAndGet();

		} catch (Exception e) {
			
//			try {
//				conn.rollback();
//			} catch (SQLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			// TODO: handle exception
			e.printStackTrace();

		} finally {

			try {
				stmt.close();
				rs.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return targetSize;

	}
	
	
	/**
	 * Jobs with non-generated status 
	 * 
	 * @param status
	 * @return
	 */
	public static int sizeOfJob_byStatus(int status) {
		
		String selectRecord = "SELECT count(*) as size_counter FROM job_center " + 
			" WHERE status = ?;";

		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		int targetSize = 0;

		try {
			stmt = conn.prepareStatement(selectRecord);
			stmt.setInt(1, status);
			
			rs = stmt.executeQuery();
//			conn.commit(); 
			
			rs.next();
			
			targetSize = rs.getInt("size_counter");

		} catch (Exception e) {
			
//			try {
//				conn.rollback();
//			} catch (SQLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			// TODO: handle exception
			e.printStackTrace();

		} finally {

			try {
				stmt.close();
				rs.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return targetSize;

	}

	/**
	 * Obtaining size of jobs in variety of status
	 * 
	 * GENERATED = 0; SUBMITTED = 1; SCHEDULING = 2; PROCESSING = 3; TRANSFERRED = 4;
	 * EXECUTED = 5; FAILED = 6; SUSPENDED = 7;
	 * @return
	 */
	public static int[] sizeOfJob_allStatus() {
		
		if(conn == null) {
			return null;
		}
		
		String selectGENERATED   = "SELECT count(*) as size_counter FROM job_center WHERE status = 0;";
		String selectSUBMITTED   = "SELECT count(*) as size_counter FROM job_center WHERE status = 1;";
		String selectSCHEDULINGD = "SELECT count(*) as size_counter FROM job_center WHERE status = 2;";
		String selectPROCESSING  = "SELECT count(*) as size_counter FROM job_center WHERE status = 3;";
		String selectTRANSFERRED = "SELECT count(*) as size_counter FROM job_center WHERE status = 4;";
		String selectEXECUTED    = "SELECT count(*) as size_counter FROM job_center WHERE status = 5;";
		String selectFAILED      = "SELECT count(*) as size_counter FROM job_center WHERE status = 6;";
		String selectSUSPENDED   = "SELECT count(*) as size_counter FROM job_center WHERE status = 7;";
		
		int[] sizeAllJobs = new int[8];
		
		int sizeGenerated   = 0;
		int sizeSubmitted   = 0;
		int sizeScheduling  = 0;
		int sizeProcessing  = 0;
		int sizeTransferred = 0;
		int sizeExecuted    = 0;
		int sizeFailed      = 0;
		int sizeSuspended   = 0;
		
		Statement stmt = null;	
		ResultSet rs = null;
		
		try {
		
			if(conn == null) {
				return null;
			}
			stmt = conn.createStatement();
//			System.out.println("*************** 1 ");
			rs = stmt.executeQuery(selectGENERATED);
			rs.next();
			sizeGenerated = rs.getInt("size_counter");
			rs.close();
			
			rs = stmt.executeQuery(selectSUBMITTED);
			rs.next();
			sizeSubmitted = rs.getInt("size_counter");
			rs.close();
			
			rs = stmt.executeQuery(selectSCHEDULINGD);
			rs.next();
			sizeScheduling = rs.getInt("size_counter");
			rs.close();
			
			rs = stmt.executeQuery(selectPROCESSING);
			rs.next();
			sizeProcessing = rs.getInt("size_counter");
			rs.close();
			
			rs = stmt.executeQuery(selectTRANSFERRED);
			rs.next();
			sizeTransferred = rs.getInt("size_counter");
			rs.close();
			
			rs = stmt.executeQuery(selectEXECUTED);
			rs.next();
			sizeExecuted = rs.getInt("size_counter");
			rs.close();
			
			rs = stmt.executeQuery(selectFAILED);
			rs.next();
			sizeFailed = rs.getInt("size_counter");
			rs.close();
			
			rs = stmt.executeQuery(selectSUSPENDED);
			rs.next();
			sizeSuspended = rs.getInt("size_counter");
			rs.close();
			
			
			///////
			sizeAllJobs[0] = sizeGenerated;
			sizeAllJobs[1] = sizeSubmitted;
			sizeAllJobs[2] = sizeScheduling;
			sizeAllJobs[3] = sizeProcessing;
			sizeAllJobs[4] = sizeTransferred;
			sizeAllJobs[5] = sizeExecuted;
			sizeAllJobs[6] = sizeFailed;
			sizeAllJobs[7] = sizeSuspended;
			
		} catch (Exception e) {
			e.printStackTrace();
		
		} finally {
		
			try {
				stmt.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return sizeAllJobs;
		
	}
	
	
	public static int[] sizeOfJob_allStatusByNode(String maGateId) {
		
		if(conn == null) {
			return null;
		}
		
		String selectGENERATED   = "SELECT count(*) as size_counter FROM job_center WHERE processing_node = ? and status = 0;";
		String selectSUBMITTED   = "SELECT count(*) as size_counter FROM job_center WHERE processing_node = ? and status = 1;";
		String selectSCHEDULINGD = "SELECT count(*) as size_counter FROM job_center WHERE processing_node = ? and status = 2;";
		String selectPROCESSING  = "SELECT count(*) as size_counter FROM job_center WHERE processing_node = ? and status = 3;";
		String selectTRANSFERRED = "SELECT count(*) as size_counter FROM job_center WHERE processing_node = ? and status = 4;";
		String selectEXECUTED    = "SELECT count(*) as size_counter FROM job_center WHERE processing_node = ? and status = 5;";
		String selectFAILED      = "SELECT count(*) as size_counter FROM job_center WHERE processing_node = ? and status = 6;";
		String selectSUSPENDED   = "SELECT count(*) as size_counter FROM job_center WHERE processing_node = ? and status = 7;";
		
		int[] sizeAllJobs = new int[8];
		
		int sizeGenerated   = 0;
		int sizeSubmitted   = 0;
		int sizeScheduling  = 0;
		int sizeProcessing  = 0;
		int sizeTransferred = 0;
		int sizeExecuted    = 0;
		int sizeFailed      = 0;
		int sizeSuspended   = 0;
		
		PreparedStatement stmt = null;	
		ResultSet rs = null;
		
		try {
		
			if(conn == null) {
				return null;
			}
			stmt = conn.prepareStatement(selectGENERATED);
			stmt.setString(1, maGateId);
			rs = stmt.executeQuery();
			rs.next();
			sizeGenerated = rs.getInt("size_counter");
			rs.close();
			
			stmt = conn.prepareStatement(selectSUBMITTED);
			stmt.setString(1, maGateId);
			rs = stmt.executeQuery();
			rs.next();
			sizeSubmitted = rs.getInt("size_counter");
			rs.close();
			
			stmt = conn.prepareStatement(selectSCHEDULINGD);
			stmt.setString(1, maGateId);
			rs = stmt.executeQuery();
			rs.next();
			sizeScheduling = rs.getInt("size_counter");
			rs.close();
			
			stmt = conn.prepareStatement(selectPROCESSING);
			stmt.setString(1, maGateId);
			rs = stmt.executeQuery();
			rs.next();
			sizeProcessing = rs.getInt("size_counter");
			rs.close();
			
			stmt = conn.prepareStatement(selectTRANSFERRED);
			stmt.setString(1, maGateId);
			rs = stmt.executeQuery();
			rs.next();
			sizeTransferred = rs.getInt("size_counter");
			rs.close();
			
			stmt = conn.prepareStatement(selectEXECUTED);
			stmt.setString(1, maGateId);
			rs = stmt.executeQuery();
			rs.next();
			sizeExecuted = rs.getInt("size_counter");
			rs.close();
			
			stmt = conn.prepareStatement(selectFAILED);
			stmt.setString(1, maGateId);
			rs = stmt.executeQuery();
			rs.next();
			sizeFailed = rs.getInt("size_counter");
			rs.close();
			
			stmt = conn.prepareStatement(selectSUSPENDED);
			stmt.setString(1, maGateId);
			rs = stmt.executeQuery();
			rs.next();
			sizeSuspended = rs.getInt("size_counter");
			rs.close();
			
			
			///////
			sizeAllJobs[0] = sizeGenerated;
			sizeAllJobs[1] = sizeSubmitted;
			sizeAllJobs[2] = sizeScheduling;
			sizeAllJobs[3] = sizeProcessing;
			sizeAllJobs[4] = sizeTransferred;
			sizeAllJobs[5] = sizeExecuted;
			sizeAllJobs[6] = sizeFailed;
			sizeAllJobs[7] = sizeSuspended;
			
		} catch (Exception e) {
			e.printStackTrace();
		
		} finally {
		
			try {
				stmt.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return sizeAllJobs;
		
	}
	
	/**
	 * Get TimeSeries[] in terms of Community Status
	 * 
	 * 0. arrived_job
	 * 1. scheduling_job
	 * 
	 * 2. processing_job
	 * 3. executed_job
	 * 4. suspended_job 
	 * 
	 * 5. rjc 
	 * 6. ce 
	 * 7. network_degree 
	 * 8. cfc_degree
	 * 
	 * @return 9-size TimeSeries[]
	 */
	public static TimeSeries[] plot_getPerIterationCommunityStatus(TimeSeries[] timeseries) {
		
		String selectRecord = "SELECT DISTINCT simtime, arrived_job, scheduling_job, " +
				"processing_job, executed_job, suspended_job, " +
				"rjc, ce, network_degree, cfc_degree FROM live_community order by simtime;";

		double simttime       = 0.0;
		double arrived_job    = 0.0;
		double scheduling_job = 0.0;
		double processing_job = 0.0;
		double executed_job   = 0.0;
		double suspended_job  = 0.0;
		double rjc            = 0.0;
		double ce             = 0.0;
		double network_degree = 0.0;
		double cfc_degree     = 0.0;
		
		Statement stmt = null;
		ResultSet rs = null;
		boolean foundResult = false;

		try {

			stmt = conn.createStatement();
	        
			rs = stmt.executeQuery(selectRecord);
			Calendar calendar = GregorianCalendar.getInstance(); 
			
			while (rs.next()) {
				
				foundResult    = true;
				
				simttime       = rs.getDouble("simtime");
				arrived_job    = rs.getDouble("arrived_job");
				scheduling_job = rs.getDouble("scheduling_job");
				processing_job = rs.getDouble("processing_job");
				executed_job   = rs.getDouble("executed_job");
				suspended_job  = rs.getDouble("suspended_job");
				rjc            = rs.getDouble("rjc");
				ce             = rs.getDouble("ce");
				network_degree = rs.getDouble("network_degree");
				cfc_degree     = rs.getDouble("cfc_degree");
				
				
				calendar.setTimeInMillis((long)simttime * 1000); 
				
//				org.jfree.data.time.Minute time = new org.jfree.data.time.Minute(calendar.getTime());
				org.jfree.data.time.Hour time = new org.jfree.data.time.Hour(calendar.getTime());
				
				timeseries[0].addOrUpdate(time, arrived_job);
				timeseries[1].addOrUpdate(time, scheduling_job);
				timeseries[2].addOrUpdate(time, processing_job);
				timeseries[3].addOrUpdate(time, executed_job);
				timeseries[4].addOrUpdate(time, suspended_job);
				timeseries[5].addOrUpdate(time, rjc);
				timeseries[6].addOrUpdate(time, ce);
				timeseries[7].addOrUpdate(time, network_degree);
				timeseries[8].addOrUpdate(time, cfc_degree);
			}
			
		} catch (Exception e) {
			e.printStackTrace();

		} finally {

			try {
				stmt.close();
				rs.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(foundResult) {
			return timeseries;
		} else {
			return null;
		}
		
	}
	
	
	public static TimeSeries[] plot_getPerScenarioCommunityStatus(TimeSeries[] timeseries) {
		
		String selectRecord = "SELECT simtime, AVG(arrived_job) as arrived_job, " +
			"AVG(scheduling_job) as scheduling_job, AVG(processing_job) as processing_job, " +
			"AVG(executed_job) as executed_job, AVG(suspended_job) as suspended_job, AVG(rjc) as rjc, " +
			"AVG(ce) as ce, AVG(network_degree) as network_degree, AVG(cfc_degree) as cfc_degree " +
			"FROM scenario_community group by simtime order by simtime;";

//		String selectRecord = "SELECT simtime, AVG(arrived_job) as arrived_job, " +
//			"AVG(scheduling_job) as scheduling_job, AVG(processing_job) as processing_job, " +
//			"AVG(executed_job) as executed_job, AVG(suspended_job) as suspended_job, AVG(rjc) as rjc, " +
//			"AVG(ce) as ce, AVG(network_degree) as network_degree, AVG(cfc_degree) as cfc_degree " +
//			"FROM scenario_community group by simtime HAVING COUNT(simtime)>" + (MaGateParam.countOfExperiment - 1) + 
//			" order by simtime;";
		
		double simttime       = 0.0;
		double arrived_job    = 0.0;
		double scheduling_job = 0.0;
		double processing_job = 0.0;
		double executed_job   = 0.0;
		double suspended_job  = 0.0;
		double rjc            = 0.0;
		double ce             = 0.0;
		double network_degree = 0.0;
		double cfc_degree     = 0.0;
		
		Statement stmt = null;
		ResultSet rs = null;
		boolean foundResult = false;

		try {
			stmt = conn.createStatement();
	        
			rs = stmt.executeQuery(selectRecord);
			Calendar calendar = GregorianCalendar.getInstance(); 
			
			while (rs.next()) {
				
				foundResult    = true;
				
				simttime       = rs.getDouble("simtime");
				arrived_job    = rs.getDouble("arrived_job");
				scheduling_job = rs.getDouble("scheduling_job");
				processing_job = rs.getDouble("processing_job");
				executed_job   = rs.getDouble("executed_job");
				suspended_job  = rs.getDouble("suspended_job");
				rjc            = rs.getDouble("rjc");
				ce             = rs.getDouble("ce");
				network_degree = rs.getDouble("network_degree");
				cfc_degree     = rs.getDouble("cfc_degree");
				
				
				calendar.setTimeInMillis((long)simttime * 1000); 
				
				org.jfree.data.time.Hour time = new org.jfree.data.time.Hour(calendar.getTime());
				
				timeseries[0].addOrUpdate(time, arrived_job);
				timeseries[1].addOrUpdate(time, scheduling_job);
				timeseries[2].addOrUpdate(time, processing_job);
				timeseries[3].addOrUpdate(time, executed_job);
				timeseries[4].addOrUpdate(time, suspended_job);
				timeseries[5].addOrUpdate(time, rjc);
				timeseries[6].addOrUpdate(time, ce);
				timeseries[7].addOrUpdate(time, network_degree);
				timeseries[8].addOrUpdate(time, cfc_degree);
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();

		} finally {

			try {
				stmt.close();
				rs.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(foundResult) {
			return timeseries;
		} else {
			return null;
		}
		
	}
//	
//	/**
//	 * Recording community status change per iteraion into .csv files
//	 */
//	public static void recordLiveCommunityFlowIntoCSV() {
//		
//		String selectRecord = "SELECT " +
//		"simtime, arrived_job, scheduling_job, processing_job, transferred_job, " +
//		"executed_job, suspended_job, failed_job, rjc, ce, network_degree, cfc_degree " +
//		"FROM live_community;";
////		
////		String selectRecord = "SELECT " +
////		"simtime, arrived_job, scheduling_job, processing_job, transferred_job, " +
////		"executed_job, suspended_job, failed_job, rjc, ce, network_degree, cfc_degree " +
////		"FROM scenario_community;";
//		
//		double simtime       = 0.0;
//		double arrived_job    = 0.0;
//		double scheduling_job = 0.0;
//		double processing_job = 0.0;
//		double executed_job   = 0.0;
//		double suspended_job  = 0.0;
//		double rjc            = 0.0;
//		double ce             = 0.0;
//		double network_degree = 0.0;
//		double cfc_degree     = 0.0;
//		
//		Statement stmt = null;
//		ResultSet rs = null;
//		boolean foundResult = false;
//		
//		String toPrintContent = "\nsimtime, arrived_job, executed_job, Exp-Id\n";
//
//		try {
//			stmt = conn.createStatement();
//			rs = stmt.executeQuery(selectRecord);
//			
//			while (rs.next()) {
//				
//				foundResult    = true;
//				
//				simtime       = rs.getDouble("simtime");
//				arrived_job    = rs.getDouble("arrived_job");
//				scheduling_job = rs.getDouble("scheduling_job");
//				processing_job = rs.getDouble("processing_job");
//				executed_job   = rs.getDouble("executed_job");
//				suspended_job  = rs.getDouble("suspended_job");
//				rjc            = rs.getDouble("rjc");
//				ce             = rs.getDouble("ce");
//				network_degree = rs.getDouble("network_degree");
//				cfc_degree     = rs.getDouble("cfc_degree");
//				
//				toPrintContent += simtime + ", " + arrived_job + ", " + executed_job  + ", " + 
//					"[Exp-Id]" + MaGateParam.currentExperimentIndex + "\n";
//				
//			}
//			
//			MaGateToolkit.writeResult("live_community.csv", toPrintContent);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//
//		} finally {
//
//			try {
//				stmt.close();
//				rs.close();
//
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//	}
//	
//	/**
//	 * Recording sum/grouped community status change per scenario into .csv files
//	 */
//	public static void recordScenarioCommunityFlowIntoCSV() {
//		
//		String selectRecord = "SELECT " +
//		"simtime, sum(arrived_job) as arrived_job, sum(executed_job) as executed_job " +
//		"FROM scenario_community group by simtime;";
//		
//		
//		double simtime       = 0.0;
//		double arrived_job    = 0.0;
//		double executed_job   = 0.0;
//		
//		Statement stmt = null;
//		ResultSet rs = null;
//		boolean foundResult = false;
//		
//		String toPrintContent = "\nsimtime, arrived_job, executed_job, Exp-Id\n";
//
//		try {
//			stmt = conn.createStatement();
//			rs = stmt.executeQuery(selectRecord);
//			
//			while (rs.next()) {
//				
//				foundResult    = true;
//				
//				simtime       = rs.getDouble("simtime");
//				arrived_job    = rs.getDouble("arrived_job");
//				executed_job   = rs.getDouble("executed_job");
//				
//				toPrintContent += simtime + ", " + arrived_job + ", " + executed_job  + ", " + 
//				"[Exp-Id]" + MaGateParam.currentExperimentIndex + "\n";
//				
//			}
//			
//			MaGateToolkit.writeResult("scenario_community_grouped.csv", toPrintContent);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//
//		} finally {
//
//			try {
//				stmt.close();
//				rs.close();
//
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//	}
//	
//	/**
//	 * Recording avg/grouped community status change per scenario into .csv files
//	 */
//	public static void recordScenarioCommunityFlowIntoCSV2() {
//		
//		String selectRecord = "SELECT " +
//		"simtime, avg(arrived_job) as arrived_job, avg(executed_job) as executed_job " +
//		"FROM scenario_community group by simtime;";
//		
//		
//		double simtime       = 0.0;
//		double arrived_job    = 0.0;
//		double executed_job   = 0.0;
//		
//		Statement stmt = null;
//		ResultSet rs = null;
//		boolean foundResult = false;
//		
//		String toPrintContent = "\nsimtime, arrived_job, executed_job, Exp-Id\n";
//
//		try {
//			stmt = conn.createStatement();
//			rs = stmt.executeQuery(selectRecord);
//			
//			while (rs.next()) {
//				
//				foundResult    = true;
//				
//				simtime       = rs.getDouble("simtime");
//				arrived_job    = rs.getDouble("arrived_job");
//				executed_job   = rs.getDouble("executed_job");
//				
//				toPrintContent += simtime + ", " + arrived_job + ", " + executed_job  + ", " + 
//				"[Exp-Id]" + MaGateParam.currentExperimentIndex + "\n";
//				
//			}
//			
//			MaGateToolkit.writeResult("scenario_community_grouped_avg.csv", toPrintContent);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//
//		} finally {
//
//			try {
//				stmt.close();
//				rs.close();
//
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//	}
	
}



