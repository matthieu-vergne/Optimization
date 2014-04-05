package fr.vergne.optimization.population.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import fr.vergne.optimization.population.Evaluator;
import fr.vergne.optimization.population.PopulationManager;

/**
 * This {@link PopulationManager} keeps the N best {@link Individual}s provided.
 * The {@link Individual}s are compared by using the given {@link Evaluator},
 * where a lower value is considered as better. If there is several worst
 * {@link Individual}s, the removed one is chosen arbitrarily.
 * 
 * @author Matthieu Vergne <vergne@fbk.eu>
 * 
 * @param <Individual>
 */
public class NBestManager<Individual> implements PopulationManager<Individual> {

	private final TreeSet<Individual> population;
	private int sizeLimit;

	/**
	 * 
	 * @param evaluator
	 *            the {@link Evaluator} to use to compare {@link Individual}s
	 * @param n
	 *            the number of {@link Individual} to keep in the population
	 */
	public <Value extends Comparable<Value>> NBestManager(
			final Evaluator<Individual, Value> evaluator, int n) {
		this.sizeLimit = n;
		population = new TreeSet<Individual>(new Comparator<Individual>() {
			@Override
			public int compare(Individual i1, Individual i2) {
				return evaluator.evaluate(i1).compareTo(evaluator.evaluate(i2));
			}
		});
	}

	@Override
	public void push(Individual individual) {
		population.add(individual);
		while (population.size() > sizeLimit) {
			population.pollLast();
		}
	}

	@Override
	public Collection<Individual> getPopulation() {
		return population;
	}

	@Override
	public Iterator<Individual> getBest() {
		return population.iterator();
	}

}
