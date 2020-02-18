package ga;

public interface IGeneticAlgorithm {
	/**
	 * Creates the initial population for this GA instance
	 */
	public void createInitialPopulation();
	
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
	
}
