package org.asf.rats;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.Stream;

import org.junit.Test;

public class HttpResponseTest {
	
	class StringOutputStream extends OutputStream {

		private String buffer = "";
		
		@Override
		public void write(int arg0) throws IOException {
			buffer += (char)arg0;
		}
		
		@Override
		public String toString() {
			return buffer;
		}
		
	}

	@Test
	public void setMultiHeaderTest() throws IOException {
		HttpRequest req = new HttpRequest();
		req.version = "HTTP/1.1";
		HttpResponse res = new HttpResponse(req);
		res.setHeader("Test-One", "test", true);
		
		assertTrue(res.getHeader("Test-One")[0].equals("test"));
		res.setHeader("Test-One", "test two", true);
		
		assertTrue(Stream.of(res.getHeader("Test-One")).anyMatch(t -> t.equals("test")));	
		assertTrue(res.getHeader("Test-One").length > 1);
		assertTrue(Stream.of(res.getHeader("Test-One")).anyMatch(t -> t.equals("test two")));
		
		res.status = 204;
		res.message = "No content";
		
		StringOutputStream strm = new StringOutputStream();
		res.build(strm);
		String response = strm.toString().replaceAll("\r", "");
		
		assertTrue(Stream.of(response.split("\n")).anyMatch(t -> t.equals("Test-One: test")));
		assertTrue(Stream.of(response.split("\n")).anyMatch(t -> t.equals("Test-One: test two")));
		assertTrue(Stream.of(response.split("\n")).anyMatch(t -> t.equals("HTTP/1.1 204 No content")));
		assertTrue(response.split("\n").length == 3);
	}
	
}
