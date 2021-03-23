package org.asf.rats.main.processors;

import java.math.BigInteger;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.asf.aos.util.service.extra.slib.communication.SlibClient;
import org.asf.aos.util.service.extra.slib.communication.client.ClientPacketProcessor;
import org.asf.aos.util.service.extra.slib.math.NUMID;
import org.asf.aos.util.service.extra.slib.smcore.SlibLogger;
import org.asf.rats.main.ClientMain;
import org.asf.rats.service.packet.PacketParser;

public class LoggerProcessor extends ClientPacketProcessor {

	@Override
	public String display() {
		return "Network Logger";
	}

	@Override
	public BigInteger getPacketId() {
		return NUMID.encode("log-network");
	}

	@Override
	public void process(byte[] packetKey, byte[] content, int contentlength, DatagramSocket service,
			InetAddress address, int port, SlibClient client) {
		String level = null;
		String msg = null;
		String channel = null;
		
		try {
			PacketParser parser = new PacketParser();
			parser.setSupportedVersion(1l);
			parser.importArray(content);

			channel = parser.getEntries()[0].get().toString();
			level = parser.getEntries()[1].get().toString();
			msg = parser.getEntries()[2].get().toString();
		} catch (Exception e) {
			SlibLogger.warn("Invalid log package received, unable to parse it, error: " + e.getClass().getTypeName()
					+ ": " + e.getMessage(), "CLIENT");
		}
		
		switch (level) {
		case "error":
			ClientMain.getLogger().error(msg, channel);
			break;
		case "warn":
			ClientMain.getLogger().warn(msg, channel);
			break;
		case "normal":
			ClientMain.getLogger().normal(msg, channel);
			break;
		case "info":
			ClientMain.getLogger().info(msg, channel);
			break;
		case "debug":
			ClientMain.getLogger().debug(msg, channel);
			break;
		case "verbose":
			ClientMain.getLogger().verbose(msg, channel);
			break;
		case "trace":
			ClientMain.getLogger().trace(msg, channel);
			break;
		}
	}

}
