package fr.vergne.optimization.generator;

/**
 * An {@link InformedMutator} is a {@link Mutator} able to provide additional
 * information to better evaluate the optimality of an {@link Individual}.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 * @param <Individual>
 */
public interface InformedMutator<Individual> extends Mutator<Individual> {

	/**
	 * This methods aims at providing an evaluation of the amount of mutant
	 * {@link Individual}s to evaluate before to consider the original
	 * {@link Individual} as optimal. If there is an infinite (i.e. too big) set
	 * of mutants, this methods provides a subjective number of mutants to
	 * evaluate before to consider that the original {@link Individual} is
	 * probably the best.
	 * 
	 * @return the amount of {@link Individual}s to browse to consider having an
	 *         optimal {@link Individual}.
	 */
	public long getNeighboringLimit();

	/**
	 * This methods tells whether the limit provided by
	 * {@link #getNeighboringLimit()} should be strictly respected (no more
	 * {@link Individual} should be generated). Typically, if the
	 * {@link Mutator} generates a finite set of mutants*, the exact number of
	 * mutants should be provided by {@link #getNeighboringLimit()} and this
	 * method should return <code>true</code>. In other cases it is generally
	 * better to return <code>false</code> unless there is practical evidences
	 * that it is more efficient to strictly limit the mutants generation.<br/>
	 * <br/>
	 * * We speak about mutants for the exactly same {@link Individual}, not an
	 * equivalent. In particular, it could be that the same {@link Individual}
	 * is generated several times, but each of them is considered separately,
	 * like twins are equivalent but not the exactly same {@link Individual}s.
	 * If you do not want to regenerate the mutants for the equivalent
	 * {@link Individual}s, the method {@link #isApplicableOn(Object)} should
	 * return <code>false</code> consequently. But do it only if you know really
	 * well what you are doing.
	 * 
	 * @return <code>true</code> if browsing more {@link Individual}s than
	 *         {@link #getNeighboringLimit()} have no interest,
	 *         <code>false</code> otherwise
	 */
	public boolean isNeighboringSizeStrict();
}
