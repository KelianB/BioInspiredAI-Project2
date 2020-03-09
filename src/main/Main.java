package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ga.segmentation.Individual;
import ga.segmentation.SegmentationGeneticAlgorithm;
import problem.ProblemReadingException;
import problem.segmentation.ProblemInstance;
import problem.segmentation.ProblemReader;

/**
 * Entry point
 * @author Kelian Baert & Caroline de Pourtales
 */
public class Main {
	public static final String PROBLEMS_DIR = "../../Training/";
	public static Config config;
	
	public static void main(String[] args) {
		// Read configuration file
		config = new Config("config.properties");
		
		// Create a problem reader
		ProblemReader reader = new ProblemReader(PROBLEMS_DIR, config.getFloat("imageScaling"));
		
		// Read a problem instance
		ProblemInstance instance = null;
		try {
			instance = reader.readProblem("118035");
		}
		catch(ProblemReadingException e) {
			System.err.println("Couldn't read problem instance.");
			e.printStackTrace();
		}
		
		// Print information about the problem instance
		System.out.println("Problem instance " + instance.getName());
		System.out.println("Resized image size from " + instance.getOriginalWidth() + "x" + instance.getOriginalHeight() + 
				" to " + instance.getImage().getWidth() + "x" + instance.getImage().getHeight());
		
		SegmentationGeneticAlgorithm sga = new SegmentationGeneticAlgorithm(instance);
		sga.setMutationRate(config.getFloat("mutationRate"));
		sga.setCrossoverRate(config.getFloat("crossoverRate"));
		sga.setElites(config.getInt("elites"));
		
		sga.initializePopulation();
		for(int i = 0; i < 100; i++) {
			long time = System.nanoTime();
			System.out.println("---------- Running generation #" + i + " ----------");
			sga.runGeneration();
			sga.printState();
			System.out.println("(" + (System.nanoTime() - time) / 1000000 + " ms)");
		}
		
		Individual fittest = ((Individual) sga.getPopulation().getFittestIndividual());
		fittest.updateSegmentRepresentation();
			 
        // Save images of fittest
		BufferedImage[] images = fittest.generateImages();
        try {
        	for(int i = 0; i < images.length; i++) {
        		File file = new File(config.get("outputDir") + instance.getName() + "/segmentation" + (i+1) + ".png");
        		file.mkdirs();
        		ImageIO.write(images[i], "png", file);
        	}
        	File evalFile = new File(config.get("evaluationDir") + "/segmentation_" + instance.getName() + ".png");
        	evalFile.mkdir();
        	ImageIO.write(images[1], "png", evalFile);
		} catch (IOException e) {
			e.printStackTrace();
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
}
