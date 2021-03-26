package org.asf.rats.http.providers;

import org.asf.rats.http.ProviderContext;

/**
 * 
 * An interface to allow aliases, file extensions, index pages and restrictions
 * to access the executing context.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public interface IContextProviderExtension {
	public void provide(ProviderContext context);
}
