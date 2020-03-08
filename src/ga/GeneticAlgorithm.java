package ga;

import java.util.List;

import problem.IProblemInstance;

public abstract class GeneticAlgorithm implements IGeneticAlgorithm {
	private int generationsRan;
	private IPopulation population;
	private IProblemInstance problemInstance;
	
	public GeneticAlgorithm(IProblemInstance problemInstance) {
		this.problemInstance = problemInstance;
		this.generationsRan = 0;
	}

	@Override
	public void initializePopulation() {
		if(population != null) {
			System.err.println("Error: GA population was already initialized");
			return;
		}
		population = createInitialPopulation();
	}
	
	protected abstract IPopulation createInitialPopulation();
	
	@Override
	public void runGeneration() {		
		if(population == null) {
			System.err.println("Cannot run GA generation without first initializing the population");
			return;
		}
		
		// Create offsprings
		List<IIndividual> offspring = createOffspring();
		
		// Mutate
		for(int i = 0; i < offspring.size(); i++)
			offspring.get(i).mutate();
		
		// Insert offspring
		this.insertOffspring(offspring);
		
		generationsRan++;
	}

	@Override
	public int getGenerationsRan() {
		return generationsRan;
	}

	@Override
	public IPopulation getPopulation() {
		return population;
	}
	
	@Override
	public IProblemInstance getProblemInstance() {
		return problemInstance;
	}
}
