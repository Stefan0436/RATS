package org.asf.rats;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 
 * HttpResponse, basic builder for HTTP responses
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public class HttpResponse {

	public HttpRequest input;

	public HashMap<String, String> headers = new HashMap<String, String>();

	public int status = 200;
	public String message = "OK";
	public InputStream body = null;

	public HttpResponse(int status, String message, HttpRequest input) {
		this.input = input;
		this.status = status;
		this.message = message;
	}

	public HttpResponse(HttpRequest input) {
		this.input = input;
	}

	/**
	 * Sets the content to a body string
	 * 
	 * @param type Content type.
	 * @param body Content body.
	 */
	public HttpResponse setContent(String type, String body) {
		if (type != null)
			headers.put("Content-Type", type);
		else if (headers.containsKey("Content-Type"))
			headers.remove("Content-Type");

		if (this.body != null) {
			try {
				this.body.close();
			} catch (IOException e) {
			}
		}

		if (body == null) {
			this.body = null;
			return this;
		}

		this.body = new ByteArrayInputStream(body.getBytes());
		return this;
	}

	/**
	 * Sets the body of the response. (sets Content-Disposition to attachment)
	 * 
	 * @param type Content type.
	 * @param body Input bytes.
	 */
	public HttpResponse setContent(String type, byte[] body) {
		headers.put("Content-Type", type);
		headers.put("Content-Disposition", "attachment");

		if (this.body != null) {
			try {
				this.body.close();
			} catch (IOException e) {
			}
		}

		this.body = new ByteArrayInputStream(body);
		return this;
	}

	/**
	 * Sets the body of the response, WARNING: the stream gets closed on build.
	 * 
	 * @param type Content type.
	 * @param body Input stream.
	 */
	public HttpResponse setContent(String type, InputStream body) {
		headers.put("Content-Type", type);

		if (this.body != null) {
			try {
				this.body.close();
			} catch (IOException e) {
			}
		}

		this.body = body;
		return this;
	}

	public HttpResponse addDefaultHeaders(ConnectiveHTTPServer server) {
		headers.put("Server", server.getName() + " " + server.getVersion());
		headers.put("Date", getHttpDate(new Date()));
		setConnectionState("Closed");
		return this;
	}

	public HttpResponse setLastModified(Date date) {
		headers.put("Last-Modified", getHttpDate(new Date()));
		return this;
	}

	public HttpResponse setConnectionState(String state) {
		headers.put("Connection", state);
		return this;
	}

	public HttpResponse setHeader(String header, String value) {
		headers.put(header, value);
		return this;
	}

	/**
	 * Builds the HTTP response
	 * 
	 * @param output Output stream to write to.
	 * @throws IOException
	 */
	public void build(OutputStream output) throws IOException {
		StringBuilder resp = new StringBuilder();
		resp.append(input.version).append(" ");
		resp.append(status).append(" ");
		resp.append(message);

		headers.forEach((k, v) -> {
			resp.append("\r\n");
			resp.append(k).append(": ");
			resp.append(v);
		});

		if (body != null && !input.method.equals("HEAD") && status != 204) {
			resp.append("\r\n");
			resp.append("\r\n");
			output.write(resp.toString().getBytes());

			long v = body.transferTo(output);
			this.headers.put("Content-Length", Long.toString(v));

			body.close();
			body = null;
		} else {
			output.write(resp.toString().getBytes());
		}
	}

	// Adapted from SO answer: https://stackoverflow.com/a/8642463
	public synchronized String getHttpDate(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(date);
	}

}
