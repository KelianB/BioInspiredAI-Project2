package main;

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
		ProblemReader reader = new ProblemReader(PROBLEMS_DIR);
		
		// Read a problem instance
		ProblemInstance test = null;
		try {
			test = reader.readProblem("86016");
		}
		catch(ProblemReadingException e) {
			System.err.println("Couldn't read problem instance.");
			e.printStackTrace();
		}
		 
		System.out.println(test.getImage().getWidth() + " " + test.getImage().getHeight());
	}
}
