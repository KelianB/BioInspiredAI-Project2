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

	public int[][] getNeighbors (int xi, int yi) {

		int[][] neighbors = new int[4][2];
		int n = 0;

		if (xi < this.representation[0].length-1 && (representation[xi][yi]==Direction.DOWN || representation[xi+1][yi]==Direction.UP)) {
			neighbors[n][0] = xi+1;
			neighbors[n][1] = yi;
			n++;
		}
		if (xi>0 && (representation[xi][yi]==Direction.UP || representation[xi-1][yi]==Direction.DOWN)) {
			neighbors[n][0] = xi-1;
			neighbors[n][1] = yi;
			n++;
		}
		if (yi < this.representation.length-1 && (representation[xi][yi]==Direction.RIGHT || representation[xi][yi+1]==Direction.LEFT)) {
			neighbors[n][0] = xi;
			neighbors[n][1] = yi+1;
			n++;
		}
		if (yi>0 && (representation[xi][yi]==Direction.LEFT || representation[xi][yi-1]==Direction.RIGHT)) {
			neighbors[n][0] = xi;
			neighbors[n][1] = yi-1;
			n++;
		}

		return neighbors;
	}

	public boolean sameSegment(int xi, int yi, int xj, int yj) {
		if ((xi == xj) && (yi==yj)) {
			return true;
		}
		if (this.representation[xi][yi]==Direction.NONE) {
			return false;
		}
		else {
			int [][] neighbors = getNeighbors (xi, yi);
			boolean bool = false;
			for (int i=0 ; i <neighbors[0].length ; i++) {
				bool = bool || sameSegment(neighbors[i][0],neighbors[i][1], xj,yj);
			}
		}
	}

	public float dist (int xi, int yi, int xj, int yj) {
		if (sameSegment(xi,yi,xj,yj)) {
			return 0;
		} else {

		}
	}

}
