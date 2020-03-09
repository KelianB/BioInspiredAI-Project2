package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A simple tree representation that stores both parents and children for quick access.
 * @author Kelian Baert & Caroline de Pourtales
 */
public class Tree {
	// A constant used to indicate that a node has no parent
	public static final int NO_PARENT = -1;
	
	// Store the parent of each node
	private int[] parent;
	
	// Store the children of each node
	private List<Integer>[] children;
	
	// Root of the tree
	private int rootNode;
	
	/**
	 * Create a new tree.
	 * @param rootNode - The root node
	 * @param size - The size of the tree
	 */
	@SuppressWarnings("unchecked")
	public Tree(int rootNode, int size) {
		this.rootNode = rootNode;
		
		// Initialize parent and children arrays
		this.parent = new int[size];
		this.children = (List<Integer>[]) (new ArrayList[size]); 
		for(int i = 0; i < children.length; i++)
			children[i] = new ArrayList<Integer>();
		
		parent[rootNode] = NO_PARENT;
	}
		
	/**
	 * Get the children of a given node
	 * @param node - A node, between 0 (inclusive) and the size of the tree (exclusive)
	 * @return the children of the given node
	 */
	public List<Integer> getChildren(int node) {
		return children[node];
	}
	
	/**
	 * Get the parent of a given node
	 * @param node - A node, between 0 (inclusive) and the size of the tree (exclusive)
	 * @return the parent of the given node (or Tree.NO_PARENT if the node doesn't have a parent)
	 */
	public int getParent(int node) {
		return parent[node];
	}
	
	/**
	 * Set the parent of a given node.
	 * @param node - A node, between 0 (inclusive) and the size of the tree (exclusive)
	 * @param parentNode - Another node, different from <code>node</code>, between 0 (inclusive) and the size of the tree (exclusive)
	 */
	public void setParent(int node, int parentNode) {
		if(parentNode == node)
			System.err.println("[TREE ERROR] A tree node cannot be assigned as its own parent.");
		// If the node was a child, remove it from the corresponding children list
		if(parent[node] != -1)
			children[parent[node]].remove((Object) node);
		// Assign new parent
		parent[node] = parentNode;
		children[parentNode].add(node);
	}
	
	/**
	 * Get the root node of this tree.
	 * @return the root node
	 */
	public int getRootNode() {
		return rootNode;
	}
	
	/**
	 * Compute the total number of children (including indirect ones) of each tree node.
	 * @return a Map containing the total number of children of each node
	 */
	public Map<Integer, Integer> computeNumberOfChildren() {
		Map<Integer, Integer> storage = new HashMap<Integer, Integer>();
		computeNumberOfChildren(getRootNode(), storage);
		return storage;
	}
	
	/**
	 * Recursively compute the total number of children (including indirect ones) of a given node, 
	 * storing results for the whole sub-tree in a given Map.
	 * @param node - A node, between 0 (inclusive) and the size of the tree (exclusive)
	 * @param allChildren - A map used for storage
	 * @return the total number of chidren of the given node
	 */
	private int computeNumberOfChildren(int node, Map<Integer, Integer> allChildren) {
		if(allChildren.containsKey(node))
			return allChildren.get(node);
		int c = getChildren(node).size();
		for(int child : getChildren(node))
			c += computeNumberOfChildren(child, allChildren);
		allChildren.put(node, c);
		return c;
	}
	
	/**
	 * Test the integrity of the tree a print potential problems.
	 */
	public void test() {
		List<Integer> visitedChildren = new ArrayList<Integer>();
		for(int i = 0; i < parent.length; i++) {
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
		/*String str = "Edges\n"; 
		for(int i = 1; i < getGraph().getNumVertices(); i++) 
			str += parent[i] + "-" + i + ", ";*/
		String str = "";
		for(int i = 0; i < children.length; i++)
			str += i + ": " + String.join(",", children[i].stream().map(val -> val+"").collect(Collectors.toList())) + "\n";
		return str;
	}
}