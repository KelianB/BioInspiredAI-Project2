package problem.segmentation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import ga.segmentation.Individual.Direction;
import problem.IProblemInstance;
import utils.WeightedGraph;

public class ProblemInstance implements IProblemInstance {
	private BufferedImage image;
	
	private int[][][] rgb;
	private WeightedGraph euclideanDistanceGraph;
	
	public ProblemInstance(BufferedImage originalImage, float imageScaling) {
		this.image = imageScaling == 1 ? originalImage : scaleImage(originalImage, imageScaling);
		
		int w = image.getWidth(), h = image.getHeight();
		
		// Store the RGB of each pixel in a width x height x 3 array
		rgb = new int[w][h][3];
		for(int x = 0; x < w; x++) {
			for(int y = 0; y < h; y++) {
				Color c = new Color(getImage().getRGB(x, y));
				rgb[x][y][0] = c.getRed();
				rgb[x][y][1] = c.getGreen();
				rgb[x][y][2] = c.getBlue();
			}	
		}
		
		// Create a graph where the weight of each edge is connected to its 4 cardinal neighbours.
		// The weight of the edges are given by the Euclidean distance in RGB color space
		euclideanDistanceGraph = new WeightedGraph(image.getWidth()*image.getHeight());
		for(int x = 0; x < w; x++) {
			for(int y = 0; y < h; y++) {
				List<int[]> cardinalNeighbours = new ArrayList<int[]>();
				if(x > 0)   cardinalNeighbours.add(new int[] {x-1, y});
				if(x < w-1) cardinalNeighbours.add(new int[] {x+1, y});
				if(y > 0)   cardinalNeighbours.add(new int[] {x, y-1});
				if(y < h-1) cardinalNeighbours.add(new int[] {x, y+1});
					
				for(int i = 0; i < cardinalNeighbours.size(); i++) {
					int x2 = cardinalNeighbours.get(i)[0], y2 = cardinalNeighbours.get(i)[1];
					euclideanDistanceGraph.addConnection(
						y*w+x, // position of current pixel in flattened coordinates
						y2*w+x2, // position of neighbour in flattened coordinates
						euclideanDistance(getRGB(x, y), getRGB(x2, y2))
					);
				}
			}	
		}
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public int[] getRGB(int x, int y) {
		return rgb[x][y];
	}
	
	public int[] pixelIndexToPos(int index) {
		int x = index % getImage().getWidth();
		int y = (index  - x) / getImage().getWidth();
		return new int[] {x, y};
	}
	
	/**
	 * Get the direction to go from a pixel to a given adjacent one.
	 * @param xfrom - The source x position
	 * @param yfrom - The source y position
	 * @param xto - The destination x position
	 * @param yto - The destination y position
	 * @return the direction to go from a pixel to a given adjacent one (e.g. from x=0,y=0 to x=1,y=0, direction is RIGHT), or Direction.NONE if 
	 * given pixels aren't adjacent
	 */
	public Direction getDirection(int xfrom, int yfrom, int xto, int yto) {
		if(yfrom == yto) {
			if(xto == xfrom + 1)
				return Direction.RIGHT;
			else if(xto == xfrom - 1)
				return Direction.LEFT;
		}
		else if(xto == xfrom) {
			if(yto == yfrom + 1)
				return Direction.DOWN;
			else if(yto == yfrom - 1)
				return Direction.UP;
		}
		return Direction.NONE;
	}
	
	public WeightedGraph getEuclideanDistanceGraph() {
		return euclideanDistanceGraph;
	}
	
	/**
	 * Calculates the euclidean distance between two RGB arrays.
	 * @param rgb1 - A [red, green blue] array
	 * @param rgb2 - Another [red, green blue] array
	 * @return the euclidean distance between the two RGBs.
	 */
	private static float euclideanDistance(int[] rgb1, int[] rgb2) {
		float sumOfSquares = 0.0f;
		for(int i = 0; i < rgb1.length; i++) {
			sumOfSquares += Math.pow(rgb1[i] - rgb2[i], 2);
		}
		return (float) Math.sqrt(sumOfSquares);
	}
	
	private static BufferedImage scaleImage(BufferedImage img, float scale) {
		BufferedImage resized = new BufferedImage((int) (img.getWidth() * scale), (int) (img.getHeight() * scale), img.getType());
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, 0, 0, resized.getWidth(), resized.getHeight(), 0, 0, img.getWidth(), img.getHeight(), null);
		g.dispose();
		return resized;
		
	}
	
}
