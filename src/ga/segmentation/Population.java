package ga.segmentation;

import java.util.LinkedList;

import ga.GeneticAlgorithm;
import ga.SimplePopulation;

public class Population extends SimplePopulation {
	public Population(GeneticAlgorithm ga) {
		super(ga);
	}

	public LinkedList<Population> fastNonDominatedSorting(Population population) {
		/*LinkedList<Population> fronts = new LinkedList<Population>();
		LinkedList<Individual> F1 = new LinkedList<Individual>();
		for(IIndividual ind1 : this.getIndividuals()) {
			Individual i = (Individual) ind1;
			LinkedList<Individual> Si = new LinkedList<Individual>();
			int ni = 0;
			
			for(IIndividual ind2 : this.getIndividuals()) {
				Individual j = (Individual) ind2;
				if(!(i.edgeValue() < j.edgeValue() && i.connectivity()<j.connectivity() && i.overallDeviation() < j.overallDeviation())) {
					ni++;
				}
			}
			if(ni == 0) {
				F1.add(i);
			}
		}

		F1.add(fronts);
		LinkedList<Individual> currentFi = new LinkedList<Individual>(F1);
		while(!currentFi.isEmpty()) {
			LinkedList<Individual> Q = new LinkedList<Individual>();
			for(Individual i : currentFi) {
				LinkedList<Individual> Si = new LinkedList<Individual>();
				for(Individual j : this.individuals) {
					if(i.edgeValue() < j.edgeValue() && i.connectivity()<j.connectivity() && i.overallDeviation() < j.overallDeviation()) {
						Si.add(j);
					}
				}
				int nj=0;
				for(Individual j : Sp) {
					nj--;
					if(nj==0) {
						Q.add(j);
					}
				}
			}
			currentFi = new LinkedList<Individual>(Q);;
			fronts.add(currentFi);
		}*/
		return new LinkedList<Population>();
	}
}
