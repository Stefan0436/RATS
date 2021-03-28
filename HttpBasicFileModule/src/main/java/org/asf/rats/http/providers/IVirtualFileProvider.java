package org.asf.rats.http.providers;

import java.net.Socket;

import org.asf.rats.HttpRequest;
import org.asf.rats.HttpResponse;

/**
 * 
 * Virtual file provider - a system for virtual HTTP files that uses the Basic
 * File Context system.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public interface IVirtualFileProvider {

	/**
	 * Checks if the provider is compatible with the given path and request
	 * 
	 * @param path    Input path (real path in the context folder)
	 * @param request HTTP Request
	 * @return True if compatible, false otherwise.
	 */
	public boolean match(String path, HttpRequest request);

	/**
	 * Creates a new instance of the provider
	 */
	public IVirtualFileProvider newInstance();

	/**
	 * Processes the file request,
	 * 
	 * @param path          Input path (real path in the context folder)
	 * @param postMediaType Media type, null if not a post request (supportsPost
	 *                      needs to be true in order to use this)
	 * @param request       HTTP Request
	 * @param response      HTTP Response
	 * @param client        Client making the request
	 */
	public void process(String path, String postMediaType, HttpRequest request, HttpResponse response, Socket client);

	public default boolean supportsPost() {
		return false;
	}

}
