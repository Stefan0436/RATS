package org.asf.rats.main;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.asf.aos.util.service.extra.slib.communication.SlibClient;
import org.asf.aos.util.service.extra.slib.communication.SlibPacket;
import org.asf.aos.util.service.extra.slib.communication.client.ClientPacketProcessor;
import org.asf.aos.util.service.extra.slib.math.NUMID;
import org.asf.aos.util.service.extra.slib.smcore.SlibLogHandler;
import org.asf.aos.util.service.extra.slib.smcore.SlibLogger;
import org.asf.rats.main.config.ClientConfiguration;
import org.asf.rats.main.logging.ClientLogger;
import org.asf.rats.main.processors.LoggerProcessor;
import org.asf.rats.service.packet.PacketBuilder;

public class ClientMain {

	private static SlibClient client = null;
	private static SlibLogHandler logger = null;

	private static final String version = "1.0.0.A1";
	private static boolean overrideClient = false;

	private static String serverPath = null;
	private static int port = 18120;

	private static boolean completed = false;
	private static Thread netCheckThread = new Thread(() -> {
		while (true) {
			if (client == null)
				break;

			if (!client.hasConnection()) {
				client.disconnect();
				break;
			}

			try {
				Thread.sleep(ClientConfiguration.getInstance().connectionPingInterval);
			} catch (InterruptedException | ThreadDeath e) {
			}
		}
	}, "RaTs! Client Connection Checker");

	public static void main(String[] args) throws IOException {
		if (System.getProperty("ideMode") != null) {
			System.setProperty("log4j2.configurationFile", ClientMain.class.getResource("/log4j2-client-ide.xml").toString());
		} else {
			System.setProperty("log4j2.configurationFile", ClientMain.class.getResource("/log4j2-client.xml").toString());
		}

		logger = new ClientLogger();
		SlibLogger.addLogger(logger);

		port = ClientConfiguration.getInstance().servicePort;
		if (!overrideClient) {
			serverPath = ClientConfiguration.getInstance().servicePath;
		}

		client = new SlibClient(serverPath);
		client.setPort(port);
		client.setMode(true);

		client.registerProcessor(new LoggerProcessor());
		
		client.registerProcessor(new ClientPacketProcessor() {

			@Override
			public String display() {
				return "dispatch-completion-processor";
			}

			@Override
			public BigInteger getPacketId() {
				return NUMID.encode("dispatch-completed");
			}

			@Override
			public void process(byte[] packetKey, byte[] content, int contentlength, DatagramSocket service,
					InetAddress address, int port, SlibClient client) {
				completed = true;
				client.disconnect();
			}

		});

		client.connect();
		while (client.getAOSAddress().equals("unknown")) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}

		PacketBuilder builder = new PacketBuilder();
		for (String arg : args) {
			builder.add(arg);
		}
		client.sendPacket(new SlibPacket("event-dispatcher", "dispatch", builder.build()));

		try {
			netCheckThread.start();
		} catch (Exception e) {

		}
		while (!completed) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}

		if (!overrideClient) {
			System.exit(0);
			return;
		}

		client.disconnect();
		client = null;

		SlibLogger.removeLogger(logger.id());
	}

	public static String getVersion() {
		return version;
	}

	public static void setPath(String clientPath) {
		serverPath = clientPath;
		overrideClient = true;
	}

	public static SlibLogHandler getLogger() {
		return logger;
	}

}
