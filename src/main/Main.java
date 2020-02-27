package main;

import ga.segmentation.Individual;
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
			instance = reader.readProblem("86016");
		}
		catch(ProblemReadingException e) {
			System.err.println("Couldn't read problem instance.");
			e.printStackTrace();
		}
		 
		System.out.println("Image size: " + instance.getImage().getWidth() + " x " + instance.getImage().getHeight());
		System.out.println("Creating a random individual:");
		Individual ind = Individual.createRandomIndividual(instance);
		ind.getFitness();
		ind.print();
		System.out.println("Number of segments: " + ind.getSegments().size());
	}
}
