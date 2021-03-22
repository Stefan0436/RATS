package org.asf.rats.service.clientmanager;

import org.asf.aos.util.service.extra.slib.communication.SlibClient;
import org.asf.aos.util.service.extra.slib.communication.SlibPacket;
import org.asf.aos.util.service.extra.slib.communication.SlibUtilService;
import org.asf.aos.util.service.extra.slib.processors.ServicePacketProcessor;
import org.asf.rats.service.RatsClientManager;

public class ClientManagerConnectedProcessor extends ServicePacketProcessor<RatsClientManager> {

	@Override
	public String processorID() {
		return "connection";
	}

	@Override
	public String packetID() {
		return "connected";
	}

	@Override
	public boolean autoRegister() {
		return true;
	}

	@Override
	public void processPacket(byte[] packetKey, SlibPacket packet, SlibClient client, SlibUtilService server,
			RatsClientManager module) {
	}

}
