package ga;

import java.util.List;

public interface IPopulation {
	/**
	 * Get the individuals in the population
	 * @return a list of individuals
	 */
	public List<IIndividual> getIndividuals();
	
	/**
	 * Get the individual that has the highest fitness in the population
	 * @return the individual that has the highest fitness in the population
	 */
	public IIndividual getFittestIndividual();
	
	/**
	 * Inserts offspring into the population.
	 * The method used depends on the implementation (e.g. (lambda, mu) or (lambda+mu))
	 * @param offspring - A list of individuals to be inserted in the population
	 */
	public void insertOffspring(List<IIndividual> offspring);
}
