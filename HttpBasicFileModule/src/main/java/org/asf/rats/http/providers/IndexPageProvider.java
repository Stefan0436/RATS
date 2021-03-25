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
 * Index page provider, GET processor framework for index pages.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public abstract class IndexPageProvider {

	private ConnectiveHTTPServer server;
	private HttpResponse response;
	private HttpRequest request;

	private File[] indexFiles;
	private File[] indexDirectories;

	private String indexloc;

	private void assign(File[] files, File[] directories, ConnectiveHTTPServer server, HttpRequest request,
			HttpResponse response, String indexloc) {

		this.indexFiles = files;
		this.indexDirectories = directories;

		this.server = server;
		this.request = request;
		this.response = response;

		this.indexloc = indexloc;
	}
	
	public IndexPageProvider instanciate(File[] files, File[] directories, ConnectiveHTTPServer server, HttpRequest request,
			HttpResponse response, String indexloc) {
		IndexPageProvider provider = newInstance();
		provider.assign(files, directories, server, request, response, indexloc);
		return provider;
	}

	/**
	 * Creates a new instance of this provider
	 */
	protected abstract IndexPageProvider newInstance();

	/**
	 * Gets the folder path indexed (relative to the root source directory on disk)
	 * 
	 * @return Folder path as string
	 */
	protected String getFolderPath() {
		return indexloc;
	}

	/**
	 * Gets the files to be listed
	 * 
	 * @return Array of files.
	 */
	protected File[] getFiles() {
		return indexFiles;
	}

	/**
	 * Gets the directory to be listed
	 * 
	 * @return Array of directories.
	 */
	protected File[] getDirectories() {
		return indexDirectories;
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

	public abstract void process(Socket client, File[] directories, File[] files);
}
