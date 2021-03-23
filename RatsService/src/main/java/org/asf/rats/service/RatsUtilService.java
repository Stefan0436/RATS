package org.asf.rats.service;

import java.util.function.Consumer;

import org.asf.aos.util.service.extra.slib.communication.SlibClient;
import org.asf.aos.util.service.extra.slib.communication.SlibUtilService;

/**
 * 
 * RaTs! Util Service, allows for client events.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class RatsUtilService extends SlibUtilService {
	/**
	 * Attaches an event listener to the client connection handler
	 * 
	 * @param listener Event listener
	 */
	public void attachClientConnectionEventListener(Consumer<SlibClient> listener) {
		connectedEvents.add(listener);
	}

	/**
	 * Attaches an event listener to the client disconnection handler
	 * 
	 * @param listener Event listener
	 */
	public void attachClientDisconnectionEventListener(Consumer<SlibClient> listener) {
		disconnectedEvents.add(listener);
	}
}
