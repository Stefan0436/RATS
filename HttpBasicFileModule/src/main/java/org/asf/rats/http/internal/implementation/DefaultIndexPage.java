package org.asf.rats.http.internal.implementation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import org.asf.rats.http.providers.IndexPageProvider;

public class DefaultIndexPage extends IndexPageProvider {

	@Override
	public void process(Socket client, File[] directories, File[] files) {
		try {
			InputStream strm = getClass().getResource("/index.template.html").openStream();
			setBody("text/html", process(new String(strm.readAllBytes()), getRequest().path,
					new File(getFolderPath()).getName(), null, directories, files));
		} catch (IOException e) {
		}
	}

	private String process(String str, String path, String name, File data, File[] directories, File[] files) {
		if (!path.endsWith("/")) {
			path += "/";
		}
		
		if (data != null) {
			str = str.replaceAll("\\%c-name\\%", name);
			str = str.replaceAll("\\%c-path\\%", path);
		}

		if (files != null) {
			if (str.contains("<%%PROCESS:FILES:$")) {
				String buffer = "";
				String template = "";
				int percent = 0;
				boolean parsing = false;
				for (char ch : str.toCharArray()) {
					if (ch == '<' && !parsing) {
						if (buffer.isEmpty()) {
							buffer = "<";
						} else {
							buffer = "";
						}
					} else if (ch == '\n' && !parsing) {
						buffer = "";
					} else if (ch == '\n') {
						buffer += "\n";
					} else {
						if (!buffer.isEmpty() && !parsing) {
							buffer += ch;
							if (ch == '$') {
								if (!buffer.equals("<%%PROCESS:FILES:$")) {
									buffer = "";
								} else {
									parsing = true;
								}
							}
						} else if (parsing) {
							buffer += ch;
							if (ch == '%' && percent < 2) {
								percent++;
							} else if (ch == '%' && percent >= 2) {
								percent = 0;
							} else if (ch == '>' && percent == 2) {
								percent = 0;
								template = buffer;
								buffer = buffer.substring(
										"<%%PROCESS:FILES:$".length() + System.lineSeparator().length(),
										buffer.length() - 4);

								StringBuilder strs = new StringBuilder();
								for (File f : files) {
									strs.append(process(buffer, path, f.getName(), f, null, null));
								}
								str = str.replace(template, strs.toString());
								buffer = "";
							} else {
								percent = 0;
							}
						}
					}
				}
			}
			if (str.contains("<%%PROCESS:DIRECTORIES:$")) {
				String buffer = "";
				String template = "";
				int percent = 0;
				boolean parsing = false;
				for (char ch : str.toCharArray()) {
					if (ch == '<' && !parsing) {
						if (buffer.isEmpty()) {
							buffer = "<";
						} else {
							buffer = "";
						}
					} else if (ch == '\n' && !parsing) {
						buffer = "";
					} else if (ch == '\n') {
						buffer += "\n";
					} else {
						if (!buffer.isEmpty() && !parsing) {
							buffer += ch;
							if (ch == '$') {
								if (!buffer.equals("<%%PROCESS:DIRECTORIES:$")) {
									buffer = "";
								} else {
									parsing = true;
								}
							}
						} else if (parsing) {
							buffer += ch;
							if (ch == '%' && percent < 2) {
								percent++;
							} else if (ch == '%' && percent >= 2) {
								percent = 0;
							} else if (ch == '>' && percent == 2) {
								percent = 0;
								template = buffer;
								buffer = buffer.substring(
										"<%%PROCESS:DIRECTORIES:$".length() + System.lineSeparator().length(),
										buffer.length() - 4);

								StringBuilder strs = new StringBuilder();
								for (File f : directories) {
									strs.append(process(buffer, path, f.getName(), f, null, null));
								}
								str = str.replace(template, strs.toString());
								buffer = "";
							} else {
								percent = 0;
							}
						}
					}
				}
			}
		}
		
		str = str.replaceAll("\\%path\\%", path);
		str = str.replaceAll("\\%name\\%", name);
		str = str.replaceAll("\\%up-path\\%", (path.equals("/") || path.isEmpty()) ? "" : new File(path).getParent());
		str = str.replaceAll("\\%server-name\\%", getServer().getName());
		str = str.replaceAll("\\%server-version\\%", getServer().getVersion());

		return str;
	}

	@Override
	protected IndexPageProvider newInstance() {
		return new DefaultIndexPage();
	}

}
