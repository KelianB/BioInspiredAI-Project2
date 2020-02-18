package ga;

public abstract class CachedFitnessIndividual implements IIndividual {
	private float fitness;
	private boolean fitnessRequiresUpdate;
	
	public CachedFitnessIndividual() {
		this.fitnessRequiresUpdate = true;
	}
	
	/**
	 * Get the fitness, only re-computing it when needed
	 * @return the individual's fitness
	 */
	@Override
	public float getFitness() {
		if(fitnessRequiresUpdate) {
			fitness = this.calculateFitness();
			fitnessRequiresUpdate = false;
		}
		return fitness;
	}
		
	/**
	 * Mutate and register that the fitness needs to be recalculated
	 */
	@Override
	public void mutate() {
		this.performMutation();
		this.fitnessRequiresUpdate = true;
	}
	
	/**
	 * Calculate the individual's fitness
	 * @return the individual's fitness
	 */
	protected abstract float calculateFitness();
	
	/**
	 * Performs a mutation on this individual
	 */
	protected abstract void performMutation();
}
