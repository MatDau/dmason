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
package it.isislab.dmason.experimentals.systemmanagement.utils;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

/**
 * A class to find an available port on node
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class FindAvailablePort {
	
	private  int MIN_PORT_NUMBER = 0;
	private  int MAX_PORT_NUMBER = 100;
	
	public FindAvailablePort(int min,int max) {
	     MIN_PORT_NUMBER=min;
	     MAX_PORT_NUMBER=max;
	}

	/**
	 * Checks to see if a specific port is available.
	 *
	 * @param port the port to check for availability
	 */
	private boolean available(int port, int _min, int _max) {
	    if (port < _min || port > _max) {
	        throw new IllegalArgumentException("Invalid start port: " + port);
	    }

	    ServerSocket ss = null;
	    DatagramSocket ds = null;
	    try {
	        ss = new ServerSocket(port);
	        ss.setReuseAddress(true);
	        ds = new DatagramSocket(port);
	        ds.setReuseAddress(true);
	        return true;
	    } catch (IOException e) {
	    } finally {
	        if (ds != null) {
	            ds.close();
	        }

	        if (ss != null) {
	            try {
	                ss.close();
	            } catch (IOException e) {
	                /* should not be thrown */
	            }
	        }
	    }

	    return false;
	}
	
	
	/**
	 * Return available port
	 * @return first available port
	 */
	public int getPortAvailable(){
		
		 int port=-1; 
		 for(int i=MIN_PORT_NUMBER; i< MAX_PORT_NUMBER; i++){
			 if(available(i, MIN_PORT_NUMBER, MAX_PORT_NUMBER)){
				 port=i; 
			     break;
			 }    
		 }
		 return port;
	}
	
	
}
