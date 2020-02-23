package ga.segmentation;

import java.util.ArrayList;
import java.util.List;

import ga.GeneticAlgorithm;
import ga.IIndividual;
import ga.Population;

public class SegmentationGeneticAlgorithm extends GeneticAlgorithm {
	public SegmentationGeneticAlgorithm() {
		super();
	}

	@Override
	public List<IIndividual> createOffspring() {
		// Just a copy for now
		List<IIndividual> offspring = new ArrayList<IIndividual>();
		for(int i = 0; i < getPopulation().getSize(); i++) {
			offspring.add(getPopulation().getIndividuals().get(i));
		}
		return offspring;
	}

	@Override
	public void insertOffspring(List<IIndividual> offspring) {
		// Simply replace for now
		getPopulation().getIndividuals().clear();
		getPopulation().getIndividuals().addAll(offspring);
	}

	@Override
	public void printState() {
		System.out.println("Fitness of fittest individual: " + getPopulation().getFittestIndividual().getFitness());
	}

	@Override
	public Population createInitialPopulation() {
		return null;
	}


}
