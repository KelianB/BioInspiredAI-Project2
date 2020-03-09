package utils;

/**
 * Represents a unidirectional connection in a weighted graph (immutable).
 * @author Kelian Baert & Caroline de Pourtales
 */
public class WeightedConnection {
	private int vertex;
	private float weight;

	/**
	 * Create a weighted connection.
	 * @param vertex - The end vertex of the connection
	 * @param weight - The associated weight
	 */
	public WeightedConnection(int vertex, float weight) {
		this.vertex = vertex;
		this.weight = weight;
	}
	
	/**
	 * Get the end vertex of this connection.
	 * @return this connection's end vertex
	 */
	public int getVertex() {
		return vertex;
	}
	
	/**
	 * Get the weight of this connection.
	 * @return this connection's weight
	 */
	public float getWeight() {
		return weight;
	}
}