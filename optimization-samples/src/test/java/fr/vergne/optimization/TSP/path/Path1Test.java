package fr.vergne.optimization.TSP.path;

import java.util.List;

public class Path1Test extends AbstractPathTest<Path1> {

	@Override
	protected Path1 generatePath(List<Location> locations) {
		return new Path1(locations);
	}

}
