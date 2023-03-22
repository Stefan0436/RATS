package org.asf.rats.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

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
	private static ArrayList<Consumer<RatsConfiguration>> loadHooks = new ArrayList<Consumer<RatsConfiguration>>();

	public static void addLoadHook(Consumer<RatsConfiguration> hook) {
		loadHooks.add(hook);
	}

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
		super(System.getProperty("rats.config.dir") == null ? baseDir : System.getProperty("rats.config.dir"));
		for (Consumer<RatsConfiguration> call : loadHooks) {
			call.accept(this);
		}
	}

	@Override
	public String filename() {
		return "rats.ccfg";
	}

	@Override
	public String folder() {
		return "";
	}

	@Override
	public RatsConfiguration readAll(String content, boolean allowWrite, boolean newfile) {
		for (Consumer<RatsConfiguration> call : loadHooks) {
			call.accept(this);
		}
		return super.readAll(content, allowWrite, newfile);
	}

	@Comment("RaTs service port,")
	@Comment("the rats command line interface connects with this port")
	public int servicePort = 18120;

	@Comment("Client connection ping interval (in miliseconds)")
	public int connectionPingInterval = 5000;

	@Comment("HTTP Server Port")
	public int httpPort = 8080;

	@Comment("Module configuration, modules should use this map for configuration.")
	@Comment("Format goes as following:")
	@Comment("")
	@Comment("module> {")
	@Comment("    (config map)")
	@Comment("}")
	public HashMap<String, HashMap<String, String>> modules = new HashMap<String, HashMap<String, String>>();
}
