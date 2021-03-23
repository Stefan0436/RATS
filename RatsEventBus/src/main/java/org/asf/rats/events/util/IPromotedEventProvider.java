package org.asf.rats.events.util;

import org.asf.cyan.api.events.IEventProvider;

/**
 * 
 * Promoted Event Provider - registers to the help event.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public interface IPromotedEventProvider extends IEventProvider {
	
	/**
	 * Event description
	 */
	public String getDescription();

	/**
	 * Event usage syntax
	 */
	public String getSyntax();
	
}
