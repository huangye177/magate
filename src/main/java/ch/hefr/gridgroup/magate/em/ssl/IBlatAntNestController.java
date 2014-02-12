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

public interface IBlatAntNestController {

	public abstract void startQuery(String recipient, String queryId,
			HashMap<String, Object> profile) throws Exception;

	public abstract void updateProfile(String recipient,
			HashMap<String, Object> profile) throws Exception;

}