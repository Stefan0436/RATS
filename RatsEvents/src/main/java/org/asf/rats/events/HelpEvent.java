package org.asf.rats.events;

import org.asf.aos.util.service.extra.slib.smcore.SlibLogHandler;
import org.asf.cyan.api.events.IEventProvider;
import org.asf.cyan.api.events.core.BiTypedSynchronizedEventListener;
import org.asf.rats.components.RatsComponents;
import org.asf.rats.events.util.IPromotedEventProvider;

public class HelpEvent implements IPromotedEventProvider, BiTypedSynchronizedEventListener<SlibLogHandler, String> {

	@Override
	public String getChannelName() {
		return "help";
	}

	@Override
	public String getListenerName() {
		return "Help Command Processor";
	}

	@Override
	public String getDescription() {
		return "shows a list of known events";
	}

	@Override
	public String getSyntax() {
		return "[event-path]";
	}

	@Override
	public void received(Object... params) {
		try {
			if (params.length == 1) {
				received((SlibLogHandler) params[0]);
				return;
			} else if (params.length >= 2) {
				received((SlibLogHandler) params[0], params[1].toString());
				return;
			}
		} catch (ClassCastException e) {

		}
		throw new IllegalArgumentException("Invalid event arguments, expected: SlibLogHandler and optionally a String");
	}

	@Override
	public void received(SlibLogHandler logger) {
		displayHelp(logger, "");
	}

	@Override
	public void received(SlibLogHandler logger, String filter) {
		displayHelp(logger, filter);
	}

	private void displayHelp(SlibLogHandler logger, String filter) {
		StringBuilder builder = new StringBuilder();
		for (Class<? extends IEventProvider> provider : RatsComponents.findClasses(IEventProvider.class)) {
			if (IPromotedEventProvider.class.isAssignableFrom(provider)
					&& !provider.getTypeName().equals(IPromotedEventProvider.class.getTypeName())) {
				IPromotedEventProvider prov = EventManager.getPromotedProvider(provider);
				boolean match = true;

				if (filter.contains("*") && !filter.equals("*")) {
					match = parseMatch(filter, prov.getChannelName());
				} else if (!filter.isEmpty() && !filter.equals("*")) {
					if (!filter.contains(prov.getChannelName())) {
						match = false;
					}
				}

				if (match) {
					builder.append(System.lineSeparator()).append("- ").append(prov.getChannelName())
							.append(prov.getSyntax().isEmpty() ? "" : " " + prov.getSyntax()).append(" - ")
							.append(prov.getDescription());
				}
			}
		}

		logger.info("List of events matching your input:" + builder, "HELP");
	}

	public static boolean parseMatch(String filter, String matcher) {
		String before = filter.substring(0, filter.indexOf("*"));
		String after = filter.substring(filter.indexOf("*") + 1);
		if (after.equals(""))
			return matcher.startsWith(before);

		if (!matcher.startsWith(before))
			return false;

		if (after.contains("*")) {
			if (!matcher.contains(after.substring(0, after.indexOf("*")))) {
				return false;
			}
			return parseMatch(after, matcher.substring(matcher.indexOf(after.substring(0, after.indexOf("*")))));
		}

		return matcher.endsWith(after);
	}

}
