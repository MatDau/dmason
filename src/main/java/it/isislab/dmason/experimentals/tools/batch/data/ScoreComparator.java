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
package it.isislab.dmason.experimentals.tools.batch.data;

import java.util.Comparator;


public class ScoreComparator implements Comparator<EntryWorkerScore<Integer, String>>{

	@Override
	public int compare(EntryWorkerScore<Integer, String> o1,
			EntryWorkerScore<Integer, String> o2) {
		return (o1.getScore() > o2.getScore() ? -1 : (o1.getScore() == o2.getScore() ? 0 : 1));
	}
 


}
