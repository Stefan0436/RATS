package org.asf.rats.processors;

import java.net.Socket;

import org.asf.rats.ConnectiveHTTPServer;
import org.asf.rats.HttpRequest;

/**
 * 
 * HTTP Post Request Processor, processes post requests.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public abstract class HttpPostProcessor extends HttpGetProcessor {

	@Override
	public void process(Socket client) {
		process(null, null, client);
	}
	
	/**
	 * Instanciates a new processor with the server and request.
	 * 
	 * @param server  Server to use
	 * @param request HTTP request
	 * @return New HttpGetProcessor configured for processing.
	 */
	public HttpPostProcessor instanciate(ConnectiveHTTPServer server, HttpRequest request) {
		return (HttpPostProcessor) super.instanciate(server, request);
	}

	/**
	 * Checks if the processor support get requests, false by default.
	 * 
	 * @return True if the processor supports this, false otherwise.
	 */
	public boolean supportsGet() {
		return false;
	}

	/**
	 * Processes a request, the post-specific parameters will be null if get is
	 * used.
	 * 
	 * @param contentType Content type
	 * @param body        Post body
	 * @param client      Client used to contact the server.
	 */
	public abstract void process(String contentType, String body, Socket client);

	/**
	 * Creates an instance for processing HTTP requests.
	 * 
	 * @return New instance of this processor.F
	 */
	public abstract HttpPostProcessor createNewInstance();

}
