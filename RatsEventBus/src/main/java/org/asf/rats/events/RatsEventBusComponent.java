package org.asf.rats.events;

import org.asf.cyan.api.common.CYAN_COMPONENT;
import org.asf.cyan.api.common.CyanComponent;
import org.asf.cyan.api.events.core.IEventListener;
import org.asf.rats.Memory;
import org.asf.rats.events.internal.ListenerList;

@CYAN_COMPONENT
public class RatsEventBusComponent extends CyanComponent {
	protected static void initComponent() {
		Memory.getInstance().getOrCreate("eventmanager.eventbus.factory").assign(new RatsEventBusFactory());
	}
}
