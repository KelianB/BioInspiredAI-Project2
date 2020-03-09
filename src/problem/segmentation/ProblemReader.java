package problem.segmentation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import problem.IProblemReader;
import problem.ProblemReadingException;

/**
 * Handles reading image segmentation problem instances
 * @author Kelian Baert & Caroline de Pourtales
 */
public class ProblemReader implements IProblemReader {
	// The base directory where problems are stored
	private String baseDir;
	
	// The scaling factor by which all instance images are scaled
	private float imageScaling;
	
	/**
	 * 
	 * @param baseDir - The base directory where problems are stored
	 * @param imageScaling - The scaling factor by which all instance images will be scaled
	 */
	public ProblemReader(String baseDir, float imageScaling) {
		this.baseDir = baseDir;
		this.imageScaling = imageScaling;
	}
	
	@Override
	public ProblemInstance readProblem(String problemName) throws ProblemReadingException {
		File file = new File(baseDir + problemName + "/Test image.jpg");
		
		BufferedImage img;
		try {
			img = ImageIO.read(file);
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new ProblemReadingException();
		}
		
		return new ProblemInstance(problemName, img, imageScaling);
	}
}
