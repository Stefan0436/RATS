package org.asf.rats.service.clientmanager.events;

import org.asf.cyan.api.events.IEventProvider;

public class DisconnectionEventProvider implements IEventProvider {

	@Override
	public String getChannelName() {
		return "client.disconnected";
	}

}
