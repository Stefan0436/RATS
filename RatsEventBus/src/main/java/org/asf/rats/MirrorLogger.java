package org.asf.rats;

import org.asf.aos.util.service.extra.slib.smcore.SlibLogHandler;

/**
 * 
 * Mirror logger, mirrors log from one logger to another's debug
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class MirrorLogger implements SlibLogHandler {
	private SlibLogHandler base;
	private SlibLogHandler mirror;

	public MirrorLogger(SlibLogHandler base, SlibLogHandler mirror) {
		this.base = base;
		this.mirror = mirror;
	}

	@Override
	public String id() {
		return base.id() + "-mirror-" + mirror.id();
	}

	@Override
	public void error(String message, String channel) {
		base.error(message, channel);
		mirror.debug("[MIRROR/ERROR] " + message, channel);
	}

	@Override
	public void warn(String message, String channel) {
		base.warn(message, channel);
		mirror.debug("[MIRROR/WARN] " + message, channel);
	}

	@Override
	public void normal(String message, String channel) {
		base.normal(message, channel);
		mirror.debug("[MIRROR/NORMAL] " + message, channel);
	}

	@Override
	public void info(String message, String channel) {
		base.info(message, channel);
		mirror.debug("[MIRROR/INFO] " + message, channel);
	}

	@Override
	public void debug(String message, String channel) {
		base.debug(message, channel);
		mirror.debug("[MIRROR/DEBUG] " + message, channel);
	}

	@Override
	public void verbose(String message, String channel) {
		base.verbose(message, channel);
		mirror.debug("[MIRROR/VERBOSE] " + message, channel);
	}

	@Override
	public void trace(String message, String channel) {
		base.trace(message, channel);
		mirror.debug("[MIRROR/TRACE] " + message, channel);
	}
}
