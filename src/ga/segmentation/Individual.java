package ga.segmentation;

import java.util.ArrayList;
import java.util.List;

import ga.IIndividual;
import problem.segmentation.ProblemInstance;
import utils.PrimMST;

import static problem.segmentation.ProblemInstance.euclideanDistance;

public class Individual implements IIndividual {
	public static enum Direction {NONE, UP, RIGHT, DOWN, LEFT}
	
	private Direction[][] representation;
	private ProblemInstance pi;
	
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

	public LinkedList<Segment> getSegmentation () {

		LinkedList<Segment> segmentation_list = new LinkedList<Segment>();

		return segmentation_list;
	}

	public Segment[][] getSegmentation () {

		Segment[][] segmentation_number = new Segment[this.representation[0].length][this.representation.length];

		return segmentation_number;
	}

	public boolean sameSegment(int xi, int yi, int xj, int yj) {

	}

	public float dist (int xi, int yi, int xj, int yj) {
		if (sameSegment(xi,yi,xj,yj)) {
			return 0;
		} else {
			return euclideanDistance(pi.getRGB(xi, yi), pi.getRGB(xj, yj);
		}
	}

	public float edgeValue () {
		float edgeValue = 0;
		for (int i=0 ; i < this.representation[0].length ; i++) {
			for (int j=0 ; j < this.representation.length ; j++) {
				int [][] neighbors = getNeighbors (i, j);
				float sum_neighbors = 0;
				for (int n=0 ; n <neighbors[0].length ; n++) {
					sum_neighbors+=dist(i,j,neighbors[i][0],neighbors[i][1]);
				}
				edgeValue+=sum_neighbors;
			}
		}
		return edgeValue;
	}

	public float connectivity () {
		float connectivity = 0;
		for (int i=0 ; i < this.representation[0].length ; i++) {
			for (int j=0 ; j < this.representation.length ; j++) {
				int [][] neighbors = getNeighbors (i, j);
				float sum_neighbors = 0;
				for (int n=0 ; n <neighbors[0].length ; n++) {
					if (!sameSegment(i,j,neighbors[i][0],neighbors[i][1])) {
						sum_neighbors+=1/8;
					}
				}
				connectivity+=sum_neighbors;
			}
		}
		return connectivity;
	}

	public float overallDeviation () {
		float overallDeviation=0;

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
