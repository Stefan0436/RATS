package org.asf.rats.http.providers;

import org.asf.rats.ConnectiveHTTPServer;

/**
 * 
 * An interface to allow aliases, file extensions and restrictions to access the
 * executing service.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public interface IServerProviderExtension {
	public void provide(ConnectiveHTTPServer server);
}
