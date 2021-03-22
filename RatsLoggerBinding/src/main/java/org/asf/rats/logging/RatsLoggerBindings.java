package org.asf.rats.logging;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asf.aos.util.service.extra.slib.smcore.SlibLogHandler;
import org.asf.cyan.api.common.CYAN_COMPONENT;
import org.asf.rats.Memory;

/**
 * 
 * RaTs! Logger Bindings.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
@CYAN_COMPONENT
public class RatsLoggerBindings implements SlibLogHandler {

	private HashMap<String, Logger> loggers = new HashMap<String, Logger>();

	protected static void initComponent() {
		RatsLoggerBindings binding = new RatsLoggerBindings();
		Memory.getInstance().getOrCreate("logging.bindings").assign(binding);
	}
	
	private Logger logger(String channel) {
		if (!loggers.containsKey(channel)) {
			loggers.put(channel, LogManager.getLogger(channel));
		}
		return loggers.get(channel);
	}

	@Override
	public String id() {
		return "rats";
	}

	@Override
	public void error(String message, String channel) {
		logger(channel).error(message);
	}

	@Override
	public void warn(String message, String channel) {
		logger(channel).warn(message);
	}

	@Override
	public void normal(String message, String channel) {
		logger(channel).info(message);
	}

	@Override
	public void info(String message, String channel) {
		logger(channel).info(message);
	}

	@Override
	public void debug(String message, String channel) {
		logger(channel).debug(message);
	}

	@Override
	public void verbose(String message, String channel) {
		logger(channel).debug(message);
	}

	@Override
	public void trace(String message, String channel) {
		logger(channel).trace(message);
	}

}
