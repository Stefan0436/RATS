package org.asf.rats.http.providers;

import org.asf.rats.HttpRequest;
import org.asf.rats.HttpResponse;
import org.asf.rats.http.FileContext;

/**
 * 
 * File extension provider - rewrites file output based on the file's extension.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public interface IFileExtensionProvider {
	
	/**
	 * Creates a new instance of the extension provider.
	 */
	public IFileExtensionProvider newInstance();

	/**
	 * The file extension that is supported
	 * 
	 * @return File extension (example: .php)
	 */
	public String fileExtension();

	/**
	 * Rewrites a file request.
	 * 
	 * @param input   Input response
	 * @param request Input request.
	 * @return FileContext containing the rewritten file request.
	 */
	public FileContext rewrite(HttpResponse input, HttpRequest request);
	
}
