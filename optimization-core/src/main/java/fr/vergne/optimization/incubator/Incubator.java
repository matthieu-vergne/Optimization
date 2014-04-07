package fr.vergne.optimization.incubator;

import java.util.Collection;

/**
 * An {@link Incubator} aims at evolving a population of {@link Individual}s. A
 * single execution of the {@link #incubate()} method correspond to a single
 * round of evolution of the population. Depending on the {@link Incubator},
 * especially when probabilistic processes are involved, the population can be
 * exactly in the same state after a single round.
 * 
 * @author Matthieu Vergne <vergne@fbk.eu>
 * 
 * @param <Individual>
 */
public interface Incubator<Individual> {

	/**
	 * 
	 * @return the current population of {@link Individual}s
	 */
	public Collection<Individual> getPopulation();

	/**
	 * The incubation process, aiming at making the population evolve by
	 * inserting or removing {@link Individual}s (or both).
	 */
	public void incubate();
}
