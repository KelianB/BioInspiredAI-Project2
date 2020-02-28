package utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores a weighted graph with adjacency list representation
 */
public class WeightedGraph {
	private List<WeightedConnection>[] connections;
	
	public WeightedGraph(int numVertices) {
		connections = (ArrayList<WeightedConnection>[]) new ArrayList[numVertices];
		for(int i = 0; i < connections.length; i++)
			connections[i] = new ArrayList<WeightedConnection>();
	}
	
	public void addConnection(int vertexA, int vertexB, float weight) {
		this.connections[vertexA].add(new WeightedConnection(vertexB, weight));
		this.connections[vertexB].add(new WeightedConnection(vertexA, weight));
	}
	
	public List<WeightedConnection> getConnections(int vertex) {
		return this.connections[vertex];
	}
	
	public float getWeight(int vertexA, int vertexB) {
		List<WeightedConnection> conns = getConnections(vertexA);
		for(int i = 0; i < conns.size(); i++) {
			WeightedConnection conn = conns.get(i);
			if(conn.getVertex() == vertexB)
				return conn.getWeight();
		}
		return 0.0f;
	}
	
	public int getNumVertices() {
		return this.connections.length;
	}
}
