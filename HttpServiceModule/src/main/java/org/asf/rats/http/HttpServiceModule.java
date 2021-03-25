package org.asf.rats.http;

import org.asf.cyan.api.common.CYAN_COMPONENT;
import org.asf.rats.ConnectiveHTTPServer;
import org.asf.rats.Memory;
import org.asf.rats.configuration.RatsConfiguration;

/**
 * 
 * ConnectiveHTTP Bindings for RaTs!
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
@CYAN_COMPONENT
public class HttpServiceModule extends ConnectiveHTTPServer {
	protected static void initComponent() {
		Memory.getInstance().getOrCreate("connective.http.props.port").assign(RatsConfiguration.getInstance().httpPort);
	}
}
