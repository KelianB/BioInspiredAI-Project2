package ga.segmentation;

import static problem.segmentation.ProblemInstance.euclideanDistance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ga.IIndividual;
import problem.segmentation.ProblemInstance;
import utils.CachedValue;
import utils.PrimMST;

public class Individual implements IIndividual {
	public static enum Direction {NONE, UP, RIGHT, DOWN, LEFT}
	
	// Store the segmentation as an array of directions.
	private Direction[] representation;
	
	// Store the segmentation as a list of segments
	private List<Segment> segments;
	// Store the segmentation as a segment matrix
	private int[] pixelSegments;
	
	// Store the last computed value for each objective
	private CachedValue<Float> edgeValue, connectivity, overallDeviation;

	private CachedValue<Float> fitness;
	
	private ProblemInstance pi;
	
	public Individual(ProblemInstance pi) {
		this.pi = pi;
		this.representation = new Direction[pi.getImage().getWidth() * pi.getImage().getHeight()];
		
		edgeValue = new CachedValue<Float>(() -> {
			return computeEdgeValue();
		});
		connectivity = new CachedValue<Float>(() -> {
			return computeConnectivity();
		});
		overallDeviation = new CachedValue<Float>(() -> {
			return computeOverallDeviation();
		});

		fitness = new CachedValue<Float>(() -> {
			return edgeValue.getValue() + connectivity.getValue() + overallDeviation.getValue();
		});
	}
	
	public float getEdgeValue() {
		return edgeValue.getValue();
	}
	
	public float getConnectivity() {
		return connectivity.getValue();
	}
	
	public float getOverallDeviation() {
		return overallDeviation.getValue();
	}
	
	@Override
	public float getFitness() {
		updateSegmentRepresentation();
		return fitness.getValue();
	}

	@Override
	public void mutate() {
		int randPos = (int) (Math.random() * this.representation.length);
		Direction randDir;
		
		do {
			randDir = Direction.values()[(int) (Math.random() * Direction.values().length)];
		} while(this.representation[randPos] == randDir);
		
		this.representation[randPos] = randDir; 
		
		edgeValue.needsUpdating();
		connectivity.needsUpdating();
		overallDeviation.needsUpdating();
		
		
	}

	@Override
	public IIndividual crossover(IIndividual parentB) {
		return null;
	}
	
	/**
	 * Create an individual using a minimum spanning tree
	 * @param pi - The problem instance for which to create a new individual
	 * @return a new individual
	 */
	public static Individual createRandomIndividual(ProblemInstance pi) {
		Individual ind = new Individual(pi);
		
		// Generate a random position as the origin of the minimum spanning tree
		int startingPos = (int) (Math.random() * pi.getImage().getWidth() * pi.getImage().getHeight());
	
		// Generate the minimum spanning tree
		System.out.println("Generating minimum spanning tree...");
		long time = System.nanoTime();
		PrimMST minSpanningTree = new PrimMST(pi.getEuclideanDistanceGraph(), startingPos);
		System.out.println("Done (took " + (System.nanoTime() - time)/1000000.0 + " ms)");
		
		// Turn the minimum spanning tree into a segmentation
		System.out.println("Creating segmentation from minimum spanning tree...");
		time = System.nanoTime();
		ind.createDirectionMatrixFromMST(minSpanningTree);
		System.out.println("Done (took " + (System.nanoTime() - time)/1000000.0 + " ms)");
		
		return ind;
	}

	/**
	 * Get the indices of a given pixel's cardinal neighbors.
	 * @param i - A pixel index
	 * @return a list of neighbors indices
	 */
	public List<Integer> getNeighbors(int i) {
		Stream<Direction> dirs = Arrays.stream(Direction.values());
		Stream<Integer> neighbors = dirs.map((d) -> getPixelIndex(i, d)).filter((a) -> a != -1);
		return neighbors.collect(Collectors.toList());
		
		/*List<Integer> neighbors = new ArrayList<>(); 
		for(Direction d : Direction.values()) {
			int pixel = getPixelIndex(i, d);
			if(pixel != -1)
				neighbors.add(pixel);
		}
		return neighbors;*/
	}


	/**
	 * Check if two pixels belong to the same segment
	 * @param i - A pixel index
	 * @param j - Another pixel index
	 * @return true if the two given pixels belong to the same segment, else false
	 */
	public boolean sameSegment(int i, int j) {
		return pixelSegments[i] == pixelSegments[j];
	}

	/**
	 * Compute the distance between two given Spixels.
	 * @param i - A pixel index
	 * @param j - Another pixel index
	 * @return the euclidean distance between pixels i and j in RGB space if i and j don't belong to the same segment, else 0
	 */
	public float dist(int i, int j) {
		return sameSegment(i, j) ? 0 : euclideanDistance(pi.getRGB(i), pi.getRGB(j));
	}

	/**
	 * Compute the edge value for the current segmentation of this individual
	 * @return the edge value
	 */
	public float computeEdgeValue() {
		float edgeValue = 0;
		for(int i = 0 ; i < this.representation.length; i++) {
			for(int n : getNeighbors(i))
				edgeValue += dist(i, n);
		}
		return edgeValue;
	}

	/**
	 * Compute the connectivity value for the current segmentation of this individual
	 * @return the connectivity
	 */
	public float computeConnectivity() {
		float connectivity = 0;
		for(int i = 0; i < this.representation.length; i++) {
			for(int n : getNeighbors(i)) {
				if(!sameSegment(i,n))
					connectivity += 1.0/8;
			}
		}
		return connectivity;
	}

	/**
	 * Compute overall deviation for the current segmentation of this individual
	 * @return the overalll deviation
	 */
	public float computeOverallDeviation() {
		float overallDeviation = 0;
		
		// For each segment
		for(Segment s : segments) {
			// First compute the centroid
			float[] centroid = s.calculateCentroid(pi);
			
			// Then add up the deviation for the current segment
			for(int i : s.getPixels()) {
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

	/**
	 * Recursively assign pixels to their segment starting at pixel index i
	 * @param i - The starting pixel
	 * @param visited - An array of visited pixels
	 */
	private void assignToSegment(int i, List<Integer> visited) {
		int j = getPixelIndex(i, representation[i]);
		if(j == -1 || visited.contains(i)) {
			visited.add(i);
			pixelSegments[i] = currentSegmentIndex++;
		}
		else {
			visited.add(i);
			if(pixelSegments[j] == -1)
				assignToSegment(j, visited);
			pixelSegments[i] = pixelSegments[j];
		}
	}
	
	/**
	 * Compute the segment reprentation from the directions matrix.
	 */
	private void updateSegmentRepresentation() {
		long time = System.nanoTime();
		currentSegmentIndex = 0;
		
		// First assign each pixel to its segment
		pixelSegments = new int[representation.length];
		for(int i = 0; i < pixelSegments.length; i++)
			pixelSegments[i] = -1;
		for(int i = 0; i < pixelSegments.length; i++) {
			if(pixelSegments[i] == -1)
				assignToSegment(i, new ArrayList<Integer>());
		}

		// System.out.println("decoding took " + (System.nanoTime() - time) / 1000000 + "ms");
		time = System.nanoTime();
		
		// Create representation as list of segments

		// Start by creating the right amount of empty segments
		segments = new ArrayList<Segment>();
		for(int i = 0; i <= currentSegmentIndex; i++)
			segments.add(new Segment());
		// Then add the pixels to the right segments
		for(int i = 0; i < pixelSegments.length; i++)
			segments.get(pixelSegments[i]).addPixel(i);	
		
		// System.out.println("transforming into list of segments took " + (System.nanoTime() - time) / 1000000 + "ms");
	}
	
	/**
	 * Get the pixel in a given direction from a source pixel.
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
	
	/**
	 * Print this individual to the console.
	 */
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
	
	/**
	 * Create a segmentation as a direction matrix from a minimum spanning tree.
	 * @param tree - A minimum spanning tree
	 */
	private void createDirectionMatrixFromMST(PrimMST tree) {
		representation[tree.getRootVertex()] = Direction.NONE;
		
		// Begin recursive segmentation at from the tree's root vertex
		segmentChildren(tree, tree.getRootVertex());
		
		// Break single segment into multiple segment
		int numSegments = 4 + (int) (Math.random() * 6);
		for(int i = 0; i < numSegments; i++) {
			int rdPos;
			do {
				rdPos = (int) (Math.random() * representation.length);
			}
			while(representation[rdPos] == Direction.NONE);
			representation[rdPos] = Direction.NONE;
		}
	}
	
	/**
	 * Recursively set the direction of all children of a given vertex in a given minimum spanning tree of the image
	 * @param tree - A minimum spanning tree
	 * @param vertex - A vertex in the tree
	 */
	private void segmentChildren(PrimMST tree, int vertex) {
		for(int child : tree.getChildren(vertex)) {
			representation[child] = pi.getDirection(child, vertex);
			segmentChildren(tree, child);
		}	
	}
	
	/**
	 * Get the segmentation as a list of segments.
	 * @return a list of segments
	 */
	public List<Segment> getSegments() {
		return segments;
	}

	/**
	 * Get the segmentation as a pixel matrix.
	 * @return a pixel matrix indicading which segment each pixel belongs to
	 */
	public int[] getPixelSegments() {
		return pixelSegments;
	}
	
}
