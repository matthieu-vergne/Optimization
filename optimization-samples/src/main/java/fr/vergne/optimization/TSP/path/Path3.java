package fr.vergne.optimization.TSP.path;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class Path3 extends AbstractPath {

	private final Boolean[] genes;
	private final List<Location> reference;

	public Path3(List<Location> reference, Boolean[] genes) {
		this.reference = reference;
		this.genes = genes;
	}

	public Path3(List<Location> reference, Collection<Transition> transitions) {
		this.reference = reference;
		this.genes = new Boolean[geneSize(reference)];
		Iterator<Transition> chain = transitionChain(reference);
		for (int i = 0; i < genes.length; i++) {
			Transition transition = chain.next();
			genes[i] = transitions.contains(transition);
		}
	}

	public static Iterator<Transition> transitionChain(
			final List<Location> locations) {
		return new Iterator<Transition>() {

			private int i1 = 0;
			private int i2 = 0;

			@Override
			public boolean hasNext() {
				return i1 < locations.size() - 2 || i2 < locations.size() - 1;
			}

			@Override
			public Transition next() {
				if (hasNext()) {
					if (i2 == locations.size() - 1) {
						i1++;
						i2 = i1 + 1;
					} else {
						i2++;
					}
					return new Transition(locations.get(i1), locations.get(i2));
				} else {
					throw new NoSuchElementException();
				}
				//
				// if (l2 == null) {
				// LinkedList<Location> remaining = new LinkedList<Location>(
				// locations);
				// remaining.remove(from);
				// l2 = remaining.iterator();
				// } else if (!l2.hasNext()) {
				// from = l1.next();
				// LinkedList<Location> remaining = new LinkedList<Location>(
				// locations);
				// remaining.remove(from);
				// l2 = remaining.iterator();
				// } else {
				// // not extreme case
				// }
				// return new Transition(from, l2.next());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

	public Boolean[] getGenes() {
		return genes;
	}

	@Override
	public Collection<Transition> getTransitions() {
		Collection<Transition> transitions = new LinkedList<Transition>();
		Iterator<Transition> transitionIterator = transitionChain(reference);
		for (int i = 0; transitionIterator.hasNext(); i++) {
			Transition transition = transitionIterator.next();
			Boolean isActive = genes[i];
			if (isActive) {
				transitions.add(transition);
			} else {
				// not used
			}
		}
		return transitions;
	}

	public static int geneSize(List<Location> locations) {
		return locations.size() * (locations.size() - 1) / 2;
	}

}
