package org.asf.rats;

import org.asf.rats.http.IAutoContextBuilder;

public class TestProvider implements IAutoContextBuilder {

	@Override
	public String hostDir() {
		return "test";
	}

	@Override
	public String virtualDir() {
		return "/test";
	}

}
