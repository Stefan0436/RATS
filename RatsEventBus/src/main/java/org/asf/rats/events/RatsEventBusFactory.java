package org.asf.rats.events;

import org.asf.cyan.api.events.core.EventBusFactory;

/**
 * 
 * RaTs! Event Bus Factory - creates event busses, you can access the factory
 * from the 'eventmanager.eventbus.factory' key in {@link org.asf.rats.Memory
 * Memory}.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class RatsEventBusFactory extends EventBusFactory<RatsEventBus> {

	RatsEventBusFactory() {

	}

	@Override
	public RatsEventBus createBus(String channel) {
		return createInstance(RatsEventBus.class).assign(channel);
	}

}
