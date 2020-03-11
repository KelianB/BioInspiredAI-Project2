package ga.segmentation.multiobjective;

import java.util.List;

import ga.IPopulation;
import ga.segmentation.Individual;
import ga.segmentation.IndividualGenerator;
import ga.segmentation.Population;
import ga.segmentation.SegmentationGA;
import main.Main;
import problem.segmentation.ProblemInstance;

public class MultiObjectiveSegmentationGA extends SegmentationGA {
	public MultiObjectiveSegmentationGA(ProblemInstance problemInstance, float mutationRate, float crossoverRate) {
		super(problemInstance, mutationRate, crossoverRate);
	}

	@Override
	protected IPopulation createInitialPopulation() {
		Population pop = new MultiObjectivePopulation(this);
		
		for(int i = 0; i < Main.config.getInt("populationSize"); i++) {
			System.out.println("Creating individual #" + i);
			Individual ind = IndividualGenerator.createRandomIndividual(this);
			pop.addIndividual(ind);
		}
		
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
