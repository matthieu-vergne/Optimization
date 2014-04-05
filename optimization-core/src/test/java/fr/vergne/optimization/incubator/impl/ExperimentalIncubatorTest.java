package fr.vergne.optimization.incubator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import fr.vergne.optimization.generator.Generator;
import fr.vergne.optimization.generator.Mutator;
import fr.vergne.optimization.incubator.impl.ExperimentalIncubator;
import fr.vergne.optimization.population.Evaluator;
import fr.vergne.optimization.population.impl.CompetitionManager;

public class ExperimentalIncubatorTest {

	Evaluator<Integer, Integer> integerEvaluator = new Evaluator<Integer, Integer>() {

		@Override
		public Integer evaluate(Integer individual) {
			return individual;
		}
	};
	Generator<Integer> randomGenerator = new Generator<Integer>() {

		private final LinkedList<Integer> sequence = new LinkedList<Integer>(
				Arrays.asList(3, 6, 1, 4, 2, 4, 5, 6, 9, 2, 3, 8));

		@Override
		public Integer generates() {
			Collections.rotate(sequence, -1);
			return sequence.getFirst();
		}
	};
	Mutator<Integer> mutatorPlusOne = new Mutator<Integer>() {

		private Integer reference;

		@Override
		public boolean isApplicableOn(Integer input) {
			return true;
		}

		@Override
		public void setReference(Integer reference) {
			this.reference = reference;
		}

		@Override
		public Integer generates() {
			return reference + 1;
		}
	};
	Mutator<Integer> mutatorModulo = new Mutator<Integer>() {

		private Integer reference;

		@Override
		public boolean isApplicableOn(Integer input) {
			return true;
		}

		@Override
		public void setReference(Integer reference) {
			this.reference = reference;
		}

		@Override
		public Integer generates() {
			return reference * 2 % 10;
		}
	};

	@Test
	public void testRandomGenerator() {
		for (int i = 0; i < 10; i++) {
			assertEquals(6, (int) randomGenerator.generates());
			assertEquals(1, (int) randomGenerator.generates());
			assertEquals(4, (int) randomGenerator.generates());
			assertEquals(2, (int) randomGenerator.generates());
			assertEquals(4, (int) randomGenerator.generates());
			assertEquals(5, (int) randomGenerator.generates());
			assertEquals(6, (int) randomGenerator.generates());
			assertEquals(9, (int) randomGenerator.generates());
			assertEquals(2, (int) randomGenerator.generates());
			assertEquals(3, (int) randomGenerator.generates());
			assertEquals(8, (int) randomGenerator.generates());
			assertEquals(3, (int) randomGenerator.generates());
		}
	}

	@Test
	public void testMutatorPlusOne() {
		for (int i = 0; i < 100; i++) {
			int individual = randomGenerator.generates();
			mutatorPlusOne.setReference(individual);
			for (int j = 0; j < 10; j++) {
				int mutant = mutatorPlusOne.generates();
				assertEquals(individual + 1, mutant);
			}
		}
	}

	@Test
	public void testMutatorModulo() {
		int individual = 3;

		mutatorModulo.setReference(individual);
		individual = mutatorModulo.generates();
		assertEquals(6, individual);

		mutatorModulo.setReference(individual);
		individual = mutatorModulo.generates();
		assertEquals(2, individual);

		mutatorModulo.setReference(individual);
		individual = mutatorModulo.generates();
		assertEquals(4, individual);

		mutatorModulo.setReference(individual);
		individual = mutatorModulo.generates();
		assertEquals(8, individual);

		mutatorModulo.setReference(individual);
		individual = mutatorModulo.generates();
		assertEquals(6, individual);
	}

	@Test
	public void testPopulationManager() {
		ExperimentalIncubator<Integer> manager = new ExperimentalIncubator<Integer>(
				integerEvaluator);
		manager.addGenerator(randomGenerator);
		manager.addGenerator(mutatorPlusOne);
		manager.addGenerator(mutatorModulo);
		@SuppressWarnings("unchecked")
		CompetitionManager<?, Integer> populationManager = (CompetitionManager<?, Integer>) manager
				.getPopulationManager();

		populationManager.push(2);
		assertEquals(1, populationManager.size());
		assertEquals(2, (int) populationManager.getBest().next());

		populationManager.push(1);
		assertEquals(1, populationManager.size());
		assertEquals(1, (int) populationManager.getBest().next());

		populationManager.push(1);
		assertEquals(1, populationManager.size());
		assertEquals(1, (int) populationManager.getBest().next());

		populationManager.push(3);
		assertEquals(1, populationManager.size());
		assertEquals(1, (int) populationManager.getBest().next());
	}

	@Test
	public void testGenerators() {
		@SuppressWarnings("unchecked")
		List<Generator<Integer>> generators = Arrays.asList(randomGenerator,
				mutatorPlusOne, mutatorModulo);
		ExperimentalIncubator<Integer> manager = new ExperimentalIncubator<Integer>(
				integerEvaluator);
		for (Generator<Integer> generator : generators) {
			manager.addGenerator(generator);
		}
		Collection<Generator<Integer>> generators2 = manager.getGenerators();
		assertTrue(
				generators2 + " != " + generators,
				generators.size() == generators2.size()
						&& generators.containsAll(generators2)
						&& generators2.containsAll(generators));
	}

	private static class CallData {
		int done = 0;
		int expected = 0;

		public void call() {
			done++;
			assertEquals(expected, done);
		}
	}

	@Test
	public void testIncubationGeneratorCalls() {
		final CallData calls = new CallData();
		Generator<Integer> randomGenerator = new Generator<Integer>() {

			@Override
			public Integer generates() {
				calls.call();
				return ExperimentalIncubatorTest.this.randomGenerator
						.generates();
			}
		};

		ExperimentalIncubator<Integer> manager = new ExperimentalIncubator<Integer>(
				integerEvaluator);
		manager.addGenerator(randomGenerator);

		calls.expected = 1;
		manager.incubate();
		calls.expected = 2;
		manager.incubate();
		calls.expected = 3;
		manager.incubate();
	}

	@Test
	public void testIncubationMutatorCalls() {
		final CallData calls = new CallData();
		Mutator<Integer> mutatorModulo = new Mutator<Integer>() {

			@Override
			public boolean isApplicableOn(Integer input) {
				return ExperimentalIncubatorTest.this.mutatorModulo
						.isApplicableOn(input);
			}

			@Override
			public void setReference(Integer reference) {
				ExperimentalIncubatorTest.this.mutatorModulo
						.setReference(reference);
			}

			@Override
			public Integer generates() {
				calls.call();
				return ExperimentalIncubatorTest.this.mutatorModulo.generates();
			}
		};

		ExperimentalIncubator<Integer> manager = new ExperimentalIncubator<Integer>(
				integerEvaluator);
		manager.addGenerator(mutatorModulo);

		calls.expected = 0;
		manager.incubate();
		manager.incubate();
		manager.incubate();
		calls.expected = 1;
		manager.getPopulationManager().push(1);
		manager.incubate();
		calls.expected = 2;
		manager.incubate();
		calls.expected = 3;
		manager.incubate();
	}

	@Test
	public void testIncubationTotalGeneratorCalls() {
		final CallData calls = new CallData();
		Generator<Integer> randomGenerator = new Generator<Integer>() {

			@Override
			public Integer generates() {
				calls.call();
				return ExperimentalIncubatorTest.this.randomGenerator
						.generates();
			}
		};
		Mutator<Integer> mutatorModulo = new Mutator<Integer>() {

			@Override
			public boolean isApplicableOn(Integer input) {
				return ExperimentalIncubatorTest.this.mutatorModulo
						.isApplicableOn(input);
			}

			@Override
			public void setReference(Integer reference) {
				ExperimentalIncubatorTest.this.mutatorModulo
						.setReference(reference);
			}

			@Override
			public Integer generates() {
				calls.call();
				return ExperimentalIncubatorTest.this.mutatorModulo.generates();
			}
		};
		Mutator<Integer> mutatorPlusOne = new Mutator<Integer>() {

			@Override
			public boolean isApplicableOn(Integer input) {
				return ExperimentalIncubatorTest.this.mutatorPlusOne
						.isApplicableOn(input);
			}

			@Override
			public void setReference(Integer reference) {
				ExperimentalIncubatorTest.this.mutatorPlusOne
						.setReference(reference);
			}

			@Override
			public Integer generates() {
				calls.call();
				return ExperimentalIncubatorTest.this.mutatorPlusOne
						.generates();
			}
		};

		ExperimentalIncubator<Integer> manager = new ExperimentalIncubator<Integer>(
				integerEvaluator);
		manager.addGenerator(randomGenerator);
		manager.addGenerator(mutatorPlusOne);
		manager.addGenerator(mutatorModulo);

		calls.expected = 1;
		manager.incubate();
		calls.expected = 2;
		manager.incubate();
		calls.expected = 3;
		manager.incubate();
	}

	@Test
	public void testPopulationSizeWithGenerator() {
		ExperimentalIncubator<Integer> manager = new ExperimentalIncubator<Integer>(
				integerEvaluator);
		manager.addGenerator(randomGenerator);

		assertEquals(0, manager.getPopulationManager().getPopulation().size());
		manager.incubate();
		assertEquals(1, manager.getPopulationManager().getPopulation().size());
		manager.incubate();
		assertEquals(2, manager.getPopulationManager().getPopulation().size());
		manager.incubate();
		assertEquals(3, manager.getPopulationManager().getPopulation().size());
	}

	@Test
	public void testPopulationWithModulo() {
		ExperimentalIncubator<Integer> manager = new ExperimentalIncubator<Integer>(
				integerEvaluator);
		manager.addGenerator(mutatorModulo);

		manager.getPopulationManager().push(9);
		assertEquals(1, manager.getPopulationManager().getPopulation().size());
		assertEquals(9, (int) manager.getPopulationManager().getBest().next());
		manager.incubate();
		assertEquals(1, manager.getPopulationManager().getPopulation().size());
		assertEquals(8, (int) manager.getPopulationManager().getBest().next());
		manager.incubate();
		assertEquals(1, manager.getPopulationManager().getPopulation().size());
		assertEquals(6, (int) manager.getPopulationManager().getBest().next());
		manager.incubate();
		assertEquals(1, manager.getPopulationManager().getPopulation().size());
		assertEquals(2, (int) manager.getPopulationManager().getBest().next());
		manager.incubate();
		assertEquals(1, manager.getPopulationManager().getPopulation().size());
		assertEquals(2, (int) manager.getPopulationManager().getBest().next());
	}

	@Ignore
	@Test
	public void testPopulationWithRandomAndModulo() {
		ExperimentalIncubator<Integer> manager = new ExperimentalIncubator<Integer>(
				integerEvaluator);
		manager.addGenerator(randomGenerator);
		manager.addGenerator(mutatorModulo);

		@SuppressWarnings("unchecked")
		CompetitionManager<?, Integer> populationManager = (CompetitionManager<?, Integer>) manager
				.getPopulationManager();
		assertEquals(0, populationManager.size());
		manager.incubate(); // random 6
		assertEquals(1, populationManager.size());
		assertEquals(6, (int) populationManager.getBest().next());

		for (int i = 0; i < 101; i++) {
			System.err.println("Round A" + i);
			manager.incubate(); // mutate 6
			System.err.println("Check A" + i);
			assertEquals(1, populationManager.size());
			assertEquals(2, (int) populationManager.getBest().next());
		}

		{
			manager.incubate(); // random 1
			assertEquals(2, populationManager.size());
			Iterator<Integer> best = populationManager.getBest();
			assertEquals(1, (int) best.next());
			assertEquals(2, (int) best.next());
		}

		for (int i = 0; i < 100; i++) {
			System.err.println("Round B" + i);
			manager.incubate(); // mutate 1, 2 looses
			System.err.println("Check B" + i);
			assertEquals(1, populationManager.size());
			assertEquals(1, (int) populationManager.getBest().next());
		}

		{
			manager.incubate(); // random 4
			assertEquals(2, populationManager.size());
			Iterator<Integer> best = populationManager.getBest();
			assertEquals(1, (int) best.next());
			assertEquals(4, (int) best.next());
		}

		for (int i = 0; i < 102; i++) {
			System.err.println("Round C" + i);
			manager.incubate(); // mutate 4
			System.err.println("Check C" + i);
			assertEquals(2, populationManager.size());
			assertEquals(1, (int) populationManager.getBest().next());
		}

		{
			manager.incubate(); // random 2
			assertEquals(3, populationManager.size());
			Iterator<Integer> best = populationManager.getBest();
			assertEquals(1, (int) best.next());
			assertEquals(2, (int) best.next());
			assertEquals(4, (int) best.next());
		}

		for (int i = 0; i < 101; i++) {
			System.err.println("Round D" + i);
			manager.incubate(); // mutate 2, 4 looses
			System.err.println("Check D" + i);
			assertEquals(2, populationManager.size());
			assertEquals(1, (int) populationManager.getBest().next());
		}

		{
			manager.incubate(); // random 4
			assertEquals(3, populationManager.size());
			Iterator<Integer> best = populationManager.getBest();
			assertEquals(1, (int) best.next());
			assertEquals(2, (int) best.next());
			assertEquals(4, (int) best.next());
		}

		for (int i = 0; i < 101; i++) {
			System.err.println("Round E" + i);
			manager.incubate(); // mutate 4
			System.err.println("Check E" + i);
			assertEquals(3, populationManager.size());
			assertEquals(1, (int) populationManager.getBest().next());
		}

		while (populationManager.size() == 3) {
			System.err.println("Round align");
			manager.incubate(); // mutate all
			System.err.println("Check align");
			assertEquals(1, (int) populationManager.getBest().next());
		}
		assertEquals(2, populationManager.size());

		{
			manager.incubate(); // random 5
			assertEquals(3, populationManager.size());
			Iterator<Integer> best = populationManager.getBest();
			assertEquals(1, (int) best.next());
			best.next();
			assertEquals(5, (int) best.next());
		}

		{
			manager.incubate(); // mutate 5, replaced by 0
			assertEquals(3, populationManager.size());
			Iterator<Integer> best = populationManager.getBest();
			assertEquals(0, (int) best.next());
			assertEquals(1, (int) best.next());
		}

		for (int i = 0; i < 100; i++) {
			System.err.println("Round G" + i);
			manager.incubate(); // mutate 0
			System.err.println("Check G" + i);
			assertEquals(3, populationManager.size());
			assertEquals(0, (int) populationManager.getBest().next());
		}

		for (int i = 0; i < 1000; i++) {
			System.err.println("Round H" + i);
			manager.incubate();
			System.err.println("Check H" + i);
			assertEquals(0, (int) populationManager.getBest().next());
		}
	}

}
