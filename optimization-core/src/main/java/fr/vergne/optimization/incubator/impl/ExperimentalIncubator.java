package fr.vergne.optimization.incubator.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.vergne.optimization.generator.Explorator;
import fr.vergne.optimization.generator.Generator;
import fr.vergne.optimization.generator.Mutator;
import fr.vergne.optimization.incubator.Incubator;
import fr.vergne.optimization.population.Evaluator;
import fr.vergne.optimization.population.PopulationManager;
import fr.vergne.optimization.population.impl.CompetitionManager;
import fr.vergne.optimization.population.impl.CompetitionManager.Competition;
import fr.vergne.optimization.population.impl.CompetitionManager.ScoreDescriptor;

//TODO replace individuals by "pools" (individual + history-based properties)
//FIXME document
public class ExperimentalIncubator<Individual> implements Incubator<Individual> {

	private final CompetitionManager<Generator<Individual>, Individual> manager;
	private final Collection<Generator<Individual>> generators = new LinkedList<Generator<Individual>>();
	private final Random rand = new Random();
	private final Logger logger = Logger.getAnonymousLogger();

	public <Value extends Comparable<Value>> ExperimentalIncubator(
			final Evaluator<Individual, Value> evaluator) {
		logger.setLevel(Level.ALL);
		this.manager = new CompetitionManager<Generator<Individual>, Individual>(
				new Competition<Individual>() {

					@Override
					public Collection<Individual> compete(
							Collection<Individual> competitors) {
						List<Individual> winners = new LinkedList<Individual>();
						Value bestValue = null;
						for (Individual competitor : competitors) {
							Value newValue = evaluator.evaluate(competitor);
							if (bestValue == null
									|| newValue.compareTo(bestValue) < 0) {
								winners.clear();
								bestValue = newValue;
								winners.add(competitor);
							} else if (newValue.compareTo(bestValue) == 0) {
								winners.add(competitor);
							} else {
								// loser, don't store it
							}
						}
						return winners;
					}
				});
	}

	@Override
	public PopulationManager<Individual> getPopulationManager() {
		return manager;
	}

	public void addGenerator(Generator<Individual> generator) {
		generators.add(generator);
	}

	public void removeGenerator(Generator<Individual> generator) {
		generators.remove(generator);
	}

	@Override
	public Collection<Generator<Individual>> getGenerators() {
		return generators;
	}

	@Override
	public void incubate() {
		logger.info("INCUBATION START!");
		logger.info("Searching generator...");
		Iterator<Wrapper> iterator = generateGenerators();
		TreeMap<Double, Wrapper> lottery = new TreeMap<Double, Wrapper>();
		double max = 0;
		while (iterator.hasNext()) {
			Wrapper generator = iterator.next();
			double interest = generator.evaluateInterest();
			max += interest;
			lottery.put(max, generator);
			logger.fine("+" + generator + " (" + interest + "/" + max + ")");
		}

		if (lottery.isEmpty()) {
			logger.info("No applicable generator.");
		} else {
			final Wrapper wrapper = lottery.ceilingEntry(
					rand.nextDouble() * max).getValue();
			logger.info("Final generator: " + wrapper);
			manager.setCompetitors(wrapper.getCompetitors());
			manager.setCurrentTournament(wrapper.getGenerator());
			Individual individual = wrapper.generates();
			Collection<Individual> competitors = new LinkedList<Individual>(
					wrapper.getCompetitors());
			competitors.add(individual);
			manager.push(individual);
			Collection<Individual> losers = new LinkedList<Individual>(
					competitors);
			losers.removeAll(manager.getPopulation());
			logger.info("Competition: " + competitors + " => loosers: "
					+ losers);
			if (competitors.size() > 1 && losers.isEmpty()) {
				logger.info("Remove least scored to avoid population explosion.");
				TreeSet<Individual> set = new TreeSet<Individual>(
						new Comparator<Individual>() {
							@Override
							public int compare(Individual i1, Individual i2) {
								Generator<Individual> generator = wrapper
										.getGenerator();
								return manager
										.getScore(generator, i1)
										.compareTo(
												manager.getScore(generator, i2));
							}
						});
				set.addAll(competitors);
				manager.getPopulation().remove(set.first());
			} else {
				// normal behavior
			}
		}
		logger.info("INCUBATION DONE!");
	}

	private Iterator<Wrapper> generateGenerators() {
		return new Iterator<Wrapper>() {

			private final Iterator<Generator<Individual>> a = generators
					.iterator();
			private Mutator<Individual> temporaryMutator = null;
			private Iterator<Individual> b = null;
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
					if (b != null && b.hasNext()) {
						next = new MutatorWrapper(temporaryMutator, b.next());
					} else if (a.hasNext()) {
						Generator<Individual> generator = a.next();
						if (generator instanceof Mutator) {
							if (manager.getPopulation().isEmpty()) {
								continue;
							} else {
								temporaryMutator = (Mutator<Individual>) generator;
								b = manager.getPopulation().iterator();
								Individual individual = b.next();
								if (temporaryMutator.isApplicableOn(individual)) {
									next = new MutatorWrapper(temporaryMutator,
											individual);
								} else {
									continue;
								}
							}
						} else if (generator instanceof Explorator) {
							Explorator<Individual> explorator = (Explorator<Individual>) generator;
							Collection<Individual> population = manager
									.getPopulation();
							if (explorator.isApplicableOn(population)) {
								next = new ExploratorWrapper(explorator,
										population);
							} else {
								continue;
							}
						} else {
							next = new GeneratorWrapper(generator);
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

	private abstract class Wrapper implements Generator<Individual> {
		protected abstract double computeInterest();

		public abstract Generator<Individual> getGenerator();

		private Double interest = null;

		public double evaluateInterest() {
			interest = interest == null ? computeInterest() : interest;
			return interest;
		}

		public abstract Collection<Individual> getCompetitors();
	}

	private class MutatorWrapper extends Wrapper {

		private final Mutator<Individual> mutator;
		private final Individual individual;

		public MutatorWrapper(Mutator<Individual> mutator, Individual individual) {
			this.mutator = mutator;
			this.individual = individual;
		}

		@Override
		public Generator<Individual> getGenerator() {
			return mutator;
		}

		@Override
		public Individual generates() {
			mutator.setReference(individual);
			return mutator.generates();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Collection<Individual> getCompetitors() {
			return Arrays.asList(individual);
		}

		@Override
		protected double computeInterest() {
			// FIXME consider average time to improve (window)
			// TODO consider mutator-dependent neighboring size
			// TODO formalize computation
			int score = manager.getScore(mutator, individual);
			int max = manager.getMaxScoreGlobal(mutator);
			int ref = max + 1;
			double interest = ((double) ref - score) / ref;
			logger.fine("Mutation: S=" + score + "/" + max + " => " + interest);
			return interest;
		}

		@Override
		public String toString() {
			return "Mutator[" + mutator + ", " + individual + "] ("
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
		public Generator<Individual> getGenerator() {
			return explorator;
		}

		@Override
		public Individual generates() {
			explorator.setReference(population);
			return use(explorator);
		}

		@Override
		public Collection<Individual> getCompetitors() {
			return Collections.emptyList();
		}

		@Override
		protected double computeInterest() {
			// FIXME use improvement history (pools)
			// TODO use tournament neighboring size to weight
			// TODO formalize computation
			Iterator<ScoreDescriptor<Generator<Individual>>> descriptors = manager
					.getScoreDescriptors();
			double interest = 1;
			while (descriptors.hasNext()) {
				ScoreDescriptor<Generator<Individual>> descriptor = descriptors
						.next();
				interest *= descriptor.getRatio();
				logger.finer("T=" + descriptor.getTournament() + ", S="
						+ descriptor.getMin() + "/" + descriptor.getMax()
						+ " => " + descriptor.getRatio());
			}
			double size = manager.size();
			double uses = getUses(explorator);
			System.out.println("Explorator: S=" + "?" + ", E=" + uses + ", P="
					+ size + " => " + interest);
			logger.fine("Explorator: S=" + "?" + ", E=" + uses + ", P=" + size
					+ " => " + interest);
			return interest;
		}

		@Override
		public String toString() {
			return "Explorator[" + explorator + ", " + population + "] ("
					+ evaluateInterest() + ")";
		}
	}

	private class GeneratorWrapper extends Wrapper {

		private final Generator<Individual> generator;

		public GeneratorWrapper(Generator<Individual> generator) {
			this.generator = generator;
		}

		@Override
		public Generator<Individual> getGenerator() {
			return generator;
		}

		@Override
		public Individual generates() {
			return use(generator);
		}

		@Override
		public Collection<Individual> getCompetitors() {
			return Collections.emptyList();
		}

		@Override
		protected double computeInterest() {
			// TODO formalize computation
			int size = manager.size();
			int uses = getUses(generator);
			double interest = Math.exp(-uses * size);
			logger.fine("Generation: G=" + uses + ", P=" + size + " => "
					+ interest);
			return interest;
		}

		@Override
		public String toString() {
			return "Generator[" + generator + "] (" + evaluateInterest() + ")";
		}
	}

	private final Map<Generator<Individual>, Integer> uses = new HashMap<Generator<Individual>, Integer>();

	private Individual use(Generator<Individual> generator) {
		uses.put(generator, getUses(generator) + 1);
		return generator.generates();
	}

	private int getUses(Generator<Individual> generator) {
		return uses.containsKey(generator) ? uses.get(generator) : 0;
	}
}
