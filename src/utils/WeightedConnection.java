package utils;

public class WeightedConnection {
	private int vertex;
	private float weight;
	
	public WeightedConnection(int vertex, float weight) {
		this.vertex = vertex;
		this.weight = weight;
	}
	
	public int getVertex() {
		return vertex;
	}
	
	public float getWeight() {
		return weight;
	}
}