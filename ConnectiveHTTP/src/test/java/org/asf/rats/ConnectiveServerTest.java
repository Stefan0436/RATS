package org.asf.rats;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
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
	public void getTest() throws IOException {
		ConnectiveHTTPServer testServer = new ConnectiveHTTPServer();
		testServer.start();
		testServer.registerProcessor(new TestProc());

		URL u = new URL("http://localhost:" + testServer.getPort() + "/test");
		InputStream strm = u.openStream();
		byte[] test = strm.readAllBytes();
		strm.close();
		String outp = new String(test);

		assertTrue(outp.equals("12345"));
		testServer.stop();
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
