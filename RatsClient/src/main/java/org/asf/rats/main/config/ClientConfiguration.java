package org.asf.rats.main.config;

import java.io.IOException;

import org.asf.cyan.api.config.Configuration;
import org.asf.cyan.api.config.annotations.Comment;

/**
 * 
 * Main Client Configuration
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
@Comment("WARNING!")
@Comment("At the time of writing, CCFG does not support value overwriting!")
@Comment("When a configuration changes programmatically, it will be re-generated entirely, comments will get lost!")
@Comment("")
@Comment("RaTs Client Configuration File.")
@Comment("You can configure settings such as the server port here.")
public class ClientConfiguration extends Configuration<ClientConfiguration> {

	private static ClientConfiguration instance;

	public static ClientConfiguration getInstance() {
		if (instance == null) {
			instance = new ClientConfiguration();
			try {
				instance.readAll();
			} catch (IOException e) {
			}
		}
		return instance;
	}

	private ClientConfiguration() {
		super(System.getProperty("rats.config.dir") == null ? baseDir : System.getProperty("rats.config.dir"));
	}

	@Override
	public String filename() {
		return "client.ccfg";
	}

	@Override
	public String folder() {
		return "";
	}

	@Comment("RaTs service port")
	public int servicePort = 18120;

	@Comment("Client connection ping interval (in miliseconds)")
	public int connectionPingInterval = 5000;

	@Comment("RaTs service information path")
	public String servicePath = "/usr/lib/rats/.rats-service";
}
