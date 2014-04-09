package fr.vergne.optimization.TSP.path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Path2 extends AbstractPath {

	private final List<Integer> indexes;
	private final List<Location> locations;

	public Path2(List<Location> references, List<Integer> indexes) {
		this.indexes = new ArrayList<Integer>(indexes);
		this.locations = new LinkedList<Location>();
		List<Location> remaining = new LinkedList<Location>(references);
		Iterator<Integer> iterator = indexes.iterator();
		while (iterator.hasNext()) {
			locations.add(remaining.remove((int) iterator.next()));
		}
		locations.add(remaining.remove(0));
	}

	public List<Integer> getIndexes() {
		return indexes;
	}

	@Override
	public Collection<Transition> getTransitions() {
		return explode(locations);
	}
}
