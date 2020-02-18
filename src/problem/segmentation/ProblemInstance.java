package problem.segmentation;

import java.awt.image.BufferedImage;

import problem.IProblemInstance;

public class ProblemInstance implements IProblemInstance {
	private BufferedImage image;
	
	public ProblemInstance(BufferedImage image) {
		this.image = image;
	}
	
	public BufferedImage getImage() {
		return image;
	}
}
