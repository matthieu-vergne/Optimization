package fr.vergne.optimization.TSP.path;

import java.util.LinkedList;
import java.util.List;

public class Path2Test extends AbstractPathTest<Path2> {

	@Override
	protected Path2 generatePath(List<Location> locations) {
		List<Integer> choices = new LinkedList<Integer>();
		for (int i = 0; i < locations.size() - 1; i++) {
			choices.add(0);
		}
		return new Path2(locations, choices);
	}

}
