/**
 * Copyright 2012 Universita' degli Studi di Salerno


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

package it.isislab.dmason.experimentals.sim.field.support.field2D.loadbalanced;

import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.support.field2D.DistributedRegionInterface;
import it.isislab.dmason.sim.field.support.field2D.region.Region;

import java.io.Serializable;

/**
 * A wrapper class for the regions those must be swapped among the peers.
 * @param <E> the type of coordinates
 * @param <F> the type of locations
 *
 */
public class DistributedRegionLB<E,F> implements Serializable, DistributedRegionInterface
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Valid values for <code>position</code>
	public static int LEFT=1;
	public static int RIGHT=2;
	public static int UP=3;
	public static int DOWN=4;
	
	public static int CORNER_DIAG_DOWN_RIGHT=5;
	public static int CORNER_DIAG_DOWN_LEFT=6;
	public static int CORNER_DIAG_UP_RIGHT=7;
	public static int CORNER_DIAG_UP_LEFT=8;
	
	public int position;
	
	//the regions swapped
	public Region<E,F> mine;
	public Region<E,F> out;
	
	public long step;
	public CellType type;
	
	public int numAgents;
	public int mineNumAgents;
	public E width;
	
	/**
	 * Constructor of class with parameters:
	 * 
	 * @param mine RegionNumeric into field that send the updates
	 * @param out RegionNumeric external field that send the updates
	 * @param step the number of step in which send the updates
	 * @param type the celltype of cell that send the updates
	 * @param position Specify the position of this cell relative to the cell <code>type</code>
	 * @param numAgents the number of agents of the region
	 * @param mineNumAgents the number of agents in the left/right mine
	 * @param width the width of the region
	 */
	public DistributedRegionLB(Region<E, F> mine,
			Region<E, F> out, long step, CellType type,int position, 
			int numAgents,int mineNumAgents,E width) {
		super();
		this.position = position;
		this.mine = mine;
		this.out = out;
		this.step = step;
		this.type = type;
		this.numAgents = numAgents;
		this.mineNumAgents=mineNumAgents;
		this.width=width;
	}
	
	
	/**
	 * Constructor of class with parameters:
	 * 
	 * @param mine RegionNumeric into field that send the updates
	 * @param out RegionNumeric external field that send the updates
	 * @param step the number of step in which send the updates
	 * @param type the celltype of cell that send the updates
	 */
	public DistributedRegionLB(Region<E,F> mine, Region<E,F> out,long step,CellType type) 
	{
		super();
		this.mine = mine.clone();
		this.out = out.clone();
		this.step = step;
		this.type = type;
	}
	/**
	 * Creates a new region used for swapping.
	 * @param mine The <i>mine</i> region relative to the cell <code>type</code>.
	 * @param out The <i>out</i> region relative to the cell <code>type</code>.
	 * @param step Simulation step at which this region is referring.
	 * @param type Identifies the cell that created this region.
	 * @param position Specify the position of this cell relative to the cell <code>type</code>.
	 */
	public DistributedRegionLB(Region<E,F> mine, Region<E,F> out, long step, CellType type, int position) 
	{
		super();
		this.mine = mine.clone();
		this.out = out.clone();
		this.step = step;
		this.type = type;
		this.position = position;
	}
	
	//getters and setters
	public Region<E,F> getmine() { return mine; }
	public void setmine(Region<E,F> mine) { this.mine = mine; }
	public Region<E,F> getout() { return out; }
	public void setout(Region<E,F> out) { this.out = out;}
	
	@Override
	public long getStep() {	return step;}
	public void setstep(long step) {this.step = step;}
	public CellType gettype() { return type; }
	public void settype(CellType type) { this.type = type; }

	@Override
	public int getPosition() { return position; }
}