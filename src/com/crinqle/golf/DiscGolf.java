package com.crinqle.golf;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class DiscGolf extends Activity {
	private LinearLayout root;
	private Button startButton = null;
	private LinearLayout userGrid;
	private LinearLayout currentUserRow;
	private Set<ToggleButton> playerButtons = new HashSet<ToggleButton>();

	private String selectedCourse;

	private void addNewPlayer(final String player) {
		ToggleButton newPlayer = new ToggleButton(this);
		playerButtons.add(newPlayer);
		newPlayer.setLayoutParams(Utils.WIDGET_PARAMS);
		newPlayer.setText(player + "?");
		newPlayer.setTextOff(player + "?");
		newPlayer.setTextOn(player);

		if (Round.isPlaying(player)) {
			newPlayer.setChecked(true);
			startButton.setEnabled(true);
			startButton.setText("Resume Round...");
		}

		OnCheckedChangeListener listener = new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton b, boolean isChecked) {
				if (isChecked) {
					Round.addPlayer(player);
				} else {
					Round.removePlayer(player);
				}
				if (0 < Round.getPlayerCount()) {
					startButton.setEnabled(true);
				} else {
					startButton.setEnabled(false);
				}
			}
		};
		newPlayer.setOnCheckedChangeListener(listener);

		/*
		 * userGrid.addView(newPlayer);
		 */

		if (null == currentUserRow) {
			currentUserRow = new LinearLayout(this);
			currentUserRow.setOrientation(LinearLayout.HORIZONTAL);
			currentUserRow.setLayoutParams(Utils.CONTAINER_PARAMS);
			userGrid.addView(currentUserRow);
			currentUserRow.addView(newPlayer);
		} else {
			currentUserRow.addView(newPlayer);
			currentUserRow = null;
		}
	}

	@Override
	public void onPrepareDialog(int id, Dialog d) {
		switch (id) {
		case Utils.NEW_PLAYER_DIALOG:
			d.setTitle("Add player");
			break;
		}
	}

	@Override
	public Dialog onCreateDialog(int id) {
		switch (id) {
		case Utils.NEW_PLAYER_DIALOG:
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
			 * TextView label = new TextView(this); //
			 * label.setLayoutParams(Utils.WIDGET_PARAMS);
			 * label.setText("New Player Name:");
			 * dv.addView(label);
			 */

			final EditText field = new EditText(this);
			// field.setLayoutParams(Utils.WIDGET_PARAMS);
			dv.addView(field);

			LinearLayout buttonBox = new LinearLayout(this);
			buttonBox.setOrientation(LinearLayout.HORIZONTAL);
			buttonBox.setLayoutParams(Utils.CONTAINER_PARAMS);
			dv.addView(buttonBox);

			Button b = new Button(this);
			// b.setLayoutParams(Utils.WIDGET_PARAMS);
			b.setText("Cancel");
			buttonBox.addView(b);

			b.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					d.dismiss();
				}
			});

			b = new Button(this);
			// b.setLayoutParams(Utils.WIDGET_PARAMS);
			b.setText("Add");
			buttonBox.addView(b);

			b.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					final String player = field.getText().toString();
					Utils.addAllPlayer(player);
					addNewPlayer(player);
					d.dismiss();
				}
			});

			/*
			 * Finalize dialog.
			 */
			d.setContentView(dv);
			return d;

		default:
			return null;
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		Utils.save();
	}

	/*
	 * @Override
	 * public void onResume() {
	 * super.onResume();
	 * 
	 * Utils.init(this, getResources());
	 * Utils.v("DiscGolf.onResume()");
	 * if (Round.load(Utils.getRoundFile())) {
	 * Utils.d("Round data loaded...skipping DiscGolf init...");
	 * 
	 * Intent myIntent = new Intent(this, CrinqleScores.class);
	 * startActivityForResult(myIntent, 0);
	 * 
	 * Utils.v("Should not see this...");
	 * }
	 * 
	 * Utils.load();
	 * Utils.v("  If round data is loaded, we should not be here...");
	 * Utils.d("  Initializing DiscGolf UI...");
	 * }
	 */

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		Utils.init(this, getResources());
		Utils.v("DiscGolf.onCreate()");
		if (Round.load(Utils.getRoundFile())) {
			Utils.d("Round data loaded...skipping DiscGolf init...");

			Intent myIntent = new Intent(this, DiscGolfScores.class);
			startActivityForResult(myIntent, 0);

			Utils.v("Should not see this...");
		}

		Utils.load();
		Utils.v("  If round data is loaded, we should not be here...");
		Utils.d("  Initializing DiscGolf UI...");

		root = new LinearLayout(this);
		root.setOrientation(LinearLayout.VERTICAL);
		root.setLayoutParams(Utils.CONTAINER_PARAMS);

		OnItemSelectedListener courseListener = new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapter, View view, int pos, long id) {
				final String course = adapter.getItemAtPosition(pos).toString();
				selectedCourse = course;
			}

			public void onNothingSelected(AdapterView<?> adapter) {
			}
		};

		Spinner courseChooser = new Spinner(this);
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		final Set<String> courses = Utils.getCourses();
		for (String course : courses) {
			adapter.add(course);
		}

		courseChooser.setAdapter(adapter);
		courseChooser.setOnItemSelectedListener(courseListener);
		root.addView(courseChooser);

		userGrid = new LinearLayout(this);
		userGrid.setOrientation(LinearLayout.VERTICAL);
		userGrid.setLayoutParams(Utils.CONTAINER_PARAMS);

		startButton = new Button(this);
		startButton.setEnabled(false);
		startButton.setText("Start Round");
		startButton.setLayoutParams(Utils.WIDGET_PARAMS);

		startButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Round.start(selectedCourse, Utils.getRoundFile());

				Intent myIntent = new Intent(view.getContext(), DiscGolfScores.class);
				startActivityForResult(myIntent, 0);
			}
		});

		final Set<String> allPlayers = Utils.getAllPlayers();
		for (String player : allPlayers) {
			addNewPlayer(player);
		}

		ScrollView userScrollView = new ScrollView(this);
		userScrollView.setLayoutParams(Utils.WIDGET_PARAMS);
		userScrollView.addView(userGrid);
		root.addView(userScrollView);

		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.HORIZONTAL);
		ll.setLayoutParams(Utils.CONTAINER_PARAMS);
		root.addView(ll);

		Button b = new Button(this);
		b.setText("Add Player");
		b.setLayoutParams(Utils.WIDGET_PARAMS);
		ll.addView(b);

		b.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				showDialog(Utils.NEW_PLAYER_DIALOG);
			}
		});

		ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.HORIZONTAL);
		ll.setLayoutParams(Utils.CONTAINER_PARAMS);
		root.addView(ll);

		ll.addView(startButton);

		setContentView(root);
	}

	@Override
	public void onActivityResult(int req, int result, Intent intent) {
		/*
		 * Do some cleanup here.
		 */
		if (RESULT_OK == result) {
			for (ToggleButton b : playerButtons) {
				b.setChecked(false);
			}
			finish();
		}
	}
}