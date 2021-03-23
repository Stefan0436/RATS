package org.asf.rats.events;

import org.asf.cyan.api.events.core.EventBus;
import org.asf.cyan.api.events.core.IEventListener;
import org.asf.rats.events.internal.ListenerList;

/**
 * 
 * RaTs! Event Bus, needs be constructed from the factory.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class RatsEventBus extends EventBus {

	private ListenerList listeners = new ListenerList();
	private String channel = "";

	protected RatsEventBus() {
	}

	/**
	 * Assign the channel, can only be called ONCE, the factory calls this.
	 * @param channel Channel to assign
	 * @return Self
	 */
	public RatsEventBus assign(String channel) {
		if (this.channel.isEmpty())
			this.channel = channel;

		return this;
	}

	@Override
	public String getChannel() {
		return channel;
	}

	@Override
	public void attachListener(IEventListener listener) {
		if (listeners.contains(listener))
			throw new IllegalArgumentException(
					"Listener conflict; listener name: " + listener.getListenerName() + ", channel: " + getChannel());

		listeners.add(listener);
	}

	@Override
	public void dispatch(Object... params) {
		for (IEventListener listener : listeners) {
			callListener(listener, params);
		}
	}

	protected static RatsEventBus getNewInstance() {
		return new RatsEventBus();
	}

}
