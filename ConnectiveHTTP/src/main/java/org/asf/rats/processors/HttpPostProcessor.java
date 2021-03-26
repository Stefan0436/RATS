package org.asf.rats.processors;

import java.io.InputStream;
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
		process(null, client);
	}
	
	/**
	 * Retrieves the body text of the post request.
	 * @return Body text.
	 */
	protected String getBody() {
		return getRequest().getBody();
	}

	/**
	 * Retrieves the body input stream. (unusable if getBody was called)
	 * @return Body input stream.
	 */
	protected InputStream getBodyStream() {
		return getRequest().getBodyStream();
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
	 * @param client      Client used to contact the server.
	 */
	public abstract void process(String contentType, Socket client);

	/**
	 * Creates an instance for processing HTTP requests.
	 * 
	 * @return New instance of this processor.F
	 */
	public abstract HttpPostProcessor createNewInstance();

}
