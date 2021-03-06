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
package it.isislab.dmason.sim.app.DAntsForage;

import java.awt.Color;
import java.util.List;

import javax.swing.JFrame;

import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
/**
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * 
 */
public class DAntsForageWithUI extends GUIState
    {
    public Display2D display;
    public JFrame displayFrame;
    public static String name;

    FastValueGridPortrayal2D homePheromonePortrayal = new FastValueGridPortrayal2D("Home Pheromone");
    FastValueGridPortrayal2D foodPheromonePortrayal = new FastValueGridPortrayal2D("Food Pheromone");
    FastValueGridPortrayal2D sitesPortrayal = new FastValueGridPortrayal2D("Site", true);  // immutable
    FastValueGridPortrayal2D obstaclesPortrayal = new FastValueGridPortrayal2D("Obstacle", true);  // immutable
    SparseGridPortrayal2D bugPortrayal = new SparseGridPortrayal2D();
                
    /*public DAntsForageWithUI(Object[] args) { 
    	super(new DAntsForage(args)); 
    	name=String.valueOf(args[7])+""+(String.valueOf(args[8]));
    }*/
    
    public DAntsForageWithUI(GeneralParam args,List<EntryParam<String, Object>>list,String prefix) { 
    	super(new DAntsForage(args,list,prefix)); 
    	name=String.valueOf(args.getI())+""+(String.valueOf(args.getJ()));
    }
    
    public DAntsForageWithUI(GeneralParam args, String prefix) { 
    	super(new DAntsForage(args,prefix)); 
    	name=String.valueOf(args.getI())+""+(String.valueOf(args.getJ()));
    }
    
   // public DAntsForageWithUI(SimState state) { super(state); }
    
    // allow the user to inspect the model
    @Override
	public Object getSimulationInspectedObject() { return state; }  // non-volatile

    public static String getName() { return "Peer: <"+name+">"; }
    
    public void setupPortrayals()
        {
        DAntsForage af = (DAntsForage)state;

        // tell the portrayals what to portray and how to portray them
        homePheromonePortrayal.setField(af.toHomeGrid);
        homePheromonePortrayal.setMap(new sim.util.gui.SimpleColorMap(0, DAntsForage.LIKELY_MAX_PHEROMONE,
                // home pheromones are beneath all, just make them opaque
                Color.white, //new Color(0,255,0,0),
                new Color(0,255,0,255) )
            { @Override
			public double filterLevel(double level) { return Math.sqrt(Math.sqrt(level)); } } );  // map with custom level filtering
        foodPheromonePortrayal.setField(af.toFoodGrid);
        foodPheromonePortrayal.setMap(new sim.util.gui.SimpleColorMap(
                0,
                DAntsForage.LIKELY_MAX_PHEROMONE,
                new Color(0,0,255,0),
                new Color(0,0,255,255) )
            { @Override
			public double filterLevel(double level) { return Math.sqrt(Math.sqrt(level)); } } );  // map with custom level filtering
        
        sitesPortrayal.setField(af.sites);
        sitesPortrayal.setMap(new sim.util.gui.SimpleColorMap(
                0,
                1,
                new Color(0,0,0,0),
                new Color(255,0,0,255) ));
        obstaclesPortrayal.setField(af.obstacles);
        obstaclesPortrayal.setMap(new sim.util.gui.SimpleColorMap(
                0,
                1,
                new Color(0,0,0,0),
                new Color(128,64,64,255) ));
        bugPortrayal.setField(af.buggrid);
            
        // reschedule the displayer
        display.reset();

        // redraw the display
        display.repaint();
        }
    
    @Override
	public void start()
        {
        super.start();  // set up everything but replacing the display
        // set up our portrayals
        setupPortrayals();
        }
            
    @Override
	public void load(SimState state)
        {
        super.load(state);
        // we now have new grids.  Set up the portrayals to reflect that
        setupPortrayals();
        }

    @SuppressWarnings("deprecation")
	@Override
	public void init(Controller c)
        {
        super.init(c);
        
        // Make the Display2D.  We'll have it display stuff later.
        display = new Display2D(600,600,this,1); // at 400x400, we've got 4x4 per array position
        displayFrame = display.createFrame();
        c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
        displayFrame.setVisible(true);

        // attach the portrayals from bottom to top
        display.attach(homePheromonePortrayal,"Pheromones To Home");
        display.attach(foodPheromonePortrayal,"Pheromones To Food");
        display.attach(sitesPortrayal,"Site Locations");
        display.attach(obstaclesPortrayal,"Obstacles");
        display.attach(bugPortrayal,"Agents");
        
        // specify the backdrop color  -- what gets painted behind the displays
        display.setBackdrop(Color.white);
        }
        
    @Override
	public void quit()
        {
        super.quit();
        
        // disposing the displayFrame automatically calls quit() on the display,
        // so we don't need to do so ourselves here.
        if (displayFrame!=null) displayFrame.dispose();
        displayFrame = null;  // let gc
        display = null;       // let gc
        }
        
    }
    
    
    
    
