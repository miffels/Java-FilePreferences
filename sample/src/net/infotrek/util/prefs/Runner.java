package net.infotrek.util.prefs;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Runner {

	public static void main(String[] args) throws BackingStoreException {
		System.setProperty("java.util.prefs.PreferencesFactory",
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, "myprefs.txt");

		// Use any of your classes here, e.g. your "main" class
		Preferences p = Preferences.userNodeForPackage(Runner.class);

		for (String s : p.keys()) {
			System.out.println("p[" + s + "]=" + p.get(s, null));
		}

		p.putBoolean("hi", true);
		p.put("Number", String.valueOf(System.currentTimeMillis()));
	}

}
