package org.asf.rats;

import static org.junit.Assert.assertTrue;

import org.asf.rats.events.HelpEvent;
import org.junit.Test;

public class MatcherTest {

	@Test
	public void test() {
		assertTrue(HelpEvent.parseMatch("test*", "tester one"));
		assertTrue(HelpEvent.parseMatch("test* one", "tester one"));
		assertTrue(HelpEvent.parseMatch("test* one*", "tester one"));
		assertTrue(HelpEvent.parseMatch("test* o*e*", "tester one"));
	}
}
