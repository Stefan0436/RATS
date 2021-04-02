package org.asf.rats.http.internal.processors;

import java.net.Socket;

import org.asf.rats.ConnectiveHTTPServer;
import org.asf.rats.HttpRequest;
import org.asf.rats.HttpResponse;
import org.asf.rats.http.FileProcessor;
import org.asf.rats.http.FileProcessor.HttpResourceProvider;
import org.asf.rats.http.ProviderContext;
import org.asf.rats.processors.HttpUploadProcessor;

public class MainFileProcessor extends HttpUploadProcessor {

	protected String path = "";
	protected ProviderContext context;

	protected FileProcessor processor;

	public MainFileProcessor(String path, ProviderContext context) {
		this.path = path;
		this.context = context;

		processor = FileProcessor.forContext(context, path);
	}

	@Override
	public String path() {
		return path;
	}

	@Override
	public void process(String contentType, Socket client, String method) {
		processor.process((request, response) -> {
			setResponse(response);
		}, new HttpResourceProvider() {
			@Override
			public Socket client() {
				return client;
			}

			@Override
			public ConnectiveHTTPServer server() {
				return getServer();
			}

			@Override
			public HttpRequest request() {
				return getRequest();
			}

			@Override
			public HttpResponse response() {
				return getResponse();
			}

			@Override
			public String path() {
				return getRequestPath();
			}

			@Override
			public String contentType() {
				return contentType;
			}

			@Override
			public String method() {
				return method;
			}
		});
	}

	@Override
	public HttpUploadProcessor createNewInstance() {
		return new MainFileProcessor(path, context);
	}

	@Override
	public boolean supportsChildPaths() {
		return true;
	}

	@Override
	public boolean supportsGet() {
		return true;
	}

}
