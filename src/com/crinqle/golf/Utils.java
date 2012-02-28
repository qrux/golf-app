package com.crinqle.golf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

class Utils {
	public static final boolean DEBUG = true;

	static final String LOG_TAG = "crin";
	static final int SCORE_DIALOG = 1;
	static final int NEW_PLAYER_DIALOG = 2;
	static final String PREF_MAX_SCORE = "MAX_SCORE";

	static LinearLayout.LayoutParams CONTAINER_PARAMS = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.0F);
	static LinearLayout.LayoutParams WIDGET_PARAMS = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 1.0F);
	static LinearLayout.LayoutParams SCROLLER_PARAMS = WIDGET_PARAMS;

	private static MessageDigest digest = null;

	private static Resources res = null;
	private static File extDir = null;
	private static File prefsFile = null;
	private static String roundFileName = null;
	private static Map<String, String> prefs = new LinkedHashMap<String, String>();

	private static Toast curToast = null;
	private static Context curContext = null;

	private static Map<String, int[]> courses = new LinkedHashMap<String, int[]>();
	private static Set<String> allPlayers = new LinkedHashSet<String>();

	static void addAllPlayer(String player) {
		allPlayers.add(player);
		saveAllPlayers();
	}

	static final void clearPrefs() {
	}

	private static final File createFile(String filename, String data) {
		File file = new File(extDir, filename);
		return createFile(file, data);
	}

	static final File createFile(File file, String data) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
			os.write(data.getBytes());
			os.flush();
			os.close();
			return file;
		} catch (Exception e) {
			try {
				if (null != os) {
					os.close();
				}
			} catch (Exception e2) {
			}
			toast("Cannot write the data to file " + file.getAbsolutePath() + ": " + e.getMessage());
		}

		return null;
	}

	static final void d(String mesg) {
		Log.d(LOG_TAG, mesg);
	}

	static final void e(String mesg) {
		Log.e(LOG_TAG, mesg);
	}

	static final Set<String> getAllPlayers() {
		return allPlayers;
	}

	static final int[] getCoursePars(final String course) {
		return courses.get(course);
	}

	static final Set<String> getCourses() {
		return courses.keySet();
	}

	static final File getExtDir() {
		return extDir;
	}

	static final int getPrefAsInt(String pref) {
		return Integer.parseInt(prefs.get(pref));
	}

	static final File getRoundFile() {
		return new File(extDir, roundFileName);
	}

	static final String getStringResource(final int id) {
		return res.getString(id);
	}

	static final void i(String mesg) {
		Log.i(LOG_TAG, mesg);
	}

	/**
	 * **************************************************
	 * This method starts the Utils class humming.
	 * 
	 * @param context
	 *            Current context for things like toasts.
	 * @param res
	 *            Resources file for app.
	 */
	static final void init(Context context, Resources res) {
		if (null == digest) {
			try {
				digest = java.security.MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				digest = null;
			}
		}

		curContext = context;
		Utils.res = res;

		/*
		 * Initialize resource file.
		 */
		final String extDirName = res.getString(R.string.ext_dir);
		roundFileName = res.getString(R.string.round_file);

		/*
		 * Initialize com.crinqle.golf data directory.
		 */
		final String absPath = Environment.getExternalStorageDirectory() + File.separator + extDirName;
		extDir = new File(absPath);
		if (false == extDir.exists()) {
			extDir.mkdir();
		}

		if (false == extDir.isDirectory()) {
			e(absPath + " exists, but is not a directory--cannot store files");
			extDir = null;
		}

		/*
		 * Load preferences into application state.
		 * 
		 * If there were preferences to load, return (true);
		 * otherwise, return (false).
		 */
		// return Round.loadData();
	}

	static final void load() {
		if (null == digest) {
			try {
				digest = java.security.MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				digest = null;
			}
		}

		final String prefsFileName = res.getString(R.string.prefs_file);
		final String playerFileName = res.getString(R.string.player_file);
		final String courseFileName = res.getString(R.string.course_file);

		/*
		 * Load player data.
		 */
		final File playerFile = new File(extDir, playerFileName);
		int loadedPlayerCount = 0;
		boolean isOkay = true;
		BufferedReader reader = null;

		if (playerFile.exists()) {
			try {
				reader = new BufferedReader(new FileReader(playerFile));
				String line = null;

				while (null != (line = reader.readLine())) {
					line = line.trim();
					if (0 >= line.length())
						continue;
					v("player file line: " + line);

					try {
						final String player = line;
						allPlayers.add(player);
						++loadedPlayerCount;
					} catch (Exception e) {
						e("Invalid player data: " + line);
						isOkay = false;
					}
				}
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
				isOkay = false;
				try {
					reader.close();
				} catch (Exception e2) {
				}
			}
			if (isOkay) {
				i("All players loaded successfully (" + loadedPlayerCount + ").");
			} else {
				i("Loaded " + loadedPlayerCount + " players.");
			}
		} else {
			toast("No player file found.");
		}

		/*
		 * Load courses.
		 */
		final File courseFile = new File(extDir, courseFileName);
		int loadedCourseCount = 0;
		isOkay = true;

		if (courseFile.exists()) {
			try {
				reader = new BufferedReader(new FileReader(courseFile));
				String line = null;

				while (null != (line = reader.readLine())) {
					line = line.trim();
					if (0 >= line.length())
						continue;
					v("course file line: " + line);

					try {
						parseCourseData(line);
						++loadedCourseCount;
					} catch (Exception e) {
						e("Invalid course data: " + line);
						isOkay = false;
					}
				}
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
				isOkay = false;
				try {
					reader.close();
				} catch (Exception e2) {
				}
			}
			if (isOkay) {
				i("All courses loaded successfully (" + loadedCourseCount + ").");
			} else {
				i("Loaded " + loadedCourseCount + " courses.");
			}
		} else {
			toast("No course file found.");
		}

		/*
		 * Initialize preferences.
		 */
		prefsFile = new File(extDir, prefsFileName);
		if (prefsFile.exists()) {
			try {
				reader = new BufferedReader(new FileReader(prefsFile));
				String line = null;

				while (null != (line = reader.readLine())) {
					line = line.trim();
					if (0 >= line.length())
						continue;
					v("prefs file line: " + line);

					try {
						final String[] prefTokens = line.split(":");
						final String key = prefTokens[0].trim();
						final String value = prefTokens[1].trim();

						prefs.put(key, value);
					} catch (Exception e) {
						e("Invalid prefs line: " + line);
					}
				}

				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
				try {
					reader.close();
				} catch (Exception e2) {
				}
			}
		}
	}

	public static String md5(String s) {
		if (null == digest)
			return "";

		// Create MD5 Hash
		digest.reset();
		digest.update(s.getBytes());
		byte messageDigest[] = digest.digest();

		// Create Hex String
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < messageDigest.length; i++)
			hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
		return hexString.toString();
	}

	/**
	 * Parse basic course data.
	 */
	private static void parseCourseData(final String line) throws Exception {
		final String[] nameTokens = line.split(":");
		final String courseName = nameTokens[0].trim();
		final int holeCount = Integer.parseInt(nameTokens[1].trim());
		final String coursePars = nameTokens[2].trim();

		v("  course name: " + courseName);
		v("  course hole counts: " + holeCount);
		v("  course pars: " + coursePars);

		final String[] parStrings = coursePars.split(",");
		if (holeCount != parStrings.length) {
			e("The specified hole count (" + holeCount + ") doesn't match the number of pars listed: (" + parStrings.length + "); aborting");
			toast("The specified hole count (" + holeCount + ") doesn't match the number of pars listed: (" + parStrings.length + "); aborting");
			throw new Exception("Bad hole count");
		}

		final int[] pars = new int[holeCount];
		for (int i = 0; i < holeCount; ++i) {
			pars[i] = Integer.parseInt(parStrings[i]);
		}

		courses.put(courseName, pars);
	}

	static final void save() {
		saveAllPlayers();
		/*
		 * TODO - save course-data and pref-data
		 */
	}

	static void saveAllPlayers() {
		String playerData = tocsv(allPlayers);
		playerData = playerData.replace(',', '\n');
		v("All players (to save): " + playerData);

		final String playerFileName = res.getString(R.string.player_file);
		createFile(playerFileName, playerData);
	}

	static final void setCurrentContext(Context c) {
		curContext = c;
	}

	static final void toast(String mesg) {
		if (null != curToast) {
			curToast.cancel();
		}

		curToast = Toast.makeText(curContext, mesg, Toast.LENGTH_SHORT);
		curToast.show();
	}

	static final void toastLong(String mesg) {
		if (null != curToast) {
			curToast.cancel();
		}

		curToast = Toast.makeText(curContext, mesg, Toast.LENGTH_LONG);
		curToast.show();
	}

	static String tocsv(int[] arr) {
		final StringBuffer buf = new StringBuffer();
		final int count = arr.length;
		if (count > 0) {
			buf.append(arr[0]);
		}
		for (int i = 1; i < count; ++i) {
			buf.append(",");
			buf.append(arr[i]);
		}

		return buf.toString();
	}

	static String tocsv(String[] arr) {
		final StringBuffer buf = new StringBuffer();
		final int count = arr.length;
		if (count > 0) {
			buf.append(arr[0]);
		}
		for (int i = 1; i < count; ++i) {
			buf.append(",");
			buf.append(arr[i]);
		}

		return buf.toString();
	}

	static String tocsv(Iterable<String> set) {
		Iterator<String> iter = set.iterator();
		StringBuffer buf = new StringBuffer();
		if (iter.hasNext()) {
			buf.append(iter.next());
		}
		while (iter.hasNext()) {
			buf.append(",");
			buf.append(iter.next());
		}

		return buf.toString();
	}

	static final void v(String mesg) {
		if (DEBUG) {
			Log.v(LOG_TAG, mesg);
		}
	}
}
