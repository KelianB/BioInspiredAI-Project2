package ga.segmentation;

import static problem.segmentation.ProblemInstance.euclideanDistance;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ga.GeneticAlgorithm;
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
	
	private GeneticAlgorithm ga;

	public Individual(GeneticAlgorithm ga) {
		this.ga = ga;
		
		ProblemInstance pi = ((ProblemInstance) ga.getProblemInstance());
		this.representation = new Direction[pi.getImage().getWidth() * pi.getImage().getHeight()];
		
		edgeValue = new CachedValue<Float>(this::computeEdgeValue);
		connectivity = new CachedValue<Float>(this::computeConnectivity);
		overallDeviation = new CachedValue<Float>(this::computeOverallDeviation);

		float alpha = 1 / (float) 5e2,
			  beta = 1.0f,
			  gamma = 1 / (float) 5e5;
		
		fitness = new CachedValue<Float>(() -> {
			return alpha * edgeValue.getValue() - beta * connectivity.getValue() - gamma * overallDeviation.getValue();
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
		return fitness.getValue();
	}

	@Override
	public void mutate() {
		int randPos = (int) (ga.random() * this.representation.length);
		
		Direction randDir;
		Direction[] dirs = Direction.values();
		do {
			randDir = dirs[(int) (ga.random() * dirs.length)];
		} while(this.representation[randPos] == randDir);
		
		this.representation[randPos] = randDir; 
		
		updateSegmentRepresentation();
		edgeValue.needsUpdating();
		connectivity.needsUpdating();
		overallDeviation.needsUpdating();
		fitness.needsUpdating();
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
	public static Individual createRandomIndividual(GeneticAlgorithm ga) {
		Individual ind = new Individual(ga);
		
		ProblemInstance pi = (ProblemInstance) ga.getProblemInstance();
		
		// Generate a random position as the origin of the minimum spanning tree
		int startingPos = (int) (ga.random() * pi.getImage().getWidth() * pi.getImage().getHeight());
		
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
		
		ind.updateSegmentRepresentation();
		System.out.println("segments: " + ind.segments.size());
		return ind;
	}

	/**
	 * Get the indices of a given pixel's cardinal neighbors.
	 * @param i - A pixel index
	 * @return a list of neighbors indices
	 */
	public List<Integer> getNeighbors(int i) {
		Stream<Direction> dirs = Arrays.stream(Direction.values());
		Stream<Integer> neighbors = dirs.map((d) -> getPixelIndex(i, d)).filter(a -> a != -1);
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
		ProblemInstance pi = (ProblemInstance) ga.getProblemInstance();
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
					connectivity += 1.0f/8;
			}
		}
		return connectivity;
	}

	/**
	 * Compute overall deviation for the current segmentation of this individual
	 * @return the overalll deviation
	 */
	public float computeOverallDeviation() {
		ProblemInstance pi = (ProblemInstance) ga.getProblemInstance();
		
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
	public void updateSegmentRepresentation() {
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
		for(int i = 0; i < currentSegmentIndex; i++)
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
		ProblemInstance pi = ((ProblemInstance) ga.getProblemInstance());
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
		int width = ((ProblemInstance) ga.getProblemInstance()).getImage().getWidth();
		String str = "";
		for(int i = 0; i < representation.length; i++) {
			if(i % width == 0) {
				System.out.println(str);
				str = "";
			}
			str += pixelSegments[i] + " ";
		}
		System.out.println(str);
	}
	
	
	private void getEdges(PrimMST tree, int node, List<Edge> edges) {
		List<Integer> children = tree.getChildren(node);
		for(int i = 0; i < children.size(); i++) {
			int childNode = children.get(i);
			float distance = ((ProblemInstance) ga.getProblemInstance()).getEuclideanDistanceGraph().getWeight(node, childNode);
			edges.add(new Edge(node, childNode, distance));
			getEdges(tree, childNode, edges);
		}
	}
	
	private int getIndirectChildren(PrimMST tree, int node, HashMap<Integer, Integer> indirectChildren) {
		if(indirectChildren.containsKey(node))
			return indirectChildren.get(node);
		int c = tree.getChildren(node).size();
		for(int child : tree.getChildren(node))
			c += getIndirectChildren(tree, child, indirectChildren);
		indirectChildren.put(node, c);
		return c;
	}
	
	class Edge {
		public int parent, child;
		public float weight;
		public Edge(int parent, int child, float weight) {this.parent = parent; this.child = child; this.weight = weight;}
	}
	
	/**
	 * Create a segmentation as a direction matrix from a minimum spanning tree.
	 * @param tree - A minimum spanning tree
	 */
	public void createDirectionMatrixFromMST(PrimMST tree) {
		representation[tree.getRootVertex()] = Direction.NONE;
		
		// Begin recursive segmentation from the tree's root vertex
		segmentChildren(tree, tree.getRootVertex());
		
		// Break single segment into multiple segment (break the segment where the rgb distance is the highest)
		
		// we first go through the tree in order to build a list of all edges in the tree associated with their weight in the graph
		List<Edge> edges = new ArrayList<Edge>();
		getEdges(tree, tree.getRootVertex(), edges);
		// then sort the edges by decreasing weight
		edges.sort((a,b) -> (int) Math.signum(b.weight - a.weight));
		
		
		HashMap<Integer, Integer> indirectChildren = new HashMap<Integer, Integer>();
		for(int i = 0; i < representation.length; i++)
			getIndirectChildren(tree, i, indirectChildren);
		
		for(int i = 0; i < edges.size(); i++) {
			if(indirectChildren.get(edges.get(i).child) < 100) {
				edges.remove(i);
				i--;
			}
			// System.out.println(e.weight);
		}
		
		edges = edges.subList(0, (int) (edges.size() * 0.05));		
		
		// then break the segment
		int numberOfSegments = 6 + (int) (ga.random() * 12);

		for(int i = 0; i < numberOfSegments - 1; i++) {
			int edge = (int) (ga.random() * edges.size());

			int breakingPoint = /*edges.get(i)*/edges.get(edge).child;
			representation[breakingPoint] = Direction.NONE;
			edges.remove(edge);
		}
		
	}
	
	/**
	 * Recursively set the direction of all children of a given vertex in a given minimum spanning tree of the image
	 * @param tree - A minimum spanning tree
	 * @param vertex - A vertex in the tree
	 */
	private void segmentChildren(PrimMST tree, int vertex) {
		ProblemInstance pi = ((ProblemInstance) ga.getProblemInstance());
		for(int child : tree.getChildren(vertex)) {
			representation[child] = pi.getDirection(child, vertex);
			segmentChildren(tree, child);
		}	
	}
	
	public void printDirectionArray() {
		String str = "";
		int w = ((ProblemInstance) ga.getProblemInstance()).getImage().getWidth();
		for(int i = 0; i < representation.length; i++) {
			Direction d = representation[i];
			str += (d == Direction.DOWN ? "_" : d == Direction.UP ? "^" : d == Direction.LEFT ? "<" : d == Direction.RIGHT ? ">" : d == Direction.NONE ? "o" : "?");
			str += " ";
			if((i+1) % w == 0)
				str += "\n";
		}
		System.out.println(str);	
	}
	
	public void printSegmentation() {
		String str = "";
		int w = ((ProblemInstance) ga.getProblemInstance()).getImage().getWidth();
		for(int i = 0; i < representation.length; i++) {
			str += pixelSegments[i] + " ";
			if((i+1) % w == 0)
				str += "\n";
		}
		System.out.println(str);	
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
	
	/**
	 * Generate the output images for the current segmentation.
	 * @return the output images
	 */
	public BufferedImage[] generateImages() {
		List<Integer> edgePixels = new ArrayList<Integer>();
		ProblemInstance pi = ((ProblemInstance) ga.getProblemInstance());
		int w = pi.getImage().getWidth();
		int h = pi.getImage().getHeight();
		for(int i = 0; i < pixelSegments.length; i++) {
			if((i+1) % w != 0 && 
				(pixelSegments[i] != pixelSegments[i+1] || 
				(i+w < pixelSegments.length && pixelSegments[i] != pixelSegments[i+w])))
				edgePixels.add(i);
		}
		
		// First output image (overlayed green edges)
		BufferedImage bufferedImage1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufferedImage1.createGraphics();
        g.drawImage(pi.getImage(), 0, 0, null);
        
        g.setColor(new Color(0, 255, 0));
        for(int i : edgePixels) {
        	int[] pos = pi.pixelIndexToPos(i);
            g.fillRect(pos[0], pos[1], 1, 1);
        }
        g.dispose();

        // Second output image (only black edges)
		BufferedImage bufferedImage2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        g = bufferedImage2.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.BLACK);
        for(int i : edgePixels) {
        	int[] pos = pi.pixelIndexToPos(i);
            g.fillRect(pos[0], pos[1], 1, 1);
        }
        g.dispose();

        return new BufferedImage[] {bufferedImage1, bufferedImage2};
	}
	
	@Override
	public IIndividual copy() {
		Individual copy = new Individual(ga);
		copy.representation = representation.clone();
		copy.edgeValue = edgeValue.copy();
		copy.connectivity = connectivity.copy();
		copy.overallDeviation = overallDeviation.copy();
		copy.fitness = fitness.copy();
		copy.updateSegmentRepresentation();
		return copy;
	}
}
