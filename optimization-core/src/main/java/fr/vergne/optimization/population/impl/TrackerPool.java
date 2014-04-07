package fr.vergne.optimization.population.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import fr.vergne.optimization.generator.Generator;
import fr.vergne.optimization.generator.Mutator;
import fr.vergne.optimization.population.PopulationManager;
import fr.vergne.optimization.population.impl.TrackerPool.Tracker;

public class TrackerPool<Individual> implements Iterable<Tracker<Individual>>,
		PopulationManager<Individual> {
	private final Collection<Tracker<Individual>> trackers = new LinkedList<Tracker<Individual>>();
	private final Competition<Individual> competition;

	@Override
	public Collection<Individual> getPopulation() {
		Collection<Individual> population = new LinkedList<Individual>();
		for (Tracker<Individual> tracker : trackers) {
			population.add(tracker.getRepresentative());
		}
		return population;
	}

	public TrackerPool(Competition<Individual> competition) {
		this.competition = competition;
	}

	@Override
	public Iterator<Tracker<Individual>> iterator() {
		return trackers.iterator();
	}

	@Override
	public void push(Individual individual) {
		trackers.add(new Tracker<Individual>(competition, individual));
	}

	public <Input> void push(Generator<Input, Individual> starter, Input input) {
		trackers.add(new Tracker<Individual>(competition, starter, input));
	}

	@Override
	public Iterator<Individual> getBest() {
		return new Iterator<Individual>() {

			private final List<Individual> remaining = new LinkedList<Individual>(
					getPopulation());

			@Override
			public boolean hasNext() {
				return !remaining.isEmpty();
			}

			@Override
			public Individual next() {
				Iterator<Individual> iterator = remaining.iterator();
				Individual best = iterator.next();
				while (iterator.hasNext()) {
					best = competition.compete(best, iterator.next());
				}
				remaining.remove(best);
				return best;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public static class Tracker<Individual> {
		private final Map<Mutator<Individual>, Individual> neighborReferences = new HashMap<Mutator<Individual>, Individual>();
		private final Map<Mutator<Individual>, Integer> neighborLoops = new HashMap<Mutator<Individual>, Integer>();
		private final Competition<Individual> competition;
		private Individual representative;
		private final Logger logger = Logger.getAnonymousLogger();

		public <Input> Tracker(Competition<Individual> competition,
				Generator<Input, Individual> starter, Input input) {
			this.competition = competition;
			representative = starter.generates(input);
		}

		public Tracker(Competition<Individual> competition,
				Individual individual) {
			this.competition = competition;
			representative = individual;
		}

		public double getOptimalityWith(Mutator<Individual> mutator) {
			Integer loops = neighborLoops.get(mutator);
			if (!mutator.isApplicableOn(representative)) {
				return 1;
			} else if (loops == null || loops == 0) {
				return 0;
			} else {
				// double victories = getVictoriesAgainst(mutator);
				// double cycleLength = victories / loops;
				// double exp = Math.exp(loops);
				// double p = exp / (1 + exp);
				// double o = p * 2 - 1;
				// logger.finest("V/L=" + victories + "/" + loops + "="
				// + cycleLength + ", EXP=" + exp + ", P=" + p + ", O="
				// + o);
				return loops / (loops + 1);
			}
		}

		public <Input> void compete(Mutator<Individual> mutator) {
			Individual challenger = mutator.generates(representative);
			Individual winner = competition.compete(representative, challenger);
			logger.info("Competition: " + representative + " VS " + challenger
					+ " => winner: " + winner);
			if (winner.equals(representative)) {
				if (!neighborReferences.containsKey(mutator)) {
					neighborReferences.put(mutator, challenger);
					neighborLoops.put(mutator, 0);
				} else if (neighborReferences.get(mutator) == null) {
					neighborReferences.put(mutator, challenger);
				} else if (neighborReferences.get(mutator).equals(challenger)) {
					neighborLoops.put(mutator, neighborLoops.get(mutator) + 1);
					neighborReferences.put(mutator, null);
				} else {
					// not a reference neighbor to consider
				}
			} else {
				representative = winner;
				neighborReferences.clear();
				neighborLoops.clear();
			}

		}

		public Individual getRepresentative() {
			return representative;
		}

		@Override
		public String toString() {
			String sep = ", ";
			String opt = "";
			for (Mutator<Individual> mutator : neighborReferences.keySet()) {
				opt += sep + mutator + "=" + getOptimalityWith(mutator);
			}
			return representative
					+ (opt.isEmpty() ? "" : " (" + opt.substring(sep.length())
							+ ")");
		}
	}

	/**
	 * A {@link Competition} aims at selecting a winner among two
	 * {@link Competitor}s. This is a strict {@link Competition}, in the sense
	 * that exactly one of the two {@link Competitor}s should be selected.
	 * 
	 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
	 * 
	 * @param <Competitor>
	 */
	public static interface Competition<Competitor> {
		/**
		 * 
		 * @param competitor1
		 *            a first {@link Competitor}
		 * @param competitor2
		 *            a second {@link Competitor}
		 * @return the winner of the {@link Competition}
		 */
		public Competitor compete(Competitor competitor1, Competitor competitor2);
	}
}
