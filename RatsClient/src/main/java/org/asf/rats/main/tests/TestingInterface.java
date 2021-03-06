package org.asf.rats.main.tests;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.Set;

import org.asf.rats.main.tests.commands.exceptions.InvalidAliasException;

import org.asf.rats.main.ClientMain;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

// Forked-ish from the Cornflower test interface
public class TestingInterface {

	public static void main(String[] args) throws Exception {
		new TestingInterface().run();
	}

	static final String version = "1.0";
	static Scanner scanner = new Scanner(System.in);
	static ArrayList<InteractiveTestCommand> commands = new ArrayList<InteractiveTestCommand>();

	private TestingInterface() throws Exception {
		WriteLine("RaTs! Interactive is starting...");
		WriteLine("Scanning classpath for commands...");
		ScanClasspath();
		WriteLine("");
	}

	void run() throws Exception {
		String clientPath = System.getProperty("client.info.file");
		ClientMain.setPath(clientPath);

		WriteLine("");
		WriteLine("");
		WriteLine("RaTs! Interactive Testing Environment, version " + version);
		WriteLine("Client Version: " + ClientMain.getVersion());
		WriteLine("Client Information Path: " + clientPath);
		WriteLine("");
		WriteLine("You can use the commands: rats for interacting with a rats server.");
		WriteLine("Use help for more commands");

		while (true) {
			String input = ReadInput("TESTER: " + new File(System.getProperty("user.dir")).getName());
			System.clearProperty("log4j2.configurationFile");
			String response = Execute(input);
			if (response == null) {
				System.err.println("Command not recognized, use help for a list of commands");
			} else if (response != "") {
				System.err.println(response);
			}
			WriteLine("");
		}
	}

	void ScanClasspath() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvalidAliasException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		Enumeration<URL> roots = ClassLoader.getSystemClassLoader().getResources("");

		ConfigurationBuilder conf = ConfigurationBuilder.build();

		for (URL i : Collections.list(roots)) {
			conf.addUrls(i);
		}

		Reflections reflections = new Reflections(conf);
		Set<Class<?>> classes = reflections.getTypesAnnotatedWith(TestCommand.class);
		for (Class<?> c : classes) {
			if (InteractiveTestCommand.class.isAssignableFrom(c)
					&& !c.getName().equals(InteractiveTestCommand.class.getName())) {
				@SuppressWarnings("unchecked")
				Class<InteractiveTestCommand> c2 = (Class<InteractiveTestCommand>) c;
				RegisterTestCommand(c2);
			}
		}
		commands.sort((p1, p2) -> p1.getId().compareTo(p2.getId()));
	}

	/**
	 * Get a list of all commands
	 * 
	 * @return List of commands
	 */
	public InteractiveTestCommand[] getAllCommands() {
		return commands.toArray(new InteractiveTestCommand[0]);
	}

	/**
	 * Register a command class (note, the system scans the classpath and registers
	 * all classes extending InteractiveTestCommand automatically, only use if the
	 * command was not found)
	 * 
	 * @param command The command class to register
	 * @throws InstantiationException    if the command initialization fails
	 * @throws IllegalAccessException    if the command cannot be accessed
	 * @throws IllegalArgumentException  if the command id is in use
	 * @throws InvalidAliasException     if an alias is invalid
	 * @throws InvocationTargetException if creating the command instance fails
	 * @throws SecurityException         if creating the command instance fails
	 * @throws NoSuchMethodException     if creating the command instance fails
	 */
	public void RegisterTestCommand(Class<InteractiveTestCommand> command)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InvalidAliasException {
		commands.forEach((entry) -> {
			if (entry.getClass().getName().equals(command.getName())) {
				throw new IllegalArgumentException("Command of same type already registered.");
			}
		});
		InteractiveTestCommand cmd = InteractiveTestCommand.CreateInstance(this, command);
		if (cmd.getId().toLowerCase() != cmd.getId())
			new IllegalArgumentException("Command id '" + cmd.getId() + "' is invalid, ids must be lowercase only.");
		if (cmd.getId().contains(" "))
			new IllegalArgumentException("Command id '" + cmd.getId() + "' is invalid, ids cannot contain spaces.");
		WriteLine("Registering command with ID: '" + cmd.getId() + "', Syntax '" + cmd.helpSyntax()
				+ "', Description: '" + cmd.helpDescription() + "'...");
		for (InteractiveTestCommand entry : commands) {
			if (entry.getId().equalsIgnoreCase(cmd.getId())) {
				throw new IllegalArgumentException("Command with id '" + cmd.getId() + "' already registered.");
			}
			for (String alias : entry.getAliases()) {
				if (alias.contentEquals(cmd.getId()))
					throw new InvalidAliasException("Alias is invalid, id was found as alias to another command.",
							alias, cmd);
				else {
					for (String alias2 : cmd.getAliases()) {
						if (cmd.getId().equalsIgnoreCase(alias2))
							throw new InvalidAliasException("Alias is invalid, the command id was found as alias",
									alias2, cmd);
						else {
							if (alias2.equalsIgnoreCase(entry.getId()))
								throw new InvalidAliasException(
										"Alias is invalid, id was found as alias to another command", alias2, entry);
							else if (alias2.toLowerCase() != alias2)
								throw new InvalidAliasException("Alias id is invalid, aliases must be lowercase only.",
										alias2, entry);
							else if (alias2.contains(" "))
								throw new InvalidAliasException("Alias id is invalid, aliases cannot contain spaces.",
										alias2, entry);
							else {
								if (alias2.equalsIgnoreCase(alias))
									throw new InvalidAliasException("Alias was already registered.", alias2, entry);
							}
						}
					}
				}
			}
		}
		commands.add(cmd);
	}

	/**
	 * Get command object by id or alias
	 * 
	 * @param id The command id/alias
	 * @return Command object, null if not found
	 */
	public InteractiveTestCommand GetCommand(String id) {
		for (InteractiveTestCommand entry : commands) {
			if (entry.getId().equalsIgnoreCase(id)) {
				return entry;
			}
			for (String alias : entry.getAliases()) {
				if (id.equalsIgnoreCase(alias)) {
					return entry;
				}
			}
		}
		return null;
	}

	public String Execute(String input) throws Exception {
		if (input.isEmpty())
			return null;
		ArrayList<String> cmdline = ParseCommand(input);
		String id = cmdline.get(0);
		cmdline.remove(0);
		InteractiveTestCommand cmd = GetCommand(id);
		if (cmd == null)
			return null;
		if (System.getProperty("debug") == null) {
			try {
				if (!cmd.execute(this, cmdline.toArray(new String[0]))) {
					return "Invalid usage,\nUsage: " + cmd.getHelpMessage(false) + "\n";
				}
			} catch (Exception ex) {
				return ex.toString();
			}
		}
		if (!cmd.execute(this, cmdline.toArray(new String[0]))) {
			return "Invalid usage,\nUsage: " + cmd.getHelpMessage(false) + "\n";
		}
		return "";
	}

	public String ReadInput(String message) {
		Write(message + "> ");
		return scanner.nextLine();
	}

	public void Write(Object o) {
		System.out.print(o.toString());
	}

	public void WriteLine(Object o) {
		System.out.println(o.toString());
	}

	public ArrayList<String> ParseCommand(String args) {
		ArrayList<String> args3 = new ArrayList<String>();
		char[] argarray = args.toCharArray();
		boolean ignorespaces = false;
		String last = "";
		int i = 0;
		for (char c : args.toCharArray()) {
			if (c == '"' && (i == 0 || argarray[i - 1] != '\\')) {
				if (ignorespaces)
					ignorespaces = false;
				else
					ignorespaces = true;
			} else if (c == ' ' && !ignorespaces && (i == 0 || argarray[i - 1] != '\\')) {
				args3.add(last);
				last = "";
			} else if (c != '\\' || (i + 1 < argarray.length && argarray[i + 1] != '"'
					&& (argarray[i + 1] != ' ' || ignorespaces))) {
				last += c;
			}

			i++;
		}

		if (last == "" == false)
			args3.add(last);

		return args3;
	}

}
