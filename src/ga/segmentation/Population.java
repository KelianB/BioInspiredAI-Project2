package ga.segmentation;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import ga.GeneticAlgorithm;
import ga.IIndividual;
import ga.SimplePopulation;

public class Population extends SimplePopulation {
	// Store mean and deviation for each objective, used for normalization
	private float[] objectiveMeans, objectiveDeviations;
	
	public Population(GeneticAlgorithm ga) {
		super(ga);
	}

	public ArrayList<ArrayList<Individual>> fastNonDominatedSorting (Population population) {
		// list of fronts
		ArrayList<ArrayList<Individual>> fronts = new ArrayList<ArrayList<Individual>>();
		// HashMap of ni
		HashMap<Individual, Integer> nindexes = new HashMap<Individual, Integer>();
		// HashMap of Si
		HashMap<Individual, ArrayList<Individual>> Sindexes = new HashMap<Individual, ArrayList<Individual>>();

		// first front : none of the individuals in it is dominated
		ArrayList<Individual> F1 = new ArrayList<Individual>();

		for(IIndividual ii : this.getIndividuals()) {
			Individual i = (Individual) ii;
			int ni=0;
			ArrayList<Individual> Si = new ArrayList<Individual>();

			for (IIndividual ji : this.getIndividuals()) {
				Individual j = (Individual) ji;
				// j dominates i
				if(i.getEdgeValue() < j.getEdgeValue() && i.getConnectivity() < j.getConnectivity() && i.getOverallDeviation() < j.getOverallDeviation()) {
					ni++;
				} else if (i.getEdgeValue() > j.getEdgeValue() && i.getConnectivity() > j.getConnectivity() && i.getOverallDeviation() > j.getOverallDeviation()) {
					Si.add(j);
				}
			}
			if(ni==0)
				F1.add(i);
			
			nindexes.put(i,ni);
			Sindexes.put(i,Si);
		}
		fronts.add(F1);

		// other fronts
		ArrayList<Individual> currentFi = new ArrayList<Individual>(F1);
		while(!currentFi.isEmpty()) {
			List<Individual> Q = new ArrayList<Individual>();
			for(Individual i : currentFi) {
				for(Individual j : Sindexes.get(i)) {
					nindexes.put(j,nindexes.get(j)-1) ;
					if(nindexes.get(j) == 0)
						Q.add(j);
				}
			}
			currentFi = new ArrayList<Individual>(Q);;
			fronts.add(currentFi);
		}
		return fronts;
	}

	public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
		Comparator<K> valueComparator = (k1, k2) -> {
			int compare = map.get(k1).compareTo(map.get(k2));
			return compare == 0 ? 1 : compare;
		};

		Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
		sortedByValues.putAll(map);
		return sortedByValues;
	}

	public List<Map<Individual, Float>> crowdingDistance(List<List<Individual>> fronts) {

		List<Map<Individual, Float>> frontsCrowdingDistance = new ArrayList<Map<Individual, Float>>();
		SortedMap<Individual, Float> frontCrowdingDistance;

		// for each front, calculate the crowding distance of the individuals inside
		for(List<Individual> front : fronts) {

			frontCrowdingDistance = new TreeMap<Individual, Float>();

			for(int i = 0; i < front.size(); i++)
				frontCrowdingDistance.put(front.get(i), 0.0f);

			// criterion edgeValue
			front.sort((a,b) -> (int) Math.signum(b.getEdgeValue() - a.getEdgeValue()));

			frontCrowdingDistance.put(front.get(0), Float.POSITIVE_INFINITY);
			frontCrowdingDistance.put(front.get(front.size()-1), Float.POSITIVE_INFINITY);

			Float edgeValueMax = front.get(front.size()-1).getEdgeValue();
			Float edgeValueMin = front.get(0).getEdgeValue();

			for(int i = 1; i < front.size()-1; i++) {
				frontCrowdingDistance.put(front.get(i), frontCrowdingDistance.get(front.get(i))  + (front.get(i+1).getEdgeValue() - front.get(i-1).getEdgeValue())/(edgeValueMax-edgeValueMin));
			}

			// criterion connectivity
			front.sort((a, b) -> (int) Math.signum(b.getConnectivity() - a.getConnectivity()));

			frontCrowdingDistance.put(front.get(0), Float.POSITIVE_INFINITY);
			frontCrowdingDistance.put(front.get(front.size()-1), Float.POSITIVE_INFINITY);

			edgeValueMax = front.get(front.size()-1).getConnectivity();
			edgeValueMin = front.get(0).getConnectivity();

			for(int i = 1; i < front.size()-1 ; i++) {
				frontCrowdingDistance.put(front.get(i), frontCrowdingDistance.get(front.get(i))  + (front.get(i+1).getConnectivity() - front.get(i-1).getConnectivity())/(edgeValueMax-edgeValueMin));
			}

			// criterion overallDeviation
			front.sort((a,b) -> (int) Math.signum(b.getOverallDeviation() - a.getOverallDeviation()));

			frontCrowdingDistance.put(front.get(0), Float.POSITIVE_INFINITY);
			frontCrowdingDistance.put(front.get(front.size()-1), Float.POSITIVE_INFINITY);

			edgeValueMax = front.get(front.size()-1).getOverallDeviation();
			edgeValueMin = front.get(0).getOverallDeviation();

			for(int i = 1; i < front.size()-1; i++) {
				frontCrowdingDistance.put(front.get(i), frontCrowdingDistance.get(front.get(i))  + (front.get(i+1).getOverallDeviation() - front.get(i-1).getOverallDeviation())/(edgeValueMax-edgeValueMin));
			}

			Map sortedMap = sortByValues(frontCrowdingDistance);
			frontsCrowdingDistance.add(sortedMap);
		}
		return frontsCrowdingDistance;
	}

	public List<Map<Individual, Float>> selection (List<Map<Individual, Float>> fronts) {
		List<Map<Individual, Float>> newPopulation = new ArrayList<Map<Individual, Float>>();
		int n = 0;
		for (int i = 0 ; i< fronts.size() ;i++) {
			Map<Individual, Float> front = new TreeMap<Individual, Float>();
			for (Individual ind : fronts.get(i).keySet()) {
				while (n<this.getSize()) {
					 front.put(ind,fronts.get(i).get(ind));
					 n++;
				}
			}
			newPopulation.add(front);
		}
		return newPopulation;
	}

	public Individual binaryTournamentSelection(List<Map<Individual, Float>> sortedFronts) {
		int frontParent1 = (int) (ga.random() * sortedFronts.size());
		Individual parent1 = new ArrayList<>(sortedFronts.get(frontParent1).keySet()).get((int) (ga.random() * sortedFronts.get(frontParent1).size()));

		int frontParent2 = (int) (ga.random() * sortedFronts.size());
		Individual parent2 = new ArrayList<>(sortedFronts.get(frontParent2).keySet()).get((int) (ga.random() * sortedFronts.get(frontParent2).size()));

		if(frontParent1 < frontParent2)
			return parent1;
		else if(frontParent1 > frontParent2)
			return parent2;
		else
			return sortedFronts.get(frontParent1).get(parent1) < sortedFronts.get(frontParent2).get(parent2) ? parent1 : parent2;
	}

	/**
	 * Update the normalization factors for the weighted sum (means and standard deviations)
	 */
	public void updateNormalizationValues() {
		objectiveMeans = new float[3];
		objectiveDeviations = new float[3];
		
		// Update means
		int numIndividuals = getIndividuals().size();
		for(IIndividual ii : getIndividuals()) {
			Individual i = (Individual) ii;
			objectiveMeans[0] += i.getEdgeValue() / numIndividuals;
			objectiveMeans[1] += i.getConnectivity() / numIndividuals;
			objectiveMeans[2] += i.getOverallDeviation() / numIndividuals;
		}
		// Update deviations
		for(IIndividual ii : getIndividuals()) {
			Individual i = (Individual) ii;
			objectiveDeviations[0] += Math.pow(i.getEdgeValue() - objectiveMeans[0], 2) / numIndividuals;
			objectiveDeviations[1] += Math.pow(i.getConnectivity() - objectiveMeans[1], 2) / numIndividuals;
			objectiveDeviations[2] += Math.pow(i.getOverallDeviation() - objectiveMeans[2], 2) / numIndividuals;
		}
		objectiveDeviations[0] = (float) Math.sqrt(objectiveDeviations[0]);
		objectiveDeviations[1] = (float) Math.sqrt(objectiveDeviations[1]);
		objectiveDeviations[2] = (float) Math.sqrt(objectiveDeviations[2]);
	}
	
	/**
	 * Normalizes an edge value
	 * @param val - A raw edge value
	 * @return the normalized edge value
	 */
	public float normalizeEdgeValue(float val) {
		return (val - objectiveMeans[0]) / objectiveDeviations[0];
	}
	
	/**
	 * Normalizes a connectivity value
	 * @param val - A raw connectivity value
	 * @return the normalized connectivity value
	 */
	public float normalizeConnectivity(float val) {
		return (val - objectiveMeans[1]) / objectiveDeviations[1];
	}

	/**
	 * Normalizes an overall deviation value
	 * @param val - A raw overall deviation value
	 * @return the normalized overall deviation value
	 */
	public float normalizeOverallDeviation(float val) {
		return (val - objectiveMeans[2]) / objectiveDeviations[2];
	}
}

