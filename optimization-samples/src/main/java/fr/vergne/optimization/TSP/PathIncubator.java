package fr.vergne.optimization.TSP;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.vergne.optimization.TSP.path.AbstractPath;
import fr.vergne.optimization.TSP.path.AbstractPath.Transition;
import fr.vergne.optimization.TSP.path.Location;
import fr.vergne.optimization.incubator.impl.ExperimentalIncubator;

public class PathIncubator<Path extends AbstractPath> extends
		ExperimentalIncubator<Path> {

	public PathIncubator() {
		super(new Comparator<Path>() {

			@Override
			public int compare(Path p1, Path p2) {
				Collection<Transition> t1 = p1.getTransitions();
				Set<Location> l1 = new HashSet<Location>();
				Double d1 = feed(t1, l1);

				Collection<Transition> t2 = p2.getTransitions();
				Set<Location> l2 = new HashSet<Location>();
				Double d2 = feed(t2, l2);

				int comparison = Integer.valueOf(l2.size())
						.compareTo(l1.size());
				comparison = comparison != 0 ? comparison : d1.compareTo(d2);
				comparison = comparison != 0 ? comparison : p1.getLength()
						.compareTo(p2.getLength());
				return comparison;
			}

			private double feed(Collection<Transition> transitions,
					Set<Location> locations) {
				Map<Location, Integer> counters = new HashMap<Location, Integer>();
				for (Transition transition : transitions) {
					for (Location location : Arrays.asList(transition.getL1(),
							transition.getL2())) {
						locations.add(location);
						counters.put(location, getCount(counters, location) + 1);
					}
				}
				double distance = 0;
				for (int count : counters.values()) {
					int delta = 2 - count;
					distance += delta * delta;
				}
				return distance;
			}

			private int getCount(Map<Location, Integer> m1, Location a) {
				Integer countA = m1.get(a);
				return countA == null ? 0 : countA;
			}
		});
	}

	int generatedIndividuals = 0;

	public int getGeneratedIndividuals() {
		return generatedIndividuals;
	}

	@Override
	public void incubate() {
		super.incubate();
		generatedIndividuals++;
	}

}
