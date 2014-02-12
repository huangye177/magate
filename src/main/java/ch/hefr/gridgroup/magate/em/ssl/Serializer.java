/**
 *  BeBop Asynchronous Communication Library
 *  
 *  Copyright (C) 2008  Amos Brocco <amos.brocco@unifr.ch>
 *  					Pervasive and Artificial Intelligence Research Group
 *  					Department of Informatics DIUF
 *  					University of Fribourg, Fribourg
 *  					Switzerland
 *  Copyright (C) 2006  Part of Solenopsis Framework
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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Serializer implements ISerializer {

	private PrintStream swr;

	public Serializer(OutputStream sprintr) {
		swr = new PrintStream(sprintr);
	}

	public Serializer(PrintStream pprintr) {
		swr = pprintr;
	}

	@SuppressWarnings("unchecked")
	public void print(Object item) {
		if (item instanceof List)
			printList((List)item);
		else if (item instanceof Map)
			printDictionary((Map)item);
		else if (item instanceof String)
			printString((String)item);
		else if (item instanceof Integer)
			printInteger((Integer)item);
		else if (item instanceof Float)
			printFloat((Float)item);
		else if (item instanceof Boolean) {
			Boolean b = (Boolean) item;
			if(b == false) 
					printNil(null);
				else
					printBoolean((Boolean)item);
		}
	}

	@SuppressWarnings("unchecked")
	private void printList(List item) {
		swr.print('l');
		for (int i = 0; i < item.size(); i++)
			print(item.get(i));
		swr.print('e');
	}

	@SuppressWarnings("unchecked")
	private void printDictionary(Map item) {
		swr.print('d');
		Iterator keys = item.keySet().iterator();
		while (keys.hasNext()) {
			Object key = keys.next();
			Object value = item.get(key);
			print(key);
			print(value);
		}
		swr.print('e');
	}

	private void printString(String item) {
		String str = item.toString();
		swr.print(str.length());
		swr.print(':');
		swr.print(str);
	}

	private void printInteger(Integer item) {
		swr.print('i');
		swr.print(item.toString());
		swr.print('e');
	}
	
	private void printFloat(Float item) {
		swr.print('f');
		swr.print(item.toString());
		swr.print('e');
	}
	
	private void printBoolean(Boolean item) {
		swr.print('o');
		if(item.booleanValue())
			swr.print("t");
		else
			swr.print("f");
		swr.print('e');
	}

	private void printNil(Boolean item) {
		swr.print('n');
		swr.print('e');
	}

}
