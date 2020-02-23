package ga.segmentation;

import ga.IIndividual;

public class Individual implements IIndividual {
	public static enum Direction {NONE, UP, RIGHT, DOWN, LEFT}
	
	private Direction[][] representation;
	
	public Individual(int imgWidth, int imgHeight) {
		this.representation = new Direction[imgWidth][imgHeight];
	}
	
	@Override
	public float getFitness() {
		return 0;
	}

	@Override
	public void mutate() {
		
	}

	@Override
	public IIndividual crossover(IIndividual parentB) {
		return null;
	}
	
	public static Individual createRandomIndividual(int imgWidth, int imgHeight) {
		Individual ind = new Individual(imgWidth, imgHeight);
		// TODO create min spanning tree
		
		return ind;
	}

}
