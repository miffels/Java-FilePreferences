package net.infotrek.util.prefs;

import java.io.File;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

/**
 * PreferencesFactory implementation that stores the preferences in a
 * user-defined file. To use it, set the system property
 * <tt>java.util.prefs.PreferencesFactory</tt> to
 * <tt>net.infotrek.util.prefs.FilePreferencesFactory</tt>
 * <p/>
 * The file defaults to [user.home]/.fileprefs, but may be overridden with the
 * system property <tt>net.infotrek.util.prefs.FilePreferencesFactory.file</tt>
 * 
 * @author David Croft (<a href="http://www.davidc.net">www.davidc.net</a>)
 * @author Michael Jess (<a href="https://github.com/miffels">Michael@github</a>)
 */
public class FilePreferencesFactory implements PreferencesFactory {
	public static final String SYSTEM_PROPERTY_FILE = "net.infotrek.util.prefs.FilePreferencesFactory.file";

	Preferences rootPreferences;

	public Preferences systemRoot() {
		return userRoot();
	}

	public Preferences userRoot() {
		if (rootPreferences == null) {
			rootPreferences = new FilePreferences(null, "");
		}
		return rootPreferences;
	}

	private static volatile File preferencesFile;
	private static final Object lock = new Object();

	public static File getPreferencesFile() {
		if (preferencesFile == null) {
			synchronized(lock) {
				if(preferencesFile == null) {
					String prefsFile = System.getProperty(SYSTEM_PROPERTY_FILE);
					if (prefsFile == null || prefsFile.length() == 0) {
						prefsFile = System.getProperty("user.home") + File.separator
								+ ".fileprefs";
					}
					preferencesFile = new File(prefsFile).getAbsoluteFile();
				}
			}
		}
		return preferencesFile;
	}

}