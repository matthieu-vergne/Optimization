package fr.vergne.optimization.generator;

/**
 * A {@link Generator} aims at generating new {@link Individual}s based on some
 * {@link Configuration}s.
 * 
 * @author Matthieu Vergne <vergne@fbk.eu>
 * 
 * @param <Configuration>
 * @param <Individual>
 */
public interface Generator<Configuration, Individual> {

	/**
	 * 
	 * @param configuration
	 *            the {@link Configuration} to check
	 * @return <code>true</code> if the {@link Configuration} provided can be
	 *         used, <code>false</code> otherwise
	 */
	public boolean isApplicableOn(Configuration configuration);

	/**
	 * 
	 * @param configuration
	 *            the {@link Configuration} to generate an {@link Individual}
	 * @return the generated {@link Individual}
	 */
	public Individual generates(Configuration configuration);

}
