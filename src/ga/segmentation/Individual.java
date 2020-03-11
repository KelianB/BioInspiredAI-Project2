package ga.segmentation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ga.IIndividual;
import main.Main;
import problem.segmentation.ProblemInstance;
import utils.CachedValue;

/**
 * A specific IIndividual implementation for the image segmentation GA.
 * @author Kelian Baert & Caroline de Pourtales
 */
public class Individual implements IIndividual {
	public static enum Direction {
		NONE, UP, RIGHT, DOWN, LEFT;
	}
	
	// Store the segmentation as an array of directions.
	private Direction[] representation;
	
	// Store the segmentation as a list of segments
	private List<Segment> segments;
	
	// Store the segmentation as a segment matrix
	private int[] pixelSegments;
	
	// Store the last computed value for each objective
	private CachedValue<Float> edgeValue, connectivity, overallDeviation;
	
	private CachedValue<Float> fitness;
	
	// Keep a reference to the GA this individual belongs to
	private SegmentationGA ga;

	/**
	 * Create an individual from a given genotype
	 * @param ga - The GA this individual belongs to
	 * @param genotype - A genotype, as an array of directions (each pixel has a Direction)
	 */
	public Individual(SegmentationGA ga, Direction[] genotype) {
		this.ga = ga;
		
		ProblemInstance pi = ((ProblemInstance) ga.getProblemInstance());
		
		if(genotype == null) {
			// Initialize the representation
			this.representation = new Direction[pi.getImage().getWidth() * pi.getImage().getHeight()];
		}
		else {	
			this.representation = genotype;
			this.updateSegmentRepresentation();
		}
			
		float alpha = Main.config.getFloat("fitness_alpha"),
			beta = Main.config.getFloat("fitness_beta"),
			gamma = Main.config.getFloat("fitness_gamma");
		
		// Initialize the caches
		edgeValue = new CachedValue<Float>(this::computeEdgeValue);
		connectivity = new CachedValue<Float>(this::computeConnectivity);
		overallDeviation = new CachedValue<Float>(this::computeOverallDeviation);

		fitness = new CachedValue<Float>(() -> {
			return alpha * edgeValue.getValue() + 
				beta * connectivity.getValue() + 
				gamma * overallDeviation.getValue();
		});
		
	}
	
	/**
	 * Create a new Individual
	 * @param ga - The GA this individual belongs to
	 */
	public Individual(SegmentationGA ga) {
		this(ga, null);
	}
	
	/**
	 * Get the edge value from cache
	 * @return the edge value
	 */
	public float getEdgeValue() {
		return edgeValue.getValue();
	}
	
	/**
	 * Get the connectivity from cache
	 * @return the connectivity
	 */
	public float getConnectivity() {
		return connectivity.getValue();
	}
	
	/**
	 * Get the overall deviation from cache
	 * @return the overall deviation
	 */
	public float getOverallDeviation() {
		return overallDeviation.getValue();
	}
	
	@Override
	public float getFitness() {
		return fitness.getValue();
	}

	@Override
	public void mutate() {
		float r = ga.random();
		if(r < 0.15f*0) {
			significantMutation();
		}
		/*else if(r < 0.2f) {
			segmentMergeMutation();
		}*/ 
		else {
			// Mutate on a single position
			int randPos = (int) (ga.random() * this.representation.length);
			
			Direction randDir;
			if(ga.random() < 0.1)
				randDir = Direction.NONE;
			else {
				Direction[] dirs = {Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT};
				int dir = (int) (ga.random() * dirs.length);
				if(representation[randPos] == dirs[dir])
					dir = (dir+1) % dirs.length;
				randDir = dirs[dir];
			}
			this.representation[randPos] = randDir; 
		}
		
		/*Direction randDir = Direction.NONE;
		
		if(ga.random() < 0.4f)
			segmentMergeMutation();
		else {
			if(ga.random() < 0.99f) {
				Direction[] dirs = {Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT};
				do {
					randDir = dirs[(int) (ga.random() * dirs.length)];
				} while(this.representation[randPos] == randDir);
			}
				
			this.representation[randPos] = randDir; 
		}*/
		
		
		// Random chance of mutating each direction
		/*for(int i = 0; i < representation.length; i++) {
			if(ga.random() < 0.02f) {
				Direction randDir = Direction.NONE;
				
				// Very low chance of keeping direction to NONE
				if(ga.random() < 0.99) {	
					// Find a new direction
					Direction[] dirs = {Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT};
					do {
						randDir = dirs[(int) (ga.random() * dirs.length)];
					} while(representation[i] == randDir);
				}
				
				representation[i] = randDir;
			}
		}*/
		
		// Update segment representation and notify that the objective values need to be updated
		updateSegmentRepresentation();
		edgeValue.needsUpdating();
		connectivity.needsUpdating();
		overallDeviation.needsUpdating();
		fitness.needsUpdating();
	}
	
	private void segmentMergeMutation() {
		ProblemInstance pi = ga.getProblemInstance();
		
		// First, find two adjacent segments
		int seg1 = (int) (ga.random() * segments.size());
		int seg2 = -1;
		
		outer: for(int p : segments.get(seg1).getPixels()) {
			List<Integer> neighbors = pi.get4Neighbors(p);
			for(int n : neighbors) {
				if(pixelSegments[n] != seg1) {
					seg2 = pixelSegments[n];
					break outer;
				}
			}
		}
		
		// Unable to find an adjacent segment (should never happen)
		if(seg2 == -1)
			return;
		
		// Merge segment 2 into segment 1
		for(int p : segments.get(seg2).getPixels()) {
			// If pixel p has a neighbor in seg1, point toward it
			List<Integer> neighbors = pi.get4Neighbors(p);
			inner: for(int n : neighbors) {
				if(pixelSegments[n] == seg1) {
					representation[p] = pi.getDirection(p, n);
					break inner;
				}
			}
		}
		
		/*
		int segmentsBefore = segments.size();
		updateSegmentRepresentation();
		int segmentsAfter = segments.size();
		System.out.println("before: " + segmentsBefore + ", after: " + segmentsAfter);*/
	}

	private void significantMutation() {
		HashMap<Integer, Integer> totalChildren = new HashMap<Integer, Integer>();
		List<Integer> remaining = computeSegmentBoundaryPixels();
		for(int i = 0; i < representation.length; i++)
			remaining.add(i);
		
		int i;
		do {
			i = remaining.get((int) (ga.random() * remaining.size()));
		} while(getTotalChildren(i, totalChildren, remaining) < 200);
		
		Direction[] dirs = {Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT};
		int dir = (int) (ga.random() * dirs.length);
		if(representation[i] == dirs[dir])
			dir = (dir+1) % dirs.length;
		representation[i] = dirs[dir];
	}
	
	/**
	 * Get a list of pixels that are at boundary with another segment.
	 * @return a list of pixels
	 */
	private List<Integer> computeSegmentBoundaryPixels() {
		ProblemInstance pi = ga.getProblemInstance();
		int w = pi.getImage().getWidth(), h = pi.getImage().getHeight();
		
		List<Integer> boundaryPixels = new ArrayList<Integer>();
		for(int i = 0; i < pixelSegments.length; i++) {
			// Check if on the very right or very bottom of the image
			boolean right = (i+1) % w == 0, 
					bottom = i >= (h-1)*w;
			// Check for segment edge
			if((!right && pixelSegments[i] != pixelSegments[i+1]) || (!bottom && pixelSegments[i] != pixelSegments[i+w]))
				boundaryPixels.add(i);
		}
		
		return boundaryPixels;
	}
	
	/**
	 * Get a list of pixels tha are on the outer boundary of the image.
	 * @return a list of pixels
	 */
	private List<Integer> computeImageBoundaryPixels() {
		ProblemInstance pi = ga.getProblemInstance();
		int w = pi.getImage().getWidth(), h = pi.getImage().getHeight();
		
		List<Integer> boundaryPixels = new ArrayList<Integer>();
		for(int i = 0; i < pixelSegments.length; i++) {
			// Check for outside border
			boolean left = i % w == 0,
					right = (i+1) % w == 0,
					top = i < w,
					bottom = i >= (h-1)*w;
			if(left || right || top || bottom)
				boundaryPixels.add(i);
		}
		
		return boundaryPixels;
	}
	
	

	private int getTotalChildren(int i, HashMap<Integer, Integer> totalChildren, List<Integer> remaining) {
		if(totalChildren.containsKey(i))
			return totalChildren.get(i);
		remaining.remove((Object) i);
		
		List<Integer> neighbors = ga.getProblemInstance().get4Neighbors(i);
		int pointingToward = 0;
		
		for(Integer n : neighbors) {
			if(!remaining.contains(n))
				continue;
			int j = getPixelIndex(n, representation[n]);
			// If the neighbor is pointing toward us
			if(i == j) {
				pointingToward++;
				pointingToward += getTotalChildren(n, totalChildren, remaining);
			}
		}
		totalChildren.put(i, pointingToward);
		return pointingToward;
	}
	
	@Override
	public IIndividual crossover(IIndividual iparentB) {
		Individual ind = new Individual(ga);
		
		Individual parentB = (Individual) iparentB;
		
		if(ga.random() < 0.5f) {
			// One-point crossover
			int crossoverPoint = (int) (ga.random() * representation.length);
			for(int i = 0; i < crossoverPoint; i++)
				ind.representation[i] = representation[i];
			for(int i = crossoverPoint; i < representation.length; i++)
				ind.representation[i] = parentB.representation[i];
		}
		else {	
			// Uniform crossover
			for(int i = 0; i < ind.representation.length; i++)
				ind.representation[i] = ga.random() < 0.7 ? representation[i] : parentB.representation[i]; 
		}
				
		ind.updateSegmentRepresentation();
		return ind;
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
		return sameSegment(i, j) ? 0 : ga.getProblemInstance().getEuclideanDistance(i, j);
	}

	/**
	 * Compute the edge value for the current segmentation of this individual
	 * @return the edge value
	 */
	public float computeEdgeValue() {
		float edgeValue = 0;
		for(int i = 0 ; i < this.representation.length; i++) {
			for(int n : ga.getProblemInstance().get4Neighbors(i))
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
			List<Integer> neighbors = ga.getProblemInstance().get8Neighbors(i);
			for(int j = 0; j < neighbors.size(); j++) {
				if(!sameSegment(i, neighbors.get(j)))
					connectivity += 1.0f / (j+1);
			}
		}
		return -connectivity;
	}

	/**
	 * Compute overall deviation for the current segmentation of this individual
	 * @return the overall deviation
	 */
	public float computeOverallDeviation() {
		ProblemInstance pi = ga.getProblemInstance();
		
		float overallDeviation = 0;
		
		// For each segment
		for(Segment s : segments) {
			// First compute the centroid
			float[] centroid = s.calculateCentroid(pi);
			
			// Then add up the deviation for the current segment
			for(int i : s.getPixels()) {
				int[] hsb = pi.getRGB(i);
				
				overallDeviation += Math.sqrt(
					Math.pow(hsb[0]-centroid[0], 2) +
					Math.pow(hsb[1]-centroid[1], 2) +
					Math.pow(hsb[2]-centroid[2], 2)
				);
			}
		}
		return -overallDeviation;
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
	 * Compute the segment representation from the directions matrix.
	 */
	public void updateSegmentRepresentation() {
		long time = System.nanoTime();
		currentSegmentIndex = 0;
		
		// First assign each pixel to its segment
		pixelSegments = new int[representation.length];
		Arrays.fill(pixelSegments, -1);
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
		int w = ga.getProblemInstance().getImage().getWidth();
				
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
				if(source >= w * (ga.getProblemInstance().getImage().getHeight()-1))
					return none;
				return source + w;
			default:
				return none;
		}	
	}
	
	/**
	 * Print this individual's current segmentation to the console.
	 */
	public void print() {
		String str = "";
		int w = ga.getProblemInstance().getImage().getWidth();
		for(int i = 0; i < representation.length; i++) {
			str += pixelSegments[i] + " ";
			if((i+1) % w == 0)
				str += "\n";
		}
		System.out.println(str);	
	}
	
	public void printDirectionArray() {
		String str = "";
		int w = ga.getProblemInstance().getImage().getWidth();
		for(int i = 0; i < representation.length; i++) {
			Direction d = representation[i];
			str += (d == Direction.DOWN ? "_" : d == Direction.UP ? "^" : d == Direction.LEFT ? "<" : d == Direction.RIGHT ? ">" : d == Direction.NONE ? "o" : "?");
			str += " ";
			if((i+1) % w == 0)
				str += "\n";
		}
		System.out.println(str);	
	}
	
	/**
	 * Generate the output images for the current segmentation.
	 * @return the output images
	 */
	public BufferedImage[] generateImages() {
		ProblemInstance pi = ga.getProblemInstance();
		int w = pi.getImage().getWidth(), h = pi.getImage().getHeight();
		
		Set<Integer> edgePixels = new TreeSet<Integer>();
		edgePixels.addAll(computeSegmentBoundaryPixels());
		edgePixels.addAll(computeImageBoundaryPixels());
		
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
        BufferedImage bufferedImage2 = new BufferedImage(pi.getOriginalWidth(), pi.getOriginalHeight(), BufferedImage.TYPE_INT_RGB);
        //BufferedImage bufferedImage2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        g = bufferedImage2.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, bufferedImage2.getWidth(), bufferedImage2.getHeight());
        g.setColor(Color.BLACK);
        float r = 1.0f / pi.getImageScaling();
        for(int i : edgePixels) {
        	int[] pos = pi.pixelIndexToPos(i);
            g.fillRect((int) (pos[0]*r), (int) (pos[1]*r), 1, 1);
            // g.fillRect(pos[0], pos[1], 1, 1);
        }
        g.dispose();
        
        // Scale the second image back up
        //bufferedImage2 = ImageUtils.resizeImage(bufferedImage2, pi.getOriginalWidth(), pi.getOriginalHeight());*/
        
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
	
	/**
	 * Get the segmentation as a list of segments.
	 * @return a list of segments
	 */
	public List<Segment> getSegments() {
		return segments;
	}

	/**
	 * Get the segmentation as a pixel matrix.
	 * @return a pixel matrix indicating which segment each pixel belongs to
	 */
	public int[] getPixelSegments() {
		return pixelSegments;
	}
}
