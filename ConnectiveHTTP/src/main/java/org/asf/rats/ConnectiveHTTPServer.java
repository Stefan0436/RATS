package org.asf.rats;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import org.asf.cyan.api.common.CYAN_COMPONENT;
import org.asf.cyan.api.common.CyanComponent;
import org.asf.rats.processors.HttpGetProcessor;
import org.asf.rats.processors.HttpPostProcessor;
import org.asf.rats.processors.IAutoRegisterProcessor;

/**
 * 
 * ConnectiveHTTP Server, HTTP Server API.<br />
 * <br />
 * <b>NOTE:</b> This class provides a CYAN component and will automatically
 * start the server if such implementation calls it!<br />
 * <br />
 * The autostart options can be configured using {@link Memory RaTs!
 * Memory}.<br />
 * Memory keys:<br />
 * - <b>connective.http.props.autostart</b> - true to autostart, false
 * otherwise.</b><br />
 * - <b>connective.http.props.port</b> - server port
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
@CYAN_COMPONENT
public class ConnectiveHTTPServer extends CyanComponent {

	protected String name = "ASF Connective";
	protected String version = "1.0.0.A1";

	protected boolean connected = false;
	protected ServerSocket socket = null;

	protected ArrayList<Socket> sockets = new ArrayList<Socket>();
	protected HashMap<Socket, InputStream> inStreams = new HashMap<Socket, InputStream>();
	protected HashMap<Socket, OutputStream> outStreams = new HashMap<Socket, OutputStream>();

	protected ArrayList<HttpGetProcessor> getProcessors = new ArrayList<HttpGetProcessor>();
	protected ArrayList<HttpPostProcessor> postProcessors = new ArrayList<HttpPostProcessor>();

	protected int port = 8080;

	protected Thread serverProcessor = new Thread(() -> {
		while (connected) {
			try {
				Socket client = socket.accept();

				acceptConnection(client);
				InputStream in = getClientInput(client);
				OutputStream out = getClientOutput(client);

				Thread clientProcessor = new Thread(() -> {

					try {
						try {
							while (in.available() == 0) {
								Thread.sleep(10);
							}
						} catch (InterruptedException e) {
						}

						HttpRequest msg = HttpRequest.parse(readStreamForRequest(in));
						if (msg == null) {
							HttpRequest dummy = new HttpRequest();
							dummy.version = "HTTP/1.1";

							closeConnection(dummy, 503, "Unsupported request", client);
						} else {
							processRequest(client, msg);
						}
					} catch (IOException ex) {
					}

				}, "Client processor (" + client.getInetAddress().toString() + ":" + client.getPort() + ")");

				clientProcessor.start();
				sockets.add(client);
				inStreams.put(client, in);
				outStreams.put(client, out);

			} catch (IOException ex) {
				if (!connected)
					break;

				error("Failed to process client", ex);
			}
		}
	}, "Connective server thread");

	protected static ConnectiveHTTPServer implementation;

	protected static void initComponent() throws IOException {
		if (implementation == null)
			implementation = new ConnectiveHTTPServer();

		ConnectiveHTTPServer server = implementation;

		for (Class<IAutoRegisterProcessor> register : findClasses(getMainImplementation(),
				IAutoRegisterProcessor.class)) {

			if (HttpPostProcessor.class.isAssignableFrom(register)) {
				try {
					server.registerProcessor((HttpPostProcessor) register.getConstructor().newInstance());
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					error("Automatic HTTP processor registration failed, HTTP POST Processor Type: "
							+ register.getTypeName(), e);
				}
			} else if (HttpGetProcessor.class.isAssignableFrom(register)) {
				try {
					server.registerProcessor((HttpGetProcessor) register.getConstructor().newInstance());
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					error("Automatic HTTP processor registration failed, HTTP GET Processor Type: "
							+ register.getTypeName(), e);
				}
			}

		}

		Boolean autostart = Memory.getInstance().getOrCreate("connective.http.props.autostart").getValue(Boolean.class);
		if (autostart == null)
			autostart = true;

		Integer portSetting = Memory.getInstance().getOrCreate("connective.http.props.port").getValue(Integer.class);
		if (portSetting == null)
			portSetting = server.port;

		server.port = portSetting;

		if (autostart)
			server.start();
	}

	/**
	 * Retrieves the client output stream (override only)
	 */
	private OutputStream getClientOutput(Socket client) throws IOException {
		return client.getOutputStream();
	}

	/**
	 * Retrieves the client input stream (override only)
	 */
	protected InputStream getClientInput(Socket client) throws IOException {
		return client.getInputStream();
	}

	/**
	 * Reads the client input stream available bytes. (for http request generation)
	 */
	protected byte[] readStreamForRequest(InputStream in) throws IOException {
		return in.readNBytes(in.available());
	}

	/**
	 * Called on client connect, potential override.
	 */
	protected void acceptConnection(Socket client) {
	}

	/**
	 * Called to write to the client output (override only)
	 */
	protected void clientOutWrite(OutputStream strm, byte[] content) throws IOException {
		strm.write(content);
	}

	/**
	 * Processes HTTP Requests
	 * 
	 * @param client HTTP Client
	 * @param msg    Request
	 * @throws IOException If processing fails
	 */
	protected void processRequest(Socket client, HttpRequest msg) throws IOException {
		boolean compatible = false;
		ArrayList<HttpGetProcessor> getProcessorLst = new ArrayList<HttpGetProcessor>(getProcessors);
		ArrayList<HttpPostProcessor> postProcessorLst = new ArrayList<HttpPostProcessor>(postProcessors);

		for (HttpPostProcessor proc : postProcessorLst) {
			if (proc.supportsGet()) {
				getProcessorLst.add(proc);
			}
		}

		if (msg.method.equals("POST")) {
			HttpPostProcessor impl = null;
			for (HttpPostProcessor proc : postProcessorLst) {
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
				postProcessorLst.sort((t1, t2) -> {
					return -Integer.compare(t1.path().split("/").length, t2.path().split("/").length);
				});
				for (HttpPostProcessor proc : postProcessorLst) {
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
				HttpPostProcessor processor = impl.instanciate(this, msg);
				processor.process(msg.headers.get("Content-Type"), msg.body, client);
				closeConnection(processor.getResponse(), client);
			}
		} else {
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
				HttpGetProcessor processor = impl.instanciate(this, msg);
				processor.process(client);

				closeConnection(processor.getResponse(), client);
			}
		}

		if (!compatible) {
			if (msg.method.equals("POST")) {
				closeConnection(msg, 415, "Unsupported post request", client);
			} else {
				closeConnection(msg, 404, "Command not found", client);
			}
		}
	}

	/**
	 * Closes a client connection
	 * 
	 * @param response Http response
	 * @param client   Client to disconnect
	 * @return HttpResponse instance.
	 * @throws IOException If transmitting the response fails
	 */
	public synchronized HttpResponse closeConnection(HttpResponse response, Socket client) throws IOException {
		clientOutWrite(outStreams.get(client), response.addDefaultHeaders(this).setConnectionState("Closed").build());
		closeConnection(client);
		return response;
	}

	/**
	 * Closes a client connection, sends no response
	 * 
	 * @param client Client to disconnect.
	 */
	public synchronized void closeConnection(Socket client) {
		try {
			client.close();
		} catch (IOException e) {
		}

		InputStream strm = inStreams.get(client);
		OutputStream strmOut = outStreams.get(client);

		try {
			strm.close();
		} catch (IOException e) {
		}
		try {
			strmOut.close();
		} catch (IOException e) {
		}

		inStreams.remove(client);
		outStreams.remove(client);
		sockets.remove(client);
	}

	/**
	 * Closes a client connection
	 * 
	 * @param request The request used
	 * @param status  Status code
	 * @param message Status message
	 * @param client  Client to disconnect
	 * @return HttpResponse instance.
	 * @throws IOException If transmitting the response fails
	 */
	public synchronized HttpResponse closeConnection(HttpRequest request, int status, String message, Socket client)
			throws IOException {
		HttpResponse resp = new HttpResponse(status, message, request);
		clientOutWrite(outStreams.get(client), resp.addDefaultHeaders(this).setConnectionState("Closed").build());
		closeConnection(client);
		return resp;
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
		socket = new ServerSocket(port);
		serverProcessor.start();
	}

	/**
	 * Stops the server and disconnects all clients (abruptly)
	 */
	public void stop() {
		if (!connected)
			return;

		connected = false;
		for (InputStream strm : inStreams.values()) {
			try {
				strm.close();
			} catch (IOException e) {
			}
		}
		for (OutputStream strm : outStreams.values()) {
			try {
				strm.close();
			} catch (IOException e) {
			}
		}
		for (Socket soc : sockets) {
			try {
				soc.close();
			} catch (IOException e) {
			}
		}

		try {
			socket.close();
		} catch (IOException e) {
		}

		sockets.clear();
		inStreams.clear();
		outStreams.clear();
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
	 * Retrieves the server port
	 * 
	 * @return Server port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Retrieves the instance of the auto-started server.
	 * 
	 * @return ConnectiveHTTPServer instance.
	 */
	public static ConnectiveHTTPServer getMainServer() {
		return implementation;
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
	 * Registers a new POST request processor.
	 * 
	 * @param processor The processor implementation to register.
	 */
	public void registerProcessor(HttpPostProcessor processor) {
		postProcessors.add(processor);
	}

	/**
	 * Registers a new GET request processor.
	 * 
	 * @param processor The processor implementation to register.
	 */
	public void registerProcessor(HttpGetProcessor processor) {
		getProcessors.add(processor);
	}
}
