/**
 * 
 */
package ch.hefr.gridgroup.magate.cm;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.MaGateEntity;
import ch.hefr.gridgroup.magate.em.NetworkNeighborsManager;
import ch.hefr.gridgroup.magate.model.JobInfo;
import ch.hefr.gridgroup.magate.storage.GlobalStorage;

/**
 * @author yehuang
 *
 */
public class OutputResponser {

	private static Log log = LogFactory.getLog(OutputResponser.class);
	
	private MaGateEntity maGate;
	private String maGateIdentity;
	
	public OutputResponser(MaGateEntity maGate) {
		
		this.maGate = maGate;
		this.maGateIdentity = maGate.getMaGateIdentity();
	}

}
