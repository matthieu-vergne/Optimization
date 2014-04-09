package fr.vergne.optimization.TSP.path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Path1 extends AbstractPath {

	private final ArrayList<Location> locations;

	public Path1(List<Location> locations) {
		this.locations = new ArrayList<Location>(locations);
	}

	public List<Location> getLocations() {
		return locations;
	}

	@Override
	public Collection<Transition> getTransitions() {
		return explode(locations);
	}
}
