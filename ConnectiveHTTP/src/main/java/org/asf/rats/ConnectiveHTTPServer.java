package org.asf.rats;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asf.rats.processors.HttpGetProcessor;
import org.asf.rats.processors.HttpUploadProcessor;

/**
 * 
 * ConnectiveHTTP Server, HTTP Server API. <b>Avoid direct construction, use the
 * factory instead: {@link ConnectiveServerFactory ConnectiveServerFactory}</b>
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class ConnectiveHTTPServer {

	// TODO:
	// Chunked content
	// Compressed content

	public ConnectiveHTTPServer() {
		try {
			ip = InetAddress.getByName("0.0.0.0");
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	protected double httpVersion = 1.1;
	protected String protocol = "HTTP/%v";

	/**
	 * Retrieves the preferred protocol version (HTTP/version)
	 */
	public String getPreferredProtocol() {
		return protocol.replace("%v", Double.toString(httpVersion));
	}

	protected String name = "ASF Connective";
	protected String version = "1.0.0.A12";

	protected boolean connected = false;
	protected ServerSocket socket = null;

	protected ArrayList<ConnectedClient> clients = new ArrayList<ConnectedClient>();

	protected ArrayList<HttpGetProcessor> getProcessors = new ArrayList<HttpGetProcessor>();
	protected ArrayList<HttpUploadProcessor> uploadProcessors = new ArrayList<HttpUploadProcessor>();

	protected int port = 8080;
	protected InetAddress ip = null;

	private Logger logger = LogManager.getLogger("connective-http");

	/**
	 * Implementation of the main server, assign to set a server implementation
	 */
	protected static ConnectiveHTTPServer implementation;

	/**
	 * Retrieves the instance of the auto-started server.
	 * 
	 * @return ConnectiveHTTPServer instance.
	 */
	public static ConnectiveHTTPServer getMainServer() {
		return implementation;
	}

	protected Thread serverProcessor = new Thread(() -> {
		while (connected) {
			try {
				Socket client = socket.accept();

				acceptConnection(client);
				InputStream in = getClientInput(client);
				OutputStream out = getClientOutput(client);

				ConnectedClient cl = new ConnectedClient(client, in, out, this);
				clients.add(cl);
				cl.beginReceive();
			} catch (IOException ex) {
				if (!connected)
					break;

				logger.error("Failed to process client", ex);
			}
		}
	}, "Connective server thread");

	/**
	 * Retrieves the client output stream (override only)
	 */
	protected OutputStream getClientOutput(Socket client) throws IOException {
		return client.getOutputStream();
	}

	/**
	 * Retrieves the client input stream (override only)
	 */
	protected InputStream getClientInput(Socket client) throws IOException {
		return client.getInputStream();
	}

	/**
	 * Properly transfers the request body to the given output stream, uses the
	 * Content-Length header if present.
	 * 
	 * @param headers    Request headers
	 * @param bodyStream Request body stream
	 * @param output     Output stream
	 * @throws IOException If transferring fails.
	 */
	public static void transferRequestBody(HashMap<String, String> headers, InputStream bodyStream, OutputStream output)
			throws IOException {
		if (headers.containsKey("Content-Length")) {
			long length = Long.valueOf(headers.get("Content-Length"));
			int tr = 0;
			for (long i = 0; i < length; i += tr) {
				tr = Integer.MAX_VALUE / 1000;
				if ((length - (long) i) < tr) {
					tr = bodyStream.available();
					if (tr == 0) {
						output.write(bodyStream.read());
						i += 1;
					}
					tr = bodyStream.available();
				}
				output.write(bodyStream.readNBytes(tr));
			}
		} else {
			bodyStream.transferTo(output);
		}
	}

	/**
	 * Called on client connect, potential override.
	 */
	protected void acceptConnection(Socket client) {
	}

	/**
	 * Called to construct a new server socket (override only)
	 */
	protected ServerSocket getServerSocket(int port, InetAddress ip) throws IOException {
		return new ServerSocket(port, 0, ip);
	}

	/**
	 * Generates an error html (bifunction connective.http.gen.error.provider is
	 * called with response and request as parameters)
	 * 
	 * @param response HTTP Response to use
	 * @return HTML String
	 */
	@SuppressWarnings("unchecked")
	public String genError(HttpResponse response, HttpRequest request) {
		if (Memory.getInstance().getOrCreate("connective.http.gen.error.provider").getValue(BiFunction.class) == null) {
			Memory.getInstance().getOrCreate("connective.http.gen.error.provider")
					.assign(new BiFunction<HttpResponse, HttpRequest, String>() {

						protected String htmlCache = null;

						@Override
						public String apply(HttpResponse response, HttpRequest request) {
							try {
								InputStream strm = getClass().getResource("/error.template.html").openStream();
								htmlCache = new String(strm.readAllBytes());
							} catch (Exception ex) {
								if (htmlCache == null)
									return "FATAL ERROR GENERATING PAGE: " + ex.getClass().getTypeName() + ": "
											+ ex.getMessage();
							}

							String str = htmlCache;

							str = str.replace("%path%", request.path);
							str = str.replace("%server-name%", getName());
							str = str.replace("%server-version%", getVersion());
							str = str.replace("%error-status%", Integer.toString(response.status));
							str = str.replace("%error-message%", response.message);

							return str;
						}

					});
		}
		return Memory.getInstance().getOrCreate("connective.http.gen.error.provider").getValue(BiFunction.class)
				.apply(response, request).toString();
	}

	/**
	 * Starts the server
	 * 
	 * @throws IOException if starting the server fails.
	 */
	public void start() throws IOException {
		if (socket != null)
			throw new IllegalStateException("Server already running!");

		connected = true;
		socket = getServerSocket(port, ip);
		serverProcessor.start();
	}

	/**
	 * Stops the server and disconnects all clients (abruptly)
	 */
	public void stop() {
		if (!connected)
			return;

		connected = false;
		for (ConnectedClient client : new ArrayList<ConnectedClient>(clients)) {
			client.closeConnection();
		}

		try {
			socket.close();
		} catch (IOException e) {
		}

		socket = null;
	}

	/**
	 * Sets the server port
	 * 
	 * @param port Server port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Sets the server ip address
	 * 
	 * @param ip Server ip
	 */
	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

	/**
	 * Retrieves the server port
	 * 
	 * @return Server port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Retrieves the server ip
	 * 
	 * @return Server ip
	 */
	public InetAddress getIp() {
		return ip;
	}

	/**
	 * Checks if the server is active
	 * 
	 * @return True if active, false otherwise.
	 */
	public boolean isActive() {
		return connected;
	}

	/**
	 * Waits for the server to shut down
	 */
	public void waitExit() {
		while (connected) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	/**
	 * Retrieves the server version
	 * 
	 * @return Server version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Retrieves the server name
	 * 
	 * @return Server name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Registers a new upload request processor. (POST, DELETE or PUT)
	 * 
	 * @param processor The processor implementation to register.
	 */
	public void registerProcessor(HttpUploadProcessor processor) {
		if (!uploadProcessors.stream()
				.anyMatch(t -> t.getClass().getTypeName().equals(processor.getClass().getTypeName())
						&& t.supportsChildPaths() == processor.supportsChildPaths()
						&& t.supportsGet() == processor.supportsGet() && t.path() == processor.path()))
			uploadProcessors.add(processor);
	}

	/**
	 * Registers a new GET request processor.
	 * 
	 * @param processor The processor implementation to register.
	 */
	public void registerProcessor(HttpGetProcessor processor) {
		if (!getProcessors.stream().anyMatch(t -> t.getClass().getTypeName().equals(processor.getClass().getTypeName())
				&& t.supportsChildPaths() == processor.supportsChildPaths() && t.path() == processor.path()))
			getProcessors.add(processor);
	}
}
