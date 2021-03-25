package org.asf.rats.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.asf.rats.http.providers.FilePostHandler;
import org.asf.rats.http.providers.IFileAlias;
import org.asf.rats.http.providers.IFileExtensionProvider;
import org.asf.rats.http.providers.IFileRestrictionProvider;
import org.asf.rats.http.providers.IndexPageProvider;
import org.asf.rats.processors.HttpGetProcessor;

/**
 * 
 * Provider Context - needed for the basic file module.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class ProviderContext {

	protected ProviderContext() {
	}

	protected IndexPageProvider defaultIndexPage = null;
	protected HashMap<String, IndexPageProvider> altIndexPages = new HashMap<String, IndexPageProvider>();

	protected String sourceDirectory;
	protected ArrayList<IFileAlias> aliases = new ArrayList<IFileAlias>();
	protected ArrayList<IFileExtensionProvider> extensions = new ArrayList<IFileExtensionProvider>();
	protected ArrayList<IFileRestrictionProvider> restrictions = new ArrayList<IFileRestrictionProvider>();
	protected ArrayList<FilePostHandler> postHandlers = new ArrayList<FilePostHandler>();
	protected ArrayList<HttpGetProcessor> processors = new ArrayList<HttpGetProcessor>();

	public HttpGetProcessor[] getProcessors() {
		return processors.toArray(t -> new HttpGetProcessor[t]);
	}

	public IndexPageProvider getDefaultIndexPage() {
		return defaultIndexPage;
	}

	public IFileAlias[] getAliases() {
		return aliases.toArray(t -> new IFileAlias[t]);
	}

	public IFileRestrictionProvider[] getRestrictions() {
		return restrictions.toArray(t -> new IFileRestrictionProvider[t]);
	}

	public String getSourceDirectory() {
		return sourceDirectory;
	}

	public Map<String, IndexPageProvider> getAltIndexPages() {
		return new HashMap<String, IndexPageProvider>(altIndexPages);
	}

	public FilePostHandler[] getPostHandlers() {
		return postHandlers.toArray(t -> new FilePostHandler[t]);
	}

	public IFileExtensionProvider[] getExtensions() {
		return extensions.toArray(t -> new IFileExtensionProvider[t]);
	}

}
