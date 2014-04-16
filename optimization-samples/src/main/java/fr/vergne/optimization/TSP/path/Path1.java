package fr.vergne.optimization.TSP.path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Path1 extends AbstractPath {

	private final List<Location> locations;

	public Path1(List<Location> locations) {
		this.locations = Collections.unmodifiableList(new ArrayList<Location>(locations));
	}

	public List<Location> getLocations() {
		return locations;
	}

	@Override
	public Collection<Transition> getTransitions() {
		return explode(locations);
	}
}
