package utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple representation of a weighted graph, using adjacency list representation.
 * @author Kelian Baert & Caroline de Pourtales
 */
public class WeightedGraph {
	// Adjacency list
	private List<WeightedConnection>[] connections;
	
	/**
	 * Create a new weighted graph.
	 * @param numVertices - The total number of vertices in the graph
	 */
	@SuppressWarnings("unchecked")
	public WeightedGraph(int numVertices) {
		// Initialize the adjacency list
		connections = (List<WeightedConnection>[]) new ArrayList[numVertices];
		for(int i = 0; i < connections.length; i++)
			connections[i] = new ArrayList<WeightedConnection>();
	}
	
	/**
	 * Adds a connection between two vertices, with a given weight.
	 * @param vertexA - A vertex between 0 (inclusive) and numVertices (exclusive)
	 * @param vertexB - Another vertex between 0 (inclusive) and numVertices (exclusive)
	 * @param weight - The weight of the connection
	 */
	public void addConnection(int vertexA, int vertexB, float weight) {
		// Add connection both ways
		this.connections[vertexA].add(new WeightedConnection(vertexB, weight));
		this.connections[vertexB].add(new WeightedConnection(vertexA, weight));
	}
	
	/**
	 * Get the connections from a given vertex.
	 * @param vertex - A vertex between 0 (inclusive) and numVertices (exclusive)
	 * @return a list of connections from the given vertex
	 */
	public List<WeightedConnection> getConnections(int vertex) {
		return this.connections[vertex];
	}
	
	/**
	 * Get the weight of the connection between two given vertices.
	 * @param vertexA - A vertex between 0 (inclusive) and numVertices (exclusive)
	 * @param vertexB - Another vertex between 0 (inclusive) and numVertices (exclusive)
	 * @return the weight of the connection (0 if no such connection exists)
	 */
	public float getWeight(int vertexA, int vertexB) {
		List<WeightedConnection> conns = getConnections(vertexA);
		for(int i = 0; i < conns.size(); i++) {
			WeightedConnection conn = conns.get(i);
			if(conn.getVertex() == vertexB)
				return conn.getWeight();
		}
		return 0;
	}
	
	/**
	 * Get the number of vertices in this graph.
	 * @return the number of vertices
	 */
	public int getNumVertices() {
		return this.connections.length;
	}
}
