package org.asf.rats;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.Random;

import javax.net.ssl.SSLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asf.rats.processors.HttpGetProcessor;
import org.asf.rats.processors.HttpUploadProcessor;

/**
 * 
 * HTTP Connected Clients
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class ConnectedClient {
	protected static String[] uploadMethods = new String[] { "POST", "PUT", "DELETE", "PATCH" };

	protected static int timeout = 5;
	protected static int maxRequests = 0;

	protected Socket client;
	protected OutputStream output;
	protected InputStream input;

	protected ConnectiveHTTPServer server;

	protected Thread executionThread = null;
	protected Thread keepAliveProcessor = null;

	protected int requestNumber = 0;
	private Logger logger = LogManager.getLogger("connective-http");

	public ConnectedClient(Socket client, InputStream input, OutputStream output, ConnectiveHTTPServer server) {
		this.server = server;
		this.client = client;
		this.input = input;
		this.output = output;
		executionThread = new Thread(() -> receive(), "Client processor " + client);
	}

	/**
	 * Closes the client connection
	 * 
	 * @param request The request used
	 * @param status  Status code
	 * @param message Status message
	 * @return HttpResponse instance.
	 * @throws IOException If transmitting the response fails
	 */
	public synchronized HttpResponse closeConnection(HttpRequest request, int status, String message)
			throws IOException {
		HttpResponse resp = new HttpResponse(status, message, request);

		if (resp.getBodyStream() == null) {
			resp.setContent("text/html", server.genError(resp, request));
		}

		resp.addDefaultHeaders(server).setConnectionState("Closed").build(output);
		closeConnection();
		return resp;
	}

	/**
	 * Closes the client connection
	 * 
	 * @param response Http response
	 * @return HttpResponse instance.
	 * @throws IOException If transmitting the response fails
	 */
	public synchronized HttpResponse closeConnection(HttpResponse response, Socket client) throws IOException {
		response.addDefaultHeaders(server).setConnectionState("Closed").build(output);
		closeConnection();
		return response;
	}

	/**
	 * Closes the client connection, sends no response
	 */
	public synchronized void closeConnection() {
		try {
			client.close();
		} catch (IOException e) {
		}

		InputStream strm = input;
		OutputStream strmOut = output;

		try {
			strm.close();
		} catch (IOException e) {
		}
		try {
			strmOut.close();
		} catch (IOException e) {
		}

		server.clients.remove(this);
	}

	/**
	 * Processes HTTP Requests
	 * 
	 * @param msg Request
	 * @throws IOException If processing fails
	 */
	protected void processRequest(HttpRequest msg) throws IOException {
		receiving = true;

		// Handle client keep-alive
		boolean clientKeepAlive = false;
		if (msg.headers.containsKey("Connection") && Stream.of(msg.headers.get("Connection").split(", "))
				.anyMatch(t -> t.equalsIgnoreCase("Keep-Alive"))) {
			if (msg.headers.containsKey("Keep-Alive")) {
				// Set values from existing header
				String keepAliveInfo = msg.headers.get("Keep-Alive");
				timeout = 5;
				maxRequests = 0;
				for (String entry : keepAliveInfo.split(", ")) {
					// Parse
					if (entry.contains("=")) {
						String key = entry.substring(0, entry.indexOf("="));
						String value = entry.substring(entry.indexOf("=") + 1);
						switch (key) {

						case "timeout": {
							if (value.matches("^[0-9]+$"))
								timeout = Integer.parseInt(value);
							break;
						}
						case "max": {
							if (value.matches("^[0-9]+$"))
								maxRequests = Integer.parseInt(value);
							break;
						}

						}
					}
				}
			}

			// Keep alive
			clientKeepAlive = true;
		}

		// Handle request
		boolean compatible = false;
		ArrayList<HttpGetProcessor> getProcessorLst = new ArrayList<HttpGetProcessor>(server.getProcessors);
		ArrayList<HttpUploadProcessor> uploadProcessorLst = new ArrayList<HttpUploadProcessor>(server.uploadProcessors);

		for (HttpUploadProcessor proc : uploadProcessorLst) {
			if (proc.supportsGet()) {
				getProcessorLst.add(proc);
			}
		}

		if (Stream.of(uploadMethods).anyMatch(t -> t.equals(msg.method))) {
			HttpUploadProcessor impl = null;
			for (HttpUploadProcessor proc : uploadProcessorLst) {
				if (!proc.supportsChildPaths()) {
					String url = msg.path;
					if (!url.endsWith("/"))
						url += "/";

					String supportedURL = proc.path();
					if (!supportedURL.endsWith("/"))
						supportedURL += "/";

					if (url.equals(supportedURL)) {
						compatible = true;
						impl = proc;
						break;
					}
				}
			}
			if (!compatible) {
				uploadProcessorLst.sort((t1, t2) -> {
					return -Integer.compare(t1.path().split("/").length, t2.path().split("/").length);
				});
				for (HttpUploadProcessor proc : uploadProcessorLst) {
					if (proc.supportsChildPaths()) {
						String url = msg.path;
						if (!url.endsWith("/"))
							url += "/";

						String supportedURL = proc.path();
						if (!supportedURL.endsWith("/"))
							supportedURL += "/";

						if (url.startsWith(supportedURL)) {
							compatible = true;
							impl = proc;
							break;
						}
					}
				}
			}
			if (compatible) {
				HttpUploadProcessor processor = impl.instanciate(server, msg);
				processor.process((msg.headers.get("Content-Type") == null ? msg.headers.get("Content-type")
						: msg.headers.get("Content-Type")), client, msg.method);

				// Read remaining bytes
				if (msg.getRequestBodyStream() != null) {
					// Read all remaining bytes from the content stream
					LengthTrackingStream strm = msg.getRequestBodyStream();

					// FIXME: simplify when the header problem is solved
					if (msg.headers.containsKey("Content-Length")) {
						long length = Long.parseLong(msg.headers.get("Content-Length"));
						for (long i = strm.getBytesRead(); i < length; i++)
							strm.read();
					} else if (msg.headers.containsKey("Content-length")) {
						long length = Long.parseLong(msg.headers.get("Content-length"));
						for (long i = strm.getBytesRead(); i < length; i++)
							strm.read();
					} else if (msg.headers.containsKey("content-length")) {
						long length = Long.parseLong(msg.headers.get("content-Length"));
						for (long i = strm.getBytesRead(); i < length; i++)
							strm.read();
					}
				}

				HttpResponse resp = processor.getResponse();
				if (resp.status >= 400)
					logger.error(msg.version + " " + msg.method + " " + msg.path
							+ (msg.query == null || msg.query.isEmpty() ? "" : "?" + msg.query) + ": " + resp.status
							+ " " + resp.message + " [" + client.getRemoteSocketAddress() + "]");
				else
					logger.info(msg.version + " " + msg.method + " " + msg.path
							+ (msg.query == null || msg.query.isEmpty() ? "" : "?" + msg.query) + ": " + resp.status
							+ " " + resp.message + " [" + client.getRemoteSocketAddress() + "]");
				if (clientKeepAlive)
					resp.headers.put("Connection", "Keep-Alive");

				if ((!resp.headers.containsKey("Connection")
						|| !resp.headers.get("Connection").equalsIgnoreCase("Keep-Alive"))
						|| (maxRequests != 0 && requestNumber >= maxRequests))
					closeConnection(resp, client);
				else {
					resp.addDefaultHeaders(server);
					if (resp.headers.containsKey("Keep-Alive")) {
						// Set values from existing header
						String keepAliveInfo = resp.headers.get("Keep-Alive");
						timeout = 5;
						maxRequests = 0;
						for (String entry : keepAliveInfo.split(", ")) {
							// Parse
							if (entry.contains("=")) {
								String key = entry.substring(0, entry.indexOf("="));
								String value = entry.substring(entry.indexOf("=") + 1);
								switch (key) {

								case "timeout": {
									if (value.matches("^[0-9]+$"))
										timeout = Integer.parseInt(value);
									break;
								}
								case "max": {
									if (value.matches("^[0-9]+$"))
										maxRequests = Integer.parseInt(value);
									break;
								}

								}
							}
						}
					} else if (timeout != 0 || maxRequests != 0)
						resp.setHeader("Keep-Alive", "timeout=" + timeout + ", max=" + maxRequests);
					if (maxRequests != 0)
						requestNumber++;
					else
						requestNumber = 1;
					rndT = rnd.nextInt();
					tsT = System.currentTimeMillis();
					keepAliveProcessor = new Thread(() -> keepAlive(), "Client keepalive " + client);
					keepAliveProcessor.start();
					resp.setConnectionState("Keep-Alive");
					resp.build(output);
					receiving = false;
				}
			}
		} else if (msg.method.equals("GET") || msg.method.equals("HEAD")) {
			HttpGetProcessor impl = null;
			for (HttpGetProcessor proc : getProcessorLst) {
				if (!proc.supportsChildPaths()) {
					String url = msg.path;
					if (!url.endsWith("/"))
						url += "/";

					String supportedURL = proc.path();
					if (!supportedURL.endsWith("/"))
						supportedURL += "/";

					if (url.equals(supportedURL)) {
						compatible = true;
						impl = proc;
						break;
					}
				}
			}
			if (!compatible) {
				getProcessorLst.sort((t1, t2) -> {
					return -Integer.compare(t1.path().split("/").length, t2.path().split("/").length);
				});
				for (HttpGetProcessor proc : getProcessorLst) {
					if (proc.supportsChildPaths()) {
						String url = msg.path;
						if (!url.endsWith("/"))
							url += "/";

						String supportedURL = proc.path();
						if (!supportedURL.endsWith("/"))
							supportedURL += "/";

						if (url.startsWith(supportedURL)) {
							compatible = true;
							impl = proc;
							break;
						}
					}
				}
			}
			if (compatible) {
				HttpGetProcessor processor = impl.instanciate(server, msg);
				processor.process(client);

				// Read remaining bytes
				if (msg.getRequestBodyStream() != null) {
					// Read all remaining bytes from the content stream
					LengthTrackingStream strm = msg.getRequestBodyStream();

					// FIXME: simplify when the header problem is solved
					if (msg.headers.containsKey("Content-Length")) {
						long length = Long.parseLong(msg.headers.get("Content-Length"));
						for (long i = strm.getBytesRead(); i < length; i++)
							strm.read();
					} else if (msg.headers.containsKey("Content-length")) {
						long length = Long.parseLong(msg.headers.get("Content-length"));
						for (long i = strm.getBytesRead(); i < length; i++)
							strm.read();
					} else if (msg.headers.containsKey("content-length")) {
						long length = Long.parseLong(msg.headers.get("content-Length"));
						for (long i = strm.getBytesRead(); i < length; i++)
							strm.read();
					}
				}

				HttpResponse resp = processor.getResponse();
				if (resp.status >= 400)
					logger.error(msg.version + " " + msg.method + " " + msg.path
							+ (msg.query == null || msg.query.isEmpty() ? "" : "?" + msg.query) + ": " + resp.status
							+ " " + resp.message + " [" + client.getRemoteSocketAddress() + "]");
				else
					logger.info(msg.version + " " + msg.method + " " + msg.path
							+ (msg.query == null || msg.query.isEmpty() ? "" : "?" + msg.query) + ": " + resp.status
							+ " " + resp.message + " [" + client.getRemoteSocketAddress() + "]");
				if (clientKeepAlive)
					resp.headers.put("Connection", "Keep-Alive");

				if ((!resp.headers.containsKey("Connection")
						|| !resp.headers.get("Connection").equalsIgnoreCase("Keep-Alive"))
						|| (maxRequests != 0 && requestNumber >= maxRequests))
					closeConnection(resp, client);
				else {
					resp.addDefaultHeaders(server);
					if (resp.headers.containsKey("Keep-Alive")) {
						// Set values from existing header
						String keepAliveInfo = resp.headers.get("Keep-Alive");
						timeout = 5;
						maxRequests = 0;
						for (String entry : keepAliveInfo.split(", ")) {
							// Parse
							if (entry.contains("=")) {
								String key = entry.substring(0, entry.indexOf("="));
								String value = entry.substring(entry.indexOf("=") + 1);
								switch (key) {

								case "timeout": {
									if (value.matches("^[0-9]+$"))
										timeout = Integer.parseInt(value);
									break;
								}
								case "max": {
									if (value.matches("^[0-9]+$"))
										maxRequests = Integer.parseInt(value);
									break;
								}

								}
							}
						}
					} else
						resp.setHeader("Keep-Alive", "timeout=" + timeout + ", max=" + maxRequests);
					rndT = rnd.nextInt();
					tsT = System.currentTimeMillis();
					keepAliveProcessor = new Thread(() -> keepAlive(), "Client keepalive " + client);
					keepAliveProcessor.start();
					if (maxRequests != 0)
						requestNumber++;
					else
						requestNumber = 1;
					resp.setConnectionState("Keep-Alive");
					resp.build(output);
					receiving = false;
				}
			}
		}

		if (!compatible) {
			if (!msg.method.equals("GET") && !msg.method.equals("HEAD")) {
				logger.error(msg.version + " " + msg.method + " " + msg.path
						+ (msg.query == null || msg.query.isEmpty() ? "" : "?" + msg.query)
						+ ": 405 Unsupported request " + " [" + client.getRemoteSocketAddress() + "]");
				closeConnection(msg, 405, "Unsupported request");
			} else {
				logger.error(msg.version + " " + msg.method + " " + msg.path
						+ (msg.query == null || msg.query.isEmpty() ? "" : "?" + msg.query) + ": 404 Command not found "
						+ " [" + client.getRemoteSocketAddress() + "]");
				closeConnection(msg, 404, "Command not found");
			}
		}
	}

	public void beginReceive() {
		requestNumber = 0;
		receiving = false;
		executionThread.start();
	}

	protected boolean receiving = false;
	private static Random rnd = new Random();
	private int rndT = 0;
	private long tsT = 0;

	protected void keepAlive() {
		int rndTc = rndT;
		long tsTc = tsT;

		int current = 0;
		while (!receiving && tsTc == tsT && rndTc == rndT) {
			if (receiving || tsTc != tsT || rndTc != rndT)
				return;
			if (current >= timeout) {
				closeConnection();
				break;
			}
			current++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	protected void receive() {
		while (true) {
			receiving = false;
			HttpRequest msg = null;
			try {
				try {
					msg = HttpRequest.parse(input);
				} catch (Exception e) {
					HttpRequest dummy = new HttpRequest();
					dummy.version = server.getPreferredProtocol();
					closeConnection(dummy, 400, "Bad request");
					return;
				}
				receiving = true;
				if (msg == null) {
					HttpRequest dummy = new HttpRequest();
					dummy.version = server.getPreferredProtocol();

					closeConnection(dummy, 503, "Unsupported request");
					dummy.close();
				} else {
					// TODO: handle HTTP 2
					if (Double.valueOf(msg.version.substring("HTTP/".length())) < server.httpVersion) {
						HttpRequest dummy = new HttpRequest();
						dummy.version = msg.version;

						closeConnection(dummy, 505, "HTTP Version Not Supported");
						dummy.close();
					}

					processRequest(msg);
					msg.close();
				}
			} catch (IOException ex) {
				if (!server.connected || ex instanceof SSLException || ex instanceof SocketException)
					return;

				logger.error("Failed to process request", ex);
				try {
					if (msg == null) {
						msg = new HttpRequest();
						msg.version = server.getPreferredProtocol();
					}
					closeConnection(msg, 503, "Internal server error");
				} catch (IOException ex2) {
				}
			}

			if (requestNumber == 0)
				break;
		}
	}
}
