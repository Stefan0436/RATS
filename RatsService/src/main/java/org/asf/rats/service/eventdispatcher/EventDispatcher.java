package org.asf.rats.service.eventdispatcher;

import org.asf.aos.util.service.ServiceModule;
import org.asf.aos.util.service.UtilService;
import org.asf.aos.util.service.extra.slib.smcore.SlibModule;

public class EventDispatcher extends ServiceModule implements SlibModule {

	@Override
	public String displayName() {
		return "RaTs! Event Dispatcher";
	}

	@Override
	public void preLoad() {
	}

	@Override
	public void load() {
	}

	@Override
	public void postInit() {
	}

	@Override
	public String id() {
		return "event-dispatcher";
	}

	@Override
	public void start(UtilService service) {
	}

}
