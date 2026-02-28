package com.tlcsdm.eclipse.openexplorer.util;

/**
 * Copyright (c) 2011 Samson Wu
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;

/**
 * @author <a href="mailto:samson959@gmail.com">Samson Wu</a>
 * @version 1.5.0
 */
public class Utils {

	private static final ILog LOG = Platform.getLog(Utils.class);

	private Utils() {
	}

	/**
	 * Use {@code which} command to found the modern file manager, if not found use
	 * the xdg-open.
	 * 
	 * @return the file manager
	 */
	public static String detectLinuxSystemBrowser() {
		String[] fileManagers = { "nautilus", "dolphin", "thunar", "pcmanfm", "rox", "xdg-open" };
		for (String fm : fileManagers) {
			String result = executeCommand("which " + fm);
			if (result != null && !result.isEmpty()) {
				String[] pathnames = result.split(File.separator);
				return pathnames[pathnames.length - 1];
			}
		}
		return IFileManagerExecutables.OTHER;
	}

	/**
	 * Execute the command and return the result.
	 * 
	 * @param command the command to execute
	 * @return the trimmed stdout output, or null if the command failed
	 */
	public static String executeCommand(String command) {
		try {
			ProcessBuilder builder = new ProcessBuilder(command.split(" "));
			Process process = builder.start();
			String stdout = new String(process.getInputStream().readAllBytes()).trim();
			return stdout.isEmpty() ? null : stdout;
		} catch (IOException e) {
			LOG.warn("Failed to execute command: " + command, e);
			return null;
		}
	}

}
