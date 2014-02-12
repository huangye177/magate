/**
 *  BeBop Asynchronous Communication Library
 *  
 *  Copyright (C) 2008  Amos Brocco <amos.brocco@unifr.ch>
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

import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;




public class MailService implements Observer {
	
	private Hashtable<String,MailBox> mailboxes;
	private Buffer buffer;
	
	public MailService(Buffer buffer) {
		this.buffer = buffer;
		mailboxes = new Hashtable<String,MailBox>();
		buffer.addObserver(this);
	}
	
	public MailBox createMailBox(Observer o, String id) {
		MailBox m = createMailBox(id);
		m.addObserver(o);
		return m;
	}
	
	public MailBox createMailBox(String id) {
		synchronized(mailboxes) {
			if (mailboxes.containsKey(id))
				return mailboxes.get(id);
			MailBox m = new MailBox();
			mailboxes.put(id, m);
			return m;
		}
	}
	
	public MailBox getMailBox(String id) {
		synchronized(mailboxes) {
			return mailboxes.get(id);
		}
	}
	
	public void destroyMailBox(String id) {
		synchronized(mailboxes) {
			mailboxes.remove(id);
		}
	}
	
	private void pushMessage(IMessage m) {
		if (m.getRecipient() != null) {
			String id = m.getRecipient();
			MailBox mbox = getMailBox(id);
			if (mbox != null)
				mbox.pushMessage(m);
		}
	}

	public void update(Observable source, Object data) {
		if (source == this.buffer) {
			getMessage();
		}
	}
	
	private synchronized void getMessage() {
		IMessage msg = buffer.popMessage();
		if (msg != null) {
			pushMessage(msg);
		}
	}
	
	public static MailService start(int port) {
		Buffer sbuffer = new Buffer();
		Server server = new Server(sbuffer, port);
		MailService ms = new MailService(sbuffer);
		new Thread(server).start();
		return ms;
	}

}
