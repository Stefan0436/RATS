package org.asf.rats.service;

import org.asf.aos.util.service.extra.slib.communication.SlibClient;
import org.asf.aos.util.service.extra.slib.smcore.SlibManager;
import org.asf.rats.configuration.RatsConfiguration;

/**
 * 
 * Client manager thread, pings all clients to check if they are still connected.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class ClientManagerThread implements Runnable {
	public boolean up = false;
	public boolean exit = false;

	@Override
	public void run() {
		up = true;
		while (!exit) {
			for (SlibClient client : SlibManager.getService().getClients()) {
				if (!client.hasConnection()) {
					SlibManager.getService().disconnectClient(client);
				}
			}
			try {
				for (int i = 0; i < RatsConfiguration.getInstance().connectionPingInterval / 10; i++) {
					if (exit)
						break;
					Thread.sleep(10);
				}
			} catch (InterruptedException e) {
			}
		}
		up = false;
	}

}
