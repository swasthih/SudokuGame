package com.example.sudokugame;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class GameActivity extends AppCompatActivity {

    public static final String EXTRA_DIFFICULTY = "EXTRA_DIFFICULTY";

    private GridLayout sudokuGrid;
    private EditText[][] cells = new EditText[9][9];
    private int[][] solution = new int[9][9];
    private int[][] puzzle = new int[9][9];

    private Button buttonCheck, buttonHint;
    private TextView timerText;
    private CountDownTimer timer;
    private long timeLeft = 600000; // 10 minutes for demo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        String difficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);

        sudokuGrid = findViewById(R.id.sudokuGrid);
        sudokuGrid.setColumnCount(9);
        sudokuGrid.setRowCount(9);

        buttonCheck = findViewById(R.id.buttonCheck);
        buttonHint = findViewById(R.id.buttonHint);
        timerText = findViewById(R.id.timerText);

        generatePuzzle(difficulty);
        createSudokuGrid();
        startTimer();

        buttonCheck.setOnClickListener(v -> checkSolution());
        buttonHint.setOnClickListener(v -> giveHint());
    }

    /** Timer setup **/
    private void startTimer() {
        timer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                int minutes = (int) (timeLeft / 1000) / 60;
                int seconds = (int) (timeLeft / 1000) % 60;
                timerText.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                Toast.makeText(GameActivity.this, "Time Over!", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    /** Generate Sudoku puzzle **/
    private void generatePuzzle(String difficulty) {
        int clues;
        switch (difficulty) {
            case "EASY": clues = 40; break;
            case "MEDIUM": clues = 30; break;
            case "HARD": clues = 20; break;
            default: clues = 30;
        }

        // Simple random puzzle generation for demo
        solution = new int[9][9];
        Random random = new Random();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                solution[i][j] = 0;
            }
        }

        for (int i = 0; i < clues; i++) {
            int row, col, num;
            do {
                row = random.nextInt(9);
                col = random.nextInt(9);
                num = random.nextInt(9) + 1;
            } while (solution[row][col] != 0 || !isSafe(row, col, num));

            solution[row][col] = num;
        }

        // Copy puzzle to display, hide some numbers for gameplay
        puzzle = new int[9][9];
        for (int i = 0; i < 9; i++)
            System.arraycopy(solution[i], 0, puzzle[i], 0, 9);
    }

    private boolean isSafe(int row, int col, int num) {
        // Row and column check
        for (int i = 0; i < 9; i++) {
            if (solution[row][i] == num) return false;
            if (solution[i][col] == num) return false;
        }
        // 3x3 box
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int i = startRow; i < startRow + 3; i++)
            for (int j = startCol; j < startCol + 3; j++)
                if (solution[i][j] == num) return false;
        return true;
    }

    /** Create 9x9 grid **/
    private void createSudokuGrid() {
        int size = getResources().getDisplayMetrics().widthPixels / 9;

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                EditText cell = new EditText(this);
                cell.setWidth(size);
                cell.setHeight(size);
                cell.setGravity(Gravity.CENTER);
                cell.setTextSize(18);
                cell.setBackgroundColor((row / 3 + col / 3) % 2 == 0 ? Color.parseColor("#AED581") : Color.parseColor("#81C784"));
                cell.setInputType(InputType.TYPE_CLASS_NUMBER);
                cell.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});

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
                        if (!hasFocus) validateCell(finalRow, finalCol);
                    });
                }

                cells[row][col] = cell;
                sudokuGrid.addView(cell);
            }
        }
    }

    /** Validate cell input **/
    private void validateCell(int row, int col) {
        String text = cells[row][col].getText().toString();
        if (text.isEmpty()) return;
        int num = Integer.parseInt(text);
        if (!isSafeForUser(row, col, num)) {
            cells[row][col].setTextColor(Color.RED);
            Toast.makeText(this, "Wrong number!", Toast.LENGTH_SHORT).show();
        } else {
            cells[row][col].setTextColor(Color.BLUE);
        }
    }

    private boolean isSafeForUser(int row, int col, int num) {
        // Row
        for (int i = 0; i < 9; i++) if (i != col && getCellValue(row, i) == num) return false;
        // Column
        for (int i = 0; i < 9; i++) if (i != row && getCellValue(i, col) == num) return false;
        // Box
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int i = startRow; i < startRow + 3; i++)
            for (int j = startCol; j < startCol + 3; j++)
                if ((i != row || j != col) && getCellValue(i, j) == num) return false;
        return true;
    }

    private int getCellValue(int row, int col) {
        String text = cells[row][col].getText().toString();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    /** Check entire solution **/
    private void checkSolution() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                String val = cells[row][col].getText().toString();
                int num = val.isEmpty() ? 0 : Integer.parseInt(val);
                if (num != solution[row][col]) {
                    Toast.makeText(this, "Solution Incorrect!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        Toast.makeText(this, "Congratulations! You solved it!", Toast.LENGTH_LONG).show();
        timer.cancel();
    }

    /** Fill one empty cell as hint **/
    private void giveHint() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (cells[row][col].getText().toString().isEmpty()) {
                    cells[row][col].setText(String.valueOf(solution[row][col]));
                    cells[row][col].setTextColor(Color.MAGENTA);
                    return;
                }
            }
        }
        Toast.makeText(this, "No empty cells left!", Toast.LENGTH_SHORT).show();
    }
}
