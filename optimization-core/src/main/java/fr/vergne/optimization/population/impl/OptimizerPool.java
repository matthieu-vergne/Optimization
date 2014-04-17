package fr.vergne.optimization.population.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import fr.vergne.logging.LoggerConfiguration;
import fr.vergne.optimization.generator.InformedMutator;
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
	private final Collection<Optimizer<Individual>> optimizers = new LinkedList<Optimizer<Individual>>();
	private Competition<Individual> competition;
	private OptimalityChecker<Individual> optimalityCheker = null;

	public OptimizerPool(Competition<Individual> competition) {
		setCompetition(competition);
	}

	public void setCompetition(Competition<Individual> competition) {
		if (!this.competition.equals(competition)) {
			this.competition = competition;
			for (Optimizer<Individual> optimizer : optimizers) {
				optimizer.reset();
			}
		} else {
			// ignore the change to not loose valuable data
		}
	}

	public Competition<Individual> getCompetition() {
		return competition;
	}

	public void setOptimalityCheker(
			OptimalityChecker<Individual> optimalityCheker) {
		this.optimalityCheker = optimalityCheker;
	}

	public OptimalityChecker<Individual> getOptimalityCheker() {
		return optimalityCheker;
	}

	@Override
	public Collection<Individual> getPopulation() {
		Collection<Individual> population = new LinkedList<Individual>();
		for (Optimizer<Individual> optimizer : optimizers) {
			population.add(optimizer.getRepresentative());
		}
		return population;
	}

	/**
	 * Remove all the current {@link Optimizer}s.
	 */
	public void clear() {
		optimizers.clear();
	}

	@Override
	public Iterator<Optimizer<Individual>> iterator() {
		return optimizers.iterator();
	}

	@Override
	public void push(Individual individual) {
		optimizers.add(new Optimizer<Individual>(individual, this));
	}

	public void remove(Individual individual) {
		Iterator<Optimizer<Individual>> iterator = optimizers.iterator();
		while (iterator.hasNext()) {
			Optimizer<Individual> optimizer = iterator.next();
			if (optimizer.getRepresentative().equals(individual)) {
				iterator.remove();
				return;
			} else {
				continue;
			}
		}
	}

	public int size() {
		return optimizers.size();
	}

	@Override
	public Iterator<Individual> getBest() {
		return new Iterator<Individual>() {

			private final List<Individual> remaining = new LinkedList<Individual>(
					getPopulation());
			private Individual best;

			@Override
			public boolean hasNext() {
				return !remaining.isEmpty();
			}

			@Override
			public Individual next() {
				Iterator<Individual> iterator = remaining.iterator();
				best = iterator.next();
				while (iterator.hasNext()) {
					Individual winner = competition.compete(best, iterator.next());
					best = winner == null ? best : winner;
				}
				remaining.remove(best);
				return best;
			}

			@Override
			public void remove() {
				OptimizerPool.this.remove(best);
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
		private final Map<InformedMutator<Individual>, Integer> neighborCounts = new HashMap<InformedMutator<Individual>, Integer>();
		private final OptimizerPool<Individual> parentPool;
		private Individual representative;
		public static final Logger logger = LoggerConfiguration.getSimpleLogger();

		public Optimizer(Individual individual, OptimizerPool<Individual> parent) {
			representative = individual;
			parentPool = parent;
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
			if (!mutator.isApplicableOn(representative)
					|| parentPool.getOptimalityCheker() != null
					&& parentPool.getOptimalityCheker().isOptimal(
							representative)) {
				return 1;
			} else if (mutator instanceof InformedMutator) {
				InformedMutator<Individual> mut = (InformedMutator<Individual>) mutator;
				Integer counts = neighborCounts.get(mut);
				counts = counts == null ? 0 : counts;
				if (mut.isNeighboringSizeStrict()) {
					return (double) counts / mut.getNeighboringLimit();
				} else {
					double exp = Math.exp((double) counts
							/ mut.getNeighboringLimit());
					return exp / (1 + exp) * 2 - 1;
				}
			} else {
				Integer loops = neighborLoops.get(mutator);
				if (loops == null || loops == 0) {
					return 0;
				} else {
					return (double) (loops - 1) / loops;
				}
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
			Competition<Individual> competition = parentPool.getCompetition();
			if (competition == null) {
				throw new IllegalStateException(
						"No competition operator has been provided.");
			} else {
				// computation can be done
			}
			Individual winner = competition.compete(representative, challenger);
			logger.info("Competition: " + representative + " VS " + challenger
					+ " => winner: " + winner);
			winner = winner == null ? representative : winner;
			if (challengerGenerator instanceof InformedMutator) {
				if (winner.equals(representative)) {
					InformedMutator<Individual> mutator = (InformedMutator<Individual>) challengerGenerator;
					if (!neighborCounts.containsKey(challengerGenerator)) {
						neighborCounts.put(mutator, 0);
					} else {
						neighborCounts.put(mutator,
								neighborCounts.get(mutator) + 1);
					}
				} else {
					neighborCounts.clear();
				}
			} else {
				if (winner.equals(representative)) {
					if (!neighborReferences.containsKey(challengerGenerator)) {
						neighborReferences.put(challengerGenerator, challenger);
						neighborLoops.put(challengerGenerator, 0);
					} else if (neighborReferences.get(challengerGenerator) == null) {
						neighborReferences.put(challengerGenerator, challenger);
					} else if (neighborReferences.get(challengerGenerator)
							.equals(challenger)) {
						neighborLoops.put(challengerGenerator,
								neighborLoops.get(challengerGenerator) + 1);
						neighborReferences.put(challengerGenerator, null);
					} else {
						// not a reference neighbor to consider
					}
				} else {
					neighborReferences.clear();
					neighborLoops.clear();
				}
			}
			representative = winner;
		}

		/**
		 * 
		 * @return the best {@link Individual} identified by this
		 *         {@link Optimizer}
		 */
		public Individual getRepresentative() {
			return representative;
		}

		/**
		 * Clear all the statistics as if this {@link Optimizer} has just been
		 * created.
		 */
		public void reset() {
			neighborReferences.clear();
			neighborLoops.clear();
			neighborCounts.clear();
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
	 * {@link Competitor}s.
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
		 * @return the winner of the {@link Competition}, <code>null</code> if
		 *         there is no winner
		 */
		public Competitor compete(Competitor competitor1, Competitor competitor2);
	}

	/**
	 * An {@link OptimalityChecker} allows to assess the optimality of an
	 * {@link Individual}.
	 * 
	 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
	 * 
	 * @param <Individual>
	 */
	public static interface OptimalityChecker<Individual> {
		/**
		 * 
		 * @return <code>true</code> if the {@link Individual} is ensured to be
		 *         an optimum (global or local), <code>false</code> otherwise
		 */
		public boolean isOptimal(Individual individual);
	}
}
