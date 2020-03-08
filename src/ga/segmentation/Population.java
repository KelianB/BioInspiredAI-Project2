package ga.segmentation;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import ga.SimplePopulation;

public class Population extends SimplePopulation {

	public LinkedList<LinkedList<Individual>> fastNonDominatedSorting (ga.Population population) {
		LinkedList<LinkedList<Individual>> fronts = new LinkedList<LinkedList<Individual>>();
		LinkedList<Individual> F1 = new LinkedList<Individual>();
		for (Individual i : this.getIndividuals()) {
			LinkedList<Individual> Si = new LinkedList<Individual>();
			int ni=0;
			for (Individual j : this.getIndividuals()) {
				//j dominates i
				if (!(i.edgeValue() < j.edgeValue() && i.connectivity()<j.connectivity() && i.overallDeviation() < j.overallDeviation())) {
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
				LinkedList<Individual> Si = new LinkedList<Individual>();
				for (Individual j : this.getIndividuals()) {
					if (i.edgeValue() < j.edgeValue() && i.connectivity()<j.connectivity() && i.overallDeviation() < j.overallDeviation()) {
						Si.add(j);
					}
				}
				int nj=0;
				for (Individual j : Si) {
					nj--;
					if (nj==0) {
						Q.add(j);
					}
				}
			}
			currentFi = new LinkedList<Individual>(Q);;
			fronts.add(currentFi);
		}
		return fronts;
	}

}
