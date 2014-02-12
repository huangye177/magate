package ch.hefr.gridgroup.magate.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.env.MaGateProfile;


public class SystemSetup {

	private static Log log = LogFactory.getLog(SystemSetup.class);
	
	public static void getProperties() {
		
		try {
			File f = new File(MaGateProfile.inputSetupFileLocation()); 
			
			if (f.exists() && f.isFile()) { 
				System.out.println("READING SYSTEM SETUP FILE");
			}
			
			Properties properties = new Properties();  
			InputStream is = new FileInputStream(f); 
			
			properties.load(is);
			String selectedJobload = properties.getProperty("selectedJobload");
			String selectedWorkload = properties.getProperty("selectedWorkload");
			
			System.out.println("[selectedJobload, selectedWorkload]: " + selectedJobload + "; " + selectedWorkload);
			
		}  catch (FileNotFoundException e) {
			System.out.println("WARNING! NO SYSTEM SETUP FILE FOUND (" + MaGateProfile.inputSetupFileLocation() + 
					")" + e.getMessage());
			System.exit(0);
			
		} catch (IOException e1) {
			System.out.println("SYSTEM SETUP FILE IO ERROR (" + MaGateProfile.inputSetupFileLocation() + 
					")" + e1.getMessage());
			System.exit(0);
			
		} catch (Exception e2) {
			e2.printStackTrace();
			System.exit(0);
			
		}
		
	}
}
