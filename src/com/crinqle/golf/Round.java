package com.crinqle.golf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.location.Location;

public class Round {
	private static File roundFile = null;
	private static Map<String, String> round = null;

	private static String course = null;
	private static Set<String> players = null;
	private static int[] pars = null;
	private static Calendar startTime = null;

	private static int holeIndex = 0;
	private static Map<String, int[]> scores = null;
	private static List<List<Location>> marks = null;
	private static Location lastMark = null;

	private static Set<String> sortedPlayers = null;

	static final void addPlayer(String player) {
		players.add(player);
	}

	static final void clear() {
		round = new HashMap<String, String>();
		save();
		round = null;

		course = null;
		players = new LinkedHashSet<String>();
		pars = null;
		startTime = null;

		holeIndex = 0;
		scores = null;
		marks = null;
		sortedPlayers = null;
	}

	static final String getCourse() {
		return course;
	}

	static String getCourseDiffAsString(final int diff) {
		if (0 == diff) {
			return "Even";
		} else if (0 < diff) {
			return "+" + String.valueOf(diff);
		} else {
			return String.valueOf(diff);
		}
	}

	static final int getCoursePar() {
		int total = 0;
		for (int par : pars) {
			total += par;
		}
		return total;
	}

	static final int getCurrentDiff(final String player) {
		int curscore = 0;
		int curpar = 0;
		final int[] ss = scores.get(player);
		/*
		 * This is less-than-OR-equal-to...
		 */
		for (int i = 0; i <= holeIndex; ++i) {
			curscore += ss[i];
			curpar += pars[i];
		}

		return curscore - curpar;
	}

	static final int getCurrentHoleIndex() {
		return holeIndex;
	}

	static final int getCurrentHolePar() {
		return pars[holeIndex];
	}

	static final int getCurrentScore(final String player) {
		int curscore = 0;
		final int[] ss = scores.get(player);
		/*
		 * This is less-than-OR-equal-to...
		 */
		for (int i = 0; i <= holeIndex; ++i) {
			curscore += ss[i];
		}

		return curscore;
	}

	static final String getDataAsJSON() {
		/*
		 * Turn score data into JSON.
		 */
		int k = 0;
		String json = "{";
		for (final String player : players) {
			final int[] ss = scores.get(player);
			String js = "\"" + player + "\":[" + ss[0];
			for (int i = 1; i < ss.length; ++i) {
				js += ("," + String.valueOf(ss[i]));
			}
			js += "]";

			if (0 != k) {
				json += ",";
			}
			json += js;

			++k;
		}
		json += "}";

		Utils.d("JSON-encoded scores: " + json);

		/*
		 * Write the scores to a file on the SD card.
		 */
		// Utils.saveRound();

		/*
		 * Upload the scores from the file to the server.
		 */

		return json;
	}

	static final int getDiff(final String player) {
		return getPar() - getScore(player);
	}

	static final int getHoleCount() {
		return pars.length;
	}

	static String getHoleDiffAsString(final int diff) {
		if (0 == diff) {
			return "Par";
		} else if (0 < diff) {
			return "+" + String.valueOf(diff);
		} else {
			return String.valueOf(diff);
		}
	}

	static final Location getLastMark() {
		final List<Location> ms = marks.get(holeIndex);
		if (0 == ms.size()) {
			return null;
		}
		final Location last = ms.get(ms.size() - 1);
		return last;
	}

	static final int getPar() {
		return pars[holeIndex];
	}

	static int getPlayerCount() {
		return players.size();
	}

	static final Set<String> getPlayers() {
		return players;
	}

	static final int getScore(final String player) {
		return scores.get(player)[holeIndex];
	}

	static final Set<String> getSortedPlayers() {
		return sortedPlayers;
	}

	static final Calendar getStartTime() {
		return startTime;
	}

	static final String getTrackString() {
		StringBuffer buf = new StringBuffer();
		final List<Location> locs = marks.get(holeIndex);
		if (0 == locs.size()) {
			return null;
		}

		final String teeHash = Utils.md5(GPS.getRawLocString(locs.get(0)));
		buf.append("Tee: " + teeHash);
		for (int i = 1; i < locs.size(); ++i) {
			final float dist = GPS.getDistance(locs.get(i), locs.get(i - 1));
			buf.append(", (" + i + ") " + dist + "m");
		}

		final String trackString = buf.toString();
		Utils.v("Hole " + (holeIndex + 1) + ": " + trackString);
		return trackString;
	}

// static final String getTrackString() {
// StringBuffer buf = new StringBuffer();
// final List<Location> locs = marks.get(holeIndex);
// if (0 == locs.size()) {
// return null;
// }
//
// int k = 0;
// for (Location l : locs) {
// if (0 != k) {
// buf.append("\nShot ");
// buf.append(k);
// buf.append(": ");
// } else {
// buf.append("Tee: ");
// }
// buf.append(GPS.getRawLocString(l));
// ++k;
// }
//
// final String trackString = buf.toString();
// Utils.v("Hole " + (holeIndex + 1) + ": " + trackString);
// return trackString;
// }

	static final boolean isPlaying(String player) {
		if (null == players)
			return false;

		return players.contains(player);
	}

	static final boolean isReadyForNext() {
		for (String player : players) {
			final int score = getScore(player);
			Utils.v("Utils.isReadyForNext - score for " + player + ": " + score);

			if (0 == score) {
				return false;
			}
		}

		return true;
	}

	static final boolean load(final File roundFile) {
		/*
		 * Load file into hash.
		 */
		Round.roundFile = roundFile;

		round = new LinkedHashMap<String, String>();
		if (roundFile.exists()) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(roundFile));
				String line = null;

				while (null != (line = reader.readLine())) {
					line = line.trim();
					if (0 >= line.length())
						continue;
					Utils.v("round file line: " + line);

					try {
						final String[] prefTokens = line.split(":");
						final String key = prefTokens[0].trim();
						final String value = prefTokens[1].trim();

						round.put(key, value);
					} catch (Exception e) {
						Utils.e("Invalid round line: " + line);
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

		/*
		 * Initialize data from hash.
		 */
		course = round.get("course");
		final String playerString = round.get("players");
		final String timeString = round.get("time");
		final String parsString = round.get("pars");

		if (null == course || null == playerString || null == timeString || null == parsString) {
			Utils.v("  No round in progress; clearing round file...");
			clear();
			return false;
		}
		final String[] ps = playerString.split(",");
		final String[] parsTokens = parsString.split(",");
		startTime = Calendar.getInstance();

		Utils.v("Loaded round datum - course: " + course);
		Utils.v("Loaded round datum - pars: " + parsString);
		Utils.v("Loaded round datum - startTime: " + timeString);
		Utils.v("Loaded round datum - players: " + playerString);

		/*
		 * Initialize variables.
		 */
		pars = new int[parsTokens.length];
		for (int i = 0; i < parsTokens.length; ++i) {
			pars[i] = Integer.parseInt(parsTokens[i]);
		}

		startTime.setTimeInMillis(Long.parseLong(timeString));
		players = new LinkedHashSet<String>();
		scores = new LinkedHashMap<String, int[]>();
		marks = new ArrayList<List<Location>>(getHoleCount());
		for (int i = 0; i < getHoleCount(); ++i) {
			marks.add(new LinkedList<Location>());
		}

		/*
		 * Initialize arrays.
		 */
		for (String player : ps) {
			Utils.v("  Prep'ing player score array for " + player + "...");
			players.add(player);
			final int[] sar = new int[getHoleCount()];
			scores.put(player, sar);
		}

		/*
		 * Load scores.
		 */
		for (String player : players) {
			final String scoresString = round.get(player);

			if (null == scoresString) {
				continue;
			}

			Utils.v("  Loading player scores for " + player + "...");

			final String[] scoreStrings = scoresString.split(",");
			final int[] sar = scores.get(player);

			int i = 0;
			for (String scorestr : scoreStrings) {
				Utils.v("    Score for Hole + " + (i + 1) + ": " + scorestr);
				final int score = Integer.parseInt(scorestr);
				sar[i++] = score;
			}
		}

		/*
		 * Load tracks.
		 */
		for (int idx = 0; idx < getHoleCount(); ++idx) {
			final String trackKey = "Track " + (idx + 1);
			if (round.containsKey(trackKey)) {
				final List<Location> locs = marks.get(idx);
				final String trackString = round.get(trackKey);
				final String[] trackTokens = trackString.split(",");
				for (String trackToken : trackTokens) {
					final String[] locTokens = trackToken.split(" ");
					try {
						final double lat = Double.parseDouble(locTokens[0]);
						final double lon = Double.parseDouble(locTokens[1]);
						final double alt = Double.parseDouble(locTokens[2]);

						final Location l = new Location("gps");
						l.setLatitude(lat);
						l.setLongitude(lon);
						l.setAltitude(alt);

						locs.add(l);
					} catch (Exception e) {
						Utils.e("Bad track - " + trackKey + ": " + trackString);
					}
				}
			}
		}

		/*
		 * Determine current hole index.
		 */
		for (int idx = 0; idx < getHoleCount(); ++idx) {
			boolean anyZero = false;
			for (String player : players) {
				if (0 == scores.get(player)[idx]) {
					anyZero = true;
					break;
				}
			}

			if (anyZero) {
				holeIndex = idx;
				break;
			}
		}

		Utils.v("  holeIndex (loaded from round): " + holeIndex);
		Utils.v("Loading round data finished.");

		return true;
	}

	static final String markShot() {
		lastMark = GPS.getLocation();
		marks.get(getCurrentHoleIndex()).add(lastMark);
		return GPS.getRawLocString();
	}

	static final void next() {
		save();
		++holeIndex;

		/*
		 * Clamp to end value.
		 */
		if (getHoleCount() <= holeIndex) {
			holeIndex = (getHoleCount() - 1);
		}
	}

	static final void prev() {
		save();
		--holeIndex;

		/*
		 * Clamp to start value.
		 */
		if (0 > holeIndex) {
			holeIndex = 0;
		}
	}

	static final void removePlayer(String player) {
		players.remove(player);
	}

	static final void resetLastMark() {
		lastMark = null;
	}

	static final void save() {
		savePlayerScores();
		saveTracking();

		final StringBuffer buf = new StringBuffer();
		if (null != round) {
			for (String key : round.keySet()) {
				final String value = round.get(key);
				buf.append(key);
				buf.append(":");
				buf.append(value);
				buf.append("\n");
			}
		}
		final String datastr = buf.toString();

		Utils.v("Round.save - datastr: " + datastr);

		Utils.createFile(roundFile, datastr);
	}

	private static final void savePlayerScores() {
		if (null == players)
			return;

		for (String player : players) {
			final String scoreString = Utils.tocsv(scores.get(player));
			Utils.v("Round.savePlayerScores - scoreString: " + scoreString);
			round.put(player, scoreString);
		}
	}

	public static final void saveRound() {
		/*
		 * Save data for the last time.
		 */
		save();

		/*
		 * Create filename from START_TIME.
		 */
		final int year = startTime.get(Calendar.YEAR);
		final int month = 1 + startTime.get(Calendar.MONTH);
		final int dom = startTime.get(Calendar.DAY_OF_MONTH);
		final int hour = startTime.get(Calendar.HOUR_OF_DAY);
		final int min = startTime.get(Calendar.MINUTE);

		final String yearstr = String.valueOf(year);
		final String monstr = (10 > month) ? ("0" + month) : String.valueOf(month);
		final String domstr = (10 > dom) ? ("0" + dom) : String.valueOf(dom);
		final String hourstr = (10 > hour) ? ("0" + hour) : String.valueOf(hour);
		final String minstr = (10 > min) ? ("0" + min) : String.valueOf(min);

		final String timestamp = yearstr + "-" + monstr + domstr + "-" + hourstr + minstr;

		final StringBuffer fnstr = new StringBuffer();
		fnstr.append(timestamp);
		fnstr.append('_');
		fnstr.append(course);
		final String filename = fnstr.toString();
		final File targetFile = new File(Utils.getExtDir(), filename);

		roundFile.renameTo(targetFile);
		Utils.toastLong("Scores saved to " + targetFile.getAbsolutePath());

		clear();
		save();
	}

	private static final void saveTracking() {
		if (null == marks)
			return;

		/*
		 * Generate lines of data. Inject each line into
		 * prefs.
		 */
		Utils.v("About to store hole tracking in round...");

		int hn = 1;
		for (List<Location> hole : marks) {
			final StringBuffer buf = new StringBuffer();

			int tn = 0;
			for (Location mark : hole) {
				double lat = mark.getLatitude();
				double lon = mark.getLongitude();
				double alt = mark.getAltitude();

				if (0 != tn) {
					buf.append(",");
				}

				buf.append(lat);
				buf.append(" ");
				buf.append(lon);
				buf.append(" ");
				buf.append(alt);

				++tn;
			}

			if (0 < hole.size()) {
				final String track = buf.toString();
				final String key = "Track " + String.valueOf(hn);
				Utils.v("  " + key + ":" + track);
				round.put(key, track);
			}

			++hn;
		}
	}

	static final void setScore(String player, int score) {
		final int[] s = scores.get(player);
		s[holeIndex] = score;
	}

	static final void sortPlayers() {
		/*
		 * Persist existing score data.
		 */
		Round.save();

		/*
		 * Create new, sorted, set of player names.
		 */
		sortedPlayers = new LinkedHashSet<String>();

		/*
		 * Perform AwkwardSort(TM).
		 */
		final Set<String> keys = scores.keySet();
		for (String player : players) {
			/*
			 * 'target' is being reset to match player with
			 * lowest score of this scanning pass...This
			 * cannot be simplified into either the 'player'
			 * or 'p' vars.
			 */
			String target = player;
			int low = getCurrentDiff(player);

			for (String p : keys) {
				if (sortedPlayers.contains(p)) {
					Utils.d("    Skipping inner player " + p + " (already in list).");
					continue;
				}

				if (target.equals(p)) {
					Utils.d("    Skipping same player " + p + ".");
					continue;
				}

				final int d = getCurrentDiff(p);
				Utils.d("  Comparing outer to inner: " + player + " (" + low + ") vs " + p + " (" + d + ")...");
				if (d < low) {
					low = d;
					target = p;
					Utils.d("        Switching to " + target + " (" + low + ")");
				}
			}

			Utils.d("=====> Adding " + target + " (score-as-diff " + low + ")");
			sortedPlayers.add(target);
		}
	}

	static final void start(final String course, final File roundFile) {
		round = new LinkedHashMap<String, String>();

		Round.course = course;
		pars = Utils.getCoursePars(course);
		startTime = Calendar.getInstance();

		/*
		 * Save the data here. If we had a fuck-up before
		 * this point, who cares? It's pretty easy to
		 * recreate.
		 * 
		 * Add new players to the all-players list.
		 * Preferences should contain the course name &
		 * time.
		 */
		round.put("course", course);
		round.put("pars", Utils.tocsv(pars));
		round.put("time", String.valueOf(startTime.getTimeInMillis()));
		round.put("players", Utils.tocsv(players));

		scores = new LinkedHashMap<String, int[]>();
		marks = new ArrayList<List<Location>>(getHoleCount());
		for (int i = 0; i < getHoleCount(); ++i) {
			marks.add(new LinkedList<Location>());
		}

		for (String player : players) {
			Utils.v("  Initializing score-array for " + player + "...");

			final int[] s = new int[getHoleCount()];
			for (int i = 0; i < getHoleCount(); ++i) {
				s[i] = 0;
			}
			scores.put(player, s);
		}

		/*
		 * Persist the course/player data.
		 */
		Round.roundFile = roundFile;
		save();
	}
}
