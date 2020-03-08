package ga.segmentation;

import java.util.ArrayList;
import java.util.List;

import ga.GeneticAlgorithm;
import ga.IIndividual;
import ga.IPopulation;
import ga.SimplePopulation;
import main.Main;
import problem.segmentation.ProblemInstance;

public class SegmentationGeneticAlgorithm extends GeneticAlgorithm {
	public SegmentationGeneticAlgorithm(ProblemInstance problemInstance) {
		super(problemInstance);
	}

	@Override
	public List<IIndividual> createOffspring() {
		// Use tournament selection
		int numOffsprings = getPopulation().getSize() - Main.config.getInt("elites");
		int k = Main.config.getInt("tournamentSelectionSize");
		float p = Main.config.getFloat("tournamentSelectionP");
	
		List<IIndividual> offspring = new ArrayList<IIndividual>();
		for(int i = 0; i < numOffsprings; i++) {
			IIndividual ind = ((SimplePopulation) getPopulation()).tournamentSelection(k, p);
			offspring.add(ind.copy());
		}
			
		return offspring;
	}

	@Override
	public void insertOffspring(List<IIndividual> offspring) {
		int elites = Main.config.getInt("elites");
		if(elites == 0) {
			getPopulation().getIndividuals().clear();
			getPopulation().getIndividuals().addAll(offspring);
		}
		else {
			((SimplePopulation) getPopulation()).putElitesFirst(elites);
			for(int i = 0; i < offspring.size(); i++) {
				getPopulation().getIndividuals().set(elites + i, offspring.get(i));
			}
		}
	}

	@Override
	public void printState() {
		System.out.println("Fitness of fittest individual: " + getPopulation().getFittestIndividual().getFitness());
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
