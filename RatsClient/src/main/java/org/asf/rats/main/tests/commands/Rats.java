package org.asf.rats.main.tests.commands;

import java.util.Arrays;
import java.util.List;

import org.asf.rats.main.ClientMain;
import org.asf.rats.main.tests.InteractiveTestCommand;

public class Rats extends InteractiveTestCommand {
	
	@Override
	public String getId() {
		return "rats";
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList();
	}

	@Override
	public String helpSyntax() {
		return "[rats arguments]";
	}

	@Override
	public String helpDescription() {
		return "run the client";
	}

	@Override
	protected Boolean execute(String[] arguments) throws Exception {
		ClientMain.main(arguments);
		return true;
	}
	
}
