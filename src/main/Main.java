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
import problem.segmentation.ProblemReader;

/**
 * Entry point
 * @author Kelian Baert & Caroline de Pourtales
 */
public class Main {
	private static final String PROBLEMS_DIR = "../../Training/";
	public static Config config;
	
	public static void main(String[] args) {
		// Read configuration file
		config = new Config("config.properties");
		
		// Create a problem reader
		ProblemReader reader = new ProblemReader(PROBLEMS_DIR, config.getFloat("imageScaling"));
		
		// Read a problem instance
		final ProblemInstance instance = reader.readProblem("118035");
		
		// Abort if the problem instance couldn't be read
		if(instance == null)
			System.exit(1);
		
		// Print information about the problem instance
		System.out.println("Problem instance " + instance.getName());
		System.out.println("Resized image size from " + instance.getOriginalWidth() + "x" + instance.getOriginalHeight() + 
				" to " + instance.getImage().getWidth() + "x" + instance.getImage().getHeight());
		
		// Init GA
		SegmentationGA sga = new MultiObjectiveSegmentationGA(instance, config.getFloat("mutationRate"), config.getFloat("crossoverRate"));
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
		
        
		// TESTING 
		
		/*System.out.println("Creating a random individual:");
		Individual ind = Individual.createRandomIndividual(sga, instance);
		ind.getFitness();
		//ind.print();
		System.out.println("Number of segments: " + ind.getSegments().size());
		*/
		
		
		/*File file = new File(PROBLEMS_DIR + "debug_image.png");
		BufferedImage img;
		try {
			img = ImageIO.read(file);
			SegmentationGeneticAlgorithm testSga = new SegmentationGeneticAlgorithm(new ProblemInstance(img, 1));
			
			System.out.println("Creating a random individual:");
			Individual ind = Individual.createRandomIndividual(testSga);
			ind.print();
			System.out.println("Number of segments: " + ind.getSegments().size());
		}
		catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	
	private static void saveImages(ProblemInstance pi, Individual ind) {
		ind.updateSegmentRepresentation();
		
		int numSegments = ind.getSegments().size();
		
		BufferedImage[] images = ind.generateImages();
	    try {
	    	// Save both images in the output directory
	    	for(int i = 0; i < images.length; i++) {
	    		File file = new File(config.get("outputDir") + pi.getName() + "/segmentation_" + numSegments + "_" + (i+1) + ".png");
	    		file.mkdirs();
	    		ImageIO.write(images[i], "png", file);
	    	}
	    	
	    	// Also save the image for evaluation
	    	File evalFile = new File(config.get("evaluationDir") + "/segmentation_" + pi.getName() + "_" + numSegments + ".png");
	    	evalFile.mkdir();
	    	ImageIO.write(images[1], "png", evalFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
