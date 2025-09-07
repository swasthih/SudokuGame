package com.example.sudokugame;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText[][] cells = new EditText[9][9];
    private int[][] solution;
    private int[][] currentPuzzle;
    private TextView timerText, bestTimeText;
    private Handler timerHandler = new Handler();
    private int secondsPassed = 0;
    private boolean isTimerRunning = false;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            int minutes = secondsPassed / 60;
            int seconds = secondsPassed % 60;
            timerText.setText(String.format("Time: %02d:%02d", minutes, seconds));
            if (isTimerRunning) {
                secondsPassed++;
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerText = findViewById(R.id.timerText);
        bestTimeText = findViewById(R.id.bestTimeText);

        Button startButton = findViewById(R.id.startButton);
        Button checkButton = findViewById(R.id.checkButton);
        Button restartButton = findViewById(R.id.restartButton);
        Button hintButton = findViewById(R.id.hintButton);
        Button saveButton = findViewById(R.id.saveButton);
        Button resumeButton = findViewById(R.id.resumeButton);

        startButton.setOnClickListener(v -> {
            generatePuzzleWithDifficulty();
            startTimer();
        });

        checkButton.setOnClickListener(v -> checkAnswer());
        restartButton.setOnClickListener(v -> restartGame());
        hintButton.setOnClickListener(v -> provideHint());
        saveButton.setOnClickListener(v -> saveGame());
        resumeButton.setOnClickListener(v -> resumeGame());

        loadBestTime();
    }

    private void generatePuzzleWithDifficulty() {
        RadioGroup difficultyGroup = findViewById(R.id.difficultyGroup);
        int selectedId = difficultyGroup.getCheckedRadioButtonId();

        int difficulty = 40;
        if (selectedId == R.id.medium) difficulty = 50;
        else if (selectedId == R.id.hard) difficulty = 60;

        createSudokuGrid(difficulty);
    }

    private void createSudokuGrid(int difficulty) {
        GridLayout grid = findViewById(R.id.sudokuGrid);
        grid.removeAllViews();

        SudokuGenerator generator = new SudokuGenerator();
        int[][] puzzle = generator.generate(difficulty);
        solution = generator.getSolution();
        currentPuzzle = new int[9][9];

        for (int i = 0; i < 9; i++) {
            System.arraycopy(puzzle[i], 0, currentPuzzle[i], 0, 9);
        }

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                EditText cell = new EditText(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 100;
                params.height = 100;
                params.rowSpec = GridLayout.spec(row);
                params.columnSpec = GridLayout.spec(col);
                cell.setLayoutParams(params);
                cell.setGravity(Gravity.CENTER);
                cell.setTextSize(18);
                cell.setSingleLine();
                cell.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                cell.setBackgroundResource(android.R.drawable.edit_text);

                if (puzzle[row][col] != 0) {
                    cell.setText(String.valueOf(puzzle[row][col]));
                    cell.setEnabled(false);
                    cell.setTextColor(Color.BLACK);
                } else {
                    cell.setText("");
                    cell.setTextColor(Color.BLUE);
                    int finalRow = row;
                    int finalCol = col;

                    cell.setOnFocusChangeListener((v, hasFocus) -> {
                        if (!hasFocus) {
                            String input = cell.getText().toString().trim();
                            if (!input.isEmpty()) {
                                try {
                                    int value = Integer.parseInt(input);
                                    if (value < 1 || value > 9) {
                                        showMessage("Only numbers 1 to 9 allowed");
                                        cell.setText("");
                                        return;
                                    }

                                    if (value != solution[finalRow][finalCol]) {
                                        cell.setTextColor(Color.RED);
                                        showMessage("Invalid move!");
                                    } else {
                                        cell.setTextColor(Color.BLUE);
                                    }
                                } catch (NumberFormatException e) {
                                    cell.setText("");
                                    showMessage("Enter a valid number");
                                }
                            }
                        }
                    });
                }

                grid.addView(cell);
                cells[row][col] = cell;
            }
        }
    }

    private void checkAnswer() {
        boolean isCorrect = true;

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                EditText cell = cells[row][col];
                String input = cell.getText().toString().trim();

                if (!input.isEmpty()) {
                    int value = Integer.parseInt(input);
                    if (value != solution[row][col]) {
                        isCorrect = false;
                        cell.setTextColor(Color.RED);
                    } else {
                        cell.setTextColor(Color.BLUE);
                    }
                } else {
                    isCorrect = false;
                }
            }
        }

        if (isCorrect) {
            stopTimer();
            updateBestTime();
            showEndDialog("🎉 Congratulations! You solved the puzzle.");
        } else {
            showMessage("❌ Some values are incorrect. Try again.");
        }
    }

    private void provideHint() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                EditText cell = cells[row][col];
                if (cell.isEnabled() && cell.getText().toString().isEmpty()) {
                    cell.setText(String.valueOf(solution[row][col]));
                    cell.setTextColor(Color.MAGENTA);
                    cell.setEnabled(false);
                    return;
                }
            }
        }
    }

    private void saveGame() {
        SharedPreferences prefs = getSharedPreferences("SudokuPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                String val = cells[row][col].getText().toString();
                sb.append(val.isEmpty() ? "0" : val).append(",");
            }
        }

        editor.putString("saved_game", sb.toString());
        editor.putInt("saved_time", secondsPassed);
        editor.apply();

        showMessage("✅ Game saved.");
    }

    private void resumeGame() {
        SharedPreferences prefs = getSharedPreferences("SudokuPrefs", MODE_PRIVATE);
        String saved = prefs.getString("saved_game", "");
        secondsPassed = prefs.getInt("saved_time", 0);

        if (saved.isEmpty()) {
            showMessage("⚠️ No saved game.");
            return;
        }

        generatePuzzleWithDifficulty();
        String[] values = saved.split(",");
        int index = 0;

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (cells[row][col].isEnabled()) {
                    String val = values[index++];
                    if (!val.equals("0")) {
                        cells[row][col].setText(val);
                    } else {
                        cells[row][col].setText("");
                    }
                } else {
                    index++;
                }
            }
        }

        startTimer();
        showMessage("⏳ Game resumed.");
    }

    private void restartGame() {
        stopTimer();
        generatePuzzleWithDifficulty();
        startTimer();
    }

    private void startTimer() {
        stopTimer();
        isTimerRunning = true;
        timerHandler.post(timerRunnable);
    }

    private void stopTimer() {
        isTimerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void updateBestTime() {
        SharedPreferences prefs = getSharedPreferences("SudokuPrefs", MODE_PRIVATE);
        int bestTime = prefs.getInt("best_time", Integer.MAX_VALUE);
        if (secondsPassed < bestTime) {
            prefs.edit().putInt("best_time", secondsPassed).apply();
            bestTimeText.setText("Best Time: " + formatTime(secondsPassed));
        }
    }

    private void loadBestTime() {
        SharedPreferences prefs = getSharedPreferences("SudokuPrefs", MODE_PRIVATE);
        int bestTime = prefs.getInt("best_time", 0);
        if (bestTime > 0) {
            bestTimeText.setText("Best Time: " + formatTime(bestTime));
        }
    }

    private String formatTime(int time) {
        return String.format("%02d:%02d", time / 60, time % 60);
    }

    private void showEndDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
