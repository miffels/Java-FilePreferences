package net.infotrek.util.prefs;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Runner {

	public static void main(String[] args) throws BackingStoreException {
		setSystemPreferencesFactory();
		setPreferencesFilePath();

		// Use any of your classes here, e.g. your "main" class
		Preferences preferences = getPreferencesOf(Runner.class);

		echoPreviousPreferences(preferences);
		putSamplePreferences(preferences);
	}
	
	private static void setSystemPreferencesFactory() {
		System.setProperty("java.util.prefs.PreferencesFactory",
				FilePreferencesFactory.class.getName());
	}
	
	private static void setPreferencesFilePath() {
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, "myprefs.txt");
	}
	
	private static Preferences getPreferencesOf(Class<?>  clazz) {
		return Preferences.userNodeForPackage(clazz);
	}
	
	private static void echoPreviousPreferences(Preferences preferences) throws BackingStoreException {
		for (String s : preferences.keys()) {
			System.out.println("preferences[" + s + "]=" + preferences.get(s, null));
		}
	}
	
	private static void putSamplePreferences(Preferences p) {
		p.putBoolean("hi", true);
		p.put("Number", String.valueOf(System.currentTimeMillis()));
	}

}
