package org.asf.rats.service;

import java.io.IOException;

import org.asf.aos.util.service.ServiceModule;
import org.asf.aos.util.service.UtilService;
import org.asf.aos.util.service.extra.slib.communication.SlibClient;
import org.asf.aos.util.service.extra.slib.communication.SlibPacket;
import org.asf.aos.util.service.extra.slib.communication.SlibUtilService;
import org.asf.aos.util.service.extra.slib.smcore.SlibManager;
import org.asf.aos.util.service.extra.slib.smcore.SlibModule;

public class RatsClientManager extends ServiceModule implements SlibModule {
	private Thread managerThread = new Thread(new ClientManagerThread());

	@Override
	public String displayName() {
		return "Rats Client Manager";
	}

	@Override
	public void preLoad() {

	}

	@Override
	public void load() {

	}

	@Override
	@SuppressWarnings("deprecation")
	public void postInit() {
		managerThread.start();
		SlibUtilService service = SlibManager.getService();
		SlibManager.getService().registerPostProcessor((packet, client) -> {
			if (client == null) {
				SlibClient netclient = SlibManager.getService().createServerClient(packet.getReplyAddress(),
						packet.getReplyPort(), packet.getPID(), packet.getKey(), displayName());
				service.addClient(netclient);
				service.dispatchConnectionEvent(netclient);
				try {
					netclient.sendPacket(new SlibPacket("assign-address", netclient.getAOSAddress().getBytes()));
				} catch (IOException e) {
				}
			}
		});
		service.addExitHook(() -> {
			for (SlibClient client : service.getClients()) {
				service.disconnectClient(client);
			}
			managerThread.stop();
		});
	}

	@Override
	public String id() {
		return "slib-clientmanager";
	}

	@Override
	public void start(UtilService service) {

	}

}
