package org.asf.rats.components;

import java.io.IOException;
import java.util.ArrayList;

import org.asf.cyan.api.common.CYAN_COMPONENT;
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
	
	public static String getMarker() {
		return "RATS";
	}

	public static boolean isInitialized() {
		return init;
	}

	private static boolean simpleInit = false;
	private static RatsComponents impl;
	
	public static void simpleInit() {
		if (simpleInit)
			return;

		simpleInit = true;
		impl = new RatsComponents();		
		impl.assignImplementation();
	}
	
	protected static void initComponent() {
		init = true;
	}

	@Override
	protected void setupComponents() {
		if (init)
			throw new IllegalStateException("Components have already been initialized.");

		conf = new ComponentConfiguration();
		try {
			conf.readAll();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

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

	protected Class<?>[] getKnownClasses() {
		return conf.toClassArray();
	}

	@Override
	protected Class<?>[] getComponentClasses() {
		info("Searching for classes annotated with @CYAN_COMPONENT...");
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(getClass());

		for (Class<?> cls : getKnownClasses()) {
			if (cls.isAnnotationPresent(CYAN_COMPONENT.class)) {
				info("Found class: " + cls.getTypeName());
				classes.add(cls);
			}
		}

		return classes.toArray(t -> new Class[t]);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T> Class<T>[] findClassesInternal(Class<T> interfaceOrSupertype) {
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

		debug("Searching for classes of type: " + interfaceOrSupertype.getTypeName());
		for (Class<?> cls : getKnownClasses()) {
			if (interfaceOrSupertype.isAssignableFrom(cls)
					&& !interfaceOrSupertype.getTypeName().equals(cls.getTypeName())) {
				debug("Found class: " + cls.getTypeName());
				classes.add(cls);
			}
		}

		return classes.toArray(t -> new Class[t]);
	}

	public static void initializeComponents() throws IllegalStateException {
		simpleInit();
		impl.initializeComponentClasses();
	}

	public static <T> Class<T>[] findClasses(Class<T> supertypeOrInterface) {
		return findClasses(getMainImplementation(), supertypeOrInterface);
	}
}
