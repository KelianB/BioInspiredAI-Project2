package problem.segmentation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import problem.IProblemReader;
import problem.ProblemReadingException;

public class ProblemReader implements IProblemReader {
	private String baseDir;
	private float imageScaling;
	
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
		
		return new ProblemInstance(img, imageScaling);
	}
}
