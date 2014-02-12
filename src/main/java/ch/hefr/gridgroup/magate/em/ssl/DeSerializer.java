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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeSerializer implements IDeSerializer {

	private InputStreamReader streamReader;

	private Object dataTree;

	private char ch;

	private boolean skipNextRead = false;

	public DeSerializer(InputStream serialized) throws IOException {
			streamReader = new InputStreamReader(serialized);
			dataTree = readValue();
			serialized.close();
			streamReader.close();
	}
	
	private char read() throws IOException {
		if(!skipNextRead)
			ch = (char) streamReader.read();
		else
			skipNextRead = false;
		return ch;
	}
	
	private void unread() {
		skipNextRead = true;
	}

	private Object readValue() throws IOException {
		read();
		if (Character.isDigit(ch)) {
			return readString();
		} else if (ch == 'i') {
			return readInteger();
		} else if (ch == 'f') {
			return readFloat();
		} else if (ch == 'd') {
			return readDictionary();
		} else if (ch == 'l') {
			return readList();
		} else if (ch == 'o') {
			return readBoolean();
		} else if (ch == 'n') {
			return readNil();
		} else {
			return null;
		}
	}

	private List<Object> readList() throws IOException {
		ArrayList<Object> list = new ArrayList<Object>();
		read();
		if (ch == 'e')
			return list;
		else
			unread();
		while (true) {
			Object item = readValue();
			if (item == null)
				break;
			list.add(item);
			read();
			unread();
		}
		return list;
	}
	
	private Integer readInteger() throws IOException {
		String num = new String();
		int sign = 1;
		read();
		if (ch == '-') {
			sign = -1;
			read();
		}
		while (Character.isDigit(ch)) {
			num += ch;
			read();
		}
		return new Integer(num)*sign;
	}
	
	private Boolean readBoolean() throws IOException {
		boolean value = true;
		read();
		if (ch == 'f')
			value = false;
		read();
		return new Boolean(value);
	}
	
	private Float readFloat() throws IOException {
		String num = new String();
		int sign = 1;
		read();
		if (ch == '-') {
			sign = -1;
			read();
		}
		while (Character.isDigit(ch)) {
			num += ch;
			read();
		}
		if (ch == '.') {
			read();
			num = num + ch;
			while (Character.isDigit(ch)) {
				num += ch;
				read();
			}
		}
		return new Float(num)*sign;
	}

	private String readString() throws IOException {
		String slen = new String();
		while (Character.isDigit(ch)) {
			slen = slen + ch;
			read();
		}
		int len = new Integer(slen).intValue();
		String str = new String();
		for (int i = 0; i < len; i++) {
			str += read();
		}
		return str;
	}

	private Map<String, Object> readDictionary() throws IOException {
		HashMap<String,Object> dict = new HashMap<String,Object>();
		while (true) {
			Object key = readValue();
			if ((key == null) || !(key instanceof String))
				break;
			Object value = readValue();
			dict.put((String)key, value);
		}
		return dict;
	}

	private Boolean readNil() throws IOException {
		read();
		return new Boolean(false);
	}

	public Object get() {
		return dataTree;
	}

}
