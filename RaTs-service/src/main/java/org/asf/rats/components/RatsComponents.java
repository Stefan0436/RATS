package org.asf.rats.components;

import java.io.IOException;

import org.asf.cyan.api.common.CyanComponent;

/**
 * 
 * RATS Components, configuration-based CyanComponents implementation.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class RatsComponents extends CyanComponent {

	private static boolean init = false;
	private static ComponentConfiguration conf;

	public static boolean isInitialized() {
		return init;
	}

	protected static void initComponent() {
		init = true;
		conf = new ComponentConfiguration();
		try {
			conf.readAll();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void setupComponents() {
		if (init)
			throw new IllegalStateException("RaTs! components have already been initialized.");

		if (LOG == null)
			initLogger();
	}

	@Override
	protected void preInitAllComponents() {
		trace("INITIALIZE all components, caller: " + CallTrace.traceCallName());
	}

	@Override
	protected void finalizeComponents() {
	}

	@Override
	protected Class<?>[] getComponentClasses() {
		return new Class<?>[] { RatsComponents.class };
	}

	public static void initializeComponents() throws IllegalStateException {
		RatsComponents impl = new RatsComponents();
		impl.initializeComponentClasses();
	}

}
