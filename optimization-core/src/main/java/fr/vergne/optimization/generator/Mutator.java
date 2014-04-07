package fr.vergne.optimization.generator;

/**
 * A {@link Mutator} generates a mutant {@link Individual} based on another
 * {@link Individual}.
 * 
 * @author Matthieu Vergne <vergne@fbk.eu>
 * 
 * @param <Individual>
 */
public interface Mutator<Individual> extends Generator<Individual, Individual> {
	
	@Override
	public boolean isApplicableOn(Individual original);

	@Override
	public Individual generates(Individual original);
}
