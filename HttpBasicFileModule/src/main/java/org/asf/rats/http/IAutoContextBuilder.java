package org.asf.rats.http;

import java.util.Map;

import org.asf.rats.http.internal.implementation.DefaultIndexPage;
import org.asf.rats.http.providers.FileUploadHandler;
import org.asf.rats.http.providers.IFileAlias;
import org.asf.rats.http.providers.IFileExtensionProvider;
import org.asf.rats.http.providers.IFileRestrictionProvider;
import org.asf.rats.http.providers.IndexPageProvider;
import org.asf.rats.processors.HttpGetProcessor;

/**
 * 
 * Context provider - automatically creates basic file contexts.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public interface IAutoContextBuilder {

	/**
	 * Retrieves the real path of the supported folder.
	 */
	public String hostDir();

	/**
	 * Retrieves the virtual folder. (the url folder)
	 */
	public String virtualDir();

	/**
	 * Retrieves the default options for the provider context.
	 */
	public default int contextOptions() {
		return 0;
	}

	/**
	 * Retrieves the default index page provider.
	 */
	public default IndexPageProvider defaultIndexPage() {
		return new DefaultIndexPage();
	}

	/**
	 * Retrieves the alternative index pages.
	 */
	public default Map<String, IndexPageProvider> altIndexPages() {
		return Map.of();
	}

	/**
	 * Retrieves the default aliases.
	 */
	public default IFileAlias[] aliases() {
		return new IFileAlias[0];
	}

	/**
	 * Retrieves the default extension.
	 */
	public default IFileExtensionProvider[] extensions() {
		return new IFileExtensionProvider[0];
	}

	/**
	 * Retrieves the post request handlers.
	 */
	public default FileUploadHandler[] uploadHandlers() {
		return new FileUploadHandler[0];
	}

	/**
	 * Retrieves the default get request processors.
	 */
	public default HttpGetProcessor[] getHandlers() {
		return new HttpGetProcessor[0];
	}

	/**
	 * Retrieves the default restrictions.
	 */
	public default IFileRestrictionProvider[] restrictions() {
		return new IFileRestrictionProvider[0];
	}

}
