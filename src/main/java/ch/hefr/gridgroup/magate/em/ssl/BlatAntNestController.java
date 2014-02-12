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
import java.util.Map;
import java.util.Observable;
import java.util.Observer;


public class BlatAntNestController implements Observer, IBlatAntNestController {

	private MailBox mailbox;
	private IBlatAntNestObserver client;
	private String clientAddress;
	private String serverAddress;

	public BlatAntNestController(IBlatAntNestObserver client, String clienthost, int clientport, String serverhost, int serverport) {
		this.client = client;
		MailService ms = MailService.start(clientport);
		mailbox = ms.createMailBox(this, "global");
		serverAddress = String.format("bebop://%s:%d", serverhost, serverport);
		clientAddress = String.format("bebop://%s:%d", clienthost, clientport);
	}
	
	/* (non-Javadoc)
	 * @see controller.IBlatAntNestController#startQuery(java.lang.String, java.util.HashMap)
	 */
	public void startQuery(String recipient, String queryId, HashMap<String,Object> profile) throws Exception {
		Map<String,Object> m = new HashMap<String,Object>();
		for (String field : profile.keySet()) {
			if (!field.equals("command")) {
				m.put(new String(field), profile.get(field));
			}
		}
		m.put("qid", queryId);
		m.put("command", new String("query"));
		m.put("requester", clientAddress);
		Message msg = new Message(recipient, m);
		Client.sendMessage(serverAddress, msg);
	}
	
	
	/* (non-Javadoc)
	 * @see controller.IBlatAntNestController#updateProfile(java.lang.String, java.util.HashMap)
	 */
	public void updateProfile(String recipient, HashMap<String,Object> profile) throws Exception {
		Map<String,Object> m = new HashMap<String,Object>();
		for (String field : profile.keySet()) {
			if (!field.equals("command")) {
				m.put(new String(field), profile.get(field));
			}
		}
		m.put("command", new String("profile"));
		Message msg = new Message(recipient, m);
		Client.sendMessage(serverAddress, msg);
	}

	@SuppressWarnings("unchecked")
	public void update(Observable source, Object data) {
		if (source == mailbox) {
			IMessage msg = mailbox.popMessage();
			if (msg != null) {
				Map<String,Object> contents = msg.getContents();
				String command = (String) contents.get("command");
				if (command.equals("queryreply")) {
					String qid = (String) contents.get("qid");
					String match = (String) contents.get("match");
					client.addQueryResult(qid, match);
				}
			}
		}
	}


}
