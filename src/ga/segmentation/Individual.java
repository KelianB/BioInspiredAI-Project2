package ga.segmentation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ga.GeneticAlgorithm;
import ga.IIndividual;
import problem.segmentation.ProblemInstance;
import utils.CachedValue;
import utils.ImageUtils;

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
	private GeneticAlgorithm ga;

	public Individual(GeneticAlgorithm ga, Direction[] genotype) {
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
			
		float alpha = 1.0f,
			  beta = 1.0f,
			  gamma = 1.0f;
		
		// Initialize the caches
		edgeValue = new CachedValue<Float>(this::computeEdgeValue);
		connectivity = new CachedValue<Float>(this::computeConnectivity);
		overallDeviation = new CachedValue<Float>(this::computeOverallDeviation);

		fitness = new CachedValue<Float>(() -> {
			Population pop = (Population) ga.getPopulation();
			return alpha * pop.normalizeEdgeValue(edgeValue.getValue()) + 
				beta * pop.normalizeConnectivity(connectivity.getValue()) + 
				gamma * pop.normalizeOverallDeviation(overallDeviation.getValue());
		});
		
	}
	
	public Individual(GeneticAlgorithm ga) {
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
		// Mutate on a single position
		int randPos = (int) (ga.random() * this.representation.length);
		
		Direction randDir = Direction.NONE;
		
		if(ga.random() > 0.99) {
			Direction[] dirs = {Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT};
			do {
				randDir = dirs[(int) (ga.random() * dirs.length)];
			} while(this.representation[randPos] == randDir);
		}
			
		this.representation[randPos] = randDir; 
		
		
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

	@Override
	public IIndividual crossover(IIndividual iparentB) {
		Individual ind = new Individual(ga);
		
		Individual parentB = (Individual) iparentB;
		
		// One-point crossover
		/*
		int crossoverPoint = (int) (ga.random() * representation.length);
		for(int i = 0; i < crossoverPoint; i++)
			ind.representation[i] = representation[i];
		for(int i = crossoverPoint; i < representation.length; i++)
			ind.representation[i] = parentB.representation[i];
		*/
		
		// Uniform crossover
		for(int i = 0; i < ind.representation.length; i++)
			ind.representation[i] = ga.random() < 0.5 ? representation[i] : parentB.representation[i]; 
				
		ind.updateSegmentRepresentation();
		return ind;
	}	

	/**
	 * Get the indices of a given pixel's cardinal neighbors.
	 * @param i - A pixel index
	 * @return a list of neighbors indices
	 */
	public List<Integer> getNeighbors(int i) {
		// TODO Cache neighbors?
		/*Stream<Direction> dirs = Arrays.stream(Direction.values());
		Stream<Integer> neighbors = dirs.map((d) -> getPixelIndex(i, d)).filter(a -> a != -1);
		return neighbors.collect(Collectors.toList());*/
		
		List<Integer> neighbors = new ArrayList<>(); 
		for(Direction d : Direction.values()) {
			int pixel = getPixelIndex(i, d);
			if(pixel != -1)
				neighbors.add(pixel);
		}
		return neighbors;
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
		return sameSegment(i, j) ? 0 : ((ProblemInstance) ga.getProblemInstance()).getEuclideanDistance(i, j);
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
		return -connectivity;
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
				float[] hsb = pi.getHSB(i);
				
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
		boolean outsideBorder = true;
		
		ProblemInstance pi = ((ProblemInstance) ga.getProblemInstance());
		int w = pi.getImage().getWidth(), h = pi.getImage().getHeight();
		
		List<Integer> edgePixels = new ArrayList<Integer>();
		for(int i = 0; i < pixelSegments.length; i++) {
			// Check for outside border
			boolean left = i % w == 0,
					right = (i+1) % w == 0,
					top = i < w,
					bottom = i > (h-1)*w;
			boolean b = outsideBorder && (left || right || top || bottom);
			// Check for segment edge
			b = b || (!right && (pixelSegments[i] != pixelSegments[i+1] || (!bottom && pixelSegments[i] != pixelSegments[i+w])));

			if(b)
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
        
        // Scale the second image back up
        bufferedImage2 = ImageUtils.resizeImage(bufferedImage2, pi.getOriginalWidth(), pi.getOriginalHeight());
        
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
