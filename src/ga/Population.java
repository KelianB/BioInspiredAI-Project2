package ga;

import java.util.ArrayList;
import java.util.List;

import ga.segmentation.Individual;

public class Population implements IPopulation {
	private List<IIndividual> individuals;
	
	public Population() {
		individuals = new ArrayList<IIndividual>();
	}
	
	@Override
	public List<IIndividual> getIndividuals() {
		return individuals;
	}

	@Override
	public int getSize() {
		return individuals.size();
	}
	
	
	@Override
	public IIndividual getFittestIndividual() {
		int popSize = getSize();
		if(popSize == 0)
			return null;
		
		int maxIndex = 0;
		float maxFitness = getIndividuals().get(0).getFitness();
		for(int i = 1; i < popSize; i++) {
			float fitness = getIndividuals().get(i).getFitness();
			if(fitness > maxFitness) {
				maxFitness = fitness;
				maxIndex = i;
			}
		}
		
		return getIndividuals().get(maxIndex);
	}

	public LinkedList<Population> fastNonDominatedSorting (Population population) {
		LinkedList<Population> fronts = new LinkedList<Population>();
		LinkedList<Individual> F1 = new LinkedList<Individual>();
		for (Individual i : this.individuals) {
			LinkedList<Individual> Si = new LinkedList<Individual>();
			int ni=0;
			for (Individual j : this.individuals) {
				if (i.edgeValue() < j.edgeValue() && i.connectivity()<j.connectivity() && i.overallDeviation() < j.overallDeviation()) {
					Si.add(j);
				}
				else {
					ni++;
				}
			}
			if (ni==0) {
				F1.add(i);
			}
		}
		fronts.add(F1);
		LinkedList<Individual> currentFi = new LinkedList<Individual>(F1);
		while (!currentFi.isEmpty()) {
			LinkedList<Individual> Q = new LinkedList<Individual>();
			for (Individual i : currentFi) {

			}

		}




	}
}
