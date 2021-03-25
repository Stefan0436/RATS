package org.asf.rats.http;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 
 * Processor context factory for the basic file module.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class FileProcessorContextFactory {

	private static FileProcessorContextFactory defaultFactory = null;
	private ArrayList<ProviderContext> contexts = new ArrayList<ProviderContext>();

	/**
	 * Retrieves the default (shared) factory.
	 * 
	 * @return Default FileProcessorContextFactory instance.
	 */
	public static FileProcessorContextFactory getDefault() {
		if (defaultFactory == null) {
			defaultFactory = new FileProcessorContextFactory();
		}

		return defaultFactory;
	}

	/**
	 * Adds provider contexts to the factory.
	 * 
	 * @param context ProviderContext instance.
	 */
	public void addProviderContext(ProviderContext context) {
		contexts.add(context);
	}

	/**
	 * Builds the processor context.
	 * 
	 * @return ProcessorContext instance.
	 */
	public ProcessorContext build() {
		ProcessorContext context = new ProcessorContext();

		for (ProviderContext cont : contexts) {
			context.addAll(Arrays.asList(cont.getProcessors()));
		}

		return context;
	}

}
