package utils;

import java.util.TreeSet; 

public class FasterPrimMST { 
	/**
	 * Creates a Minimum Spanning Tree (MST) for a given graph, starting at a given node.
	 * @param graph - A graph as an adjacency matrix
	 */
	public static Tree createMinimumSpanningTree(WeightedGraph graph, int startingNode) { 
		int n = graph.getNumVertices();
		Tree tree = new Tree(startingNode, n);
		
		VertexKeyPair[] keyMap = new VertexKeyPair[n]; 
		
		// Whether a vertex was removed from the queue or not (initially all false)
		boolean[] mstSet = new boolean[n]; 
		
		for(int i = 0; i < n; i++)
			keyMap[i] = new VertexKeyPair(i, Float.MAX_VALUE); 
		
		// Set the key value of the origin node to 0 so that it is extracted first out of the queue 
		keyMap[startingNode].key = 0; 

		// Use TreeSet for fast implementation 
		TreeSet<VertexKeyPair> queue = new TreeSet<VertexKeyPair>();

		for(int o = 0; o < n; o++) 
			queue.add(keyMap[o]);	
	
		// Keep going until the queue is empty 
		while(!queue.isEmpty()) {
			// Extracts a node with min key value 
			int u = queue.pollFirst().vertex;
			
			mstSet[u] = true; 

			// For all adjacent nodes
			for(WeightedConnection conn : graph.getConnections(u)) { 
				int v = conn.getVertex();
				// If the node wasn't removed from the queue
				if(!mstSet[v]) {
					VertexKeyPair pair = keyMap[v];
					// If the key value of the adjacent vertex is more than the extracted key
					if(conn.getWeight() < pair.key) { 
						// Then update the key value of the adjacent vertex.
						// To update the queue, remove the node and add it again.
						tree.setParent(v, u); 
						queue.remove(pair);
						pair.key = conn.getWeight();
						queue.add(pair); 
					} 
				} 
			} 
		} 
		
		return tree;
	} 

	private static class VertexKeyPair implements Comparable<VertexKeyPair> {
		public int vertex;
		public float key;
		
		public VertexKeyPair(int vertex, float key) {
			this.vertex = vertex;
			this.key = key;
		}

		@Override
		public int compareTo(VertexKeyPair pair) {
			return (int) Math.signum(key == pair.key ? vertex - pair.vertex : key - pair.key);
		}
	}
}
