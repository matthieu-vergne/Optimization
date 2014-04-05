package fr.vergne.optimization.generator;

/**
 * A {@link Mutator} generates a mutant {@link Individual} based on another
 * {@link Individual}.
 * 
 * @author Matthieu Vergne <vergne@fbk.eu>
 * 
 * @param <Individual>
 */
public interface Mutator<Individual> extends Generator<Individual>,
		RestrictedUsability<Individual> {

	/**
	 * 
	 * @param reference
	 *            the {@link Individual} to create a mutant from
	 */
	public void setReference(Individual reference);
}
