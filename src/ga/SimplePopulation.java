package ga;

import java.util.ArrayList;
import java.util.List;

public class SimplePopulation implements IPopulation {
	private List<IIndividual> individuals;
	
	public SimplePopulation() {
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
}
