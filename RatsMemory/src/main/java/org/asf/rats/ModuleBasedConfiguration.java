package org.asf.rats;

import java.util.HashMap;

import org.asf.cyan.api.config.Configuration;
import org.asf.cyan.api.config.annotations.Comment;

/**
 * 
 * Shared configuration system for module systems, allowing ConnectiveHTTP
 * modules to run with RATS!
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public abstract class ModuleBasedConfiguration<T extends ModuleBasedConfiguration<T>> extends Configuration<T> {

	public ModuleBasedConfiguration(String base) {
		super(base);
		Memory.getInstance().getOrCreate("memory.modules.shared.config").assign(this);
	}

	@Comment("Module configuration, modules should use this map for configuration.")
	public HashMap<String, String> modules = new HashMap<String, String>();

}
