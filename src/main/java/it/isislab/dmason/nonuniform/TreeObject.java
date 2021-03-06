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
package it.isislab.dmason.nonuniform;


import java.io.Serializable;

/**
 * Entry object for node of QuadTree
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class TreeObject{
	public Serializable obj;
	double x;
	double y;
	public TreeObject(Serializable obj, double x, double y) {
		super();
		this.obj = obj;
		this.x = x;
		this.y = y;
	}
	
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if(obj instanceof TreeObject)
		{
			TreeObject t=(TreeObject)obj;
			return x==t.x && y==t.y && t.obj.equals(obj);
		}
		return false;
	}

}