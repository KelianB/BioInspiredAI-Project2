package ga;

import java.util.List;

public interface IGeneticAlgorithm {
	/**
	 * Creates the initial population for this GA instance
	 */
	public void initializePopulation();
	
	/**
	 * Get the current population
	 * @return the current population
	 */
	public Population getPopulation();
	
	/**
	 * Run one generation of the GA
	 */
	public void runGeneration();
	
	/**
	 * Print the current state of the GA (fitness of fittest individual, etc)
	 */
	public void printState();
	
	/**
	 * Get the number of generations that have been ran
	 * @return the number of generations that have been ran
	 */
	public int getGenerationsRan();
	
	/**
	 * Create offspring from the current population 
	 * @return
	 */
	public List<IIndividual> createOffspring();
	
	/**
	 * Insert offspring into the population
	 * @param offspring - A list of offspring
	 */
	public void insertOffspring(List<IIndividual> offspring);
	
	
}
