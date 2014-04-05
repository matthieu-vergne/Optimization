package fr.vergne.optimization.generator;

import java.util.Collection;

/**
 * An {@link Explorator} aims at generating new {@link Individual}s to improve
 * the exploration of the solution space. A typical example is to generate an
 * {@link Individual} which is far from all the {@link Individual}s already in
 * the population, or to use some of them to generate another one which combines
 * some qualities.
 * 
 * @author Matthieu Vergne <vergne@fbk.eu>
 * 
 * @param <Individual>
 */
public interface Explorator<Individual> extends Generator<Individual>,
		RestrictedUsability<Collection<Individual>> {

	/**
	 * 
	 * @param population
	 *            the current population of {@link Individual}s
	 */
	public void setReference(Collection<Individual> population);

}
