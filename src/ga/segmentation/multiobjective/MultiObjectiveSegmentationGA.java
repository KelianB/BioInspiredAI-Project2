package ga.segmentation.multiobjective;

import java.util.ArrayList;
import java.util.List;

import ga.IIndividual;
import ga.IPopulation;
import ga.segmentation.Individual;
import ga.segmentation.IndividualGenerator;
import ga.segmentation.ProblemInstance;
import ga.segmentation.SegmentationGA;
import main.Main;

public class MultiObjectiveSegmentationGA extends SegmentationGA {
	public MultiObjectiveSegmentationGA(ProblemInstance problemInstance, float mutationRate, float crossoverRate) {
		super(problemInstance, mutationRate, crossoverRate);
	}

	@Override
	protected IPopulation createInitialPopulation() {
		MultiObjectivePopulation pop = new MultiObjectivePopulation(this);
		
		int popSize = Main.config.getInt("populationSize");
		int poolSize =  Main.config.getInt("initialPopulationPool");
		
		List<IIndividual> inds = new ArrayList<IIndividual>();
		for(int i = 0; i < poolSize; i++) {
			System.out.println("Creating individual #" + (i+1) + "/" + poolSize);
			inds.add(IndividualGenerator.createRandomIndividual(this));
		}
		pop.updateFrontsAndCrowdingDistances(inds);
		inds.sort(pop.getSelectionComparator());
		pop.setIndividuals(inds.subList(0, popSize));
		System.out.println("Initial population: " + pop.getFirstFront().size() + " individuals in the first front");
		
		return pop;
	}
	
	@Override
	public void printState() {
		List<Individual> firstFront = ((MultiObjectivePopulation) getPopulation()).getFirstFront();
		
		System.out.println("Size of first front: " + firstFront.size());
		
		boolean firstFrontDetails = false;
		if(firstFrontDetails) {
			for(int i = 0; i < firstFront.size(); i++) {
				Individual ind = firstFront.get(i);
				System.out.println("  firstfront." + i + ": " + ind.getSegments().size() + " segments, fitness = " + ind.getFitness());
			}
		}
	}
	
	// No fitness-based elitism
	@Override
	public void setElites(int elites) {
		super.setElites(0);
	}

}
