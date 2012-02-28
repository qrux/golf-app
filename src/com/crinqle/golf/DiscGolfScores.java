/**
 * 
 */
package com.crinqle.golf;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * @author troy
 * 
 */
public class DiscGolfScores extends Activity {
	private LinearLayout root;
	private String selectedPlayer = null;
	private Button selectedScore = null;

	private Map<String, Button> playerButtons = new HashMap<String, Button>();
	private Map<String, TextView> playerFields = new HashMap<String, TextView>();
	private TextView parField;
	private TextView holeField;
	private Button[] scoreButtons = new Button[15];
	private LinearLayout navbar = null;

	private LocationManager lm = null;
	private LocationListener locListener = null;

	private Button markButton;
	private Button nextButton;
	private TextView locField;
	private TextView trackingField;

	/**
	 * **************************************************
	 * This method controls the state of the UI moving from
	 * hole to hole. All the hole-states should be processed
	 * here, and used to update the UI.
	 * 
	 * I'm not sure how this interacts with the Activity
	 * state yet; specifically, I don't know how when this
	 * is triggered w.r.t. the Act state changing.
	 * **************************************************
	 */
	private void updateUI() {
		parField.setText(String.valueOf(Round.getCurrentHolePar()));
		holeField.setText(String.valueOf(Round.getCurrentHoleIndex() + 1));

		/*
		 * Reset shot-counter & shot-GPS-tracking.
		 */
		String trackString = Round.getTrackString();
		if (null == trackString) {
			trackingField.setText("Tee unmarked...");
		} else {
			trackingField.setText(trackString);
		}

		if (null != nextButton) {
			nextButton.setEnabled(Round.isReadyForNext());
		}

		/*
		 * Update scores for players.
		 */
		final Set<String> players = playerButtons.keySet();
		for (String player : players) {
			updateScore(player);
		}

		updateNav();
	}

	private void updateScore(String player) {
		final int holeScore = Round.getScore(player);
		final int holeDiff = Round.getDiff(player);
		final String holeDiffString = Round.getHoleDiffAsString(holeDiff);
		final int currentDiff = Round.getCurrentDiff(player);
		final String currentDiffString = Round.getCourseDiffAsString(currentDiff);

		Utils.d("CrinqleScores.updateScore(" + player + ") - holeScore: " + holeScore);
		Utils.d("CrinqleScores.updateScore(" + player + ") - holeDiff: " + holeDiff);
		Utils.d("CrinqleScores.updateScore(" + player + ") - holeDiffString: " + holeDiffString);
		Utils.d("CrinqleScores.updateScore(" + player + ") - currentDiff: " + currentDiff);
		Utils.d("CrinqleScores.updateScore(" + player + ") - currentDiffString: " + currentDiffString);

		final TextView playerField = (TextView) playerFields.get(player);
		final String holeScoreString = (0 == holeScore) ? "(?)" : String.valueOf(holeScore);
		playerField.setText(holeScoreString);

		if (0 == holeScore) {
			return;
		}

		final Button playerButton = (Button) playerButtons.get(player);
		playerButton.setText(player + " (" + currentDiffString + ")");
	}

	private void updateNav() {
		if (null != navbar) {
			navbar.removeAllViews();
		} else {
			navbar = new LinearLayout(this);
			navbar.setOrientation(LinearLayout.HORIZONTAL);
			// navbar.setBackgroundColor(Color.DKGRAY);
			navbar.setLayoutParams(Utils.CONTAINER_PARAMS);
			root.addView(navbar);
		}

		final int holeCount = Round.getHoleCount();
		final int currentHole = Round.getCurrentHoleIndex() + 1;

		/*
		 * Create the "Prev" button.
		 */
		Button prevButton = new Button(this);
		prevButton.setText("Previous (Hole " + (currentHole - 1) + ")");
		prevButton.setLayoutParams(Utils.WIDGET_PARAMS);

		prevButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				/*
				 * Reset the text in the buttons.
				 */
				prev();
			}
		});

		/*
		 * Create the "Next" button.
		 */
		nextButton = new Button(this);
		nextButton.setText("Next (Hole " + (currentHole + 1) + ")");
		nextButton.setLayoutParams(Utils.WIDGET_PARAMS);
		// nextButton.setEnabled(false);

		nextButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				/*
				 * Reset the text in the buttons.
				 */
				next();
			}
		});

		Utils.d("updateNav - holeIndex: " + Round.getCurrentHoleIndex());

		if (1 < currentHole && holeCount > currentHole) {
			navbar.addView(prevButton);
			navbar.addView(nextButton);
		} else if (1 == currentHole) {
			navbar.addView(nextButton);
		} else {
			/*
			 * Create the "Done" button.
			 */
			Button done = new Button(this);
			done.setText("Done");
			done.setTextColor(Color.BLACK);
			done.setLayoutParams(Utils.WIDGET_PARAMS);

			done.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					//Round.sortPlayers();

					Intent myIntent = new Intent(view.getContext(), DiscGolfSheet.class);
					startActivityForResult(myIntent, 0);
				}
			});

			navbar.addView(prevButton);
			navbar.addView(done);
		}
	}

	private void prev() {
		Round.prev();
		updateUI();
	}

	private void next() {
		Round.next();
		updateUI();
	}

	@Override
	public void onPrepareDialog(int id, Dialog d) {
		switch (id) {
		case Utils.SCORE_DIALOG:
			d.setTitle(selectedPlayer + " - Hole " + (Round.getCurrentHoleIndex() + 1) + " (Par " + Round.getCurrentHolePar() + ")");
			break;
		}
	}

	@Override
	public Dialog onCreateDialog(int id) {
		switch (id) {
		case Utils.SCORE_DIALOG:
			/*
			 * Create the dialog.
			 */
			final Dialog d = new Dialog(this);
			Window w = d.getWindow();
			w.setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

			/*
			 * Create the dialog view.
			 */
			LinearLayout dv = new LinearLayout(this);
			dv.setOrientation(LinearLayout.VERTICAL);
			dv.setLayoutParams(Utils.CONTAINER_PARAMS);

			/*
			 * Create the score buttons.
			 * TODO - add real prefs.
			 */
			for (int s = 0; s < 9 /* FIXME */; ++s) {
				final int score = s + 1;
				final String scoreString = String.valueOf(score);
				final Button scoreButton = new Button(this);
				scoreButton.setLayoutParams(Utils.WIDGET_PARAMS);
				scoreButton.setText(scoreString);
				// scoreButton.setTextOn(scoreString);
				// scoreButton.setTextOff(scoreString);
				scoreButton.setOnClickListener(new OnClickListener() {
					public void onClick(View arg0) {
						/*
						 * Actually set the score data here.
						 * Then, dismiss dialog.
						 */
						Round.setScore(selectedPlayer, score);
						/*
						 * Adjust the values in the UI
						 * itself.
						 */
						updateScore(selectedPlayer);
						/*
						 * Dismiss dialog.
						 */
						d.dismiss();
						/*
						 * Decide if we're ready to enable
						 * "Next Hole" button.
						 */
						final boolean isReady = Round.isReadyForNext();
						Utils.v("CrinScores:score selected - isReady: " + isReady);
						if (isReady) {
							nextButton.setEnabled(true);
						}

						if (null != selectedScore) {
							Utils.d("  Found selectedRadio: " + selectedScore);
							// selectedScore.setChecked(false);
						}

						selectedScore = scoreButton;
						Utils.d("Setting selectedRadio to " + selectedScore);
					}
				});

				scoreButtons[s] = scoreButton;
			}

			/*
			 * Create the score grid.
			 */
			LinearLayout grid = new LinearLayout(this);
			grid.setOrientation(LinearLayout.VERTICAL);
			grid.setLayoutParams(Utils.CONTAINER_PARAMS);

			/*
			 * Add score buttons to score grid;
			 */
			final int totalRows = /* FIXME - Utils.getPref(Utils.PREF_MAX_SCORE) */9 / 3;
			for (int r = 0; r < totalRows; ++r) {
				LinearLayout row = new LinearLayout(this);
				row.setOrientation(LinearLayout.HORIZONTAL);
				row.setLayoutParams(Utils.CONTAINER_PARAMS);
				for (int c = 0; c < 3; ++c) {
					final int sidx = ((r * 3) + c);
					row.addView(scoreButtons[sidx]);
				}
				grid.addView(row);
			}

			/*
			 * Add grid to dialog view.
			 */
			dv.addView(grid);

			/*
			 * Add cancel button.
			 */
			Button cancel = new Button(this);
			cancel.setText("Cancel");
			cancel.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					/*
					 * Dismiss dialog.
					 */
					d.dismiss();
				}
			});
			dv.addView(cancel);

			/*
			 * Finalize dialog.
			 */
			d.setContentView(dv);
			return d;

		default:
			return null;
		}
	}

	class LocListener implements LocationListener {
		boolean toggle = true;

		public void onLocationChanged(Location loc) {
			toggle = !toggle;
			GPS.setLocation(loc);

			if (null != markButton) {
				markButton.setEnabled(true);
			}
			if (null != locField) {
				/*
				 * Find distance, if it's valid...
				 */
				final Location start = Round.getLastMark();
				final Location end = loc;
				final float dist = GPS.getDistance(start, end);
				/*
				 * ...Then, display the data.
				 */
				locField.setTextColor(toggle ? Color.GREEN : Color.CYAN);
				if (0 > dist) {
					final String rawString = GPS.getRawLocString();
					final String hash = Utils.md5(rawString);
					final int halflen = hash.length() >> 1;
					final String twolines = hash.substring(0, halflen) + "\n" + hash.substring(halflen);

					locField.setText(twolines);
				} else {
					if (1 > dist) {
						locField.setText("~" + (dist * 100) + " cm\n(from last mark)");
					} else {
						locField.setText("~" + dist + " meters\n(from last mark)");
					}
				}
			}
		}

		public void onProviderDisabled(String provider) {
			Utils.i("Location provider '" + provider + "' is disabled.");
			if (null != markButton) {
				markButton.setEnabled(false);
			}
		}

		public void onProviderEnabled(String provider) {
			Utils.i("Location provider '" + provider + "' is enabled.");
			if (null != markButton) {
				markButton.setEnabled(true);
			}
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			final String mesg;
			switch (status) {
			case LocationProvider.AVAILABLE:
				mesg = "AVAILABLE";
				if (null != markButton) {
					markButton.setEnabled(true);
				}
				break;
			case LocationProvider.OUT_OF_SERVICE:
				mesg = "OUT_OF_SERVCE";
				if (null != markButton) {
					markButton.setEnabled(false);
				}
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				mesg = "TEMPORARILY_UNAVAILABLE";
				if (null != markButton) {
					markButton.setEnabled(false);
				}
				break;
			default:
				mesg = "(unknown to API)";
				break;
			}
			Utils.i("Location provider '" + provider + "' STATUS is now " + mesg + " (" + status + ").");
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		final Resources res = getResources();
		Utils.init(this, res);

		/*
		 * Add GPS initializer.
		 */
		if (null == locListener) {
			locListener = new LocListener();
		}

		Utils.d("Initializing LocationManager...");
		if (null == lm) {
			lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
		Utils.d("  Requesting location updates (" + GPS.DEFAULT_GPS_UPDATE_INTERVAL_MILLIS + " millis, " + GPS.DEFAULT_GPS_UPDATE_THRESHOLD_METERS + " metres)...");

		Utils.toastLong("Initializing GPS subsystem...");
		lm.requestLocationUpdates("gps", GPS.DEFAULT_GPS_UPDATE_INTERVAL_MILLIS, GPS.DEFAULT_GPS_UPDATE_THRESHOLD_METERS, locListener);
	}

	private void stopGPS() {
		if (null != lm && null != locListener) {
			Utils.toastLong("Suspending GPS updates...");
			Utils.d("  Removing location updates...");
			lm.removeUpdates(locListener);
		}
		Round.resetLastMark();
	}

	public void onPause() {
		super.onPause();

		stopGPS();
		Utils.d("PAUSED.");
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		root = new LinearLayout(this);
		root.setOrientation(LinearLayout.VERTICAL);
		root.setLayoutParams(Utils.CONTAINER_PARAMS);

		LinearLayout header = new LinearLayout(this);
		header.setOrientation(LinearLayout.VERTICAL);
		header.setLayoutParams(Utils.CONTAINER_PARAMS);
		root.addView(header);

		TextView course = new TextView(this);
		course.setText(Round.getCourse());
		// course.setGravity(Gravity.CENTER);
		course.setTextSize(15f);
		course.setLayoutParams(Utils.WIDGET_PARAMS);
		header.addView(course);

		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.HORIZONTAL);
		ll.setLayoutParams(Utils.CONTAINER_PARAMS);

		holeField = new TextView(this);
		holeField.setText(String.valueOf(Round.getCurrentHoleIndex() + 1));
		holeField.setGravity(Gravity.RIGHT);
		holeField.setTextSize(40f);
		holeField.setLayoutParams(Utils.WIDGET_PARAMS);
		ll.addView(holeField);

		TextView holeLabel = new TextView(this);
		holeLabel.setText("Hole");
		holeLabel.setTextSize(20f);
		holeLabel.setLayoutParams(Utils.WIDGET_PARAMS);
		ll.addView(holeLabel);

		TextView parLabel = new TextView(this);
		parLabel.setText("Par");
		parLabel.setGravity(Gravity.RIGHT);
		parLabel.setTextSize(20f);
		parLabel.setLayoutParams(Utils.WIDGET_PARAMS);
		ll.addView(parLabel);

		parField = new TextView(this);
		parField.setText(String.valueOf(Round.getCurrentHolePar()));
		parField.setTextSize(40f);
		parField.setLayoutParams(Utils.WIDGET_PARAMS);
		ll.addView(parField);

		header.addView(ll);

		LinearLayout headings = new LinearLayout(this);
		headings.setOrientation(LinearLayout.HORIZONTAL);
		headings.setLayoutParams(Utils.CONTAINER_PARAMS);

		TextView text = new TextView(this);
		text.setText("Player (Course Score)");
		text.setTextSize(15f);
		text.setLayoutParams(Utils.WIDGET_PARAMS);
		headings.addView(text);
		text = new TextView(this);
		text.setText("Hole Score");
		text.setGravity(Gravity.RIGHT);
		text.setTextSize(15f);
		text.setLayoutParams(Utils.WIDGET_PARAMS);
		headings.addView(text);

		root.addView(headings);

		ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setLayoutParams(Utils.CONTAINER_PARAMS);

		final Set<String> players = Round.getPlayers();
		for (final String player : players) {
			LinearLayout llrow = new LinearLayout(this);
			llrow.setOrientation(LinearLayout.HORIZONTAL);
			llrow.setLayoutParams(Utils.CONTAINER_PARAMS);

			Button playerButton = new Button(this);
			playerButton.setLayoutParams(Utils.WIDGET_PARAMS);
			playerButton.setText(player);
			llrow.addView(playerButton);
			playerButtons.put(player, playerButton);

			OnClickListener listener = new OnClickListener() {
				public void onClick(View view) {
					selectedPlayer = player;
					showDialog(Utils.SCORE_DIALOG);
				}
			};
			playerButton.setOnClickListener(listener);

			/*
			 * Add cum-score field.
			 */
			TextView playerField = new TextView(this);
			playerField.setLayoutParams(Utils.WIDGET_PARAMS);
			playerField.setText("(?)");
			playerField.setTextSize(30f);
			playerField.setGravity(Gravity.CENTER);
			llrow.addView(playerField);
			playerFields.put(player, playerField);

			updateScore(player);

			ll.addView(llrow);
		}

		ScrollView playerScrollView = new ScrollView(this);
		playerScrollView.setLayoutParams(Utils.WIDGET_PARAMS);
		playerScrollView.setBackgroundColor(Color.DKGRAY);
		playerScrollView.addView(ll);
		root.addView(playerScrollView);

		/*
		 * Add GPS data.
		 */
		LinearLayout locbar = new LinearLayout(this);
		locbar.setOrientation(LinearLayout.HORIZONTAL);
		locbar.setLayoutParams(Utils.CONTAINER_PARAMS);
		root.addView(locbar);

		trackingField = new TextView(this);
		trackingField.setLayoutParams(Utils.WIDGET_PARAMS);
		trackingField.setText("Tee unmarked...");
		trackingField.setGravity(Gravity.CENTER);
		trackingField.setPadding(0, 5, 0, 5);
		locbar.addView(trackingField);

		LinearLayout gpsbar = new LinearLayout(this);
		gpsbar.setOrientation(LinearLayout.HORIZONTAL);
		gpsbar.setLayoutParams(Utils.CONTAINER_PARAMS);
		root.addView(gpsbar);

		locbar = new LinearLayout(this);
		locbar.setOrientation(LinearLayout.HORIZONTAL);
		locbar.setLayoutParams(Utils.CONTAINER_PARAMS);
		root.addView(locbar);

		markButton = new Button(this);
		markButton.setLayoutParams(Utils.WIDGET_PARAMS);
		markButton.setGravity(Gravity.CENTER);
		markButton.setText("Mark Shot");
		markButton.setEnabled(false);
		locbar.addView(markButton);

		markButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				/*
				 * Update tracking data.
				 */
				Round.markShot();
				trackingField.setText(Round.getTrackString());
			}
		});

		locField = new TextView(this);
		locField.setLayoutParams(Utils.WIDGET_PARAMS);
		locField.setGravity(Gravity.CENTER);
		locField.setText("(current location unknown)");
		locField.setPadding(0, 5, 0, 5);
		locbar.addView(locField);

		/*
		 * Add nav buttons.
		 */
		updateUI();

		setContentView(root);
	}

	@Override
	public void onActivityResult(int req, int result, Intent intent) {
		if (RESULT_OK == result) {
			Round.clear();

			setResult(RESULT_OK);
			finish();
		}
	}
}

// /*
// * Show map.
// */
// // Uri uri = Uri.parse(GPS.getGeoURI());
// // Intent intent = new
// // Intent(Intent.ACTION_VIEW, uri);
// // startActivity(intent);
// }
// });
