package ga.segmentation;

import java.util.ArrayList;
import java.util.List;

import ga.GeneticAlgorithm;
import ga.IIndividual;
import ga.IPopulation;
import ga.SimplePopulation;
import main.Main;
import problem.segmentation.ProblemInstance;

/**
 * A Genetic Algorithm implementation for image segmentation.
 * @author Kelian Baert & Caroline de Pourtales
 *
 */
public class SegmentationGeneticAlgorithm extends GeneticAlgorithm {
	public SegmentationGeneticAlgorithm(ProblemInstance problemInstance) {
		super(problemInstance);
	}

	@Override
	public List<IIndividual> createOffspring() {
		float crossoverRate = getCrossoverRate();
		
		// Use tournament selection
		int numOffsprings = getPopulation().getSize() - Main.config.getInt("elites");
		int k = Main.config.getInt("tournamentSelectionSize");
		float p = Main.config.getFloat("tournamentSelectionP");
		
		List<IIndividual> offspring = new ArrayList<IIndividual>();
		for(int i = 0; i < numOffsprings; i++) {
			IIndividual parent1 = ((SimplePopulation) getPopulation()).tournamentSelection(k, p); 
			// Crossover
			if(random() < crossoverRate) {
				IIndividual parent2 = ((SimplePopulation) getPopulation()).tournamentSelection(k, p);
				offspring.add(parent1.crossover(parent2));
			}
			// Copy
			else {
				offspring.add(parent1);
			}
		}
			
		return offspring;
	}

	@Override
	public void printState() {
		Individual fittest = (Individual) getPopulation().getFittestIndividual(); 
		System.out.println("Fittest individual: fitness = " + fittest.getFitness() + " (" + fittest.getSegments().size() + " segments)");
		boolean details = true;
		if(details) {
			System.out.println("	Edge value = " + fittest.getEdgeValue());
			System.out.println("	Connectivity = " + fittest.getConnectivity());
			System.out.println("	Overall deviation = " + fittest.getOverallDeviation());
		}
	}

	@Override
	protected IPopulation createInitialPopulation() {
		Population pop = new Population(this);
		for(int i = 0; i < Main.config.getInt("populationSize"); i++) {
			System.out.println("Creating individual #" + i);
			Individual ind = Individual.createRandomIndividual(this);
			pop.addIndividual(ind);
		}
		return pop;
	}
}
