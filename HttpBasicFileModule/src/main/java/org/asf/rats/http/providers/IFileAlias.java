package org.asf.rats.http.providers;

import org.asf.rats.HttpRequest;

/**
 * 
 * Request alias, rewrites the input location if it matches.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public interface IFileAlias {

	/**
	 * Creates a new instance of the alias provider.
	 */
	public IFileAlias newInstance();

	/**
	 * Checks if the alias is compatible.
	 * 
	 * @param request HTTP Request.
	 * @param input   Input path.
	 * @return True if compatible, false otherwise.
	 */
	public boolean match(HttpRequest request, String input);

	/**
	 * Rewrites the input request.
	 * 
	 * @param request HTTP Request.
	 * @param input   Request string.
	 * @return Rewritten request path.
	 */
	public String rewrite(HttpRequest request, String input);

}
