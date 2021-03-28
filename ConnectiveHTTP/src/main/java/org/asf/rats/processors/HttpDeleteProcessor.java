package org.asf.rats.processors;

import org.asf.rats.ConnectiveHTTPServer;
import org.asf.rats.HttpRequest;

/**
 * 
 * HTTP Delete Request Processor, processes delete requests.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public abstract class HttpDeleteProcessor extends HttpGetProcessor {

	/**
	 * Instanciates a new processor with the server and request.
	 * 
	 * @param server  Server to use
	 * @param request HTTP request
	 * @return New HttpDeleteProcessor configured for processing.
	 */
	public HttpDeleteProcessor instanciate(ConnectiveHTTPServer server, HttpRequest request) {
		return (HttpDeleteProcessor) super.instanciate(server, request);
	}

	/**
	 * Creates an instance for processing HTTP requests.
	 * 
	 * @return New instance of this processor.
	 */
	public abstract HttpDeleteProcessor createNewInstance();

}
