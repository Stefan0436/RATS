package org.asf.rats.http;

import java.net.Socket;
import java.util.HashMap;

import org.asf.rats.ConnectiveHTTPServer;
import org.asf.rats.HttpRequest;
import org.asf.rats.HttpResponse;
import org.asf.rats.http.internal.ProcessorAbstract;

/**
 * 
 * The control interface needed to use the file processing module (not a java
 * interface)
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class FileProcessor {

	private static HashMap<String, FileProcessor> instanciatedProcessors = new HashMap<String, FileProcessor>();
	private static FileProcessor implementation;
	private FileExecutionHandler handler;

	/**
	 * 
	 * Connects the ProcessorAbstract implementation with the FileProcessor
	 * 
	 * @author Stefan0436 - AerialWorks Software Foundation
	 *
	 */
	public interface FileExecutionHandler {
		public void run(ServerOutputHander outputHandler, HttpResourceProvider resourceProvider);
	}

	/**
	 * 
	 * Writes the output request and response
	 * 
	 * @author Stefan0436 - AerialWorks Software Foundation
	 *
	 */
	public interface ServerOutputHander {
		public void write(HttpRequest request, HttpResponse response);
	}

	/**
	 * 
	 * Provides the objects needed to run the processor
	 * 
	 * @author Stefan0436 - AerialWorks Software Foundation
	 *
	 */
	public interface HttpResourceProvider {

		/**
		 * Client socket
		 */
		public Socket client();

		/**
		 * Server instance
		 */
		public ConnectiveHTTPServer server();

		/**
		 * HTTP Request instance
		 */
		public HttpRequest request();

		/**
		 * HTTP Response instance
		 */
		public HttpResponse response();

		/**
		 * Request path
		 */
		public String path();

		/**
		 * Request content type
		 */
		public String contentType();

		/**
		 * Request method
		 */
		public String method();

	}

	static {
		assignImplementation(new FileProcessor());
	}

	private FileProcessor() {
	}

	/**
	 * Assings the FileProcessor implementation to use.
	 */
	protected static void assignImplementation(FileProcessor impl) {
		implementation = impl;
	}

	/**
	 * Retrieves the processor for a given context.
	 * 
	 * @param context     Provider context
	 * @param virtualRoot Provider virtual root
	 * @return FileProcessor instance for the context information.
	 */
	public static FileProcessor forContext(ProviderContext context, String virtualRoot) {

		if (instanciatedProcessors.containsKey(virtualRoot))
			return instanciatedProcessors.get(virtualRoot);

		ProcessorAbstract inst = ProcessorAbstract.instanciateBase(virtualRoot, context);
		FileProcessor processor = toProcessor(inst);

		instanciatedProcessors.put(virtualRoot, processor);

		return processor;
	}

	/**
	 * Processes the request.
	 * 
	 * @param outputHandler    Output handler
	 * @param resourceProvider Resource provider
	 */
	public void process(ServerOutputHander outputHandler, HttpResourceProvider resourceProvider) {
		handler.run(outputHandler, resourceProvider);
	}

	/**
	 * Connects the file execution context with this processor, internal use only.
	 */
	public void connect(FileExecutionHandler handler) {
		if (this.handler == null)
			this.handler = handler;
	}

	/**
	 * Creates a new FileProcessor instance for the given processor
	 * 
	 * @param processor File processor
	 * @return New FileProcessor instance.
	 */
	protected static FileProcessor toProcessor(ProcessorAbstract processor) {
		FileProcessor inst = implementation.newInstance();
		processor.assignProcessorContainer(inst);
		return inst;
	}

	/**
	 * Intended to be overridden by alternate implementations.
	 */
	protected FileProcessor newInstance() {
		return new FileProcessor();
	}

}
