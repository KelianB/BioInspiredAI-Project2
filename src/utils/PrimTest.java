package utils;

public class PrimTest {
	public static void main(String[] args) { 
	    /* Let us create the following graph 
		2 3 
		(0)--(1)--(2) 
		| / \ | 
		6| 8/ \5 |7 
		| /     \ | 
		(3)-------(4) 
		    9         */
		CorrectMST t = new CorrectMST(); 
		int graph[][] = new int[][] {
			{ 0, 2, 0, 6, 0 }, 
			{ 2, 0, 3, 8, 5 }, 
			{ 0, 3, 0, 0, 7 }, 
			{ 6, 8, 0, 0, 9 }, 
			{ 0, 5, 7, 9, 0 }
		};                                       
		     	  
		// Print the solution 
		t.primMST(graph); 
		
		WeightedGraph wg = new WeightedGraph(graph.length);
		for(int i = 0; i < graph.length; i++) {
			for(int j = 0; j < graph[i].length; j++) {
				if(graph[i][j] != 0)
					wg.addConnection(i, j, graph[i][j]);
			}
		}
		
		Tree tree = PrimMST.createMinimumSpanningTree(wg, 0);
		System.out.println(tree);
		
	}
}
