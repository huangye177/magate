/**
 *  BlatAnt Net Client
 *  
 *  Copyright (C) 2008-2009  Amos Brocco <amos.brocco@unifr.ch>
 *  					Pervasive and Artificial Intelligence Research Group
 *  					Department of Informatics DIUF
 *  					University of Fribourg, Fribourg
 *  					Switzerland
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package ch.hefr.gridgroup.magate.em.ssl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;


public class BlatAntNetworkController implements Observer, IBlatAntNetworkController {

	private MailBox mailbox;	
	private IBlatAntNetworkObserver client;
	private String clientAddress;
	private String serverAddress;
	

	public BlatAntNetworkController(IBlatAntNetworkObserver client, String clienthost, int clientport, String serverhost, int serverport) {
		this.client = client;
		MailService ms = MailService.start(clientport);
		mailbox = ms.createMailBox(this, "global");
		serverAddress = String.format("bebop://%s:%d", serverhost, serverport);
		clientAddress = String.format("bebop://%s:%d", clienthost, clientport);
	}
	
	/* (non-Javadoc)
	 * @see controller.IBlatAntNetworkController#attachNode(java.lang.String, java.lang.String)
	 */
	public void attachNode(String nodeId, String hook) throws Exception {
		Map<String,Object> m = new HashMap<String,Object>();
		m.put("command", "attachNode");
		m.put("id", nodeId);
		m.put("hook", hook);
		Message msg = new Message("global", m);
		Client.sendMessage(serverAddress, msg);
	}
	
	/* (non-Javadoc)
	 * @see controller.IBlatAntNetworkController#removeNode(java.lang.String)
	 */
	public void removeNode(String nodeId)  throws Exception  {
		Map<String,Object> m = new HashMap<String,Object>();
		m.put("command", "removeNode");
		m.put("id", nodeId);
		Message msg = new Message("global", m);
		Client.sendMessage(serverAddress, msg);
	}
	
	/* (non-Javadoc)
	 * @see controller.IBlatAntNetworkController#detachNode(java.lang.String)
	 */
	public  void detachNode(String nodeId) throws Exception {
		Map<String,Object> m = new HashMap<String,Object>();
		m.put("command", "detachNode");
		m.put("id", nodeId);
		Message msg = new Message("global", m);
		Client.sendMessage(serverAddress, msg);
	}

	@SuppressWarnings("unchecked")
	public void update(Observable source, Object data) {
		if (source == mailbox) {
			IMessage msg = mailbox.popMessage();
			if (msg != null) {
				Map<String,Object> contents = msg.getContents();
				String command = (String) contents.get("command");
				if (command.equals("getNodeList")) {
					List<String> nodes = (List<String>) contents.get("result");
					client.setNodeList(nodes);
				} else if (command.equals("getNeighbors")) {
					String nodeId = (String) contents.get("id");
					List<String> neighbors = (List<String>) contents.get("result");
					client.setNodeNeighbors(nodeId,neighbors);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see controller.IBlatAntNetworkController#getNeighbors(java.lang.String)
	 */
	public  void getNeighbors(String nodeId) throws Exception {
		Map<String,Object> m = new HashMap<String,Object>();
		m.put("command", "getNeighbors");
		m.put("address", clientAddress);
		m.put("recipient", "global");
		m.put("id", nodeId);
		Message msg = new Message("global", m);
		Client.sendMessage(serverAddress, msg);
	}
	
	
	/* (non-Javadoc)
	 * @see controller.IBlatAntNetworkController#getNodelist()
	 */
	public  void getNodelist() throws Exception {
		Map<String,Object> m = new HashMap<String,Object>();
		m.put("command", "getNodeList");
		m.put("address", clientAddress);
		m.put("recipient", "global");
		Message msg = new Message("global", m);
		Client.sendMessage(serverAddress, msg);
	}


}
