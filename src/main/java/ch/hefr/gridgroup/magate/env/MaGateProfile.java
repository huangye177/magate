package ch.hefr.gridgroup.magate.env;

import java.util.HashMap;

import gridsim.ResourceCharacteristics;

import org.ggf.schemas.jsdl.x2005.x11.jsdl.OperatingSystemTypeEnumeration;

import ch.hefr.gridgroup.magate.toolkit.OSDetector;

public class MaGateProfile {

	/** default MIPS
	 * Intel Pentium III 	1,354 MIPS at 500 MHz 	02708 2.708 MIPS/MHz
	 * ref: http://en.wikipedia.org/wiki/Instructions_per_second
	 */
	
	public static final int numOfMachine_perResource  = 1;      // default number of Machine for each resource
	public static final int numOfPE_eachResource      = 32;     // default
	public static final double numOfPE_perResource_minRage = 0.5;
	public static final double numOfPE_perResource_maxRage = 0.0;
	public static final int numOfPE_requestedByBadJob = 32;     // default
	public static final int numOfPE_requestedByJob = 4;     // default
	public static final double job_minRange   = 0.5;
	public static final double job_maxRange   = 0.0;
	public static final double job_badRate = 0.0;
	
	public static final int peMIPS   = 1000;
	public static final String res_archType = "x86";
	public static final String job_archType = "x86";
	public static final String osType    = OperatingSystemTypeEnumeration.LINUX.toString();
	public static final double timeZone  = 2.0;
	public static final double useCost   = 1.0;
	public static final double bandWidth = 1e8;    // bandwidth bits/sec: 100 Mbps
	public static final int bandDelay       = 10;     // propagation delay in millisecond 
	public static final int MTU             = 1024;   // max transmission unit in byte; default: 1 M
	public static final int packetSize      = 500;    // rough estimation of packeit size in bytes
	public static final int intSize         = 12;     // size of integer object included overhead
	public static final int jobArrival      = 43200;    // job arrival time
	public static final int jobArrivalDelay = 100; // for synchronization job arrival
	public static final double peakLoad    = 0.0;  // default resource load during peak hour
	public static final double offPeakLoad = 0.0;  // default resource load during off-peak hr
	public static final double holidayLoad = 0.0;  // default resource load during holiday
	// default scheduling policy: time shared
	public static final int allocationPolicy = ResourceCharacteristics.SPACE_SHARED;
	
	public static String inputSetupFileLocation() {
		
		if(OSDetector.isWindows()) {
			return System.getProperty("user.dir") + "\\magateinput\\config\\magate.properties";
		} else {
			return "./magateinput/config/magate.properties";
		} 
	}
	
	public static String outputLocation() {
		if(OSDetector.isWindows()) {
			return System.getProperty("user.dir") + "\\target\\magateoutput\\";
		} else {
			return "./target/magateoutput/";
		}
	}
	
	public static String chartLocation() {
		if(OSDetector.isWindows()) {
			return System.getProperty("user.dir") + "\\target\\magateoutput\\chartresult";
		} else {
			return "./target/magateoutput/chartresult";
		}
	}
	
	public static String inputLocation() {
		if(OSDetector.isWindows()) {
			return System.getProperty("user.dir") + "\\magateinput\\";
		} else {
			return "./magateinput/";
		}
	}
	
	public static String dwLocation() {
		if(OSDetector.isWindows()) {
			return "dw\\";
		} else {
			return "dw/";
		}
	}
	
	public static String logLocation() {
		if(OSDetector.isWindows()) {
			return  "logs\\";
		} else {
			return  "logs/";
		}
	}
	
	public static String resultLocation() {
		if(OSDetector.isWindows()) {
			return "results\\";
		} else {
			return "results/";
		}
	}
	
	public static String plotResultLocation() {
		if(OSDetector.isWindows()) {
			return "plotresults\\";
		} else {
			return "plotresults/";
		}
	}
	
	public static String workLoadTraceLocatoin() {
		if(OSDetector.isWindows()) {
			return "workloadtrace\\";
		} else {
			return "workloadtrace/";
		}
	}
	
//	public static final String inputSetupFileLocation = "magateinput/config/magate.properties";
//	public static final String outputLocation = "target/magateoutput/";
//	public static final String chartLocation = "target/magateoutput/chartresult";
//	public static final String inputLocation = "magateinput/";
//	public static final String dwLocation = "dw/";
//	public static final String logLocation = "logs/";
//	public static final String resultLocation = "results/";
//	public static final String plotResultLocation = "plotresults/";
//	public static final String workLoadTraceLocatoin = "workloadtrace/";
	
	
//	public static final String inputSetupFileLocation = System.getProperty("user.dir") + "/magateinput/config/magate.properties";
//	public static final String outputLocation = System.getProperty("user.dir") + "/target/magateoutput/";
//	public static final String chartLocation = System.getProperty("user.dir") + "/target/magateoutput/chartresult";
//	public static final String inputLocation = System.getProperty("user.dir") + "/magateinput/";
	
	// default job length expressed in MI (Millions Instruction): (jobSize * MIPSRating ?)
	public static final double jobSize   = 1e6;
	public static final long   jobInputSize  = 100;
	public static final long   jobOutputSize = 100;
	/** estimated computational length, it stands for how many seconds a CPU (with "estimatedMachine' MIPS") need to execute the job */
	public static final double estimatedSec = 2000;
	/** MIPS rating of a machine used to compute estimated comp. length */
	public static final double estimatedMIPS = 1000;
	public static final double Threshold = 500;
	public static final double Limit_Neg_Input_Efficient = 0.3;
	
	public static HashMap<String, String> workload = new HashMap<String, String>();
	
	static {
		MaGateProfile.workload.put("UserGUI", "UserGUI");
		MaGateProfile.workload.put("User", "User");
		MaGateProfile.workload.put("AuverGrid", "AuverGrid");
		MaGateProfile.workload.put("SHARCNET", "SHARCNET");
		MaGateProfile.workload.put("Grid5000", "Grid5000");
		MaGateProfile.workload.put("NorduGrid", "NorduGrid");
	}
	
	public static final String gwaCustomized    = "gwaCustomized";
	public static final String gwaAuverGrid     = "gwaAuverGrid";
	public static final String gwaSHARCNET      = "gwaSHARCNET";
	public static final String gwaGrid5000      = "gwaGrid5000";
	public static final String gwaNorduGrid     = "gwaNorduGrid";
	
	/**@deprecated */
	public static final String gwaCustomizedGUI = "gwaCustomizedGUI";
	
	public static final String gwaLocationAuverGrid = "jdbc:sqlite:magateinput/workloadtrace/GWA-AuverGrid.db3";
	public static final String gwaLocationSHARCNET  = "jdbc:sqlite:magateinput/workloadtrace/GWA-SHARCNET.db3";
	public static final String gwaLocationGrid5000  = "jdbc:sqlite:magateinput/workloadtrace/GWA-Grid5000.db3";
	public static final String gwaLocationNorduGrid = "jdbc:sqlite:magateinput/workloadtrace/GWA-NorduGrid.db3";

}
