/**
 *  BeBop Asynchronous Communication Library
 *  
 *  Copyright (C) 2008  Amos Brocco <amos.brocco@unifr.ch>
 *  					Pervasive and Artificial Intelligence Research Group
 *  					Department of Informatics DIUF
 *  					University of Fribourg, Fribourg
 *  					Switzerland
 *  Copyright (C) 2008  Part of Gryphon Executor
 *  					Amos Brocco <amos.brocco@unifr.ch>  
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;



public class ServingThread implements Runnable {
	
	private int MAX_MESSAGE_SIZE = 8192;

	private Socket incoming = null;
	
	private IBuffer buffer;
	
	public ServingThread(Socket incoming, IBuffer buffer) {
		this.incoming = incoming;
		this.buffer = buffer;
	}
	
	@SuppressWarnings("unchecked")
	public void processIncomingRequest() throws IOException {
		Message msg = null;
		byte[] buffer = new byte[MAX_MESSAGE_SIZE];
		for (int k=0; k < MAX_MESSAGE_SIZE; k++)
			buffer[k] = '\0';
		int count = 0;
		int read = 0;
		
		incoming.setSoTimeout(2000);
		
		InputStream is;
		OutputStream os;
		is = new BufferedInputStream(incoming.getInputStream());
		os = new BufferedOutputStream(incoming.getOutputStream());
		
		boolean doneReading = false;
		
        while ((count < 1024) && !doneReading) {
			read = is.read(buffer, count, MAX_MESSAGE_SIZE - count);
            if (read == -1)
            	break;
            int i = count;
            count += read;
            for (; i < count; i++) {
                if (buffer[i] == (byte)'\n' || buffer[i] == (byte)'\r') {
                	doneReading = true;
                    break;
                }
            }
        }
        incoming.setSoTimeout(0);
        ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
		DeSerializer ds = null;
		ds = new DeSerializer(bis);
		bis.close();
		try {
			List data = (List) ds.get();
			msg = new Message((String) data.get(0), (Map) data.get(1));
		} catch (Exception e) {
			return;
		}
		try {
			if (msg != null) {	
				this.buffer.pushMessage(msg);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		os.flush();
		os.close();
		is.close();
	}
	
	public void run() {
		try {
			processIncomingRequest();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				incoming.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
