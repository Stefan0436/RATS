package org.asf.rats.http.providers;

import org.asf.rats.HttpRequest;

/**
 * 
 * File restriction provider - to secure file storage where needed.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
public interface IFileRestrictionProvider {

	/**
	 * Checks file access, false disconnects the client with a 403, true allows the
	 * request.<br />
	 * <br />
	 * <b>WARNING:</b> Windows paths are case-insensitive, make sure you keep that
	 * in mind when writing a restriction provider!
	 * 
	 * @param file File (or directory) to check
	 * @return True if access is granted, false otherwise.
	 */
	public boolean checkRestriction(String file, HttpRequest request);

}
