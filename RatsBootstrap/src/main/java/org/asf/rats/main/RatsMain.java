package org.asf.rats.main;

import java.io.File;

import org.asf.rats.Memory;
import org.asf.rats.components.RatsComponents;

public class RatsMain {

	public static void deleteDir(File dir) {
		for (File f : dir.listFiles(t -> !t.isDirectory())) {
			f.delete();
		}
		for (File d : dir.listFiles(t -> t.isDirectory())) {
			deleteDir(d);
		}
		dir.delete();
	}

	public static void main(String[] args) {
		if (new File("logs").exists())
			deleteDir(new File("logs"));
		
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
