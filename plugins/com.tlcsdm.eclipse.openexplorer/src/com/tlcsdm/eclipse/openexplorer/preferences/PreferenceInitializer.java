package com.tlcsdm.eclipse.openexplorer.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.tlcsdm.eclipse.openexplorer.Activator;
import com.tlcsdm.eclipse.openexplorer.util.IFileManagerExecutables;
import com.tlcsdm.eclipse.openexplorer.util.OperatingSystem;
import com.tlcsdm.eclipse.openexplorer.util.Utils;

/**
 * @author <a href="mailto:samson959@gmail.com">Samson Wu</a>
 * @version 1.4.0
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(IPreferenceConstants.LINUX_FILE_MANAGER,
				OperatingSystem.INSTANCE.isLinux() ? Utils.detectLinuxSystemBrowser()
						: IFileManagerExecutables.NAUTILUS);
		store.setDefault(IPreferenceConstants.LINUX_FILE_MANAGER_PATH, "");
	}

}
