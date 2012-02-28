package com.crinqle.golf;

import android.location.Location;

public class GPS {
	public static final int DEFAULT_MAP_ZOOM_LEVEL = 23;
	public static final long DEFAULT_GPS_UPDATE_INTERVAL_MILLIS = 5000L;
	public static final float DEFAULT_GPS_UPDATE_THRESHOLD_METERS = 0.0f;

	private static Location loc = null;
	private static final float[] distanceParameters = new float[3];

	static final Location getLocation() {
		return loc;
	}

	static final void setLocation(Location l) {
		loc = l;
		// Utils.v(getRawLocString() + " --> " + getLocString());
	}

	/**
	 * Get the distance between two points on the geoid.
	 * 
	 * @param start
	 *            Point A
	 * @param end
	 *            Point B
	 * @return Distance in meters, with centimeter resolution.
	 */
	static final float getDistance(Location start, Location end) {
		if (null == start || null == end) {
			return -1.0f;
		}

		final double startLat = start.getLatitude();
		final double startLon = start.getLongitude();
		final double endLat = end.getLatitude();
		final double endLon = end.getLongitude();

		Location.distanceBetween(startLat, startLon, endLat, endLon, distanceParameters);
		float dist = Math.round(distanceParameters[0] * 100) / 100.0f;
		return dist;
	}

	static final String getGeoURI() {
		return "geo:" + getLat() + "," + getLon() + "?z=" + DEFAULT_MAP_ZOOM_LEVEL;
	}

	private static final String getLatAsDegString() {
		final String dir;
		if (0 > getLat()) {
			dir = "S";
		} else {
			dir = "N";
		}

		return (getCoordsAsDegString(getLat()) + " " + dir);
	}

	private static final String getLonAsDegString() {
		final String dir;
		if (0 > getLon()) {
			dir = "W";
		} else {
			dir = "E";
		}

		return (getCoordsAsDegString(getLon()) + " " + dir);
	}

	private static final String getCoordsAsDegString(double coord) {
		double l = Math.abs(coord);
		final int deg = (int) Math.floor(l);
		l -= deg;
		double md = 60 * l;
		final int min = (int) Math.floor(md);
		md -= min;
		double ms = 60 * md;
		final int sec = (int) Math.round(ms);

		return (deg + "¡" + min + "'" + sec + "\"");
	}

	static final String getLocString() {
		return (getLatAsDegString() + " ... " + getLonAsDegString() + " ... " + (0.0d < getAlt() ? "+" : "") + getAlt() + " m");
	}

	static final String getRawLocString() {
		return getLat() + ", " + getLon() + " @ " + (0.0d < getAlt() ? "+" : "") + getAlt() + " m";
	}

	static final String getRawLocStringWithShortAlt() {
		double alt = getAlt() * 10;
		final int shortAlt = (int) Math.round(alt);
		alt = shortAlt / 10.0;

		return getLat() + ", " + getLon() + " @ " + (0.0d < alt ? "+" : "") + alt + " m";
	}

	static final String getRawLocString(final Location l) {
		return l.getLatitude() + ", " + l.getLongitude() + " @ " + (0.0d < l.getAltitude() ? "+" : "") + l.getAltitude() + " m";
	}

	static final double getLat() {
		if (null == loc) {
			return 0d;
		}

		return loc.getLatitude();
	}

	static final float getLatAsFloat() {
		return (float) getLat();
	}

	static final double getLon() {
		if (null == loc) {
			return 0d;
		}

		return loc.getLongitude();
	}

	static final float getLonAsFloat() {
		return (float) getLon();
	}

	/**
	 * Gets the altitude of the current location.
	 * 
	 * @return Altitude in meters, with centimeter resolution.
	 */
	static final double getAlt() {
		if (null == loc) {
			return 0.0d;
		}

		final double alt = Math.round(loc.getAltitude() * 100) / 100.0d;
		return alt;
	}

}
