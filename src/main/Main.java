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
		 
		System.out.println("Image size: " + instance.getImage().getWidth() + " x " + instance.getImage().getHeight());
		
		SegmentationGeneticAlgorithm sga = new SegmentationGeneticAlgorithm(instance);
		sga.setMutationRate(config.getFloat("mutationRate"));
		
		/*System.out.println("Creating a random individual:");
		Individual ind = Individual.createRandomIndividual(sga, instance);
		ind.getFitness();
		//ind.print();
		System.out.println("Number of segments: " + ind.getSegments().size());
		*/
		
		sga.initializePopulation();
		
		
		/*Individual fittest = ((Individual) sga.getPopulation().getFittestIndividual());
		fittest.updateSegmentRepresentation();
		fittest.print();		 
        // Save images of fittest
		BufferedImage[] images = fittest.generateImages();
        try {
        	for(int i = 0; i < images.length; i++) {
        		File file = new File(PROBLEMS_DIR + "output/segmentation" + (i+1) + ".png");
        		ImageIO.write(images[i], "png", file);
        	}
		} catch (IOException e) {
			e.printStackTrace();
		}*/

		for(int i = 0; i < 10000; i++) {
			System.out.println("---------- Running generation #" + i + " ----------");
			sga.runGeneration();
			Individual fittest = (Individual) sga.getPopulation().getFittestIndividual(); 
			System.out.println("Fittest: " + fittest.getFitness() + " (" + fittest.getSegments().size() + " segments)");
			//System.out.println("Edge value = " + fittest.getEdgeValue());
			//System.out.println("Connectivity = " + fittest.getConnectivity());
			//System.out.println("Overall deviation = " + fittest.getOverallDeviation());
		}
		
		
		// TESTING 
		
		// Generate a random position as the origin of the minimum spanning tree
		
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
