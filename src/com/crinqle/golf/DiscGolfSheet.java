package com.crinqle.golf;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class DiscGolfSheet extends Activity {
	private LinearLayout root;

	private void init() {
		root = new LinearLayout(this);
		root.setOrientation(LinearLayout.VERTICAL);
		root.setLayoutParams(Utils.CONTAINER_PARAMS);

		TextView text = new TextView(this);
		text.setText(Round.getCourse());
		text.setGravity(Gravity.CENTER);
		text.setTextSize(30f);
		root.addView(text);

		LinearLayout scoreSheet = new LinearLayout(this);
		scoreSheet.setOrientation(LinearLayout.VERTICAL);
		scoreSheet.setLayoutParams(Utils.CONTAINER_PARAMS);

		for (String player : Round.getPlayers()) {
			Utils.d("### CrinqleSheet - player: " + player);
			final int diff = Round.getCurrentDiff(player);
			Utils.d("  # CrinqleSheet -   diff: " + diff);

			LinearLayout ll = new LinearLayout(this);
			ll.setOrientation(LinearLayout.HORIZONTAL);
			ll.setLayoutParams(Utils.CONTAINER_PARAMS);

			TextView name = new TextView(this);
			name.setLayoutParams(Utils.WIDGET_PARAMS);
			name.setText(player);
			name.setGravity(Gravity.CENTER);
			name.setTextSize(30f);

			TextView score = new TextView(this);
			score.setLayoutParams(Utils.WIDGET_PARAMS);
			score.setText(Round.getCourseDiffAsString(diff));
			score.setGravity(Gravity.CENTER);
			score.setTextSize(50f);

			ll.addView(name);
			ll.addView(score);
			scoreSheet.addView(ll);
		}

		ScrollView playerScrollView = new ScrollView(this);
		playerScrollView.setLayoutParams(Utils.WIDGET_PARAMS);
		playerScrollView.setBackgroundColor(Color.DKGRAY);
		playerScrollView.addView(scoreSheet);
		root.addView(playerScrollView);

		Button finish = new Button(this);
		finish.setText("Save Round");
		finish.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				/*
				 * Persist round data.
				 */
				Round.saveRound();

				/*
				 * End app.
				 */
				setResult(RESULT_OK);
				finish();
			}
		});

		root.addView(finish);

		setContentView(root);
	}

	@Override
	public void onResume() {
		super.onResume();
		final Resources res = getResources();
		Utils.init(this, res);

		init();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		final Resources res = getResources();
		Utils.init(this, res);

		init();
	}
}
