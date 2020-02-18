package main;

import problem.ProblemReadingException;
import problem.segmentation.ProblemInstance;
import problem.segmentation.ProblemReader;

public class Main {
	public static final String PROBLEMS_DIR = "../../Training/";
	
	public static void main(String[] args) {
		 ProblemReader reader = new ProblemReader(PROBLEMS_DIR);
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
