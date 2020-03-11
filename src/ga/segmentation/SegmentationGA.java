package ga.segmentation;

import java.util.ArrayList;
import java.util.List;

import ga.GeneticAlgorithm;
import ga.IIndividual;
import ga.IPopulation;
import main.Main;
import problem.segmentation.ProblemInstance;

/**
 * A Genetic Algorithm implementation for image segmentation.
 * @author Kelian Baert & Caroline de Pourtales
 *
 */
public class SegmentationGA extends GeneticAlgorithm {
	public SegmentationGA(ProblemInstance problemInstance, float mutationRate, float crossoverRate) {
		super(problemInstance, mutationRate, crossoverRate);
	}

	
	@Override
	public void printState() {
		Population pop = getPopulation();
		Individual fittest = (Individual) pop.getFittestIndividual(); 
		// Average fitness
		float avgFitness = 0.0f;
		for(IIndividual i : pop.getIndividuals())
			avgFitness += i.getFitness() / pop.getSize();
		System.out.println("Average fitness = " + avgFitness);

		// Fittest
		System.out.println("Fittest individual: fitness = " + fittest.getFitness() + " (" + fittest.getSegments().size() + " segments)");
		boolean details = true;
		if(details) {
			float alpha = Main.config.getFloat("fitness_alpha"), beta = Main.config.getFloat("fitness_beta"), gamma = Main.config.getFloat("fitness_gamma");
			
			float edge = fittest.getEdgeValue(),
				  conn = fittest.getConnectivity(),
				  ovdev = fittest.getOverallDeviation();
			System.out.println("	Edge value = " + edge + " (" + alpha*edge + ")");
			System.out.println("	Connectivity = " + conn + " (" + beta*conn + ")");
			System.out.println("	Overall deviation = " + ovdev + " (" + gamma*ovdev + ")");
		}
	}

	@Override
	protected IPopulation createInitialPopulation() {
		Population pop = new Population(this);
		
		int popSize = Main.config.getInt("populationSize");
		int poolSize =  Main.config.getInt("initialPopulationPool");
		
		List<IIndividual> inds = new ArrayList<IIndividual>();
		for(int i = 0; i < poolSize; i++) {
			System.out.println("Creating individual #" + i + "/" + poolSize);
			inds.add(IndividualGenerator.createRandomIndividual(this));
		}
		inds.sort((a,b) -> (int) Math.signum(b.getFitness() - a.getFitness()));
		pop.setIndividuals(inds.subList(0, popSize));
		
		return pop;
	}
	
	@Override
	public ProblemInstance getProblemInstance() {
		return (ProblemInstance) super.getProblemInstance();
	}
	@Override
	public Population getPopulation() {
		return (Population) super.getPopulation();
	}
}
