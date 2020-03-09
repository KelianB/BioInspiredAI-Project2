package ga.segmentation;


import java.util.*;
import java.util.stream.Collectors;
import ga.IIndividual;
import ga.GeneticAlgorithm;
import ga.SimplePopulation;

public class Population extends SimplePopulation {
	public Population(GeneticAlgorithm ga) {
		super(ga);
	}

	public ArrayList<ArrayList<Individual>> fastNonDominatedSorting (Population population) {
		//list of fronts
		ArrayList<ArrayList<Individual>> fronts = new ArrayList<ArrayList<Individual>>();
		//HashMap of ni
		HashMap<Individual, Integer> nindexes = new HashMap<Individual, Integer>();
		//HashMap of Si
		HashMap<Individual, ArrayList<Individual>> Sindexes = new HashMap<Individual, ArrayList<Individual>>();

		//first front : none of the individuals in it is dominated
		ArrayList<Individual> F1 = new ArrayList<Individual>();

		for (Individual i : this.getIndividuals()) {
			int ni=0;
			ArrayList<Individual> Si = new ArrayList<Individual>();

			for (Individual j : this.getIndividuals()) {
				//j dominates i
				if (i.getEdgeValue() < j.getEdgeValue() && i.getConnectivity() < j.getConnectivity() && i.getOverallDeviation() < j.getOverallDeviation()) {
					ni++;
				} else if (i.getEdgeValue() > j.getEdgeValue() && i.getConnectivity() > j.getConnectivity() && i.getOverallDeviation() > j.getOverallDeviation()) {
					Si.add(j);
				}
			}
			if (ni==0) {
				F1.add(i);
			}
			nindexes.put(i,ni);
			Sindexes.put(i,Si);
		}
		fronts.add(F1);

		//other fronts
		ArrayList<Individual> currentFi = new ArrayList<Individual>(F1);
		while (!currentFi.isEmpty()) {
			ArrayList<Individual> Q = new ArrayList<Individual>();
			for (Individual i : currentFi) {
				for (Individual j : Sindexes.get(i)) {
					nindexes.put(j,nindexes.get(j)-1) ;
					if (nindexes.get(j)==0) {
						Q.add(j);
					}
				}
			}
			currentFi = new ArrayList<Individual>(Q);;
			fronts.add(currentFi);
		}
		return fronts;
	}

	class SortByEdgeValue implements Comparator<Individual>
	{
		public int compare(Individual a, Individual b)
		{
			if (a.getEdgeValue() > b.getEdgeValue()) {
				return -1;
			}
			else if (a.getEdgeValue() < b.getEdgeValue()) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}

	class SortByConnectivity implements Comparator<Individual>
	{
		public int compare(Individual a, Individual b)
		{
			if (a.getConnectivity()> b.getConnectivity()) {
				return -1;
			}
			else if (a.getConnectivity() < b.getConnectivity()) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}

	class SortByOverallDeviation implements Comparator<Individual>
	{
		public int compare(Individual a, Individual b)
		{
			if (a.getOverallDeviation() > b.getOverallDeviation()) {
				return -1;
			}
			else if (a.getOverallDeviation() < b.getOverallDeviation()) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}

	public static <K, V extends Comparable<V>> Map<K, V>
	sortByValues(final Map<K, V> map) {
		Comparator<K> valueComparator =
				new Comparator<K>() {
					public int compare(K k1, K k2) {
						int compare =
								map.get(k1).compareTo(map.get(k2));
						if (compare == 0)
							return 1;
						else
							return compare;
					}
				};

		Map<K, V> sortedByValues =
				new TreeMap<K, V>(valueComparator);
		sortedByValues.putAll(map);
		return sortedByValues;
	}


	public Map<Individual, Double> crowdingDistance (List<List<Individual>> fronts) {

		SortedMap<Individual, Double> populationCrowdingDistance = new TreeMap<Individual, Double>();

		//for each front, calculate the crowding distance of the individuals inside
		for (List<Individual> front : fronts) {

			for (int i = 0 ; i<front.size() ;i++ ) {
				populationCrowdingDistance.put(front.get(i), 0.0);
			}

			//criterion edgeValue
			front.sort(new SortByEdgeValue());

			populationCrowdingDistance.put(front.get(0), Double.POSITIVE_INFINITY);
			populationCrowdingDistance.put(front.get(front.size()-1), Double.POSITIVE_INFINITY);

			double edgeValueMax = front.get(front.size()-1).getEdgeValue();
			double edgeValueMin = front.get(0).getEdgeValue();

			for (int i=1 ; i< front.size()-1 ; i++) {
				populationCrowdingDistance.put(front.get(i), populationCrowdingDistance.get(front.get(i))  + (front.get(i+1).getEdgeValue() - front.get(i-1).getEdgeValue())/(edgeValueMax-edgeValueMin));
			}

			//criterion connectivity
			front.sort(new SortByConnectivity());

			populationCrowdingDistance.put(front.get(0), Double.POSITIVE_INFINITY);
			populationCrowdingDistance.put(front.get(front.size()-1), Double.POSITIVE_INFINITY);

			edgeValueMax = front.get(front.size()-1).getConnectivity();
			edgeValueMin = front.get(0).getConnectivity();

			for (int i=1 ; i< front.size()-1 ; i++) {
				populationCrowdingDistance.put(front.get(i), populationCrowdingDistance.get(front.get(i))  + (front.get(i+1).getConnectivity() - front.get(i-1).getConnectivity())/(edgeValueMax-edgeValueMin));
			}

			//criterion overallDeviation
			front.sort(new SortByOverallDeviation());

			populationCrowdingDistance.put(front.get(0), Double.POSITIVE_INFINITY);
			populationCrowdingDistance.put(front.get(front.size()-1), Double.POSITIVE_INFINITY);

			edgeValueMax = front.get(front.size()-1).getOverallDeviation();
			edgeValueMin = front.get(0).getOverallDeviation();

			for (int i=1 ; i< front.size()-1 ; i++) {
				populationCrowdingDistance.put(front.get(i), populationCrowdingDistance.get(front.get(i))  + (front.get(i+1).getOverallDeviation() - front.get(i-1).getOverallDeviation())/(edgeValueMax-edgeValueMin));
			}
		}

		Map sortedMap = sortByValues(populationCrowdingDistance);

		return sortedMap;

	}

	public Individual binaryTournamentSelection (ArrayList<ArrayList<Individual>> fronts, Map<Individual, Double> sortedMap) {

		int frontParent1 = (int) Math.random()*(fronts.size() +1);
		Individual parent1 = fronts.get(frontParent1).get((int) Math.random()*(fronts.get(frontParent1).size() +1));

		int frontParent2 = (int) Math.random()*(fronts.size() +1);
		Individual parent2 = fronts.get(frontParent1).get((int) Math.random()*(fronts.get(frontParent1).size() +1));

		if (frontParent1 < frontParent2) {
			return parent1;
		}
		else if (frontParent1 > frontParent2) {
			return parent2;
		}
		else {
			if (sortedMap.get(parent1) < sortedMap.get(parent2)) {
				return parent1;
			} else {
				return parent2;
			}
		}
	}
}

