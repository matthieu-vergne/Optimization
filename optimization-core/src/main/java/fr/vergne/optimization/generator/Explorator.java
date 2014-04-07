package fr.vergne.optimization.generator;

import java.util.Collection;

/**
 * An {@link Explorator} aims at generating new {@link Individual}s based on an
 * existing population, in order to improve the exploration of the solution
 * space. A typical example is to generate an {@link Individual} which is far
 * from all the {@link Individual}s already in the population, or to use some of
 * them to generate another one which combines some qualities. A random
 * generation, which does not exploit any information about the current
 * population, can also be seen as an {@link Explorator}.
 * 
 * @author Matthieu Vergne <vergne@fbk.eu>
 * 
 * @param <Individual>
 */
public interface Explorator<Individual> extends
		Generator<Collection<Individual>, Individual> {
	
	@Override
	public boolean isApplicableOn(Collection<Individual> population);

	@Override
	public Individual generates(Collection<Individual> population);
}
