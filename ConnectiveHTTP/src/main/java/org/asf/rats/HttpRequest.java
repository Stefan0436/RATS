package org.asf.rats;

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
	public String body = "";

	/**
	 * Parses a request into a new HttpMessage object.
	 * 
	 * @param request Request bytes
	 * @return HttpMessage representing the request.
	 */
	public static HttpRequest parse(byte[] request) {
		String requestMsg = new String(request);
		if (!requestMsg.substring(0, 1).matches("[A-Za-z0-9]")) {
			return null;
		}
		HttpRequest msg = new HttpRequest();

		int ln = 0;
		boolean first = true;
		for (String line : requestMsg.replaceAll("\r", "").split("\n")) {
			if (line.equals(""))
				break;

			if (first) {
				first = false;
				String[] mainHeader = line.split(" ");
				msg.method = mainHeader[0];
				msg.path = mainHeader[1];
				msg.version = mainHeader[2];
			} else {
				String key = line.substring(0, line.indexOf(": "));
				String value = line.substring(line.indexOf(": ") + 2);
				msg.headers.put(key, value);
			}
			ln++;
		}

		if (msg.headers.containsKey("Content-Length")) {
			int l = 0;
			long length = Long.valueOf(msg.headers.get("Content-Length"));

			StringBuilder builder = new StringBuilder();
			ln++;

			int index = 0;
			for (char ch : requestMsg.toCharArray()) {
				if (ch == '\n' && l != ln) {
					l++;
				} else if (l == ln) {
					builder.append(ch);
				}

				if (index == length)
					break;
				if (l == ln)
					index++;
			}

			msg.body = builder.toString();
		}

		return msg;
	}
}
