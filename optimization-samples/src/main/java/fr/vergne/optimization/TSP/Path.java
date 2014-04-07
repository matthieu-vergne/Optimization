package fr.vergne.optimization.TSP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Path implements Comparable<Path> {

	private final ArrayList<Location> locations;

	public Path(List<Location> locations) {
		this.locations = new ArrayList<Location>(locations);
	}

	@Override
	public int compareTo(Path i) {
		Path p = (Path) i;
		return getLength().compareTo(p.getLength());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof Path) {
			List<Location> g1 = getLocations();
			List<Location> g2 = ((Path) obj).getLocations();

			if (g1.size() != g2.size()) {
				return false;
			} else {
				Collections.rotate(g1, -g1.indexOf(g2.get(0)));
				if (g1.indexOf(g2.get(1)) == g1.size() - 1) {
					Collections.rotate(g1, -1);
					Collections.reverse(g1);
				} else {
					// no chance to face a wrong direction
				}
				return Collections.indexOfSubList(g1, g2) == 0;
			}
		} else {
			return false;
		}
	}

	public List<Location> getLocations() {
		return locations;
	}

	Double length = null;

	public Double getLength() {
		if (length == null) {
			length = 0d;
			Location previous = locations.get(locations.size() - 1);
			for (Location current : locations) {
				double dx = current.getX() - previous.getX();
				double dy = current.getY() - previous.getY();
				length += Math.sqrt(dx * dx + dy * dy);
				// length += Math.hypot(dx, dy);
				previous = current;
			}
		} else {
			// already computed
		}
		return length;
	}

	@Override
	public String toString() {
		return "" + getLength();// + locations.toString();
	}
}
