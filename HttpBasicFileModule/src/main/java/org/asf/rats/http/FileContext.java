package org.asf.rats.http;

import java.io.IOException;
import java.io.InputStream;

import org.asf.rats.HttpResponse;

/**
 * 
 * File context - contains response information for the request.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class FileContext {
	private HttpResponse response;
	private InputStream file;
	private long length = -1;

	protected FileContext() {
	}

	/**
	 * Creates a new file context instance.
	 * 
	 * @param input     Input response.
	 * @param mediaType New media type.
	 * @param body      Content stream.
	 * @return New FileContext instance.
	 */
	public static FileContext create(HttpResponse input, String mediaType, InputStream body, long length) {
		FileContext context = new FileContext();

		context.file = body;
		context.length = length;
		context.response = input;
		context.response.headers.put("Content-Type", mediaType);

		return context;
	}

	/**
	 * Creates a new file context instance.
	 * 
	 * @param input     Input response.
	 * @param mediaType New media type.
	 * @param body      Content stream.
	 * @return New FileContext instance.
	 * @deprecated Use create(input, mediaType, body, length) instead
	 */
	@Deprecated
	public static FileContext create(HttpResponse input, String mediaType, InputStream body) {
		FileContext context = new FileContext();

		context.file = body;
		context.response = input;
		context.response.headers.put("Content-Type", mediaType);

		return context;
	}

	/**
	 * Retrieves the current document stream
	 * 
	 * @since ASF Connective 1.0.0.A4
	 * @return Document InputStream
	 */
	public InputStream getCurrentStream() {
		return file;
	}

	/**
	 * Creates the re-written HTTP response.
	 * 
	 * @return Rewritten HttpResponse instance.
	 */
	@SuppressWarnings("deprecation")
	public HttpResponse getRewrittenResponse() {
		if (response.getBodyStream() != null) {
			try {
				response.getBodyStream().close();
			} catch (IOException e) {
			}
		}

		response.body = file;
		if (length != -1)
			response.setHeader("Content-Length", Long.toString(length));
		return response;
	}
}
