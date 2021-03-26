package org.asf.rats.http.providers;

/**
 * 
 * An interface to allow extensions to access the requested path
 * (alias-controlled, file path relative to the virtual root)
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public interface IPathProviderExtension {
	public void provide(String path);
}
