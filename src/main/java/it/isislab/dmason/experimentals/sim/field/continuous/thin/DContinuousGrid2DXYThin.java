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

package it.isislab.dmason.experimentals.sim.field.continuous.thin;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.sim.field.support.globals.GlobalInspectorHelper;
import it.isislab.dmason.experimentals.util.visualization.globalviewer.RemoteSnap;
import it.isislab.dmason.experimentals.util.visualization.globalviewer.VisualizationUpdateMap;
import it.isislab.dmason.experimentals.util.visualization.zoomviewerapp.ZoomArrayList;
import it.isislab.dmason.nonuniform.QuadTree;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.TraceableField;
import it.isislab.dmason.sim.field.continuous.region.RegionDouble;
import it.isislab.dmason.sim.field.support.field2D.DistributedRegion;
import it.isislab.dmason.sim.field.support.field2D.EntryAgent;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import it.isislab.dmason.sim.field.support.field2D.region.Region;
import it.isislab.dmason.util.connection.Connection;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.MutableInt2D;

/**
 *  <h3>This Field extends Continuous2D, to be used in a distributed environment. All the necessary informations 
 *  for the distribution of simulation are wrapped in this class.</h3>
 * <p> This version is for a distribution in a <i>square mode</i>.
 *  It represents the field managed by a single peer.
 *  This is an example for a square mode distribution with 9 peers (only to distinguish the regions):
 *  (for code down)
 *  <p>
 *
 *	<ul>
 *	<li>MYFIELD : Region to be simulated by peer.</li>
 *
 *	<li>LEFT_MINE, RIGHT_MINE, UP_MINE, DOWN_MINE,CORNER_MINE_LEFT_UP,CORNER_MINE_LEFT_DOWN,
 *		CORNER_MINE_RIGHT_UP,CORNER_MINE_RIGHT_DOWN :Boundaries Regions those must be simulated and sent to neighbors.</li>
 *	
 *	<li>LEFT_OUT, RIGHT_OUT, UP_OUT, DOWN_OUT, CORNER_OUT_LEFT_UP_DIAG, CORNER_OUT_LEFT_DOWN_DIAG,
 *		CORNER_OUT_RIGHT_UP_DIAG, CORNER_OUT_RIGHT_DOWN_DIAG : Boundaries Regions those must not be simulated and sent to neighbors to be simulated.</li>
 *   <li>
 *	All peers subscribes to the topic of boundary region which want the information and run a asynchronous thread
 *	to receive the updates, then publish a topic for every their border (or neighbor), that can be :
 *	<ul>
 *	<li> MYTOPIC L (LEFT BORDER)</li>
 *	<li> MYTOPIC R (RIGHT BORDER)</li>
 *	<li> MYTOPIC U (UPPER BORDER)</li>
 *	<li> MYTOPIC D (LOWER BORDER)</li>
 *
 *	<li> MYTOPIC CUDL (Corner Up Diagonal Left)</li>
 *	<li> MYTOPIC CUDR (Corner Up Diagonal Right)</li>
 *	<li> MYTOPIC CDDL (Corner Down Diagonal Left)</li>
 *	<li> MYTOPIC CDDR (Corner Down Diagonal Right)</li>
 *</ul>
 *</li>
 *	</ul></p>
 *	
 * <PRE>
 * ---------------------------------------------------------------------------------------
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |         00             |  |  |          01          |  |  |            02           |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |   NORTH EAST            |
 * |                        |  |  |                      |  |  |  /                      |
 * |                        |  |  |                      |  |  | /                       |
 * |________________________|__|__|______NORTH_OUT__________|__|__|/________________________|
 * |________________________|__|__|______NORTH_MINE_________|__|__|_________________________|
 * |________________________|__|__|______________________|__|__|_________________________|
 * |                        |  |  |                     /|  |  |                         |
 * |                        W  W  |                    / |  |  |                         |
 * |                        E  E  |                   /  |  E  E                         |
 * |         10             S  S  |         11   CORNER  |  A  A         12              |
 * |                        T  T  |               MINE   |  S  S                         |
 * |                        |  |  |                      |  T  T                         |
 * |                        O  M  |       MYFIELD        |  |  |                         |
 * |                        U  I  |                      |  M  O                         |
 * |                        T  N  |                      |  I  U                         |
 * |________________________|__|__|______________________|__|__|_________________________|
 * |________________________|__|__|___SOUTH_MINE__________|__|__|_________________________|
 * |________________________|__|__|___SOUTH_OUT___________|__|__|_________________________|
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |       20               |  |  |          21          |  |  |           22            |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * ---------------------------------------------------------------------------------------
 * </PRE>
 */

public class DContinuousGrid2DXYThin extends DContinuousGrid2DThin implements TraceableField
{	
	private static Logger logger = Logger.getLogger(DContinuousGrid2DYThin.class.getCanonicalName());

	private String name;

	private WritableRaster writer;
	private int white[]={255,255,255};

	/*
	private FileOutputStream file;
	private PrintStream ps;
	 */

	/** List of parameters to trace */
	private ArrayList<String> tracing = new ArrayList<String>();
	/** The image to send */
	private BufferedImage currentBitmap;

	/** Simulation's time when the image was generated */
	private double actualTime;

	/** Statistics to send */
	HashMap<String, Object> actualStats;

	/** True if the global viewer requested graphics **/
	boolean isSendingGraphics;

	private ZoomArrayList<RemotePositionedAgent> tmp_zoom=new ZoomArrayList<RemotePositionedAgent>();
	private double width,height,field_width,field_height;
	private String topicPrefix = "";

	// -----------------------------------------------------------------------
	// GLOBAL PROPERTIES -----------------------------------------------------
	// -----------------------------------------------------------------------
	/** Will contain globals properties */
	public VisualizationUpdateMap<String, Object> globals = new VisualizationUpdateMap<String, Object>();


	/**
	 * Constructor of class with paramaters:
	 * 
	 * @param discretization the discretization of the field
	 * @param width field's width  
	 * @param height field's height
	 * @param sm The SimState of simulation
	 * @param max_distance maximum shift distance of the agents
	 * @param i i position in the field of the cell
	 * @param j j position in the field of the cell
	 * @param rows number of rows in the division
	 * @param columns number of columns in the division
	 * @param name ID of a region
	 * @param prefix Prefix for the name of topics used only in Batch mode
	 */
	public DContinuousGrid2DXYThin(double discretization, double width, double height, double field_width, double field_height, SimState sm, int max_distance, int i, int j,int rows,int columns, String name, String prefix) {
		super(discretization, field_width, field_height,width,height);
		this.width=width;
		this.height=height;
		this.field_width=field_width;
		this.field_height=field_height;
		this.sm = sm;	
		this.AOI = max_distance;
		//this.numPeers = num_peers;	
		this.rows = rows;
		this.columns = columns;
		this.cellType = new CellType(i, j);
		this.updates_cache = new ArrayList<Region<Double,Double2D>>();
		this.name = name;
		this.topicPrefix = prefix;

		
		createRegions();		
	}

	/**
	 * This method first calculates the upper left corner's coordinates, so the regions where the field is divided
	 * @return true if all is ok
	 */
	public  boolean createRegions(QuadTree... cell)
	{		
		if(cell.length > 1 ) return false; 
		//upper left corner's coordinates
		if(cellType.pos_j<(width%columns))
			own_x=(int)Math.floor(width/columns+1)*cellType.pos_j; 
		else
			own_x=(int)Math.floor(width/columns+1)*((width%columns))+(int)Math.floor(width/columns)*(cellType.pos_j-((width%columns))); 

		if(cellType.pos_i<(height%rows))
			own_y=(int)Math.floor(height/rows+1)*cellType.pos_i; 
		else
			own_y=(int)Math.floor(height/rows+1)*((height%rows))+(int)Math.floor(height/rows)*(cellType.pos_i-((height%rows))); 



		// own width and height
		if(cellType.pos_j<(width%columns))
			my_width=(int) Math.floor(width/columns+1);
		else
			my_width=(int) Math.floor(width/columns);

		if(cellType.pos_i<(height%rows))
			my_height=(int) Math.floor(height/rows+1);
		else
			my_height=(int) Math.floor(height/rows);


		//calculating the neighbors
		for (int k = -1; k <= 1; k++) 
		{
			for (int k2 = -1; k2 <= 1; k2++) 
			{				
				int v1=cellType.pos_i+k;
				int v2=cellType.pos_j+k2;
				if(v1>=0 && v2 >=0 && v1<rows && v2<columns)
					if( v1!=cellType.pos_i || v2!=cellType.pos_j)
					{
						neighborhood.add(v1+""+v2);
					}	
			}
		}
		try{
			currentBitmap = new BufferedImage((int)my_width, (int)my_height, BufferedImage.TYPE_3BYTE_BGR);
		}
		catch(Exception e)
		{
			System.out.println("Do not use the GlobalViewer, the requirements of the simulation exceed the limits of the BufferedImage.\n");
		}
		actualTime = sm.schedule.getTime();
		actualStats = new HashMap<String, Object>();
		isSendingGraphics = false;
		writer=currentBitmap.getRaster();

		// Building the regions

		myfield=new RegionDouble(own_x+AOI,own_y+AOI, own_x+my_width-AOI , own_y+my_height-AOI);

		//corner up left
		rmap.NORTH_WEST_OUT=new RegionDouble((own_x-AOI + width)%width, (own_y-AOI+height)%height, 
				(own_x+width)%width, (own_y+height)%height);
		rmap.NORTH_WEST_MINE=new RegionDouble(own_x, own_y, 
				own_x+AOI, own_y+AOI);

		//corner up right
		rmap.NORTH_EAST_OUT = new RegionDouble((own_x+my_width+width)%width, (own_y-AOI+height)%height,
				(own_x+my_width+AOI+width)%width, (own_y+height)%height);
		rmap.NORTH_EAST_MINE=new RegionDouble(own_x+my_width-AOI, own_y, 
				own_x+my_width, own_y+AOI);

		//corner down left
		rmap.SOUTH_WEST_OUT=new RegionDouble((own_x-AOI+width)%width, (own_y+my_height+height)%height,
				(own_x+width)%width,(own_y+my_height+AOI+height)%height);
		rmap.SOUTH_WEST_MINE=new RegionDouble(own_x, own_y+my_height-AOI,
				own_x+AOI, own_y+my_height);

		//corner down right
		rmap.SOUTH_EAST_OUT=new RegionDouble((own_x+my_width+width)%width, (own_y+my_height+height)%height, 
				(own_x+my_width+AOI+width)%width,(own_y+my_height+AOI+height)%height);
		rmap.SOUTH_EAST_MINE=new RegionDouble(own_x+my_width-AOI, own_y+my_height-AOI,
				own_x+my_width,own_y+my_height);

		rmap.WEST_OUT=new RegionDouble((own_x-AOI+width)%width,(own_y+height)%height,
				(own_x+width)%width, ((own_y+my_height)+height)%height);
		rmap.WEST_MINE=new RegionDouble(own_x,own_y,
				own_x + AOI , own_y+my_height);

		rmap.EAST_OUT=new RegionDouble((own_x+my_width+width)%width,(own_y+height)%height,
				(own_x+my_width+AOI+width)%width, (own_y+my_height+height)%height);
		rmap.EAST_MINE=new RegionDouble(own_x + my_width - AOI,own_y,
				own_x +my_width , own_y+my_height);

		rmap.NORTH_OUT=new RegionDouble((own_x+width)%width, (own_y - AOI+height)%height,
				(own_x+ my_width +width)%width,(own_y+height)%height);
		rmap.NORTH_MINE=new RegionDouble(own_x ,own_y,
				own_x+my_width, own_y + AOI );

		rmap.SOUTH_OUT=new RegionDouble((own_x+width)%width,(own_y+my_height+height)%height,
				(own_x+my_width+width)%width, (own_y+my_height+AOI+height)%height);
		rmap.SOUTH_MINE=new RegionDouble(own_x,own_y+my_height-AOI,
				own_x+my_width, (own_y+my_height));

		return true;
	}

	/**
	 * Set a available location to a Remote Agent:
	 * it generates the location depending on the field of expertise
	 * @return The location assigned to Remote Agent
	 */
	@Override
	public Double2D getAvailableRandomLocation()
	{
		double shiftx=((DistributedState)sm).random.nextDouble();
		double shifty=((DistributedState)sm).random.nextDouble();
		double x=(own_x+AOI)+((my_width+own_x-AOI)-(own_x+AOI))*shiftx;
		double y=(own_y+AOI)+((my_height+own_y-AOI)-(own_y+AOI))*shifty;

		//rm.setPos(new Double2D(x,y));

		return (new Double2D(x, y));
	}

	/**  
	 * Provide the shift logic of the agents among the peers
	 * @param location The new location of the remote agent
	 * @param rm The remote agent to be stepped
	 * @param sm SimState of simulation
	 * @return 1 if it's in the field, -1 if there's an error (setObjectLocation returns null)
	 */
	@Override
	//public boolean setDistributedObjectLocation(final Double2D location,RemotePositionedAgent<Double2D> rm,SimState sm){
	public boolean setDistributedObjectLocation(final Double2D location,Object remoteObject,SimState sm) throws DMasonException
	{


		RemotePositionedAgent<Double2D> rm=null;

			if(remoteObject instanceof RemotePositionedAgent ){
			if(((RemotePositionedAgent)remoteObject).getPos() instanceof Double2D){

			rm=(RemotePositionedAgent<Double2D>) remoteObject;	
			}
			else{throw new DMasonException("Cast Exception setDistributedObjectLocation, second input parameter RemotePositionedAgent<E>, E must be a Double2D");}
		}
		else{throw new DMasonException("Cast Exception setDistributedObjectLocation, second input parameter must be a RemotePositionedAgent<>");}
		 

		if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
			writer.setPixel((int)(location.x%my_width), (int)(location.y%my_height), white);
		if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
			tmp_zoom.add(rm);

		if(setAgents(rm, location))
		{
			return true;
		}
		else
		{
			String errorMessage = String.format("Agent %s tried to set position (%f, %f): out of boundaries on cell %s. (ex OH MY GOD!)",
					rm.getId(), location.x, location.y, cellType);
			logger.severe( errorMessage );
		}

		return false;
	}

	/**
	 * 	This method provides the synchronization in the distributed environment.
	 * 	It's called after every step of schedule.
	 */
	@Override
	public synchronized boolean synchro() 
	{
		ConnectionJMS conn = (ConnectionJMS)((DistributedState<?>)sm).getCommunicationVisualizationConnection();
		Connection connWorker = (ConnectionJMS)((DistributedState<?>)sm).getCommunicationWorkerConnection();

		// If there is any viewer, send a snap
		if(conn!=null &&
				((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
		{
			RemoteSnap snap = new RemoteSnap(cellType, sm.schedule.getSteps() - 1, actualTime);
			actualTime = sm.schedule.getTime();

			if (isSendingGraphics)
			{
				try
				{
					ByteArrayOutputStream by = new ByteArrayOutputStream();
					ImageIO.write(currentBitmap, "png", by);
					by.flush();
					snap.image = by.toByteArray();
					by.close();
					currentBitmap = new BufferedImage((int)my_width, (int)my_height, BufferedImage.TYPE_3BYTE_BGR);
					writer=currentBitmap.getRaster();
				}
				catch(Exception e)
				{
					System.out.println("Do not use the GlobalViewer, the requirements of the simulation exceed the limits of the BufferedImage.\n");
				}
			}

			//if (isSendingGraphics || tracing.size() > 0)
			/* The above line is commented because if we don't send the
			 * RemoteSnap at every simulation step, the global viewer
			 * will block waiting on the queue.
			 */
			{
				try
				{
					snap.stats = actualStats;
					conn.publishToTopic(snap, "GRAPHICS", "GRAPHICS");
				} catch (Exception e) {
					logger.severe("Error while publishing the snap message");
					e.printStackTrace();
				}
			}

			// Update statistics
			Class<?> simClass = sm.getClass();
			for (int i = 0; i < tracing.size(); i++)
			{
				try
				{
					Method m = simClass.getMethod("get" + tracing.get(i), (Class<?>[])null);
					Object res = m.invoke(sm, new Object [0]);
					snap.stats.put(tracing.get(i), res);
				} catch (Exception e) {
					logger.severe("Reflection error while calling get" + tracing.get(i));
					e.printStackTrace();
				}
			}

		} //numViewers > 0

		for(Region<Double, Double2D> region : updates_cache)
		{
			for(EntryAgent<Double2D> remote_agent : region.values())
			{
				this.remove(remote_agent.r);
			}
		}

		//every agent in the myfield region is scheduled
		for(EntryAgent<Double2D> e: myfield.values())
		{
			RemotePositionedAgent<Double2D> rm=e.r;
			Double2D loc=e.l;
			rm.setPos(loc);
			this.remove(rm);
			sm.schedule.scheduleOnce(rm);
			setObjectLocation(rm,loc);

		}   

		updateFields(); //update fields with java reflect
		updates_cache=new ArrayList<Region<Double,Double2D>>();

		memorizeRegionOut();

		//--> publishing the regions to correspondent topics for the neighbors
		if(rmap.WEST_OUT!=null)
		{
			try 
			{
				DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.WEST_MINE,rmap.WEST_OUT,
						(sm.schedule.getSteps()-1),cellType,DistributedRegion.WEST);

				connWorker.publishToTopic(dr,topicPrefix+cellType+"L",name);

			} catch (Exception e1) { e1.printStackTrace();}
		}
		if(rmap.EAST_OUT!=null)
		{
			try 
			{
				DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.EAST_MINE,rmap.EAST_OUT,
						(sm.schedule.getSteps()-1),cellType,DistributedRegion.EAST);				

				connWorker.publishToTopic(dr,topicPrefix+cellType.toString()+"R",name);

			} catch (Exception e1) {e1.printStackTrace(); }
		}
		if(rmap.NORTH_OUT!=null )
		{
			try 
			{
				DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.NORTH_MINE,rmap.NORTH_OUT,
						(sm.schedule.getSteps()-1),cellType,DistributedRegion.NORTH);

				connWorker.publishToTopic(dr,cellType.toString()+"U",name);

			} catch (Exception e1) {e1.printStackTrace();}
		}

		if(rmap.SOUTH_OUT!=null )
		{
			try 
			{
				DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.SOUTH_MINE,rmap.SOUTH_OUT,
						(sm.schedule.getSteps()-1),cellType,DistributedRegion.SOUTH);

				connWorker.publishToTopic(dr,cellType.toString()+"D",name);

			} catch (Exception e1) { e1.printStackTrace(); }
		}

		if(rmap.NORTH_WEST_OUT!=null)
		{
			DistributedRegion<Double,Double2D> dr=
					new DistributedRegion<Double,Double2D>(rmap.NORTH_WEST_MINE,
					rmap.NORTH_WEST_OUT,
					(sm.schedule.getSteps()-1),cellType,DistributedRegion.NORTH_WEST);
			try 
			{
				connWorker.publishToTopic(dr,cellType.toString()+"CUDL",name);

			} catch (Exception e1) { e1.printStackTrace();}

		}
		if(rmap.NORTH_EAST_OUT!=null)
		{
			DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(
					rmap.NORTH_EAST_MINE,
					rmap.NORTH_EAST_OUT,
					(sm.schedule.getSteps()-1),cellType,DistributedRegion.NORTH_EAST);
			try 
			{

				connWorker.publishToTopic(dr,cellType.toString()+"CUDR",name);

			} catch (Exception e1) {e1.printStackTrace();}
		}
		if( rmap.SOUTH_WEST_OUT!=null)
		{
			DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(rmap.SOUTH_WEST_MINE,
					rmap.SOUTH_WEST_OUT,(sm.schedule.getSteps()-1),cellType,DistributedRegion.SOUTH_WEST);
			try 
			{
				connWorker.publishToTopic(dr,cellType.toString()+"CDDL",name);

			} catch (Exception e1) {e1.printStackTrace();}
		}
		if(rmap.SOUTH_EAST_OUT!=null)
		{
			DistributedRegion<Double,Double2D> dr=new DistributedRegion<Double,Double2D>(
					rmap.SOUTH_EAST_MINE,
					rmap.SOUTH_EAST_OUT,(sm.schedule.getSteps()-1),cellType,DistributedRegion.SOUTH_EAST);

			try 
			{				

				connWorker.publishToTopic(dr,cellType.toString()+"CDDR",name);

			} catch (Exception e1) { e1.printStackTrace(); }
		}//<--

		//take from UpdateMap the updates for current last terminated step and use 
		//verifyUpdates() to elaborate informations

		PriorityQueue<Object> q;
		try 
		{
			q = updates.getUpdates(sm.schedule.getSteps()-1, 8);
			while(!q.isEmpty())
			{
				DistributedRegion<Double, Double2D> region=(DistributedRegion<Double,Double2D>)q.poll();
				verifyUpdates(region);
			}
		} catch (InterruptedException e1) {e1.printStackTrace(); } catch (DMasonException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for(Region<Double, Double2D> region : updates_cache)
			for(EntryAgent<Double2D> e_m: region.values())
			{
				RemotePositionedAgent<Double2D> rm=e_m.r;
				((DistributedState<Double2D>)sm).addToField(rm,e_m.l);
			}

		this.reset();
		/*
		/ps.println(sm.schedule.getSteps()+";"+System.currentTimeMillis());
		 */

		if(conn!=null && 
				((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
		{
			try {
				tmp_zoom.STEP=((DistributedMultiSchedule)sm.schedule).getSteps()-1;
				conn.publishToTopic(tmp_zoom,"GRAPHICS"+cellType,name);
				tmp_zoom=new ZoomArrayList<RemotePositionedAgent>();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return true;
	}

	/**
	 * Method written with Java Reflect that memorizes the agent of out regions in the update cache
	 */
	private void memorizeRegionOut()
	{
		Class o=rmap.getClass();

		Field[] fields = o.getDeclaredFields();
		for (int z = 0; z < fields.length; z++)
		{
			fields[z].setAccessible(true);
			try
			{
				String name=fields[z].getName();
				Method method = o.getMethod("get"+name, null);
				Object returnValue = method.invoke(rmap, null);
				if(returnValue!=null)
				{
					Region<Double,Double2D> region=((Region<Double,Double2D>)returnValue);
					if(name.contains("out"))
					{
						updates_cache.add(region.clone());
					}
				}
			}
			catch (IllegalArgumentException e){e.printStackTrace();} 
			catch (IllegalAccessException e) {e.printStackTrace();} 
			catch (SecurityException e) {e.printStackTrace();} 
			catch (NoSuchMethodException e) {e.printStackTrace();} 
			catch (InvocationTargetException e) {e.printStackTrace();}
		}
	}

	/**
	 * This method takes updates from box and schedules every agent in the regions out.
	 * Every agent in the regions mine is compared with every agent in the updates_cache:
	 * if they are not equals, that in box mine is added.
	 * 
	 * @param box A Distributed Region that contains the updates
	 */
	private void verifyUpdates(DistributedRegion<Double,Double2D> box)
	{
		Region<Double,Double2D> r_mine=box.out;
		Region<Double,Double2D> r_out=box.mine;

		for(EntryAgent<Double2D> e_m: r_mine.values())
		{
			RemotePositionedAgent<Double2D> rm=e_m.r;
			((DistributedState<Double2D>)sm).addToField(rm,e_m.l);
			rm.setPos(e_m.l);
			//setPortrayalForObject(rm);
			sm.schedule.scheduleOnce(rm);
		}

		updates_cache.add(r_out);
	}

	/**
	 * This method, written with Java Reflect, follows two logical ways for all the regions:
	 * - if a region is an out one, the agent's location is updated and it's insert a new Entry 
	 * 		in the updates_cache (cause the agent is moving out and it's important to maintain the information
	 * 		for the next step)
	 * - if a region is a mine one, the agent's location is updated and the agent is scheduled.
	 */
	private void updateFields()
	{
		Class o=rmap.getClass();

		Field[] fields = o.getDeclaredFields();
		for (int z = 0; z < fields.length; z++)
		{
			fields[z].setAccessible(true);
			try
			{
				String name=fields[z].getName();
				Method method = o.getMethod("get"+name, null);
				Object returnValue = method.invoke(rmap, null);
				if(returnValue!=null)
				{
					Region<Double,Double2D> region=((Region<Double,Double2D>)returnValue);
					if(name.contains("out"))
					{
						for(EntryAgent<Double2D> e: region.values())
						{
							RemotePositionedAgent<Double2D> rm=e.r;
							rm.setPos(e.l);
							this.remove(rm);
						} 
					}
				}
			}
			catch (IllegalArgumentException e){e.printStackTrace();} 
			catch (IllegalAccessException e) {e.printStackTrace();} 
			catch (SecurityException e) {e.printStackTrace();} 
			catch (NoSuchMethodException e) {e.printStackTrace();} 
			catch (InvocationTargetException e) {e.printStackTrace();}
		}	     
	}

	/**
	 * This method, written with Java Reflect, provides to add the Remote Agent
	 * in the right Region.
	 * @param rm The Remote Agent to add
	 * @param location The new location of the Remote Agent
	 * @return true if the agent is added in right way
	 */
	private boolean setAgents(RemotePositionedAgent<Double2D> rm,Double2D location)
	{

		if(rmap.NORTH_WEST_MINE!=null && rmap.NORTH_WEST_MINE.isMine(location.x,location.y))
		{
			if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
				tmp_zoom.add(rm);
			if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
				GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);

			rmap.NORTH_WEST_MINE.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
			rmap.WEST_MINE.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
			myfield.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));	
			return rmap.NORTH_MINE.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
		}
		else
			if(rmap.NORTH_EAST_MINE!=null && rmap.NORTH_EAST_MINE.isMine(location.x,location.y))
			{
				if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
					tmp_zoom.add(rm);
				if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
					GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
				rmap.NORTH_EAST_MINE.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
				rmap.EAST_MINE.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
				myfield.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
				return rmap.NORTH_MINE.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
			}
			else
				if(rmap.SOUTH_WEST_MINE!=null && rmap.SOUTH_WEST_MINE.isMine(location.x,location.y))
				{
					if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
						tmp_zoom.add(rm);
					if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
						GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
					rmap.SOUTH_WEST_MINE.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
					rmap.WEST_MINE.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
					myfield.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
					return rmap.SOUTH_MINE.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
				}
				else
					if(rmap.SOUTH_EAST_MINE!=null && rmap.SOUTH_EAST_MINE.isMine(location.x,location.y))
					{
						if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
							tmp_zoom.add(rm);
						if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
							GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
						rmap.SOUTH_EAST_MINE.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
						rmap.EAST_MINE.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
						myfield.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
						return rmap.SOUTH_MINE.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
					}
					else
						if(rmap.WEST_MINE != null && rmap.WEST_MINE.isMine(location.x,location.y))
						{
							if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
								tmp_zoom.add(rm);
							if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
								GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
							myfield.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
							return rmap.WEST_MINE.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
						}
						else
							if(rmap.EAST_MINE != null && rmap.EAST_MINE.isMine(location.x,location.y))
							{
								if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
									tmp_zoom.add(rm);
								if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
									GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
								myfield.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
								return rmap.EAST_MINE.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
							}
							else
								if(rmap.NORTH_MINE != null && rmap.NORTH_MINE.isMine(location.x,location.y))
								{
									if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
										tmp_zoom.add(rm);
									if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
										GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
									myfield.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
									return rmap.NORTH_MINE.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
								}
								else
									if(rmap.SOUTH_MINE != null && rmap.SOUTH_MINE.isMine(location.x,location.y))
									{
										if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
											tmp_zoom.add(rm);
										if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
											GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
										myfield.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
										return rmap.SOUTH_MINE.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
									}
									else
										if(myfield.isMine(location.x,location.y))
										{
											if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
												tmp_zoom.add(rm);
											if(((DistributedMultiSchedule)((DistributedState)sm).schedule).numViewers.getCount()>0)
												GlobalInspectorHelper.updateBitmap(currentBitmap, rm, location, own_x, own_y);
											return myfield.addAgents(new EntryAgent<Double2D>(rm, new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI)));
										}
										else
											if(rmap.WEST_OUT!=null && rmap.WEST_OUT.isMine(location.x,location.y)) 
												return rmap.WEST_OUT.addAgents(new EntryAgent<Double2D>(rm, location));
											else
												if(rmap.EAST_OUT!=null && rmap.EAST_OUT.isMine(location.x,location.y)) 
													return rmap.EAST_OUT.addAgents(new EntryAgent<Double2D>(rm, location));
												else
													if(rmap.NORTH_OUT!=null && rmap.NORTH_OUT.isMine(location.x,location.y))
														return rmap.NORTH_OUT.addAgents(new EntryAgent<Double2D>(rm, location));
													else
														if(rmap.SOUTH_OUT!=null && rmap.SOUTH_OUT.isMine(location.x,location.y))
															return rmap.SOUTH_OUT.addAgents(new EntryAgent<Double2D>(rm, location));
														else
															if(rmap.NORTH_WEST_OUT!=null && rmap.NORTH_WEST_OUT.isMine(location.x,location.y)) 
																return rmap.NORTH_WEST_OUT.addAgents(new EntryAgent<Double2D>(rm, location));
															else 
																if(rmap.SOUTH_WEST_OUT!=null && rmap.SOUTH_WEST_OUT.isMine(location.x,location.y)) 
																	return rmap.SOUTH_WEST_OUT.addAgents(new EntryAgent<Double2D>(rm, location));
																else
																	if(rmap.NORTH_EAST_OUT!=null && rmap.NORTH_EAST_OUT.isMine(location.x,location.y)) 
																		return rmap.NORTH_EAST_OUT.addAgents(new EntryAgent<Double2D>(rm, location));
																	else
																		if(rmap.SOUTH_EAST_OUT!=null && rmap.SOUTH_EAST_OUT.isMine(location.x,location.y))
																			return rmap.SOUTH_EAST_OUT.addAgents(new EntryAgent<Double2D>(rm, location));

		return false;
	}

	/**
	 * Clear all Regions.
	 * @return true if the clearing is successful, false if exception is generated
	 */
	private boolean reset()
	{
		myfield.clear();	

		Class o=rmap.getClass();

		Field[] fields = o.getDeclaredFields();
		for (int z = 0; z < fields.length; z++)
		{
			fields[z].setAccessible(true);
			try
			{
				String name=fields[z].getName();
				Method method = o.getMethod("get"+name, null);
				Object returnValue = method.invoke(rmap, null);
				if(returnValue!=null)
				{
					Region<Double,Double2D> region=((Region<Double,Double2D>)returnValue);
					region.clear();    
				}
			}
			catch (IllegalArgumentException e){e.printStackTrace(); return false;} 
			catch (IllegalAccessException e) {e.printStackTrace();return false;} 
			catch (SecurityException e) {e.printStackTrace();return false;} 
			catch (NoSuchMethodException e) {e.printStackTrace();return false;} 
			catch (InvocationTargetException e) {e.printStackTrace();return false;}
		}
		return true;
	}


	/**
	 * Implemented method from the abstract class.
	 */
	@Override
	public DistributedState getState() { return (DistributedState)sm; }

	//getters and setters
	public double getOwn_x() { return own_x; }
	public void setOwn_x(double own_x) { this.own_x = own_x; }
	public double getOwn_y() {	return own_y; }
	public void setOwn_y(double own_y) { this.own_y = own_y; }


	@Override
	public void setTable(HashMap table) {
		ConnectionJMS conn = (ConnectionJMS) ((DistributedState<?>)sm).getCommunicationManagementConnection();
		if(conn!=null)
			conn.setTable(table);
	}

	@Override
	public String getDistributedFieldID() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public UpdateMap getUpdates() {
		// TODO Auto-generated method stub
		return updates;
	}

	
	public void resetParameters() {
		
	}

	@Override
	public void trace(String param)
	{
		if (param.equals("-GRAPHICS"))
			isSendingGraphics = true;
		else
			tracing.add(param);
	}

	@Override
	public void untrace(String param)
	{
		if (param.equals("-GRAPHICS"))
			isSendingGraphics = false;
		else
		{
			tracing.remove(param);
			actualStats.remove(param);
		}
	}


	@Override
	public Double2D getObjectLocationThin(Object obj) {
		Double2D loc=super.getObjectLocation(obj);
		return new Double2D(loc.x+own_x-2*AOI,loc.y+own_y-2*AOI);
	}

	@Override
	public Double2D getObjectLocationAsDouble2DThin(Object obj) {
		Double2D loc=super.getObjectLocationAsDouble2D(obj);
		return new Double2D(loc.x+own_x-2*AOI,loc.y+own_y-2*AOI);
	}

	@Override
	public boolean setObjectLocationThin(Object obj, Double2D location) {
		Double2D loc=new Double2D(location.x-own_x+2*AOI,location.y-own_y+2*AOI);
		return super.setObjectLocation(obj, loc);
	}

	@Override
	public Bag getRawObjectsAtLocationThin(MutableInt2D loc) {
		int ownxD= (int)((own_x+2*AOI) / discretization);
		int ownyD= (int)((own_y+2*AOI) / discretization);
		return super.getRawObjectsAtLocation(new Double2D((loc.x-ownxD), (loc.y-ownyD)));
	}

	@Override
	public Bag getRawObjectsAtLocationThin(Int2D loc) {
		int ownxD= (int)((own_x+2*AOI) / discretization);
		int ownyD= (int)((own_y+2*AOI) / discretization);
		return super.getRawObjectsAtLocation(new Double2D((loc.x-ownxD), (loc.y-ownyD)));
	}

	@Override
	public VisualizationUpdateMap<String, Object> getGlobals()
	{
		return globals;
	}
	@Override
	public boolean verifyPosition(Double2D pos) {

		//we have to implement this
		return false;

	}
}