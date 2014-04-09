package fr.vergne.optimization.TSP.path;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractPath implements Comparable<AbstractPath> {

	@Override
	public int compareTo(AbstractPath i) {
		AbstractPath p = (AbstractPath) i;
		int compareTo = getLength().compareTo(p.getLength());
		return compareTo == 0 ? equals(p) ? 0 : -1 : compareTo;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof AbstractPath) {
			AbstractPath p = (AbstractPath) obj;
			if (Math.abs(getLength() - p.getLength()) > 0.00001) {
				return false;
			} else {
				Collection<Transition> t1 = new LinkedList<Transition>(
						getTransitions());
				Collection<Transition> t2 = p.getTransitions();
				t1.removeAll(t2);
				return t1.isEmpty();
			}
		} else {
			return false;
		}
	}

	Double length = null;

	public Double getLength() {
		if (length == null) {
			length = 0.0;
			for (Transition transition : getTransitions()) {
				Location from = transition.getL1();
				Location to = transition.getL2();
				double dx = from.getX() - to.getX();
				double dy = from.getY() - to.getY();
				length += Math.sqrt(dx * dx + dy * dy);
			}
		} else {
			// already computed
		}
		return length;
	}

	@Override
	public String toString() {
		return "" + getLength();
	}

	public abstract Collection<Transition> getTransitions();

	public static class Transition {
		private final Location l1;
		private final Location l2;

		public Transition(Location l1, Location l2) {
			if (l1 == null) {
				throw new NullPointerException(
						"The first parameter cannot be null.");
			} else if (l2 == null) {
				throw new NullPointerException(
						"The second parameter cannot be null.");
			} else {
				this.l1 = l1;
				this.l2 = l2;
			}
		}

		public Location getL1() {
			return l1;
		}

		public Location getL2() {
			return l2;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} else if (obj instanceof Transition) {
				Transition p = (Transition) obj;
				return p.l1.equals(l1) && p.l2.equals(l2) || p.l1.equals(l2)
						&& p.l2.equals(l1);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return l1.hashCode() * l2.hashCode();
		}

		@Override
		public String toString() {
			return l1 + "->" + l2;
		}
	}

	public static Collection<Transition> explode(List<Location> locations) {
		List<Transition> transitions = new LinkedList<Transition>();
		Iterator<Location> iterator = locations.iterator();
		Location from = iterator.next();
		Location first = from;
		while (iterator.hasNext()) {
			Location to = iterator.next();
			transitions.add(new Transition(from, to));
			from = to;
		}
		transitions.add(new Transition(from, first));
		return transitions;
	}

}
