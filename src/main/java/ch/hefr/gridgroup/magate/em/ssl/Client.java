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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class Client {

	public static void sendMessage(String address, IMessage m) throws UnknownHostException, IOException {
		if (address.startsWith("bebop://")) {
			address = address.replace("bebop://", "");
			String host = "localhost";
			int port = 7855;
			if (address.contains(":")) {
				String[] splitted = address.split(":");
				host = splitted[0];
				splitted = splitted[1].split("/");
				port = Integer.parseInt(splitted[0]);
			} else {
				String[] splitted = address.split("/");
				host = splitted[0];
			}
			sendMessage(host, port, m);
		}	
	}
	
	private static void sendMessage(String host, int port, IMessage m) throws UnknownHostException, IOException {
		Socket s = new Socket(InetAddress.getByName(host), port);
		s.setKeepAlive(true);
		s.setSoTimeout(0);
		byte[] buffer = new byte[1024];
		for (int k = 0; k < 1024; k++)
			buffer[k] = '\0';
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		List<Object> l = new ArrayList<Object>();
		l.add(m.getRecipient());
		l.add(m.getContents());
		ISerializer ser = new Serializer(out);
		ser.print(l);
		String ba = out.toString();
		OutputStream oStream = new BufferedOutputStream(s.getOutputStream());
		oStream.write(ba.getBytes());
		oStream.write('\n');
		oStream.flush();
		oStream.close();
		s.close();
	}
	

}
