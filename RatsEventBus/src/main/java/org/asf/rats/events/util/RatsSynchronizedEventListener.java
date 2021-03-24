package org.asf.rats.events.util;

import java.util.Arrays;

import org.asf.aos.util.service.extra.slib.smcore.SlibLogHandler;
import org.asf.cyan.api.common.CyanComponent;
import org.asf.cyan.api.events.core.ISynchronizedEventListener;

public abstract class RatsSynchronizedEventListener extends CyanComponent implements ISynchronizedEventListener {

	@Override
	public void received(Object... params) {
		if (params.length != 0 && params[0] instanceof SlibLogHandler) {
			received((SlibLogHandler) params[0], Arrays.copyOfRange(params, 1, params.length));
		}
	}

	public abstract void received(SlibLogHandler logger, Object... params);

}
