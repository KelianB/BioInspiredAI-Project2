package ga;

import java.util.List;

import problem.IProblemInstance;

/**
 * An interface describing a general purpose Genetic Algorithm.
 * @author Kelian Baert & Caroline de Pourtales
 */
public interface IGeneticAlgorithm {
	/**
	 * Creates the initial population for this GA instance
	 */
	public void initializePopulation();
	
	/**
	 * Get the current population
	 * @return the current population
	 */
	public IPopulation getPopulation();
	
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
	
	/**
	 * Get the problem instance this GA operates on
	 * @return the problem instance
	 */
	public IProblemInstance getProblemInstance();
	
	/**
	 * Set the GA's mutation rate.
	 * @param r - The new mutation rate
	 */
	public void setMutationRate(float r);
	
	/**
	 * Get the GA's mutation rate
	 * @return the mutation rate
	 */
	public float getMutationRate();
	
	/**
	 * Set the GA's crossover rate.
	 * @param r - The new crossover rate
	 */
	public void setCrossoverRate(float r);
	
	/**
	 * Get the GA's crossover rate
	 * @return the crossover rate
	 */
	public float getCrossoverRate();
	
	/**
	 * Set the number of elites (0 disables elitism).
	 * @param elites - The number of best individuals to keep at each generation
	 */
	public void setElites(int elites);
	
	/**
	 * Get the number of elites
	 * @return The number of best individuals to keep at each generation
	 */
	public int getElites();
}
