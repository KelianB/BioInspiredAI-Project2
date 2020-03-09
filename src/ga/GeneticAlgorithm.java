package ga;

import java.util.List;
import java.util.Random;

import problem.IProblemInstance;

/**
 * A simple implementation of the IGeneticAlgorithm interface.
 * @author Kelian Baert & Caroline de Pourtales
 */
public abstract class GeneticAlgorithm implements IGeneticAlgorithm {
	private int generationsRan;
	private IPopulation population;
	private IProblemInstance problemInstance;
	private float mutationRate, crossoverRate;
	private int elites;
	private Random random;
	
	public GeneticAlgorithm(IProblemInstance problemInstance) {
		this.problemInstance = problemInstance;
		this.generationsRan = 0;
		this.mutationRate = 0.0f;
		this.crossoverRate = 0.0f;
		this.elites = 0;
		this.random = new Random();
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
		boolean printTimes = false;
		
		if(population == null) {
			System.err.println("Cannot run GA generation without first initializing the population");
			return;
		}
		
		// Create offsprings
		long globalTime = System.nanoTime();
		long time = System.nanoTime();
		List<IIndividual> offspring = createOffspring();
		if(printTimes)
			System.out.println("Creating offspring took " + (System.nanoTime() - time) / 1000000 + "ms");
		
		// Mutate
		time = System.nanoTime();
		for(int i = 0; i < offspring.size(); i++) {
			if(random() < getMutationRate())
				offspring.get(i).mutate();
		}
		if(printTimes)
			System.out.println("Mutating took " + (System.nanoTime() - time) / 1000000 + "ms");
		
		// Insert offspring
		time = System.nanoTime();
		this.insertOffspring(offspring);
		if(printTimes)
			System.out.println("Inserting offspring took " + (System.nanoTime() - time) / 1000000 + "ms");
		
		if(printTimes)
			System.out.println("Ran generation in " + (System.nanoTime() - globalTime) / 1000000 + "ms");
		
		generationsRan++;
	}
	
	@Override
	public void insertOffspring(List<IIndividual> offspring) {
		// No elitism - replace whole population
		if(getElites() == 0) {
			getPopulation().getIndividuals().clear();
			getPopulation().getIndividuals().addAll(offspring);
		}
		// Elitism - replace population except n best
		else {
			((SimplePopulation) getPopulation()).putElitesFirst(getElites());
			for(int i = 0; i < offspring.size(); i++) {
				getPopulation().getIndividuals().set(getElites() + i, offspring.get(i));
			}
		}
	}
	
	/**
	 * Get a random float in [0,1[ using this GA's random generator
	 * @return a random float between 0 (inclusive) and 1 (exclusive)
	 */
	public float random() {
		return random.nextFloat();
	}
	
	/* SETTERS */
	
	@Override
	public void setMutationRate(float r) {
		this.mutationRate = r;
	}
	
	@Override
	public void setCrossoverRate(float r) {
		this.crossoverRate = r;
	}
	
	@Override
	public void setElites(int elites) {
		this.elites = elites;
	}

	/* GETTERS */
	
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
	
	@Override
	public float getMutationRate() {
		return mutationRate;
	}
	
	public float getCrossoverRate() {
		return crossoverRate;
	}
	
	@Override
	public int getElites() {
		return elites;
	}
}
