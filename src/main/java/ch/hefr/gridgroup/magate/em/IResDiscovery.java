package ch.hefr.gridgroup.magate.em;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.hefr.gridgroup.magate.MaGateEntity;
import ch.hefr.gridgroup.magate.im.SimJobSubmitter;
import ch.hefr.gridgroup.magate.storage.MaGateStorage;

public interface IResDiscovery {
	
	public abstract String[] syncSearchRemoteNode(ConcurrentHashMap<String,Object> maGateQuery);
	
//	public abstract String[] searchRemoteMaGateIdentity(HashMap<String,Object> maGateQuery, int timeout);
	
	public abstract void onResultFound(String queryId, String matchedResult);
	
	public HashMap<String, Object> getMaGateProfile();
}


