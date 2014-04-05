package fr.vergne.optimization.population;

/**
 * An {@link Evaluator} aims at providing a {@link Value} to an
 * {@link Individual}. The {@link Value} must be {@link Comparable} in order to
 * identify which {@link Individual} is better than another. Typical examples of
 * {@link Value}s are {@link Integer}s and {@link Double}s.
 * 
 * @author Matthieu Vergne <vergne@fbk.eu>
 * 
 * @param <Individual>
 * @param <Value>
 */
public interface Evaluator<Individual, Value extends Comparable<Value>> {

	/**
	 * 
	 * @param individual
	 *            the {@link Individual} to evaluate
	 * @return the {@link Value} of this {@link Individual}
	 */
	public Value evaluate(Individual individual);
}
