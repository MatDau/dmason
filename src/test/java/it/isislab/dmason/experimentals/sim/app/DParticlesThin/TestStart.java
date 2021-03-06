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
package it.isislab.dmason.experimentals.sim.app.DParticlesThin;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
/*
 * THIS CLASS HAS BEEN USED FOR TESTING PURPOSES IN THE BEGINNINGS,
 */
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2DFactory;
import it.isislab.dmason.util.connection.ConnectionType;

import java.util.ArrayList;

import sim.display.Console;

/**
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
public class TestStart {

	private static boolean graphicsOn=false; //with or without graphics?
	private static int numSteps = 300; //only graphicsOn=false
	private static int rows = 2; //number of rows
	private static int columns = 2; //number of columns
	private static int AOI=1; //max distance
	private static int NUM_AGENTS=400; //number of agents
	private static int WIDTH=200; //field width
	private static int HEIGHT=200; //field height
	private static String ip="127.0.0.1"; //ip of activemq
	private static String port="61616"; //port of activemq
	
	//don't modify this...
	private static int MODE = DistributedField2D.UNIFORM_PARTITIONING_MODE; 
	
	public static void main(String[] args) 
	{		
		class worker extends Thread
		{
			private DistributedState ds;
			public worker(DistributedState ds) {
				this.ds=ds;
			}
			@Override
			public void run() {
				ds.start();
				int i=0;
				while(i!=numSteps)
				{
					System.out.println(i);
					ds.schedule.step(ds);
					i++;
				}
			}
		}

		ArrayList<worker> myWorker = new ArrayList<worker>();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				
				GeneralParam genParam = new GeneralParam(WIDTH, HEIGHT, AOI, rows,columns,NUM_AGENTS, MODE,ConnectionType.pureActiveMQ); 
				genParam.setI(i);
				genParam.setJ(j);
				genParam.setIp(ip);
				genParam.setPort(port);
				if(graphicsOn)
				{
					it.isislab.dmason.experimentals.sim.app.DParticlesThin.DParticlesWithUI sim =new it.isislab.dmason.experimentals.sim.app.DParticlesThin.DParticlesWithUI(genParam);
					((Console)sim.createController()).pressPause();
				}
				else
				{
					it.isislab.dmason.experimentals.sim.app.DParticlesThin.DParticles sim = new it.isislab.dmason.experimentals.sim.app.DParticlesThin.DParticles(genParam); 
					worker a = new worker(sim);
					myWorker.add(a);
				}
			}
		}
		if(!graphicsOn)
			for (worker w : myWorker) {
				w.start();
			}
	}
}