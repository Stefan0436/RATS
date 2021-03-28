package org.asf.rats.http.providers;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;

import org.asf.rats.ConnectiveHTTPServer;
import org.asf.rats.HttpRequest;
import org.asf.rats.HttpResponse;

/**
 * 
 * HTTP Post Provider - provides support for post requests.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public abstract class FilePostHandler {

	private ConnectiveHTTPServer server;
	private HttpResponse response;
	private HttpRequest request;

	private String loc;

	private void assign(ConnectiveHTTPServer server, HttpRequest request, HttpResponse response, String location) {
		this.server = server;
		this.request = request;
		this.response = response;

		this.loc = location;
	}

	public FilePostHandler instanciate(ConnectiveHTTPServer server, HttpRequest request, HttpResponse response,
			String location) {

		FilePostHandler handler = newInstance();
		handler.assign(server, request, response, location);
		return handler;
	}

	/**
	 * Retrieves the body text of the post request.
	 * 
	 * @return Body text.
	 */
	protected String getBody() {
		return getRequest().getBody();
	}

	/**
	 * Retrieves the body input stream. (unusable if getBody was called)
	 * 
	 * @return Body input stream.
	 */
	protected InputStream getBodyStream() {
		return getRequest().getBodyStream();
	}

	/**
	 * Creates a new instance of this handler
	 */
	protected abstract FilePostHandler newInstance();

	/**
	 * Checks if the handler is compatible.
	 * 
	 * @param request HTTP Request.
	 * @param path    Input path.
	 * @return True if compatible, false otherwise.
	 */
	public abstract boolean match(HttpRequest request, String path);

	/**
	 * Processes the post request.
	 * 
	 * @param contentType Content type.
	 * @param client      Client sending the request.
	 */
	public abstract void process(String contentType, Socket client);

	/**
	 * Checks if this processor supports directories, false will let the default
	 * system handle the directory.
	 */
	public boolean supportsDirectories() {
		return false;
	}

	/**
	 * Gets the REAL file path (relative to the root source directory on disk)
	 * 
	 * @return Folder path as string
	 */
	protected String getFolderPath() {
		return loc;
	}

	/**
	 * Retrieves the request HTTP headers.
	 * 
	 * @return Request HTTP headers.
	 */
	protected HashMap<String, String> getHeaders() {
		return getRequest().headers;
	}

	/**
	 * Retrieves a specific request HTTP header.
	 * 
	 * @return HTTP header value.
	 */
	protected String getHeader(String name) {
		return getRequest().headers.get(name);
	}

	/**
	 * Checks if a specific request HTTP header is present.
	 * 
	 * @return True if the header is present, false otherwise.
	 */
	protected boolean hasHeader(String name) {
		return getRequest().headers.containsKey(name);
	}

	/**
	 * Assigns the value of the given HTTP header.
	 * 
	 * @param header Header name
	 * @param value  Header value
	 */
	protected void setResponseHeader(String header, String value) {
		getResponse().setHeader(header, value);
	}

	/**
	 * Retrieves the server processing the request.
	 * 
	 * @return ConnectiveHTTPServer instance.
	 */
	protected ConnectiveHTTPServer getServer() {
		return server;
	}

	/**
	 * Sets the response object
	 * 
	 * @param response HttpResponse instance.
	 */
	protected void setResponse(HttpResponse response) {
		this.response = response;
	}

	/**
	 * Sets the response body
	 * 
	 * @param type Content type
	 * @param body Response body string
	 */
	protected void setBody(String type, String body) {
		getResponse().setContent(type, body);
	}

	/**
	 * Sets the response body (plaintext)
	 * 
	 * @param body Response body string
	 */
	protected void setBody(String body) {
		setBody("text/plain", body);
	}

	/**
	 * Sets the response body (binary)
	 * 
	 * @param body Response body string
	 */
	protected void setBody(byte[] body) {
		getResponse().setContent("application/octet-stream", body);
	}

	/**
	 * Sets the response body (InputStream)
	 * 
	 * @param body Response body string
	 * @throws IOException If reading the available bytes fails.
	 */
	protected void setBody(InputStream body) throws IOException {
		getResponse().setContent("application/octet-stream", body);
	}

	/**
	 * Sets the response code.
	 * 
	 * @param code Response status code.
	 */
	protected void setResponseCode(int code) {
		getResponse().status = code;
	}

	/**
	 * Sets the response message.
	 * 
	 * @param message Response message.
	 */
	protected void setResponseMessage(String message) {
		getResponse().message = message;
	}

	/**
	 * Retrieves the HTTP request object.
	 * 
	 * @return HttpRequest instance.
	 */
	protected HttpRequest getRequest() {
		return request;
	}

	/**
	 * Retrieves the HTTP response object.
	 * 
	 * @return HttpResponse instance.
	 */
	public HttpResponse getResponse() {
		return response;
	}

	/**
	 * Retrieves the HTTP request 'path'
	 * 
	 * @return Request path
	 */
	public String getRequestPath() {
		return getRequest().path;
	}

}
