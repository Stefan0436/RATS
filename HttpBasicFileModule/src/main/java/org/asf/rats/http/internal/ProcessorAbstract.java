package org.asf.rats.http.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;

import org.asf.rats.ConnectiveHTTPServer;
import org.asf.rats.HttpRequest;
import org.asf.rats.HttpResponse;
import org.asf.rats.http.FileProcessor;
import org.asf.rats.http.ProviderContext;

public abstract class ProcessorAbstract {

	private ConnectiveHTTPServer server;
	private String contextRoot;
	private ProviderContext context;

	private HttpResponse response;
	private HttpRequest request;

	private FileProcessor container;

	private static ProcessorAbstract implementation;

	protected ProcessorAbstract instanciateProcessor(HttpResponse response, HttpRequest request,
			ConnectiveHTTPServer server) {
		ProcessorAbstract inst = newInstance();

		inst.server = server;
		inst.request = request;
		inst.response = response;

		inst.contextRoot = contextRoot;
		inst.context = context;

		return inst;
	}

	public static ProcessorAbstract instanciateBase(String contextRoot, ProviderContext context) {
		ProcessorAbstract inst = implementation.newInstance();

		inst.contextRoot = contextRoot;
		inst.context = context;

		return inst;
	}

	/**
	 * Creates a new instance of the implementation for processing.
	 */
	protected abstract ProcessorAbstract newInstance();

	/**
	 * Checks if an implementation has been asigned.
	 */
	protected static boolean hasBeenAssigned() {
		return implementation != null;
	}

	/**
	 * Assigns the implementation used.
	 */
	protected static void assignImplementation(ProcessorAbstract impl) {
		implementation = impl;
	}

	/**
	 * Retrieves the context virtual root directory
	 */
	protected String getContextRoot() {
		return contextRoot;
	}

	/**
	 * Processes the file
	 * 
	 * @param path        File relative path (relative to the context root)
	 * @param contentType Content file type
	 * @param client      Client making the request
	 * @param method      Request method
	 */
	protected abstract void process(String path, String contentType, Socket client, String method);

	/**
	 * Retrieves the provider context
	 */
	protected ProviderContext getContext() {
		return context;
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
		if (response == null)
			response = new HttpResponse(200, "OK", getRequest()).addDefaultHeaders(getServer());

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

	/**
	 * Internal, do not use
	 */
	public void assignProcessorContainer(FileProcessor inst) {
		if (container == null) {
			container = inst;
			inst.connect((outputHandler, resourceProvider) -> {

				ConnectiveHTTPServer server = resourceProvider.server();

				String path = resourceProvider.path();
				String contentType = resourceProvider.contentType();
				String method = resourceProvider.method();

				Socket client = resourceProvider.client();

				HttpRequest request = resourceProvider.request();
				HttpResponse response = resourceProvider.response();

				path = path.substring(contextRoot.length());
				while (path.startsWith("/")) {
					path = path.substring(1);
				}

				ProcessorAbstract processor = instanciateProcessor(response, request, server);
				processor.process(path, contentType, client, method);

				outputHandler.write(processor.getRequest(), processor.getResponse());
			});
		}
	}

}
