package org.asf.rats.http;

import org.asf.cyan.api.common.CYAN_COMPONENT;
import org.asf.rats.components.ComponentConfiguration;
import org.asf.rats.configuration.RatsConfiguration;

/**
 * 
 * ConnectiveHTTP Core Bindings for RaTs!
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
@CYAN_COMPONENT
public class HttpServiceCoreModule {
	protected static void initComponent() {
		try {
			Class.forName("org.asf.rats.extensions.connective.https.ConnectiveHTTPS", false,
					ComponentConfiguration.getInstance().getClassLoader());
			
			RatsConfiguration.addLoadHook(conf -> {
				conf.httpPort = 8043;
			});
		} catch (ClassNotFoundException e) {
		}
	}
}
