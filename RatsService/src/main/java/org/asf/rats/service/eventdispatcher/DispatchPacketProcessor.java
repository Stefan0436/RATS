package org.asf.rats.service.eventdispatcher;

import java.io.IOException;
import java.util.ArrayList;

import org.asf.aos.util.service.extra.slib.communication.SlibClient;
import org.asf.aos.util.service.extra.slib.communication.SlibPacket;
import org.asf.aos.util.service.extra.slib.communication.SlibUtilService;
import org.asf.aos.util.service.extra.slib.processors.ServicePacketProcessor;
import org.asf.rats.events.EventManager;
import org.asf.rats.events.util.IPromotedEventProvider;
import org.asf.rats.events.util.ISyntaxSensitivePromotedEvent;
import org.asf.rats.service.packet.PacketEntry;
import org.asf.rats.service.packet.PacketParser;

public class DispatchPacketProcessor extends ServicePacketProcessor<EventDispatcher> {

	@Override
	public String processorID() {
		return "event-dispatcher";
	}

	@Override
	public String packetID() {
		return "dispatch";
	}

	@Override
	public boolean autoRegister() {
		return true;
	}

	@Override
	public void processPacket(byte[] packetKey, SlibPacket packet, SlibClient client, SlibUtilService server,
			EventDispatcher module) {

		PacketParser parser = new PacketParser();
		parser.setSupportedVersion(1l);
		parser.importArray(packet.getContent());

		String ch = null;
		ArrayList<Object> params = new ArrayList<Object>();
		NetworkLogger netLogger = new NetworkLogger(client);

		for (PacketEntry<?> ent : parser.getEntries()) {
			if (ch == null) {
				ch = ent.get().toString();
				params.add(netLogger);
			} else {
				params.add(ent.get());
			}
		}

		if (ch != null) {
			try {
				IPromotedEventProvider prov = EventManager.getPromotedProvider(ch);
				if (prov instanceof ISyntaxSensitivePromotedEvent) {
					boolean incorrectSyntax = false;
					ISyntaxSensitivePromotedEvent sProv = (ISyntaxSensitivePromotedEvent) prov;
					if (params.size() - 1 < sProv.minimalParameterCount() || (sProv.maximalParameterCount() != -1
							&& params.size() - 1 > sProv.maximalParameterCount())) {
						incorrectSyntax = true;
					} else {
						Class<?>[] classes = sProv.parameterTypes();
						for (int i = 1; i < params.size(); i++) {
							Class<?> clsParam = params.get(i).getClass();
							Class<?> clsExpected = classes[i - 1];
							if (!clsExpected.isAssignableFrom(clsParam)) {
								incorrectSyntax = true;
								break;
							}
						}
					}

					if (incorrectSyntax) {
						StringBuilder builder = new StringBuilder();
						builder.append(prov.getChannelName())
								.append(prov.getSyntax().isEmpty() ? "" : " " + prov.getSyntax()).append(" - ")
								.append(prov.getDescription());

						netLogger.error("Invalid usage!", "RATS-EVENT-MANAGER");
						netLogger.error("Usage: " + builder, "RATS-EVENT-MANAGER");
						ch = null;
					}
				}
			} catch (IllegalStateException ex) {
			}

			if (ch != null) {
				try {
					EventManager.dispatchEvent(ch, params.toArray(t -> new Object[t]));
				} catch (Exception e) {
					if (e instanceof IllegalStateException
							&& e.getMessage().startsWith("Event channel " + ch + " could not be found, caller: ")) {
						netLogger.error("Event not recognized! Use the help event for a list of events.",
								"RATS-EVENT-MANAGER");
					} else {
						StringBuilder stack = new StringBuilder();
						for (StackTraceElement element : e.getStackTrace()) {
							stack.append("\tat ").append(element).append(System.lineSeparator());
						}
						netLogger.error("Dispatch failed!\nException: " + e.getClass().getTypeName() + ": "
								+ e.getMessage() + "\n" + stack, "RATS-EVENT-MANAGER");
					}
				}
			}
		} else {
			netLogger.error("Event not recognized! Use the help event for a list of events.", "RATS-EVENT-MANAGER");
		}

		if (netLogger.isConnected()) {
			try {
				client.sendPacket(new SlibPacket("dispatch-completed", new byte[0]));
			} catch (IOException e) {
			}
		}

		if (client != null)
			server.disconnectClient(client);
	}

}
