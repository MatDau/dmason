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
package it.isislab.dmason.experimentals.systemmanagement.worker;

import java.io.Serializable;
import java.text.DecimalFormat;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;




/**
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class WorkerResourceInfo implements Serializable{

	private static Sigar sigar = new Sigar();
	private static final long serialVersionUID = 1L;

	private Runtime runtime=null;
	private long available=0;
	private long busy=0;
	private long maxHeap = 0;
	private int cores=0;
	private static final double byte_giga=1073741824;
	private static final double byte_mega=1048576;



	public WorkerResourceInfo() {
		cores = Runtime.getRuntime().availableProcessors();
		runtime = Runtime.getRuntime();
	}


	public double getMaxHeapMb(){
		maxHeap = runtime.maxMemory();
		double toReturn=maxHeap/byte_mega; 
		//System.out.println("Max "+toReturn);
		return toReturn;		
	}

	public double getMaxHeapGb(){
		maxHeap = runtime.maxMemory();
		double toReturn=maxHeap/byte_giga; 

		return toReturn;		
	}


	public double getAvailableHeapMb(){
		available=runtime.freeMemory();
		double toReturn=available/byte_mega; 
		//System.out.println("Avaiable "+toReturn);
		return toReturn;
	}


	public double getAvailableHeapGb(){	
		available=runtime.freeMemory();
		double toReturn=available/byte_giga; 
		return toReturn;		
	}

	public double getBusyHeapMb(){
		busy= (runtime.maxMemory() - runtime.freeMemory());
		double toReturn=busy/byte_mega; 
		//System.out.println("busy "+toReturn);
		return toReturn;
	}


	public double getBusyHeapGb(){
		busy= (runtime.totalMemory() - runtime.freeMemory());
		double toReturn=busy/byte_giga; 
		DecimalFormat format=new DecimalFormat("#,##");
		String dx=format.format(toReturn);
		toReturn=Double.valueOf(dx);
		return toReturn;


	}
	public int getNumCores(){return cores;}


	public static double  getCPULoad(){

		double value=-1;
	
	    CpuPerc cpuperc = null;
	 
	    try {
	    	sigar.getCpuList();
	        cpuperc = sigar.getCpuPerc();
	    } catch (SigarException se) {
	        System.err.println(se.getMessage());
	    	value=-1;
	    	return value;
	    }
	    
	    value=cpuperc.getCombined()*100;
	    value=Math.round(value*100);
	    value/=100;
	    return value;

	}

}