package org.asf.rats.http.providers;

import java.net.Socket;
import java.util.function.Consumer;

import org.asf.rats.HttpRequest;
import org.asf.rats.HttpResponse;

/**
 * 
 * HTTP Document Post-Processor -- run to amend any response of the text/html
 * type before sending to the client. (useful for global warnings, <b>does not
 * work with virtual files or low-level processors, only works with physical
 * files and extension processors</b>)
 * 
 * @author Sky Swimmer - AerialWorks Software Foundation
 * @since ASF Connective 1.0.0.A4
 *
 */
public interface IDocumentPostProcessor {

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
	public IDocumentPostProcessor newInstance();

	/**
	 * Processes the file request,
	 * 
	 * @param path            Input path (real path in the context folder)
	 * @param uploadMediaType Media type, null if not a post or put request
	 *                        (supportsPost needs to be true in order to use this)
	 * @param request         HTTP Request
	 * @param response        HTTP Response
	 * @param client          Client making the request
	 * @param method          Request method (either GET, POST, PUT or DELETE)
	 */
	public void process(String path, String uploadMediaType, HttpRequest request, HttpResponse response, Socket client,
			String method);

	/**
	 * Appends to the start of the document text
	 * 
	 * @param text Text to append
	 */
	public default void writeLine(String text) {
		write(text + "\n");
	}

	/**
	 * Appends to the start of the document text
	 * 
	 * @param text Text to append
	 */
	public default void write(String text) {
		getWriteCallback().accept(text);
	}

	/**
	 * Assigns the document append callback
	 * 
	 * @param callback Callback function
	 */
	public void setWriteCallback(Consumer<String> callback);

	/**
	 * Retrieves the document append callback
	 * 
	 * @return Callback function
	 */
	public Consumer<String> getWriteCallback();

}
