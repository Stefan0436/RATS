package org.asf.rats.http.internal.implementation;

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

import org.asf.rats.http.internal.ProcessorAbstract;
import org.asf.rats.http.providers.FileUploadHandler;
import org.asf.rats.http.providers.IClientSocketProvider;
import org.asf.rats.http.providers.IContextProviderExtension;
import org.asf.rats.http.providers.IContextRootProviderExtension;
import org.asf.rats.http.providers.IFileAlias;
import org.asf.rats.http.providers.IFileExtensionProvider;
import org.asf.rats.http.providers.IFileRestrictionProvider;
import org.asf.rats.http.providers.IPathProviderExtension;
import org.asf.rats.http.providers.IServerProviderExtension;
import org.asf.rats.http.providers.IVirtualFileProvider;
import org.asf.rats.http.providers.IndexPageProvider;

public class DefaultFileProcessor extends ProcessorAbstract {

	private DefaultFileProcessor() {
	}

	public static void assign() {
		if (hasBeenAssigned())
			return;

		assignImplementation(new DefaultFileProcessor());
	}

	@Override
	public void process(String path, String contentType, Socket client, String method) {
		if (path.startsWith("..")) {
			setResponseCode(403);
			setResponseMessage("Access to parent directories denied");
			setBody("text/html", getError());
		} else {
			path = "/" + path;
			for (IFileAlias provider : getContext().getAliases()) {
				if (provider.match(getRequest(), path)) {
					IFileAlias alias = provider.newInstance();

					if (alias instanceof IContextProviderExtension) {
						((IContextProviderExtension) alias).provide(getContext());
					}
					if (alias instanceof IServerProviderExtension) {
						((IServerProviderExtension) alias).provide(getServer());
					}
					if (alias instanceof IClientSocketProvider) {
						((IClientSocketProvider) alias).provide(client);
					}
					if (alias instanceof IContextRootProviderExtension) {
						((IContextRootProviderExtension) alias).provideVirtualRoot(getContextRoot());
					}

					path = alias.rewrite(getRequest(), path);
					break;
				}
			}

			for (IFileRestrictionProvider provider : getContext().getRestrictions()) {
				if (provider.match(getRequest(), path)) {
					IFileRestrictionProvider restriction = provider.newInstance();

					if (restriction instanceof IContextProviderExtension) {
						((IContextProviderExtension) restriction).provide(getContext());
					}
					if (restriction instanceof IServerProviderExtension) {
						((IServerProviderExtension) restriction).provide(getServer());
					}
					if (restriction instanceof IClientSocketProvider) {
						((IClientSocketProvider) restriction).provide(client);
					}
					if (restriction instanceof IContextRootProviderExtension) {
						((IContextRootProviderExtension) restriction).provideVirtualRoot(getContextRoot());
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
			}

			for (IVirtualFileProvider provider : getContext().getVirtualFiles()) {
				if (((contentType == null && !method.equals("DELETE")) || provider.supportsUpload())
						&& provider.match(path, getRequest())) {

					IVirtualFileProvider file = provider.newInstance();

					if (file instanceof IContextProviderExtension) {
						((IContextProviderExtension) file).provide(getContext());
					}
					if (file instanceof IServerProviderExtension) {
						((IServerProviderExtension) file).provide(getServer());
					}
					if (file instanceof IClientSocketProvider) {
						((IClientSocketProvider) file).provide(client);
					}
					if (file instanceof IContextRootProviderExtension) {
						((IContextRootProviderExtension) file).provideVirtualRoot(getContextRoot());
					}

					setResponseCode(200);
					file.process(path, contentType, getRequest(), getResponse(), client, method);

					if (this.getResponse().body == null) {
						this.setBody("text/html", this.getError());
					}

					return;
				}
			}

			File sourceFile = new File(getContext().getSourceDirectory(), path);
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
				for (FileUploadHandler handler : getContext().getUploadHandlers()) {
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
							((IContextProviderExtension) inst).provide(getContext());
						}
						if (inst instanceof IContextRootProviderExtension) {
							((IContextRootProviderExtension) inst).provideVirtualRoot(getContextRoot());
						}

						if (inst.requiresClosedFile() && strm != null) {
							try {
								strm.close();
							} catch (IOException e) {
							}
						}

						boolean support = inst.process(contentType, client, method);
						if (support) {
							setResponse(response);

							if (getResponse().body == null) {
								setBody("text/html", this.getError());
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

			for (IFileExtensionProvider provider : getContext().getExtensions()) {
				if (sourceFile.getName().endsWith(provider.fileExtension())) {

					IFileExtensionProvider inst = provider.newInstance();

					if (inst instanceof IPathProviderExtension) {
						String pth = path;
						if (pth.endsWith("/"))
							pth = pth.substring(0, pth.length() - 1);

						((IPathProviderExtension) inst).provide(pth);
					}

					if (inst instanceof IContextProviderExtension) {
						((IContextProviderExtension) inst).provide(getContext());
					}
					if (inst instanceof IServerProviderExtension) {
						((IServerProviderExtension) inst).provide(getServer());
					}
					if (inst instanceof IClientSocketProvider) {
						((IClientSocketProvider) inst).provide(client);
					}
					if (inst instanceof IContextRootProviderExtension) {
						((IContextRootProviderExtension) inst).provideVirtualRoot(getContextRoot());
					}

					getResponse().body = strm;
					file = inst.rewrite(getResponse(), getRequest());

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
				getContext().getAltIndexPages());

		if (indexPages.isEmpty()) {
			if (getContext().getDefaultIndexPage() == null) {
				setResponseCode(404);
				setResponseMessage("File not found");
				setBody("text/html", getError());
			} else {
				setResponseCode(300);
				execPage(getContext().getDefaultIndexPage(), sourceFile, path, client);
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

			if (getContext().getDefaultIndexPage() == null) {
				setResponseCode(404);
				setResponseMessage("File not found");
				setBody("text/html", getError());
			} else {
				setResponseCode(300);
				execPage(getContext().getDefaultIndexPage(), sourceFile, path, client);
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
			((IContextProviderExtension) inst).provide(getContext());
		}
		if (inst instanceof IContextRootProviderExtension) {
			((IContextRootProviderExtension) inst).provideVirtualRoot(getContextRoot());
		}
		inst.process(client, dirs, files);
	}

	@Override
	protected ProcessorAbstract newInstance() {
		return new DefaultFileProcessor();
	}

}