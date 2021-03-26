package org.asf.rats;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * 
 * HttpRequest, basic parser for HTTP requests
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class HttpRequest {
	public HashMap<String, String> headers = new HashMap<String, String>();

	public String path = "";
	public String method = "";
	public String version = "";

	@Deprecated
	public String body = null;
	protected InputStream bodyStream = null;

	public String query = "";

	/**
	 * Parses a request into a new HttpMessage object.
	 * 
	 * @param request Request stream
	 * @return HttpMessage representing the request.
	 * @throws IOException If reading fails.
	 */
	public static HttpRequest parse(InputStream request) throws IOException {
		String firstLine = readStreamLine(request);

		if (!firstLine.substring(0, 1).matches("[A-Za-z0-9]")) {
			return null;
		}

		HttpRequest msg = new HttpRequest();
		String[] mainHeader = firstLine.split(" ");
		URI req;
		try {
			req = new URI(mainHeader[1]);
			msg.path = req.getPath();
			msg.query = req.getQuery();
		} catch (URISyntaxException e) {
			msg.path = mainHeader[1].replaceAll("\\", "/");
		}
		msg.method = mainHeader[0];
		msg.version = mainHeader[2];

		while (true) {
			String line = readStreamLine(request);
			if (line.equals(""))
				break;

			String key = line.substring(0, line.indexOf(": "));
			String value = line.substring(line.indexOf(": ") + 2);
			msg.headers.put(key, value);
		}

		if (msg.method.equals("POST")) {
			msg.bodyStream = request;
		}

		return msg;
	}

	/**
	 * Reads a single line from an inputstream
	 * 
	 * @param strm Stream to read from.
	 * @return String representing the line read.
	 * @throws IOException If reading fails.
	 */
	protected static String readStreamLine(InputStream strm) throws IOException {
		String buffer = "";
		while (true) {
			char ch = (char) strm.read();
			if (ch == '\n') {
				return buffer;
			} else if (ch != '\r') {
				buffer += ch;
			}
		}
	}

	public void close() throws IOException {
		if (bodyStream != null) {
			bodyStream.close();
		}
	}

	public boolean isBinaryMode() {
		return (headers.containsKey("Content-Disposition") && headers.get("Content-Disposition").equals("attachment"))
				|| (headers.containsKey("Content-Type")
						&& headers.get("Content-Type").equals("application/octet-stream"));
	}

	/**
	 * Returns the post body stream, null if not a post request.
	 * 
	 * @return Body InputStream or null.
	 */
	public InputStream getBodyStream() {
		return bodyStream;
	}

	/**
	 * Returns the post body in string format. <b>WARNING:</b> leaves the body
	 * stream in a useless state!
	 * 
	 * @return String representing the body.
	 */
	public String getBody() {
		if (bodyStream != null) {
			if (body == null) {
				try {
					body = new String(bodyStream.readNBytes(bodyStream.available()));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			return body;
		} else {
			return null;
		}
	}
}
