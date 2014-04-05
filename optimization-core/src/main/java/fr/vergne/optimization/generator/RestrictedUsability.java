package fr.vergne.optimization.generator;

/**
 * A class implementing the {@link RestrictedUsability} interface aims at
 * specifying in which cases a specific {@link Input} can be used for this
 * class. Typically, a class uses a data structure as {@link Input}, but only
 * some configurations are consistent for the intended use. Implementing this
 * interface allows to identify when unexpected configurations are provided,
 * giving the possibility to avoid using the class with inconsistent data.
 * 
 * @author Matthieu Vergne <vergne@fbk.eu>
 * 
 * @param <Input>
 */
public interface RestrictedUsability<Input> {

	/**
	 * 
	 * @param input
	 *            the {@link Input} to check
	 * @return <code>true</code> if the {@link Input} provided can be used,
	 *         <code>false</code> otherwise
	 */
	public boolean isApplicableOn(Input input);

}
