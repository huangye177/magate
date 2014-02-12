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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class Example implements IBlatAntNestObserver, IBlatAntNetworkObserver {
	

//	public static void main(String[] args) throws Exception {
//		new Example();
//	}
	
	public Example() throws Exception {
		// Default nest controller port is 47334
		IBlatAntNestController nestc = new BlatAntNestController(this, "localhost", 56789, "localhost", 47334);
		// Default network controller port is 34567 
		IBlatAntNetworkController netc = new BlatAntNetworkController(this, "localhost", 56790, "localhost", 34567);
		
		// Create a sample profile
		HashMap<String,Object> sampleProfile = new HashMap<String,Object>();
		sampleProfile.put("linux", "os");
		sampleProfile.put("x86", "arch");
		sampleProfile.put("RAM", 1024);
		sampleProfile.put("HDD", 300);
		
		// Create a sample query
		HashMap<String,Object> sampleQuery = new HashMap<String,Object>();
		sampleQuery.put("linux", "os");
		sampleQuery.put("x86", "arch");
		sampleQuery.put("RAM", 512);
		sampleQuery.put("HDD", 300);
		
		List<String> sampleNodes = new ArrayList<String>();	
		
		String hook = new Integer("0").toString();
		System.out.format("Adding initial hook node %s\n", hook);
		netc.attachNode(hook, "");

		// Important! The nest control in the simulator is started when the first
		// node gets added. So here we must wait a little bit for the nest control to start
		Thread.sleep(1000);
		
		System.out.format("Adding more nodes...");
		
		for (int i=1; i<250;i++) {
			String nodeId = new Integer(i).toString();
			netc.attachNode(nodeId, hook);
			nestc.updateProfile(nodeId, sampleProfile);
			sampleNodes.add(nodeId);
			System.out.print(".");
		}
		
		System.out.println("\nWaiting 60s for network bootstrap...");
		
		Thread.sleep(30000);
		
		System.out.println("Asking for node list...");
		
		netc.getNodelist();
		
		System.out.println("Asking for neighbors...");
		
		for (String n : sampleNodes) {
			netc.getNeighbors(n);
		}
		
		System.out.println("Waiting 15s before starting queries");
		
		Thread.sleep(15000);
		
		Integer queryId = 1000;
		
		// Start some random queries
		for (int i=0; i<1;i++) {
			String startNodeId = sampleNodes.get(new Double(Math.random()*sampleNodes.size()).intValue());
			queryId++;
			System.out.format(System.currentTimeMillis() + "Starting query %s from node %s\n", queryId.toString(), startNodeId);
			nestc.startQuery(startNodeId, queryId.toString(), sampleQuery);
		}
		
		System.out.println("Will start removing nodes in 120s...");
		Thread.sleep(12000);
		
		// Remove nodes
		for (String n : sampleNodes) {
			System.out.format("Detaching node %s\n", n);
			netc.detachNode(n);
			Thread.sleep(500);
		}
		
		System.exit(0);
	}

	public void addQueryResult(String qid, String match) {
		System.out.format(System.currentTimeMillis() + "Matching node for query %s is %s\n", qid, match);
	}

	public void setNodeList(List<String> list) {
		System.out.println("Actual node list::");
		for (String n : list) {
			System.out.format("\t%s\n",n);
		}
	}

	public void setNodeNeighbors(String nodeId, List<String> neighbors) {
		System.out.format("Actual neighbors for node %s are %s\n", nodeId, neighbors.toString());
	}

}
