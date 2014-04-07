package fr.vergne.optimization.population.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import fr.vergne.optimization.generator.Mutator;
import fr.vergne.optimization.population.PopulationManager;
import fr.vergne.optimization.population.impl.OptimizerPool.Optimizer;

/**
 * An {@link OptimizerPool} is a simple collection of {@link Optimizer}s with a
 * common {@link Competition} process. This commonality allows to use the same
 * evaluation process between the {@link Individual}s of each {@link Optimizer},
 * providing a consistent comparison. It is especially used to sort the
 * representatives of the different {@link Optimizer}s for the method
 * {@link #getBest()}.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 * @param <Individual>
 */
public class OptimizerPool<Individual> implements
		Iterable<Optimizer<Individual>>, PopulationManager<Individual> {
	private final Collection<Optimizer<Individual>> trackers = new LinkedList<Optimizer<Individual>>();
	private final Competition<Individual> competition;

	public OptimizerPool(Competition<Individual> competition) {
		this.competition = competition;
	}

	@Override
	public Collection<Individual> getPopulation() {
		Collection<Individual> population = new LinkedList<Individual>();
		for (Optimizer<Individual> tracker : trackers) {
			population.add(tracker.getRepresentative());
		}
		return population;
	}

	@Override
	public Iterator<Optimizer<Individual>> iterator() {
		return trackers.iterator();
	}

	@Override
	public void push(Individual individual) {
		trackers.add(new Optimizer<Individual>(competition, individual));
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

	/**
	 * An {@link Optimizer} aims at finding an optimal {@link Individual} (local
	 * optimum) by exploiting {@link Mutator}s to find better solutions. Each
	 * {@link Mutator} defines a specific topology over the space of
	 * {@link Individual}s, and using one {@link Mutator} while keeping only the
	 * best {@link Individual} generated allows to converge quickly to such
	 * optimal {@link Individual}. By using several {@link Mutator}s, a broader
	 * topology is involved, increasing the chance to find better
	 * {@link Individual}s, although it does not ensure to find a global
	 * optimum. More details are provided in the documentation of
	 * {@link #getOptimalityWith(Mutator)}.
	 * 
	 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
	 * 
	 * @param <Individual>
	 */
	public static class Optimizer<Individual> {
		private final Map<Mutator<Individual>, Individual> neighborReferences = new HashMap<Mutator<Individual>, Individual>();
		private final Map<Mutator<Individual>, Integer> neighborLoops = new HashMap<Mutator<Individual>, Integer>();
		private final Competition<Individual> competition;
		private Individual representative;
		private final Logger logger = Logger.getAnonymousLogger();

		public Optimizer(Competition<Individual> competition,
				Individual individual) {
			this.competition = competition;
			representative = individual;
		}

		/**
		 * This method aims at evaluating the degree of optimality of the
		 * current representative of this {@link Optimizer}. This optimality
		 * corresponds to the probability to <b>not</b> be improved by a given
		 * {@link Mutator}.<br/>
		 * <br/>
		 * More formally, each {@link Mutator} generates a mutant, among a set
		 * of possible mutants which depends on the original {@link Individual}
		 * provided. This set of mutants can be described as direct neighbors of
		 * the original {@link Individual}, where the neighboring is defined by
		 * the {@link Mutator} used. Thus, the {@link Mutator} acts similarly to
		 * a <a
		 * href="http://en.wikipedia.org/wiki/Topological_space">topology</a>
		 * over the set of {@link Individual}s, excepted that the
		 * {@link Individual} is not necessarily in its own neighborhoods (i.e.
		 * one of the possible mutants).<br/>
		 * <br/>
		 * Given that a {@link Mutator} is used to find better
		 * {@link Individual}s within this neighborhood, an {@link Individual}
		 * is optimal regarding this {@link Mutator} if no neighbor is better.
		 * An optimality of 1 means that no neighbor is better, while a value of
		 * 0 means that there is no information supporting such optimality.
		 * 
		 * @param mutator
		 *            the {@link Mutator} to evaluate
		 * @return the degree of optimality of the current {@link Optimizer}
		 *         representative for the given {@link Mutator}
		 */
		public double getOptimalityWith(Mutator<Individual> mutator) {
			Integer loops = neighborLoops.get(mutator);
			if (!mutator.isApplicableOn(representative)) {
				return 1;
			} else if (loops == null || loops == 0) {
				return 0;
			} else {
				return (double) (loops - 1) / loops;
			}
		}

		/**
		 * 
		 * @param challengerGenerator
		 *            the {@link Mutator} used to generate the challenger which
		 *            will compete the representative of this {@link Optimizer}
		 */
		public <Input> void compete(Mutator<Individual> challengerGenerator) {
			Individual challenger = challengerGenerator
					.generates(representative);
			Individual winner = competition.compete(representative, challenger);
			logger.info("Competition: " + representative + " VS " + challenger
					+ " => winner: " + winner);
			if (winner.equals(representative)) {
				if (!neighborReferences.containsKey(challengerGenerator)) {
					neighborReferences.put(challengerGenerator, challenger);
					neighborLoops.put(challengerGenerator, 0);
				} else if (neighborReferences.get(challengerGenerator) == null) {
					neighborReferences.put(challengerGenerator, challenger);
				} else if (neighborReferences.get(challengerGenerator).equals(
						challenger)) {
					neighborLoops.put(challengerGenerator,
							neighborLoops.get(challengerGenerator) + 1);
					neighborReferences.put(challengerGenerator, null);
				} else {
					// not a reference neighbor to consider
				}
			} else {
				representative = winner;
				neighborReferences.clear();
				neighborLoops.clear();
			}

		}

		/**
		 * 
		 * @return the best {@link Individual} identified by this
		 *         {@link Optimizer}
		 */
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
