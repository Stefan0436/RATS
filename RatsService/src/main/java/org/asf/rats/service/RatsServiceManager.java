package org.asf.rats.service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import org.asf.aos.util.service.ServiceModule;
import org.asf.aos.util.service.UtilService;
import org.asf.aos.util.service.extra.slib.LoadSet;
import org.asf.aos.util.service.extra.slib.communication.SlibPacket;
import org.asf.aos.util.service.extra.slib.communication.SlibUtilService;
import org.asf.aos.util.service.extra.slib.coremodule.CoreloadSet;
import org.asf.aos.util.service.extra.slib.coremodule.SlibCoremodule;
import org.asf.aos.util.service.extra.slib.processors.ProcessorRegistry;
import org.asf.aos.util.service.extra.slib.processors.ServicePacketProcessor;
import org.asf.aos.util.service.extra.slib.smcore.SlibLogHandler;
import org.asf.aos.util.service.extra.slib.smcore.SlibLogger;
import org.asf.aos.util.service.extra.slib.smcore.SlibManager;
import org.asf.aos.util.service.extra.slib.smcore.SlibModule;
import org.asf.aos.util.service.extra.slib.smcore.SlibStopHandler;
import org.asf.cyan.api.common.CYAN_COMPONENT;
import org.asf.rats.Memory;
import org.asf.rats.components.RatsComponents;
import org.asf.rats.configuration.RatsConfiguration;

/**
 * 
 * RaTs! AOS-UTIL Service Component.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
@CYAN_COMPONENT
public class RatsServiceManager {
	public static final String RATS_SERUP_CHANNEL = "RATS-SERVICE-SETUP";
	
	private static boolean ownThread;
	private static SlibUtilService service;
	private static SlibCoremodule moduleStartMethod;
	private static ArrayList<ServiceModule> modules;
	private static ArrayList<SlibStopHandler> lowLevelStopHandlers;

	@SuppressWarnings("unchecked")
	protected static void initComponent() {
		try {
			Field field = SlibPacket.class.getDeclaredField("fromServer");
			field.setAccessible(true);
			field.set(null, true);
			field.setAccessible(false);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
		}

		ProcessorRegistry.setClassRetriever(() -> {
			return (Class<? extends ServicePacketProcessor<?>>[]) RatsComponents
					.findClasses(ServicePacketProcessor.class);
		});

		SlibLogger.addLogger(Memory.getInstance().getOrCreate("logging.bindings").getValue(SlibLogHandler.class));
		Memory.getInstance().getOrCreate("bootstrap.exec.call").append(new Runnable() {

			@Override
			public void run() {
				SlibLogger.info("Finding SLIB Coremodules...", RATS_SERUP_CHANNEL);
				ArrayList<Class<? extends SlibCoremodule>> coremoduleClasses = new ArrayList<Class<? extends SlibCoremodule>>(
						Arrays.asList(RatsComponents.findClasses(SlibCoremodule.class)));

				ArrayList<SlibCoremodule> coremodules = new ArrayList<SlibCoremodule>();
				modules = new ArrayList<ServiceModule>();

				SlibLogger.info("Loading SLIB Coremodules... Loading embedded regular modules...", RATS_SERUP_CHANNEL);
				for (Class<? extends SlibCoremodule> module : coremoduleClasses) {
					if (ServiceModule.class.isAssignableFrom(module) && CoreloadSet.class.isAssignableFrom(module)) {

						ServiceModule mod = null;
						SlibCoremodule coremodInteface = null;
						CoreloadSet loadset = null;

						try {
							mod = UtilService.constructModule(module.getTypeName(), SlibManager.classLoader);
							coremodInteface = (SlibCoremodule) mod;
							loadset = (CoreloadSet) mod;
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
								| InvocationTargetException | NoSuchMethodException | SecurityException
								| ClassNotFoundException e) {
							SlibLogger.error(
									"Failed to load module " + module.getTypeName() + ", an exception was thrown: "
											+ e.getClass().getTypeName() + ": " + e.getMessage(),
									RATS_SERUP_CHANNEL);
						}

						SlibLogger.info("Found module: " + mod.id(), RATS_SERUP_CHANNEL);
						modules.add(mod);
						if (loadset.allowEarlyLoading()) {
							SlibLogger.info("Found coremodule: " + mod.id(), RATS_SERUP_CHANNEL);
							coremodules.add(coremodInteface);
						}
					}
				}

				moduleStartMethod = null;
				SlibLogger.info("Calling SLIB Coremodule early loading methods...", RATS_SERUP_CHANNEL);
				for (SlibCoremodule module : coremodules) {
					if (module.loadEarly())
						moduleStartMethod = module;
				}

				SlibLogger.info("Loading AOS-UTIL service...", RATS_SERUP_CHANNEL);
				ownThread = SlibManager.startInOwnThread;
				service = SlibManager.constructingService;
				if (service == null) {
					service = new SlibUtilService();
				}

				service.setMode(true);
				service.setPort(RatsConfiguration.getInstance().servicePort);
				service.serverInfoPath = ".rats-service";

				SlibLogger.info("Setting up SLIB...", RATS_SERUP_CHANNEL);
				SlibLogger.normal("--- SERVICE STARTUP ---", RATS_SERUP_CHANNEL);
				SlibManager.setupForService(service, ".", "RaTs");

				SlibLogger.info("Loading remaining core module classes...", RATS_SERUP_CHANNEL);
				for (Class<? extends SlibCoremodule> module : coremoduleClasses) {
					if (ServiceModule.class.isAssignableFrom(module)) {
						ServiceModule mod = null;
						SlibCoremodule coremodInteface = null;
						CoreloadSet loadset = null;
						try {
							mod = UtilService.constructModule(module.getTypeName(), SlibManager.classLoader);
							coremodInteface = (SlibCoremodule) mod;
							if (mod instanceof CoreloadSet)
								loadset = (CoreloadSet) mod;
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
								| InvocationTargetException | NoSuchMethodException | SecurityException
								| ClassNotFoundException e) {
							SlibLogger.error(
									"Failed to load module " + module.getTypeName() + ", an exception was thrown: "
											+ e.getClass().getTypeName() + ": " + e.getMessage(),
									RATS_SERUP_CHANNEL);
						}
						SlibLogger.debug("Loaded class: " + module.getTypeName(), RATS_SERUP_CHANNEL);
						modules.add(mod);
						if (loadset == null || !loadset.allowEarlyLoading())
							coremodules.add(coremodInteface);
					}
				}

				SlibLogger.info("Loading remaining core modules...", RATS_SERUP_CHANNEL);
				for (SlibCoremodule module : coremodules) {
					SlibLogger.info("Loading core module: " + ((ServiceModule) module).id() + "...",
							RATS_SERUP_CHANNEL);
					module.initCoremodule();
					coremodules.remove(module);
				}

				SlibLogger.info("Loading normal module classes...", RATS_SERUP_CHANNEL);
				Class<? extends ServiceModule>[] moduleClasses = RatsComponents.findClasses(ServiceModule.class);
				for (Class<? extends ServiceModule> module : moduleClasses) {
					if (!SlibCoremodule.class.isAssignableFrom(module) && SlibModule.class.isAssignableFrom(module)) {
						ServiceModule mod = null;
						try {
							mod = UtilService.constructModule(module.getTypeName(), SlibManager.classLoader);
							SlibLogger.debug("Loaded class: " + module.getTypeName(), RATS_SERUP_CHANNEL);
							modules.add(mod);
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
								| InvocationTargetException | NoSuchMethodException | SecurityException
								| ClassNotFoundException e) {
							SlibLogger.error(
									"Failed to load module " + module.getTypeName() + ", an exception was thrown: "
											+ e.getClass().getTypeName() + ": " + e.getMessage(),
									RATS_SERUP_CHANNEL);
						}
					}
				}

				SlibLogger.info("Setting up normal modules...", RATS_SERUP_CHANNEL);
				for (ServiceModule module : new ArrayList<ServiceModule>(modules)) {
					if (module instanceof SlibModule && !SlibManager.hasEnableState(module.id())) {
						boolean defaultValue = true;
						if (module instanceof LoadSet) {
							defaultValue = ((LoadSet) module).enabledDefault();
						}

						SlibLogger.debug("Registering default state of module " + module.id() + "... Module sate: "
								+ defaultValue, RATS_SERUP_CHANNEL);
						if (defaultValue) {
							SlibManager.enableModule((ServiceModule & SlibModule) module);
						} else {
							SlibManager.disableModule((ServiceModule & SlibModule) module);
						}
					}

					if (module instanceof LoadSet) {
						if (!((LoadSet) module).allowLoad()) {
							modules.remove(module);
						}
					}
				}

				SlibLogger.debug("Pre-loading modules...", RATS_SERUP_CHANNEL);
				for (ServiceModule module : modules) {
					SlibManager.addModule((ServiceModule & SlibModule) module);
				}

				SlibLogger.info("Registering module processors...", RATS_SERUP_CHANNEL);
				for (ServiceModule module : modules) {
					ProcessorRegistry.registerProcessorsForModule((ServiceModule & SlibModule) module);
				}

				SlibLogger.normal("Initializing modules...", RATS_SERUP_CHANNEL);
				for (ServiceModule module : modules) {
					SlibManager.initModule(module.id());
				}

				SlibLogger.info("Loading service jar modules from command line...", RATS_SERUP_CHANNEL);
				service.loadJarModules();

				lowLevelStopHandlers = new ArrayList<SlibStopHandler>();
				SlibLogger.info("Loading low-level ServiceModules...", RATS_SERUP_CHANNEL);
				for (Class<? extends ServiceModule> module : moduleClasses) {
					if (!SlibCoremodule.class.isAssignableFrom(module) && !SlibModule.class.isAssignableFrom(module)) {
						ServiceModule mod = null;
						try {
							mod = UtilService.constructModule(module.getTypeName(), SlibManager.classLoader);
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
								| InvocationTargetException | NoSuchMethodException | SecurityException
								| ClassNotFoundException e) {
						}
						if (mod instanceof SlibStopHandler) {
							lowLevelStopHandlers.add((SlibStopHandler) mod);
						}
						service.registerModule(mod);
					}
				}

				
			}
		});
		
		Memory.getInstance().getOrCreate("bootstrap.exec.call").append(new Runnable() {

			@Override
			public void run() {
				SlibLogger.normal("Starting the AOS-UTIL service...", RATS_SERUP_CHANNEL);
				SlibManager.loadFinish();

				if (ownThread) {
					FINALMOD = moduleStartMethod;
					SERVICE = service;
					Thread server = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								if (FINALMOD != null) {
									FINALMOD.startMethod(SERVICE);
								} else {
									SERVICE.run();
								}
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					}, "RaTs! Service Thread");
					server.start();
					while (!SERVICE.isOnline()) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							break;
						}
					}
					SlibLogger.normal("--- SERVICE EXECUTION START ---", RATS_SERUP_CHANNEL);
					SlibLogger.info("Post-initializing modules...", RATS_SERUP_CHANNEL);
					for (ServiceModule module : modules) {
						SlibManager.postInitModule(module.id());
					}
					SlibLogger.info("Service loading cycle completed.", RATS_SERUP_CHANNEL);
					while (SERVICE.isOnline()) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							break;
						}
					}
					SlibLogger.normal("--- SERVICE EXECUTION END ---", RATS_SERUP_CHANNEL);
					SlibLogger.info("Calling exit events...", RATS_SERUP_CHANNEL);
					for (ServiceModule module : modules) {
						if (module instanceof SlibStopHandler) {
							((SlibStopHandler) module).postStop();
						}
					}
					for (SlibStopHandler module : lowLevelStopHandlers) {
						module.postStop();
					}
					SlibLogger.normal("--- SERVICE SHUTDOWN ---", RATS_SERUP_CHANNEL);
				} else
					moduleStartMethod.startMethod(service);
			}
			
		});
	}

	private static SlibCoremodule FINALMOD = null;
	private static UtilService SERVICE = null;
}
