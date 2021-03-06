/**
 * Copyright 2016 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package it.isislab.dmason.sim.field;

import it.isislab.dmason.exception.DMasonException;
import sim.engine.SimState;


/**
 * An interface for all Distributed Fields 2D
 * @param <E> the type of locations
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * 
 */
public interface DistributedField2D<E> extends DistributedField<E>
{	
	public static final int UNIFORM_PARTITIONING_MODE=0;
	public static final int SQUARE_BALANCED_DISTRIBUTION_MODE=1;
	public static final int HORIZONTAL_BALANCED_DISTRIBUTION_MODE=2;
	public static final int NON_UNIFORM_PARTITIONING_MODE=4;
	public static final int THIN_MODE=3;
	
	/**  
	 * Provide the shift logic of the agents among the peers
	 * @param location The new location of the remote agent
	 * @param remoteObject The remote agent to be stepped or the value 
	 * @param sm SimState of simulation
	 * @throws DMasonException the exception
	 * @return 1 if it's in the field, -1 if there's an error (setObjectLocation returns null)
	 */
	public boolean setDistributedObjectLocation(final E location,Object remoteObject,SimState sm) throws DMasonException;
	

	/**
	 * Set a available location to a Remote Agent:
	 * it generates the location depending on the field of expertise
	 * @return The location assigned to Remote Agent
	 */
	public E getAvailableRandomLocation();
	
	public boolean isToroidal();
	
	public void setToroidal(boolean isToroidal);
	
	public boolean verifyPosition(E pos);
	
}