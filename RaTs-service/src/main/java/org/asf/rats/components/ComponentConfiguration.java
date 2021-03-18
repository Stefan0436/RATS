package org.asf.rats.components;

import java.util.HashMap;

import org.asf.cyan.api.config.Configuration;

public class ComponentConfiguration extends Configuration<ComponentConfiguration> {

	@Override
	public String filename() {
		return "components.ccfg";
	}

	@Override
	public String folder() {
		return "";
	}

	public HashMap<String, String> components;

	public ComponentConfiguration() {
		components = new HashMap<String, String>();
		components.put("", "");
	}
}
