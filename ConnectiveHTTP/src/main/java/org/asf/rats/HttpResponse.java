package org.asf.rats;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
	public InputStream body;

	public HttpResponse(int status, String message, HttpRequest input) {
		this.input = input;
		this.status = status;
		this.message = message;
	}

	public HttpResponse(HttpRequest input) {
		this.input = input;
	}

	public HttpResponse setContent(String type, String body) {
		headers.put("Content-Type", type);

		if (this.body != null) {
			try {
				this.body.close();
			} catch (IOException e) {
			}
		}

		this.body = new ByteArrayInputStream(body.getBytes());
		return this;
	}

	public HttpResponse setContent(String type, byte[] body) {
		headers.put("Content-Disposition", "attachment");
		headers.put("Content-Type", type);

		if (this.body != null) {
			try {
				this.body.close();
			} catch (IOException e) {
			}
		}

		this.body = new ByteArrayInputStream(body);
		return this;
	}

	public HttpResponse setContent(String type, InputStream body) {
		headers.put("Content-Disposition", "attachment");
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
	 * @throws IOException If building fails
	 */
	public byte[] build() throws IOException {
		StringBuilder resp = new StringBuilder();
		resp.append(input.version).append(" ");
		resp.append(status).append(" ");
		resp.append(message);

		ByteArrayOutputStream strm = new ByteArrayOutputStream();
		ByteArrayOutputStream intermediary = null;

		if (body != null) {
			intermediary = new ByteArrayOutputStream();
			long length = body.transferTo(intermediary);
			headers.put("Content-Length", Long.toString(length));
		}

		headers.forEach((k, v) -> {
			resp.append("\r\n");
			resp.append(k).append(": ");
			resp.append(v);
		});

		if (intermediary != null) {
			resp.append("\r\n");
			resp.append("\r\n");
			strm.write(resp.toString().getBytes());
			strm.write(intermediary.toByteArray());
		} else {
			strm.write(resp.toString().getBytes());
		}

		byte[] bytes = strm.toByteArray();
		strm.close();
		return bytes;
	}

	// Adapted from SO answer: https://stackoverflow.com/a/8642463
	public synchronized String getHttpDate(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(date);
	}

}
