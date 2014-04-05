package fr.vergne.optimization.population.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import org.junit.Test;

import fr.vergne.optimization.population.impl.CompetitionManager;
import fr.vergne.optimization.population.impl.CompetitionManager.Competition;

public class CompetitionManagerTest {

	Competition<Integer> competition = new Competition<Integer>() {

		@Override
		public Collection<Integer> compete(Collection<Integer> competitors) {
			LinkedList<Integer> ordered = new LinkedList<Integer>(competitors);
			if (competitors.size() > 1) {
				Collections.sort(ordered);
				while (ordered.getFirst() < ordered.getLast()) {
					ordered.removeLast();
				}
			} else {
				// nothing to order
			}
			return ordered;
		}
	};

	@Test
	public void testCompetition() {
		{
			Collection<Integer> competitors = Arrays.asList(1, 2, 3);
			Collection<Integer> expected = Arrays.asList(1);
			Collection<Integer> winners = competition.compete(competitors);
			assertTrue(winners + " != " + expected,
					exactlySameSet(expected, winners));
		}
		{
			Collection<Integer> competitors = Arrays.asList(3, 2, 1);
			Collection<Integer> expected = Arrays.asList(1);
			Collection<Integer> winners = competition.compete(competitors);
			assertTrue(winners + " != " + expected,
					exactlySameSet(expected, winners));
		}
		{
			Collection<Integer> competitors = Arrays.asList(1, 2, 1);
			Collection<Integer> expected = Arrays.asList(1, 1);
			Collection<Integer> winners = competition.compete(competitors);
			assertTrue(winners + " != " + expected,
					exactlySameSet(expected, winners));
		}
		{
			Collection<Integer> competitors = Arrays.asList(2);
			Collection<Integer> expected = Arrays.asList(2);
			Collection<Integer> winners = competition.compete(competitors);
			assertTrue(winners + " != " + expected,
					exactlySameSet(expected, winners));
		}
		{
			Collection<Integer> competitors = Arrays.asList();
			Collection<Integer> expected = Arrays.asList();
			Collection<Integer> winners = competition.compete(competitors);
			assertTrue(winners + " != " + expected,
					exactlySameSet(expected, winners));
		}
	}

	private boolean exactlySameSet(Collection<Integer> expected,
			Collection<Integer> winners) {
		return expected.size() == winners.size()
				&& expected.containsAll(winners)
				&& winners.containsAll(expected);
	}

	@Test
	public void testPopulationSize() {
		CompetitionManager<Object, Integer> manager = new CompetitionManager<Object, Integer>(
				competition);
		{
			Collection<Integer> expected = Arrays.asList();
			Collection<Integer> population = manager.getPopulation();
			assertTrue(population + " != " + expected,
					exactlySameSet(expected, population));
		}
		manager.push(1);
		{
			Collection<Integer> expected = Arrays.asList(1);
			Collection<Integer> population = manager.getPopulation();
			assertTrue(population + " != " + expected,
					exactlySameSet(expected, population));
		}
		manager.push(2);
		{
			Collection<Integer> expected = Arrays.asList(1);
			Collection<Integer> population = manager.getPopulation();
			assertTrue(population + " != " + expected,
					exactlySameSet(expected, population));
		}
		manager.push(0);
		{
			Collection<Integer> expected = Arrays.asList(0);
			Collection<Integer> population = manager.getPopulation();
			assertTrue(population + " != " + expected,
					exactlySameSet(expected, population));
		}
	}

	@Test
	public void testBests() {
		CompetitionManager<Object, Integer> manager = new CompetitionManager<Object, Integer>(
				competition);
		{
			Iterator<Integer> best = manager.getBest();
			assertFalse(best.hasNext());
		}
		manager.push(1);
		{
			Iterator<Integer> best = manager.getBest();
			assertEquals(1, (int) best.next());
			assertFalse(best.hasNext());
		}
		manager.push(2);
		{
			Iterator<Integer> best = manager.getBest();
			assertEquals(1, (int) best.next());
			assertFalse(best.hasNext());
		}
		manager.push(0);
		{
			Iterator<Integer> best = manager.getBest();
			assertEquals(0, (int) best.next());
			assertFalse(best.hasNext());
		}
	}

	@Test
	public void testScores() {
		CompetitionManager<Object, Integer> manager = new CompetitionManager<Object, Integer>(
				competition);
		{
			assertEquals(0, (int) manager.getScore(null, 0));
			assertEquals(0, (int) manager.getScore(null, 1));
			assertEquals(0, (int) manager.getScore(null, 2));
		}
		manager.push(1);
		{
			assertEquals(0, (int) manager.getScore(null, 0));
			assertEquals(1, (int) manager.getScore(null, 1));
			assertEquals(0, (int) manager.getScore(null, 2));
		}
		manager.push(2);
		{
			assertEquals(0, (int) manager.getScore(null, 0));
			assertEquals(2, (int) manager.getScore(null, 1));
			assertEquals(0, (int) manager.getScore(null, 2));
		}
		manager.push(0);
		{
			assertEquals(1, (int) manager.getScore(null, 0));
			assertEquals(0, (int) manager.getScore(null, 1));
			assertEquals(0, (int) manager.getScore(null, 2));
		}
	}

}
