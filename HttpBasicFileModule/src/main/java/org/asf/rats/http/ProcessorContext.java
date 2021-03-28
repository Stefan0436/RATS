package org.asf.rats.http;

import java.util.ArrayList;

import org.asf.rats.ConnectiveHTTPServer;
import org.asf.rats.processors.HttpGetProcessor;
import org.asf.rats.processors.HttpUploadProcessor;

/**
 * 
 * ProcessorContext - ArrayList with apply function for http processors.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class ProcessorContext extends ArrayList<HttpGetProcessor> {
	
	private static final long serialVersionUID = -4972759252163443243L;

	/**
	 * Applies the context to a given server, <b>ONCE APPLIED, IT CANNOT BE
	 * UNDONE.</b>
	 * 
	 * @param server Server to register the processors for.
	 */
	public void apply(ConnectiveHTTPServer server) {
		for (HttpGetProcessor proc : this) {
			if (proc instanceof HttpUploadProcessor)
				server.registerProcessor((HttpUploadProcessor) proc);
			else
				server.registerProcessor(proc);
		}
	}
	
}
