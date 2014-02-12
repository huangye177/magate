package ch.hefr.gridgroup.magate.env;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.OperatingSystemType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.RangeValueType;
import org.ogf.graap.wsag.api.types.TemplateType;
import org.ogf.schemas.graap.wsAgreement.AgreementTemplateType;
import org.ogf.schemas.graap.wsAgreement.ServiceDescriptionTermType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.hefr.gridgroup.magate.model.JobInfo;
import ch.hefr.gridgroup.magate.model.MaGateAgreement;
import ch.hefr.gridgroup.magate.xsd.MaGateHostType;
import ch.hefr.gridgroup.magate.xsd.MagateHostDocument;
import gridsim.GridSim;

/**
 * To adaptor parameters used in different infrastructure, 
 * e.g. GridSim simulation, real clusters.
 * @author ye huang
 */
public class MaGateMediator {

	private static final Logger log = Logger.getLogger(MaGateMediator.class);
	
	/**
	 * Map a resource Name from resource Id
	 * @param resourceId
	 * @return
	 */
	public static String getComplexResourceName(int resourceId) {
		return GridSim.getEntityName(resourceId);
	}
	
	/**
	 * Map a Simulation Entity Name from Entity Id
	 * @param entityId
	 * @return
	 */
	public static String getEntityName(int entityId) {
		return GridSim.getEntityName(entityId);
	}
	
	/**
	 * Get Simulation platform time
	 * 
	 * @return
	 */
	public static double getSystemTime() {
		return GridSim.clock();
	}
	
	public static boolean isDayTime() {
		
		boolean isDayTime    = false;
		double currentTime   = GridSim.clock();
		double secondsPerDay = 3600 * 24;
		
		double seconds8Am = 3600 * 8;
		double seconds8Pm = 3600 * 20;
		
		double secondsInOneDay = currentTime % secondsPerDay;
		
		if((seconds8Am < secondsInOneDay) && (secondsInOneDay < seconds8Pm)) {
			isDayTime = true;
		} else {
			isDayTime = false;
		}
		
		// return
		return isDayTime;
	}
	
	/**
	 * Convert JobInfo into byte[]
	 * 
	 * @param state
	 * @return
	 */
	public static byte[] serialiseJob(JobInfo state) {
		
	    try {
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        ObjectOutputStream oos = new ObjectOutputStream(bos);
	        oos.writeObject(state);
	        oos.flush();
	        return bos.toByteArray();
	    } catch (IOException e) {
	        throw new IllegalArgumentException(e);
	    }
	}
	
	/**
	 * Convert byte[] into JobInfo
	 * 
	 * @param byteArray
	 * @return
	 */
	public static JobInfo deserialiseJob(byte[] byteArray) {
		
	    try {
	        ObjectInputStream oip = new ObjectInputStream(new ByteArrayInputStream(byteArray));
	        return (JobInfo) oip.readObject();
	        
	    } catch (IOException e) {
	        throw new IllegalArgumentException(e);
	        
	    } catch (ClassNotFoundException e) {
	        throw new IllegalArgumentException(e);
	    }
	}
	
	/**
	 * Generate a JSDL based on AgreementTemplate (unused)
	 * @param template
	 * @return
	 * @throws Exception
	 * @deprecated
	 */
	public static JobDefinitionType getSingleJSDLJobDefinitionFromAgreementTemplate(AgreementTemplateType template) throws Exception {
		
		JobDefinitionType template_jsdl = null;
		ServiceDescriptionTermType sdt = template.getTerms().getAll().getServiceDescriptionTermArray(0);
		
        NodeList nl = sdt.getDomNode().getChildNodes();
        
        // Get JSDL from the SDT of the Template
        for(int k = 0; k < nl.getLength(); k++) {
        	if(nl.item(k).getNodeType() == Node.ELEMENT_NODE) {
        		template_jsdl = JobDefinitionType.Factory.parse(nl.item(k));
        		break;
        	}
        }
		
		return template_jsdl;
	}
	
	/**
	 * Insert a JSDL into a SDT (unique JSDL each SDT)
	 * 
	 * @param jdef
	 * @param sdt
	 * @throws Exception
	 */
	public static void setJSDLJobDefinitionInSDT(JobDefinitionType jdef, ServiceDescriptionTermType sdt) throws Exception {
		
		Node sdt_doc  = sdt.getDomNode();
        Node imported = sdt_doc.getOwnerDocument().importNode(jdef.getDomNode(), true);
        sdt_doc.appendChild(imported);
        
	}
	
	/**
	 * Get a JSDL froma SDT (unique JSDL each SDT)
	 * 
	 * @param sdt
	 * @return
	 * @throws Exception
	 */
	public static JobDefinitionType getJSDLJobDefinitionFromSDT(ServiceDescriptionTermType sdt) throws Exception {
		
		JobDefinitionType outJSDL = null;
		
        NodeList nl = sdt.getDomNode().getChildNodes();
        
        // Get JSDL from the SDT 
        for(int k = 0; k < nl.getLength(); k++) {
        	
        	if(nl.item(k).getNodeType() == Node.ELEMENT_NODE) {
        		outJSDL = JobDefinitionType.Factory.parse(nl.item(k));
        		break;
        	}
        }
		
		return outJSDL;
	}
	
	/**
	 * Give a JSDL specification based OperatingSystemTypeEnum based on String-represented OS type 
	 * <b>(needs re-factoring because only three OS supported currently)</b>
	 * @param os
	 * @return
	 */
	public static OperatingSystemTypeEnumeration.Enum convertOSToEnum(String os) {
		
		if(os.equals(OperatingSystemTypeEnumeration.LINUX.toString())) {
			return OperatingSystemTypeEnumeration.LINUX;
			
		} else if (os.equals(OperatingSystemTypeEnumeration.WINDOWS_XP.toString())) {
			return OperatingSystemTypeEnumeration.WINDOWS_XP;
			
		} else if (os.equals(OperatingSystemTypeEnumeration.MACOS.toString())) {
			return OperatingSystemTypeEnumeration.MACOS;
			
		} else {
			return null;
		}
	}
	
	/**
	 * Convert information from JobInfo into Agreement, including:
	 * jobId, OS, CPUCount, ExePrice
	 * @param jobInfo
	 * @return
	 */
	public static MaGateAgreement convertJobInfoToAgreement(String maGateId, JobInfo jobInfo) {
		
		ConcurrentHashMap<String, Object> jobProfile = jobInfo.getCommunityJobInfoProfile();
		
		JobDefinitionType jsdlJobDefinition = JobDefinitionType.Factory.newInstance();
		
		// Create a JSDL with JobInfo.getGlobalJobID()
		jsdlJobDefinition.addNewJobDescription().addNewJobIdentification().setJobName(jobInfo.getGlobalJobID());
		
		jsdlJobDefinition.getJobDescription().addNewResources();
		
		// fetch profile into JSDL
		if(jobProfile.containsKey(MaGateMessage.MatchProfile_OS)) {
			
			jsdlJobDefinition.getJobDescription().getResources().addNewOperatingSystem().
				addNewOperatingSystemType().
				setOperatingSystemName(MaGateMediator.convertOSToEnum((String)jobProfile.get(MaGateMessage.MatchProfile_OS)));
			
		} 
		
		if(jobProfile.containsKey(MaGateMessage.MatchProfile_CPUCount)) {
			
			Integer cpuCountInteger = (Integer) jobProfile.get(MaGateMessage.MatchProfile_CPUCount);
			
			double cpuCount = cpuCountInteger.doubleValue();
			
			RangeValueType rangeDoc = RangeValueType.Factory.newInstance();
	        rangeDoc.addNewExact().setDoubleValue(cpuCount);
	        
			jsdlJobDefinition.getJobDescription().getResources().setTotalCPUCount(rangeDoc);
			
		}
		
//		if(jobProfile.containsKey(MaGateMessage.MatchProfile_ExePrice)) {
//	        
//			Double priceDouble = (Double) jobProfile.get(MaGateMessage.MatchProfile_ExePrice);
//				
//			double exePrice = Math.abs(priceDouble);
//			jsdlJobDefinition.getJobDescription().getJobIdentification().setDescription(Double.toString(exePrice));
//	        
//		}
		
		MaGateAgreement agreement = new MaGateAgreement();
		
		/**  
		 * set one Context (<b>AgreementInitiator</b>, AgreementResponder, 
		 * Service Provider, ExpirationTime, TemplateId, TemplateName)
		 */
		MagateHostDocument magateHostDoc = MagateHostDocument.Factory.newInstance();
		MaGateHostType magateHost = magateHostDoc.addNewMagateHost();
		magateHost.setIdentity(maGateId);
		
        // create SDT
        ServiceDescriptionTermType sdt = null;
        
        try {
        	
        	agreement.getContext().setAgreementInitiator(magateHost);
			sdt = agreement.getTerms().addNewAll().addNewServiceDescriptionTerm();
			sdt.setName(agreement.getAgreementId() + ":SDT_Name");
			sdt.setServiceName(agreement.getAgreementId() + ":" + jobInfo.getGlobalJobID());
			
			// put JSDL in SDT
	        MaGateMediator.setJSDLJobDefinitionInSDT(jsdlJobDefinition, sdt);
	        
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.toString());
			return null;
		}
		
        return agreement;
		
	}
	
}


