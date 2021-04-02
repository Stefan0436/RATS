package org.asf.rats.http.providers;

/**
 * 
 * An interface to allow extensions to access the context root (virtual root)
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public interface IContextRootProviderExtension {

	// did not use the name 'provide' because there is another provider that provides a String
	public void provideVirtualRoot(String virtualRoot);

}
