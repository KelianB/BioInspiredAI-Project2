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

	public int[][] getNeighbors (int xi, int yi) {

		int[][] neighbors = new int[4][2];
		int n = 0;

		if (xi < this.representation[0].length-1 && (representation[xi][yi]==Direction.DOWN || representation[xi+1][yi]==Direction.UP)) {
			neighbors[n][0] = xi+1;
			neighbors[n][1] = yi;
			n++;
		}
		if (xi>0 && (representation[xi][yi]==Direction.UP || representation[xi-1][yi]==Direction.DOWN)) {
			neighbors[n][0] = xi-1;
			neighbors[n][1] = yi;
			n++;
		}
		if (yi < this.representation.length-1 && (representation[xi][yi]==Direction.RIGHT || representation[xi][yi+1]==Direction.LEFT)) {
			neighbors[n][0] = xi;
			neighbors[n][1] = yi+1;
			n++;
		}
		if (yi>0 && (representation[xi][yi]==Direction.LEFT || representation[xi][yi-1]==Direction.RIGHT)) {
			neighbors[n][0] = xi;
			neighbors[n][1] = yi-1;
			n++;
		}

		return neighbors;
	}

	public boolean sameSegment(int xi, int yi, int xj, int yj) {
		if ((xi == xj) && (yi==yj)) {
			return true;
		}
		if (this.representation[xi][yi]==Direction.NONE) {
			return false;
		}
		else {
			int [][] neighbors = getNeighbors (xi, yi);
			boolean bool = false;
			for (int i=0 ; i <neighbors[0].length ; i++) {
				bool = bool || sameSegment(neighbors[i][0],neighbors[i][1], xj,yj);
			}
		}
	}

	public float dist (int xi, int yi, int xj, int yj) {
		if (sameSegment(xi,yi,xj,yj)) {
			return 0;
		} else {

		}
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
