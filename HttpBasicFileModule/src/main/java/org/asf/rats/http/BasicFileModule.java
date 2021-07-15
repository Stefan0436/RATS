package org.asf.rats.http;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.asf.cyan.api.common.CYAN_COMPONENT;
import org.asf.cyan.api.common.CyanComponent;
import org.asf.rats.ConnectiveHTTPServer;
import org.asf.rats.Memory;
import org.asf.rats.http.internal.implementation.DefaultFileProcessor;

/**
 * 
 * Basic file module for ConnectiveHTTP.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
@CYAN_COMPONENT
public class BasicFileModule extends CyanComponent {
	protected static void initComponent() {
		DefaultFileProcessor.assign();
		
		Memory.getInstance().getOrCreate("bootstrap.call").append(new Runnable() {

			@Override
			public void run() {
				Class<IAutoContextBuilder>[] builders = findClasses(getMainImplementation(), IAutoContextBuilder.class);
				Class<IAutoContextModificationProvider>[] providers = findClasses(getMainImplementation(),
						IAutoContextModificationProvider.class);

				if (builders.length != 0) {
					for (Class<IAutoContextBuilder> builder : builders) {
						try {
							Constructor<IAutoContextBuilder> cont = builder.getDeclaredConstructor();
							cont.setAccessible(true);
							IAutoContextBuilder contBuilder = cont.newInstance();

							ProviderContextFactory factory = new ProviderContextFactory();
							factory.setOption(contBuilder.contextOptions());

							if ((ProviderContextFactory.OPTION_DISABLE_DEFAULT_INDEX | contBuilder
									.contextOptions()) != ProviderContextFactory.OPTION_DISABLE_DEFAULT_INDEX)
								factory.setDefaultIndexPage(contBuilder.defaultIndexPage());

							factory.setExecLocation(contBuilder.hostDir());
							factory.setRootFile(contBuilder.virtualDir());

							factory.addAliases(contBuilder.aliases());
							factory.addExtensions(contBuilder.extensions());
							factory.addUploadHandlers(contBuilder.uploadHandlers());
							factory.addProcessors(contBuilder.getHandlers());
							factory.addRestrictions(contBuilder.restrictions());
							factory.addIndexPages(contBuilder.altIndexPages());
							factory.addDocumentPostProcessors(contBuilder.documentPostProcessors());

							for (Class<IAutoContextModificationProvider> provider : providers) {
								try {
									Constructor<IAutoContextModificationProvider> prov = provider
											.getDeclaredConstructor();
									cont.setAccessible(true);

									IAutoContextModificationProvider providerInstance = prov.newInstance();
									providerInstance.accept(factory);
								} catch (NoSuchMethodException | SecurityException | InstantiationException
										| IllegalAccessException | IllegalArgumentException
										| InvocationTargetException ex) {
									error("Failed to construct context modification provider: "
											+ provider.getTypeName(), ex);
								}
							}

							FileProcessorContextFactory.getDefault().addProviderContext(factory.build());
						} catch (NoSuchMethodException | SecurityException | InstantiationException
								| IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
							error("Failed to construct default context provider: " + builder.getTypeName(), ex);
						}
					}
				}

				FileProcessorContextFactory.getDefault().build().apply(ConnectiveHTTPServer.getMainServer());
			}

		});
	}
}
