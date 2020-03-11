package ga.segmentation;

import java.util.ArrayList;
import java.util.List;

import problem.segmentation.ProblemInstance;

/**
 * Represents a segment in the context of image segmentation.
 * @author Kelian Baert & Caroline de Pourtales
 */
public class Segment {
	// Store the pixels that belong to this segment.
    private List<Integer> pixels;

    /**
     * Create a new empty segment.
     */
    public Segment() {
        this.pixels = new ArrayList<Integer>();
    }
    
    /**
     * Add a pixel to this segment.
     * @param i - A pixel index
     */
    public void addPixel(int i) {
    	this.pixels.add(i);
    }

    /**
     * Get the pixels of this segment.
     * @return the indices of the pixels that belong to this segment
     */
    public List<Integer> getPixels() {
        return this.pixels;
    }
    
    /**
     * Calculate the centroid of this segment (average of the RGB of each pixel in this segment).
     * @param pi - The problem instance
     * @return the centroid as a [r, g, b] array
     */
    public float[] calculateCentroid(ProblemInstance pi) {
    	float[] centroid = new float[3];
    	
    	List<Integer> pixels = getPixels();
    	float numPixels = (float) pixels.size();
		for(int i : pixels) {
			float[] color = pi.getColorValue(i);
			centroid[0] += color[0] / numPixels;
			centroid[1] += color[1] / numPixels;
			centroid[2] += color[2] / numPixels;
		}
		
		return centroid;
    }
}
