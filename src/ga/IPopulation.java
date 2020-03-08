package ga;

import java.util.List;

public interface IPopulation {
	/**
	 * Get the individuals in the population
	 * @return a list of individuals
	 */
	public List<IIndividual> getIndividuals();
	
	/**
	 * Add an individual to the population
	 * @param ind - An individual
	 */
	public void addIndividual(IIndividual ind);
	
	/**
	 * Get the size of the population
	 * @return the number of individuals in the population
	 */
	public int getSize();
	
	
	/**
	 * Get the individual that has the highest fitness in the population
	 * @return the individual that has the highest fitness in the population
	 */
	public IIndividual getFittestIndividual();
}
