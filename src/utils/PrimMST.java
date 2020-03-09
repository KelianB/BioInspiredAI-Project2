package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates Minimum Spanning Trees using Prim's algorithm.
 * @author Kelian Baert & Caroline de Pourtales
 */
public class PrimMST { 
	/**
	 * Creates a Minimum Spanning Tree (MST) for a given graph, starting at a given node.
	 * @param graph - A graph as an adjacency matrix
	 */
	public static Tree createMinimumSpanningTree(WeightedGraph graph, int startingNode) {
		Tree tree = new Tree(startingNode, graph.getNumVertices());
		
		// Key values used to pick minimum weight edge in cut 
		// float[] key = new float[graph.getNumVertices()]; 
		List<VertexKeyPair> key = new ArrayList<VertexKeyPair>();
		VertexKeyPair[] keyMap = new VertexKeyPair[graph.getNumVertices()];
		
		// Store set of vertices not yet included in the MST
		boolean[] mstSet = new boolean[graph.getNumVertices()]; 

		// Initialize all keys as INFINITE and all vertices as not yet included
		for(int i = 0; i < graph.getNumVertices(); i++) { 
			VertexKeyPair pair = new VertexKeyPair(i, Float.MAX_VALUE);
			key.add(pair);
			keyMap[i] = pair;
			mstSet[i] = false; 
		} 

		// Always include first 1st vertex in MST. 
		key.get(startingNode).key = 0; // Set key value of first vertex to 0 so that it is picked as first vertex 
		Collections.sort(key);
		
		for(int count = 0; count < graph.getNumVertices(); count++) { 
			//if(count % 10000 == 0)
				//System.out.println(count + "/" + graph.getNumVertices());
			// Pick the minimum key vertex from the set of vertices not yet included in MST 
			// int u = minKey(key, mstSet); 

			int u = key.get(0).vertex;
						
			// Add the picked vertex to the MST Set 
			mstSet[u] = true; 
			key.remove(0);
			
			// Update key value and parent index of the adjacent vertices of the picked vertex. 
			// Consider only those vertices which are not yet included in the MST
			for(WeightedConnection conn : graph.getConnections(u)) {
				int v = conn.getVertex();
				// graph[u][v] is non zero only for adjacent vertices of m 
				// mstSet[v] is false for vertices not yet included in MST 
				// Update the key only if graph[u][v] is smaller than key[v] 
				if(!mstSet[v]) {
					VertexKeyPair pair = keyMap[v];
					if(conn.getWeight() < pair.key) {
						pair.key = conn.getWeight();
						tree.setParent(v, u);
						key.remove(pair);
						key.add(binarySearch(key, pair), pair);
					}
				}
			}
		}
		
		return tree;
	} 
		  
	private static <T> int binarySearch(List<T> sortedList, Comparable<T> item) { 
		return binarySearch(sortedList, item, 0, sortedList.size());
	}
	
	private static <T> int binarySearch(List<T> sortedList, Comparable<T> item, int low, int high) { 
		if(sortedList.size() == 0)
			return 0;
		
	    if (high <= low) 
	        return (item.compareTo(sortedList.get(low)) == 1)?  (low + 1): low; 
	  
	    int mid = (low + high) / 2; 
	  
	    if(item == sortedList.get(mid)) 
	        return mid + 1; 
	  
	    return item.compareTo(sortedList.get(mid)) == 1 ?
	        binarySearch(sortedList, item, mid+1, high) :
	    	binarySearch(sortedList, item, low, mid-1); 
	} 
	
	private static class VertexKeyPair implements Comparable<VertexKeyPair> {
		private int vertex;
		private float key;
		
		public VertexKeyPair(int vertex, float key) {
			this.vertex = vertex;
			this.key = key;
		}

		@Override
		public int compareTo(VertexKeyPair pair) {
			return (int) Math.signum(key - pair.key);
		}
	}
} 
