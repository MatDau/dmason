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
package it.isislab.dmason.sim.field.network.kway.util;

import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.sim.field.network.kway.algo.jabeja.JabeJa;
import it.isislab.dmason.sim.field.network.kway.algo.kaffpa.KaffpaEProcessBinding;
import it.isislab.dmason.sim.field.network.kway.algo.kaffpa.KaffpaProcessBinding;
import it.isislab.dmason.sim.field.network.kway.algo.metis.MetisProcessBinding;
import it.isislab.dmason.sim.field.network.kway.algo.metis.MetisRelaxedProcessBinding;

import java.io.IOException;

@AuthorAnnotation(
		author = {"Alessia Antelmi", "Carmine Spagnuolo"},
		date = "20/7/2015"
		)
public class Partitioner {

	/**
	 * Partitions the graph with Jabeja
	 * 
	 * @param graph_file_name - file describing the graph
	 * @param partNumber - number of partitions
	 * @param iterNumber - number of iteration
	 * @param temp - temperature
	 * @param tempDelta - decrease of temperature
	 * @param vertex_names - all vertex ids
	 * @throws IOException the exception
	 * @throws InterruptedException the exception
	 * @return NetworkPartition which stores information about the graph
	 */
	public static NetworkPartition partitioningWithJabeja(
			String graph_file_name, int partNumber, int iterNumber, float temp,
			float tempDelta, Integer[] vertex_names) throws IOException, InterruptedException {

		JabeJa j = new JabeJa(graph_file_name, 1, "edgelist", iterNumber,
				partNumber, temp, tempDelta, vertex_names);
		String partition_filename = j.partitioning();

		return PartitionManager.getNetworkPartition(graph_file_name, partition_filename);

	}

	/**
	 * Partitions the graph with Metis
	 * 
	 * @param bin_path - path where the executable is stored
	 * @param graph_file_name - file describing the graph
	 * @param partNumber - number of partitions
	 * @throws IOException the exception
	 * @throws InterruptedException the exception 
	 * @return NetworkPartition which stores information about the graph
	 */
	public static NetworkPartition partitioningWithMetis(String bin_path,
			String graph_file_name, int partNumber) throws IOException, InterruptedException {

		MetisProcessBinding m = new MetisProcessBinding(bin_path,
				graph_file_name, partNumber);
		String partition_filename = m.partitioning();

		return PartitionManager.getNetworkPartition(graph_file_name, partition_filename);

	}

	/**
	 * Partitions the graph with Metis-relaxed
	 * 
	 * @param bin_path - path where the executable is stored
	 * @param graph_file_name - file describing the graph
	 * @param partNumber - number of partitions
	 * @throws IOException the exception
	 * @throws InterruptedException the exception 
	 * @return NetworkPartition which stores information about the graph
	 */
	public static NetworkPartition partitioningWithMetisRelaxed(
			String bin_path, String graph_file_name, int partNumber) throws IOException, InterruptedException {

		MetisRelaxedProcessBinding m = new MetisRelaxedProcessBinding(bin_path,
				graph_file_name, partNumber);
		String partition_filename = m.partitioning();

		return PartitionManager.getNetworkPartition(graph_file_name, partition_filename);

	}

	/**
	 * Partitions the graph with Kaffpa
	 * 
	 * @param bin_path - path where the executable is stored
	 * @param graph_file_name - file describing the graph
	 * @param partNumber - number of partitions
	 * @throws IOException the exception
	 * @throws InterruptedException the exception 
	 * @return NetworkPartition which stores information about the graph
	 */
	public static NetworkPartition partitionWithKaffpa(String bin_path,
			String graph_file_name, int partNumber) throws IOException, InterruptedException {

		KaffpaProcessBinding k = new KaffpaProcessBinding(bin_path,
				graph_file_name, partNumber);
		String partition_filename = k.partitioning();

		return PartitionManager.getNetworkPartition(graph_file_name, partition_filename);

	}

	/**
	 * Partitions the graph with KaffpaE
	 * 
	 * @param bin_path - path where the executable is stored
	 * @param graph_file_name - file describing the graph
	 * @param partNumber - number of partitions
	 * @throws IOException the exception
	 * @throws InterruptedException the exception 
	 * @return NetworkPartition which stores information about the graph
	 */
	public static NetworkPartition partitionWithKaffpaE(String bin_path,
			String graph_file_name, int partNumber) throws IOException, InterruptedException {

		KaffpaEProcessBinding k = new KaffpaEProcessBinding(bin_path,
				graph_file_name, partNumber);
		String partition_filename = k.partitioning();

		return PartitionManager.getNetworkPartition(graph_file_name, partition_filename);

	}

}
