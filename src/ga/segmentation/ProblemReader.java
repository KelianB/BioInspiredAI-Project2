package ga.segmentation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ga.IProblemReader;
import ga.segmentation.ProblemInstance.ColorMode;

/**
 * Handles reading image segmentation problem instances
 * @author Kelian Baert & Caroline de Pourtales
 */
public class ProblemReader implements IProblemReader {
	// The scaling factor by which all instance images are scaled
	private float imageScaling;
	
	// The color mode value to pass to new problem instances
	private ColorMode colorMode;
	
	/**
	 * 
	 * @param imageScaling - The scaling factor by which all instance images will be scaled
	 */
	public ProblemReader(ColorMode colorMode, float imageScaling) {
		this.colorMode = colorMode;
		this.imageScaling = imageScaling;
	}
	
	@Override
	public ProblemInstance readProblem(String imagePath) {
		File file = new File(imagePath);
		
		BufferedImage img = null;
		try {
			img = ImageIO.read(file);
		}
		catch (IOException e) {
			System.err.println("Exception raised while reading problem instance:");
			e.printStackTrace();
		}
		
		return img == null ? null : new ProblemInstance(img, colorMode, imageScaling);
	}
}
