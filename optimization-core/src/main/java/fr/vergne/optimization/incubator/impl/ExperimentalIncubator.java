package fr.vergne.optimization.incubator.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.vergne.optimization.generator.Explorator;
import fr.vergne.optimization.generator.Generator;
import fr.vergne.optimization.generator.Mutator;
import fr.vergne.optimization.incubator.Incubator;
import fr.vergne.optimization.population.Evaluator;
import fr.vergne.optimization.population.impl.OptimizerPool;
import fr.vergne.optimization.population.impl.OptimizerPool.Competition;
import fr.vergne.optimization.population.impl.OptimizerPool.Optimizer;

//FIXME document
public class ExperimentalIncubator<Individual> implements Incubator<Individual> {

	private final OptimizerPool<Individual> optimizerPool;
	private final Collection<Mutator<Individual>> mutators = new LinkedList<Mutator<Individual>>();
	private final Collection<Explorator<Individual>> explorators = new LinkedList<Explorator<Individual>>();
	private final Competition<Individual> competition;
	private final Random rand = new Random();
	private final Logger logger = Logger.getAnonymousLogger();
	private boolean hasEvolved;

	public <Value extends Comparable<Value>> ExperimentalIncubator(
			final Evaluator<Individual, Value> evaluator) {
		logger.setLevel(Level.ALL);
		this.competition = new Competition<Individual>() {

			@Override
			public Individual compete(Individual competitor1,
					Individual competitor2) {
				Value value1 = evaluator.evaluate(competitor1);
				Value value2 = evaluator.evaluate(competitor2);
				if (value1.compareTo(value2) < 0) {
					return competitor1;
				} else if (value1.compareTo(value2) > 0) {
					return competitor2;
				} else if (value1.compareTo(value2) == 0) {
					/*
					 * TODO Avoid convergence hiding: if competitors have the
					 * exactly same value, we can spend time switching between
					 * each other in a sequence of competitions. In the case
					 * where better solutions are in their neighboring, it
					 * allows to visit different parts of the space and the
					 * optimizer can eventually exit such attractor, thus it
					 * should not be avoided. But in the case where all
					 * competitors are potential optima, switching between each
					 * other makes the optimizer running indefinitely without
					 * "seeing" any optimum. This can be rare, but in such a
					 * case the optimizer never provide any convergence although
					 * it should do so.
					 */
					return rand.nextBoolean() ? competitor1 : competitor2;
				} else {
					throw new IllegalStateException(
							"This case should not happen.");
				}
			}
		};
		this.optimizerPool = new OptimizerPool<Individual>(competition);
	}

	@Override
	public Collection<Individual> getPopulation() {
		return optimizerPool.getPopulation();
	}

	public OptimizerPool<Individual> getOptimizerPool() {
		return optimizerPool;
	}

	public void push(Individual individual) {
		optimizerPool.push(individual);
	}

	public void addMutator(Mutator<Individual> mutator) {
		mutators.add(mutator);
	}

	public void removeMutator(Mutator<Individual> mutator) {
		mutators.remove(mutator);
	}

	public Collection<Mutator<Individual>> getMutators() {
		return mutators;
	}

	public void addExplorator(Explorator<Individual> explorator) {
		explorators.add(explorator);
	}

	public void removeExplorator(Explorator<Individual> explorator) {
		explorators.remove(explorator);
	}

	public Collection<Explorator<Individual>> getExplorators() {
		return explorators;
	}

	public boolean hasEvolved() {
		return hasEvolved;
	}

	@Override
	public void incubate() {
		logger.info("INCUBATION START!");
		logger.info("Searching generator...");
		hasEvolved = false;
		Iterator<Wrapper> iterator = generateGenerators();
		TreeMap<Double, Wrapper> lottery = new TreeMap<Double, Wrapper>();
		double total = 0;
		while (iterator.hasNext()) {
			Wrapper generator = iterator.next();
			double interest = generator.evaluateInterest();
			if (interest > 0) {
				total += interest;
				lottery.put(total, generator);
				logger.fine("+ " + generator + " total=" + total);
			} else {
				logger.fine("X " + generator);
			}
		}

		if (lottery.isEmpty()) {
			logger.info("No applicable generator.");
		} else {
			final Wrapper wrapper = lottery.ceilingEntry(
					rand.nextDouble() * total).getValue();
			logger.info("Final generator: " + wrapper);
			wrapper.execute();
			hasEvolved = true;
		}
		logger.info("INCUBATION DONE!");
	}

	private Iterator<Wrapper> generateGenerators() {
		return new Iterator<Wrapper>() {

			private final Iterator<Mutator<Individual>> mutatorIterator = mutators
					.iterator();
			private final Iterator<Explorator<Individual>> exploratorIterator = explorators
					.iterator();
			private Iterator<Optimizer<Individual>> optimizerIterator = null;
			private Mutator<Individual> temporaryMutator = null;
			private Wrapper next = null;

			@Override
			public boolean hasNext() {
				findNext();
				return next != null;
			}

			@Override
			public Wrapper next() {
				findNext();
				Wrapper generator = next;
				next = null;
				return generator;
			}

			private void findNext() {
				while (next == null) {
					if (optimizerIterator != null
							&& optimizerIterator.hasNext()) {
						Optimizer<Individual> optimizer = optimizerIterator
								.next();
						if (temporaryMutator.isApplicableOn(optimizer
								.getRepresentative())) {
							next = new MutatorWrapper(temporaryMutator,
									optimizer);
						} else {
							continue;
						}
					} else if (mutatorIterator.hasNext()) {
						temporaryMutator = mutatorIterator.next();
						optimizerIterator = optimizerPool.iterator();
						continue;
					} else if (exploratorIterator.hasNext()) {
						Explorator<Individual> explorator = exploratorIterator
								.next();
						Collection<Individual> population = getPopulation();
						if (explorator.isApplicableOn(population)) {
							next = new ExploratorWrapper(explorator, population);
						} else {
							continue;
						}
					} else {
						// empty population
						return;
					}
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	private abstract class Wrapper {
		private Double interest = null;

		public double evaluateInterest() {
			interest = interest == null ? computeInterest() : interest;
			return interest;
		}

		protected abstract double computeInterest();

		public abstract void execute();

		public abstract Generator<?, Individual> getGenerator();
	}

	private class MutatorWrapper extends Wrapper {

		private final Mutator<Individual> mutator;
		private final Optimizer<Individual> optimizer;

		public MutatorWrapper(Mutator<Individual> mutator,
				Optimizer<Individual> optimizer) {
			this.mutator = mutator;
			this.optimizer = optimizer;
		}

		@Override
		public void execute() {
			optimizer.compete(mutator);
		}

		@Override
		public Generator<?, Individual> getGenerator() {
			return mutator;
		}

		@Override
		protected double computeInterest() {
			double optimality = optimizer.getOptimalityWith(mutator);
			double interest = 1 - optimality;
			logger.fine("Mutation: O=" + optimality + " => " + interest);
			return interest;
		}

		@Override
		public String toString() {
			return "Mutator[" + mutator + ", " + optimizer + "] ("
					+ evaluateInterest() + ")";
		}
	}

	private class ExploratorWrapper extends Wrapper {

		private final Explorator<Individual> explorator;
		private final Collection<Individual> population;

		public ExploratorWrapper(Explorator<Individual> explorator,
				Collection<Individual> population) {
			this.explorator = explorator;
			this.population = population;
		}

		@Override
		public void execute() {
			optimizerPool.push(explorator.generates(population));
		}

		@Override
		public Generator<?, Individual> getGenerator() {
			return explorator;
		}

		@Override
		protected double computeInterest() {
			double interest = 1;
			for (Mutator<Individual> mutator : mutators) {
				for (Optimizer<Individual> optimizer : optimizerPool) {
					double optimality = optimizer.getOptimalityWith(mutator);
					interest = Math.min(interest, optimality);
				}
			}
			logger.fine("Explorator => " + interest);
			return interest;
		}

		@Override
		public String toString() {
			return "Explorator[" + explorator + ", " + population + "] ("
					+ evaluateInterest() + ")";
		}
	}

}
