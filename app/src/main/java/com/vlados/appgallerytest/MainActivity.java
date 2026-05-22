package com.vlados.appgallerytest;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String PREFS_NAME = "daily_todo_state";
    private static final String KEY_TASKS = "tasks";
    private static final String KEY_LAST_UPDATED = "last_updated";

    private final List<TodoItem> tasks = new ArrayList<>();

    private SharedPreferences prefs;
    private EditText taskInput;
    private TextView summaryText;
    private TextView emptyText;
    private TextView updatedText;
    private LinearLayout tasksContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadTasks();

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

        TextView title = text("Daily Todo", 30, Color.rgb(23, 32, 38), true);
        TextView subtitle = text(
                "Plan your day, tick off completed tasks, and keep the checklist saved on this device.",
                17,
                Color.rgb(82, 96, 105),
                false
        );
        subtitle.setPadding(0, dp(10), 0, dp(22));
        root.addView(title);
        root.addView(subtitle);

        LinearLayout addCard = card();
        TextView addTitle = text("Add task", 18, Color.rgb(23, 32, 38), true);
        taskInput = new EditText(this);
        taskInput.setHint("Task for today");
        taskInput.setSingleLine(true);
        taskInput.setTextSize(16);
        taskInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        taskInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        taskInput.setPadding(dp(12), dp(8), dp(12), dp(8));
        taskInput.setBackground(inputBackground());
        taskInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addTaskFromInput();
                return true;
            }
            return false;
        });

        Button addButton = primaryButton("Add to today's list");
        addButton.setOnClickListener(view -> addTaskFromInput());

        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        inputParams.setMargins(0, dp(14), 0, dp(12));

        addCard.addView(addTitle);
        addCard.addView(taskInput, inputParams);
        addCard.addView(addButton, matchWidthParams());
        root.addView(addCard);

        LinearLayout listCard = card();
        TextView listTitle = text("Today's checklist", 18, Color.rgb(23, 32, 38), true);
        summaryText = text("", 15, Color.rgb(82, 96, 105), false);
        summaryText.setPadding(0, dp(8), 0, dp(12));

        emptyText = text("No tasks yet. Add your first task above.", 16, Color.rgb(82, 96, 105), false);
        emptyText.setPadding(0, dp(6), 0, dp(6));

        tasksContainer = new LinearLayout(this);
        tasksContainer.setOrientation(LinearLayout.VERTICAL);

        listCard.addView(listTitle);
        listCard.addView(summaryText);
        listCard.addView(emptyText);
        listCard.addView(tasksContainer);
        root.addView(listCard);

        LinearLayout actionsCard = card();
        TextView actionsTitle = text("List actions", 18, Color.rgb(23, 32, 38), true);
        updatedText = text("", 15, Color.rgb(82, 96, 105), false);
        updatedText.setPadding(0, dp(10), 0, dp(14));

        Button clearButton = secondaryButton("Clear completed");
        clearButton.setOnClickListener(view -> clearCompleted());

        Button resetButton = secondaryButton("Reset day");
        resetButton.setOnClickListener(view -> resetDay());

        LinearLayout.LayoutParams secondButtonParams = matchWidthParams();
        secondButtonParams.setMargins(0, dp(10), 0, 0);

        actionsCard.addView(actionsTitle);
        actionsCard.addView(updatedText);
        actionsCard.addView(clearButton, matchWidthParams());
        actionsCard.addView(resetButton, secondButtonParams);
        root.addView(actionsCard);

        setContentView(scrollView);
        updateUi();
    }

    private void addTaskFromInput() {
        String value = taskInput.getText().toString().trim();
        if (value.isEmpty()) {
            taskInput.setError("Enter a task");
            return;
        }

        tasks.add(new TodoItem(value, false));
        taskInput.setText("");
        saveTasks();
        updateUi();
    }

    private void clearCompleted() {
        for (int index = tasks.size() - 1; index >= 0; index--) {
            if (tasks.get(index).completed) {
                tasks.remove(index);
            }
        }
        saveTasks();
        updateUi();
    }

    private void resetDay() {
        tasks.clear();
        saveTasks();
        updateUi();
    }

    private void updateUi() {
        tasksContainer.removeAllViews();

        int completed = 0;
        for (TodoItem task : tasks) {
            if (task.completed) {
                completed++;
            }
        }

        summaryText.setText(completed + " of " + tasks.size() + " tasks completed.");
        emptyText.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
        updatedText.setText("Last updated: " + prefs.getString(KEY_LAST_UPDATED, "Not updated yet"));

        for (int index = 0; index < tasks.size(); index++) {
            tasksContainer.addView(taskRow(index));
        }
    }

    private LinearLayout taskRow(int index) {
        TodoItem task = tasks.get(index);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(6), 0, dp(6));

        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(task.text);
        checkBox.setTextSize(16);
        checkBox.setTextColor(Color.rgb(23, 32, 38));
        checkBox.setChecked(task.completed);
        checkBox.setPaintFlags(task.completed
                ? checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                : checkBox.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.completed = isChecked;
            saveTasks();
            updateUi();
        });

        Button deleteButton = secondaryButton("Delete");
        deleteButton.setTextSize(14);
        deleteButton.setOnClickListener(view -> {
            tasks.remove(index);
            saveTasks();
            updateUi();
        });

        LinearLayout.LayoutParams checkParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        );
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                dp(96),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        deleteParams.setMargins(dp(10), 0, 0, 0);

        row.addView(checkBox, checkParams);
        row.addView(deleteButton, deleteParams);
        return row;
    }

    private void loadTasks() {
        tasks.clear();
        String savedTasks = prefs.getString(KEY_TASKS, "[]");
        try {
            JSONArray array = new JSONArray(savedTasks);
            for (int index = 0; index < array.length(); index++) {
                JSONObject object = array.getJSONObject(index);
                String text = object.optString("text", "").trim();
                if (!text.isEmpty()) {
                    tasks.add(new TodoItem(text, object.optBoolean("completed", false)));
                }
            }
        } catch (JSONException ignored) {
            tasks.clear();
        }
    }

    private void saveTasks() {
        JSONArray array = new JSONArray();
        try {
            for (TodoItem task : tasks) {
                JSONObject object = new JSONObject();
                object.put("text", task.text);
                object.put("completed", task.completed);
                array.put(object);
            }
        } catch (JSONException ignored) {
            return;
        }

        String time = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
        prefs.edit()
                .putString(KEY_TASKS, array.toString())
                .putString(KEY_LAST_UPDATED, time)
                .apply();
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

    private Button primaryButton(String label) {
        Button button = baseButton(label);
        button.setTextColor(Color.WHITE);
        button.setBackground(buttonBackground(Color.rgb(0, 116, 111)));
        return button;
    }

    private Button secondaryButton(String label) {
        Button button = baseButton(label);
        button.setTextColor(Color.rgb(23, 32, 38));
        button.setBackground(buttonBackground(Color.rgb(224, 232, 232)));
        return button;
    }

    private Button baseButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(16);
        button.setPadding(dp(12), dp(8), dp(12), dp(8));
        return button;
    }

    private LinearLayout.LayoutParams matchWidthParams() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private GradientDrawable cardBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.WHITE);
        drawable.setCornerRadius(dp(8));
        drawable.setStroke(dp(1), Color.rgb(222, 229, 229));
        return drawable;
    }

    private GradientDrawable inputBackground() {
        GradientDrawable drawable = cardBackground();
        drawable.setColor(Color.rgb(250, 252, 252));
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

    private static class TodoItem {
        private final String text;
        private boolean completed;

        private TodoItem(String text, boolean completed) {
            this.text = text;
            this.completed = completed;
        }
    }
}
