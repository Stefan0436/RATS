package org.asf.rats.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.asf.rats.http.internal.implementation.DefaultIndexPage;
import org.asf.rats.http.internal.processors.MainFileProcessor;
import org.asf.rats.http.providers.FilePostHandler;
import org.asf.rats.http.providers.IFileAlias;
import org.asf.rats.http.providers.IFileExtensionProvider;
import org.asf.rats.http.providers.IFileRestrictionProvider;
import org.asf.rats.http.providers.IndexPageProvider;
import org.asf.rats.processors.HttpGetProcessor;

/**
 * 
 * Provider context factory for the basic file module.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class ProviderContextFactory {

	private static ProviderContextFactory defaultFactory = null;

	/**
	 * Retrieves the default (shared) factory.<br />
	 * <b>NOTE:</b> the default factory is always locked.
	 * 
	 * @return Default FileProcessorContextFactory instance.
	 */
	public static ProviderContextFactory getDefault() {
		if (defaultFactory == null) {
			defaultFactory = new ProviderContextFactory();
			defaultFactory.locked = true;
		}

		return defaultFactory;
	}

	private int options = 0;
	private boolean locked = false;

	private boolean hasOption(int opt) {
		return (opt & options) == opt;
	}

	/**
	 * Disables the default index page set by the factory.
	 */
	public static final int OPTION_DISABLE_DEFAULT_INDEX = 0x10;

	/**
	 * Disables the default http processor added by the factory.
	 */
	public static final int OPTION_DISABLE_MAIN_PROCESSOR = 0x20;

	/**
	 * Sets building options
	 * 
	 * @param option Options to set.
	 * @throws IllegalStateException If the factory has been locked
	 */
	public ProviderContextFactory setOption(int option) {
		if (locked) {
			throw new IllegalStateException("The factory has been locked!");
		}
		options = options | option;
		return this;
	}

	private String file = null;
	private String path = null;

	private IndexPageProvider defaultIndex = null;
	protected HashMap<String, IndexPageProvider> altIndexPages = new HashMap<String, IndexPageProvider>();

	private ArrayList<IFileAlias> aliases = new ArrayList<IFileAlias>();
	private ArrayList<IFileExtensionProvider> extensions = new ArrayList<IFileExtensionProvider>();
	private ArrayList<IFileRestrictionProvider> restrictions = new ArrayList<IFileRestrictionProvider>();
	private ArrayList<FilePostHandler> postHandlers = new ArrayList<FilePostHandler>();
	private ArrayList<HttpGetProcessor> extraProcessors = new ArrayList<HttpGetProcessor>();

	public ProviderContextFactory addProcessor(HttpGetProcessor processor) {
		extraProcessors.add(processor);
		return this;
	}

	public ProviderContextFactory addProcessors(HttpGetProcessor[] processors) {
		for (HttpGetProcessor itm : processors) {
			addProcessor(itm);
		}
		return this;
	}

	public ProviderContextFactory addProcessors(Iterable<HttpGetProcessor> processors) {
		for (HttpGetProcessor itm : processors) {
			addProcessor(itm);
		}
		return this;
	}

	public ProviderContextFactory addPostHandler(FilePostHandler handler) {
		postHandlers.add(handler);
		return this;
	}

	public ProviderContextFactory addPostHandlers(FilePostHandler[] handlers) {
		for (FilePostHandler itm : handlers) {
			addPostHandler(itm);
		}
		return this;
	}

	public ProviderContextFactory addPostHandlers(Iterable<FilePostHandler> handlers) {
		for (FilePostHandler itm : handlers) {
			addPostHandler(itm);
		}
		return this;
	}

	public ProviderContextFactory addAlias(IFileAlias alias) {
		aliases.add(alias);
		return this;
	}

	public ProviderContextFactory addAliases(IFileAlias[] aliases) {
		for (IFileAlias itm : aliases) {
			addAlias(itm);
		}
		return this;
	}

	public ProviderContextFactory addAliases(Iterable<IFileAlias> aliases) {
		for (IFileAlias itm : aliases) {
			addAlias(itm);
		}
		return this;
	}

	public ProviderContextFactory addExtension(IFileExtensionProvider extension) {
		extensions.add(extension);
		return this;
	}

	public ProviderContextFactory addExtensions(IFileExtensionProvider[] extensions) {
		for (IFileExtensionProvider ext : extensions) {
			addExtension(ext);
		}
		return this;
	}

	public ProviderContextFactory addExtensions(Iterable<IFileExtensionProvider> extensions) {
		for (IFileExtensionProvider ext : extensions) {
			addExtension(ext);
		}
		return this;
	}

	public ProviderContextFactory addRestriction(IFileRestrictionProvider restriction) {
		restrictions.add(restriction);
		return this;
	}

	public ProviderContextFactory addRestrictions(IFileRestrictionProvider[] restrictions) {
		for (IFileRestrictionProvider itm : restrictions) {
			addRestriction(itm);
		}
		return this;
	}

	public ProviderContextFactory addRestrictions(Iterable<IFileRestrictionProvider> restrictions) {
		for (IFileRestrictionProvider itm : restrictions) {
			addRestriction(itm);
		}
		return this;
	}

	/**
	 * Sets the execution location property of the context.
	 * 
	 * @param location Folder location
	 */
	public ProviderContextFactory setExecLocation(String location) {
		if (!location.startsWith("/")) {
			location = (System.getProperty("rats.config.dir") == null ? "." : System.getProperty("rats.config.dir"))
					+ "/" + location;
		}
		
		path = location;
		return this;
	}

	/**
	 * Sets the folder path that is processed by the HTTP processors.
	 * 
	 * @param path Folder name (the path after the host in the url)
	 */
	public ProviderContextFactory setRootFile(String path) {
		file = path;
		return this;
	}

	/**
	 * Sets the default index page.
	 * 
	 * @param indexPage Default index page provider
	 */
	public ProviderContextFactory setDefaultIndexPage(IndexPageProvider indexPage) {
		defaultIndex = indexPage;
		return this;
	}

	/**
	 * Builds the provider context. (locks the factory when building completes)
	 * 
	 * @return New ProviderContext instance.
	 * @throws IllegalStateException If the execution path has not been assigned.
	 *                               (also thrown if the root file is null without
	 *                               having OPTION_DISABLE_MAIN_PROCESSOR enabled)
	 */
	public ProviderContext build() {
		if (path == null)
			throw new IllegalStateException("Execution path not assigned");
		if (!hasOption(OPTION_DISABLE_MAIN_PROCESSOR) && path == null)
			throw new IllegalStateException("URL folder not assigned");

		ProviderContext context = new ProviderContext();
		context.sourceDirectory = path;
		context.defaultIndexPage = defaultIndex;

		if (context.defaultIndexPage == null && !hasOption(OPTION_DISABLE_DEFAULT_INDEX)) {
			context.defaultIndexPage = new DefaultIndexPage();
		}

		if (!hasOption(OPTION_DISABLE_MAIN_PROCESSOR)) {
			if (!file.startsWith("/"))
				file = "/" + file;
			
			context.processors.add(new MainFileProcessor(file, context));
		}

		context.aliases.addAll(aliases);
		context.extensions.addAll(extensions);
		context.postHandlers.addAll(postHandlers);
		context.restrictions.addAll(restrictions);
		context.processors.addAll(extraProcessors);
		context.altIndexPages.putAll(altIndexPages);

		locked = true;
		return context;
	}

	/**
	 * Adds an index page to the context
	 */
	public void addIndexPage(String path, IndexPageProvider page) {
		altIndexPages.put(path, page);
	}
	
	/**
	 * Adds a map of index pages to the context
	 */
	public void addIndexPages(Map<String, IndexPageProvider> altIndexPages) {
		altIndexPages.putAll(altIndexPages);
	}

}
