package fr.vergne.optimization.population.impl;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import fr.vergne.optimization.population.Evaluator;
import fr.vergne.optimization.population.impl.NBestManager;

public class NBestManagerTest {

	Evaluator<Integer, Integer> integerEvaluator = new Evaluator<Integer, Integer>() {
		
		@Override
		public Integer evaluate(Integer individual) {
			return individual;
		}
	};
	
	@Test
	public void testSizeLimit() {
		NBestManager<Integer> manager = new NBestManager<Integer>(integerEvaluator, 5);
		assertEquals(0, manager.getPopulation().size());
		manager.push(1);
		assertEquals(1, manager.getPopulation().size());
		manager.push(2);
		assertEquals(2, manager.getPopulation().size());
		manager.push(3);
		assertEquals(3, manager.getPopulation().size());
		manager.push(4);
		assertEquals(4, manager.getPopulation().size());
		manager.push(5);
		assertEquals(5, manager.getPopulation().size());
		manager.push(6);
		assertEquals(5, manager.getPopulation().size());
		manager.push(7);
		assertEquals(5, manager.getPopulation().size());
		manager.push(8);
		assertEquals(5, manager.getPopulation().size());
	}
	
	@Test
	public void testBestOrder() {
		NBestManager<Integer> manager = new NBestManager<Integer>(integerEvaluator, 5);
		
		{
			manager.push(1);
			Iterator<Integer> best = manager.getBest();
			assertEquals(1, (int) best.next());
			assertFalse(best.hasNext());
		}
		{
			manager.push(2);
			Iterator<Integer> best = manager.getBest();
			assertEquals(1, (int) best.next());
			assertEquals(2, (int) best.next());
			assertFalse(best.hasNext());
		}
		{
			manager.push(3);
			Iterator<Integer> best = manager.getBest();
			assertEquals(1, (int) best.next());
			assertEquals(2, (int) best.next());
			assertEquals(3, (int) best.next());
			assertFalse(best.hasNext());
		}
		{
			manager.push(4);
			Iterator<Integer> best = manager.getBest();
			assertEquals(1, (int) best.next());
			assertEquals(2, (int) best.next());
			assertEquals(3, (int) best.next());
			assertEquals(4, (int) best.next());
			assertFalse(best.hasNext());
		}
		{
			manager.push(5);
			Iterator<Integer> best = manager.getBest();
			assertEquals(1, (int) best.next());
			assertEquals(2, (int) best.next());
			assertEquals(3, (int) best.next());
			assertEquals(4, (int) best.next());
			assertEquals(5, (int) best.next());
			assertFalse(best.hasNext());
		}
		{
			manager.push(6);
			Iterator<Integer> best = manager.getBest();
			assertEquals(1, (int) best.next());
			assertEquals(2, (int) best.next());
			assertEquals(3, (int) best.next());
			assertEquals(4, (int) best.next());
			assertEquals(5, (int) best.next());
			assertFalse(best.hasNext());
		}
		{
			manager.push(7);
			Iterator<Integer> best = manager.getBest();
			assertEquals(1, (int) best.next());
			assertEquals(2, (int) best.next());
			assertEquals(3, (int) best.next());
			assertEquals(4, (int) best.next());
			assertEquals(5, (int) best.next());
			assertFalse(best.hasNext());
		}
		{
			manager.push(0);
			Iterator<Integer> best = manager.getBest();
			assertEquals(0, (int) best.next());
			assertEquals(1, (int) best.next());
			assertEquals(2, (int) best.next());
			assertEquals(3, (int) best.next());
			assertEquals(4, (int) best.next());
			assertFalse(best.hasNext());
		}
		{
			manager.push(-1);
			Iterator<Integer> best = manager.getBest();
			assertEquals(-1, (int) best.next());
			assertEquals(0, (int) best.next());
			assertEquals(1, (int) best.next());
			assertEquals(2, (int) best.next());
			assertEquals(3, (int) best.next());
			assertFalse(best.hasNext());
		}
	}

}
