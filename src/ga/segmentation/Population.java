package ga.segmentation;


import java.util.ArrayList;
import java.util.List;

import ga.GeneticAlgorithm;
import ga.IIndividual;
import ga.SimplePopulation;
import main.Main;
import main.Main.Mode;

public class Population extends SimplePopulation {
	public Population(GeneticAlgorithm ga) {
		super(ga);
	}

	@Override
	public List<IIndividual> createOffspring() {
		float crossoverRate = ga.getCrossoverRate();
		
		// Use tournament selection
		int numOffsprings = getSize() - ga.getElites();
		int k = Main.config.getInt(Main.mode == Mode.WEIGHTED_SUM_GA ? "WSGA_tournamentSelectionSize" : "MOEA_tournamentSelectionSize");
		float p = Main.config.getFloat(Main.mode == Mode.WEIGHTED_SUM_GA ? "WSGA_tournamentSelectionP" : "MOEA_tournamentSelectionP");
		
		List<IIndividual> offspring = new ArrayList<IIndividual>();
		while(offspring.size() < numOffsprings) {
			IIndividual parent1 = tournamentSelection(k, p); 
			// Crossover
			if(ga.random() < crossoverRate) {
				IIndividual parent2 = tournamentSelection(k, p);
				offspring.add(parent1.crossover(parent2));
				if(offspring.size() < numOffsprings)
					offspring.add(parent2.crossover(parent1));
			}
			// Copy
			else
				offspring.add(parent1.copy());
		}
		return offspring;
	}
}