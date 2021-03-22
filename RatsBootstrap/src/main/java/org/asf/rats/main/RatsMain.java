package org.asf.rats.main;

import org.asf.rats.Memory;
import org.asf.rats.components.RatsComponents;

public class RatsMain {

	public static void main(String[] args) {
		if (System.getProperty("ideMode") != null) {
			System.setProperty("log4j2.configurationFile", RatsMain.class.getResource("/log4j2-ide.xml").toString());
		} else {
			System.setProperty("log4j2.configurationFile", RatsMain.class.getResource("/log4j2.xml").toString());
		}
		RatsComponents.initializeComponents();
		Memory mem = Memory.getInstance().getOrCreate("bootstrap.call");
		for (Runnable runnable : mem.getValues(Runnable.class)) {
			runnable.run();
		}
		mem = Memory.getInstance().getOrCreate("bootstrap.exec.call");
		for (Runnable runnable : mem.getValues(Runnable.class)) {
			runnable.run();
		}
	}

}
