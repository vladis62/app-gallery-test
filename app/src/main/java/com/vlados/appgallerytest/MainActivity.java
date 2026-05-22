package com.vlados.appgallerytest;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String PREFS_NAME = "release_test_state";
    private static final String KEY_INSTALL = "install_checked";
    private static final String KEY_ACTION = "action_checked";
    private static final String KEY_STATE = "state_checked";
    private static final String KEY_RUN_COUNT = "run_count";
    private static final String KEY_LAST_RUN = "last_run";

    private SharedPreferences prefs;
    private TextView statusValue;
    private TextView checklistSummary;
    private TextView runCountValue;
    private CheckBox installCheck;
    private CheckBox actionCheck;
    private CheckBox stateCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.rgb(245, 248, 248));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(32), dp(24), dp(32));
        scrollView.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView title = text("Release Test", 30, Color.rgb(23, 32, 38), true);
        TextView subtitle = text(
                "A local checklist for checking install, launch, and basic interaction flow.",
                17,
                Color.rgb(82, 96, 105),
                false
        );
        subtitle.setPadding(0, dp(10), 0, dp(22));

        root.addView(title);
        root.addView(subtitle);

        LinearLayout statusCard = card();
        TextView statusTitle = text("Launch check", 18, Color.rgb(23, 32, 38), true);
        statusValue = text("", 16, Color.rgb(0, 116, 111), false);
        statusValue.setPadding(0, dp(10), 0, dp(14));

        Button runButton = new Button(this);
        runButton.setText("Run launch check");
        runButton.setAllCaps(false);
        runButton.setTextSize(16);
        runButton.setTextColor(Color.WHITE);
        runButton.setBackground(buttonBackground(Color.rgb(0, 116, 111)));
        runButton.setPadding(dp(14), dp(10), dp(14), dp(10));
        runButton.setOnClickListener(view -> runLaunchCheck());

        statusCard.addView(statusTitle);
        statusCard.addView(statusValue);
        statusCard.addView(runButton, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        root.addView(statusCard);

        LinearLayout checklistCard = card();
        TextView checklistTitle = text("Release checklist", 18, Color.rgb(23, 32, 38), true);
        checklistSummary = text("", 15, Color.rgb(82, 96, 105), false);
        checklistSummary.setPadding(0, dp(8), 0, dp(12));

        installCheck = checkbox("App opens from launcher", KEY_INSTALL);
        actionCheck = checkbox("Main action button responds", KEY_ACTION);
        stateCheck = checkbox("Checklist state is saved locally", KEY_STATE);

        checklistCard.addView(checklistTitle);
        checklistCard.addView(checklistSummary);
        checklistCard.addView(installCheck);
        checklistCard.addView(actionCheck);
        checklistCard.addView(stateCheck);
        root.addView(checklistCard);

        LinearLayout detailsCard = card();
        TextView detailsTitle = text("Test details", 18, Color.rgb(23, 32, 38), true);
        runCountValue = text("", 15, Color.rgb(82, 96, 105), false);
        runCountValue.setPadding(0, dp(10), 0, dp(14));

        Button resetButton = new Button(this);
        resetButton.setText("Reset checklist");
        resetButton.setAllCaps(false);
        resetButton.setTextSize(16);
        resetButton.setTextColor(Color.rgb(23, 32, 38));
        resetButton.setBackground(buttonBackground(Color.rgb(224, 232, 232)));
        resetButton.setPadding(dp(14), dp(10), dp(14), dp(10));
        resetButton.setOnClickListener(view -> resetState());

        detailsCard.addView(detailsTitle);
        detailsCard.addView(runCountValue);
        detailsCard.addView(resetButton, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        root.addView(detailsCard);

        updateUi();
        setContentView(scrollView);
    }

    private void runLaunchCheck() {
        int nextCount = prefs.getInt(KEY_RUN_COUNT, 0) + 1;
        String time = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
        prefs.edit()
                .putBoolean(KEY_ACTION, true)
                .putInt(KEY_RUN_COUNT, nextCount)
                .putString(KEY_LAST_RUN, time)
                .apply();
        actionCheck.setChecked(true);
        updateUi();
    }

    private void resetState() {
        prefs.edit().clear().apply();
        installCheck.setChecked(false);
        actionCheck.setChecked(false);
        stateCheck.setChecked(false);
        updateUi();
    }

    private CheckBox checkbox(String label, String key) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(label);
        checkBox.setTextSize(16);
        checkBox.setTextColor(Color.rgb(23, 32, 38));
        checkBox.setPadding(0, dp(6), 0, dp(6));
        checkBox.setChecked(prefs.getBoolean(key, false));
        checkBox.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            prefs.edit().putBoolean(key, isChecked).apply();
            updateUi();
        });
        return checkBox;
    }

    private void updateUi() {
        int completed = 0;
        if (installCheck != null && installCheck.isChecked()) {
            completed++;
        }
        if (actionCheck != null && actionCheck.isChecked()) {
            completed++;
        }
        if (stateCheck != null && stateCheck.isChecked()) {
            completed++;
        }

        int runCount = prefs.getInt(KEY_RUN_COUNT, 0);
        String lastRun = prefs.getString(KEY_LAST_RUN, "Not run yet");
        if (runCount == 0) {
            statusValue.setText("Ready. Tap the button to verify that the app responds.");
        } else {
            statusValue.setText("Check completed successfully at " + lastRun + ".");
        }
        checklistSummary.setText(completed + " of 3 checks completed.");
        runCountValue.setText("Launch checks completed: " + runCount + "\nLast result: " + lastRun);
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(cardBackground());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(16));
        card.setLayoutParams(params);
        return card;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView textView = new TextView(this);
        textView.setText(value);
        textView.setTextSize(size);
        textView.setTextColor(color);
        textView.setLineSpacing(dp(2), 1.0f);
        if (bold) {
            textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return textView;
    }

    private GradientDrawable cardBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.WHITE);
        drawable.setCornerRadius(dp(8));
        drawable.setStroke(dp(1), Color.rgb(222, 229, 229));
        return drawable;
    }

    private GradientDrawable buttonBackground(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(6));
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
