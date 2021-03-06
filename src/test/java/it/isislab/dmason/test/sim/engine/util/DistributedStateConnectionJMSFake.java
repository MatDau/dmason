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
package it.isislab.dmason.test.sim.engine.util;


import it.isislab.dmason.experimentals.util.management.globals.util.UpdateGlobalVarAtStep;
import it.isislab.dmason.experimentals.util.trigger.Trigger;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.DistributedStateConnectionJMS;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.test.util.connection.VirtualConnectionNFieldsWithVirtualJMS;
import it.isislab.dmason.test.util.connection.VirtualMessageListener;
import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;

import java.util.ArrayList;


/**
 * 
 * @param <E>
 *            the type of locations
 *            
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Luca Vicidomini       
 */
public class DistributedStateConnectionJMSFake<E> extends DistributedStateConnectionJMS<E> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ArrayList<VirtualMessageListener> listeners = new ArrayList<VirtualMessageListener>();
	private FakeUpdaterThreadForListener u1;
	private FakeUpdaterThreadForListener u2;
	private FakeUpdaterThreadForListener u3;
	private FakeUpdaterThreadForListener u4;
	private FakeUpdaterThreadForListener u5;
	private FakeUpdaterThreadForListener u6;
	private FakeUpdaterThreadForListener u7;
	private FakeUpdaterThreadForListener u8;

	public DistributedStateConnectionJMSFake(DistributedState dm) {
		super();
		this.dm=dm;
		connectionJMS = new VirtualConnectionNFieldsWithVirtualJMS();
		schedule=(DistributedMultiSchedule<E>)dm.schedule;
		topicPrefix=dm.topicPrefix;
		TYPE=dm.TYPE;
		MODE=dm.MODE;
		NUMPEERS=dm.NUMPEERS;
		rows=dm.rows;
		columns=dm.columns;
		networkNumberOfSubscribersForField=dm.networkNumberOfSubscribersForField;
	}

	public DistributedStateConnectionJMSFake(DistributedState dm, String ip,String port, ConnectionJMS connectionJMS) {
		this.ip = ip;
		this.port = port;
		this.dm=dm;
		this.connectionJMS = connectionJMS;


		schedule=(DistributedMultiSchedule<E>)dm.schedule;
		topicPrefix=dm.topicPrefix;
		TYPE=dm.TYPE;
		MODE=dm.MODE;
		NUMPEERS=dm.NUMPEERS;
		rows=dm.rows;
		columns=dm.columns;
		networkNumberOfSubscribersForField=dm.networkNumberOfSubscribersForField;
	}

	public void init_connection() {
		
		try {
			connectionJMS.setupConnection(new Address(ip, port));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.TRIGGER = new Trigger(connectionJMS);
		if(((DistributedMultiSchedule<E>)dm.schedule).fields2D.size()>0)
			init_spatial_connection();
		if(((DistributedMultiSchedule<E>)dm.schedule).fieldsNetwork.size()>0)
			super.init_network_connection();
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

		dm.upVar=new UpdateGlobalVarAtStep(dm);

		if (toroidal_need)
		{
			connection_IS_toroidal();
		}
		else
		{
			connection_NO_toroidal();
		}

	}
	
	protected void connection_IS_toroidal() {

		if (MODE == DistributedField2D.UNIFORM_PARTITIONING_MODE) {
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
					
					u3 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
									+ ((j + columns) % columns) + "N",
									schedule.fields2D, listeners);
					u3.start();

					u4 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
									+ ((j + columns) % columns) + "S",
									schedule.fields2D, listeners);
					u4.start();

					u5 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
									+ ((j - 1 + columns) % columns) + "SE",
									schedule.fields2D, listeners);
					u5.start();

					u6 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
									+ ((j + 1 + columns) % columns) + "SW",
									schedule.fields2D, listeners);
					u6.start();

					u7 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
									+ ((j - 1 + columns) % columns) + "NE",
									schedule.fields2D, listeners);
					u7.start();

					u8 = new FakeUpdaterThreadForListener(
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

					u1 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + rows) % rows) + "-"
									+ ((j + 1 + columns) % columns) + "W",
									schedule.fields2D, listeners);
					u1.start();

					u2 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + rows) % rows) + "-"
									+ ((j - 1 + columns) % columns) + "E",
									schedule.fields2D, listeners);
					u2.start();


					u5 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
									+ ((j - 1 + columns) % columns) + "SE",
									schedule.fields2D, listeners);
					u5.start();

					u6 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
									+ ((j + 1 + columns) % columns) + "SW",
									schedule.fields2D, listeners);
					u6.start();

					u7 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
									+ ((j - 1 + columns) % columns) + "NE",
									schedule.fields2D, listeners);
					u7.start();

					u8 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
									+ ((j + 1 + columns) % columns) + "NW",
									schedule.fields2D, listeners);
					u8.start();
				}else{
					// M rows and N columns
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

					u1 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + rows) % rows) + "-"
									+ ((j + 1 + columns) % columns) + "W",
									schedule.fields2D, listeners);
					u1.start();

					u2 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + rows) % rows) + "-"
									+ ((j - 1 + columns) % columns) + "E",
									schedule.fields2D, listeners);
					u2.start();

					u3 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
									+ ((j + columns) % columns) + "N",
									schedule.fields2D, listeners);
					u3.start();

					u4 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
									+ ((j + columns) % columns) + "S",
									schedule.fields2D, listeners);
					u4.start();

					u5 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
									+ ((j - 1 + columns) % columns) + "SE",
									schedule.fields2D, listeners);
					u5.start();

					u6 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
									+ ((j + 1 + columns) % columns) + "SW",
									schedule.fields2D, listeners);
					u6.start();

					u7 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
									+ ((j - 1 + columns) % columns) + "NE",
									schedule.fields2D, listeners);
					u7.start();

					u8 = new FakeUpdaterThreadForListener(
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

			u1 = new FakeUpdaterThreadForListener(
					connectionJMS, topicPrefix+((i + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "W",
							schedule.fields2D, listeners);
			u1.start();

			u2 = new FakeUpdaterThreadForListener(
					connectionJMS, topicPrefix+((i + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "E",
							schedule.fields2D, listeners);
			u2.start();


			u5 = new FakeUpdaterThreadForListener(
					connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "SE",
							schedule.fields2D, listeners);
			u5.start();

			u6 = new FakeUpdaterThreadForListener(
					connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "SW",
							schedule.fields2D, listeners);
			u6.start();

			u7 = new FakeUpdaterThreadForListener(
					connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "NE",
							schedule.fields2D, listeners);
			u7.start();

			u8 = new FakeUpdaterThreadForListener(
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

				u1 = new FakeUpdaterThreadForListener(connectionJMS,topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"W",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u1.start();

				u2 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"E",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u2.start();

				u3 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"N",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u3.start();

				u4 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"S",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u4.start();

				u5 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"SE",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u5.start();

				u6 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"SW",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u6.start();	

				u7 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"NE",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u7.start();		

				u8 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"NW",((DistributedMultiSchedule)schedule).fields2D,listeners);
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

			u1 = new FakeUpdaterThreadForListener(
					connectionJMS, topicPrefix+((i + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "W",
							schedule.fields2D, listeners);
			u1.start();

			u2 = new FakeUpdaterThreadForListener(
					connectionJMS, topicPrefix+((i + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "E",
							schedule.fields2D, listeners);
			u2.start();


			u5 = new FakeUpdaterThreadForListener(
					connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "SE",
							schedule.fields2D, listeners);
			u5.start();

			u6 = new FakeUpdaterThreadForListener(
					connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
							+ ((j + 1 + columns) % columns) + "SW",
							schedule.fields2D, listeners);
			u6.start();

			u7 = new FakeUpdaterThreadForListener(
					connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
							+ ((j - 1 + columns) % columns) + "NE",
							schedule.fields2D, listeners);
			u7.start();

			u8 = new FakeUpdaterThreadForListener(
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
				//nothing to do

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
						FakeUpdaterThreadForListener u1 = new FakeUpdaterThreadForListener(
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
						FakeUpdaterThreadForListener u1 = new FakeUpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourUp() + "S",
								schedule.fields2D, listeners);
						u1.start();
					}
					else{
						connectionJMS.createTopic(topicPrefix+TYPE + "S",
								schedule.fields2D
								.size());
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDown() + "N");
						FakeUpdaterThreadForListener u1 = new FakeUpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourDown() + "N",
								schedule.fields2D, listeners);
						u1.start();

						connectionJMS.createTopic(topicPrefix+TYPE + "N",
								schedule.fields2D
								.size());
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourUp() + "S");
						FakeUpdaterThreadForListener u2 = new FakeUpdaterThreadForListener(
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
						FakeUpdaterThreadForListener u2 = new FakeUpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourRight() + "W",
								schedule.fields2D, listeners);
						u2.start();

					}
					if(TYPE.pos_j > 0){
						connectionJMS.createTopic(topicPrefix+TYPE + "W",
								schedule.fields2D
								.size());

						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourLeft() + "E");
						FakeUpdaterThreadForListener u1 = new FakeUpdaterThreadForListener(
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
						FakeUpdaterThreadForListener u1 = new FakeUpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourDiagLeftUp() + "SE",
								schedule.fields2D, listeners);
						u1.start();
					}
					if(TYPE.pos_i > 0 && TYPE.pos_j < columns){
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagRightUp()
						+ "SW");
						FakeUpdaterThreadForListener u2 = new FakeUpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourDiagRightUp() + "SW",
								schedule.fields2D, listeners);
						u2.start();
					}

					if(TYPE.pos_i < rows && TYPE.pos_j > 0){
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagLeftDown()
						+ "NE");
						FakeUpdaterThreadForListener u3 = new FakeUpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourDiagLeftDown() + "NE",
								schedule.fields2D, listeners);
						u3.start();
					}
					if(TYPE.pos_i < rows && TYPE.pos_j < columns){
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagRightDown()
						+ "NW");
						FakeUpdaterThreadForListener u4 = new FakeUpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourDiagRightDown() + "NW",
								schedule.fields2D, listeners);
						u4.start();
					}
					if(TYPE.pos_j > 0){
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourLeft() + "E");
						FakeUpdaterThreadForListener u5 = new FakeUpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourLeft() + "E",
								schedule.fields2D, listeners);
						u5.start();
					}
					if(TYPE.pos_j < columns){
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourRight() + "W");
						FakeUpdaterThreadForListener u6 = new FakeUpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourRight() + "W",
								schedule.fields2D, listeners);
						u6.start();
					}
					if(TYPE.pos_i > 0){	
						connectionJMS.subscribeToTopic(topicPrefix+(TYPE.getNeighbourUp() + "S"));
						FakeUpdaterThreadForListener u7 = new FakeUpdaterThreadForListener(
								connectionJMS, topicPrefix+TYPE.getNeighbourUp() + "S",
								schedule.fields2D, listeners);
						u7.start();
					}
					if(TYPE.pos_i < rows){
						connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDown() + "N");
						FakeUpdaterThreadForListener u8 = new FakeUpdaterThreadForListener(
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
				FakeUpdaterThreadForListener u2 = new FakeUpdaterThreadForListener(
						connectionJMS, topicPrefix+TYPE.getNeighbourRight() + "W",
						schedule.fields2D, listeners);
				u2.start();

			}
			if(TYPE.pos_j > 0){
				connectionJMS.createTopic(topicPrefix+TYPE + "W",
						schedule.fields2D
						.size());

				connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourLeft() + "E");
				FakeUpdaterThreadForListener u1 = new FakeUpdaterThreadForListener(
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

				u1 = new FakeUpdaterThreadForListener(connectionJMS,topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"W",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u1.start();

				u2 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"E",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u2.start();

				u3 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"N",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u3.start();

				u4 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"S",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u4.start();

				u5 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"SE",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u5.start();

				u6 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"SW",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u6.start();	

				u7 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"NE",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u7.start();		

				u8 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"NW",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u8.start();

			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}