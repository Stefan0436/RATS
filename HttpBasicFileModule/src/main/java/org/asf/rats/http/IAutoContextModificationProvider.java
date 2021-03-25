package org.asf.rats.http;

/**
 * 
 * Context modification provider interface, for automatic detection.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public interface IAutoContextModificationProvider {

	/**
	 * Applies the module code to a factory.
	 * 
	 * @param factory Provider context factory
	 */
	public void accept(ProviderContextFactory factory);

}
