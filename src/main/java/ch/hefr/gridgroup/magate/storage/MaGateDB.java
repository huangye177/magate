package ch.hefr.gridgroup.magate.storage;

import java.sql.*;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.Server;

import ch.hefr.gridgroup.magate.casa.CASAPolicy;
import ch.hefr.gridgroup.magate.env.MaGateParam;
import ch.hefr.gridgroup.magate.env.MaGateToolkit;
import ch.hefr.gridgroup.magate.input.ExpScenario;

public class MaGateDB {

	private static int port      = 8082;  
	private static Server server = null;  
	
	private static JdbcConnectionPool memoryConnectionPool = null;
	private static JdbcConnectionPool diskConnectionPool = null;

	protected static final Log logger = LogFactory.getLog(MaGateDB.class);
	
	private static String memoryConnctionStr = "jdbc:h2:mem:smartgrid;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
//	private static String memoryConnctionStr = "jdbc:h2:mem:sg";
	
	
	private static String diskConnctionStr = "jdbc:h2:tcp://localhost/~/magate;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE;DB_CLOSE_DELAY=-1";
//	private static String connSQL = "jdbc:h2:mem:smartgrid3;DB_CLOSE_DELAY=-1;;DB_CLOSE_ON_EXIT=FALSELOCK_MODE=1";
//	private static String connSQL = "jdbc:h2:tcp://localhost/~/magate;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE;DB_CLOSE_DELAY=-1";
	
	/**
	 * Setup DW connection pool
	 */
	public static void dbSetup() {
		
		try {
			Class.forName("org.h2.Driver");
			memoryConnectionPool = JdbcConnectionPool.create(memoryConnctionStr,  "sa", "sa");
			diskConnectionPool = JdbcConnectionPool.create(diskConnctionStr,  "sa", "sa");
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
	}
	
	/**
	 * Shutdown DW connection pool
	 */
	public static void dbShutdown() {
		try {
			
			if(memoryConnectionPool != null) {
				memoryConnectionPool.dispose();
				memoryConnectionPool = null;
			}
			
			if(diskConnectionPool != null) {
				diskConnectionPool.dispose();
				diskConnectionPool = null;
			}
			
			System.gc();
			
	    } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Removes all existing data from the DW, and creates new empty tables
	 */
	public static void initTBs() {
		
		Connection connection = null;
		Statement stmt = null; 
		ResultSet rs   = null;
		
		try {
			
			// open connection
			connection = memoryConnectionPool.getConnection();
			stmt = connection.createStatement();
			
			// prepare SQL 
		    
		    String dropScenarioIterationStatisticTB = "DROP TABLE IF EXISTS scenario_iteration_statistic;";
		    String createScenarioIterationStatisticTB = "CREATE TABLE scenario_iteration_statistic (scenario_id VARCHAR(225), " +
		    		"generated_job FLOAT, submitted_job FLOAT, scheduling_job FLOAT, processing_job FLOAT, transferred_job FLOAT, " +
		    		"executed_job FLOAT, suspended_job FLOAT, failed_job FLOAT, " +
		    		"efficency FLOAT, makespane FLOAT, localSearch FLOAT, communitySearch FLOAT, accomplishment FLOAT);";
		    
		    String dropScenarioStatisticTB = "DROP TABLE IF EXISTS scenario_statistic;";
		    String createScenarioStatisticTB = "CREATE TABLE scenario_statistic (scenario_id VARCHAR(225), " +
		    		"generated_job FLOAT, submitted_job FLOAT, scheduling_job FLOAT, processing_job FLOAT, transferred_job FLOAT, " +
		    		"executed_job FLOAT, suspended_job FLOAT, failed_job FLOAT, " +
		    		"efficency FLOAT, makespane FLOAT, localSearch FLOAT, communitySearch FLOAT, accomplishment FLOAT);";
		    
		    String dropJobCenterTB = "DROP TABLE IF EXISTS job_center;";
		    String createJobCenterTB = "CREATE TABLE job_center (job_id VARCHAR(225), " +
		    		"original_node VARCHAR(225), executor_node VARCHAR(225), processing_node VARCHAR(225), previous_node VARCHAR(225)," +
		    		"status INT default -1);";
		    
		    String dropCommunityStatusTB = "DROP TABLE IF EXISTS community_status;";
		    String createCommunityStatusTB = "CREATE TABLE community_status (scenario_id VARCHAR(225), " +
		    		"exp_iteration INT, " +
		    		"generated_job FLOAT default 0, submitted_job FLOAT default 0, scheduling_job FLOAT, processing_job FLOAT, transferred_job FLOAT, " +
		    		"executed_job FLOAT, suspended_job FLOAT, failed_job FLOAT);";
		    
		    // simtime, arrived_job, scheduling_job, processing_job, transferred_job
		    // executed_job, suspended_job, failed_job, 
		    // rjc, ce, network_degree, cfc_degree
		    String dropLiveCommunityTB = "DROP TABLE IF EXISTS live_community;";
		    String createLiveCommunityTB = "CREATE TABLE live_community (simtime DOUBLE default 0, " +
		    		"arrived_job DOUBLE default 0, scheduling_job DOUBLE default 0, " +
		    		"processing_job DOUBLE default 0, transferred_job DOUBLE default 0, " +
		    		"executed_job DOUBLE default 0, suspended_job DOUBLE default 0, " +
		    		"failed_job DOUBLE default 0, " +
		    		"rjc DOUBLE default 0, ce DOUBLE default 0, " +
		    		"network_degree DOUBLE default 0, cfc_degree FLOAT default 0);";
		    
		    String dropScenarioCommunityTB = "DROP TABLE IF EXISTS scenario_community;";
		    String createScenarioCommunityTB = "CREATE TABLE scenario_community (simtime DOUBLE default 0, " +
		    		"arrived_job DOUBLE default 0, scheduling_job DOUBLE default 0, " +
		    		"processing_job DOUBLE default 0, transferred_job DOUBLE default 0, " +
		    		"executed_job DOUBLE default 0, suspended_job DOUBLE default 0, " +
		    		"failed_job DOUBLE default 0, " +
		    		"rjc DOUBLE default 0, ce DOUBLE default 0, " +
		    		"network_degree DOUBLE default 0, cfc_degree FLOAT default 0);";
		    
		    // execute SQL
		   	
		   	stmt.executeUpdate(dropCommunityStatusTB);
		   	stmt.executeUpdate(createCommunityStatusTB);
		   	
		   	stmt.executeUpdate(dropScenarioStatisticTB);
		   	stmt.executeUpdate(createScenarioStatisticTB);
		   	
		   	stmt.executeUpdate(dropScenarioIterationStatisticTB);
		   	stmt.executeUpdate(createScenarioIterationStatisticTB);
		   	
		   	stmt.executeUpdate(dropJobCenterTB);
		   	stmt.executeUpdate(createJobCenterTB);
		   	
		   	stmt.executeUpdate(dropLiveCommunityTB);
		   	stmt.executeUpdate(createLiveCommunityTB);
		   	
		   	stmt.executeUpdate(dropScenarioCommunityTB);
		   	stmt.executeUpdate(createScenarioCommunityTB);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			
			// close connection
			try {
				stmt.close();
				connection.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
		}
		
		String createScenariosTB = "";
		String testTableExist    = "";
		
		try {
			
			connection = memoryConnectionPool.getConnection();
			stmt = connection.createStatement();
			
			testTableExist = "select count(*) as counter from SCENARIOS";
		    createScenariosTB = "CREATE TABLE scenarios (id INT AUTO_INCREMENT PRIMARY KEY AUTO_INCREMENT, scenario_Id VARCHAR(225)," +
    			"number_of_magate INT, " +
    			"allow_community_execution INT DEFAULT 1, " +
    			"res_discovery_protocol VARCHAR(225)," +
    			"delegation_queue_limit INT DEFAULT 5, " +
    			"time_search_community INT DEFAULT 250, " +
    			"allow_multi_negotiation INT DEFAULT 0," +
    			"negotiation_limit INT DEFAULT 3, " +
    			"interaction_approach VARCHAR(225), " +
    			"activated INT DEFAULT 1, " +
    			"sequence INT);";
		    
		   	rs = stmt.executeQuery(testTableExist);
		    
		} catch (Exception e ) {
			
			try {
				stmt.executeUpdate(createScenariosTB);
				
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		} finally {
			// close connection
			try {
				
				if(rs != null) {
					rs.close();
				}
				stmt.close();
				connection.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
		}
			
	}
	
	
	/**
	 * Insert a statistic record for each concerning scenario
	 * @param scenarios
	 */
	public static void initScenarioStatisticTB(Vector<ExpScenario> scenarios) {
		
		if(scenarios == null || (scenarios.size() == 0)) {
			return;
		}
		
		PreparedStatement psInsertRecord = null;
		Connection conn = null;
		
		String sqlInsertRecord = "INSERT INTO scenario_statistic " +
				"(scenario_id) VALUES (?);";
		
		try {
			
			// get connection
			conn = memoryConnectionPool.getConnection();
			
			for (ExpScenario scenario : scenarios) {
				
				String scenarioId = scenario.getScenarioId();
				
				// Insert Table
				psInsertRecord = conn.prepareStatement(sqlInsertRecord);
				psInsertRecord.setString(1, scenarioId);
				psInsertRecord.executeUpdate();
				psInsertRecord.close();
				
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			
		} finally {
			
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Insert statistic data of each experiment scenario
	 */
	public static void insertScenarioIterationStatistic_DB(String scenario_id, 
			double generated_job, double submitted_job, double scheduling_job, double processing_job, double transferred_job,
			double executed_job, double suspended_job, double failed_job, 
			double efficency, double makespane, double localSearch, double communitySearch, double accomplishment) {
	    
		PreparedStatement psUpdateRecord = null;
		Connection conn = null;
		
		String updateCommunityStatusTB = "INSERT INTO scenario_iteration_statistic " +
				"(scenario_id, " +
				"generated_job, submitted_job, scheduling_job, processing_job, transferred_job, " +
				"executed_job, suspended_job, failed_job, " +
				"efficency, makespane, localSearch, communitySearch, accomplishment) " +
				"VALUES (?," +
				"?,?,?,?,?," +
				"?,?,?," +
				"?,?,?,?,?);";
		
		try {
			
			// get connection
			conn = memoryConnectionPool.getConnection();
			
			// Insert Table
			psUpdateRecord = conn.prepareStatement(updateCommunityStatusTB);
			
			psUpdateRecord.setString(1, scenario_id);
			psUpdateRecord.setDouble(2, generated_job);
			psUpdateRecord.setDouble(3, submitted_job);
			psUpdateRecord.setDouble(4, scheduling_job);
			psUpdateRecord.setDouble(5, processing_job);
			psUpdateRecord.setDouble(6, transferred_job);
			
			psUpdateRecord.setDouble(7, executed_job);
			psUpdateRecord.setDouble(8, suspended_job);
			psUpdateRecord.setDouble(9, failed_job);
			
			psUpdateRecord.setDouble(10, efficency);
			psUpdateRecord.setDouble(11, makespane);
			psUpdateRecord.setDouble(12, localSearch);
			psUpdateRecord.setDouble(13, communitySearch);
			psUpdateRecord.setDouble(14, accomplishment);
			
			
			psUpdateRecord.executeUpdate();
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			
		} finally {
			
			try {
				psUpdateRecord.close();
				conn.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	/**
	 * Update the scenario statistic-data, by analyzing and averaging scenario-iteration-data
	 */
	public static void updateScenarioStatistic_DB() {
		
		String selectRecord = "SELECT " +
			"scenario_id, " +
			"avg(generated_job) as generated_job, avg(submitted_job) as submitted_job, " +
			"avg(scheduling_job) as scheduling_job, avg(processing_job) as processing_job, " +
			"avg(transferred_job) as transferred_job, " +
			
			"avg(executed_job) as executed_job, avg(suspended_job) as suspended_job, " +
			"avg(failed_job) as failed_job, " +
			
			"avg(efficency) as efficency, avg(makespane) as makespane, avg(localSearch) as localSearch, " +
			"avg(communitySearch) as communitySearch, avg(accomplishment) as accomplishment " +
			
			" FROM scenario_iteration_statistic GROUP BY scenario_id;";

		
		String updateCommunityStatusTB = "UPDATE scenario_statistic SET " +
			"generated_job = ?, submitted_job = ?, scheduling_job = ?, processing_job = ?, transferred_job = ?, " +
			"executed_job = ?, suspended_job = ?, failed_job = ?, " +
			"efficency = ?, makespane = ?, localSearch = ?, " +
			"communitySearch = ?, accomplishment = ?" +
			" WHERE scenario_id = ?;";
		
		Statement stmt = null;	
		PreparedStatement psUpdateRecord = null;
		ResultSet rs = null;
		Connection conn = null;
			
		try {
				
			conn = memoryConnectionPool.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(selectRecord);
				
			while (rs.next()) {

				psUpdateRecord = conn.prepareStatement(updateCommunityStatusTB);
				
				psUpdateRecord.setDouble(1, rs.getDouble("generated_job"));
				psUpdateRecord.setDouble(2, rs.getDouble("submitted_job"));
				psUpdateRecord.setDouble(3, rs.getDouble("scheduling_job"));
				psUpdateRecord.setDouble(4, rs.getDouble("processing_job"));
				psUpdateRecord.setDouble(5, rs.getDouble("transferred_job"));
				
				psUpdateRecord.setDouble(6, rs.getDouble("executed_job"));
				psUpdateRecord.setDouble(7, rs.getDouble("suspended_job"));
				psUpdateRecord.setDouble(8, rs.getDouble("failed_job"));
				
				psUpdateRecord.setDouble(9, rs.getDouble("efficency"));
				psUpdateRecord.setDouble(10, rs.getDouble("makespane"));
				psUpdateRecord.setDouble(11, rs.getDouble("localSearch"));
				psUpdateRecord.setDouble(12, rs.getDouble("communitySearch"));
				psUpdateRecord.setDouble(13, rs.getDouble("accomplishment"));
				
				psUpdateRecord.setString(14, rs.getString("scenario_id"));
				
				psUpdateRecord.executeUpdate();
				psUpdateRecord.close();
			}
			
			
				
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			
		} finally {
			
			try {
				stmt.close();
				rs.close();
				conn.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
	
	
	
	
	/**
	 * Clean up(drop/create) table for preserving per-scenario community status data (scenario_community)
	 */
	public static void flushPerScenarioCommunityStatusTB() {
		
		Statement stmt = null;
		Connection conn = null;
        String dropScenarioCommunityTB = "DROP TABLE IF EXISTS scenario_community;";
	    String createScenarioCommunityTB = "CREATE TABLE scenario_community (simtime DOUBLE default 0, " +
	    		"arrived_job DOUBLE default 0, scheduling_job DOUBLE default 0, " +
	    		"processing_job DOUBLE default 0, transferred_job DOUBLE default 0, " +
	    		"executed_job DOUBLE default 0, suspended_job DOUBLE default 0, " +
	    		"failed_job DOUBLE default 0, " +
	    		"rjc DOUBLE default 0, ce DOUBLE default 0, " +
	    		"network_degree DOUBLE default 0, cfc_degree FLOAT default 0);";
	    
        try {
			
			// get connection
			conn = memoryConnectionPool.getConnection();
			
			// Insert Table
			stmt = conn.createStatement();
			stmt.executeUpdate(dropScenarioCommunityTB);
			stmt.executeUpdate(createScenarioCommunityTB);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			
		} finally {
			
			try {
				stmt.close();
				conn.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Clean up(drop/create) table for preserving per-iteration community status data (community_status, live_community)
	 * Then, initialize the record of each scenario-iteration in table: community_status
	 */
	public static void flushPerIterationCommunityStatusTB() {
		
		Statement stmt = null;
		Connection conn = null;
	    
	    String dropCommunityStatusTB = "DROP TABLE IF EXISTS community_status;";
	    String createCommunityStatusTB = "CREATE TABLE community_status (scenario_id VARCHAR(225), " +
	    		"exp_iteration INT, " +
	    		"generated_job FLOAT default 0, submitted_job FLOAT default 0, scheduling_job FLOAT, processing_job FLOAT, transferred_job FLOAT, " +
	    		"executed_job FLOAT, suspended_job FLOAT, failed_job FLOAT);";
        
        String dropLiveCommunityTB = "DROP TABLE IF EXISTS live_community;";
	    String createLiveCommunityTB = "CREATE TABLE live_community (simtime DOUBLE default 0, " +
	    		"arrived_job DOUBLE default 0, scheduling_job DOUBLE default 0, " +
	    		"processing_job DOUBLE default 0, transferred_job DOUBLE default 0, " +
	    		"executed_job DOUBLE default 0, suspended_job DOUBLE default 0, " +
	    		"failed_job DOUBLE default 0, " +
	    		"rjc DOUBLE default 0, ce DOUBLE default 0, " +
	    		"network_degree DOUBLE default 0, cfc_degree FLOAT default 0);";
        
        try {
			
			// get connection
			conn = memoryConnectionPool.getConnection();
			
			// Insert Table
			stmt = conn.createStatement();
			stmt.executeUpdate(dropCommunityStatusTB);
			stmt.executeUpdate(createCommunityStatusTB);
			stmt.executeUpdate(dropLiveCommunityTB);
			stmt.executeUpdate(createLiveCommunityTB);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			
		} finally {
			
			try {
				stmt.close();
				conn.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		// initialize the record of community_status
		insertCommunityStatus_DB();
	}
	
	
	/**
	 * Insert CommunityStatus for each [Scenario/Iteration] through time
	 * 
	 * @param maGateId
	 * @param maGateNickName
	 */
	public static void insertCommunityStatus_DB() {	
		PreparedStatement psInsertRecord = null;
		String sqlInsertRecord = "INSERT INTO community_status " +
				"(scenario_id, exp_iteration, generated_job) VALUES (?,?,?)";
		
		Connection conn = null;
		
		try {
			
			// get connection
			conn = memoryConnectionPool.getConnection();
			
			// Insert Table
			psInsertRecord = conn.prepareStatement(sqlInsertRecord);
			psInsertRecord.setString(1, MaGateParam.currentScenarioId);
			psInsertRecord.setInt(2, MaGateParam.currentExperimentIndex);
			psInsertRecord.setFloat(3, 0);
			psInsertRecord.executeUpdate();
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			
		} finally {
			
			try {
				psInsertRecord.close();
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Update latest status of the running community (per scenario, per experiment, per MaGate)
	 */
	public static void updateCommunityStatus_DB( 
			int generated_job, int submitted_job, int scheduling_job, int processing_job, int transferred_job, 
			int executed_job, int suspended_job, int failed_job) {
		
		GlobalStorage.count_sql2.incrementAndGet();
		
		PreparedStatement psUpdateRecord = null;
		Connection conn = null;
		
		String updateCommunityStatusTB = "UPDATE community_status SET " +
			"generated_job = ?, submitted_job = ?, scheduling_job = ?, processing_job = ?, transferred_job = ?, " +
			"executed_job = ?, suspended_job = ?, failed_job = ? " +
			" WHERE scenario_id = '" + MaGateParam.currentScenarioId + "' " + 
			" AND exp_iteration = '" + MaGateParam.currentExperimentIndex + "'; ";
		
		try {
			
			// get connection
			conn = memoryConnectionPool.getConnection();
			
			// Insert Table
			psUpdateRecord = conn.prepareStatement(updateCommunityStatusTB);
			
			
			psUpdateRecord.setInt(1, generated_job);
			psUpdateRecord.setInt(2, submitted_job);
			psUpdateRecord.setInt(3, scheduling_job);
			psUpdateRecord.setInt(4, processing_job);
			psUpdateRecord.setInt(5, transferred_job);
			
			psUpdateRecord.setInt(6, executed_job);
			psUpdateRecord.setInt(7, suspended_job);
			psUpdateRecord.setInt(8, failed_job);
			
			psUpdateRecord.executeUpdate();
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			
		} finally {
			
			try {
				psUpdateRecord.close();
				conn.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Record dynamic community status by simulation time
	 * 
	 * @param simtime
	 * @param arrived_job
	 * @param scheduling_job
	 * @param processing_job
	 * @param transferred_job
	 * @param executed_job
	 * @param suspended_job
	 * @param failed_job
	 * @param rjc
	 * @param ce
	 * @param network_degree
	 * @param cfc_degree
	 */
	public static void insertLiveCommunity_DB(double simtime, 
			double arrived_job, double scheduling_job, double processing_job, double transferred_job, 
			double executed_job, double suspended_job, double failed_job, 
			double rjc, double ce, double network_degree, double cfc_degree) {
		
		PreparedStatement psInsertRecord = null;
		String sqlInsertRecord = "INSERT INTO live_community " +
				"(simtime, arrived_job, scheduling_job, processing_job, transferred_job, " +
				"executed_job, suspended_job, failed_job, " +
				"rjc, ce, network_degree, cfc_degree) " +
				"VALUES " +
				"(?,?,?,?,?, " +
				"?,?,?," +
				"?,?,?,?)";
		
		Connection conn = null;
		
		try {
			
			// get connection
			conn = memoryConnectionPool.getConnection();
			
			// Insert Table
			psInsertRecord = conn.prepareStatement(sqlInsertRecord);
			psInsertRecord.setDouble(1, simtime);
			psInsertRecord.setDouble(2, arrived_job);
			psInsertRecord.setDouble(3, scheduling_job);
			psInsertRecord.setDouble(4, processing_job);
			psInsertRecord.setDouble(5, transferred_job);
			
			psInsertRecord.setDouble(6, executed_job);
			psInsertRecord.setDouble(7, suspended_job);
			psInsertRecord.setDouble(8, failed_job);
			
			psInsertRecord.setDouble(9, rjc);
			psInsertRecord.setDouble(10, ce);
			psInsertRecord.setDouble(11, network_degree);
			psInsertRecord.setDouble(12, cfc_degree);
			
			psInsertRecord.executeUpdate();
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			
		} finally {
			
			try {
				psInsertRecord.close();
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public static void insertScenarioCommunity_DB() {
		
		Connection connection = null;
		Statement stmt = null; 
		String sqlInsertRecord = "INSERT INTO scenario_community " +
				"(simtime, arrived_job, scheduling_job, processing_job, transferred_job, " +
				"executed_job, suspended_job, failed_job, " +
				"rjc, ce, network_degree, cfc_degree) " +
				"SELECT " +
				"simtime, AVG(arrived_job) AS arrived_job, " +
				"AVG(scheduling_job) as scheduling_job, AVG(processing_job) as processing_job, " +
				"AVG(transferred_job) as transferred_job, MAX(executed_job) AS executed_job, " +
				"MAX(suspended_job) AS suspended_job, MAX(failed_job) AS failed_job, " +
				"MAX(rjc) as rjc, MAX(ce) as ce, " +
				"MAX(network_degree) as network_degree, MAX(cfc_degree) as cfc_degree " +
				"FROM live_community GROUP BY simtime;";
		
//		String sqlInsertRecord = "INSERT INTO scenario_community " +
//		"(simtime, arrived_job, scheduling_job, processing_job, transferred_job, " +
//		"executed_job, suspended_job, failed_job, rjc, ce, network_degree, cfc_degree) " +
//		"SELECT " +
//		"simtime, arrived_job, scheduling_job, processing_job, transferred_job, " +
//		"executed_job, suspended_job, failed_job, rjc, ce, network_degree, cfc_degree " +
//		"FROM live_community;";
		
		try {
			
			connection = memoryConnectionPool.getConnection();
			stmt = connection.createStatement();
			
			stmt.executeUpdate(sqlInsertRecord);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			
		} finally {
			
			try {
				stmt.close();
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static JdbcConnectionPool getConnectionPool() {
		return memoryConnectionPool;
	}
	
	
	
	/**
	 * Reset the scenario database, triggered from the GUI startup
	 * @deprecated
	 */
	public static void initScenarioSet() {
		
		Connection connection = null;
		Statement stmt = null; 
		
		try {
			
			// open connection
			connection = memoryConnectionPool.getConnection();
			stmt = connection.createStatement();
			
			// prepare SQL 
		    
		    String dropScenariosTB = "DROP TABLE scenarios IF EXISTS;";
		    String createScenariosTB = "CREATE TABLE scenarios (id INT AUTO_INCREMENT PRIMARY KEY AUTO_INCREMENT, scenario_Id VARCHAR(225)," +
		    		"number_of_magate INT, " +
		    		"allow_community_execution INT DEFAULT 1, " +
		    		"res_discovery_protocol VARCHAR(225)," +
		    		"delegation_queue_limit INT DEFAULT 5, " +
		    		"time_search_community INT DEFAULT 250, " +
		    		"allow_multi_negotiation INT DEFAULT 0," +
		    		"negotiation_limit INT DEFAULT 3, " +
		    		"interaction_approach VARCHAR(225), " +
		    		"activated INT DEFAULT 1, " +
		    		"sequence INT);";
			
		    String str1  = "INSERT INTO SCENARIOS (SCENARIO_ID, NUMBER_OF_MAGATE , ALLOW_COMMUNITY_EXECUTION ," +
		    		"RES_DISCOVERY_PROTOCOL, DELEGATION_QUEUE_LIMIT, TIME_SEARCH_COMMUNITY, " +
		    		"ALLOW_MULTI_NEGOTIATION, NEGOTIATION_LIMIT, INTERACTION_APPROACH, " +
		    		"ACTIVATED, SEQUENCE) " +
		    		"VALUES " +
		    		"('[M10J100]:Neigh:Queue5:Re-Nego0', 10, 1," +
		    		"'Res_Discovery_From_Direct_Neighbors', 5, 0," +
		    		"0, 0, 'Interaction_AgreementCall'," +
		    		"1, 11);";
		    String str2  = "INSERT INTO SCENARIOS (SCENARIO_ID, NUMBER_OF_MAGATE , ALLOW_COMMUNITY_EXECUTION ," +
		    		"RES_DISCOVERY_PROTOCOL, DELEGATION_QUEUE_LIMIT, TIME_SEARCH_COMMUNITY, " +
		    		"ALLOW_MULTI_NEGOTIATION, NEGOTIATION_LIMIT, INTERACTION_APPROACH, " +
		    		"ACTIVATED, SEQUENCE) " +
		    		"VALUES " +
		    		"('[M10J100]:Search250:Queue5:Re-Nego0', 10, 1," +
		    		"'Res_Discovery_From_Community_Search', 5, 250," +
		    		"0, 0, 'Interaction_AgreementCall'," +
		    		"1, 12);";
		    String str3  = "INSERT INTO SCENARIOS (SCENARIO_ID, NUMBER_OF_MAGATE , ALLOW_COMMUNITY_EXECUTION ," +
		    		"RES_DISCOVERY_PROTOCOL, DELEGATION_QUEUE_LIMIT, TIME_SEARCH_COMMUNITY, " +
		    		"ALLOW_MULTI_NEGOTIATION, NEGOTIATION_LIMIT, INTERACTION_APPROACH, " +
		    		"ACTIVATED, SEQUENCE)" +
		    		"VALUES " +
		    		"('[M10J100]:Search500:Queue5:Re-Nego0', 10, 1," +
		    		"'Res_Discovery_From_Community_Search', 5, 500," +
		    		"0, 0, 'Interaction_AgreementCall'," +
		    		"0, 13);";
		    String str4  = "INSERT INTO SCENARIOS (SCENARIO_ID, NUMBER_OF_MAGATE , ALLOW_COMMUNITY_EXECUTION ," +
		    		"RES_DISCOVERY_PROTOCOL, DELEGATION_QUEUE_LIMIT, TIME_SEARCH_COMMUNITY, " +
		    		"ALLOW_MULTI_NEGOTIATION, NEGOTIATION_LIMIT, INTERACTION_APPROACH, " +
		    		"ACTIVATED, SEQUENCE) " +
		    		"VALUES " +
		    		"('[M10J100]:Search1000:Queue5:Re-Nego0', 10, 1," +
		    		"'Res_Discovery_From_Community_Search', 5, 1000," +
		    		"0, 0, 'Interaction_AgreementCall'," +
		    		"0, 14);";
		    String str5  = "INSERT INTO SCENARIOS (SCENARIO_ID, NUMBER_OF_MAGATE , ALLOW_COMMUNITY_EXECUTION ," +
		    		"RES_DISCOVERY_PROTOCOL, DELEGATION_QUEUE_LIMIT, TIME_SEARCH_COMMUNITY, " +
		    		"ALLOW_MULTI_NEGOTIATION, NEGOTIATION_LIMIT, INTERACTION_APPROACH, " +
		    		"ACTIVATED, SEQUENCE) " +
		    		"VALUES " +
		    		"('[M10J100]:Search250:Queue10:Re-Nego0', 10, 1," +
		    		"'Res_Discovery_From_Community_Search', 10, 250," +
		    		"0, 0, 'Interaction_AgreementCall'," +
		    		"0, 15);";
		    String str6  = "INSERT INTO SCENARIOS (SCENARIO_ID, NUMBER_OF_MAGATE , ALLOW_COMMUNITY_EXECUTION ," +
		    		"RES_DISCOVERY_PROTOCOL, DELEGATION_QUEUE_LIMIT, TIME_SEARCH_COMMUNITY, " +
		    		"ALLOW_MULTI_NEGOTIATION, NEGOTIATION_LIMIT, INTERACTION_APPROACH, " +
		    		"ACTIVATED, SEQUENCE) " +
		    		"VALUES " +
		    		"('[M10J100]:Search250:Queue5:Re-Nego3', 10, 1," +
		    		"'Res_Discovery_From_Community_Search', 5, 250," +
		    		"1, 3, 'Interaction_AgreementCall'," +
		    		"0, 16);";
		    String str7  = "INSERT INTO SCENARIOS (SCENARIO_ID, NUMBER_OF_MAGATE , ALLOW_COMMUNITY_EXECUTION ," +
		    		"RES_DISCOVERY_PROTOCOL, DELEGATION_QUEUE_LIMIT, TIME_SEARCH_COMMUNITY, " +
		    		"ALLOW_MULTI_NEGOTIATION, NEGOTIATION_LIMIT, INTERACTION_APPROACH, " +
		    		"ACTIVATED, SEQUENCE) " +
		    		"VALUES " +
		    		"('[M100J100]:Neigh:Queue5:Re-Nego0', 100, 1," +
		    		"'Res_Discovery_From_Direct_Neighbors', 5, 0," +
		    		"0, 0, 'Interaction_AgreementCall'," +
		    		"0, 21);";
		    String str8  = "INSERT INTO SCENARIOS (SCENARIO_ID, NUMBER_OF_MAGATE , ALLOW_COMMUNITY_EXECUTION ," +
		    		"RES_DISCOVERY_PROTOCOL, DELEGATION_QUEUE_LIMIT, TIME_SEARCH_COMMUNITY, " +
		    		"ALLOW_MULTI_NEGOTIATION, NEGOTIATION_LIMIT, INTERACTION_APPROACH, " +
		    		"ACTIVATED, SEQUENCE) " +
		    		"VALUES " +
		    		"('[M100J100]:Search250:Queue5:Re-Nego0', 100, 1," +
		    		"'Res_Discovery_From_Community_Search', 5, 250," +
		    		"0, 0, 'Interaction_AgreementCall'," +
		    		"0, 22);";
		    String str9  = "INSERT INTO SCENARIOS (SCENARIO_ID, NUMBER_OF_MAGATE , ALLOW_COMMUNITY_EXECUTION ," +
		    		"RES_DISCOVERY_PROTOCOL, DELEGATION_QUEUE_LIMIT, TIME_SEARCH_COMMUNITY, " +
		    		"ALLOW_MULTI_NEGOTIATION, NEGOTIATION_LIMIT, INTERACTION_APPROACH, " +
		    		"ACTIVATED, SEQUENCE) " +
		    		"VALUES " +
		    		"('[M100J100]:Search500:Queue5:Re-Nego0', 100, 1," +
		    		"'Res_Discovery_From_Community_Search', 5, 500," +
		    		"0, 0, 'Interaction_AgreementCall'," +
		    		"0, 23);";
		    String str10 = "INSERT INTO SCENARIOS (SCENARIO_ID, NUMBER_OF_MAGATE , ALLOW_COMMUNITY_EXECUTION ," +
		    		"RES_DISCOVERY_PROTOCOL, DELEGATION_QUEUE_LIMIT, TIME_SEARCH_COMMUNITY, " +
		    		"ALLOW_MULTI_NEGOTIATION, NEGOTIATION_LIMIT, INTERACTION_APPROACH, " +
		    		"ACTIVATED, SEQUENCE) " +
		    		"VALUES " +
		    		"('[M100J100]:Search1000:Queue5:Re-Nego0', 100, 1," +
		    		"'Res_Discovery_From_Community_Search', 5, 1000," +
		    		"0, 0, 'Interaction_AgreementCall'," +
		    		"0, 24);";
		    String str11 = "INSERT INTO SCENARIOS (SCENARIO_ID, NUMBER_OF_MAGATE , ALLOW_COMMUNITY_EXECUTION ," +
		    		"RES_DISCOVERY_PROTOCOL, DELEGATION_QUEUE_LIMIT, TIME_SEARCH_COMMUNITY, " +
		    		"ALLOW_MULTI_NEGOTIATION, NEGOTIATION_LIMIT, INTERACTION_APPROACH, " +
		    		"ACTIVATED, SEQUENCE) " +
		    		"VALUES " +
		    		"('[M100J100]:Search250:Queue10:Re-Nego0', 100, 1," +
		    		"'Res_Discovery_From_Community_Search', 10, 250," +
		    		"0, 0, 'Interaction_AgreementCall'," +
		    		"0, 25);";
		    String str12 = "INSERT INTO SCENARIOS (SCENARIO_ID, NUMBER_OF_MAGATE , ALLOW_COMMUNITY_EXECUTION ," +
		    		"RES_DISCOVERY_PROTOCOL, DELEGATION_QUEUE_LIMIT, TIME_SEARCH_COMMUNITY, " +
		    		"ALLOW_MULTI_NEGOTIATION, NEGOTIATION_LIMIT, INTERACTION_APPROACH, " +
		    		"ACTIVATED, SEQUENCE) " +
		    		"VALUES " +
		    		"('[M100J100]:Search250:Queue5:Re-Nego3', 100, 1," +
		    		"'Res_Discovery_From_Community_Search', 5, 250," +
		    		"1, 3, 'Interaction_AgreementCall'," +
		    		"0, 26);";
		    String str13 = "INSERT INTO SCENARIOS (SCENARIO_ID, NUMBER_OF_MAGATE , ALLOW_COMMUNITY_EXECUTION ," +
		    		"RES_DISCOVERY_PROTOCOL, DELEGATION_QUEUE_LIMIT, TIME_SEARCH_COMMUNITY, " +
		    		"ALLOW_MULTI_NEGOTIATION, NEGOTIATION_LIMIT, INTERACTION_APPROACH, " +
		    		"ACTIVATED, SEQUENCE) " +
		    		"VALUES " +
		    		"('[M200J100]:Neigh:Queue5:Re-Nego0', 200, 1," +
		    		"'Res_Discovery_From_Direct_Neighbors', 5, 0," +
		    		"0, 0, 'Interaction_AgreementCall'," +
		    		"0, 31);";
		    String str14 = "INSERT INTO SCENARIOS (SCENARIO_ID, NUMBER_OF_MAGATE , ALLOW_COMMUNITY_EXECUTION ," +
		    		"RES_DISCOVERY_PROTOCOL, DELEGATION_QUEUE_LIMIT, TIME_SEARCH_COMMUNITY, " +
		    		"ALLOW_MULTI_NEGOTIATION, NEGOTIATION_LIMIT, INTERACTION_APPROACH, " +
		    		"ACTIVATED, SEQUENCE) " +
		    		"VALUES " +
		    		"('[M200J100]:Search250:Queue5:Re-Nego0', 200, 1," +
		    		"'Res_Discovery_From_Community_Search', 5, 250," +
		    		"0, 0, 'Interaction_AgreementCall'," +
		    		"0, 32);";
		    String str15 = "INSERT INTO SCENARIOS (SCENARIO_ID, NUMBER_OF_MAGATE , ALLOW_COMMUNITY_EXECUTION ," +
		    		"RES_DISCOVERY_PROTOCOL, DELEGATION_QUEUE_LIMIT, TIME_SEARCH_COMMUNITY, " +
		    		"ALLOW_MULTI_NEGOTIATION, NEGOTIATION_LIMIT, INTERACTION_APPROACH, " +
		    		"ACTIVATED, SEQUENCE) " +
		    		"VALUES " +
		    		"('[M200J100]:Search500:Queue5:Re-Nego0', 200, 1," +
		    		"'Res_Discovery_From_Community_Search', 5, 500," +
		    		"0, 0, 'Interaction_AgreementCall'," +
		    		"0, 33);";
		    String str16 = "INSERT INTO SCENARIOS (SCENARIO_ID, NUMBER_OF_MAGATE , ALLOW_COMMUNITY_EXECUTION ," +
		    		"RES_DISCOVERY_PROTOCOL, DELEGATION_QUEUE_LIMIT, TIME_SEARCH_COMMUNITY, " +
		    		"ALLOW_MULTI_NEGOTIATION, NEGOTIATION_LIMIT, INTERACTION_APPROACH, " +
		    		"ACTIVATED, SEQUENCE) " +
		    		"VALUES " +
		    		"('[M200J100]:Search1000:Queue5:Re-Nego0', 200, 1," +
		    		"'Res_Discovery_From_Community_Search', 5, 1000," +
		    		"0, 0, 'Interaction_AgreementCall'," +
		    		"0, 34);";
		    String str17 = "INSERT INTO SCENARIOS (SCENARIO_ID, NUMBER_OF_MAGATE , ALLOW_COMMUNITY_EXECUTION ," +
		    		"RES_DISCOVERY_PROTOCOL, DELEGATION_QUEUE_LIMIT, TIME_SEARCH_COMMUNITY, ALLOW_MULTI_NEGOTIATION, NEGOTIATION_LIMIT, INTERACTION_APPROACH, " +
		    		"ACTIVATED, SEQUENCE) " +
		    		"VALUES " +
		    		"('[M200J100]:Search250:Queue10:Re-Nego0', 200, 1," +
		    		"'Res_Discovery_From_Community_Search', 10, 250," +
		    		"0, 0, 'Interaction_AgreementCall'," +
		    		"0, 35);";
		    String str18 = "INSERT INTO SCENARIOS (SCENARIO_ID, NUMBER_OF_MAGATE , ALLOW_COMMUNITY_EXECUTION ," +
		    		"RES_DISCOVERY_PROTOCOL, DELEGATION_QUEUE_LIMIT, TIME_SEARCH_COMMUNITY, " +
		    		"ALLOW_MULTI_NEGOTIATION, NEGOTIATION_LIMIT, INTERACTION_APPROACH, " +
		    		"ACTIVATED, SEQUENCE) " +
		    		"VALUES " +
		    		"('[M200J100]:Search250:Queue5:Re-Nego3', 200, 1," +
		    		"'Res_Discovery_From_Community_Search', 5, 250," +
		    		"1, 3, 'Interaction_AgreementCall'," +
		    		"0, 36);";
		    
		    // execute SQL
		   	
		   	stmt.executeUpdate(dropScenariosTB);
		   	stmt.executeUpdate(createScenariosTB);
		   	
		   	stmt.executeUpdate(str1);
		   	stmt.executeUpdate(str2);
		   	stmt.executeUpdate(str3);
		   	stmt.executeUpdate(str4);
		   	stmt.executeUpdate(str5);
		   	stmt.executeUpdate(str6);
		   	stmt.executeUpdate(str7);
		   	stmt.executeUpdate(str8);
		   	stmt.executeUpdate(str9);
		   	stmt.executeUpdate(str10);
		   	stmt.executeUpdate(str11);
		   	stmt.executeUpdate(str12);
		   	stmt.executeUpdate(str13);
		   	stmt.executeUpdate(str14);
		   	stmt.executeUpdate(str15);
		   	stmt.executeUpdate(str16);
		   	stmt.executeUpdate(str17);
		   	stmt.executeUpdate(str18);
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			
			// close connection
			try {
				stmt.close();
				connection.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
		}
		
	}
	
	/**
	 * @deprecated 
	 */
	public static Vector<ExpScenario> getExperimentScenariosFromDB() {
		
		Vector<ExpScenario> experimentScenarios = new Vector<ExpScenario>();
		
		String selectRecord = "SELECT id, scenario_Id, number_of_magate, allow_community_execution, res_discovery_protocol, " +
				"delegation_queue_limit, time_search_community, allow_multi_negotiation, negotiation_limit, interaction_approach " +
				" FROM scenarios WHERE activated = 1 ORDER BY sequence;";
		
		Statement stmt          = null;	
		ResultSet rs            = null;
		Connection conn         = null;
		ExpScenario scenario = null;
		
		try {
			
			conn = memoryConnectionPool.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(selectRecord);
				
			while (rs.next()) {

				int totalNumberOfJobs = rs.getInt("number_of_magate") * 100;
				scenario = new ExpScenario(rs.getString("scenario_Id"), rs.getInt("number_of_magate"), 1, rs.getInt("number_of_magate"), totalNumberOfJobs,
						rs.getString("res_discovery_protocol"), rs.getInt("delegation_queue_limit"), rs.getInt("time_search_community"), 
						rs.getString("interaction_approach"), 
						new ConcurrentHashMap<String, String>());
				
				experimentScenarios.add(scenario);
				
			}
				
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			
		} finally {
			
			try {
				stmt.close();
				rs.close();
				conn.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return experimentScenarios;
	}
	
}



