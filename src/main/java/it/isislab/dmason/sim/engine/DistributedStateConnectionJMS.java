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

package it.isislab.dmason.sim.engine;

import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.experimentals.util.management.globals.UpdaterThreadForGlobalsListener;
import it.isislab.dmason.experimentals.util.management.globals.util.UpdateGlobalVarAtStep;
import it.isislab.dmason.experimentals.util.trigger.Trigger;
import it.isislab.dmason.experimentals.util.visualization.globalviewer.ThreadVisualizationCellMessageListener;
import it.isislab.dmason.experimentals.util.visualization.zoomviewerapp.ThreadZoomInCellMessageListener;
import it.isislab.dmason.nonuniform.QuadTree;
import it.isislab.dmason.nonuniform.QuadTree.ORIENTATION;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.DistributedFieldNetwork;
import it.isislab.dmason.sim.field.MessageListener;
import it.isislab.dmason.sim.field.UpdaterThreadForListener;
import it.isislab.dmason.sim.field.network.DNetwork;
import it.isislab.dmason.sim.field.support.network.DNetworkJMSMessageListener;
import it.isislab.dmason.sim.field.support.network.UpdaterThreadJMSForNetworkListener;
import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;
import it.isislab.dmason.util.connection.jms.activemq.ConnectionNFieldsWithActiveMQAPI;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 * 
 * @param <E> the type of locations   
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Luca Vicidomini       
 */
public class DistributedStateConnectionJMS<E> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String ip;
	public String port;
	protected ConnectionJMS connectionJMS;
	private ArrayList<MessageListener> listeners = new ArrayList<MessageListener>();
	protected ArrayList<DNetworkJMSMessageListener> networkListeners = new ArrayList<DNetworkJMSMessageListener>();
	private UpdaterThreadForListener u1;
	private UpdaterThreadForListener u2;
	private UpdaterThreadForListener u3;
	private UpdaterThreadForListener u4;
	private UpdaterThreadForListener u5;
	private UpdaterThreadForListener u6;
	private UpdaterThreadForListener u7;
	private UpdaterThreadForListener u8;
	protected DistributedState dm;
	protected Trigger TRIGGER;
	protected DistributedMultiSchedule<E> schedule;
	protected String topicPrefix;
	protected CellType TYPE;
	protected int MODE;
	protected int NUMPEERS;
	protected int rows;
	protected int columns;
	protected HashMap<String, Integer> networkNumberOfSubscribersForField;

	public DistributedStateConnectionJMS()
	{

	}
	public DistributedStateConnectionJMS(DistributedState dm, String ip,String port) {
		this.ip = ip;
		this.port = port;
		this.dm=dm;
		connectionJMS = new ConnectionNFieldsWithActiveMQAPI();

		/**
		 * 		try {
			connectionJMS.setupConnection(new Address(ip, port));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.TRIGGER = new Trigger(connectionJMS);
		 */
		schedule=(DistributedMultiSchedule<E>)dm.schedule;
		topicPrefix=dm.topicPrefix;
		TYPE=dm.TYPE;
		MODE=dm.MODE;
		NUMPEERS=dm.NUMPEERS;
		rows=dm.rows;
		columns=dm.columns;
		networkNumberOfSubscribersForField=dm.networkNumberOfSubscribersForField;
	}

//	//this is only for test
//	public DistributedStateConnectionJMS(DistributedState dm, String ip,String port,ConnectionJMS connectionJMS,boolean thisIsATest) {
//		this.ip = ip;
//		this.port = port;
//		this.dm=dm;
//
//		this.connectionJMS = new ConnectionNFieldsWithActiveMQAPI();
//		//this.connectionJMS=connectionJMS;
//		/**
//		 * 		try {
//			connectionJMS.setupConnection(new Address(ip, port));
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		this.TRIGGER = new Trigger(connectionJMS);
//		 */
//		schedule=(DistributedMultiSchedule<E>)dm.schedule;
//		topicPrefix=dm.topicPrefix;
//		TYPE=dm.TYPE;
//		MODE=dm.MODE;
//		NUMPEERS=dm.NUMPEERS;
//		rows=dm.rows;
//		columns=dm.columns;
//		networkNumberOfSubscribersForField=dm.networkNumberOfSubscribersForField;
//	}

	public int CONNECTIONS_CREATED_STATUS_P;
	public boolean CONNECTIONS_CREATED=false;
	public void init_connection() {

		try {
			connectionJMS.setupConnection(new Address(ip, port));
			
			if(MODE == DistributedField2D.NON_UNIFORM_PARTITIONING_MODE)
			{
				try {
					connectionJMS.createTopic("CONNECTIONS_CREATED", 1);
					connectionJMS.subscribeToTopic("CONNECTIONS_CREATED");
					connectionJMS.asynchronousReceive("CONNECTIONS_CREATED", new MyMessageListener() {
						
						@Override
						public void onMessage(Message msg) {
							// TODO Auto-generated method stub
							CONNECTIONS_CREATED_STATUS_P++;
							if(CONNECTIONS_CREATED_STATUS_P==dm.P)
							{
								CONNECTIONS_CREATED=true;
								lock.lock();
									block.signal();
								lock.unlock();
							}
						}
					});
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.TRIGGER = new Trigger(connectionJMS);
		if(((DistributedMultiSchedule<E>)dm.schedule).fields2D.size()>0)
			init_spatial_connection();
		if(((DistributedMultiSchedule<E>)dm.schedule).fieldsNetwork.size()>0)
			init_network_connection();
	
		//FOR GRAPHIC TESTING 
		
		try {
			connectionJMS.createTopic(topicPrefix+"GRAPHICS", 1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	private  ReentrantLock lock;
	private  Condition block;
	public void initNonUnfiromCommunication(QuadTree q)
	{
		lock=new ReentrantLock();
		block=lock.newCondition();
		
		try {

			for(ORIENTATION neighbors:q.neighborhood.keySet())
			{
				//System.err.println(this.TYPE+" crea "+ topicPrefix+q.ID + neighbors);
				connectionJMS.createTopic(topicPrefix+q.ID + neighbors,
						schedule.fields2D
						.size());	
				
//				for(QuadTree neighbor:q.neighborhood.get(neighbors))
//				{
//					System.err.println(this.TYPE+" si sottoscrive a  "+topicPrefix+neighbor.ID+QuadTree.swapOrientation(neighbors));
//					connectionJMS.subscribeToTopic(topicPrefix+neighbor.ID+QuadTree.swapOrientation(neighbors));
//					UpdaterThreadForListener u1 = new UpdaterThreadForListener(
//					connectionJMS,topicPrefix+neighbor.ID+QuadTree.swapOrientation(neighbors),schedule.fields2D, listeners);
//					u1.start();
//				}

			}
			
			for(ORIENTATION neighbors:q.toSubscribe.keySet())
			{
				
				for(QuadTree neighbor:q.toSubscribe.get(neighbors))
				{
					//System.err.println(this.TYPE+" si sottoscrive a  "+topicPrefix+neighbor.ID+(neighbors));
					connectionJMS.subscribeToTopic(topicPrefix+neighbor.ID+(neighbors));
					UpdaterThreadForListener u1 = new UpdaterThreadForListener(
					connectionJMS,topicPrefix+neighbor.ID+(neighbors),schedule.fields2D, listeners);
					u1.start();
				}

			}
			connectionJMS.publishToTopic("READY "+q.ID, "CONNECTIONS_CREATED", "");	

			while(!CONNECTIONS_CREATED)
			{
				//Block this thread
				lock.lock();
					block.await();
				lock.unlock();
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	protected void init_spatial_connection() {
		boolean toroidal_need=false;
		for(DistributedField2D field : 
			((DistributedMultiSchedule<E>)dm.schedule).getFields())
		{
			if(field.isToroidal())
			{
				toroidal_need=true;
				break;
			}
		}
		//only for global variables
		dm.upVar=new UpdateGlobalVarAtStep(dm);
		ThreadVisualizationCellMessageListener thread = new ThreadVisualizationCellMessageListener(
				connectionJMS,
				((DistributedMultiSchedule) this.schedule));
		thread.start();

		try {
			boolean a = connectionJMS.createTopic(topicPrefix+"GRAPHICS" + TYPE,
					schedule.fields2D.size());
			connectionJMS.subscribeToTopic(topicPrefix+"GRAPHICS" + TYPE);
			ThreadZoomInCellMessageListener t_zoom = new ThreadZoomInCellMessageListener(
					connectionJMS,
					TYPE.toString(), (DistributedMultiSchedule) this.schedule);
			t_zoom.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (toroidal_need)
		{
			connection_IS_toroidal();
		}
		else
		{
			connection_NO_toroidal();
		}

		// Support for Global Parameters
		try {
			connectionJMS.subscribeToTopic(topicPrefix + "GLOBAL_REDUCED");
			UpdaterThreadForGlobalsListener ug = new UpdaterThreadForGlobalsListener(
					connectionJMS,
					topicPrefix + "GLOBAL_REDUCED",
					schedule.fields2D,
					listeners);
			ug.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void connection_IS_toroidal() {

		if (MODE == DistributedField2D.UNIFORM_PARTITIONING_MODE) { // HORIZONTAL_MODE
			int i = TYPE.pos_i, j = TYPE.pos_j;
			try {

				//one columns and N rows
				if(rows > 1 && columns == 1){
					connectionJMS.createTopic(topicPrefix+TYPE + "N",
							schedule.fields2D
							.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "S",
							schedule.fields2D
							.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "NW",
							schedule.fields2D
							.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "NE",
							schedule.fields2D
							.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "SW",
							schedule.fields2D
							.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "SE",
							schedule.fields2D
							.size());
					
					connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "N");
					connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "S");
					
					connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "SE");
					connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "SW");
					connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "NE");
					connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "NW");
					
					u3 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
									+ ((j + columns) % columns) + "N",
									schedule.fields2D, listeners);
					u3.start();

					u4 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
									+ ((j + columns) % columns) + "S",
									schedule.fields2D, listeners);
					u4.start();

					u5 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
									+ ((j - 1 + columns) % columns) + "SE",
									schedule.fields2D, listeners);
					u5.start();

					u6 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
									+ ((j + 1 + columns) % columns) + "SW",
									schedule.fields2D, listeners);
					u6.start();

					u7 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
									+ ((j - 1 + columns) % columns) + "NE",
									schedule.fields2D, listeners);
					u7.start();

					u8 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
									+ ((j + 1 + columns) % columns) + "NW",
									schedule.fields2D, listeners);
					u8.start();
				}
				//one rows and N columns
				else if(rows==1 && columns>1){
					connectionJMS.createTopic(topicPrefix+TYPE + "W",
							schedule.fields2D
							.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "E",
							schedule.fields2D
							.size());

					connectionJMS.createTopic(topicPrefix+TYPE + "NW",
							schedule.fields2D
							.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "NE",
							schedule.fields2D
							.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "SW",
							schedule.fields2D
							.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "SE",
							schedule.fields2D
							.size());

					

					connectionJMS.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "W");
					connectionJMS.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "E");

					connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "SE");
					connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "SW");
					connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "NE");
					connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "NW");

					u1 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + rows) % rows) + "-"
									+ ((j + 1 + columns) % columns) + "W",
									schedule.fields2D, listeners);
					u1.start();

					u2 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + rows) % rows) + "-"
									+ ((j - 1 + columns) % columns) + "E",
									schedule.fields2D, listeners);
					u2.start();


					u5 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
									+ ((j - 1 + columns) % columns) + "SE",
									schedule.fields2D, listeners);
					u5.start();

					u6 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
									+ ((j + 1 + columns) % columns) + "SW",
									schedule.fields2D, listeners);
					u6.start();

					u7 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
									+ ((j - 1 + columns) % columns) + "NE",
									schedule.fields2D, listeners);
					u7.start();

					u8 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
									+ ((j + 1 + columns) % columns) + "NW",
									schedule.fields2D, listeners);
					u8.start();
				}else{
					// N rows and N columns
					connectionJMS.createTopic(topicPrefix+TYPE + "W",
							schedule.fields2D
							.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "E",
							schedule.fields2D
							.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "S",
							schedule.fields2D
							.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "N",
							schedule.fields2D
							.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "NW",
							schedule.fields2D
							.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "NE",
							schedule.fields2D
							.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "SW",
							schedule.fields2D
							.size());
					connectionJMS.createTopic(topicPrefix+TYPE + "SE",
							schedule.fields2D
							.size());
					

					connectionJMS.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "W");
					connectionJMS.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "E");
					connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "N");
					connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + columns) % columns) + "S");
					connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "SE");
					connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "SW");
					connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "NE");
					connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "NW");

					u1 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + rows) % rows) + "-"
									+ ((j + 1 + columns) % columns) + "W",
									schedule.fields2D, listeners);
					u1.start();

					u2 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + rows) % rows) + "-"
									+ ((j - 1 + columns) % columns) + "E",
									schedule.fields2D, listeners);
					u2.start();

					u3 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
									+ ((j + columns) % columns) + "N",
									schedule.fields2D, listeners);
					u3.start();

					u4 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
									+ ((j + columns) % columns) + "S",
									schedule.fields2D, listeners);
					u4.start();

					u5 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
									+ ((j - 1 + columns) % columns) + "SE",
									schedule.fields2D, listeners);
					u5.start();

					u6 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
									+ ((j + 1 + columns) % columns) + "SW",
									schedule.fields2D, listeners);
					u6.start();

					u7 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
									+ ((j - 1 + columns) % columns) + "NE",
									schedule.fields2D, listeners);
					u7.start();

					u8 = new UpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
									+ ((j + 1 + columns) % columns) + "NW",
									schedule.fields2D, listeners);
					u8.start();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(MODE == DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE){
			int i = TYPE.pos_i, j = TYPE.pos_j;
			try{
			connectionJMS.createTopic(topicPrefix+TYPE + "W",
					schedule.fields2D
					.size());
			connectionJMS.createTopic(topicPrefix+TYPE + "E",
					schedule.fields2D
					.size());

			connectionJMS.createTopic(topicPrefix+TYPE + "NW",
					schedule.fields2D
					.size());
			connectionJMS.createTopic(topicPrefix+TYPE + "NE",
					schedule.fields2D
					.size());
			connectionJMS.createTopic(topicPrefix+TYPE + "SW",
					schedule.fields2D
					.size());
			connectionJMS.createTopic(topicPrefix+TYPE + "SE",
					schedule.fields2D
					.size());

			

			connectionJMS.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
					+ ((j + 1 + columns) % columns) + "W");
			connectionJMS.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
					+ ((j - 1 + columns) % columns) + "E");

			connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
					+ ((j - 1 + columns) % columns) + "SE");
			connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
					+ ((j + 1 + columns) % columns) + "SW");
			connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
					+ ((j - 1 + columns) % columns) + "NE");
			connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
					+ ((j + 1 + columns) % columns) + "NW");

			u1 = new UpdaterThreadForListener(
					connectionJMS, topicPrefix+((i + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "W",
							schedule.fields2D, listeners);
			u1.start();

			u2 = new UpdaterThreadForListener(
					connectionJMS, topicPrefix+((i + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "E",
							schedule.fields2D, listeners);
			u2.start();


			u5 = new UpdaterThreadForListener(
					connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "SE",
							schedule.fields2D, listeners);
			u5.start();

			u6 = new UpdaterThreadForListener(
					connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "SW",
							schedule.fields2D, listeners);
			u6.start();

			u7 = new UpdaterThreadForListener(
					connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "NE",
							schedule.fields2D, listeners);
			u7.start();

			u8 = new UpdaterThreadForListener(
					connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "NW",
							schedule.fields2D, listeners);
			u8.start();
			}catch(Exception e){
				e.printStackTrace();
			}
		} else if (MODE == DistributedField2D.SQUARE_BALANCED_DISTRIBUTION_MODE) { // SQUARE BALANCED

			try {

				connectionJMS.createTopic(topicPrefix+TYPE+"W",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"E",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"S",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"N",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"NW",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"NE",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"SW",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"SE",((DistributedMultiSchedule)schedule).fields2D.size());

				int i=TYPE.pos_i,j=TYPE.pos_j;
				int sqrt=(int)Math.sqrt(NUMPEERS);

				connectionJMS.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"W");
				connectionJMS.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"E");
				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"N");
				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"S");
				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"SE");
				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"SW");
				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"NE");
				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"NW");

				u1 = new UpdaterThreadForListener(connectionJMS,topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"W",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u1.start();

				u2 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"E",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u2.start();

				u3 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"N",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u3.start();

				u4 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"S",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u4.start();

				u5 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"SE",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u5.start();

				u6 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"SW",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u6.start();	

				u7 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"NE",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u7.start();		

				u8 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"NW",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u8.start();

			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(MODE == DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE){
			int i = TYPE.pos_i, j = TYPE.pos_j;
			
			//N columns and one row
			try{
			
			connectionJMS.createTopic(topicPrefix+TYPE + "W",
					schedule.fields2D
					.size());
			connectionJMS.createTopic(topicPrefix+TYPE + "E",
					schedule.fields2D
					.size());

			connectionJMS.createTopic(topicPrefix+TYPE + "NW",
					schedule.fields2D
					.size());
			connectionJMS.createTopic(topicPrefix+TYPE + "NE",
					schedule.fields2D
					.size());
			connectionJMS.createTopic(topicPrefix+TYPE + "SW",
					schedule.fields2D
					.size());
			connectionJMS.createTopic(topicPrefix+TYPE + "SE",
					schedule.fields2D
					.size());

			

			connectionJMS.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
					+ ((j + 1 + columns) % columns) + "W");
			connectionJMS.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
					+ ((j - 1 + columns) % columns) + "E");

			connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
					+ ((j - 1 + columns) % columns) + "SE");
			connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
					+ ((j + 1 + columns) % columns) + "SW");
			connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
					+ ((j - 1 + columns) % columns) + "NE");
			connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
					+ ((j + 1 + columns) % columns) + "NW");

			u1 = new UpdaterThreadForListener(
					connectionJMS, topicPrefix+((i + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "W",
							schedule.fields2D, listeners);
			u1.start();

			u2 = new UpdaterThreadForListener(
					connectionJMS, topicPrefix+((i + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "E",
							schedule.fields2D, listeners);
			u2.start();


			u5 = new UpdaterThreadForListener(
					connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "SE",
							schedule.fields2D, listeners);
			u5.start();

			u6 = new UpdaterThreadForListener(
					connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "SW",
							schedule.fields2D, listeners);
			u6.start();

			u7 = new UpdaterThreadForListener(
					connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "NE",
							schedule.fields2D, listeners);
			u7.start();

			u8 = new UpdaterThreadForListener(
					connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "NW",
							schedule.fields2D, listeners);
			u8.start();
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (MODE == DistributedField2D.NON_UNIFORM_PARTITIONING_MODE) { // NON UNFIRORM DISTRIBUTION MODE TOROIDAL

			try {
				System.out.println("Non Uniform Partitioning.");
				//				connectionJMS.createTopic(topicPrefix+TYPE+"W",((DistributedMultiSchedule)schedule).fields2D.size());
				//				connectionJMS.createTopic(topicPrefix+TYPE+"E",((DistributedMultiSchedule)schedule).fields2D.size());
				//				connectionJMS.createTopic(topicPrefix+TYPE+"S",((DistributedMultiSchedule)schedule).fields2D.size());
				//				connectionJMS.createTopic(topicPrefix+TYPE+"N",((DistributedMultiSchedule)schedule).fields2D.size());
				//				connectionJMS.createTopic(topicPrefix+TYPE+"NW",((DistributedMultiSchedule)schedule).fields2D.size());
				//				connectionJMS.createTopic(topicPrefix+TYPE+"NE",((DistributedMultiSchedule)schedule).fields2D.size());
				//				connectionJMS.createTopic(topicPrefix+TYPE+"SW",((DistributedMultiSchedule)schedule).fields2D.size());
				//				connectionJMS.createTopic(topicPrefix+TYPE+"SE",((DistributedMultiSchedule)schedule).fields2D.size());
				//
				//				int i=TYPE.pos_i,j=TYPE.pos_j;
				//				int sqrt=(int)Math.sqrt(NUMPEERS);
				//
				//				connectionJMS.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"W");
				//				connectionJMS.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"E");
				//				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"N");
				//				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"S");
				//				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"SE");
				//				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"SW");
				//				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"NE");
				//				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"NW");
				//
				//				u1 = new UpdaterThreadForListener(connectionJMS,topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"W",((DistributedMultiSchedule)schedule).fields2D,listeners);
				//				u1.start();
				//
				//				u2 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"E",((DistributedMultiSchedule)schedule).fields2D,listeners);
				//				u2.start();
				//
				//				u3 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"N",((DistributedMultiSchedule)schedule).fields2D,listeners);
				//				u3.start();
				//
				//				u4 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"S",((DistributedMultiSchedule)schedule).fields2D,listeners);
				//				u4.start();
				//
				//				u5 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"SE",((DistributedMultiSchedule)schedule).fields2D,listeners);
				//				u5.start();
				//
				//				u6 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"SW",((DistributedMultiSchedule)schedule).fields2D,listeners);
				//				u6.start();	
				//
				//				u7 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"NE",((DistributedMultiSchedule)schedule).fields2D,listeners);
				//				u7.start();		
				//
				//				u8 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"NW",((DistributedMultiSchedule)schedule).fields2D,listeners);
				//				u8.start();

			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}

	protected void connection_NO_toroidal() {

		if (MODE == DistributedField2D.UNIFORM_PARTITIONING_MODE) { // HORIZONTAL_MODE

			try {

				if(rows>1 && columns==1){
					if(TYPE.pos_i==0){
						//crea sotto e sottomettiti a i+1-spra
						connectionJMS.createTopic(topicPrefix+TYPE + "S",
								schedule.fields2D
								.size());
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDown() + "N");
						UpdaterThreadForListener u1 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourDown() + "N",
								schedule.fields2D, listeners);
						u1.start();
					}
					else if(TYPE.pos_i == rows-1){
						//crea sopra e sottomettiti a i-1-sotto
						connectionJMS.createTopic(topicPrefix+TYPE + "N",
								schedule.fields2D
								.size());
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourUp() + "S");
						UpdaterThreadForListener u1 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourUp() + "S",
								schedule.fields2D, listeners);
						u1.start();
					}
					else{
						connectionJMS.createTopic(topicPrefix+TYPE + "S",
								schedule.fields2D
								.size());
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDown() + "N");
						UpdaterThreadForListener u1 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourDown() + "N",
								schedule.fields2D, listeners);
						u1.start();

						connectionJMS.createTopic(topicPrefix+TYPE + "N",
								schedule.fields2D
								.size());
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourUp() + "S");
						UpdaterThreadForListener u2 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourUp() + "S",
								schedule.fields2D, listeners);
						u2.start();
					}	
					//crea sopra e sotto e sottomettiti a i-1-sotto e a i+1 sopra

				}
				else if(rows==1 && columns > 1){


					if(TYPE.pos_j < columns){
						connectionJMS.createTopic(topicPrefix+TYPE + "E",
								schedule.fields2D
								.size());

						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourRight() + "W");
						UpdaterThreadForListener u2 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourRight() + "W",
								schedule.fields2D, listeners);
						u2.start();

					}
					if(TYPE.pos_j > 0){
						connectionJMS.createTopic(topicPrefix+TYPE + "W",
								schedule.fields2D
								.size());

						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourLeft() + "E");
						UpdaterThreadForListener u1 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourLeft() + "E",
								schedule.fields2D, listeners);
						u1.start();
					}
				}else{
					//N rows and N columns
					if(TYPE.pos_j > 0)					
						connectionJMS.createTopic(topicPrefix+TYPE + "W",
								schedule.fields2D
								.size());
					if(TYPE.pos_j < columns)
						connectionJMS.createTopic(topicPrefix+TYPE + "E",
								schedule.fields2D
								.size());
					if(TYPE.pos_i > 0)
						connectionJMS.createTopic(topicPrefix+TYPE + "N",
								schedule.fields2D
								.size());
					if(TYPE.pos_i < rows)
						connectionJMS.createTopic(topicPrefix+TYPE + "S",
								schedule.fields2D
								.size());
					if(TYPE.pos_i < rows && TYPE.pos_j < columns)
						connectionJMS.createTopic(topicPrefix+TYPE + "SE",
								schedule.fields2D
								.size());
					if(TYPE.pos_i > 0 && TYPE.pos_j < columns)
						connectionJMS.createTopic(topicPrefix+TYPE + "NE",
								schedule.fields2D
								.size());
					if(TYPE.pos_i < rows && TYPE.pos_j > 0)
						connectionJMS.createTopic(topicPrefix+TYPE + "SW",
								schedule.fields2D
								.size());
					if(TYPE.pos_i > 0 && TYPE.pos_j > 0)
						connectionJMS.createTopic(topicPrefix+TYPE + "NW",
								schedule.fields2D
								.size());

					if(TYPE.pos_i > 0 && TYPE.pos_j > 0){
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagLeftUp()
						+ "SE");
						UpdaterThreadForListener u1 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourDiagLeftUp() + "SE",
								schedule.fields2D, listeners);
						u1.start();
					}
					if(TYPE.pos_i > 0 && TYPE.pos_j < columns){
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagRightUp()
						+ "SW");
						UpdaterThreadForListener u2 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourDiagRightUp() + "SW",
								schedule.fields2D, listeners);
						u2.start();
					}

					if(TYPE.pos_i < rows && TYPE.pos_j > 0){
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagLeftDown()
						+ "NE");
						UpdaterThreadForListener u3 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourDiagLeftDown() + "NE",
								schedule.fields2D, listeners);
						u3.start();
					}
					if(TYPE.pos_i < rows && TYPE.pos_j < columns){
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagRightDown()
						+ "NW");
						UpdaterThreadForListener u4 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourDiagRightDown() + "NW",
								schedule.fields2D, listeners);
						u4.start();
					}
					if(TYPE.pos_j > 0){
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourLeft() + "E");
						UpdaterThreadForListener u5 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourLeft() + "E",
								schedule.fields2D, listeners);
						u5.start();
					}
					if(TYPE.pos_j < columns){
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourRight() + "W");
						UpdaterThreadForListener u6 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourRight() + "W",
								schedule.fields2D, listeners);
						u6.start();
					}
					if(TYPE.pos_i > 0){	
						connectionJMS.subscribeToTopic(topicPrefix+(TYPE.getNeighbourUp() + "S"));
						UpdaterThreadForListener u7 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourUp() + "S",
								schedule.fields2D, listeners);
						u7.start();
					}
					if(TYPE.pos_i < rows){
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDown() + "N");
						UpdaterThreadForListener u8 = new UpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourDown() + "N",
								schedule.fields2D, listeners);
						u8.start();
					}
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}else if(MODE == DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE){
			// one row and N columns
			try{
			if(TYPE.pos_j < columns){
				connectionJMS.createTopic(topicPrefix+TYPE + "E",
						schedule.fields2D
						.size());

				connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourRight() + "W");
				UpdaterThreadForListener u2 = new UpdaterThreadForListener(
						connectionJMS, topicPrefix+TYPE.getNeighbourRight() + "W",
						schedule.fields2D, listeners);
				u2.start();

			}
			if(TYPE.pos_j > 0){
				connectionJMS.createTopic(topicPrefix+TYPE + "W",
						schedule.fields2D
						.size());

				connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourLeft() + "E");
				UpdaterThreadForListener u1 = new UpdaterThreadForListener(
						connectionJMS, topicPrefix+TYPE.getNeighbourLeft() + "E",
						schedule.fields2D, listeners);
				u1.start();
			}
			}catch(Exception e){
				e.printStackTrace();
			}
		} 
		 else if (MODE == DistributedField2D.SQUARE_BALANCED_DISTRIBUTION_MODE) { // SQUARE BALANCED

			try {

				connectionJMS.createTopic(topicPrefix+TYPE+"W",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"E",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"S",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"N",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"NW",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"NE",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"SW",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"SE",((DistributedMultiSchedule)schedule).fields2D.size());

				int i=TYPE.pos_i,j=TYPE.pos_j;
				int sqrt=(int)Math.sqrt(NUMPEERS);

				connectionJMS.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"W");
				connectionJMS.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"E");
				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"N");
				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"S");
				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"SE");
				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"SW");
				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"NE");
				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"NW");

				u1 = new UpdaterThreadForListener(connectionJMS,topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"W",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u1.start();

				u2 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"E",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u2.start();

				u3 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"N",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u3.start();

				u4 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"S",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u4.start();

				u5 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"SE",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u5.start();

				u6 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"SW",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u6.start();	

				u7 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"NE",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u7.start();		

				u8 = new UpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"NW",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u8.start();

			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@AuthorAnnotation(
			author = {"Francesco Milone","Carmine Spagnuolo"},
			date = "6/3/2014"
			)
	/**
	 * Setup topics for a  distributed field Network.
	 */
	protected void init_network_connection()
	{
		DistributedMultiSchedule dms = schedule;
		ArrayList<DNetwork> networkLists = dms.fieldsNetwork;
		String toSubscribe;

		HashMap<String, ArrayList<DistributedField>> listToPublish = new HashMap<String, ArrayList<DistributedField>>();
		HashMap<String, ArrayList<DNetwork>> listToSubscribe = new HashMap<String, ArrayList<DNetwork>>();


		for (DNetwork distributedNetwork : networkLists) {
			int my_community = (TYPE.pos_i*rows)+TYPE.pos_j;
			ArrayList<Integer> myPublishNeighborhood = distributedNetwork.grpsub.getSubscribers(my_community);
			for (Integer integer : myPublishNeighborhood)
			{
				String my_topic = topicPrefix+"-Network-"+my_community+"-"+integer;
				if(listToPublish.get(my_topic)==null)
				{
					listToPublish.put(my_topic, new ArrayList<DistributedField>());
				}
				listToPublish.get(my_topic).add(distributedNetwork);
			}
			ArrayList<Integer> myNeighborhood = distributedNetwork.grpsub.getPublisher(my_community);
			for (Integer integer : myNeighborhood) {

				toSubscribe=topicPrefix+"-Network-"+integer+"-"+my_community;
				if(listToSubscribe.get(toSubscribe)==null)
				{
					listToSubscribe.put(toSubscribe, new ArrayList<DNetwork>());

				}
				listToSubscribe.get(toSubscribe).add(distributedNetwork);

				if(networkNumberOfSubscribersForField.get(distributedNetwork.getDistributedFieldID())==null)
					networkNumberOfSubscribersForField.put(distributedNetwork.getDistributedFieldID(), new Integer(0));
				networkNumberOfSubscribersForField.put(distributedNetwork.getDistributedFieldID(), 
						(networkNumberOfSubscribersForField.get(distributedNetwork.getDistributedFieldID())+1));
			}	
		}

		for (DNetwork distributedNetwork : networkLists) {
			((DistributedFieldNetwork)distributedNetwork).setNumberOfUpdatesToSynchro(networkNumberOfSubscribersForField.get(distributedNetwork.getDistributedFieldID()));
		}

		Set<String> keySetToPublish = listToPublish.keySet();
		for(String topicName : keySetToPublish)
		{
			ArrayList<DistributedField> publishers = listToPublish.get(topicName);
			try {
				connectionJMS.createTopic(topicName, publishers.size());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Set<String> keySetToSubscribe = listToSubscribe.keySet();
		for(String topicName : keySetToSubscribe)
		{
			ArrayList<DNetwork> subscribers = listToSubscribe.get(topicName);
			try {
				connectionJMS.subscribeToTopic(topicName);
			} catch (Exception e) {
				e.printStackTrace();
			}

			UpdaterThreadJMSForNetworkListener utfl = new UpdaterThreadJMSForNetworkListener(connectionJMS, topicName, subscribers, networkListeners);
			utfl.start();
		}
	}

	public CellType getType() {
		return TYPE;
	}

	public ConnectionJMS getConnection() {
		return connectionJMS;
	}

	public ArrayList<MessageListener> getLocalListener() {
		return listeners;
	}

	public Trigger getTrigger() {
		return TRIGGER;
	}

	//added for close connection of current simulation after reset
	public void closeConnectionJMS() throws JMSException
	{
		connectionJMS.close();
	}

	public void init_service_connection() {
		dm.upVar=new UpdateGlobalVarAtStep(dm);
		ThreadVisualizationCellMessageListener thread = new ThreadVisualizationCellMessageListener(
				connectionJMS,
				((DistributedMultiSchedule) this.schedule));
		thread.start();

		try {
			boolean a = connectionJMS.createTopic(topicPrefix+"GRAPHICS" + TYPE,
					schedule.fields2D.size());
			connectionJMS.subscribeToTopic(topicPrefix+"GRAPHICS" + TYPE);
			ThreadZoomInCellMessageListener t_zoom = new ThreadZoomInCellMessageListener(
					connectionJMS,
					TYPE.toString(), (DistributedMultiSchedule) this.schedule);
			t_zoom.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Support for Global Parameters
		try {
			connectionJMS.subscribeToTopic(topicPrefix + "GLOBAL_REDUCED");
			UpdaterThreadForGlobalsListener ug = new UpdaterThreadForGlobalsListener(
					connectionJMS,
					topicPrefix + "GLOBAL_REDUCED",
					schedule.fields2D,
					listeners);
			ug.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}