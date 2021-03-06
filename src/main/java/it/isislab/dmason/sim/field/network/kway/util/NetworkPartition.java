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
import it.isislab.dmason.sim.field.network.kway.graph.Graph;
import it.isislab.dmason.sim.field.network.kway.graph.SuperVertex;
import it.isislab.dmason.sim.field.network.kway.graph.Vertex;
import it.isislab.dmason.sim.field.support.network.GraphSubscribersEdgeList;

import java.util.HashMap;

/**
 * Stores information about a graph
 * and its partitioning
 */
@AuthorAnnotation(
		author = {"Alessia Antelmi", "Carmine Spagnuolo"},
		date = "20/7/2015"
		)
public class NetworkPartition{
	private Graph original_graph;
	private Graph super_graph;
	private HashMap<Integer, Vertex> parts2graph;
	private HashMap<Integer, Integer> parts2graph_id;
	private GraphSubscribersEdgeList edges_subscriber_lsit;
	private HashMap<Vertex, Integer> graph2parts;
	private HashMap<Integer, SuperVertex> parts2SuperGraph;

	public NetworkPartition(Graph original_graph, Graph super_graph,
			HashMap<Integer, Vertex> parts2graph,
			HashMap<Integer, Integer> parts2graph_id,
			HashMap<Vertex, Integer> graph2parts,
			HashMap<Integer, SuperVertex> parts2SuperGraph,
			GraphSubscribersEdgeList edges_subscriber_lsit) {
		super();
		this.original_graph = original_graph;
		this.super_graph = super_graph;
		this.parts2graph = parts2graph;
		this.parts2graph_id = parts2graph_id;
		this.edges_subscriber_lsit = edges_subscriber_lsit;
		this.graph2parts=graph2parts;
		this.parts2SuperGraph=parts2SuperGraph;
	}

	public HashMap<Integer, SuperVertex> getParts2SuperGraph() {
		return parts2SuperGraph;
	}

	public void setParts2SuperGraph(HashMap<Integer, SuperVertex> parts2SuperGraph) {
		this.parts2SuperGraph = parts2SuperGraph;
	}

	public HashMap<Vertex, Integer> getGraph2parts() {
		return graph2parts;
	}

	public void setGraph2parts(HashMap<Vertex, Integer> graph2parts) {
		this.graph2parts = graph2parts;
	}

	public Graph getOriginal_graph() {
		return original_graph;
	}

	public void setOriginal_graph(Graph original_graph) {
		this.original_graph = original_graph;
	}

	public Graph getSuper_graph() {
		return super_graph;
	}

	public void setSuper_graph(Graph super_graph) {
		this.super_graph = super_graph;
	}

	public HashMap<Integer, Vertex> getParts2graph() {
		return parts2graph;
	}

	public void setParts2graph(HashMap<Integer, Vertex> parts2graph) {
		this.parts2graph = parts2graph;
	}

	public HashMap<Integer, Integer> getParts2graph_id() {
		return parts2graph_id;
	}

	public void setParts2graph_id(HashMap<Integer, Integer> parts2graph_id) {
		this.parts2graph_id = parts2graph_id;
	}

	public GraphSubscribersEdgeList getEdges_subscriber_lsit() {
		return edges_subscriber_lsit;
	}

	public void setEdges_subscriber_lsit(
			GraphSubscribersEdgeList edges_subscriber_lsit) {
		this.edges_subscriber_lsit = edges_subscriber_lsit;
	}

}