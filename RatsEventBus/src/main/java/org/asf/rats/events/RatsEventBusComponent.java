package org.asf.rats.events;

import org.asf.cyan.api.common.CYAN_COMPONENT;
import org.asf.cyan.api.common.CyanComponent;
import org.asf.rats.Memory;

/**
 * 
 * RaTs! Event Component, internal use only
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
@CYAN_COMPONENT
public class RatsEventBusComponent extends CyanComponent {
	protected static void initComponent() {
		Memory.getInstance().getOrCreate("eventmanager.eventbus.factory").assign(new RatsEventBusFactory());
		Memory.getInstance().getOrCreate("bootstrap.call").append(new Runnable() {

			@Override
			public void run() {
				EventManager.init();
			}
			
		});
	}
}
