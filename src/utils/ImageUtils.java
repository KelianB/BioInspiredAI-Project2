package utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import ga.segmentation.Individual;
import ga.segmentation.ProblemInstance;
import main.Main;

/**
 * Contains various utility function that could have a use in any project.
 * @author Kelian Baert & Caroline de Pourtales
 */
public class ImageUtils {
	private static enum UpscalingMethod {NONE, DOTTED, INTERPOLATE_BICUBIC, INTERPOLATE_NEAREST_NEIGHBOR};
	
	/**
	 * Scales a given image at a given ratio.
	 * @param img - A buffered image
	 * @param scale - A scaling ratio
	 * @param interpolation - The interpolation method used for resizing (@see java.awt.RenderingHints)
	 * @return the scaled image
	 */
	public static BufferedImage scaleImage(BufferedImage img, float scale, Object interpolation) {
		return resizeImage(img, (int) (img.getWidth() * scale), (int) (img.getHeight() * scale), interpolation);
	}
	
	/**
	 * Scales a given image at a given ratio.
	 * @param img - A buffered image
	 * @param scale - A scaling ratio
	 * @return the scaled image
	 */
	public static BufferedImage scaleImage(BufferedImage img, float scale) {
		return scaleImage(img, scale, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	}
	
	/**
	 * Resizes a given image to a given size.
	 * @param img - A buffered image
	 * @param width - The target width
	 * @param height - The target height
	 * @param interpolation - The interpolation method used for resizing (@see java.awt.RenderingHints)
	 * @return the scaled image
	 */
	public static BufferedImage resizeImage(BufferedImage img, int width, int height, Object interpolation) {
		BufferedImage resized = new BufferedImage(width, height, img.getType());
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation);
		g.drawImage(img, 0, 0, resized.getWidth(), resized.getHeight(), 0, 0, img.getWidth(), img.getHeight(), null);
		g.dispose();
		return resized;
	}
	
	/**
	 * Generate the output images for a given individual.
	 * @return the output images (first: green edges over original image, second: black edges over white background)
	 */
	public static BufferedImage[] generateImages(ProblemInstance pi, Individual ind) {
		int w = pi.getImage().getWidth(), h = pi.getImage().getHeight();
		
		List<Integer> segmentEdgePixels = ind.computeSegmentBoundaryPixels();
		
		// First output image (overlayed green edges)
		BufferedImage bufferedImage1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufferedImage1.createGraphics();
        g.drawImage(pi.getImage(), 0, 0, null);
        g.setColor(new Color(0, 255, 0));
        for(int i : segmentEdgePixels) {
        	int[] pos = pi.pixelIndexToPos(i);
            g.fillRect(pos[0], pos[1], 1, 1);
        }
        g.drawRect(0, 0, bufferedImage1.getWidth()-1, bufferedImage1.getHeight()-1);
        g.dispose();

        // Second output image (only black edges)
        UpscalingMethod upscaling = UpscalingMethod.valueOf(Main.config.get("upscaling"));
        BufferedImage bufferedImage2 = new BufferedImage(
        		upscaling == UpscalingMethod.DOTTED ? pi.getOriginalWidth() : w, 
        		upscaling == UpscalingMethod.DOTTED ? pi.getOriginalHeight() : h, 
        		BufferedImage.TYPE_INT_RGB
        );
        
        g = bufferedImage2.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, bufferedImage2.getWidth(), bufferedImage2.getHeight());
        g.setColor(Color.BLACK);
        
        if(upscaling == UpscalingMethod.DOTTED) {
        	// Translate the pixel positions by the inverse of the scaling ratio
	        float r = 1.0f / pi.getImageScaling();
	        for(int i : segmentEdgePixels) {
	        	int[] pos = pi.pixelIndexToPos(i);
	        	g.fillRect((int) (pos[0]*r), (int) (pos[1]*r), 1, 1);
	        }
        }
        else {
	        for(int i : segmentEdgePixels) {
	        	int[] pos = pi.pixelIndexToPos(i);
	        	g.fillRect(pos[0], pos[1], 1, 1);
	        }
	        
        	if(upscaling == UpscalingMethod.INTERPOLATE_BICUBIC)
        		bufferedImage2 = ImageUtils.resizeImage(bufferedImage2, pi.getOriginalWidth(), pi.getOriginalHeight(), RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        	else if(upscaling == UpscalingMethod.INTERPOLATE_NEAREST_NEIGHBOR)
        		bufferedImage2 = ImageUtils.resizeImage(bufferedImage2, pi.getOriginalWidth(), pi.getOriginalHeight(), RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        
        	g = bufferedImage2.createGraphics();
        }
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, bufferedImage2.getWidth()-1, bufferedImage2.getHeight()-1);
        g.dispose();
        
        return new BufferedImage[] {bufferedImage1, bufferedImage2};
	}
}
