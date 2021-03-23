package org.asf.rats.events;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.asf.aos.util.service.extra.slib.smcore.SlibLogHandler;
import org.asf.aos.util.service.extra.slib.util.ArrayUtil;
import org.asf.cyan.api.common.CyanComponent;
import org.asf.cyan.api.events.IEventProvider;

import org.asf.cyan.api.events.core.EventBus;
import org.asf.cyan.api.events.core.EventBusFactory;
import org.asf.cyan.api.events.core.IEventListener;

import org.asf.rats.Memory;
import org.asf.rats.MirrorLogger;
import org.asf.rats.components.RatsComponents;
import org.asf.rats.events.util.IPromotedEventProvider;

/**
 * 
 * RaTs! Event Manager - dispatches events and attaches listeners.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class EventManager extends CyanComponent {
	private static EventBus mainEventBus;
	private static EventBusFactory<?> eventBusFactory;
	private static HashMap<String, IPromotedEventProvider> promotedProviders = new HashMap<String, IPromotedEventProvider>();

	private static boolean init = false;

	/**
	 * Initializes the event manager, registers all event providers. Called with any
	 * event method if not initialized before.
	 */
	public static void init() {
		if (init)
			return;

		info("Initializing EventManager...");
		eventBusFactory = Memory.getInstance().getOrCreate("eventmanager.eventbus.factory")
				.getValue(EventBusFactory.class);
		mainEventBus = eventBusFactory.createBus("rats.events");

		init = true;
		
		for (Class<IEventProvider> provider : RatsComponents.findClasses(IEventProvider.class)) {
			try {
				IEventProvider prov = provider.getConstructor().newInstance();
				if (prov instanceof IPromotedEventProvider) {
					promotedProviders.put(provider.getTypeName(), (IPromotedEventProvider) prov);
				}
				createEventChannel(prov.getChannelName());
				if (prov instanceof IEventListener) {
					info("Auto-registering event listener to provider...");
					attachEventListener(prov.getChannelName(), (IEventListener) prov);
				}
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				error("Failed to register event provider: " + provider.getTypeName(), e);
			}
		}
	}

	/**
	 * Retrieves a promoted event provider by its class.
	 * 
	 * @param provider Event provider class
	 * @return IPromotedEventProvider instance.
	 */
	public static IPromotedEventProvider getPromotedProvider(Class<? extends IEventProvider> provider) {
		if (IPromotedEventProvider.class.isAssignableFrom(provider)) {
			if (!promotedProviders.containsKey(provider.getTypeName()))
				throw new IllegalStateException("The given provider has not been registered!");
			else {
				return promotedProviders.get(provider.getTypeName());
			}
		} else {
			throw new IllegalStateException("The given provider has not been promoted!");
		}
	}

	/**
	 * Dispatches an event
	 * 
	 * @param event  Event channel
	 * @param params Event parameters (the logger is automatically added if not
	 *               present as first)
	 */
	public static void dispatchEvent(String event, Object... params) {
		if (params.length == 0 || !(params[0] instanceof SlibLogHandler)) {
			if (params.length == 0) {
				params = new Object[] { Memory.getInstance().get("logging.bindings").getValue(SlibLogHandler.class) };
			} else {
				params = (Object[]) ArrayUtil.insert(params, 0,
						new Object[] { Memory.getInstance().get("logging.bindings").getValue(SlibLogHandler.class) });
			}
		} else {
			params[0] = new MirrorLogger((SlibLogHandler) params[0],
					Memory.getInstance().get("logging.bindings").getValue(SlibLogHandler.class));
		}

		getEventChannel(event).dispatch(params);
	}

	/**
	 * Attaches an event listener.<br />
	 * <b>Warning:</b> by default, events are not supported and need to be
	 * implemented.
	 * 
	 * @param event    Event name
	 * @param listener Event listener
	 */
	public static void attachEventListener(String event, IEventListener listener) {
		debug("Attaching event listener " + listener.getListenerName() + " to event " + event + "...");
		getEventChannel(event).attachListener(listener);
	}

	private static EventBus getEventChannel(String name) {
		init();
		return getBusRecursive(name, CallTrace.traceCallName());
	}

	private static EventBus getBusRecursive(String name, String caller) {
		name = name.toLowerCase();

		if (getBus(mainEventBus.getChannel() + "." + name) == null) {
			throw new IllegalStateException("Event channel " + name + " could not be found, caller: " + caller);
		}

		return getBus(mainEventBus.getChannel() + "." + name);
	}

	private static EventBus getBus(String channel) {
		EventBus bus = mainEventBus;
		while (bus != null) {
			if (bus.getChannel().equals(channel)) {
				return bus;
			}

			bus = eventBusFactory.getChildBus(bus);
		}
		return null;
	}

	private static void createEventChannel(String name) {
		name = name.toLowerCase();

		if (getBus(mainEventBus.getChannel() + "." + name) != null)
			throw new IllegalStateException(
					"Event channel " + name + " already registered. Caller: " + CallTrace.traceCallName());

		info("Creating event channel " + mainEventBus.getChannel() + "." + name + "...");
		eventBusFactory.createBus(mainEventBus.getChannel() + "." + name);
	}
}
