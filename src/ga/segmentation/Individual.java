package ga.segmentation;

import java.util.ArrayList;
import java.util.List;

import ga.IIndividual;
import problem.segmentation.ProblemInstance;
import utils.PrimMST;

public class Individual implements IIndividual {
	public static enum Direction {NONE, UP, RIGHT, DOWN, LEFT}
	
	private Direction[][] representation;
	
	public Individual(ProblemInstance pi) {
		this.representation = new Direction[pi.getImage().getWidth()][pi.getImage().getHeight()];
	}
	
	@Override
	public float getFitness() {
		return 0;
	}

	@Override
	public void mutate() {
		
	}

	@Override
	public IIndividual crossover(IIndividual parentB) {
		return null;
	}
	
	public static Individual createRandomIndividual(ProblemInstance pi) {
		Individual ind = new Individual(pi);
		
		// Generate a random position as the origin of the min spanning tree
		int mstX = (int) Math.floor(Math.random() * pi.getImage().getWidth());
		int mstY = (int) Math.floor(Math.random() * pi.getImage().getHeight());
	
		System.out.println("Generating minimum spanning tree...");
		long time = System.nanoTime();
		PrimMST minSpanningTree = new PrimMST(pi.getEuclideanDistanceGraph(), mstY * pi.getImage().getHeight() + mstX);
		System.out.println("Done (took " + (System.nanoTime() - time)/1000000.0 + " ms)");
		
		/*for(int i = 0; i < minSpanningTree.getGraph().getNumVertices(); i++) {
			String str = "";
			for(int j = 0; j < minSpanningTree.getChildren(i).size(); j++)
				str += minSpanningTree.getChildren(i).get(j) + ",";
			System.out.println(str);
		}*/
		minSpanningTree.test();
		
		// Turn the MST into segments
		System.out.println("Creating segmentation from minimum spanning tree...");
		time = System.nanoTime();
		//mstToSegmentation(pi, ind.representation, minSpanningTree);
		System.out.println("Done (took " + (System.nanoTime() - time)/1000000.0 + " ms)");
				
		
		return ind;
	}
	
	public static void mstToSegmentation(ProblemInstance pbi, Direction[][] seg, PrimMST tree) {
		int[] pos = pbi.pixelIndexToPos(tree.getRootVertex());
		seg[pos[0]][pos[1]] = Direction.NONE;
		segmentChildren(pbi, seg, tree, tree.getRootVertex());
	}
	
	private static List<Integer> visited = new ArrayList<Integer>();
	private static void segmentChildren(ProblemInstance pbi, Direction[][] seg, PrimMST tree, int vertex) {
		List<Integer> children = tree.getChildren(vertex);
		int[] pos = pbi.pixelIndexToPos(vertex);
		
		if(visited.contains(vertex))
			System.out.println("Already visited " + vertex);
		
		visited.add(vertex);
		
		for(int i = 0; i < children.size(); i++) {
			int child = children.get(i);
			int[] pos2 = pbi.pixelIndexToPos(child);
			Direction d = pbi.getDirection(pos2[0], pos2[1], pos[0], pos[1]);
			seg[pos2[0]][pos2[1]] = d;
			segmentChildren(pbi, seg, tree, child);
		}
		
	}
}
