package org.asf.rats.service.clientmanager.events;

import org.asf.cyan.api.events.IEventProvider;

public class ConnectionEventProvider implements IEventProvider {

	@Override
	public String getChannelName() {
		return "client.connected";
	}

}
