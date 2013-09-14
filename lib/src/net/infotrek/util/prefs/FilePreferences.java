package net.infotrek.util.prefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * Preferences implementation that stores to a user-defined file. See
 * FilePreferencesFactory.
 * 
 * @author David Croft (<a href="http://www.davidc.net">www.davidc.net</a>)
 * @author Michael Jess (<a href="https://github.com/miffels">Michael@github</a>)
 */
public class FilePreferences extends AbstractPreferences {
	private Map<String, String> root;
	private Map<String, FilePreferences> children;
	private boolean isRemoved = false;

	public FilePreferences(AbstractPreferences parent, String name) {
		super(parent, name);

		root = new TreeMap<String, String>();
		children = new TreeMap<String, FilePreferences>();

		try {
			sync();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	protected void putSpi(String key, String value) {
		root.put(key, value);
		try {
			flush();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	protected String getSpi(String key) {
		return root.get(key);
	}

	protected void removeSpi(String key) {
		root.remove(key);
		try {
			flush();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	protected void removeNodeSpi() throws BackingStoreException {
		isRemoved = true;
		flush();
	}

	protected String[] keysSpi() throws BackingStoreException {
		return root.keySet().toArray(new String[root.keySet().size()]);
	}

	protected String[] childrenNamesSpi() throws BackingStoreException {
		return children.keySet().toArray(new String[children.keySet().size()]);
	}

	protected FilePreferences childSpi(String name) {
		FilePreferences child = children.get(name);
		if (child == null || child.isRemoved()) {
			child = new FilePreferences(this, name);
			children.put(name, child);
		}
		return child;
	}

	protected void syncSpi() throws BackingStoreException {
		if (isRemoved())
			return;

		final File file = FilePreferencesFactory.getPreferencesFile();

		if (!file.exists())
			return;

		synchronized (file) {
			Properties p = new Properties();
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				p.load(fis);
				fis.close();

				StringBuilder sb = new StringBuilder();
				getPath(sb);
				String path = sb.toString();

				final Enumeration<?> pnen = p.propertyNames();
				while (pnen.hasMoreElements()) {
					String propKey = (String) pnen.nextElement();
					if (propKey.startsWith(path)) {
						String subKey = propKey.substring(path.length());
						// Only load immediate descendants
						if (subKey.indexOf('.') == -1) {
							root.put(subKey, p.getProperty(propKey));
						}
					}
				}
			} catch (IOException e) {
				throw new BackingStoreException(e);
			} finally {
				try {
					if(fis != null) {
						fis.close();
					}
				} catch (IOException e) {
					throw new BackingStoreException(e);
				}
			}
		}
	}

	private void getPath(StringBuilder sb) {
		final FilePreferences parent = (FilePreferences) parent();
		if (parent == null)
			return;

		parent.getPath(sb);
		sb.append(name()).append('.');
	}

	protected void flushSpi() throws BackingStoreException {
		final File file = FilePreferencesFactory.getPreferencesFile();

		synchronized (file) {
			Properties p = new Properties();
			FileInputStream fis = null;
			FileOutputStream fos = null;
			try {
				StringBuilder sb = new StringBuilder();
				getPath(sb);
				String path = sb.toString();

				if (file.exists()) {
					fis = new FileInputStream(file);
					p.load(fis);

					List<String> toRemove = new ArrayList<String>();

					// Make a list of all direct children of this node to be
					// removed
					final Enumeration<?> pnen = p.propertyNames();
					while (pnen.hasMoreElements()) {
						String propKey = (String) pnen.nextElement();
						if (propKey.startsWith(path)) {
							String subKey = propKey.substring(path.length());
							// Only do immediate descendants
							if (subKey.indexOf('.') == -1) {
								toRemove.add(propKey);
							}
						}
					}

					// Remove them now that the enumeration is done with
					for (String propKey : toRemove) {
						p.remove(propKey);
					}
				}

				// If this node hasn't been removed, add back in any values
				if (!isRemoved) {
					for (String s : root.keySet()) {
						p.setProperty(path + s, root.get(s));
					}
				}

				fos = new FileOutputStream(file);
				p.store(fos, "FilePreferences");
			} catch (IOException e) {
				throw new BackingStoreException(e);
			} finally {
				if(fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						throw new BackingStoreException(e);
					}
				}
				if(fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						throw new BackingStoreException(e);
					}
				}
			}
		}
	}
}