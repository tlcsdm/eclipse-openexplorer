package com.tlcsdm.eclipse.openexplorer.util;

import org.eclipse.jface.preference.IPreferenceStore;

import com.tlcsdm.eclipse.openexplorer.Activator;
import com.tlcsdm.eclipse.openexplorer.preferences.IPreferenceConstants;

/**
 * OperatingSystem singleton
 * 
 * @author <a href="mailto:samson959@gmail.com">Samson Wu</a>
 * @version 1.5.0
 */
public enum OperatingSystem {
	INSTANCE;

	public static final String WINDOWS = "win32";
	public static final String LINUX = "linux";
	public static final String MACOSX = "macosx";

	private String os;

	private OperatingSystem() {
		os = System.getProperty("osgi.os");
	}

	/**
	 * @return the systemBrowser
	 */
	public String getSystemBrowser() {
		String systemBrowser = null;
		if (isWindows()) {
			systemBrowser = IFileManagerExecutables.EXPLORER;
		} else if (isLinux()) {
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			systemBrowser = store.getString(IPreferenceConstants.LINUX_FILE_MANAGER);
			if (systemBrowser.equals(IFileManagerExecutables.OTHER)) {
				systemBrowser = store.getString(IPreferenceConstants.LINUX_FILE_MANAGER_PATH);
			}
		} else if (isMacOSX()) {
			systemBrowser = IFileManagerExecutables.FINDER;
		}
		return systemBrowser;
	}

	public String getOS() {
		return os;
	}

	public boolean isWindows() {
		return WINDOWS.equalsIgnoreCase(os);
	}

	public boolean isMacOSX() {
		return MACOSX.equalsIgnoreCase(this.os);
	}

	public boolean isLinux() {
		return LINUX.equalsIgnoreCase(os);
	}
}
