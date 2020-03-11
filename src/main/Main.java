package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ga.segmentation.Individual;
import ga.segmentation.SegmentationGA;
import ga.segmentation.multiobjective.MultiObjectivePopulation;
import ga.segmentation.multiobjective.MultiObjectiveSegmentationGA;
import problem.segmentation.ProblemInstance;
import problem.segmentation.ProblemInstance.ColorMode;
import problem.segmentation.ProblemReader;
import utils.ImageUtils;

/**
 * Entry point
 * @author Kelian Baert & Caroline de Pourtales
 */
public class Main {
	public static enum Mode {WEIGHTED_SUM_GA, MOEA};
	public static Mode mode;
	
	public static Config config;
	
	private static boolean clearedOutputDirs = false;
	
	public static void main(String[] args) {
		// Read configuration file
		config = new Config("config.properties");
		
		// Create a problem reader
		ProblemReader reader = new ProblemReader(ColorMode.valueOf(config.get("colorMode")), config.getFloat("imageScaling"));
		
		// Read a problem instance
		String inputImagePath = config.get("inputImage");
		final ProblemInstance instance = reader.readProblem(inputImagePath);
		
		// Abort if the problem instance couldn't be read
		if(instance == null) {
			System.err.println("[Critical Error] Couldn't read problem instance.");
			System.exit(1);
		}
		
		// Print information about the problem instance
		System.out.println("Problem instance " + inputImagePath);
		System.out.println("Resized image size from " + instance.getOriginalWidth() + "x" + instance.getOriginalHeight() + 
				" to " + instance.getImage().getWidth() + "x" + instance.getImage().getHeight());
		
		// Get the mode from the config (weighted sum or MOEA)
		mode = Mode.valueOf(config.get("mode"));
		
		// Init GA
		SegmentationGA sga =
			mode == Mode.WEIGHTED_SUM_GA ? new SegmentationGA(instance, config.getFloat("mutationRate"), config.getFloat("crossoverRate")) :
			mode == Mode.MOEA ? new MultiObjectiveSegmentationGA(instance, config.getFloat("mutationRate"), config.getFloat("crossoverRate")) :
			null;
			
		if(sga == null) {
			System.err.println("[Critical Error] Couldn't parse GA mode.");
			System.exit(1);
		}
		
		sga.setElites(config.getInt("elites"));
		sga.initializePopulation();
		
		// on weighted-sum GA termination: save fittest
		Runnable onTerminationGA = () ->  {
			System.out.println("Saving fittest");
	        saveImages(instance, ((Individual) sga.getPopulation().getFittestIndividual()));			
		};
		
		// on MOEA termination: save first front
		Runnable onTerminationMOEA = () ->  {
			System.out.println("Saving first front");
			for(Individual i : ((MultiObjectivePopulation) sga.getPopulation()).getFirstFront())
				saveImages(instance, i);
		};
		
		Runnable onFinish = sga instanceof MultiObjectiveSegmentationGA ? onTerminationMOEA : onTerminationGA;
		
		// Define the shutdown hook to execute on termination
		Runtime.getRuntime().addShutdownHook(new Thread(onFinish));
		
		for(int i = 0; i < config.getInt("generations"); i++) {
			long time = System.nanoTime();
			System.out.println("---------- Running generation #" + i + " ----------");
			sga.runGeneration();
			sga.printState();
			System.out.println("(" + (System.nanoTime() - time) / 1000000 + " ms)");
		}
	}
	
	/**
	 * Saves an individual's segmentation images
	 * @param pi - A problem instance
	 * @param ind - An individual
	 */
	private static void saveImages(ProblemInstance pi, Individual ind) {
		ind.updateSegmentRepresentation();
		
		int numSegments = ind.getSegments().size();
		
		BufferedImage[] images = ImageUtils.generateImages(pi, ind);
	    try {
	    	if(!clearedOutputDirs) {
	    		clearDirectory(new File(config.get("outputDir")));
	    		clearDirectory(new File(config.get("evaluationDir")));
	    		clearedOutputDirs = true;
	    	}
	    	
	    	// Save both images in the output directory
	    	for(int i = 0; i < images.length; i++) {
	    		File file = new File(config.get("outputDir") + "segmentation_" + numSegments + "_" + (i+1) + ".png");
	    		file.mkdirs();
	    		ImageIO.write(images[i], "png", file);
	    	}
	    	
	    	// Also save the image for evaluation
	    	File evalFile = new File(config.get("evaluationDir") + "segmentation_" + numSegments + ".png");
	    	evalFile.mkdir();
	    	ImageIO.write(images[1], "png", evalFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Removes all files in a given directory
	 * @param dir - A directory
	 */
	private static void clearDirectory(File dir) {
	    File[] files = dir.listFiles();
	    if(files != null) { // some JVMs return null for empty directories
	        for(File f: files)
	            f.delete();
	    }
	}
}
