package org.asf.rats.http.internal.processors;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import org.asf.rats.HttpResponse;
import org.asf.rats.http.FileContext;
import org.asf.rats.http.MainFileMap;
import org.asf.rats.http.ProviderContext;
import org.asf.rats.http.providers.FileUploadHandler;
import org.asf.rats.http.providers.IContextProviderExtension;
import org.asf.rats.http.providers.IFileAlias;
import org.asf.rats.http.providers.IFileExtensionProvider;
import org.asf.rats.http.providers.IFileRestrictionProvider;
import org.asf.rats.http.providers.IPathProviderExtension;
import org.asf.rats.http.providers.IServerProviderExtension;
import org.asf.rats.http.providers.IVirtualFileProvider;
import org.asf.rats.http.providers.IndexPageProvider;
import org.asf.rats.processors.HttpUploadProcessor;

public class MainFileProcessor extends HttpUploadProcessor {

	protected String path = "";
	protected ProviderContext context;

	public MainFileProcessor(String path, ProviderContext context) {
		this.path = path;
		this.context = context;
	}

	@Override
	public String path() {
		return path;
	}

	@Override
	public void process(String contentType, Socket client, String method) {
		String path = getRequestPath().substring(path().length());
		if (path.contains("\\")) {
			setResponseCode(403);
			setResponseMessage("Use of Windows path separator denied");
			setBody("text/html", getError());
			return;
		}
		while (path.startsWith("/")) {
			path = path.substring(1);
		}

		if (path.startsWith("..")) {
			setResponseCode(403);
			setResponseMessage("Access to parent directories denied");
			setBody("text/html", getError());
		} else {
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
					setResponseCode(restriction.getResponseCode(getRequest()));
					setResponseMessage(restriction.getResponseMessage(getRequest()));
					restriction.rewriteResponse(getRequest(), getResponse());

					if (getResponse().body == null) {
						setBody("text/html", getError());
					}

					return;
				}
			}

			for (IVirtualFileProvider provider : context.getVirtualFiles()) {
				if (((contentType == null && !method.equals("DELETE")) || provider.supportsUpload())
						&& provider.match(path, getRequest())) {

					IVirtualFileProvider file = provider.newInstance();

					if (file instanceof IContextProviderExtension) {
						((IContextProviderExtension) file).provide(context);
					}
					if (file instanceof IServerProviderExtension) {
						((IServerProviderExtension) file).provide(getServer());
					}

					setResponseCode(200);
					file.process(path, contentType, getRequest(), getResponse(), client, method);

					if (this.getResponse().body == null) {
						this.setBody("text/html", this.getError());
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

			if (contentType == null && !method.equals("DELETE")) {
				if (sourceFile.isDirectory()) {
					processDir(sourceFile, path, client);
					return;
				}
			} else {
				for (FileUploadHandler handler : context.getUploadHandlers()) {
					if (!handler.supportsDirectories() && sourceFile.exists() && sourceFile.isDirectory()) {
						continue;
					}
					if (handler.match(getRequest(), path)) {
						HttpResponse response = (file != null ? file.getRewrittenResponse() : getResponse());

						FileUploadHandler inst = handler.instanciate(getServer(), getRequest(), response, path,
								sourceFile);
						if (inst instanceof IPathProviderExtension) {
							String pth = path;
							if (pth.endsWith("/"))
								pth = pth.substring(0, pth.length() - 1);
							((IPathProviderExtension) inst).provide(pth);
						}
						if (inst instanceof IContextProviderExtension) {
							((IContextProviderExtension) inst).provide(context);
						}
						if (inst.requiresClosedFile() && strm != null) {
							try {
								strm.close();
							} catch (IOException e) {
							}
						}
						boolean support = inst.process(contentType, client, method);
						if (support) {
							file = FileContext.create(response, path, response.body);

							this.setResponse(file.getRewrittenResponse());
							if (this.getResponse().body == null) {
								this.setBody("text/html", this.getError());
							}

							return;
						} else {
							setResponseCode(405);
							setResponseMessage(method.toUpperCase() + " not supported");
							setBody("text/html", getError());
							return;
						}
					}
				}

				if (sourceFile.exists() && sourceFile.isDirectory()) {
					processDir(sourceFile, path, client);
					return;
				}

				setResponseCode(405);
				setResponseMessage(method.toUpperCase() + " not supported");
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

	protected void processDir(File sourceFile, String path, Socket client) {
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
	}

	protected String getError() {
		return getServer().genError(getResponse(), getRequest());
	}

	protected void execPage(IndexPageProvider page, File sourceFile, String path, Socket client) {
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
	public HttpUploadProcessor createNewInstance() {
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
