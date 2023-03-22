package org.asf.rats.http.providers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;

import org.asf.rats.ConnectiveHTTPServer;
import org.asf.rats.HttpRequest;
import org.asf.rats.HttpResponse;

/**
 * 
 * HTTP Upload Provider - provides support for post, delete and put requests.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public abstract class FileUploadHandler {

	private ConnectiveHTTPServer server;
	private HttpResponse response;
	private HttpRequest request;
	private File source;

	private String loc;

	private void assign(ConnectiveHTTPServer server, HttpRequest request, HttpResponse response, String location,
			File source) {
		this.server = server;
		this.request = request;
		this.response = response;

		this.loc = location;
		this.source = source;
	}

	public FileUploadHandler instanciate(ConnectiveHTTPServer server, HttpRequest request, HttpResponse response,
			String location, File source) {

		FileUploadHandler handler = newInstance();
		handler.assign(server, request, response, location, source);
		return handler;
	}

	/**
	 * Retrieves the source file
	 */
	protected File getSourceFile() {
		return source;
	}

	/**
	 * Retrieves the body text of the request.
	 * 
	 * @return Body text.
	 */
	protected String getRequestBody() {
		return getRequest().getRequestBody();
	}

	/**
	 * Retrieves the body input stream. (unusable if getBody was called)
	 * 
	 * @return Body input stream.
	 */
	protected InputStream getRequestBodyStream() {
		return getRequest().getRequestBodyStream();
	}

	/**
	 * Creates a new instance of this handler
	 */
	protected abstract FileUploadHandler newInstance();

	/**
	 * Checks if the handler is compatible.
	 * 
	 * @param request HTTP Request.
	 * @param path    Input path.
	 * @return True if compatible, false otherwise.
	 */
	public abstract boolean match(HttpRequest request, String path);

	/**
	 * Processes the request.
	 * 
	 * @param contentType Content type.
	 * @param client      Client sending the request.
	 * @param method      Method used, either POST, PUT or DELETE.
	 * @return True if the method is supported, false otherwise.
	 */
	public abstract boolean process(String contentType, Socket client, String method);

	/**
	 * Checks if this processor supports directories, false will let the default
	 * system handle the directory.
	 */
	public boolean supportsDirectories() {
		return false;
	}

	/**
	 * Checks if the input source file needs to be closed in order for the processor
	 * to function.
	 */
	public boolean requiresClosedFile() {
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
	 * @param body Response body stream
	 * @throws IOException If reading the available bytes fails.
	 * @deprecated Deprecated, use setContent(type, body, length) or
	 *             setContent(body, length) instead
	 */
	@Deprecated
	protected HttpResponse setBody(InputStream body) throws IOException {
		return getResponse().setContent("application/octet-stream", body);
	}

	/**
	 * Sets the response body (InputStream)
	 * 
	 * @param type   Content type.
	 * @param body   Input stream.
	 * @param length Content length.
	 */
	public HttpResponse setContent(String type, InputStream body, long length) {
		return getResponse().setContent(type, body, length);
	}

	/**
	 * Sets the response body (InputStream)
	 * 
	 * @param body   Input stream.
	 * @param length Content length.
	 */
	public HttpResponse setContent(InputStream body, long length) {
		return getResponse().setContent(body, length);
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
