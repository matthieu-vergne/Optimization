package fr.vergne.optimization.TSP.path;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import fr.vergne.optimization.TSP.path.AbstractPath.Transition;

public class Path3Test extends AbstractPathTest<Path3> {

	@Override
	protected Path3 generatePath(List<Location> locations) {
		Iterator<Transition> chain = Path3.transitionChain(locations);
		Boolean[] genes = new Boolean[Path3.geneSize(locations)];
		for (int i = 0; i < genes.length; i++) {
			Transition transition = chain.next();
			int indexFrom = locations.indexOf(transition.getL1());
			int indexTo = locations.indexOf(transition.getL1());
			genes[i] = (indexTo - indexFrom) % locations.size() == 1;
		}
		return new Path3(locations, genes);
	}

	@Test
	public void testChain() {
		Location l1 = new Location(1, 0);
		Location l2 = new Location(2, 0);
		Location l3 = new Location(3, 0);
		Location l4 = new Location(4, 0);
		List<Location> locations = Arrays.asList(l1, l2, l3, l4);

		Iterator<Transition> iterator = Path3.transitionChain(locations);
		assertEquals(new Transition(l1, l2), iterator.next());
		assertEquals(new Transition(l1, l3), iterator.next());
		assertEquals(new Transition(l1, l4), iterator.next());

		// assertEquals(new Transition(l2, l1), iterator.next());
		assertEquals(new Transition(l2, l3), iterator.next());
		assertEquals(new Transition(l2, l4), iterator.next());

		// assertEquals(new Transition(l3, l1), iterator.next());
		// assertEquals(new Transition(l3, l2), iterator.next());
		assertEquals(new Transition(l3, l4), iterator.next());

		// assertEquals(new Transition(l4, l1), iterator.next());
		// assertEquals(new Transition(l4, l2), iterator.next());
		// assertEquals(new Transition(l4, l3), iterator.next());

		assertFalse(iterator.hasNext());
	}
}
