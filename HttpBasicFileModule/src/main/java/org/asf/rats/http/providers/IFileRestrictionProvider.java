package org.asf.rats.http.providers;

import org.asf.rats.HttpRequest;
import org.asf.rats.HttpResponse;

/**
 * 
 * File restriction provider - to secure file storage where needed.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public interface IFileRestrictionProvider {

	/**
	 * Creates a new instance of the restriction provider.
	 */
	public IFileRestrictionProvider newInstance();

	/**
	 * Checks if it is possible that the restriction can be applied to the given
	 * path, this method is intended for path checking only.
	 * 
	 * @param file    Input file
	 * @param request HTTP request.
	 * @return True if the restriction needs to be checked, false otherwise.
	 */
	public boolean match(HttpRequest request, String file);

	/**
	 * Checks file access, false disconnects the client with a 403, true allows the
	 * request.<br />
	 * <br />
	 * <b>WARNING:</b> Windows paths are case-insensitive, make sure you keep that
	 * in mind when writing a restriction provider!
	 * 
	 * @param file    File (or directory) to check
	 * @param request HTTP Request.
	 * @return True if access is granted, false otherwise.
	 */
	public boolean checkRestriction(String file, HttpRequest request);

	/**
	 * Retrieves the response code if this restriction does not check out.
	 */
	public default int getResponseCode(HttpRequest request) {
		return 403;
	}

	/**
	 * Retrieves the response message if this restriction does not check out.
	 */
	public default String getResponseMessage(HttpRequest request) {
		return "Access denied";
	}

	/**
	 * Rewrites the response if this restriction does not check out.
	 */
	public default void rewriteResponse(HttpRequest request, HttpResponse response) {
	}

}
