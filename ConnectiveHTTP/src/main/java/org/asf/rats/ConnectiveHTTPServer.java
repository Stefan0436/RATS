package org.asf.rats;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;

import org.asf.cyan.api.common.CYAN_COMPONENT;
import org.asf.cyan.api.common.CyanComponent;
import org.asf.rats.processors.HttpGetProcessor;
import org.asf.rats.processors.HttpUploadProcessor;
import org.asf.rats.processors.IAutoRegisterProcessor;

/**
 * 
 * ConnectiveHTTP Server, HTTP Server API. <b>Avoid direct construction, use the
 * factory instead: {@link ConnectiveServerFactory
 * ConnectiveServerFactory}</b><br />
 * <br />
 * <b>NOTE:</b> This class provides a CYAN component and will automatically
 * start the server if such implementation calls it!<br />
 * <br />
 * The autostart options can be configured using {@link Memory RaTs!
 * Memory}.<br />
 * Memory keys:<br />
 * - <b>connective.http.props.autostart</b> - true to autostart, false
 * otherwise.<br />
 * - <b>connective.http.props.port</b> - server port -
 * <b>connective.http.props.ip</b> - server ip
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
@CYAN_COMPONENT
public class ConnectiveHTTPServer extends CyanComponent {

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
	protected String version = "1.0.0.A3";

	protected boolean connected = false;
	protected ServerSocket socket = null;

	@Deprecated
	protected ArrayList<Socket> sockets = new ArrayList<Socket>();

	@Deprecated
	protected HashMap<Socket, InputStream> inStreams = new HashMap<Socket, InputStream>();

	@Deprecated
	protected HashMap<Socket, OutputStream> outStreams = new HashMap<Socket, OutputStream>();

	protected ArrayList<ConnectedClient> clients = new ArrayList<ConnectedClient>();

	protected ArrayList<HttpGetProcessor> getProcessors = new ArrayList<HttpGetProcessor>();
	protected ArrayList<HttpUploadProcessor> uploadProcessors = new ArrayList<HttpUploadProcessor>();

	protected int port = 8080;
	protected InetAddress ip = null;

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

			if (HttpUploadProcessor.class.isAssignableFrom(register)) {
				try {
					server.registerProcessor((HttpUploadProcessor) register.getConstructor().newInstance());
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					error("Automatic HTTP processor registration failed, HTTP POST/PUT/DELETE Processor Type: "
							+ register.getTypeName(), e);
				}
			} else if (HttpGetProcessor.class.isAssignableFrom(register)) {
				try {
					server.registerProcessor((HttpGetProcessor) register.getConstructor().newInstance());
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					error("Automatic HTTP processor registration failed, HTTP POST/PUT/DELETE Processor Type: "
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

		InetAddress addr = Memory.getInstance().getOrCreate("connective.http.props.ip").getValue(InetAddress.class);
		if (addr != null)
			server.ip = addr;

		server.port = portSetting;

		if (autostart)
			server.start();
	}

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

							str = str.replaceAll("\\%path\\%", request.path);
							str = str.replaceAll("\\%server-name\\%", getName());
							str = str.replaceAll("\\%server-version\\%", getVersion());
							str = str.replaceAll("\\%error-status\\%", Integer.toString(response.status));
							str = str.replaceAll("\\%error-message\\%", response.message);

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
