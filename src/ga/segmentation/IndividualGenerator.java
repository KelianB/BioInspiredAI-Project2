package ga.segmentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ga.segmentation.Individual.Direction;
import main.Main;
import utils.FastPrimMST;
import utils.Tree;

/**
 * A class used for generating individuals.
 * @author Kelian Baert & Caroline de Pourtales
 */
public class IndividualGenerator {
	/**
	 * Create an individual using a minimum spanning tree
	 * @param pi - The problem instance for which to create a new individual
	 * @return a new individual
	 */
	public static Individual createRandomIndividual(SegmentationGA ga) {
		ProblemInstance pi = ga.getProblemInstance();
		
		// Generate a random position as the origin of the minimum spanning tree
		int startingPos = (int) (ga.random() * pi.getImage().getWidth() * pi.getImage().getHeight());
		
		// Generate the minimum spanning tree
		long time = System.nanoTime();
		Tree minSpanningTree = FastPrimMST.createMinimumSpanningTree(pi.getEuclideanDistanceGraph(), startingPos);
		long mstGenTime = (System.nanoTime() - time) / 1000000;
		
		// Turn the minimum spanning tree into a segmentation
		time = System.nanoTime();
		Direction[] genotype = createDirectionMatrixFromTree(ga, minSpanningTree);
		long segTime = (System.nanoTime() - time) / 1000000;
				
		Individual ind = new Individual(ga, genotype);
		
		System.out.println("Generated new individual (MST: " + mstGenTime + "ms, segmentation: " + segTime + "ms). Segments: " + ind.getSegments().size());
		return ind;
	}

	private static class Edge {
		public int node;
		public float weight;
		public Edge(int node, float weight) {this.node = node; this.weight = weight;}
	}

	private static void getEdges(ProblemInstance pi, Tree tree, int node, List<Edge> edges) {
		List<Integer> children = tree.getChildren(node);
		for(int i = 0; i < children.size(); i++) {
			int childNode = children.get(i);
			edges.add(new Edge(childNode, pi.getEuclideanDistance(node, childNode)));
			getEdges(pi, tree, childNode, edges);
		}
	}
	
	/**
	 * Create a segmentation as a direction matrix from a minimum spanning tree.
	 * @param tree - A minimum spanning tree
	 */
	private static Direction[] createDirectionMatrixFromTree(SegmentationGA ga, Tree tree) {
		ProblemInstance pi = ga.getProblemInstance();
		
		// Create a blank genotype
		Direction[] directions = new Direction[tree.getSize()];
		directions[tree.getRootNode()] = Direction.NONE;
		
		// Begin recursive segmentation from the tree's root vertex
		segmentChildren(pi, tree, tree.getRootNode(), directions);
		
		// Break single segment into multiple segment (break the segment where the rgb distance is the highest)
		
		// we first go through the tree in order to build a list of all edges in the tree associated with their weight in the graph
		List<Edge> edges = new ArrayList<Edge>();
		getEdges(pi, tree, tree.getRootNode(), edges);
		
		// remove edges that have a low number of children
		Map<Integer, Integer> numberOfChildren = tree.computeNumberOfChildren();
		for(int i = 0; i < edges.size(); i++) {
			if(numberOfChildren.get(edges.get(i).node) < 150) {
				edges.remove(i);
				i--;
			}
		}
		
		int minSegments = Main.config.getInt("minInitialSegments"),
			maxSegments = Main.config.getInt("maxInitialSegments");
		
		int numberOfSegments = minSegments + (int) (ga.random() * (maxSegments - minSegments));

		// then sort the edges by decreasing weight and keep only the edges with good enough weight
		edges.sort((a,b) -> (int) Math.signum(b.weight - a.weight));	
		edges = edges.subList(0, Math.min(edges.size()-1, numberOfSegments*20));		
		
		// then break the segment
		for(int i = 0; i < numberOfSegments - 1; i++) {
			int edge = (int) (ga.random() * edges.size());
			int breakingPoint = edges.get(edge).node;
			directions[breakingPoint] = Direction.NONE;
			edges.remove(edge);
		}
		
		return directions;
	}
	
	/**
	 * Recursively set the direction of all children of a given vertex in a given tree.
	 * The direction is set to that each node points to its parent.
	 * @param pi - A reference to the problem instance
	 * @param tree - A minimum spanning tree
	 * @param vertex - A vertex in the tree
	 * @param directions - A directions array
	 */
	private static void segmentChildren(ProblemInstance pi, Tree tree, int vertex, Direction[] directions) {
		for(int child : tree.getChildren(vertex)) {
			directions[child] = pi.getDirection(child, vertex);
			segmentChildren(pi, tree, child, directions);
		}	
	}
}
