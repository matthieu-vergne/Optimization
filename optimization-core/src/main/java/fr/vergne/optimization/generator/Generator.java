package fr.vergne.optimization.generator;

/**
 * A {@link Generator} aims at generating new {@link Individual}s. As it is the
 * only assumption, a class implementing directly this interface as no specific
 * use out of generating new {@link Individual}s. For instance, a random
 * generation could be implemented directly with this interface. In other cases,
 * more specialized interfaces such as {@link Mutator} or {@link Explorator}
 * should be used.
 * 
 * @author Matthieu Vergne <vergne@fbk.eu>
 * 
 * @param <Individual>
 */
public interface Generator<Individual> {

	/**
	 * 
	 * @return a new {@link Individual}
	 */
	public Individual generates();

}
