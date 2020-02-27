package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates a Minimum Spanning Tree (MST) from a given graph using Prim's algorithm
 */
public class PrimMST { 
	// Store the parent of each vertex
	private int[] parent;
	
	// Store the children of each vertex
	private List<Integer>[] children;
	
	// Root of the tree
	private int rootVertex;
	
	// Store the original graph
	private WeightedGraph graph;
	
	/**
	 * Creates a Minimum Spanning Tree for a given graph. Starts at node 0.
	 * @param graph - A graph as an adjacency matrix
	 */
	public PrimMST(WeightedGraph graph) {
		this(graph, 0);
	}
	
	/**
	 * Creates a Minimum Spanning Tree for a given graph, starting at a given node.
	 * @param graph - A graph as an adjacency matrix
	 */
	public PrimMST(WeightedGraph graph, int startingNode) {
		this.graph = graph;
		this.parent = new int[graph.getNumVertices()];
		
		this.rootVertex = startingNode;
		
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
		key.get(startingNode).setKey(0); // Set key value of first vertex to 0 so that it is picked as first vertex 
		parent[startingNode] = -1;
		Collections.sort(key);
		
		for(int count = 0; count < graph.getNumVertices(); count++) { 
			//if(count % 10000 == 0)
				//System.out.println(count + "/" + graph.getNumVertices());
			// Pick the minimum key vertex from the set of vertices not yet included in MST 
			// int u = minKey(key, mstSet); 

			int u = key.get(0).getVertex();
						
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
					if(conn.getWeight() < pair.getKey()) {
						pair.setKey(conn.getWeight());
						parent[v] = u;
						key.remove(pair);
						key.add(binarySearch(key, pair), pair);
					}
				}
			}
		}
		
		this.children = (ArrayList<Integer>[]) (new ArrayList[graph.getNumVertices()]); 
		for(int i = 0; i < children.length; i++)
			children[i] = new ArrayList<Integer>();
		for(int i = 0; i < parent.length; i++) {
			if(parent[i] != -1)
				children[parent[i]].add(i);
		}

	} 
	
	/**
	 * Get the graph from which this tree was created.
	 * @return the graph from which this tree was created
	 */
	public WeightedGraph getGraph() {
		return graph;
	}
	
	public void test() {
		List<Integer> visitedChildren = new ArrayList<Integer>();
		for(int i = 0; i < getGraph().getNumVertices(); i++) {
			for(int j = 0; j < getChildren(i).size(); j++) {
				int child = getChildren(i).get(j);
				if(child == i)
					System.err.println("[TREE ERROR] Child is its own parent");
				if(visitedChildren.contains(child))
					System.err.println("[TREE ERROR] Child has two parents");
				else
					visitedChildren.add(child);
			}
		}
	}

	@Override
	public String toString() {
		String str = "Edge\n"; 
		for(int i = 1; i < getGraph().getNumVertices(); i++) 
			str += parent[i] + " - " + i;
		return str;
	}
		  

	
	public List<Integer> getChildren(int vertex) {
		return children[vertex];
	}
	
	public int getRootVertex() {
		return rootVertex;
	}
	
	private static <T> int binarySearch(List<T> sortedList, Comparable<T> item) { 
		return binarySearch(sortedList, item, 0, sortedList.size());
	}
	
	private static <T> int binarySearch(List<T> sortedList, Comparable<T> item, int low, int high) { 
	    if (high <= low) 
	        return (item.compareTo(sortedList.get(low)) == 1)?  (low + 1): low; 
	  
	    int mid = (low + high) / 2; 
	  
	    if(item == sortedList.get(mid)) 
	        return mid + 1; 
	  
	    return item.compareTo(sortedList.get(mid)) == 1 ?
	        binarySearch(sortedList, item, mid+1, high) :
	    	binarySearch(sortedList, item, low, mid-1); 
	} 
	
	class VertexKeyPair implements Comparable<VertexKeyPair> {
		private int vertex;
		private float key;
		
		public VertexKeyPair(int vertex, float key) {
			this.vertex = vertex;
			this.key = key;
		}
		
		public int getVertex() {
			return vertex;
		}
		public float getKey() {
			return key;
		}
		public void setKey(float key) {
			this.key = key;
		}

		@Override
		public int compareTo(VertexKeyPair pair) {
			return (int) Math.signum(getKey() - pair.getKey());
		}
		
		@Override
		public String toString() {
			return "(vertex= " + getVertex() + ", key= " + getKey() + ")";
		}
	}
	
	
	//public static void main(String[] args) { 
		/* Let us create the following graph 
		2 3 
		(0)--(1)--(2) 
		| / \ | 
		6| 8/ \5 |7 
		| /	 \ | 
		(3)-------(4) 
			9		 */
		
		/*int graph[][] = new int[][] { { 0, 2, 0, 6, 0 }, 
									{ 2, 0, 3, 8, 5 }, 
									{ 0, 3, 0, 0, 7 }, 
									{ 6, 8, 0, 0, 9 }, 
									{ 0, 5, 7, 9, 0 } }; 
		PrimMST t = new PrimMST(graph); 
		// Print the solution 
		System.out.println(t);
	}*/
} 
