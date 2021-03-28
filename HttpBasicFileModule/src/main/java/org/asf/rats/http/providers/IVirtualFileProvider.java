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
	 * @param path            Input path (real path in the context folder)
	 * @param uploadMediaType Media type, null if not a post or put request
	 *                        (supportsPost needs to be true in order to use this)
	 * @param request         HTTP Request
	 * @param response        HTTP Response
	 * @param client          Client making the request
	 * @param method Request method (either GET, POST, PUT or DELETE)
	 */
	public void process(String path, String uploadMediaType, HttpRequest request, HttpResponse response, Socket client,
			String method);

	public default boolean supportsUpload() {
		return false;
	}

}
