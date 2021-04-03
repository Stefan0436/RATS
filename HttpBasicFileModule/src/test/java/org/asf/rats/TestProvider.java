package org.asf.rats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Base64;

import org.asf.rats.http.IAutoContextBuilder;
import org.asf.rats.http.ProviderContext;
import org.asf.rats.http.providers.FileUploadHandler;
import org.asf.rats.http.providers.IContextProviderExtension;
import org.asf.rats.http.providers.IPathProviderExtension;

public class TestProvider implements IAutoContextBuilder {

	@Override
	public String hostDir() {
		return "test";
	}

	@Override
	public String virtualDir() {
		return "/";
	}

	@Override
	public FileUploadHandler[] uploadHandlers() {
		return new FileUploadHandler[] { new DefaultUploadHandler("maven", "/maven", null) } ;
	}

	public static class DefaultUploadHandler extends FileUploadHandler
			implements IContextProviderExtension, IPathProviderExtension {

		private String path = null;

		private ProviderContext context;
		private String affectedPath = "";
		private String group = "";

		private String serverDir = System.getProperty("rats.config.dir") == null ? "."
				: System.getProperty("rats.config.dir");

		public DefaultUploadHandler(String group, String path, ProviderContext context) {
			this.group = group;

			if (!path.startsWith("/"))
				path = "/" + path;
			if (!path.endsWith("/"))
				path += "/";

			this.affectedPath = path;
			this.context = context;
		}

		@Override
		protected FileUploadHandler newInstance() {
			return new DefaultUploadHandler(group, affectedPath, context);
		}

		@Override
		public boolean match(HttpRequest request, String path) {
			if (!path.endsWith("/"))
				path += "/";

			return path.toLowerCase().startsWith(affectedPath.toLowerCase());
		}

		@Override
		public boolean process(String contentType, Socket client, String method) {
			if (!method.equals("PUT"))
				return false;

			if (getHeader("Authorization") != null) {
				String header = getHeader("Authorization");
				String type = header.substring(0, header.indexOf(" "));
				String cred = header.substring(header.indexOf(" ") + 1);

				if (type.equals("Basic")) {
					cred = new String(Base64.getDecoder().decode(cred));
					String username = cred.substring(0, cred.indexOf(":"));
					String password = cred.substring(cred.indexOf(":") + 1);

					try {
						if (Memory.getInstance().get("connective.standard.authprovider")
								.getValue(IAuthenticationProvider.class)
								.authenticate(group, username, password.toCharArray())) {
							password = null;

							File file = new File(new File(serverDir, context.getSourceDirectory()), path);
							boolean existed = file.exists();
							if (!file.getParentFile().exists()) {
								file.getParentFile().mkdirs();
							}

							FileOutputStream strm = new FileOutputStream(file);
							getRequest().transferRequestBody(strm);
							strm.close();

							if (existed) {
								// I know you are supposed to return 204, but gradle doesn't like that from our system
								this.setResponseCode(201); 
								this.setResponseMessage("Updated");
								this.setBody("");
							} else {
								this.setResponseCode(201);
								this.setResponseMessage("Created");
								this.setBody("");
							}
						} else {
							this.setResponseCode(403);
							this.setResponseMessage("Access denied");
							this.setBody("text/html", null);
						}
					} catch (IOException e) {
						this.setResponseCode(503);
						this.setResponseMessage("Internal server error");
						this.setBody("text/html", null);
					}
					password = null;
				} else {
					this.setResponseCode(403);
					this.setResponseMessage("Access denied");
					this.setBody("text/html", null);
				}
			} else {
				this.setResponseHeader("WWW-Authenticate", "Basic realm=" + group);

				this.setResponseCode(401);
				this.setResponseMessage("Authorization required");
				this.setBody("");
			}
			return true;
		}

		@Override
		public void provide(ProviderContext context) {
			this.context = context;
		}

		@Override
		public void provide(String path) {
			this.path = path;
		}

	}

}
