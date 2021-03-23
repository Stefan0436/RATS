package org.asf.rats.service.eventdispatcher;

import java.io.IOException;

import org.asf.aos.util.service.extra.slib.communication.SlibClient;
import org.asf.aos.util.service.extra.slib.communication.SlibPacket;
import org.asf.aos.util.service.extra.slib.smcore.SlibLogHandler;
import org.asf.rats.service.packet.PacketBuilder;

public class NetworkLogger implements SlibLogHandler {

	private SlibClient client;
	private boolean stop = false;
	
	public boolean isConnected() {
		check();
		return !stop;
	}

	public NetworkLogger(SlibClient client) {
		this.client = client;
	}

	@Override
	public String id() {
		return "Network Logger for CLIENT: " + client.getAOSAddress() + (stop ? " (disconnected)" : "");
	}

	private void check() {
		if (client == null)
			stop = true;
		if (!stop && !client.hasConnection())
			stop = true;
	}

	@Override
	public void error(String message, String channel) {
		check();
		if (!stop) {
			try {
				client.sendPacket(
						new SlibPacket("log-network", new PacketBuilder().add(channel).add("error").add(message).build()));
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void warn(String message, String channel) {
		check();
		if (!stop) {
			try {
				client.sendPacket(
						new SlibPacket("log-network", new PacketBuilder().add(channel).add("warn").add(message).build()));
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void normal(String message, String channel) {
		check();
		if (!stop) {
			try {
				client.sendPacket(
						new SlibPacket("log-network", new PacketBuilder().add(channel).add("normal").add(message).build()));
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void info(String message, String channel) {
		check();
		if (!stop) {
			try {
				client.sendPacket(
						new SlibPacket("log-network", new PacketBuilder().add(channel).add("info").add(message).build()));
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void debug(String message, String channel) {
		check();
		if (!stop) {
			try {
				client.sendPacket(
						new SlibPacket("log-network", new PacketBuilder().add(channel).add("debug").add(message).build()));
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void verbose(String message, String channel) {
		check();
		if (!stop) {
			try {
				client.sendPacket(
						new SlibPacket("log-network", new PacketBuilder().add(channel).add("verbose").add(message).build()));
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void trace(String message, String channel) {
		check();
		if (!stop) {
			try {
				client.sendPacket(
						new SlibPacket("log-network", new PacketBuilder().add(channel).add("trace").add(message).build()));
			} catch (IOException e) {
			}
		}
	}

}
