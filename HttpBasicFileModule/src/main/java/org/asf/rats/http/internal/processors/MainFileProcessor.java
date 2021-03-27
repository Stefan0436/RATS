package org.asf.rats.http.internal.processors;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import org.asf.rats.HttpResponse;
import org.asf.rats.http.FileContext;
import org.asf.rats.http.MainFileMap;
import org.asf.rats.http.ProviderContext;
import org.asf.rats.http.providers.FilePostHandler;
import org.asf.rats.http.providers.IContextProviderExtension;
import org.asf.rats.http.providers.IFileAlias;
import org.asf.rats.http.providers.IFileExtensionProvider;
import org.asf.rats.http.providers.IFileRestrictionProvider;
import org.asf.rats.http.providers.IPathProviderExtension;
import org.asf.rats.http.providers.IServerProviderExtension;
import org.asf.rats.http.providers.IndexPageProvider;
import org.asf.rats.processors.HttpPostProcessor;

public class MainFileProcessor extends HttpPostProcessor {

	private String path = "";
	private ProviderContext context;

	public MainFileProcessor(String path, ProviderContext context) {
		this.path = path;
		this.context = context;
	}

	@Override
	public String path() {
		return path;
	}

	@Override
	public void process(String contentType, Socket client) {
		String path = getRequestPath().substring(path().length());

		if (path.startsWith("..")) {
			setResponseCode(403);
			setResponseMessage("Access to parent directories denied");
			setBody("text/html", getError());
		} else {
			if (!path.startsWith("/"))
				path = "/" + path;

			for (IFileAlias alias : context.getAliases()) {
				if (alias instanceof IContextProviderExtension) {
					((IContextProviderExtension) alias).provide(context);
				}
				if (alias instanceof IServerProviderExtension) {
					((IServerProviderExtension) alias).provide(getServer());
				}
				if (alias.match(getRequest(), path)) {
					path = alias.rewrite(getRequest(), path);
					break;
				}
			}

			for (IFileRestrictionProvider restriction : context.getRestrictions()) {
				if (restriction instanceof IContextProviderExtension) {
					((IContextProviderExtension) restriction).provide(context);
				}
				if (restriction instanceof IServerProviderExtension) {
					((IServerProviderExtension) restriction).provide(getServer());
				}
				if (!restriction.checkRestriction(path, getRequest())) {
					getResponse().body = null;
					setResponseCode(restriction.getResponseCode());
					setResponseMessage(restriction.getResponseMessage());
					restriction.rewriteResponse(getResponse());

					if (getResponse().body == null) {
						setBody("text/html", getError());
					}

					return;
				}
			}

			File sourceFile = new File(context.getSourceDirectory(), path);
			if (!sourceFile.exists() && contentType == null) {
				setResponseCode(404);
				setResponseMessage("File not found");
				setBody("text/html", getError());
				return;
			}

			FileInputStream strm = null;
			FileContext file = null;
			try {
				if (!sourceFile.isDirectory()) {
					strm = new FileInputStream(sourceFile);
					file = FileContext.create(getResponse(),
							MainFileMap.getInstance().getContentType(sourceFile.getName()), strm);
				}
			} catch (FileNotFoundException e) {
				if (contentType == null) {
					setResponseCode(404);
					setResponseMessage("File not found");
					setBody("text/html", getError());
					return;
				}
			}

			if (contentType == null) {
				if (sourceFile.isDirectory()) {
					HashMap<String, IndexPageProvider> indexPages = new HashMap<String, IndexPageProvider>(
							context.getAltIndexPages());

					if (indexPages.isEmpty()) {
						if (context.getDefaultIndexPage() == null) {
							setResponseCode(404);
							setResponseMessage("File not found");
							setBody("text/html", getError());
						} else {
							setResponseCode(300);
							execPage(context.getDefaultIndexPage(), sourceFile, path, client);
						}
					} else {
						ArrayList<String> keys = new ArrayList<String>(indexPages.keySet());
						keys.sort((t1, t2) -> {
							return -Integer.compare(t1.split("/").length, t2.split("/").length);
						});

						for (String key : keys) {
							String url = path;
							if (!url.endsWith("/"))
								url += "/";

							String supportedURL = key;
							if (!supportedURL.endsWith("/"))
								supportedURL += "/";

							if (url.startsWith(supportedURL)) {
								setResponseCode(300);
								execPage(indexPages.get(key), sourceFile, path, client);
								return;
							}
						}

						if (context.getDefaultIndexPage() == null) {
							setResponseCode(404);
							setResponseMessage("File not found");
							setBody("text/html", getError());
						} else {
							setResponseCode(300);
							execPage(context.getDefaultIndexPage(), sourceFile, path, client);
						}
					}
					return;
				}
			} else {
				for (FilePostHandler handler : context.getPostHandlers()) {
					if (handler instanceof IContextProviderExtension) {
						((IContextProviderExtension) handler).provide(context);
					}
					if (handler.match(getRequest(), path)) {
						HttpResponse response = (file != null ? file.getRewrittenResponse() : getResponse());

						FilePostHandler inst = handler.instanciate(getServer(), getRequest(), response, path);
						if (inst instanceof IPathProviderExtension) {
							String pth = path;
							if (pth.endsWith("/"))
								pth = pth.substring(0, pth.length() - 1);
							((IPathProviderExtension) inst).provide(pth);
						}
						inst.process(contentType, client);

						file = FileContext.create(response, path, response.body);

						this.setResponse(file.getRewrittenResponse());
						if (this.getResponse().body == null) {
							this.setBody("text/html", this.getError());
						}
						return;
					}
				}
				setResponseCode(403);
				setResponseMessage("POST not supported");
				setBody("text/html", getError());
				return;
			}

			for (IFileExtensionProvider provider : context.getExtensions()) {
				if (provider instanceof IPathProviderExtension) {
					String pth = path;
					if (pth.endsWith("/"))
						pth = pth.substring(0, pth.length() - 1);

					((IPathProviderExtension) provider).provide(pth);
				}
				if (provider instanceof IContextProviderExtension) {
					((IContextProviderExtension) provider).provide(context);
				}
				if (provider instanceof IServerProviderExtension) {
					((IServerProviderExtension) provider).provide(getServer());
				}
				if (sourceFile.getName().endsWith(provider.fileExtension())) {
					getResponse().body = strm;
					file = provider.rewrite(getResponse(), getRequest());
					break;
				}
			}

			this.setResponse(file.getRewrittenResponse());
			if (this.getResponse().body == null) {
				this.setBody("text/html", this.getError());
			}
		}
	}

	private String getError() {
		return getServer().genError(getResponse(), getRequest());
	}

	private void execPage(IndexPageProvider page, File sourceFile, String path, Socket client) {
		File[] files = sourceFile.listFiles(new FileFilter() {

			@Override
			public boolean accept(File arg0) {
				return !arg0.isDirectory();
			}

		});
		File[] dirs = sourceFile.listFiles(new FileFilter() {

			@Override
			public boolean accept(File arg0) {
				return arg0.isDirectory();
			}

		});
		IndexPageProvider inst = page.instanciate(files, dirs, getServer(), getRequest(), getResponse(), path);
		if (inst instanceof IContextProviderExtension) {
			((IContextProviderExtension) inst).provide(context);
		}
		inst.process(client, dirs, files);
	}

	@Override
	public HttpPostProcessor createNewInstance() {
		return new MainFileProcessor(path, context);
	}

	@Override
	public boolean supportsChildPaths() {
		return true;
	}

	@Override
	public boolean supportsGet() {
		return true;
	}

}
