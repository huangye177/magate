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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observer;



public class Server implements Runnable {
	
	private ServerSocket s;
	private int serverport = 7893;
	private IBuffer buffer;

	public Server(IBuffer buffer, int port) {
		this.buffer = buffer;
		this.serverport = port;
	}

	public void run() {
		try {
			s = new ServerSocket(serverport);
			while (true) {
				fetchAndExecuteMessage();
			}
		} catch (IOException e) {
			System.out.println("error access with port: " + serverport);
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void fetchAndExecuteMessage() {
		Socket incoming = null;
		try {
			incoming = s.accept();
			new Thread(new ServingThread(incoming, buffer)).start();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public static Server start(IBuffer buffer, int port) {
		Server server = new Server(buffer, port);
		new Thread(server).start();
		return server;
	}
	
	public static IBuffer start(Observer observer, int port) {
		Buffer buffer = new Buffer();
		buffer.addObserver(observer);
		Server server = new Server(buffer, port);
		new Thread(server).start();
		return buffer;
	}

}
