package ga.segmentation.multiobjective;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import ga.GeneticAlgorithm;
import ga.IIndividual;
import ga.segmentation.Individual;
import ga.segmentation.Population;

public class MultiObjectivePopulation extends Population {
	private List<ArrayList<Individual>> fronts;
	private Map<Individual, Integer> frontMap;
	private Map<Individual, Float> crowdingDistances;
	
	public MultiObjectivePopulation(GeneticAlgorithm ga) {
		super(ga);
		fronts = new ArrayList<ArrayList<Individual>>();
		frontMap = new HashMap<Individual, Integer>();
		crowdingDistances = new HashMap<Individual, Float>();
	}

	/**
	 * Update the fronts and crowding distances for the given individuals
	 * @param individuals
	 */
	private void updateFrontsAndCrowdingDistances(List<IIndividual> iindividuals) {
		// Cast IIndividual to Individual
		List<Individual> individuals = new ArrayList<Individual>();
		for(IIndividual i : iindividuals)
			individuals.add((Individual) i);
		
		long time = System.nanoTime();
		// Update the fronts
		fastNonDominatedSorting(individuals);
		System.out.println("Fast non-dominated sorting took " + (System.nanoTime() - time) / 1000000 + " ms");
		
		// Store a Individual:frontIndex map for fast access
		frontMap.clear();
		for(int i = 0; i < fronts.size(); i++) {
			for(Individual ind : fronts.get(i))
				frontMap.put(ind, i);
		}
		
		time = System.nanoTime();
		updateCrowdingDistances();
		System.out.println("Computing crowding distances took " + (System.nanoTime() - time) / 1000000 + " ms");
	}
	
	/** 
	 * Override the selection comparator to use fronts & crowding distance
	 * Lower-front individuals are selected first, falling back on crowding distances when two individuals belong to the same front
	 */
	@Override
	public Comparator<IIndividual> getSelectionComparator() {
		return (a,b) -> {
			int frontA = frontMap.get(a), frontB = frontMap.get(b);
			if(frontA != frontB)
				return (int) Math.signum(frontA - frontB);
			else
				return (int) Math.signum(crowdingDistances.get(b) - crowdingDistances.get(a));
		};
	}
	
	@Override
	public List<IIndividual> createOffspring() {
		// Update the fronts and crowding distances for the current population.
		updateFrontsAndCrowdingDistances(getIndividuals());
		
		// Let the superclass create the offspring, using the same tournament selection logic we use in the weighted-sum GA 
		// (but with our new comparator)
		return super.createOffspring();
		
		/*float crossoverRate = ga.getCrossoverRate();
		
		// Use tournament selection
		int numOffsprings = getSize() - ga.getElites();
		int k = Main.config.getInt("tournamentSelectionSize");
		float p = Main.config.getFloat("tournamentSelectionP");
		
		List<IIndividual> offspring = new ArrayList<IIndividual>();
		while(offspring.size() < numOffsprings) {
			IIndividual parent1 = tournamentSelection(k, p); 
			// Crossover
			if(ga.random() < crossoverRate) {
				IIndividual parent2 = tournamentSelection(k, p);
				offspring.add(parent1.crossover(parent2));
				if(offspring.size() < numOffsprings)
					offspring.add(parent2.crossover(parent1));
			}
			// Copy
			else {
				offspring.add(parent1.copy());
			}
		}
			
		return offspring;*/
	}
	
	@Override
	public void insertOffspring(List<IIndividual> offspring) {
		List<IIndividual> pool = new ArrayList<IIndividual>();
		pool.addAll(getIndividuals());
		pool.addAll(offspring);

		// Compute the fronts and crowding distances for parent + offspring
		updateFrontsAndCrowdingDistances(pool);
		
		System.out.println("Number of fronts for pop + offspring: " + fronts.size());
		
		// Reject to cull down parent+offspring to the right amount of individuals
		pool.sort(getSelectionComparator());
		
		/*pool = pool.subList(0, getSize());

		setIndividuals(pool);
		
		// Update the fronts again so we only have the current population in the fronts storage
		updateFrontsAndCrowdingDistances(pool);*/
		
		
		List<IIndividual> newpop = new ArrayList<IIndividual>();
		while(newpop.size() < getSize()) {
			pool.sort(getSelectionComparator());
			newpop.add(pool.remove(0));
			updateFrontsAndCrowdingDistances(pool);
		}
		setIndividuals(newpop);
		
		// Update the fronts again so we only have the current population in the fronts storage
		updateFrontsAndCrowdingDistances(newpop);
	}
	
	private void fastNonDominatedSorting(List<Individual> individuals) {
		fronts.clear();
		
		// HashMap of ni
		Map<Individual, Integer> nindexes = new HashMap<Individual, Integer>();
		
		// Each entry contains a list of individuals that the key individual is dominating
		Map<Individual, ArrayList<Individual>> dominating = new HashMap<Individual, ArrayList<Individual>>();

		// First front: none of the individuals in it are dominated
		fronts.add(new ArrayList<Individual>());

		for(Individual i : individuals) {			
			int idominatedBy = 0; // number of individuals i is dominated by
			ArrayList<Individual> idominating = new ArrayList<Individual>(); // individuals i is dominating

			for(Individual j : individuals) {
				// j dominates i
				if(i.getEdgeValue() < j.getEdgeValue() && i.getConnectivity() < j.getConnectivity() && i.getOverallDeviation() < j.getOverallDeviation())
					idominatedBy++;
				else if(i.getEdgeValue() > j.getEdgeValue() && i.getConnectivity() > j.getConnectivity() && i.getOverallDeviation() > j.getOverallDeviation())
					idominating.add(j);
			}
			if(idominatedBy == 0)
				fronts.get(0).add(i);
			
			nindexes.put(i,idominatedBy);
			dominating.put(i,idominating);
		}

		// Other fronts
		ArrayList<Individual> currentFi = new ArrayList<Individual>(fronts.get(0));
		while(!currentFi.isEmpty()) {
			List<Individual> newFront = new ArrayList<Individual>();
			for(Individual i : currentFi) {
				// Iterate over the individuals that i dominates
				for(Individual j : dominating.get(i)) {
					nindexes.put(j, nindexes.get(j) - 1);
					// If j is dominated by no one anymore, add it to the current front
					if(nindexes.get(j) == 0)
						newFront.add(j);
				}
			}
			
			currentFi = new ArrayList<Individual>(newFront);
			if(!currentFi.isEmpty())
				fronts.add(currentFi);
		}
	}

	private void updateCrowdingDistances() {
		crowdingDistances.clear();
		// For each front, calculate the crowding distance of the individuals inside
		for(List<Individual> front : fronts) {
			for(Individual i : front)
				crowdingDistances.put(i, 0.0f);

			// Criterion: edgeValue
			addCrowdingDistances(front, Individual::getEdgeValue, crowdingDistances);
	
			// Criterion: connectivity
			addCrowdingDistances(front, Individual::getConnectivity, crowdingDistances);
			
			// Criterion: overallDeviation
			addCrowdingDistances(front, Individual::getOverallDeviation, crowdingDistances);
		}
	}
	
	private void addCrowdingDistances(List<Individual> front, Function<Individual, Float> objectiveFunction, Map<Individual, Float> crowdingDistances) {
		front.sort((a,b) -> (int) Math.signum(objectiveFunction.apply(b) - objectiveFunction.apply(a)));
		
		Individual first = front.get(0), last = front.get(front.size() - 1);
		
		crowdingDistances.put(first, Float.POSITIVE_INFINITY);
		crowdingDistances.put(last, Float.POSITIVE_INFINITY);

		float objectiveMin = objectiveFunction.apply(last);
		float objectiveMax = objectiveFunction.apply(first);

		for(int i = 1; i < front.size()-1; i++) {
			crowdingDistances.put(front.get(i), crowdingDistances.get(front.get(i)) +
					(objectiveFunction.apply(front.get(i+1)) - objectiveFunction.apply(front.get(i-1))) / (objectiveMax - objectiveMin));
		}
	}
	
	
	public List<Individual> getFirstFront() {
		return fronts.get(0);
	}
}
