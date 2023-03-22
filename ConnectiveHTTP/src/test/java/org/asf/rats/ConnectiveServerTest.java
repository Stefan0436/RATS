package org.asf.rats;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

import org.asf.rats.processors.HttpUploadProcessor;
import org.junit.Test;

public class ConnectiveServerTest {

	Random rnd = new Random();

	public String genText() {
		char[] chars = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
				'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
		String txt = "";

		for (int i = 0; i < rnd.nextInt(100000); i++) {
			txt += chars[rnd.nextInt(chars.length)];
		}
		return txt;
	}

	class TestProc extends HttpUploadProcessor {

		@Override
		public String path() {
			return "/test";
		}

		@Override
		public void process(String contentType, Socket client, String method) {
			getResponse().setHeader("Connection", "Keep-Alive");
			if (contentType != null)
				setBody(getRequestBody() + "-test");
			else {
				setBody("12345");
			}
		}

		@Override
		public HttpUploadProcessor createNewInstance() {
			return new TestProc();
		}

		@Override
		public boolean supportsGet() {
			return true;
		}

	}

	@Test
	public void keepAliveConnectionTest() throws IOException {
		ConnectiveHTTPServer testServer = new ConnectiveHTTPServer();
		testServer.start();
		testServer.registerProcessor(new TestProc());

		URL u = new URL("http://localhost:" + testServer.getPort() + "/test?test=hi&test2=hello");
		InputStream strm = u.openStream();
		byte[] test = strm.readAllBytes();
		strm.close();
		String outp = new String(test);

		assertTrue(outp.equals("12345"));

		// Now test keep-alive
		Socket sock = new Socket("localhost", testServer.getPort());

		// Write first request
		sock.getOutputStream().write("GET /test HTTP/1.1\r\n".getBytes("UTF-8"));
		sock.getOutputStream().write("Host: localhost\r\n".getBytes("UTF-8"));
		sock.getOutputStream().write("\r\n".getBytes("UTF-8"));

		// Read response
		HashMap<String, String> headers = new HashMap<String, String>();
		String line = HttpRequest.readStreamLine(sock.getInputStream());
		assertTrue(line.equals("HTTP/1.1 200 OK"));
		while (true) {
			line = HttpRequest.readStreamLine(sock.getInputStream());
			if (line.equals(""))
				break;
			String key = line.substring(0, line.indexOf(": "));
			String value = line.substring(line.indexOf(": ") + 2);
			headers.put(key, value);
		}
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ConnectiveHTTPServer.transferRequestBody(headers, sock.getInputStream(), buffer);
		String data = new String(buffer.toByteArray(), "UTF-8");
		assertTrue(data.equals("12345"));

		// Send second request
		sock.getOutputStream().write("GET /test HTTP/1.1\r\n".getBytes("UTF-8"));
		sock.getOutputStream().write("Host: localhost\r\n".getBytes("UTF-8"));
		sock.getOutputStream().write("\r\n".getBytes("UTF-8"));

		// Read response
		headers.clear();
		line = HttpRequest.readStreamLine(sock.getInputStream());
		assertTrue(line.equals("HTTP/1.1 200 OK"));
		while (true) {
			line = HttpRequest.readStreamLine(sock.getInputStream());
			if (line.equals(""))
				break;
			String key = line.substring(0, line.indexOf(": "));
			String value = line.substring(line.indexOf(": ") + 2);
			headers.put(key, value);
		}
		buffer = new ByteArrayOutputStream();
		ConnectiveHTTPServer.transferRequestBody(headers, sock.getInputStream(), buffer);
		data = new String(buffer.toByteArray(), "UTF-8");
		assertTrue(data.equals("12345"));

		// Send third request
		sock.getOutputStream().write("POST /test HTTP/1.1\r\n".getBytes("UTF-8"));
		sock.getOutputStream().write("Host: localhost\r\n".getBytes("UTF-8"));
		sock.getOutputStream().write("Content-Type: text/plain\r\n".getBytes("UTF-8"));
		sock.getOutputStream().write("Content-Length: 4\r\n".getBytes("UTF-8"));
		sock.getOutputStream().write("\r\n".getBytes("UTF-8"));
		sock.getOutputStream().write("Test".getBytes("UTF-8"));

		// Read response
		headers.clear();
		line = HttpRequest.readStreamLine(sock.getInputStream());
		assertTrue(line.equals("HTTP/1.1 200 OK"));
		while (true) {
			line = HttpRequest.readStreamLine(sock.getInputStream());
			if (line.equals(""))
				break;
			String key = line.substring(0, line.indexOf(": "));
			String value = line.substring(line.indexOf(": ") + 2);
			headers.put(key, value);
		}
		buffer = new ByteArrayOutputStream();
		ConnectiveHTTPServer.transferRequestBody(headers, sock.getInputStream(), buffer);
		data = new String(buffer.toByteArray(), "UTF-8");
		assertTrue(data.equals("Test-test"));

		sock.close();
		testServer.stop();
	}

	@Test
	public void getTest() throws IOException {
		ConnectiveHTTPServer testServer = new ConnectiveHTTPServer();
		testServer.start();
		testServer.registerProcessor(new TestProc());

		URL u = new URL("http://localhost:" + testServer.getPort() + "/test?test=hi&test2=hello");
		InputStream strm = u.openStream();
		byte[] test = strm.readAllBytes();
		strm.close();
		String outp = new String(test);

		assertTrue(outp.equals("12345"));
		testServer.stop();
	}

	@Test
	public void malformedTest() throws IOException {
		ConnectiveHTTPServer testServer = new ConnectiveHTTPServer();
		testServer.start();
		testServer.registerProcessor(new TestProc());

		URL u = new URL("http://localhost:" + testServer.getPort() + "/%YE");
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		assertTrue(conn.getResponseCode() == 400);

		u = new URL("http://localhost:" + testServer.getPort() + "/test?malformed=%YE");
		conn = (HttpURLConnection) u.openConnection();
		assertTrue(conn.getResponseCode() == 400);
	}

	@Test
	public void postTest() throws IOException {
		ConnectiveHTTPServer testServer = new ConnectiveHTTPServer();
		testServer.registerProcessor(new TestProc());
		String str = genText();
		testServer.start();

		URL u = new URL("http://localhost:" + testServer.getPort() + "/test");
		HttpURLConnection c = (HttpURLConnection) u.openConnection();
		try {
			c.setRequestMethod("POST");
			c.setDoOutput(true);
			c.setDoInput(true);
			c.connect();
			c.getOutputStream().write(str.getBytes());

			int code = c.getResponseCode();
			String msg = c.getResponseMessage();
			if (code == 200) {
				String resp = new String(c.getInputStream().readAllBytes());
				c.disconnect();
				assertTrue(resp.equals(str + "-test"));
			} else {
				c.disconnect();
				testServer.stop();
				fail(msg);
			}
		} catch (IOException e) {
			c.disconnect();
			testServer.stop();
			throw e;
		}

		testServer.stop();
	}

}
