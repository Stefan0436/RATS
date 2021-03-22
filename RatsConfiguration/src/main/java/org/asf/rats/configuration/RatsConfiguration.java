package org.asf.rats.configuration;

import java.io.IOException;

import org.asf.cyan.api.config.Configuration;
import org.asf.cyan.api.config.annotations.Comment;

/**
 * 
 * Main RaTs! Configuration
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
@Comment("WARNING!")
@Comment("At the time of writing, CCFG does not support value overwriting!")
@Comment("When a configuration changes programmatically, it will be re-generated entirely, comments will get lost!")
@Comment("")
@Comment("RaTs Main Configuration File.")
@Comment("You can configure settings such as the server port here.")
public class RatsConfiguration extends Configuration<RatsConfiguration> {

	private static RatsConfiguration instance;

	public static RatsConfiguration getInstance() {
		if (instance == null) {
			instance = new RatsConfiguration();
			try {
				instance.readAll();
			} catch (IOException e) {
			}
		}
		return instance;
	}

	private RatsConfiguration() {
	}

	@Override
	public String filename() {
		return "rats.ccfg";
	}

	@Override
	public String folder() {
		return "";
	}

	@Comment("RaTs service port,")
	@Comment("the rats command line interface connects with this port")
	public int servicePort = 18120;

	@Comment("Client connection ping interval (in miliseconds)")
	public int connectionPingInterval = 5000;
}
