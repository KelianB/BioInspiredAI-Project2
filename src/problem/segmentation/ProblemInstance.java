package problem.segmentation;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import ga.segmentation.Individual.Direction;
import problem.IProblemInstance;
import utils.ImageUtils;
import utils.WeightedGraph;

/**
 * Represents an image segmentation problem instance.
 * @author Kelian Baert & Caroline de Pourtales
 */
public class ProblemInstance implements IProblemInstance {
	public static enum ColorMode {RGB, HSB};
	
	// The problem instance's image
	private BufferedImage image;
	
	// Name of the problem instance
	private String name;
	
	// Original size of the image, prior to scaling
	private int originalWidth, originalHeight;
	
	// Scaling factor
	private float imageScaling;
	
	// Store the RGB of each pixel as a matrix of [r, g, b] integer arrays
	private float[][][] rgb;
	
	// Store the HSB (hue, saturation, brightness) of each pixel as a matrix of [h, s, b] integer arrays
	private float[][][] hsb;
	
	// The color mode (HSB or RGB)
	private ColorMode colorMode;
	
	// A graph in which each pixel is connected to its cardinal neighbors with weights equal to the euclidean distances in HSB space
	private WeightedGraph euclideanDistanceGraph;
	
	// Cache pixel neighbors to save time
	private List<List<Integer>> pixel4NeighborsCache;
	private List<List<Integer>> pixel8NeighborsCache;
	
	/**
	 * Create a new problem instance
	 * @param name - The name of this problem instance
	 * @param originalImage - An image
	 * @param colorMode - The color mode (either ColorMode.HSB or ColorMode.RGB)
	 * @param imageScaling - A ratio by which to scale the input image
	 */
	public ProblemInstance(BufferedImage originalImage, ColorMode colorMode, float imageScaling) {
		this.name = "img";
		this.originalWidth = originalImage.getWidth();
		this.originalHeight = originalImage.getHeight();
		this.image = imageScaling == 1 ? originalImage : ImageUtils.scaleImage(originalImage, imageScaling);
		this.colorMode = colorMode;
		this.imageScaling = imageScaling;
		
		int w = image.getWidth(), h = image.getHeight();
		
		// Store the RGB and HSB of each pixel in three-dimensional arrays of shape (width, height, 3)
		rgb = new float[w][h][3];
		hsb = new float[w][h][3];
		for(int x = 0; x < w; x++) {
			for(int y = 0; y < h; y++) {
				Color c = new Color(getImage().getRGB(x, y));
				rgb[x][y][0] = c.getRed() / 255.0f;
				rgb[x][y][1] = c.getGreen() / 255.0f;
				rgb[x][y][2] = c.getBlue() / 255.0f;
				hsb[x][y] = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
			}	
		}
		
		// Initialize the neighbor caches
		pixel4NeighborsCache = new ArrayList<List<Integer>>();
		for(int i = 0; i < w*h; i++)
			pixel4NeighborsCache.add(compute4Neighbors(i));
		
		pixel8NeighborsCache = new ArrayList<List<Integer>>();
		for(int i = 0; i < w*h; i++)
			pixel8NeighborsCache.add(compute8Neighbors(i));
				
		// Create a graph in which each pixel is connected to its 4 cardinal neighbours.
		// The weight of the edges are given by the euclidean distance in HSB color space
		euclideanDistanceGraph = new WeightedGraph(w*h);
		for(int x = 0; x < w; x++) {
			for(int y = 0; y < h; y++) {
				int i = y*w+x; // position of current pixel in flattened coordinates
				for(int neighbour : get4Neighbors(i))
					euclideanDistanceGraph.addConnection(i, neighbour, getEuclideanDistance(i, neighbour));
			}	
		}
		
	}
	
	/**
	 * Get the image of this problem instance.
	 * @return the image
	 */
	public BufferedImage getImage() {
		return image;
	}
	
	/**
	 * Get the color value (either RGB or HSB depending on the color mode) at a given position of the image.
	 * @param x - A horizontal position
	 * @param y - A vertical position
	 * @return the color value at position (x, y), as a [r,g,b] or [h,s,b] float array
	 */
	private float[] getColorValue(int x, int y) {
		return colorMode == ColorMode.RGB ? rgb[x][y] : hsb[x][y];
	}
	
	/**
	 * Get the color value (either RGB or HSB depending on the color mode) of a given pixel index in the image.
	 * @param i - A pixel index (between 0 and width*height)
	 * @return the color values at pixel index i, as a [r,g,b] or [h,s,b] float array
	 */
	public float[] getColorValue(int i) {
		int[] pos = pixelIndexToPos(i);
		return getColorValue(pos[0], pos[1]);
	}
	
	/**
	 * Get the scaling factor used for the image
	 * @return the scaling factor
	 */
	public float getImageScaling() {
		return imageScaling;
	}
	
	/**
	 * Get the original width of the image (prior to scaling)
	 * @return the original width
	 */
	public int getOriginalWidth() {
		return originalWidth;
	}
	
	/**
	 * Get the original height of the image (prior to scaling)
	 * @return the original height
	 */
	public int getOriginalHeight() {
		return originalHeight;
	}
	
	/**
	 * Project a 1D pixel index into a 2D position
	 * @param index - A pixel index
	 * @return the corresponding position, as a [x, y] int array
	 */
	public int[] pixelIndexToPos(int index) {
		int x = index % getImage().getWidth();
		int y = (index  - x) / getImage().getWidth();
		return new int[] {x, y};
	}
	
	/** Get the direction to go from a pixel to a given adjacent one.
	 * @param i - The source pixel index
	 * @param j - The destination pixel index
	 * @return the direction to go from a pixel to a given adjacent one (e.g. from i=0 to j=1 direction is RIGHT), 
	 * or Direction.NONE if the given pixels aren't adjacent
	 */
	public Direction getDirection(int i, int j) {
		int[] pos1 = pixelIndexToPos(i);
		int[] pos2 = pixelIndexToPos(j);
		return getDirection(pos1[0], pos1[1], pos2[0], pos2[1]);
	}
	
	/**
	 * Get the direction to go from a pixel to a given adjacent one.
	 * @param xfrom - The source x position
	 * @param yfrom - The source y position
	 * @param xto - The destination x position
	 * @param yto - The destination y position
	 * @return the direction to go from a pixel to a given adjacent one (e.g. from x=0,y=0 to x=1,y=0, direction is RIGHT),
	 * or Direction.NONE if given pixels aren't adjacent
	 */
	public Direction getDirection(int xfrom, int yfrom, int xto, int yto) {
		if(yfrom == yto) {
			if(xto == xfrom + 1)
				return Direction.RIGHT;
			else if(xto == xfrom - 1)
				return Direction.LEFT;
		}
		else if(xfrom == xto) {
			if(yto == yfrom + 1)
				return Direction.DOWN;
			else if(yto == yfrom - 1)
				return Direction.UP;
		}
		return Direction.NONE;
	}
	
	/**
	 * Calculates the euclidean distance between two one-dimensional float arrays of same length.
	 * @param arr1 - An array of floats
	 * @param arr2 - Another array of floats
	 * @return the euclidean distance between the two arrays.
	 */
	public static float euclideanDistance(float[] arr1, float[] arr2) {
		float sumOfSquares = 0.0f;
		for(int i = 0; i < arr1.length; i++)
			sumOfSquares += Math.pow(arr1[i] - arr2[i], 2);
		return (float) Math.sqrt(sumOfSquares);
	}
	
	/**
	 * Get the euclidean distance graph for this problem instance.
	 * @return a graph in which each pixel is connected to its cardinal neighbors with weights equal to the euclidean distances in HSB space
	 */
	public WeightedGraph getEuclideanDistanceGraph() {
		return euclideanDistanceGraph;
	}

	/**
	 * Get the euclidean distance between two pixels, in HSB color space.
	 * @param i - A pixel index
	 * @param j - Another pixel index
	 * @return the euclidean distance
	 */
	public float getEuclideanDistance(int i, int j) {
		return euclideanDistance(getColorValue(i), getColorValue(j));
	}

	/**
	 * Get the indices of a given pixel's 4 cardinal neighbors.
	 * @param i - A pixel index
	 * @return a list of neighbors indices
	 */
	public List<Integer> get4Neighbors(int i) {
		return pixel4NeighborsCache.get(i);
	}
	
	/**
	 * Get the indices of a given pixel's 8 neighbors.
	 * @param i - A pixel index
	 * @return a list of neighbors indices
	 */
	public List<Integer> get8Neighbors(int i) {
		return pixel8NeighborsCache.get(i);
	}
	
	/**
	 * Compute the indices of a given pixel's 4 cardinal neighbors.
	 * @param i - A pixel index
	 * @return a list of neighbors indices
	 */
	private List<Integer> compute4Neighbors(int i) {
		int w = getImage().getWidth(), h = getImage().getHeight();
		
		// Check if the pixel is on one of the outside borders on the image		
		boolean left = i % w == 0,
				right = (i+1) % w == 0,
				top = i < w,
				bottom = i >= (h-1)*w;
	
		List<Integer> neighbors = new ArrayList<>(); 
		if(!right) neighbors.add(i+1);
		if(!left) neighbors.add(i-1);
		if(!top) neighbors.add(i-w);
		if(!bottom) neighbors.add(i+w);

		return neighbors;
	}
	
	/**
	 * Compute the indices of a given pixel's 8 neighbors.
	 * @param i - A pixel index
	 * @return a list of neighbors indices
	 */
	private List<Integer> compute8Neighbors(int i) {
		int w = getImage().getWidth(), h = getImage().getHeight();
		
		// Check if the pixel is on one of the outside borders on the image		
		boolean left = i % w == 0,
				right = (i+1) % w == 0,
				top = i < w,
				bottom = i >= (h-1)*w;
	
		List<Integer> neighbors = new ArrayList<>(); 
		if(!right) neighbors.add(i+1);
		if(!left) neighbors.add(i-1);
		if(!top) neighbors.add(i-w);
		if(!bottom) neighbors.add(i+w);
		if(!right) {
			if(!top) neighbors.add(i-w+1);
			if(!bottom) neighbors.add(i+w+1);
		}
		if(!left) {
			if(!top) neighbors.add(i-w-1);
			if(!bottom) neighbors.add(i+w-1);
		}
				
		return neighbors;
	}

	@Override
	public String getName() {
		return name;
	}
}
