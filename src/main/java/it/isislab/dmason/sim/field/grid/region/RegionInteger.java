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

package it.isislab.dmason.sim.field.grid.region;

import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.support.field2D.EntryAgent;
import it.isislab.dmason.sim.field.support.field2D.region.Region;
import it.isislab.dmason.util.Util;
import sim.util.Int2D;

/**
 * A Region specialized to be used in a field with Integer coordinates
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */
public class RegionInteger extends Region<Integer,Int2D>
{
	
	
	public RegionInteger(Integer upl_xx, Integer upl_yy, Integer down_xx,
			Integer down_yy) 
	{
		super(upl_xx, upl_yy, down_xx, down_yy);	
		
	}


	@Override
	public Region<Integer,Int2D> clone() 
	{
		RegionInteger r=new RegionInteger(upl_xx, upl_yy, down_xx, down_yy);
		for(EntryAgent<Int2D> e: this.values())
		{
			EntryAgent<Int2D> toClone = new EntryAgent(((RemotePositionedAgent<Int2D>)(Util.clone(e.r))),e.l);
			r.put(e.r.getId(),toClone);
		}
		return r;
	}

	@Override
	public boolean isMine(Integer x ,Integer y)
	{		
		return (x>=upl_xx) && (y >= upl_yy) && (x < down_xx) && (y< down_yy);
		//return (x >= upl_xx) && (x <= down_xx) && (y >= upl_yy) && (y <= down_yy);
	}

	@Override
	public boolean addAgents(EntryAgent<Int2D> e) 
	{	
		if(e == null || e.l == null || e.r == null) return false;
		if(this.containsKey(e.r.getId()) && this.get(e.r.getId()).equals(e)) return true;
		this.put(e.r.getId(),e);
		return true;
	}


}