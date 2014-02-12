package ch.hefr.gridgroup.magate.env;

import gridsim.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class MaGateToolkit {
	
	private static Log log = LogFactory.getLog(MaGateToolkit.class);
	
	/**
	 * Transferring data from storage-specific format(Atomic-*) to application-specific(program based)
	 */
	public static double convertAtomicLongToDouble(AtomicLong inputAL) {
		double db = Double.longBitsToDouble(inputAL.longValue());
		return db;
	}
	
	/**
	 * Transferring data from application-specific(program based) to storage-specific format(Atomic-*) 
	 */
	public static AtomicLong convertDoubleToAtomicLong(double inputDouble) {
		AtomicLong al = new AtomicLong(Double.doubleToLongBits(inputDouble));
		return al;
	}
	
	/**
     * Reads a text file one line at the time
     * @param fileName   a file name
     * @return <tt>true</tt> if reading a file is successful, <tt>false</tt>
     *         otherwise.
     * @pre fileName != null
     * @post $none
     * @since GridSim
     */
    public static BufferedReader readFile(String fileName)
    {
        BufferedReader reader = null;
        try
        {
            FileInputStream file = new FileInputStream(fileName);
            InputStreamReader input = new InputStreamReader(file);
            reader = new BufferedReader(input);
        }
        
        catch (FileNotFoundException f)
        {
            log.error(": Error - the file was not found: " + f.getMessage());
        }
        catch (IOException e)
        {
        	log.error(": Error - an IOException occurred: " + e.getMessage());
        }
        
        return reader;
    }
    
    /**
     * Reads a text file one line at the time
     * @param fileObject   a file object
     * @return <tt>true</tt> if reading a file is successful, <tt>false</tt>
     *         otherwise.
     * @pre fileName != null
     * @post $none
     * @since GridSim
     */
    public static BufferedReader readFile(File fileObject)
    {
        BufferedReader reader = null;
        try
        {
            FileInputStream file = new FileInputStream(fileObject);
            InputStreamReader input = new InputStreamReader(file);
            reader = new BufferedReader(input);
        }
        
        catch (FileNotFoundException f)
        {
            log.error(": Error - the file was not found: " + f.getMessage());
        }
        catch (IOException e)
        {
        	log.error(": Error - an IOException occurred: " + e.getMessage());
        }
        
        return reader;
    }
    
    public static void closeFile(BufferedReader br) {
        try{
            br.close();
            
        }catch(IOException ioe){
        	log.error("Fail to close file!");
        }
    }

    /**
     * Reads a gzip file one line at the time
     * @param fileName   a gzip file name
     * @return <tt>true</tt> if reading a file is successful, <tt>false</tt>
     *         otherwise.
     * @pre fileName != null
     * @post $none
     * @since GridSim
     */
    public static BufferedReader readGZIPFile(String fileName)
    {
        BufferedReader reader = null;
        try
        {
            FileInputStream file = new FileInputStream(fileName);
            GZIPInputStream gz =  new GZIPInputStream(file);
            InputStreamReader input = new InputStreamReader(gz);
            reader = new BufferedReader(input);

        }
        catch (FileNotFoundException f)
        {
        	log.error(": Error - the file was not found: " + f.getMessage());
        }
        catch (IOException e)
        {
        	log.error(": Error - an IOException occurred: " + e.getMessage());
        }

        return reader;
    }
    
	/**
	 * Output a random string, only for Test, NO FOR PRODUCTION
	 * @return
	 */
	public static String randomString(){
		Random r = new Random();
		String token = Long.toString(Math.abs(r.nextLong()), 36);
		
		return token;
	}
	
	
	/**
	 * @param milSecond
	 */
	public static void sleep(int milSecond){
		
		try {
			Thread.sleep(milSecond);
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** 
	 * Deletes all files and subdirectories under dir.
	 * Returns true if all deletions were successful.
	 * If a deletion fails, the method stops attempting to delete and returns false.
     * 
     * @param dir
     * @return
     */
    public static boolean deleteDir(File dir) {
    	
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        
        // The directory is now empty so delete it
        return dir.delete();
    }
    
    public static File getWorkLoadTraceDir() {
    	File dir = new File(MaGateProfile.inputLocation(), MaGateProfile.workLoadTraceLocatoin());
    	return dir;
    }
    
//	public static String getWorkingDir(){
//		return System.getProperty("user.dir");
//	}
	
	public static String getTime(){
		SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd" + " " + "hh:mm:ss");
		String datetime = tempDate.format(new java.util.Date());
		
		return datetime;
	}
    
	public static void writeFile(String s, String value)
    	throws IOException {

        PrintWriter pw = new PrintWriter(new FileWriter(s, true));
        pw.println(value);
        pw.close();
    }
	
	public static double[] arrayTransfer(String[] inputArray) {
		
		double[] outputArray = new double[inputArray.length];
		
		for(int i = 0; i < inputArray.length; i++) {
			outputArray[i] = Double.parseDouble(inputArray[i]);
		}
		
		return outputArray;
	}
	
    /// --- --- --- scenario related methods --- --- --- 
	
	
	/**
	 * Generate a output file for recording experiment result
	 * @param file: start with "/"
	 * @param value
	 */
	public static void generateResult(String file) {
		
		String outputDirName = MaGateProfile.outputLocation() + MaGateProfile.resultLocation();
		
		File outputDir = new File(outputDirName);
		if(!outputDir.exists()) {
			outputDir.mkdirs();
		}
			
		File outputFile = new File(outputDirName, file);
		 
		boolean dataFileExist = outputFile.exists();
		 
		if (!dataFileExist) {
			try {
				boolean success = outputFile.createNewFile();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void generateOrFindFile(String file) {
		
		
		
		String outputDirName = MaGateProfile.outputLocation() + MaGateProfile.resultLocation();
		
		File outputDir = new File(outputDirName);
		if(!outputDir.exists()) {
			outputDir.mkdirs();
		}
		
		File outputFile = new File(outputDirName, file);
		
		 
		boolean dataFileExist = outputFile.exists();
		 
		if (!dataFileExist) {
			try {
				System.getProperty("user.dir");
				outputFile.mkdirs();
				boolean success = outputFile.createNewFile();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Write output file for recording experiment result
	 * @param file: start with "/"
	 * @param value
	 */
	public static void writeResult(String file, String value) {
		
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
	
	/**
	 * Generate a output file for recording experiment plot-result
	 * @param file: start with "/"
	 * @param value
	 */
	public static void generatePlotResult(String file) {
		
		String outputDir = MaGateProfile.outputLocation() + MaGateProfile.plotResultLocation();
		File outputFile = new File(outputDir, file);
		 
		boolean dataFileExist = outputFile.exists();
		 
		if (!dataFileExist) {
			try {
				boolean success = outputFile.createNewFile();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Check whether the specific file exists
	 * @param file
	 */
	public static boolean isExistPlotResult(String fileName) {
		String dir = MaGateProfile.outputLocation() + MaGateProfile.plotResultLocation();
		File file = new File(dir, fileName);
		return file.exists();
	}
	
	/**
	 * Write output file for recording experiment plot-result
	 * @param file: start with "/"
	 * @param value
	 */
	public static void writePlotResult(String file, String value) {
		
		String outputFile = MaGateProfile.outputLocation() + MaGateProfile.plotResultLocation() + file;
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
	
	/**
	 * Delete output file for recording experiment plot-result
	 * @param file: start with "/"
	 * @param value
	 */
	public static void deletePlotResult(String file) {
		String existFileName = MaGateProfile.outputLocation() + MaGateProfile.plotResultLocation() + file;
		File existFile = new File(existFileName);
		existFile.delete();
	}
	
    public static void cleanMaGateSim() {
    	cleanDWDir();
    	File GridSim_stat = new File(System.getProperty("user.dir"), "/GridSim_stat.txt");
    	GridSim_stat.delete();
    	
    	// Create an empty GridSim_stat.txt
    	try {
			GridSim_stat.createNewFile();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    public static void createDWFile(String fileName) {
		
		createDWDir();
		 
		File dwFile = new File(getDWDir(), fileName);
		 
		boolean dataFileExist = dwFile.exists();
		 
		if (!dataFileExist) {
			try {
				boolean success = dwFile.createNewFile();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		 
		 
	}

    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    public static void cleanDWDir() {
    	
    	File dir = getDWDir();
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                deleteDir(new File(dir, children[i]));
            }
        }
        
    }
    
    public static void createDWDir() {
    	
		File dwPathDir       = getDWDir();
		boolean dataDirExist = dwPathDir.exists();
		
		// Only need to create DW dir if not exists
		if (!dataDirExist) {
			dwPathDir.mkdir();
		} 
    }

    public static File getDWDir() {
    	File dwDir = new File(MaGateProfile.outputLocation(), MaGateProfile.dwLocation());
    	return dwDir;
    }
    
	public static int[] getRandom(int sizeofOutputArray, int sizeofRange) {
		
		HashMap<Integer, Integer> outputMap = new HashMap<Integer, Integer>();
		
		Random r = new Random(System.currentTimeMillis());
		
		while(outputMap.size() < sizeofOutputArray) {
			
			int randomInt = r.nextInt(sizeofRange);
			int randomInteger = new Integer(randomInt);
			
			if(!outputMap.containsKey(randomInteger)) {
				outputMap.put(randomInteger, randomInteger);
			}
			
		}
		
		Collection<Integer> outputCollection = outputMap.values();
		
		int[] output = new int[sizeofOutputArray];
		int index = 0;
		
		for(Integer id : outputCollection) {
			output[index] = id;
			index++;
		}
		
		return output;
	}
}

