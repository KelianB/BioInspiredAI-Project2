package ga.segmentation;

import static problem.segmentation.ProblemInstance.euclideanDistance;

import java.util.ArrayList;
import java.util.List;

import ga.IIndividual;
import problem.segmentation.ProblemInstance;
import utils.PrimMST;

public class Individual implements IIndividual {
	public static enum Direction {NONE, UP, RIGHT, DOWN, LEFT}
	
	private Direction[] representation;
	private ProblemInstance pi;
	
	private List<Segment> segments;
	private int[] pixelSegments;
	
	public Individual(ProblemInstance pi) {
		this.pi = pi;
		this.representation = new Direction[pi.getImage().getWidth() * pi.getImage().getHeight()];
	}
	
	@Override
	public float getFitness() {
		updateSegmentRepresentation();
		System.out.println("Edge value: " + edgeValue());
		System.out.println("Connectivity: " + connectivity());
		System.out.println("Overall deviation: " + overallDeviation());
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
		
		// Turn the MST into segments
		System.out.println("Creating segmentation from minimum spanning tree...");
		time = System.nanoTime();
		mstToSegmentation(pi, ind.representation, minSpanningTree);
		System.out.println("Done (took " + (System.nanoTime() - time)/1000000.0 + " ms)");
		
		return ind;
	}

	public List<Integer> getNeighbors(int i) {
		List<Integer> neighbors = new ArrayList<>();

		for(Direction d : Direction.values()) {
			int pixel = getPixelIndex(i, d);
			if(pixel != -1)
				neighbors.add(pixel);
		}
		
		return neighbors;
	}


	public boolean sameSegment(int i, int j) {
		return pixelSegments[i] == pixelSegments[j];
	}

	public float dist(int i, int j) {
		return sameSegment(i, j) ? 0 : euclideanDistance(pi.getRGB(i), pi.getRGB(j));
	}

	public float edgeValue() {
		float edgeValue = 0;
		for(int i = 0 ; i < this.representation.length; i++) {
			for(int n : getNeighbors(i))
				edgeValue += dist(i, n);
		}
		return edgeValue;
	}

	public float connectivity() {
		float connectivity = 0.0f;
		for(int i = 0; i < this.representation.length; i++) {
			for(int n : getNeighbors(i)) {
				if(!sameSegment(i,n))
					connectivity += 1.0/8;
			}
		}
		return connectivity;
	}

	public float overallDeviation() {
		float overallDeviation = 0;
		
		// For each segment
		for(int s = 0; s < segments.size(); s++) {
			// First compute the centroid
			float[] centroid = new float[3];
			float numPixels = segments.get(s).getPixels().size();
			for(int i : segments.get(s).getPixels()) {
				int[] rgb = pi.getRGB(i);
				centroid[0] += rgb[0] / numPixels;
				centroid[1] += rgb[1] / numPixels;
				centroid[2] += rgb[2] / numPixels;
			}
			
			// Then update the deviation
			for(int i : segments.get(s).getPixels()) {
				int[] rgb = pi.getRGB(i);
				
				overallDeviation += Math.sqrt(
					Math.pow(rgb[0]-centroid[0], 2) +
					Math.pow(rgb[1]-centroid[1], 2) +
					Math.pow(rgb[2]-centroid[2], 2)
				);
			}
		}
		return overallDeviation;
	}
	
	private int currentSegmentIndex;

	private void assignSegment(int i, List<Integer> visited) {
		int j = getPixelIndex(i, representation[i]);
		if(j == -1 || visited.contains(i)) {
			visited.add(i);
			pixelSegments[i] = currentSegmentIndex++;
		}
		else {
			visited.add(i);
			if(pixelSegments[j] == -1)
				assignSegment(j, visited);
			pixelSegments[i] = pixelSegments[j];
		}
	}
	
	private void updateSegmentRepresentation() {
		currentSegmentIndex = 0;
		
		// First assign each pixel to its segment
		pixelSegments = new int[representation.length];
		for(int i = 0; i < pixelSegments.length; i++)
			pixelSegments[i] = -1;
		for(int i = 0; i < pixelSegments.length; i++) {
			if(pixelSegments[i] == -1)
				assignSegment(i, new ArrayList<Integer>());
		}

		// Create representation as list of segments

		// Start by creating the right amount of empty segments
		segments = new ArrayList<Segment>();
		for(int i = 0; i <= currentSegmentIndex; i++)
			segments.add(new Segment());
		// Then add the pixels to the right segments
		for(int i = 0; i < pixelSegments.length; i++)
			segments.get(pixelSegments[i]).addPixel(i);	
	}
	
	/**
	 * Get the pixel in a given direction from a source pixel/
	 * @param source - A pixel index
	 * @param dir - A direction
	 * @return the pixel in the given direction from the source.
	 */
	public int getPixelIndex(int source, Direction dir) {
		int none = -1;
		int w = pi.getImage().getWidth();
				
		switch(dir) {
			case NONE:
				return none;
			case LEFT:
				if(source % w == 0)
					return none;
				return source - 1;
			case RIGHT:
				if(source % w == w - 1)
					return none;
				return source + 1;
			case UP:
				if(source < w)
					return none;
				return source - w;
			case DOWN:
				if(source >= w * (pi.getImage().getHeight()-1))
					return none;
				return source + w;
			default:
				return none;
		}	
	}
	
	public List<Segment> getSegments() {
		return segments;
	}

	public int[] getPixelSegments() {
		return pixelSegments;
	}
	
	public void print() {
		String str = "";
		for(int i = 0; i < representation.length; i++) {
			if(i % pi.getImage().getWidth() == 0) {
				System.out.println(str);
				str = "";
			}
			str += pixelSegments[i] + " ";
		}
		System.out.println(str);
	}
	
	public static void mstToSegmentation(ProblemInstance pbi, Direction[] seg, PrimMST tree) {
		seg[tree.getRootVertex()] = Direction.NONE;
		segmentChildren(pbi, seg, tree, tree.getRootVertex());
		
		// Break single segment into multiple segment
		int numSegments = 4 + (int) (Math.random() * 6);
		for(int i = 0; i < numSegments; i++) {
			int rdPos;
			do {
				rdPos = (int) (Math.random() * seg.length);
			}
			while(seg[rdPos] == Direction.NONE);
			seg[rdPos] = Direction.NONE;
		}
	}
	
	private static void segmentChildren(ProblemInstance pbi, Direction[] seg, PrimMST tree, int vertex) {
		List<Integer> children = tree.getChildren(vertex);

		for(int i = 0; i < children.size(); i++) {
			int child = children.get(i);
			seg[child] = pbi.getDirection(child, vertex);
			segmentChildren(pbi, seg, tree, child);
		}	
	}
}
