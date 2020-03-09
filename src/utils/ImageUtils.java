package utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Contains various utility function that could have a use in any project.
 * @author Kelian Baert & Caroline de Pourtales
 */
public class ImageUtils {
	/**
	 * Scales a given image at a given ratio.
	 * @param img - A buffered image
	 * @param scale - A scaling ratio
	 * @return the scaled image
	 */
	public static BufferedImage scaleImage(BufferedImage img, float scale) {
		return resizeImage(img, (int) (img.getWidth() * scale), (int) (img.getHeight() * scale));
	}
	
	/**
	 * Resizes a given image to a given size.
	 * @param img - A buffered image
	 * @param width - The target width
	 * @param height - The target height
	 * @return the scaled image
	 */
	public static BufferedImage resizeImage(BufferedImage img, int width, int height) {
		BufferedImage resized = new BufferedImage(width, height, img.getType());
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.drawImage(img, 0, 0, resized.getWidth(), resized.getHeight(), 0, 0, img.getWidth(), img.getHeight(), null);
		g.dispose();
		return resized;
	}
}
