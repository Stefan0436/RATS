package org.asf.rats.components;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.asf.cyan.api.config.Configuration;
import org.asf.cyan.api.config.annotations.Comment;

/**
 * 
 * RaTs! Component Configuration Class
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
@Comment("WARNING!")
@Comment("At the time of writing, CCFG does not support value overwriting!")
@Comment("When a configuration changes programmatically, it will be re-generated entirely, comments will get lost!")
@Comment("")
@Comment("RaTs Component Configuration,")
@Comment("All classes specified here can be accessed from RatsComponents.")
@Comment("Supertypes and interfaces will also be recognized.")
public class ComponentConfiguration extends Configuration<ComponentConfiguration> {

	private static ComponentConfiguration instance;
	private URLClassLoader loader;
	private ArrayList<Class<?>> classCache = new ArrayList<Class<?>>();

	public static ComponentConfiguration getInstance() {
		return instance;
	}

	@Override
	public String filename() {
		return "components.ccfg";
	}

	@Override
	public String folder() {
		return "";
	}

	@Comment({ "Map of known classes", "Format: (source)> '(class)'",
			"Using ':<class>[/<path>]' allows for the use of class locations." })
	public HashMap<String, String> classes = new HashMap<String, String>();

	@Comment({ "Map of known classes (loads before the classes map)", "Format: (source)> '(class)'",
			"Using ':<class>[/<path>]' allows for the use of class locations." })
	public HashMap<String, String> earlyClasses = new HashMap<String, String>();

	/**
	 * Retrieves the class loader used for component loader
	 */
	public ClassLoader getClassLoader() {
		return (loader == null ? getClass().getClassLoader() : loader);
	}

	@Override
	protected ComponentConfiguration readAll(String content, boolean allowWrite, boolean newfile) {
		ComponentConfiguration conf = super.readAll(content, allowWrite, newfile);

		ArrayList<String> classes = new ArrayList<String>();
		ArrayList<URL> urls = new ArrayList<URL>();
		classCache.clear();

		conf.earlyClasses.forEach((k, v) -> {
			String source = v;
			if (source.startsWith(":")) {
				String clsSource = source.substring(1,
						(source.contains("/") ? source.lastIndexOf("/") : source.length()));
				String pathSuffix = "";
				try {
					source = new File(getClassLoader().loadClass(clsSource).getProtectionDomain().getCodeSource()
							.getLocation().toURI()).getAbsolutePath();
				} catch (ClassNotFoundException | URISyntaxException e) {
				}
				if (v.contains("/")) {
					source = source.substring(0, source.lastIndexOf(File.pathSeparator));
					pathSuffix = v.substring(v.indexOf("/"));
				}
				source += pathSuffix;
			}
			try {
				if (!source.startsWith("/")) {
					source = (System.getProperty("rats.config.dir") == null ? baseDir
							: System.getProperty("rats.config.dir")) + "/" + source;
				}
				urls.add(new File(source).toURI().toURL());
			} catch (MalformedURLException e) {
			}
			classes.add(k);
		});

		conf.classes.forEach((k, v) -> {
			String source = v;
			if (source.startsWith(":")) {
				String clsSource = source.substring(1,
						(source.contains("/") ? source.lastIndexOf("/") : source.length()));
				String pathSuffix = "";
				try {
					source = new File(getClassLoader().loadClass(clsSource).getProtectionDomain().getCodeSource()
							.getLocation().toURI()).getAbsolutePath();
				} catch (ClassNotFoundException | URISyntaxException e) {
				}
				if (v.contains("/")) {
					source = source.substring(0, source.lastIndexOf(File.pathSeparator));
					pathSuffix = v.substring(v.indexOf("/"));
				}
				source += pathSuffix;
			}
			try {
				if (!source.startsWith("/")) {
					source = (System.getProperty("rats.config.dir") == null ? baseDir
							: System.getProperty("rats.config.dir")) + "/" + source;
				}
				urls.add(new File(source).toURI().toURL());
			} catch (MalformedURLException e) {
			}
			classes.add(k);
		});

		loader = new URLClassLoader(urls.toArray(t -> new URL[t]), getClass().getClassLoader());
		for (String cls : classes) {
			try {
				Class<?> clazz = loader.loadClass(cls);

				classCache.add(clazz);
				if (!classCache.contains(clazz.getSuperclass())) {
					classCache.add(clazz.getSuperclass());
				}
				for (Class<?> inter : clazz.getInterfaces()) {
					if (!classCache.contains(inter)) {
						classCache.add(inter);
					}
				}
			} catch (ClassNotFoundException e) {
			}
		}

		return conf;
	}

	public ComponentConfiguration() {
		super(System.getProperty("rats.config.dir") == null ? baseDir : System.getProperty("rats.config.dir"));

		instance = this;
		if (!exists()) {
			InputStream strm = getClass().getResourceAsStream("/components.ccfg");
			Scanner sc = new Scanner(strm);
			StringBuilder cont = new StringBuilder();
			while (sc.hasNext()) {
				cont.append(sc.nextLine() + System.lineSeparator());
			}
			sc.close();
			try {
				Files.writeString(Path.of("components.ccfg"), cont);
				strm.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Retrieves all known classes
	 */
	public Class<?>[] toClassArray() {
		return classCache.toArray(t -> new Class[t]);
	}
}
