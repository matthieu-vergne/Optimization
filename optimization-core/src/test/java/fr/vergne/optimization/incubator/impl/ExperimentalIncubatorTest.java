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

import fr.vergne.optimization.generator.Explorator;
import fr.vergne.optimization.generator.Mutator;
import fr.vergne.optimization.population.Evaluator;

public class ExperimentalIncubatorTest {

	Evaluator<Integer, Integer> integerEvaluator = new Evaluator<Integer, Integer>() {

		@Override
		public Integer evaluate(Integer individual) {
			return individual;
		}
	};
	Explorator<Integer> randomExplorator = new Explorator<Integer>() {

		private final LinkedList<Integer> sequence = new LinkedList<Integer>(
				Arrays.asList(3, 6, 1, 4, 2, 4, 5, 6, 9, 2, 3, 8));

		@Override
		public boolean isApplicableOn(Collection<Integer> population) {
			return true;
		}

		@Override
		public Integer generates(Collection<Integer> population) {
			Collections.rotate(sequence, -1);
			return sequence.getFirst();
		}
	};
	Mutator<Integer> mutatorPlusOne = new Mutator<Integer>() {

		@Override
		public boolean isApplicableOn(Integer original) {
			return true;
		}

		@Override
		public Integer generates(Integer original) {
			return original + 1;
		}
	};
	Mutator<Integer> mutatorModulo = new Mutator<Integer>() {

		@Override
		public boolean isApplicableOn(Integer original) {
			return true;
		}

		@Override
		public Integer generates(Integer original) {
			return original * 2 % 10;
		}
	};

	@Test
	public void testRandomGenerator() {
		for (int i = 0; i < 10; i++) {
			assertEquals(6, (int) randomExplorator.generates(null));
			assertEquals(1, (int) randomExplorator.generates(null));
			assertEquals(4, (int) randomExplorator.generates(null));
			assertEquals(2, (int) randomExplorator.generates(null));
			assertEquals(4, (int) randomExplorator.generates(null));
			assertEquals(5, (int) randomExplorator.generates(null));
			assertEquals(6, (int) randomExplorator.generates(null));
			assertEquals(9, (int) randomExplorator.generates(null));
			assertEquals(2, (int) randomExplorator.generates(null));
			assertEquals(3, (int) randomExplorator.generates(null));
			assertEquals(8, (int) randomExplorator.generates(null));
			assertEquals(3, (int) randomExplorator.generates(null));
		}
	}

	@Test
	public void testMutatorPlusOne() {
		for (int i = 0; i < 100; i++) {
			int individual = randomExplorator.generates(null);
			for (int j = 0; j < 10; j++) {
				int mutant = mutatorPlusOne.generates(individual);
				assertEquals(individual + 1, mutant);
			}
		}
	}

	@Test
	public void testMutatorModulo() {
		int individual = 3;

		individual = mutatorModulo.generates(individual);
		assertEquals(6, individual);

		individual = mutatorModulo.generates(individual);
		assertEquals(2, individual);

		individual = mutatorModulo.generates(individual);
		assertEquals(4, individual);

		individual = mutatorModulo.generates(individual);
		assertEquals(8, individual);

		individual = mutatorModulo.generates(individual);
		assertEquals(6, individual);
	}

	@Test
	public void testMutators() {
		@SuppressWarnings("unchecked")
		List<Mutator<Integer>> mutators = Arrays.asList(mutatorPlusOne,
				mutatorModulo);
		ExperimentalIncubator<Integer> manager = new ExperimentalIncubator<Integer>(
				integerEvaluator);
		for (Mutator<Integer> mutator : mutators) {
			manager.addMutator(mutator);
		}
		Collection<Mutator<Integer>> mutators2 = manager.getMutators();
		assertTrue(
				mutators2 + " != " + mutators,
				mutators.size() == mutators2.size()
						&& mutators.containsAll(mutators2)
						&& mutators2.containsAll(mutators));
	}

	@Test
	public void testExplorators() {
		@SuppressWarnings("unchecked")
		List<Explorator<Integer>> explorators = Arrays.asList(randomExplorator);
		ExperimentalIncubator<Integer> manager = new ExperimentalIncubator<Integer>(
				integerEvaluator);
		for (Explorator<Integer> explorator : explorators) {
			manager.addExplorator(explorator);
		}
		Collection<Explorator<Integer>> explorators2 = manager.getExplorators();
		assertTrue(
				explorators2 + " != " + explorators,
				explorators.size() == explorators2.size()
						&& explorators.containsAll(explorators2)
						&& explorators2.containsAll(explorators));
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
		Explorator<Integer> randomExplorator = new Explorator<Integer>() {

			@Override
			public boolean isApplicableOn(Collection<Integer> population) {
				return ExperimentalIncubatorTest.this.randomExplorator
						.isApplicableOn(population);
			}

			@Override
			public Integer generates(Collection<Integer> population) {
				calls.call();
				return ExperimentalIncubatorTest.this.randomExplorator
						.generates(population);
			}
		};

		ExperimentalIncubator<Integer> manager = new ExperimentalIncubator<Integer>(
				integerEvaluator);
		manager.addExplorator(randomExplorator);

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
			public boolean isApplicableOn(Integer original) {
				return ExperimentalIncubatorTest.this.mutatorModulo
						.isApplicableOn(original);
			}

			@Override
			public Integer generates(Integer original) {
				calls.call();
				return ExperimentalIncubatorTest.this.mutatorModulo
						.generates(original);
			}
		};

		ExperimentalIncubator<Integer> manager = new ExperimentalIncubator<Integer>(
				integerEvaluator);
		manager.addMutator(mutatorModulo);

		calls.expected = 0;
		manager.incubate();
		manager.incubate();
		manager.incubate();
		calls.expected = 1;
		manager.push(1);
		manager.incubate();
		calls.expected = 2;
		manager.incubate();
		calls.expected = 3;
		manager.incubate();
	}

	@Test
	public void testIncubationTotalGeneratorCalls() {
		final CallData calls = new CallData();
		Explorator<Integer> randomExplorator = new Explorator<Integer>() {

			@Override
			public boolean isApplicableOn(Collection<Integer> population) {
				return ExperimentalIncubatorTest.this.randomExplorator
						.isApplicableOn(population);
			}

			@Override
			public Integer generates(Collection<Integer> population) {
				calls.call();
				return ExperimentalIncubatorTest.this.randomExplorator
						.generates(population);
			}
		};
		Mutator<Integer> mutatorModulo = new Mutator<Integer>() {

			@Override
			public boolean isApplicableOn(Integer original) {
				return ExperimentalIncubatorTest.this.mutatorModulo
						.isApplicableOn(original);
			}

			@Override
			public Integer generates(Integer original) {
				calls.call();
				return ExperimentalIncubatorTest.this.mutatorModulo
						.generates(original);
			}
		};
		Mutator<Integer> mutatorPlusOne = new Mutator<Integer>() {

			@Override
			public boolean isApplicableOn(Integer input) {
				return ExperimentalIncubatorTest.this.mutatorPlusOne
						.isApplicableOn(input);
			}

			@Override
			public Integer generates(Integer original) {
				calls.call();
				return ExperimentalIncubatorTest.this.mutatorPlusOne
						.generates(original);
			}
		};

		ExperimentalIncubator<Integer> manager = new ExperimentalIncubator<Integer>(
				integerEvaluator);
		manager.addExplorator(randomExplorator);
		manager.addMutator(mutatorPlusOne);
		manager.addMutator(mutatorModulo);

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
		manager.addExplorator(randomExplorator);

		assertEquals(0, manager.getPopulation().size());
		manager.incubate();
		assertEquals(1, manager.getPopulation().size());
		manager.incubate();
		assertEquals(2, manager.getPopulation().size());
		manager.incubate();
		assertEquals(3, manager.getPopulation().size());
	}

	@Test
	public void testPopulationWithModulo() {
		ExperimentalIncubator<Integer> manager = new ExperimentalIncubator<Integer>(
				integerEvaluator);
		manager.addMutator(mutatorModulo);

		manager.push(9);
		assertEquals(1, manager.getPopulation().size());
		assertEquals(9, (int) manager.getBest().next());
		manager.incubate();
		assertEquals(1, manager.getPopulation().size());
		assertEquals(8, (int) manager.getBest().next());
		manager.incubate();
		assertEquals(1, manager.getPopulation().size());
		assertEquals(6, (int) manager.getBest().next());
		manager.incubate();
		assertEquals(1, manager.getPopulation().size());
		assertEquals(2, (int) manager.getBest().next());
		manager.incubate();
		assertEquals(1, manager.getPopulation().size());
		assertEquals(2, (int) manager.getBest().next());
	}

	@Ignore
	@Test
	public void testPopulationWithRandomAndModulo() {
		ExperimentalIncubator<Integer> incubator = new ExperimentalIncubator<Integer>(
				integerEvaluator);
		incubator.addExplorator(randomExplorator);
		incubator.addMutator(mutatorModulo);

		assertEquals(0, incubator.getPopulation().size());
		incubator.incubate(); // random 6
		assertEquals(1, incubator.getPopulation().size());
		assertEquals(6, (int) incubator.getBest().next());

		for (int i = 0; i < 101; i++) {
			System.err.println("Round A" + i);
			incubator.incubate(); // mutate 6
			System.err.println("Check A" + i);
			assertEquals(1, incubator.getPopulation().size());
			assertEquals(2, (int) incubator.getBest().next());
		}

		{
			incubator.incubate(); // random 1
			assertEquals(2, incubator.getPopulation().size());
			Iterator<Integer> best = incubator.getBest();
			assertEquals(1, (int) best.next());
			assertEquals(2, (int) best.next());
		}

		for (int i = 0; i < 100; i++) {
			System.err.println("Round B" + i);
			incubator.incubate(); // mutate 1, 2 looses
			System.err.println("Check B" + i);
			assertEquals(1, incubator.getPopulation().size());
			assertEquals(1, (int) incubator.getBest().next());
		}

		{
			incubator.incubate(); // random 4
			assertEquals(2, incubator.getPopulation().size());
			Iterator<Integer> best = incubator.getBest();
			assertEquals(1, (int) best.next());
			assertEquals(4, (int) best.next());
		}

		for (int i = 0; i < 102; i++) {
			System.err.println("Round C" + i);
			incubator.incubate(); // mutate 4
			System.err.println("Check C" + i);
			assertEquals(2, incubator.getPopulation().size());
			assertEquals(1, (int) incubator.getBest().next());
		}

		{
			incubator.incubate(); // random 2
			assertEquals(3, incubator.getPopulation().size());
			Iterator<Integer> best = incubator.getBest();
			assertEquals(1, (int) best.next());
			assertEquals(2, (int) best.next());
			assertEquals(4, (int) best.next());
		}

		for (int i = 0; i < 101; i++) {
			System.err.println("Round D" + i);
			incubator.incubate(); // mutate 2, 4 looses
			System.err.println("Check D" + i);
			assertEquals(2, incubator.getPopulation().size());
			assertEquals(1, (int) incubator.getBest().next());
		}

		{
			incubator.incubate(); // random 4
			assertEquals(3, incubator.getPopulation().size());
			Iterator<Integer> best = incubator.getBest();
			assertEquals(1, (int) best.next());
			assertEquals(2, (int) best.next());
			assertEquals(4, (int) best.next());
		}

		for (int i = 0; i < 101; i++) {
			System.err.println("Round E" + i);
			incubator.incubate(); // mutate 4
			System.err.println("Check E" + i);
			assertEquals(3, incubator.getPopulation().size());
			assertEquals(1, (int) incubator.getBest().next());
		}

		while (incubator.getPopulation().size() == 3) {
			System.err.println("Round align");
			incubator.incubate(); // mutate all
			System.err.println("Check align");
			assertEquals(1, (int) incubator.getBest().next());
		}
		assertEquals(2, incubator.getPopulation().size());

		{
			incubator.incubate(); // random 5
			assertEquals(3, incubator.getPopulation().size());
			Iterator<Integer> best = incubator.getBest();
			assertEquals(1, (int) best.next());
			best.next();
			assertEquals(5, (int) best.next());
		}

		{
			incubator.incubate(); // mutate 5, replaced by 0
			assertEquals(3, incubator.getPopulation().size());
			Iterator<Integer> best = incubator.getBest();
			assertEquals(0, (int) best.next());
			assertEquals(1, (int) best.next());
		}

		for (int i = 0; i < 100; i++) {
			System.err.println("Round G" + i);
			incubator.incubate(); // mutate 0
			System.err.println("Check G" + i);
			assertEquals(3, incubator.getPopulation().size());
			assertEquals(0, (int) incubator.getBest().next());
		}

		for (int i = 0; i < 1000; i++) {
			System.err.println("Round H" + i);
			incubator.incubate();
			System.err.println("Check H" + i);
			assertEquals(0, (int) incubator.getBest().next());
		}
	}

}
