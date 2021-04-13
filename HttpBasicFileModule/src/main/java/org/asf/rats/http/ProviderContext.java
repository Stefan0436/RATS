package org.asf.rats.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.asf.rats.http.providers.FileUploadHandler;
import org.asf.rats.http.providers.IFileAlias;
import org.asf.rats.http.providers.IFileExtensionProvider;
import org.asf.rats.http.providers.IFileRestrictionProvider;
import org.asf.rats.http.providers.IVirtualFileProvider;
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

	protected String id = UUID.randomUUID() + "-" + System.currentTimeMillis();
	
	protected IndexPageProvider defaultIndexPage = null;
	protected HashMap<String, IndexPageProvider> altIndexPages = new HashMap<String, IndexPageProvider>();

	protected String sourceDirectory;
	protected ArrayList<IVirtualFileProvider> virtualFiles = new ArrayList<IVirtualFileProvider>();
	protected ArrayList<IFileAlias> aliases = new ArrayList<IFileAlias>();
	protected ArrayList<IFileExtensionProvider> extensions = new ArrayList<IFileExtensionProvider>();
	protected ArrayList<IFileRestrictionProvider> restrictions = new ArrayList<IFileRestrictionProvider>();
	protected ArrayList<FileUploadHandler> uploadHandlers = new ArrayList<FileUploadHandler>();
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

	public FileUploadHandler[] getUploadHandlers() {
		return uploadHandlers.toArray(t -> new FileUploadHandler[t]);
	}

	public IFileExtensionProvider[] getExtensions() {
		return extensions.toArray(t -> new IFileExtensionProvider[t]);
	}

	public IVirtualFileProvider[] getVirtualFiles() {
		return virtualFiles.toArray(t -> new IVirtualFileProvider[t]);
	}

	public String getID() {
		return id;
	}

}
