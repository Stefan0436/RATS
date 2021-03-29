package org.asf.rats.http.providers;

import java.net.Socket;

/**
 * 
 * Interface providing the client socket to extensions, aliases and restrictions.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public interface IClientSocketProvider {
	public void provide(Socket client);
}
