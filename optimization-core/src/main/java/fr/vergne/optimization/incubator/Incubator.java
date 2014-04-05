package fr.vergne.optimization.incubator;

import java.util.Collection;

import fr.vergne.optimization.generator.Generator;
import fr.vergne.optimization.population.PopulationManager;

/**
 * An {@link Incubator} aims at evolving a population of {@link Individual}s.
 * {@link #getGenerators()} provides the {@link Generator}s which will create
 * new {@link Individual}s while {@link #getPopulationManager()} provides the
 * {@link PopulationManager} which decides how the individuals are
 * kept during the incubation process.
 * 
 * @author Matthieu Vergne <vergne@fbk.eu>
 * 
 * @param <Individual>
 */
public interface Incubator<Individual> {

	/**
	 * 
	 * @return the {@link PopulationManager} in which the {@link Individual}s
	 *         are stored
	 */
	public PopulationManager<Individual> getPopulationManager();

	/**
	 * 
	 * @return the {@link Generator}s used to generate new {@link Individual}s
	 */
	public Collection<Generator<Individual>> getGenerators();

	/**
	 * The incubation process, aiming at creating new {@link Individual}s to
	 * make the population evolve.
	 */
	public void incubate();
}
